package org.upmobile.clearmusicdownloader.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.IBinder;

public class PlayerService extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener {
	
	/**
	 * Object used for Player service startup waiting.
	 */
	private static final Object[] sWait = new Object[0];
	/**
	 * The appplication-wide instance of the Player service.
	 */
	public static PlayerService sInstance;
	private byte playerState;
	private MediaPlayer player;
	private String path;
	private boolean isPrepared = false;
	private boolean isComplete = false;
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}

	@Override
	public IBinder onBind(Intent paramIntent) {
		return null;
	}
	
	/**
	 * 
	 * Return the Player sevice instance, creating one if needed.
	 * @Context use for call
	 */
	public static PlayerService get(Context context) {
		if (sInstance == null) {
			context.startService(new Intent(context, PlayerService.class));
		}
	
		return sInstance;
	}

	/**
	 * Returns true if a Player service instance is active.
	 */
	
	public static boolean hasInstance()
	{
		return sInstance != null;
	}
	
	@Override
	public void onCreate() {
		sInstance = this;
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		super.onCreate();
	}
	
	public void stateManagementPlayer(byte state) {
		this.playerState = state;
		switch (state) {
		case 0: // in progress
			break;
		case 1:
			play();
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

	private void pause() {
		player.pause();
	}

	private void play() {
		try {
			stop();
			player.setDataSource(sInstance, Uri.parse(getPath()));
			player.prepareAsync();
		} catch (Exception e) {

		}
	}

	@Override
	public void onPrepared(MediaPlayer paramMediaPlayer) {
		player = paramMediaPlayer;
		player.start();
		isPrepared = true;
	}

	@Override
	public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
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
}
