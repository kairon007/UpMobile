package org.upmobile.clearmusicdownloader.service;

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
	private static final int SMODE_COMPLETE = 0x00000001;
	private static final int SMODE_PREPARED = 0x00000002;
	/**
	 * bit 1 (true) - play, bit 0 (false) - pause
	 */
	private static final int SMODE_PLAY_PAUSE = 0x00000004;
	private static final int MSG_PLAY = 1;
	private static final int MSG_PLAY_CURRENT = 2;
	private static final int MSG_PAUSE = 3;
	private static final int MSG_SEEK_TO = 4;
	private static final int MSG_ERROR = 5;
	
	//multy-threading section
	private final Object lock = new Object();
	private Looper looper;
	private Handler handler;
	
	//instance section
	public static PlayerService instance;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private MediaPlayer player;
	private OnStatePlayerListener stateListener;
	private String currentPath;
	private int mode;
	
	public interface OnStatePlayerListener {
		
		public void prepare();
		public void error();
		
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
				return;
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
		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		instance = null;
		player.release();
		looper.quit();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		//TODO handleMessage
		switch (msg.what) {
		case MSG_PLAY:
			if (check(SMODE_PREPARED)) {
				offMode(SMODE_PREPARED);
				callStateListener(false);
				player.reset();
			}
			try {
				String path = (String) msg.obj;
				Uri uri = Uri.parse(path);
				player.setDataSource(this, uri);
				player.prepare();
				callStateListener(true);
				onMode(SMODE_PREPARED);
				offMode(SMODE_COMPLETE);
				player.start();
			} catch (Exception e) {
				android.util.Log.d("log", "in method \"handleMessage\" appear problem: " + e.toString());
				offMode(SMODE_PREPARED);
				callStateListener(false);
			}
			break;

		case MSG_PLAY_CURRENT:
			if (check(SMODE_PREPARED)) {
				player.start();
			}
			break;

		case MSG_PAUSE:
			if (check(SMODE_PREPARED)) {
				player.pause();
			}
			break;

		case MSG_SEEK_TO:
			if(check(SMODE_PREPARED)){
				player.seekTo(msg.arg1);
			}
			break;
			
		case MSG_ERROR:
			offMode(SMODE_PREPARED);
			callStateListener(false);
			player.release();
			player = new MediaPlayer();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
			break;
		default:
			break;
		}
		return true;
	}

	public void play(String path) {
		Message msg = new Message();	
		synchronized (lock) {
			if (path.equals(currentPath)) {
				if (check(SMODE_PLAY_PAUSE)) {
					msg.what = MSG_PLAY_CURRENT;
					offMode(SMODE_PLAY_PAUSE);
				} else {
					msg.what = MSG_PAUSE;
					onMode(SMODE_PLAY_PAUSE);
				}
			} else {
				offMode(SMODE_PLAY_PAUSE);
				msg.what = MSG_PLAY;
				msg.obj = path;
			}
			currentPath = path;
			handler.sendMessage(msg);
		}
	}
	
	private void offMode(int flag) {
		mode &= ~flag;
	}

	private void onMode(int flag) {
		mode |= flag;
	}
	
	private boolean check(int flag) {
		return (mode & flag) == flag;
	}
	
	private void callStateListener(boolean value) {
		if (stateListener == null) return;
		if (value) {
			stateListener.prepare();
		} else {
			stateListener.error();
		}
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
		onMode(SMODE_COMPLETE);
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
	
	public boolean isPrepared() {
		return check(SMODE_PREPARED);
	}
	
	public boolean showPlayPause() {
		return check(SMODE_PLAY_PAUSE);
	}
	
	public boolean isComplete() {
		return check(SMODE_COMPLETE);
	}
	
	public String getCurrentPath() {
		return currentPath;
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
