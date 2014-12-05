package org.upmobile.clearmusicdownloader.service;

import org.upmobile.clearmusicdownloader.Constants;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

public class PlayerService extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener, Handler.Callback {
	
	/**
	 * Object used for Player service startup waiting.
	 */
	private static final Object wait = new Object();
	/**
	 * The appplication-wide instance of the Player service.
	 */
	public static PlayerService instance;
	private byte playerState;
	private MediaPlayer player;
	private Looper looper;
	private Handler handler;
	private String path;
	private boolean prepared;
	private boolean isPrepared = false;
	private boolean isComplete = false;
	
	
	/**
	 * 
	 * Return the Player sevice instance, creating one if needed.
	 * @Context use for call
	 */
	public static PlayerService get(Context context) {
		if (instance == null) {
			context.startService(new Intent(context, PlayerService.class));
			try {
				synchronized (wait) {
					wait.wait();
				}
			} catch (InterruptedException e) {
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
		HandlerThread thread = new HandlerThread("PlayerService", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		looper = thread.getLooper();
		handler = new Handler(looper, this);
		instance = this;
		synchronized (wait) {
			wait.notifyAll();
		}
	}
	
	@Override
	public void onDestroy() {
		instance = null;
		looper.quit();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case Constants.MSG_PLAY:
			if (!prepared) {
				try {
					Uri uri = Uri.parse((String) msg.obj);
					player.setDataSource(this, uri);
					player.prepare();
					prepared = true;
					player.start();
				} catch (Exception e) {
					android.util.Log.d("log", "!!!!!!!!!!!!!!!!!!!!!!!!Appear problem: " + e);
					return false;
				}
			} else {
				player.start();
			}
			break;
		case Constants.MSG_PAUSE:
				player.pause();
			break;

		default:
			break;
		}

		return true;
	}
	
	public void pause() {
		Message message = new Message();
		message.what = Constants.MSG_PAUSE;
		handler.sendMessage(message);
	}
	
	public void play(String path) {
		Message message = new Message();
		message.obj = path;
		message.what = Constants.MSG_PLAY;
		handler.sendMessage(message);
	}
	
	public void stateManagementPlayer(byte state) {
		this.playerState = state;
		switch (state) {
		case 0: // in progress
			break;
		case 1:
//			play();
			break;
		case 2:
			pause();
			break;
		case 3:
			stop();
			break;
		case 4:
			restart();
			break;
		case 5:
			continuePlaying();
		}
	}

	private void continuePlaying() {
		player.start();
	}

	private void restart() {
		player.seekTo(0);
		player.start();
	}

	private void stop() {
		isPrepared = false;
		player.stop();
		player.reset();
	}

	@Override
	public void onPrepared(MediaPlayer paramMediaPlayer) {
		player = paramMediaPlayer;
		player.start();
		isPrepared = true;
	}

	@Override
	public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		isComplete = true;
		player.seekTo(0);
	}

	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	public void seekTo(int progress) {
		player.seekTo(progress);
	}

	public boolean isPrepared() {
		return isPrepared;
	}
	
	public boolean isComplete() {
		return isComplete;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getDuratioun() {
		return player.getDuration();
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
