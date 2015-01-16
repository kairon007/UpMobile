package org.upmobile.clearmusicdownloader.service;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.activity.MainActivity;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService.OnStatePlayerListener.State;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
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

public class PlayerService extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener, Handler.Callback {
	
	//constants section
	private static final int SMODE_GET_URL = 0x00000001;
	private static final int SMODE_PREPARED = 0x00000002;
	/**
	 * bit 1 (true) - play, bit 0 (false) - pause
	 */
	private static final int SMODE_PLAY_PAUSE = 0x00000004;
	private static final int SMODE_PLAYING = 0x00000008;
	private static final int SMODE_STOPPING = 0x00000010;
	private static final int SMODE_START_PREPARE = 0x00000020;
	private static final int MSG_PLAY = 1;
	private static final int MSG_PLAY_CURRENT = 2;
	private static final int MSG_PAUSE = 3;
	private static final int MSG_SEEK_TO = 4;
	private static final int MSG_ERROR = 5;
	private static final int MSG_RESET = 6;
	
	//multy-threading section
	private static final Object LOCK = new Object();
	private static final Object WAIT = new Object();
	private Looper looper;
	private Handler handler;
	
	//instance section
	private ArrayList<AbstractSong> arrayPlayback;
	private OnStatePlayerListener stateListener;
	private static PlayerService instance;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private MediaPlayer player;
	private Context context;
	private AbstractSong playingSong;
	private int playingPosition = -1;
	private int mode;
	private boolean flag = false;
	private boolean disabledDuringCall = false;
	private boolean unplugHeadphones = false;
	
	public interface OnStatePlayerListener {
		
		public enum State {
			START, PLAY, PAUSE, UPDATE, NONE
		}
		
		public void start(AbstractSong song, int position);
		public void play();
		public void	pause();
		public void update(AbstractSong song, int position);
			
	}
	
	private class HeadsetIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
				Message msg = buildMessage(MSG_PAUSE, 0, 0);
				handler.sendMessage(msg);
				unplugHeadphones = true;
				if (flag) disabledDuringCall = true;
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
					msg = buildMessage(MSG_PAUSE, 0, 0);
					handler.sendMessage(msg);
					flag = true;
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (flag && !disabledDuringCall) {
					msg = buildMessage(MSG_PLAY_CURRENT, 0, 0);
					handler.sendMessage(msg);
					flag = false;
					disabledDuringCall = false;
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	
	/**
	 * 
	 * Return the Player service instance, creating one if needed. It can't call in ui
	 * @Context use for call
	 */
	public static PlayerService get(Context context) {

		if (instance == null) {
			context.startService(new Intent(context, PlayerService.class));
			try {
				synchronized (WAIT) {
					WAIT.wait();
				}
			} catch (InterruptedException ignored) {
			}

		}
		instance.context = context;
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
		player.release();
		looper.quit();
		super.onDestroy();
	}
	
	public void seekTo(int progress) {
		Message msg = buildMessage(MSG_SEEK_TO, progress, 0);
		handler.sendMessage(msg);
	}
	
	public void remove(AbstractSong song) {
		synchronized (LOCK) {
			if (null == arrayPlayback || arrayPlayback.isEmpty()) {
				return;
			}
			if (song.equals(playingSong)) {
				arrayPlayback.remove(song);
				if (check(SMODE_PLAYING)) {
					shift(0);
				}
			} else {
				arrayPlayback.remove(song);
			}
			if (arrayPlayback.isEmpty()) {
				Message msg = buildMessage(MSG_RESET, 0, 0);
				handler.sendMessage(msg);
			}
		}
	}
	
	public void update(int position, String title, String artist, String path){
		if (playingSong.getClass() == MusicData.class){
			MusicData data = (MusicData) arrayPlayback.get(position);
			data.setArtist(artist);
			data.setTitle(title);
			data.setPath(path);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_PLAY:
			if (check(SMODE_PREPARED) || check(SMODE_START_PREPARE)) {
				player.reset();
			}
			if (check(SMODE_GET_URL)) {
				offMode(SMODE_GET_URL);
			}
			String path = (String) msg.obj;
			Uri uri = Uri.parse(path);
			try {
				player.setDataSource(this, uri);
				onMode(SMODE_START_PREPARE);
				player.prepareAsync();
			} catch (Exception e) {
				android.util.Log.e(getClass().getName(), "in method \"hanleMessage\" appear problem: " + e.toString());
			}
			break;
		case MSG_PLAY_CURRENT:
			if (check(SMODE_PREPARED)) {
				helper(State.PLAY);
				player.start();
				onMode(SMODE_PLAYING);
			}
			break;
		case MSG_PAUSE:
			if (check(SMODE_PREPARED)) {
				helper(State.PAUSE);
				player.pause();
				onMode(SMODE_STOPPING);
			}
			break;
		case MSG_SEEK_TO:
			if(check(SMODE_PREPARED)){
				player.seekTo(msg.arg1);
			}
			break;
		case MSG_ERROR:
			offMode(SMODE_PREPARED);
			player.reset();
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
		default:
			break;
		}
		return true;
	}
	
	public void shift(int delta) {
		int buf = playingPosition + delta;
		State state = State.UPDATE;
		if (delta == 0) {
			state = State.NONE;
			if(playingPosition == arrayPlayback.size()) playingPosition--;
			else if (playingPosition > arrayPlayback.size()) playingPosition = arrayPlayback.size() - 1;
		} else if (0 <= buf && buf < arrayPlayback.size()) {
			playingPosition  =  buf;
		} else if (buf >= arrayPlayback.size()) {
			playingPosition = 0;
			if (arrayPlayback.size() == 0) {
				((MainActivity) context).hidePlayerElement();
				return;
			}
		} else if (buf < 0) {
			playingPosition = arrayPlayback.size() - 1;
		}
		if (playingPosition == -1) {
			((MainActivity) context).hidePlayerElement();
			return;
		}
		playingSong = arrayPlayback.get(playingPosition);
		handler.removeCallbacksAndMessages(null);
		Message msg = new Message();
		msg.what = MSG_RESET;
		handler.sendMessage(msg);
		helper(state);
		play(new Message());
	}

	public void play(int position) {
		if (arrayPlayback == null) return;
		playingSong = arrayPlayback.get(position);
		Message msg = new Message();
		if (playingPosition == position && playingPosition != -1) {
			if (!check(SMODE_PREPARED)) return;
			if (check(SMODE_PLAY_PAUSE)) {
				msg.what = MSG_PLAY_CURRENT;
				offMode(SMODE_PLAY_PAUSE);
			} else {
				msg.what = MSG_PAUSE;
				onMode(SMODE_PLAY_PAUSE);
			}
			handler.sendMessage(msg);
		} else {
			Message m = buildMessage(MSG_RESET, 0, 0);
			handler.sendMessage(m);
			playingPosition = position;
			play(msg);
		}
	}
	
	private void play(Message msg) {
		boolean fromInternet = playingSong.getClass() != MusicData.class;
		if (fromInternet) {
			onMode(SMODE_GET_URL);
			((RemoteSong) playingSong).getDownloadUrl(new DownloadUrlListener() {

				@Override
				public void success(String url) {
					if (playingSong.getClass() == MusicData.class) return;
					((RemoteSong) playingSong).setDownloadUrl(url);
					offMode(SMODE_GET_URL);
					offMode(SMODE_PLAY_PAUSE);
					Message msg = new Message();
					msg.what = MSG_PLAY;
					msg.arg1 = playingPosition;
					msg.obj = url;
					handler.sendMessage(msg);
				}

				@Override
				public void error(String error) {
				}
			});
			return;
		}
		offMode(SMODE_PLAY_PAUSE);
		msg.what = MSG_PLAY;
		msg.arg1 = playingPosition;
		String str = playingSong.getPath();
		msg.obj = str;
		handler.sendMessage(msg);
	}

	private void helper(final State state) {
		if (stateListener == null || state.equals(State.NONE)) {
			return;
		}
		((MainActivity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				int position = arrayPlayback.indexOf(playingSong);
				switch (state) {
				case START:
					stateListener.start(playingSong, position);
					break;
				case PLAY:
					stateListener.play();
					break;
				case PAUSE:
					stateListener.pause();
					break;
				case UPDATE:
					AbstractSong buf = arrayPlayback.get(position);
					stateListener.update(buf, position);
					break;
				}
			}
		});
	}

