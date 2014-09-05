package org.kreed.musicdownloader.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.PrefKeys;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.R.drawable;
import org.kreed.musicdownloader.R.id;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Player implements SeekBar.OnSeekBarChangeListener {
	
	private MediaPlayer mediaPlayer;
	private File songFile;
	private PlaySong downloadSong;
	
	private FrameLayout view;
	private SeekBar songProgress;
	private ProgressBar buttonProgress;
	private ImageButton buttonPlay;
	private TextView songTitle;
	private TextView songDuration;
	
	private String path;
	private String strTitle;
	private String strDuration;
	private String from;
	private int position;
	private int currentProgress = 0;
	private boolean playFinish = false;
	private boolean prepared = false;
	
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
	
	public Player(String path, String strArtist, String strTitle, String strDuration, String from, int position) {
		this.path = path;
		this.strTitle = strArtist + " - " + strTitle;
		this.strDuration = strDuration;
		this.from = from;
		this.position = position;
	}
	
	public void play() {
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		downloadSong = new PlaySong();
		downloadSong.execute("");
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
	
	public int getPosition() {
		return position;
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
		songTitle.setText(strTitle);
		songDuration.setText(strDuration);
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
		songTitle = (TextView) view.findViewById(R.id.player_title_song);
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

	private class PlaySong extends AsyncTask<String, Void, Boolean> {

		@SuppressLint("NewApi")
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if(from.equals(Constans.CALL_FROM_LIBRARY)){
					songFile = new File(path);
					FileInputStream inputStream = new FileInputStream(songFile);
					mediaPlayer.setDataSource(inputStream.getFD());
					inputStream.close();
				} else if (from.equals(Constans.CALL_FROM_SERCH)) {
					HashMap<String, String> headers = new HashMap<String,String>();
					headers.put("User-Agent", "2.0.0.6 � Debian GNU/Linux 4.0 � Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
					mediaPlayer.setDataSource(view.getContext(), Uri.parse(path), headers);
				}
				mediaPlayer.prepare();
				prepared = true;
				if (isCancelled()) {
					releasePlayer();
				} else {
					return true;
				}
			} catch (Exception e) {
				if (from == Constans.CALL_FROM_LIBRARY && !songFile.exists()){
					String str = songFile.getPath();
					path = str.split("-1")[0];
					songFile = new File(path);
					try {
						FileInputStream inputStream = new FileInputStream(songFile);
						mediaPlayer.setDataSource(inputStream.getFD());
						inputStream.close();
						mediaPlayer.prepare();
						prepared = true;
						if (isCancelled()) {
							releasePlayer();
						} else {
							return true;
						}
					}  catch (Exception e1) {
						e1.printStackTrace();
					}
				}
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
				if(path.equals(Constans.CALL_FROM_SERCH)){
					int duration = mediaPlayer.getDuration();
					songProgress.removeCallbacks(progressAction);
					songProgress = null;
					songProgress = (SeekBar) view.findViewById(R.id.player_progress_song);
					songProgress.setOnSeekBarChangeListener((OnSeekBarChangeListener) this);
					songProgress.setProgress(getCurrentProgress());
					songProgress.setIndeterminate(false);
					songProgress.setMax(duration);
					songProgress.post(progressAction);
				}
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