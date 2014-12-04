package org.upmobile.clearmusicdownloader.ui;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;

public class PlayerService extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener{
	
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
//			while (sInstance == null) {
//				try {
//					synchronized (sWait) {
//						sWait.wait();
//					}
//				} catch (Exception e) {
//					Log.d(PlayerService.class.getSimpleName(), e + "");
//				}
//			}
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
//		synchronized (sWait) {
//			sWait.notifyAll();
//		}
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
		// TODO Auto-generated method stub
		
	}

	private void restart() {
		// TODO Auto-generated method stub
		
	}

	private void stop() {
		// TODO Auto-generated method stub
		
	}

	private void pause() {
		// TODO Auto-generated method stub
		
	}

	private void play() {
		
	}

	@Override
	public void onPrepared(MediaPlayer paramMediaPlayer) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		// TODO Auto-generated method stub
	}
	
}
