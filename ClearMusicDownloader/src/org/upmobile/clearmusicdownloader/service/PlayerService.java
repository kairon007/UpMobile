package org.upmobile.clearmusicdownloader.service;

import java.io.IOException;
import java.util.ArrayList;

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
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PlayerService extends Service implements OnCompletionListener, OnErrorListener, Handler.Callback {
	
	//constants section
	private static final int SMODE_GET_URL = 0x00000001;
	private static final int SMODE_PREPARED = 0x00000002;
	/**
	 * bit 1 (true) - play, bit 0 (false) - pause
	 */
	private static final int SMODE_PLAY_PAUSE = 0x00000004;
	private static final int SMODE_PLAYING = 0x00000008;
	private static final int SMODE_STOPPING = 0x00000010;
	private static final int MSG_PLAY = 1;
	private static final int MSG_PLAY_CURRENT = 2;
	private static final int MSG_PAUSE = 3;
	private static final int MSG_SEEK_TO = 4;
	private static final int MSG_ERROR = 5;
	private static final int MSG_RESET = 6;
	
	//multy-threading section
	private final Object lock = new Object();
	private Object wait = new Object();
	private Looper looper;
	private Handler handler;
	
	//instance section
	private ArrayList<AbstractSong> arrayPlayback;
	private OnStatePlayerListener stateListener;
	private static PlayerService instance;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private MediaPlayer player;
	private AbstractSong playingSong;
	private int playingPosition;
	private int mode;
	
	public interface OnStatePlayerListener {
		
		public enum State {
			START, PLAY, PAUSE, RESET, COMPLETE, UPDATE
		}
		
		public void start(AbstractSong song);
		public void play();
		public void	pause();
		public void reset();
		public void complete();
		public void update(AbstractSong song);
			
	}
	
	private class HeadsetIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
				Message msg = buildMessage(MSG_PAUSE, 0, 0);
				handler.sendMessage(msg);
			}
		}
	};
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		private boolean flag = false;
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			Message msg = null;
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				msg = buildMessage(MSG_PAUSE, 0, 0);
				handler.sendMessage(msg);
				flag = true;
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (flag) {
					msg = buildMessage(MSG_PLAY_CURRENT, 0, 0);
					handler.sendMessage(msg);
					flag = false;
				}
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	
	/**
	 * 
	 * Return the Player sevice instance, creating one if needed.
	 * @Context use for call
	 */
	public static PlayerService get(Context context) {
		if (instance == null) {
			context.startService(new Intent(context, PlayerService.class));
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
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		looper = thread.getLooper();
		handler = new Handler(looper, this);
		instance = this;
	}
	
	@Override
	public void onDestroy() {
		player.release();
		looper.quit();
		super.onDestroy();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_PLAY:
			if (check(SMODE_PREPARED)) {
				player.reset();
				offMode(SMODE_PREPARED);
			} 
			String path = (String) msg.obj;
			Uri uri = Uri.parse(path);
			try {
				player.setDataSource(this, uri);
				player.prepare();
				onMode(SMODE_PREPARED);
			} catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
				android.util.Log.e(getClass().getName(), "in method \"hanleMessage\" appear problem: " + e.toString());
			}
			if (msg.arg1 != playingPosition) {
				play(playingPosition);
				break;
			}
			helper(State.START);
			player.start();
			break;

		case MSG_PLAY_CURRENT:
			if (check(SMODE_PREPARED)) {
				player.start();
				onMode(SMODE_PLAYING);
			}
			break;

		case MSG_PAUSE:
			if (check(SMODE_PREPARED)) {
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
			player.release();
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			break;
			
		case MSG_RESET:
			if(check(SMODE_PREPARED)){
				offMode(SMODE_PREPARED);
				player.reset();
			}
			synchronized (wait) {
				wait.notifyAll();
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	public void shift(int delta) {
		int buf = playingPosition + delta;
		if (0 >= buf && buf < arrayPlayback.size()) playingPosition  =  buf;
		else {
			Message msg = buildMessage(MSG_PLAY_CURRENT, 0, 0);
			handler.sendMessage(msg);
			return;
		}
		handler.removeMessages(1, null);
		synchronized (wait) {
			Message msg = new Message();
			msg.what = MSG_RESET;
			handler.sendMessage(msg);
			try {
				wait.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		helper(State.UPDATE);
		play(playingPosition);
	}

	public void play(int position) {
		playingSong = arrayPlayback.get(position);
		boolean fromInternet = playingSong.getClass() != MusicData.class;
		Message msg = new Message();	
		synchronized (lock) {
			if (check(SMODE_PREPARED)) {
				if (check(SMODE_PLAY_PAUSE)) {
					msg.what = MSG_PLAY_CURRENT;
					offMode(SMODE_PLAY_PAUSE);
					helper(State.PLAY);
				} else {
					msg.what = MSG_PAUSE;
					helper(State.PAUSE);
					onMode(SMODE_PLAY_PAUSE);
				}
			} else {
				if (fromInternet) {
					playingPosition = position;
					play(playingSong);
					return;
				}
				offMode(SMODE_PLAY_PAUSE);
				msg.what = MSG_PLAY;
				msg.arg1 = position;
				String str = playingSong.getPath();
				msg.obj = str;
			}
			handler.sendMessage(msg);
		}
	}
	
	private void helper(State state) {
		if (stateListener == null) return;
		switch (state) {
		case START:
			stateListener.start(playingSong);
			break;
		case PLAY:
			stateListener.play();
			break;
		case PAUSE:
			stateListener.pause();
			break;
		case RESET:
			stateListener.reset();
			break;
		case COMPLETE:
			stateListener.complete();
			break;
		case UPDATE:
			AbstractSong buf = arrayPlayback.get(playingPosition);
			stateListener.update(buf);
			break;
		}
	}
	
	// for RemoteSong
	private void play(AbstractSong song) {
		onMode(SMODE_GET_URL);
		((RemoteSong) song).getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
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
		} else if (flag == SMODE_PLAYING) {
			mode &= ~SMODE_STOPPING;
		} else if (flag == SMODE_STOPPING){
			mode &= ~SMODE_PLAYING;
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
		helper(State.COMPLETE);
		Message msg = buildMessage(MSG_SEEK_TO, 0, 0);
		handler.sendMessage(msg);
	}

	public int getCurrentPosition() {
		if (!check(SMODE_PREPARED)) return 0;
		return player.getCurrentPosition();
	}

	public void seekTo(int progress) {
		Message msg = buildMessage(MSG_SEEK_TO, progress, 0);
		handler.sendMessage(msg);
	}
	
	public void reset() {
		Message msg = buildMessage(MSG_RESET, 0, 0);
		handler.sendMessage(msg);
	}
	
	public boolean isPrepared() {
		return check(SMODE_PREPARED);
	}
	
	public boolean showPlayPause() {
		return check(SMODE_PLAY_PAUSE);
	}
	
	/** 
	 * @param isPlaying - recommended parameter is false
	 */
	public AbstractSong getPlayingSong() {
		playingSong = arrayPlayback.get(playingPosition);
		return playingSong;
	}
	
	public int getPlayingPosition() {
		return playingPosition;
	}
	
	public boolean gettingURl() {
		return check(SMODE_GET_URL);
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
		if (playingSong.getClass() != calledClass) return false;
		return true;
	}
	
	public void setArrayPlayback(ArrayList<AbstractSong> arrayPlayback) {
		this.arrayPlayback = arrayPlayback;
	}
	
	public void setPlayingPosition(int playingPosition) {
		this.playingPosition = playingPosition;
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
