package ru.johnlife.lifetoolsmp3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener.State;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

public class PlaybackService  extends Service implements Constants, OnCompletionListener, OnErrorListener, OnPreparedListener, Handler.Callback {
	
	//constants section
	private static final int SMODE_GET_URL = 0x00000001;
	private static final int SMODE_PREPARED = 0x00000002;
	/**
	 * bit 1 (true) - play, bit 0 (false) - pause
	 */
	private static final int SMODE_PLAYING = 0x00000004;
	private static final int SMODE_PAUSE = 0x00000008;
	private static final int SMODE_SHUFFLE = 0x00000010;
	private static final int SMODE_REPEAT = 0x00000020;
	private static final int SMODE_START_PREPARE = 0x00000040;
	private static final int SMODE_DISABLED_DURING_CALL = 0x00000080;
	private static final int SMODE_UNPLUG_HEADPHONES = 0x00000100;
	private static final int SMODE_CALL_RINGING = 0x00000200;
	private static final int SMODE_NOTIFICATION = 0x00000400;
	private static final int SMODE_STOP = 0x00000800;
	private static final int MSG_START = 1;
	private static final int MSG_PLAY = 2;
	private static final int MSG_PAUSE = 3;
	private static final int MSG_SEEK_TO = 4;
	private static final int MSG_ERROR = 5;
	private static final int MSG_RESET = 6;
	private static final int MSG_STOP = 7;
	private static final int MSG_SHIFT = 8;
	private static final int UPDATE_DELAY = 250;
	
	//multy-threading section
	private static final Object LOCK = new Object();
	protected static final Object WAIT = new Object();
	private Looper looper;
	private Handler handler;
	
	//instance section
	private Context activityContext;
	private ArrayList<AbstractSong> arrayPlayback;
	private ArrayList<AbstractSong> arrayPlaybackOriginal;
	private ArrayList<OnStatePlayerListener> stateListeners = new ArrayList<OnStatePlayerListener>();
	private OnPlaybackServiceDestroyListener destroyListener;
	private OnErrorListener errorListener;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private MediaPlayer player;
	private static PlaybackService instance;
	private AbstractSong previousSong;
	private AbstractSong playingSong;
	private PlayerStateUpdater stateUpdater = new PlayerStateUpdater();
	private double bufferingPercent;
	private int mode;
	
	public interface OnStatePlayerListener {
		
		public enum State {
			START, PLAY, PAUSE, STOP, ERROR, UPDATE
		}
		
		public void start(AbstractSong song);
		public void play(AbstractSong song);
		public void	pause(AbstractSong song);
		public void stop (AbstractSong song);
		public void stopPressed();
		public void onTrackTimeChanged(int time, boolean isOverBuffer);	//TRUE - if overbuffered
		public void onBufferingUpdate(double percent);
		public void update (AbstractSong song);
		public void	error ();
			
	}
	
	public interface OnPlaybackServiceDestroyListener {
		public void playbackServiceIsDestroyed();
	}
	
	public interface OnErrorListener {
		public void error(String error);
	}
	
