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
	
	//multy-threading section
	private final Object lock = new Object(); 
	private Looper looper;
	private Handler handler;
	
	//instance section
	public static PlayerService instance;
	private MediaPlayer player;
	private String currentPath;
	private int mode;
	
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
		HandlerThread thread = new HandlerThread("PlayerService", Process.THREAD_PRIORITY_BACKGROUND);
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
			if ((mode & SMODE_PREPARED) == SMODE_PREPARED) {
				player.reset();
				mode ^= SMODE_PREPARED;
			}
			try {
				String path = (String) msg.obj;
				Uri uri = Uri.parse(path);
				player.setDataSource(this, uri);
				player.prepare();
				mode |= SMODE_PREPARED;
				if ((mode & SMODE_COMPLETE) == SMODE_COMPLETE) mode ^= SMODE_COMPLETE;
				player.start();
			} catch (Exception e) {
				android.util.Log.d("log", "in method \"handleMessage\" appear problem: " + e);
				return false;
			}
			break;

		case MSG_PLAY_CURRENT:
			player.start();
			break;

		case MSG_PAUSE:
			player.pause();
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
				if ((mode & SMODE_PLAY_PAUSE) == SMODE_PLAY_PAUSE) {
					message.what = MSG_PLAY_CURRENT;
					mode ^= SMODE_PLAY_PAUSE;
				} else {
					message.what = MSG_PAUSE;
					mode |= SMODE_PLAY_PAUSE;
				}
			} else {
				if ((mode & SMODE_PLAY_PAUSE) == SMODE_PLAY_PAUSE) mode ^= SMODE_PLAY_PAUSE;
				message.what = MSG_PLAY;
				message.obj = path;
			}
			currentPath = path;
			handler.sendMessage(message);
		}
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int paramInt1, int paramInt2) {
		mediaPlayer.release();
		mode ^= SMODE_PREPARED;
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		mode |= SMODE_COMPLETE;
		player.seekTo(0);
	}

	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	public void seekTo(int progress) {
		player.seekTo(progress);
	}
	
	public boolean isPrepared() {
		return (mode & SMODE_PREPARED) == SMODE_PREPARED;
	}
	
	public boolean showPlayPause() {
		return (mode & SMODE_PLAY_PAUSE) == SMODE_PLAY_PAUSE;
	}
	
	public boolean isComplete() {
		return (mode & SMODE_COMPLETE) == SMODE_COMPLETE;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}
}