	private void offMode(int flag) {
		mode &= ~flag;
		if (flag == SMODE_PREPARED) {
			mode &= ~SMODE_PLAYING;
			mode &= ~SMODE_STOPPING;
		}
	}
	
	private void onMode(int flag) {
		mode |= flag;
		if (flag == SMODE_PREPARED) {
			onMode(SMODE_PLAYING);
			offMode(SMODE_START_PREPARE);
		} else if (flag == SMODE_PLAYING) {
			mode &= ~SMODE_STOPPING;
		} else if (flag == SMODE_STOPPING){
			mode &= ~SMODE_PLAYING;
		} else if (flag == SMODE_START_PREPARE) {
			offMode(SMODE_PREPARED);
		}
	}
	
	private boolean check(int flag) {
		return (mode & flag) == flag;
	}
	
	private Message buildMessage(int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		return msg;
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		Message msg = buildMessage(MSG_ERROR, what, extra);
		handler.sendMessage(msg);
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		shift(1);
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		onMode(SMODE_PREPARED);
		mp.start();
		helper(State.START);
		if (unplugHeadphones) {
			Message msg = buildMessage(MSG_PAUSE, 0, 0);
			handler.sendMessage(msg);
			unplugHeadphones = false;
		}
	}
	
	public int getCurrentPosition() {
		if (!check(SMODE_PREPARED)) return 0;
		return player.getCurrentPosition();
	}
	
	public void reset() {
		handler.removeCallbacksAndMessages(null);
		offMode(SMODE_PREPARED);
		Message msg = new Message();
		msg.what = MSG_RESET;
		handler.sendMessage(msg);
	}
	
	public AbstractSong getPlayingSong() {
		return playingSong;
	}
	
	public int getPlayingPosition() {
		synchronized (LOCK) {
			return arrayPlayback.indexOf(playingSong);
		}
	}
	
	public void setPlayingPosition(int playingPosition) {
		this.playingPosition = playingPosition;
	}
	
	public boolean showPlayPause() {
		return check(SMODE_PLAY_PAUSE);
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
	
	public boolean hasArray() {
		return arrayPlayback != null && !arrayPlayback.isEmpty();
	}
	
	public ArrayList<AbstractSong> getArrayPlayback() {
		return arrayPlayback;
	}
	
	public boolean isPlaying() {
		boolean result=  false;
		if (check(SMODE_PLAYING)) {
			result = true;
		}
		if (check(SMODE_STOPPING)) {
			result = false;
		}
		return result;
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
		playingPosition = -1;
		this.arrayPlayback = arrayPlayback;
	}
	
	public void setStatePlayerListener(OnStatePlayerListener stateListener) {
		this.stateListener = stateListener;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}
}