	private class HeadsetIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
				buildSendMessage(playingSong, MSG_PAUSE, 0, 0);
				mode |= SMODE_UNPLUG_HEADPHONES;
				if ((mode & SMODE_CALL_RINGING) == SMODE_CALL_RINGING){
					mode |= SMODE_DISABLED_DURING_CALL;
				}
			}
		}
		
	};
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				if (check(SMODE_PLAYING)) {
					buildSendMessage(playingSong, MSG_PAUSE, 0, 0);
					mode |= SMODE_CALL_RINGING;
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				boolean isAfterCall = (mode & SMODE_CALL_RINGING) == SMODE_CALL_RINGING;
				boolean disabledDuringCall = (mode & SMODE_DISABLED_DURING_CALL) == SMODE_DISABLED_DURING_CALL;
				boolean unplugin = (mode & SMODE_UNPLUG_HEADPHONES) == SMODE_UNPLUG_HEADPHONES;
				if (isAfterCall && !disabledDuringCall && !unplugin) {
					buildSendMessage(playingSong, MSG_PLAY, 0, 0);
					mode &= ~SMODE_CALL_RINGING;
					mode &= ~SMODE_DISABLED_DURING_CALL;
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	
	private class PlayerStateUpdater extends Timer {
		
		public void startUpdating() {
			scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					if (!player.isPlaying() || null == playingSong) {
						cancel();
						return;
					}
					for (OnStatePlayerListener listener : stateListeners) {
						listener.onTrackTimeChanged(player.getCurrentPosition(), 
								player.getCurrentPosition() > playingSong.getDuration() * bufferingPercent);
					}
				}
			}, 0, UPDATE_DELAY);
		}
	}
	
	public static PlaybackService get(final Context context) {
		if (instance == null) {
			context.startService(new Intent(context, PlaybackService.class));					
			try {
				synchronized (WAIT) {
					WAIT.wait();
				}
			} catch (InterruptedException ignored) {
			}
		}
		instance.activityContext = context;
		return instance;
	}
	
	/**
	 * Returns true if a Player service instance is active.
	 */
	public static boolean hasInstance()	{
		return instance != null;
	}
	
	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		headsetReceiver = new HeadsetIntentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(headsetReceiver, filter);
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		player.setOnBufferingUpdateListener(bufferingUpdateListener);
		looper = thread.getLooper();
		handler = new Handler(looper, this);
		instance = this;
		showNotification(true);
		synchronized (WAIT) {
			WAIT.notifyAll();
		}
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(headsetReceiver);
		if (null != destroyListener) {
			destroyListener.playbackServiceIsDestroyed();
		}
		handler.removeCallbacksAndMessages(null);
		player.release();
		looper.quit();
		removeNotification();
		super.onDestroy();
	}
	
	public void seekTo(int progress) {
		buildSendMessage(null, MSG_SEEK_TO, progress, 0);
	}
	
	public void reset() {
		playingSong = null;
		handler.removeCallbacksAndMessages(null);
		mode &= ~SMODE_PREPARED;
		buildSendMessage(null, MSG_RESET, 0, 0);
	}
	
	public void remove(AbstractSong song) {
		if (null == arrayPlayback || arrayPlayback.isEmpty()) {
			return;
		}
		if (song.equals(playingSong)) {
			int pos = arrayPlayback.indexOf(playingSong);
			arrayPlayback.remove(song);
			if (arrayPlayback.isEmpty()) {
				stopPressed();
				return;
			}
			if (pos >= 0) {
				if (pos == 0) playingSong = arrayPlayback.get(pos);
				else if (pos >= arrayPlayback.size() ) playingSong = arrayPlayback.get(pos - 1);
				else playingSong = arrayPlayback.get(pos);
				if (check(SMODE_PLAYING)) {
					shift(0);
				} else if (check(SMODE_PAUSE)) {
					reset();
				}
			}
		} else {
			arrayPlayback.remove(song);
		}
		if (arrayPlayback.isEmpty()) {
			buildSendMessage(null, MSG_RESET, 0, 0);
		}
	}
	
	public void update(String title, String artist, String path){
		if (playingSong.getClass() == MusicData.class){
			playingSong.setArtist(artist);
			playingSong.setTitle(title);
			playingSong.setPath(path);
		} else {
			playingSong.setArtist(artist);
			playingSong.setTitle(title);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_START:
			synchronized (LOCK) {
				AbstractSong s = (AbstractSong) msg.obj;
				if (check(SMODE_PREPARED) || check(SMODE_START_PREPARE)) {
					player.reset();
					offMode(SMODE_PREPARED);
				}
				if (check(SMODE_GET_URL)) {
					mode &= ~SMODE_GET_URL;
				}
				try {
					Uri uri = Uri.parse(s.getClass() == MusicData.class ? s.getPath() : s.getComment());
					player.setDataSource(this, uri);
					mode |= SMODE_START_PREPARE;
					player.prepareAsync();
					sendNotification(true, s.getCover(this));
				} catch (Exception e) {
					android.util.Log.e(getClass().getName(), "in method \"hanleMessage\" appear problem: " + e.toString());
					if(e.toString().contains("setDataSourceFD failed") && null !=errorListener) {
						errorListener.error(getString(R.string.does_not_support_type));
					}
				}
			}
			break;
		case MSG_PLAY:
			if (check(SMODE_PREPARED)) {
				AbstractSong s = (AbstractSong) msg.obj;
				helper(State.PLAY, s);
				player.start();
				onMode(SMODE_PLAYING);
				sendNotification(true, s.getCover(this));
			}
			break;
		case MSG_PAUSE:
			if (check(SMODE_PREPARED)) {
				AbstractSong s = (AbstractSong) msg.obj;
				helper(State.PAUSE, s);
				player.pause();
				mode |= SMODE_PAUSE;
				sendNotification(false, s.getCover(this));
			}
			break;
		case MSG_SEEK_TO:
			if(check(SMODE_PREPARED)){
				player.seekTo(msg.arg1);
			}
			break;
		case MSG_ERROR:
			offMode(SMODE_PREPARED);
			helper(State.ERROR, (AbstractSong) msg.obj);
			player.release();
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			player.setOnPreparedListener(this);
			break;
		case MSG_RESET:
			offMode(SMODE_PREPARED);
			player.reset();
			removeNotification();
			break;
		case MSG_STOP:
			if (check(SMODE_PREPARED)) {
				player.pause();
				player.seekTo(0);
				removeNotification();
			}
			break;
		case MSG_SHIFT:
			AbstractSong s = (AbstractSong) msg.obj;
			helper(State.UPDATE, s);
			if (enabledRepeat()) {
				buildSendMessage(s, MSG_START, 0, 0);
				break;
			}
			play(s.getClass() != MusicData.class);
			sendNotification(true, s.getCover(this));
			break;
		default:
			Log.d(getClass().getName(), "invalid message send from Handler, what = " + msg.what);
			break;
		}
		return true;
	}
	
	public void shift(int delta) {
		if (null == arrayPlayback || arrayPlayback.isEmpty()) return;
		int position = arrayPlayback.indexOf(playingSong);
		position += delta;
		if (position >= arrayPlayback.size()) {
			position = 0;
		} else if (position < 0) {
			position = arrayPlayback.size() - 1;
		}
		previousSong = playingSong;
		playingSong = arrayPlayback.get(position);
		handler.removeMessages(0);
		buildSendMessage(playingSong, MSG_SHIFT, position, 0);
	}

	public void play(AbstractSong song) {
		if (arrayPlayback == null || arrayPlayback.indexOf(song) == -1) {
			return;
		}
		int position = arrayPlayback.indexOf(song);
		if (null != playingSong) {
			previousSong = playingSong;
			if (!playingSong.equals(song)) {
				if (playingSong.getClass() != MusicData.class) {
					((RemoteSong) playingSong).cancelTasks();
				}
				reset();
			}
		}
		playingSong = arrayPlayback.get(position);
		if (check(SMODE_PREPARED)) {
			int msg;
			if (check(SMODE_PAUSE)) {
				msg = MSG_PLAY;
				onMode(SMODE_PLAYING);
			} else {
				msg = MSG_PAUSE;
				onMode(SMODE_PAUSE);
			}
			buildSendMessage(playingSong, msg, 0, 0);
		} else {
			play(playingSong.getClass() != MusicData.class);
			if (null != previousSong && playingSong != previousSong) {
				helper(State.STOP, previousSong);
			}
		}
	}
	
	public void play() {
		previousSong = playingSong;
		if (check(SMODE_PREPARED)) {
			helper(State.START, playingSong);
			buildSendMessage(playingSong, MSG_PLAY, 0, 0);
		} else {
			helper(State.STOP, previousSong == null ? playingSong : previousSong);
			stopForeground(true);
		}
	}
	
	public void stop() {
		synchronized (LOCK) {
			reset();
			onMode(SMODE_STOP);
		}
		helper(State.STOP, previousSong == null ? playingSong : previousSong);
		removeNotification();
	}
	
	public void stopPressed() {
		stop();
		try {
			for (OnStatePlayerListener listener : stateListeners) {
				listener.stopPressed();
			}
		} catch (Exception e) {
			Log.i(getClass().getSimpleName(), "Application is finished, service stoped force");
		}
		offMode(SMODE_PAUSE);	
	}
	
	public boolean offOnShuffle() {
		mode ^= SMODE_SHUFFLE;
		boolean result = enabledShuffle();
		if (result) {
			arrayPlaybackOriginal = new ArrayList<AbstractSong>();
			try {
				for (AbstractSong song : arrayPlayback) {
					arrayPlaybackOriginal.add(song.cloneSong());
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			Collections.shuffle(arrayPlayback);
		} else {
			arrayPlayback = new ArrayList<AbstractSong>(arrayPlaybackOriginal);
		}
		return result;
	}
	
	public boolean offOnRepeat(){
		mode ^= SMODE_REPEAT;
		return (mode & SMODE_REPEAT) == SMODE_REPEAT;
	}
	
	private void play(boolean fromInternet) {
		if (fromInternet) {
			onMode(SMODE_GET_URL);
			((RemoteSong) playingSong).getDownloadUrl(new DownloadUrlListener() {

				@Override
				public void success(String url) {
					if (playingSong.getClass() == MusicData.class) return;
					((RemoteSong) playingSong).setDownloadUrl(url);
					mode &= ~SMODE_GET_URL;
					buildSendMessage(playingSong, MSG_START, 0, 0);
				}

				@Override
				public void error(String error) {
					mode &= ~SMODE_GET_URL;
					if (null != errorListener) {
						errorListener.error(error);
					}
				}
			});
			return;
		}
		buildSendMessage(playingSong, MSG_START, 0, 0);
	}
	
	public void pause() {
		if (!isPlaying()) return;
		play(playingSong);
	}
	
	private void helper(final State state, final AbstractSong targetSong) {
		if (stateListeners == null) {
			return;
		}
		Handler h = new Handler(getMainLooper());
		for (final OnStatePlayerListener stateListener : stateListeners) {
			h.post(new Runnable() {

				@Override
				public void run() {
					switch (state) {
					case START:
						stateListener.start(targetSong);
						stateUpdater.startUpdating();
						break;
					case PLAY:
						stateListener.play(targetSong);
						break;
					case PAUSE:
						stateListener.pause(targetSong);
						break;
					case STOP:
						stateListener.stop(targetSong);
						break;
					case ERROR:
						stateListener.error();
						break;
					case UPDATE:
						stateListener.update(targetSong);
						break;
					}
				}
			});
		}
	}

	private void offMode(int flag) {
		mode &= ~flag;
		if (flag == SMODE_PREPARED) {
			mode &= ~SMODE_PLAYING;
			mode &= ~SMODE_PAUSE;
			mode &= ~SMODE_START_PREPARE;
		}
	}
	
	private void onMode(int flag) {
		mode |= flag;
		if (flag == SMODE_PREPARED) {
			offMode(SMODE_START_PREPARE);
			offMode(SMODE_STOP);
		} else if (flag == SMODE_PLAYING) {
			mode &= ~SMODE_PAUSE;
		} else if (flag == SMODE_PAUSE){
			mode &= ~SMODE_PLAYING;
		} else if (flag == SMODE_START_PREPARE) {
			offMode(SMODE_PREPARED);
		}
	}
	
	private boolean check(int flag) {
		return (mode & flag) == flag;
	}
	
	private void buildSendMessage(Object obj, int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.obj = obj;
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		buildSendMessage(playingSong, MSG_ERROR, what, extra);
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		if (enabledRepeat()) {
			shift(0);
		} else {
			shift(1);
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		synchronized (LOCK) {
			onMode(SMODE_PREPARED);
			onMode(SMODE_PLAYING);
			helper(State.START, playingSong);
			mp.start();
			if ((mode & SMODE_UNPLUG_HEADPHONES) == SMODE_UNPLUG_HEADPHONES) {
				buildSendMessage(playingSong, MSG_PAUSE, 0, 0);
				mode &= ~SMODE_UNPLUG_HEADPHONES;
			}
		}
	}
	
	public int getCurrentPosition() {
		synchronized (LOCK) {
			if (!check(SMODE_PREPARED))
				return 0;
			return player.getCurrentPosition();
		}
	}
	
	public int getDuration() {
		if (!check(SMODE_PREPARED)) return 0;
		synchronized (LOCK) {
			return player.getDuration();
		}
	}
	
	public AbstractSong getPlayingSong() {
		return null != playingSong ? playingSong : null;
	}
	
	public int getPlayingPosition() {
		synchronized (LOCK) {
			return arrayPlayback.indexOf(playingSong);
		}
	}

	public boolean enabledShuffle() {
		return check(SMODE_SHUFFLE);
	}

	public boolean enabledRepeat() {
		return check(SMODE_REPEAT);
	}
	
	public boolean isPrepared() {
		return check(SMODE_PREPARED);
	}
	
	public boolean isPaused() {
		return check(SMODE_PAUSE);
	}
	
	public boolean isGettingURl() {
		return check(SMODE_GET_URL);
	}
	
	public boolean isStoped() {
		return check(SMODE_STOP);
	}
	
	public boolean isPlaying() {
		boolean result = false;
		if (check(SMODE_PLAYING)) {
			result = true;
		}
		if (check(SMODE_PAUSE)) {
			result = false;
		}
		return result;
	}
	
	public boolean hasArray() {
		return arrayPlayback != null && !arrayPlayback.isEmpty();
	}
	
	public ArrayList<AbstractSong> getArrayPlayback() {
		if (check(SMODE_SHUFFLE)) {
			return arrayPlaybackOriginal;
		}
		return arrayPlayback;
	}
	
	public <T> boolean isCorrectlyState(Class<T> calledClass, int transferSize) {
		boolean result = true;
		if (arrayPlayback == null || transferSize != arrayPlayback.size()) {
			result = false;
		}
		if (playingSong != null && playingSong.getClass() != calledClass) {
			result = false;
		}
		return result;
	}
	
	public void setArrayPlayback(ArrayList<AbstractSong> arrayPlayback) {
		this.arrayPlayback = arrayPlayback;
	}
	
	public void addStatePlayerListener(OnStatePlayerListener stateListener) {
		this.stateListeners.add(stateListener);
	}
	
	public void removeStatePlayerListener(OnStatePlayerListener stateListener) {
		this.stateListeners.remove(stateListener);
	}
	
	public void setDestroyListener(OnPlaybackServiceDestroyListener destroyListener) {
		this.destroyListener = destroyListener;
	}

	public void setOnErrorListener(OnErrorListener errorListener) {
		this.errorListener = errorListener;
	}
	
	public int getAudioSessionId() {
		if (null != player) {
			return player.getAudioSessionId();
		} 
		return 0;
	} 
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null != intent && null != intent.getAction() && !intent.getAction().isEmpty()) {
			if (intent.getAction().equals(CLOSE_ACTION)) {
				stopPressed();
			} else if (intent.getAction().equals(PLAY_ACTION)) {
				if (null != playingSong) {
					play(playingSong);
				}
			} else if (intent.getAction().equals(NEXT_ACTION)) {
				shift(1);
			}
		}
		return Service.START_FLAG_REDELIVERY;
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}
	
	@SuppressLint("NewApi")
	private void sendNotification(boolean playing, Bitmap updateCover) {
		if (!check(SMODE_NOTIFICATION)) return;
		Bitmap cover = playingSong.getCover(this);
		if (null != updateCover) {
			cover = updateCover;
			if (playingSong.getClass() != MusicData.class) {
				((RemoteSong) playingSong).setCover(cover);
			}
		} else if (null == cover) {
			cover = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon);
		}
		float size = Util.dpToPx(this, 64);
		cover = Util.resizeBitmap(cover, size, size);
		Intent notificationIntent = new Intent(this, ((Activity) activityContext).getClass());
		notificationIntent.setAction(MAIN_ACTION);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Intent playIntent = new Intent(this, PlaybackService.class);
		playIntent.setAction(PLAY_ACTION);
		PendingIntent pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

		Intent nextIntent = new Intent(this, PlaybackService.class);
		nextIntent.setAction(NEXT_ACTION);
		PendingIntent pnextIntent = PendingIntent.getService(this, 0, nextIntent, 0);

		Intent closeIntent = new Intent(this, PlaybackService.class);
		closeIntent.setAction(CLOSE_ACTION);
		PendingIntent pcloseIntent = PendingIntent.getService(this, 0, closeIntent, 0);

		NotificationCompat.Builder builder = null;
		int drawable = 0;
		String state = (playing) ? getString(R.string.pause) : getString(R.string.play);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			drawable = (playing) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
			RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification_player);
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
			view.setTextViewText(R.id.txt_music, playingSong.getArtist() + " - " + playingSong.getTitle());
			view.setTextViewText(R.id.txt_time, simpleDateFormat.format(calendar.getTime()));
			view.setImageViewResource(R.id.btn_play, drawable);
			view.setOnClickPendingIntent(R.id.btn_play, pplayIntent);
			view.setOnClickPendingIntent(R.id.btn_next, pnextIntent);
			view.setOnClickPendingIntent(R.id.btn_close, pcloseIntent);
			builder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.icon)
					.setLargeIcon(cover)
					.setContentIntent(pendingIntent).setContent(view);
			startForeground(NOTIFICATION_ID, builder.build());
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
			drawable = (playing) ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
			builder = new NotificationCompat.Builder(this)
					.setPriority(NotificationCompat.PRIORITY_MAX)
					.setSmallIcon(R.drawable.icon)
					.setLargeIcon(cover)
					.setContentTitle(playingSong.getTitle())
					.setContentText(playingSong.getArtist())
					.setContentIntent(pendingIntent)
					.addAction(drawable, state, pplayIntent)
					.addAction(android.R.drawable.ic_media_next, getString(R.string.next), pnextIntent)
					.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.close), pcloseIntent);
			startForeground(NOTIFICATION_ID, builder.build());
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
			drawable = (playing) ? R.drawable.ic_pause : R.drawable.ic_play;
			Notification notification = new Notification.Builder(this)
		    .setVisibility(Notification.VISIBILITY_PUBLIC)
		    .setSmallIcon(R.drawable.icon)
		    .addAction(drawable, state, pplayIntent)
			.addAction(R.drawable.ic_next, getString(R.string.next), pnextIntent)
			.addAction(R.drawable.ic_close, getString(R.string.close), pcloseIntent)
		    .setStyle(new Notification.MediaStyle()
		    .setShowActionsInCompactView(2))
		    .setContentTitle(playingSong.getTitle())
		    .setContentText(playingSong.getArtist())
		    .setContentIntent(pendingIntent)
		    .setLargeIcon(cover)
		    .build();
			startForeground(NOTIFICATION_ID, notification);
		}
	}
	
	private void removeNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
	    manager.cancel(NOTIFICATION_ID);  
	    stopForeground(true);
	}  
	
	public void  updatePictureNotification(Bitmap bmp) {
		sendNotification(isPlaying(), bmp);
	}
	
	public void showNotification(boolean flag) {
		if (flag) {
			onMode(SMODE_NOTIFICATION);
		} else {
			offMode(SMODE_NOTIFICATION);
		}
	}
	
	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		player.setOnBufferingUpdateListener(listener);
	}
	
	private OnBufferingUpdateListener bufferingUpdateListener = new OnBufferingUpdateListener() {
		
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			bufferingPercent = (float)percent / 100.0;
			for (OnStatePlayerListener listener : stateListeners) {
				listener.onBufferingUpdate(bufferingPercent);
			}
		}
	};

	// it design for debug
	private void printStateDebug() {
		StringBuilder builder = new StringBuilder();
		if (check(SMODE_GET_URL)) {
			builder.append("| SMODE_GET_URL");
		}
		if (check(SMODE_PAUSE)) {
			builder.append("| SMODE_PAUSE");
		}
		if (check(SMODE_PLAYING)) {
			builder.append("| SMODE_PLAYING");
		}
		if (check(SMODE_PREPARED)) {
			builder.append("| SMODE_PREPARED");
		}
		if (check(SMODE_REPEAT)) {
			builder.append("| SMODE_REPEAT");
		}
		if (check(SMODE_SHUFFLE)) {
			builder.append("| SMODE_SHUFFLE");
		}
		if (check(SMODE_START_PREPARE)) {
			builder.append("| SMODE_START_PREPARE");
		}	
		if (check(SMODE_NOTIFICATION)) {
			builder.append("| SMODE_NOTIFICATION");
		}
		if (check(SMODE_STOP)) {
			builder.append("| SMODE_STOP");
		}
		android.util.Log.d("logks", builder.toString());
	}
}