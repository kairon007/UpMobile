package org.upmobile.clearmusicdownloader.service;

import java.util.ArrayList;
import java.util.Stack;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
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
	private static final String EMPTY_PATH = "send.empty.path";
	private static final int SMODE_PLAYING = 0x00000001;	
	private static final int SMODE_PREPARED = 0x00000002;
	
	//multy-threading
	private static final Object wait = new Object();
	private final Object lock = new Object(); 
	private Looper looper;
	private Handler handler;
	
	//instance
	public static PlayerService instance;
	private MediaPlayer player;
	
	
	private ArrayList<AbstractSong> queue;
	private Stack<AbstractSong> stack;
	private int mode;
	
	private int currentPosition = -1;
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
		stack = new Stack<AbstractSong>();
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
		player.release();
		looper.quit();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		//TODO handleMessage
		mode |= SMODE_PLAYING;
		switch (msg.what) {
		case Constants.MSG_PLAY:
			try {
				if (msg.obj.equals(EMPTY_PATH)) {
					RemoteSong song = (RemoteSong) queue.get(currentPosition);
					song.getDownloadUrl(new DownloadUrlListener() {
						
						@Override
						public void success(String url) {
							try {
								Uri uri = Uri.parse(url);
								player.setDataSource(PlayerService.this, uri);
								player.prepareAsync();
								synchronized (wait) {
									wait.wait();
								}
							} catch (Exception e) {
								android.util.Log.d("log", "in method \"success\" into \"handleMessage\" appear problem: " + e);
							}
						}
						
						@Override
						public void error(String error) {
							
						}
						
					});
					break;
				}
				Uri uri = Uri.parse((String) msg.obj);
				player.setDataSource(this, uri);
				player.prepareAsync();
				synchronized (wait) {
					wait.wait();
				}
			} catch (Exception e) {
				android.util.Log.d("log", "in method \"handleMessage\" appear problem: " + e);
				return false;
			}
			break;

		case Constants.MSG_PLAY_CURRENT:
			player.start();
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
		if ((mode & SMODE_PLAYING) != SMODE_PLAYING) return;
		synchronized (lock) {
			Message message = new Message();
			message.what = Constants.MSG_PAUSE;
			handler.sendMessage(message);
		}
	}

	public AbstractSong play(int i) {
		Message message = new Message();
		AbstractSong song = queue.get(i);
		synchronized (lock) {
			if (currentPosition != -1 && currentPosition != i) {
				player.reset();
				message.what = Constants.MSG_PLAY;
			} else if ((mode & SMODE_PLAYING) == SMODE_PLAYING) {
				message.what = Constants.MSG_PLAY_CURRENT;
			} else {
				message.what = Constants.MSG_PLAY;
			}
			if (song.getClass() == RemoteSong.class){
				message.obj = EMPTY_PATH;
			} else if (song.getClass() == MusicData.class) {
				message.obj = song.getPath();
			} else {
				android.util.Log.d("log", "unknown type of data in object field");
			}
			stack.push(song);
			currentPosition = i;
			handler.sendMessage(message);
		}
		return song;
	}

	public AbstractSong previous() {
		AbstractSong song = null;
		if (null != stack && !stack.isEmpty()) {
			song = stack.pop();
			currentPosition = -1;
			play(song);
		}
		return song;
	}

	public AbstractSong forward() {
		AbstractSong song = null;
		currentPosition++;
		if (currentPosition >= queue.size()) {
			currentPosition = queue.size() - 1;
		}
		song = queue.get(currentPosition);
		play(song);
		return song;
	}
	
	private void play (AbstractSong song) {
		synchronized (lock) {
			player.reset();
			Message message = new Message();
			if (song.getClass() == RemoteSong.class) {
				message.obj = EMPTY_PATH;
			} else {
				message.obj = song.getPath();
			}
			message.what = Constants.MSG_PLAY;
			handler.sendMessage(message);
		}
	}

	public void setQueue(ArrayList<AbstractSong> list) {
		queue = new ArrayList<AbstractSong>(list);
	}

	// TODO are we need in this method?
//	public void IsContainsSong() {
//
//	}

	@Override
	public void onPrepared(MediaPlayer paramMediaPlayer) {
		//TODO onPrepared
		player = paramMediaPlayer;
		player.start();
		isPrepared = true;
		isComplete = false;
		synchronized (wait) {
			wait.notifyAll();
		}
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int paramInt1, int paramInt2) {
		mediaPlayer.release();
		mode ^= SMODE_PLAYING;
		isPrepared = false;
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		isComplete = true;
		mode ^= SMODE_PLAYING;
		player.seekTo(0);
	}

	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	public void seekTo(int progress) {
		player.seekTo(progress);
	}

//	public Object getData () {
//		return new Object();
//	}
//	
//	public int getCerrent() {
//		return 1;
//	}
	
	public boolean isPrepared() {
		return isPrepared;
	}
	
	public boolean isComplete() {
		return isComplete;
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
