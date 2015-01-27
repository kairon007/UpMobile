package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;
import java.util.Collections;

import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener.State;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PlaybackService  extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener, Handler.Callback {
	
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
	private static final int MSG_START = 1;
	private static final int MSG_PLAY = 2;
	private static final int MSG_PAUSE = 3;
	private static final int MSG_SEEK_TO = 4;
	private static final int MSG_ERROR = 5;
	private static final int MSG_RESET = 6;
	private static final int MSG_STOP = 7;
	
	//multy-threading section
	private static final Object LOCK = new Object();
	protected static final Object WAIT = new Object();
	private Looper looper;
	private Handler handler;
	
	//instance section
	private ArrayList<AbstractSong> arrayPlayback;
	private ArrayList<AbstractSong> arrayPlaybackOriginal;
	private OnStatePlayerListener stateListener;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private MediaPlayer player;
	private static PlaybackService instance;
	private AbstractSong previousSong;
	private AbstractSong playingSong;
	private int mode;
	
	public interface OnStatePlayerListener {
		
		public enum State {
			START, PLAY, PAUSE, STOP, ERROR, UPDATE, NONE
		}
		
		public void start(AbstractSong song);
		public void play(AbstractSong song);
		public void	pause(AbstractSong song);
		public void stop (AbstractSong song);
		public void update (AbstractSong song);
		public void	error ();
			
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
			Message msg = null;
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
				if (isAfterCall && !disabledDuringCall) {
					buildSendMessage(playingSong, MSG_PLAY, 0, 0);
					mode &= ~SMODE_CALL_RINGING;
					mode &= ~SMODE_DISABLED_DURING_CALL;
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	
	public static PlaybackService get(Context context) {
		if (instance == null) {
			context.startService(new Intent(context, PlaybackService.class));
			try {
				synchronized (WAIT) {
					WAIT.wait();
				}
			} catch (InterruptedException ignored) {
			}
		}
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
		telephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		headsetReceiver = new HeadsetIntentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(headsetReceiver, filter);
		player = new MediaPlayer();
		android.util.Log.d("logks", "PlaybackService, onCreate:");
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		looper = thread.getLooper();
		handler = new Handler(looper, this);
		instance = this;
		synchronized (WAIT) {
			WAIT.notifyAll();
		}
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(headsetReceiver);
		handler.removeCallbacksAndMessages(null);
		android.util.Log.d("logks", "PlaybackService, onDestroy: ");
		player.release();
		looper.quit();
		super.onDestroy();
	}
	
	public void seekTo(int progress) {
		buildSendMessage(null, MSG_SEEK_TO, progress, 0);
	}
	
	public void reset() {
		handler.removeCallbacksAndMessages(null);
		mode &= SMODE_PREPARED;
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
				reset();
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
		//TODO handleMessage()
		android.util.Log.d("logks", "PlaybackService, >>>>>>>>>>handleMessage<<<<<<<<<<<<<< what - " + msg.what);
		switch (msg.what) {
		case MSG_START:
			android.util.Log.d("logks", "PlaybackService, handleMessage: MSG_START, path - " + (String) msg.obj);
			if (check(SMODE_PREPARED) || check(SMODE_START_PREPARE)) {
				player.reset();
				offMode(SMODE_PREPARED);
			}	
			if (check(SMODE_GET_URL)) {
				mode &= ~SMODE_GET_URL;
			}
			try {
				Uri uri = Uri.parse((String) msg.obj);
				player.setDataSource(this, uri);
				mode |= SMODE_START_PREPARE;
				android.util.Log.d("logks", "start prepareAsync()");
				player.prepareAsync();
			} catch (Exception e) {
				android.util.Log.e("logks", "in method \"hanleMessage\" appear problem: " + e.toString());
			}
			break;

		case MSG_PLAY:
			if (check(SMODE_PREPARED)) {
				helper(State.PLAY, (AbstractSong) msg.obj);
				android.util.Log.d("logks", "in handle message, MSG_PLAY - " + ((AbstractSong) msg.obj).getTitle());
				player.start();
				mode |= SMODE_PLAYING;
			}
			break;
		case MSG_PAUSE:
			if (check(SMODE_PREPARED)) {
				helper(State.PAUSE, (AbstractSong) msg.obj);
				player.pause();
				mode |= SMODE_PAUSE;
			}
			break;
		case MSG_SEEK_TO:
			if(check(SMODE_PREPARED)){
				player.seekTo(msg.arg1);
			}
			break;
		case MSG_ERROR:
			offMode(SMODE_PREPARED);
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
			break;
		case MSG_STOP:
			if (check(SMODE_PREPARED)) {
				player.pause();
				player.seekTo(0);
			}
			break;
		default:
			Log.d(getClass().getName(), "invalid message send from Handler");
			break;
		}
		return true;
	}
	
	public void shift(int delta) {
		android.util.Log.d("logks", "PlaybackService, shift: delta = " + delta);
		if (null == arrayPlayback || arrayPlayback.isEmpty()) return;
		int position = arrayPlayback.indexOf(playingSong);
		helper(State.STOP, playingSong);
		if (enabledRepeat()) {
			android.util.Log.d("logks", "enabledRepeat(), play - " + playingSong.getTitle());
			buildSendMessage(playingSong, MSG_PLAY, 0, 0);
			return;
		}
		position += delta;
		if (position >= arrayPlayback.size()) {
			position = 0;
		} else if (position < 0) {
			position = arrayPlayback.size() - 1;
		}
		previousSong = playingSong;
		playingSong = arrayPlayback.get(position);
		helper(State.UPDATE, playingSong);
		handler.removeCallbacksAndMessages(null);
		buildSendMessage(null, MSG_RESET, 0, 0);
		play(playingSong.getClass() != MusicData.class);
	}

	public void play(AbstractSong song) {
		if (arrayPlayback == null && arrayPlayback.indexOf(song) == -1) {
			return;
		}
		int position = arrayPlayback.indexOf(song);
		if (null != playingSong) {
			previousSong = playingSong;
			if (!playingSong.equals(song)) {
				if (playingSong.getClass() != MusicData.class) {
					((RemoteSong) playingSong).cancelTasks();
				}
				mode &= ~SMODE_PREPARED;
				reset();
			}
		}
		playingSong = arrayPlayback.get(position);
		if (check(SMODE_PREPARED)) {
			Message msg = new Message();
			if (check(SMODE_PAUSE)) {
				msg.what = MSG_PLAY;
				mode &= ~SMODE_PAUSE;
				offMode(SMODE_PLAYING);
			} else {
				onMode(SMODE_PAUSE);
				msg.what = MSG_PAUSE;
				
			}
			msg.obj = playingSong;
			handler.sendMessage(msg);
		} else {
			play(playingSong.getClass() != MusicData.class);
		}
	}
	
	public void play() {
		previousSong = playingSong;
		if (check(SMODE_PREPARED)) {
			helper(State.START, playingSong);
		} else {
			helper(State.STOP, playingSong);
		}
	}
	
	public void stop() {
		if (check(SMODE_PREPARED)) {
			player.pause();
			player.seekTo(0);
			offMode(SMODE_PLAYING);
			onMode(SMODE_PAUSE);
		}
		helper(State.STOP, playingSong);
	}
	
	public boolean offOnShuffle(){
		mode ^= SMODE_SHUFFLE;
		boolean result = enabledShuffle();
		if (result) {
			arrayPlaybackOriginal = new ArrayList<AbstractSong>();
			for (AbstractSong song : arrayPlayback) {
				arrayPlaybackOriginal.add(song.cloneSong());
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
					buildSendMessage(url, MSG_START, 0, 0);
				}

				@Override
				public void error(String error) {
					mode &= ~SMODE_GET_URL;
				}
			});
			return;
		}
		buildSendMessage(playingSong.getPath(), MSG_START, 0, 0);
	}
	
	private void helper(final State state, final AbstractSong targetSong) {
		if (stateListener == null || state.equals(State.NONE)) {
			return;
		}
		Handler handler = new Handler(getMainLooper());
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				switch (state) {
				case START:
					stateListener.start(targetSong);
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
	
	private synchronized void buildSendMessage(Object obj, int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.obj = obj;
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		handler.sendMessage(msg);
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		helper(State.ERROR, null);
		buildSendMessage(playingSong, MSG_ERROR, what, extra);
		shift(1);
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		android.util.Log.d("logks", "PlaybackService, onCompletion:" );
		shift(1);
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		//TODO onPrepared
		synchronized (LOCK) {
			onMode(SMODE_PREPARED);
			onMode(SMODE_PLAYING);
			printStateDebug();
			helper(State.START, playingSong);android.util.Log.d("logks", "PlaybackService, onPrepared: song - " + playingSong.getPath());
			mp.start();
			if ((mode & SMODE_UNPLUG_HEADPHONES) == SMODE_UNPLUG_HEADPHONES) {
				buildSendMessage(playingSong, MSG_PAUSE, 0, 0);
				mode &= ~SMODE_UNPLUG_HEADPHONES;
			}
		}
	}
	
	public int getCurrentPosition() {
		if (!check(SMODE_PREPARED)) return 0;
		return player.getCurrentPosition();
	}
	
	public AbstractSong getPlayingSong() {
		return playingSong;
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
	
	public boolean hasValidSong(Class cl) {
		boolean result = false;
		if (null != playingSong) {
			if (playingSong.getClass() == cl) {
				result = true;
			}
		}
		return result;
	}
	
	public boolean isPrepared() {
		return check(SMODE_PREPARED);
	}
	
	public boolean isGettingURl() {
		return check(SMODE_GET_URL);
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
		return arrayPlayback;
	}
	
	public boolean isCorrectlyState(Class calledClass, int transferSize) {
		if (arrayPlayback == null) return false;
		if (transferSize != arrayPlayback.size()) return false;
		if (playingSong != null) {
			if (playingSong.getClass() != calledClass) return false;
		};
		return true;
	}
	
	public void setArrayPlayback(ArrayList<AbstractSong> arrayPlayback) {
		this.arrayPlayback = arrayPlayback;
	}
	
	public void setStatePlayerListener(OnStatePlayerListener stateListener) {
		this.stateListener = stateListener;
	}
	
	public void setPlayingSong(AbstractSong playingSong) {
		this.playingSong = playingSong;
	}
	
	public int[] getPosition(AbstractSong song) {
		return new int[] { arrayPlayback.indexOf(song), null != arrayPlaybackOriginal ? arrayPlaybackOriginal.indexOf(song) : -1 };
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}
	
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
		android.util.Log.d("logks", builder.toString());
	}

}