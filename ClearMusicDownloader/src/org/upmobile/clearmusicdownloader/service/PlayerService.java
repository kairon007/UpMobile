package org.upmobile.clearmusicdownloader.service;

import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
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
	
	//constants
	public static final int CURRENT_SONG = -1;
	private static final int MODE_PLAYING = 0x00000001;	
	private static final int MODE_PREPARED = 0x00000002;	
	
	//multy-threading
	private static final Object wait = new Object();
	private final Object lock = new Object(); 
	private Looper looper;
	private Handler handler;
	
	//instance
	public static PlayerService instance;
	private MediaPlayer player;
	
	
	private ArrayList<AbstractSong> queue;
//	private String path;
	private int mode;
	private AbstractSong current;
	
	private boolean isPrepared;
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
			try {
				Uri uri = Uri.parse((String) msg.obj);
				player.setDataSource(this, uri);
				player.prepare();
				player.start();
			} catch (Exception e) {
				android.util.Log.d("log", "Appear problem: " + e);
				return false;
			}
			break;
			
		case Constants.MSG_PLAY_CURRENT:
			player.start();
			
		case Constants.MSG_PAUSE:
				player.pause();
			break;

		case Constants.MSG_NEXT :
			
			break;
			
		case Constants.MSG_PREVIOUS:
			
			break;
		default:
			break;
		}

		return true;
	}
	
	public void pause() {
		synchronized (lock) {
			Message message = new Message();
			message.what = Constants.MSG_PAUSE;
			handler.sendMessage(message);
		}
	}
	
	public void play(int i) {
		synchronized (lock) {
			Message message = new Message();;
			if (i == CURRENT_SONG && isPrepared) {
				message.what = Constants.MSG_PLAY_CURRENT;
				handler.sendMessage(message);
			} else {
				String path = queue.get(i).getPath();
				message.obj = path;
				message.what = Constants.MSG_PLAY;
				handler.sendMessage(message);
			}
		}
	}
	
	public void previous() {
		synchronized (lock) {
			
		}
	}
	
	public void next() {
		synchronized (lock) {
			
		}
	}
	
	public void setQueue(ArrayList<AbstractSong> list) {
		queue = new ArrayList<AbstractSong>(list);
	}
	
	
	//TODO are we need in this method?
	public void IsContainsSong () {
		
	}

	@Override
	public void onPrepared(MediaPlayer paramMediaPlayer) {
		player = paramMediaPlayer;
		player.start();
		isPrepared = true;
		isComplete = false;
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int paramInt1, int paramInt2) {
		mediaPlayer.release();
		isPrepared = false;
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

	public Object getData () {
		return new Object();
	}
	
	public int getCerrent() {
		return 1;
	}
	
	public boolean isPrepared() {
		return isPrepared;
	}
	
	public boolean isComplete() {
		return isComplete;
	}
//
//	public String getPath() {
//		return path;
//	}

//	public void setPath(String path) {
//		this.path = path;
//	}

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
