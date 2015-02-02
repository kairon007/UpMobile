package org.upmobile.musicpro.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;
import org.upmobile.musicpro.config.GlobalValue;
import org.upmobile.musicpro.object.Song;
import org.upmobile.musicpro.util.Logger;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class MusicService extends Service {
	private final IBinder mBinder = new ServiceBinder();
	private List<Song> listSongs;
	private MediaPlayer mPlayer;
	private int length;
	private int lengthSong;
	private PlayerListener listener;
	private boolean isPause;
	private boolean isUpdatingSeek;
	private boolean isShuffle;
	private boolean isRepeat;
	private Handler mHandler;

	private final Handler handler = new Handler();
	private final Runnable r = new Runnable() {
		@Override
		public void run() {
			updateSeekProgress();
		}
	};

	public class ServiceBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPlayer = new MediaPlayer();
		mPlayer.setVolume(100, 100);
		isPause = false;
		setPlayer(mPlayer);
//		mPlayer.setOnErrorListener(new OnErrorListener() {
//			public boolean onError(MediaPlayer mp, int what, int extra) {
//				onMediaPlayerError(mPlayer, what, extra);
//				return true;
//			}
//		});
//		
//		mPlayer.setOnCompletionListener(new OnCompletionListener() {
//			@Override
//			public void onCompletion(MediaPlayer mp) {
//				nextSong();
//			}
//		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mHandler = new Handler();
		return START_STICKY;
	}

	private void updateSeekProgress() {
		try {
			listener.onSeekChanged(
					getLengSong(),
					getTime(mPlayer.getCurrentPosition()),
					(int) (((float) mPlayer.getCurrentPosition() / lengthSong) * 100));
			handler.postDelayed(r, 1000);
		} catch (Exception e) {
			handler.postDelayed(r, 1000);
		}
	}
	
	private void setPlayer(final MediaPlayer mPlayer){
		mPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				onMediaPlayerError(mPlayer, what, extra);
				return true;
			}
		});
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				nextSong();
			}
		});
	}

	public boolean isPause() {
		return isPause;
	}

	public void setPause(boolean isPause) {
		this.isPause = isPause;
	}

	public void changeStatePause() {
		isPause = !isPause;
	}

	public boolean isPlay() {
		try {
			return mPlayer.isPlaying();
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isRepeat() {
		return isRepeat;
	}

	public void setRepeat(boolean isRepeat) {
		this.isRepeat = isRepeat;
	}

	public boolean isShuffle() {
		return isShuffle;
	}

	public void setShuffle(boolean isShuffle) {
		this.isShuffle = isShuffle;
	}

	public void setListener(PlayerListener listener) {
		this.listener = listener;
	}

	public void setListSongs(List<Song> listSongs) {
		if (this.listSongs == null) {
			this.listSongs = new ArrayList<Song>();
		}
		this.listSongs.clear();
		this.listSongs.addAll(listSongs);
	}

	public List<Song> getListSongs() {
		return listSongs;
	}

	public void addSong(Song song) {
		if (listSongs == null) {
			listSongs = new ArrayList<Song>();
		}
		listSongs.add(song);
	}

	public void startMusic() {
		try {
			if (!mPlayer.isPlaying()) {
				if (isPause) {
					resumeMusic();
				} else {
					try {
						mPlayer.reset();			
						setPlayer(mPlayer);
						mPlayer.setDataSource(listSongs.get(0).getUrl());
						mPlayer.prepare();
					} catch (Exception e) {
						e.printStackTrace();
						startMusic(0);
						return;
					}
					lengthSong = mPlayer.getDuration();

					mPlayer.start();
					listener.onChangeSong(0);
					if (!isUpdatingSeek) {
						isUpdatingSeek = true;
						updateSeekProgress();
					}
					sendNotification();
				}
				isPause = false;
			}
		} catch (Exception e) {
			startMusic(0);
		}
	}

	public void startMusic(int index) {
		GlobalValue.currentSongPlay = index;
		try {
			mPlayer.reset();			
			setPlayer(mPlayer);
		} catch (Exception e) {
			e.printStackTrace();
			mPlayer = new MediaPlayer();
			setPlayer(mPlayer);
		}
		try {
			if (listSongs.get(index).getUrl() == null) {
				mPlayer.setDataSource(listSongs.get(index).getUrl());
			} else {
				mPlayer.setDataSource(listSongs.get(index).getUrl());
			}
			mPlayer.prepareAsync();
			mPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					lengthSong = mPlayer.getDuration();

					mPlayer.start();
					if (!isUpdatingSeek) {
						isUpdatingSeek = true;
						updateSeekProgress();
					}
				}
			});
			listener.onChangeSong(index);
			sendNotification();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void playOrPauseMusic() {
		if (isPause) {
			resumeMusic();
		} else {
			pauseMusic();
		}
	}

	public void pauseMusic() {
		try {
			if (mPlayer.isPlaying()) {
				isPause = true;
				length = mPlayer.getCurrentPosition();
				mPlayer.pause();
				isUpdatingSeek = false;
			}
			cancelNotification();
		} catch (Exception e) {
		}
	}

	public void resumeMusic() {
		if (isPause) {
			if (!seekTo(length)) return;
			mPlayer.start();
			isPause = false;
			sendNotification();
			if (!isUpdatingSeek) {
				isUpdatingSeek = true;
				updateSeekProgress();
			}
		}
	}

	public void stopMusic() {
		try {
			mPlayer.stop();
			mPlayer.release();
			handler.removeCallbacks(null);
			isUpdatingSeek = false;
			isPause = false;
			listener.onChangeSong(0);
			GlobalValue.currentSongPlay = 0;
			cancelNotification();
		} catch (Exception e) {
		}
	}
	
	public void reset() {
		if (null != mPlayer && isPlay()) {
			mPlayer.reset();
		}
	}

	public boolean seekTo(int progress) {
		if (null == mPlayer) return false;
		mPlayer.seekTo(lengthSong * progress / 100);
		return true;
	}

	public void backSong() {
		int currentPosition;
		int newPosition;
		try {
			currentPosition = mPlayer.getCurrentPosition();
		} catch (Exception e) {
			currentPosition = 0;
		}
		if (isShuffle) {
			newPosition = new Random().nextInt(listSongs.size());
		} else {
			if (currentPosition < 10000 && GlobalValue.currentSongPlay > 0) {
				newPosition = GlobalValue.currentSongPlay - 1;
			} else {
				newPosition = GlobalValue.currentSongPlay;
			}
		}
		startMusic(newPosition);
		listener.onChangeSong(newPosition);
	}

	public void backSongByOnClick() {
		int currentPosition;
		int newPosition;
		try {
			currentPosition = mPlayer.getCurrentPosition();
		} catch (Exception e) {
			currentPosition = 0;
		}
		if (isShuffle) {
			newPosition = new Random().nextInt(listSongs.size());
		} else {
			if (isPause) {
				if (GlobalValue.currentSongPlay == 0) {
					newPosition = GlobalValue.listSongPlay.size() - 1;
				} else {
					newPosition = GlobalValue.currentSongPlay - 1;
				}
			} else {
				if (currentPosition < 10000 && GlobalValue.currentSongPlay == 0) {
					newPosition = GlobalValue.listSongPlay.size() - 1;
				} else {
					backSong();
					return;
				}
			}
		}
		startMusic(newPosition);
		listener.onChangeSong(newPosition);
	}

	public void nextSong() {
		int newPosition;
		if (isShuffle) {
			newPosition = new Random().nextInt(listSongs.size());
		} else {
			if (GlobalValue.currentSongPlay < listSongs.size() - 1) {
				newPosition = GlobalValue.currentSongPlay + 1;
			} else {
				if (isRepeat) {
					newPosition = 0;
				} else {
					stopMusic();
					return;
				}
			}
		}
		startMusic(newPosition);
		listener.onChangeSong(newPosition);
	}

	public void nextSongByOnClick() {
		int newPosition;
		if (isShuffle) {
			newPosition = new Random().nextInt(listSongs.size());
		} else {
			if (GlobalValue.currentSongPlay == GlobalValue.listSongPlay.size() - 1) {
				newPosition = 0;
			} else {
				nextSong();
				return;
			}
		}
		startMusic(newPosition);
		listener.onChangeSong(newPosition);
	}

	public String getLengSong() {
		return getTime(lengthSong);
	}

	@SuppressLint("DefaultLocale")
	private String getTime(int millis) {
		long second = (millis / 1000) % 60;
		long minute = millis / (1000 * 60);
		return String.format("%02d:%02d", minute, second);
	}

	private void sendNotification() {
		Song song = listSongs.get(GlobalValue.currentSongPlay);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("notification", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				intent, 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(song.getName())
				.setTicker(song.getName() + "\n" + song.getArtist())
				.setContentText(song.getArtist()).setOngoing(true)
				.setContentIntent(contentIntent);
		mBuilder.setOngoing(false);
		mNotificationManager.notify(MainActivity.NOTIFICATION_ID,
				mBuilder.build());
	}

	private void cancelNotification() {
		NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nMgr.cancel(MainActivity.NOTIFICATION_ID);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.e("", "onDestroy");
		if (mPlayer != null) {
			try {
				mPlayer.stop();
				mPlayer.release();
			} finally {
				mPlayer = null;
			}
		}
	}

	public boolean onMediaPlayerError(MediaPlayer mp, int what, int extra) {
		mHandler.post(new ToastRunnable("There is an error playing this song. Please try again later."));
		if (mPlayer != null) {
			try {
				mPlayer.stop();
				mPlayer.release();
			} finally {
				mPlayer = null;
			}
		}
		return false;
	}
	
	private class ToastRunnable implements Runnable {
	    String mText;

	    public ToastRunnable(String text) {
	        mText = text;
	    }

	    @Override
	    public void run(){
	         Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_LONG).show();
	    }
	}
}
