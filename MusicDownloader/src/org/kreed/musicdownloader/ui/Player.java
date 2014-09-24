package org.kreed.musicdownloader.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player implements SeekBar.OnSeekBarChangeListener {
	
	private MediaPlayer mediaPlayer;
	private PlaySong downloadSong;
	private MusicData data;
	
	private FrameLayout view;
	private SeekBar songProgress;
	private ProgressBar buttonProgress;
	private ImageButton buttonPlay;
	private TextView songTitle;
	private TextView songArtist;
	private TextView songDuration;
	
	private String title;
	private String artist;
	private String duration;
	private int currentProgress = 0;
	private boolean playFinish = false;
	private boolean prepared = false;
	private ArrayList<String[]> header;
	
	private Runnable progressAction = new Runnable() {
		
		@Override
		public void run() {
			try {
				int current = mediaPlayer.getCurrentPosition();
				int total = mediaPlayer.getDuration();
				songProgress.setProgress(current);
				songDuration.setText(formatTime(current) + " / " + formatTime(total));
				songProgress.postDelayed(this, 1000);
			} catch (Exception e) {
			}
		}
		
	};
	
	public Player(ArrayList<String[]> header, MusicData data) {
		title = data.getSongTitle();
		artist = data.getSongArtist();
		duration = data.getSongDuration();
		this.data = data;
		this.header = header;
	}
	
	@SuppressLint("NewApi") public void play() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		downloadSong = new PlaySong();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			downloadSong.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
		} else {
			downloadSong.execute("");
		}
	}
	
	public void remove(){
		if (null != mediaPlayer) {
			try {
			mediaPlayer.stop();
			mediaPlayer = null;
			} catch (IllegalStateException e) {
			}
		}
	}
	
	public void stopTask() {
		if (downloadSong == null || downloadSong.getStatus() == Status.PENDING){
			return;
		}
		downloadSong.cancel(true);
	}
	
	public void setActivatedButton(boolean value) {
		if (value) {
			buttonPlay.setVisibility(View.VISIBLE);
			buttonProgress.setVisibility(View.GONE);
		} else {
			songProgress.setProgress(0);
			songProgress.setIndeterminate(true);
			buttonPlay.setVisibility(View.GONE);
			buttonProgress.setVisibility(View.VISIBLE);
		}
		buttonPlay.setEnabled(value);
	}
	
	public void restart() {
		if( null != mediaPlayer){
			mediaPlayer.seekTo(0);
			mediaPlayer.start();
			setActivatedButton(true);
			setImageOnButton();
		} 
	}
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
	
	public MusicData getData() {
		return data;
	}
	
	private void releasePlayer() {
		if (null != mediaPlayer) {
			songProgress.removeCallbacks(progressAction);
			try {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
			} catch (IllegalStateException e) {
			}
			mediaPlayer.reset();
			mediaPlayer.release();
			setCurrentProgress(100);
			prepared = false;
		}
	}

	public void stopDownloadSong() {
		if (downloadSong != null) {
			downloadSong.cancel(true);
		}
	}

	public boolean isTaskExecute() {
		if (downloadSong == null) {
			return false;
		}
		return downloadSong.getStatus() != Status.PENDING;
	}

	public void getView(FrameLayout footer) {
		view = footer;
		init(view);
		if (prepared){
			int duration = mediaPlayer.getDuration();
			songProgress.removeCallbacks(progressAction);
			songProgress.setIndeterminate(false);
			songProgress.setMax(duration);
			songProgress.post(progressAction);
		}
		songProgress.setOnSeekBarChangeListener(this);
		songTitle.setText(title);
		songArtist.setText(artist);
		songDuration.setText(duration);
		setImageOnButton();
		buttonPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (playFinish) {
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					downloadSong = new PlaySong();
					playFinish = false;
					if (downloadSong.getStatus() == Status.PENDING) {
						downloadSong.execute("");
					}
				}
				playPause();
			}
			
		});
	}
	
	private void setImageOnButton() {
		if (mediaPlayer == null || !prepared) {
			return;
		}
		if (mediaPlayer.isPlaying()) {
			buttonPlay.setImageResource(R.drawable.pause);
		} else {
			buttonPlay.setImageResource(R.drawable.play);
		}
	}
	
	private void init(FrameLayout view) {
		songProgress = (SeekBar) view.findViewById(R.id.player_progress_song);
		buttonProgress = (ProgressBar) view.findViewById(R.id.player_progress_play);
		buttonPlay = (ImageButton) view.findViewById(R.id.player_play_song);
		songArtist = (TextView) view.findViewById(R.id.player_artist);
		songTitle = (TextView) view.findViewById(R.id.player_title);
		songDuration = (TextView) view.findViewById(R.id.player_duration_song);
	}

	private void onPrepared() {
		int duration = mediaPlayer.getDuration();
		if (duration == -1) {
			songProgress.setIndeterminate(true);
			songProgress.setVisibility(View.INVISIBLE);
		} else {
			songProgress.setVisibility(View.VISIBLE);
			songDuration.setText(formatTime(duration));
			songProgress.setIndeterminate(false);
			songProgress.setMax(duration);
			songProgress.postDelayed(progressAction, 1000);
		}
	}

	private void onFinished() {
		songProgress.setIndeterminate(false);
		songProgress.removeCallbacks(progressAction);
	}

	public void playPause() {
		if (null == mediaPlayer) {
			return;
		}
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		} else {
			songProgress.setVisibility(View.VISIBLE);
			mediaPlayer.start();
		}
		setImageOnButton();
	}

	private String formatTime(int duration) {
		duration /= 1000;
		int min = duration / 60;
		int sec = duration % 60;
		return String.format("%d:%02d", min, sec);
	}

	private class PlaySong extends AsyncTask<String, Void, Boolean> implements OnBufferingUpdateListener {

		@SuppressLint("NewApi")
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				String path = data.getFileUri();
				File songFile = new File(path);
				if (songFile.exists()) {
					FileInputStream inputStream = new FileInputStream(songFile);
					mediaPlayer.setDataSource(inputStream.getFD());
					inputStream.close();
				} else  {
					HashMap<String, String> headers = new HashMap<String, String>();
					headers.put(
							"User-Agent",
							"2.0.0.6 � Debian GNU/Linux 4.0 � Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
					if (header != null && header.get(0) != null) {
						for (int i = 0; i< header.size(); i++) {
							headers.put(header.get(i)[0], header.get(i)[1]);
						}
					}
					mediaPlayer.setDataSource(view.getContext(), Uri.parse(path), headers);
					mediaPlayer.setOnBufferingUpdateListener(this);
				}
				mediaPlayer.prepare();
				prepared = true;
				if (isCancelled()) {
					releasePlayer();
				} else {
					return true;
				}
			} catch (Exception e) {
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result && mediaPlayer!=null) {
				setActivatedButton(true);
				mediaPlayer.seekTo(getCurrentProgress());
				mediaPlayer.start();
				setImageOnButton();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						releasePlayer();
						onFinished();
						buttonPlay.setImageResource(R.drawable.play);
						playFinish = true;
					}
					
				});
				onPrepared();
			}
		}
		@Override
		public void onBufferingUpdate(MediaPlayer arg0, int percent) {
			Log.d("percent", percent + " ");
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			setCurrentProgress(progress);
			if (mediaPlayer != null && prepared) {
				mediaPlayer.seekTo(progress);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
	public int getCurrentProgress() {
		return currentProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		this.currentProgress = currentProgress;
	}
}