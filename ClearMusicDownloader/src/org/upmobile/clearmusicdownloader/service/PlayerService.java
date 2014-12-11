package org.upmobile.clearmusicdownloader.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
	private MediaPlayer player;
	private OnStatePlayerListener stateListener;
	private String currentPath;
	private int mode;
	
	public interface OnStatePlayerListener {
		
		public void prepare();
		public void error();
		
	}
	
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
		Message message = new Message();
		synchronized (lock) {
			if (path.equals(currentPath)) {
				if (check(SMODE_PLAY_PAUSE)) {
					message.what = MSG_PLAY_CURRENT;
					offMode(SMODE_PLAY_PAUSE);
				} else {
					message.what = MSG_PAUSE;
					onMode(SMODE_PLAY_PAUSE);
				}
			} else {
				offMode(SMODE_PLAY_PAUSE);
				message.what = MSG_PLAY;
				message.obj = path;
			}
			currentPath = path;
			handler.sendMessage(message);
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

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		Message message = new Message();
		message.what = MSG_ERROR;
		message.arg1 = what;
		message.arg2 = extra;
		handler.sendMessage(message);
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		onMode(SMODE_COMPLETE);
		Message message = new Message();
		message.what  = MSG_SEEK_TO;
		message.arg1 = 0;
		handler.sendMessage(message);
	}

	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	public void seekTo(int progress) {
		Message message = new Message();
		message.what  = MSG_SEEK_TO;
		message.arg1 = progress;
		handler.sendMessage(message);
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
