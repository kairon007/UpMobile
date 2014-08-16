package org.kreed.musicdownloader;

import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Player {

	private Context context;
	private MediaPlayer mediaPlayer;
	private File songFile;
	private View view;
	private ProgressBar songProgress;
	private ImageButton buttonPlay;
	private TextView songTitle;
	private TextView songDuration;
	private String path;
	private String strTitle;
	private String strDuration;
	private String from;
	private boolean prepared = false;
	private boolean playOrPause = true; // if false buttonPlay == R.drawable.pause, else buttonPlay ==  R.drawable.play
	private Runnable progressAction = new Runnable() {
		
		@Override
		public void run() {
			try {
				int current = mediaPlayer.getCurrentPosition();
				int total = mediaPlayer.getDuration();
				songProgress.setProgress(current);
				songDuration.setText(formatTime(current) + " / " + formatTime(total));
				songProgress.postDelayed(this, 1000);
			} catch (NullPointerException e) {
			}
		}
		
	};
	
	public Player(Context context, String path, String strArtist, String strTitle, String strDuration, String from) {
		this.context = context;
		this.path = path;
		this.strTitle = strArtist + " - " + strTitle;
		this.strDuration = strDuration;
		this.from = from;
	}
	
	public String getStrTitle() {
		return strTitle;
	}

	public void setStrTitle(String strTitle) {
		this.strTitle = strTitle;
	}

	public String getStrDuration() {
		return strDuration;
	}

	public void setStrDuration(String strDuration) {
		this.strDuration = strDuration;
	}

	public void play() {
		view = LayoutInflater.from(context).inflate(R.layout.player, null);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		PlaySong task = new PlaySong();
		if (task.getStatus() == Status.PENDING) {
			task.execute("");
		}
	}
	
	public void remove(){
		mediaPlayer.stop();
		mediaPlayer = null;
	}

	private void releasePlayer() {
		if (null != mediaPlayer) {
			songProgress.removeCallbacks(progressAction);
			try {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
					playOrPause = true;
				}
			} catch (IllegalStateException e) {
			}
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
			prepared = false;
		}
	}

	public View getView() {
		view = LayoutInflater.from(context).inflate(R.layout.player, null);
		buttonPlay = (ImageButton) view.findViewById(R.id.player_play_song);
		songTitle = (TextView) view.findViewById(R.id.player_title_song);
		songDuration = (TextView) view.findViewById(R.id.player_duration_song);
		if (songProgress == null) {
			songProgress = (ProgressBar) view.findViewById(R.id.player_progress_song);
		} else {
			int duration = mediaPlayer.getDuration();
			songProgress.removeCallbacks(progressAction);
			songProgress = null;
			songProgress = (ProgressBar) view.findViewById(R.id.player_progress_song);
			songProgress.setIndeterminate(false);
			songProgress.setProgress(0);
			songProgress.setMax(duration);
			songProgress.post(progressAction);
		}
		songTitle.setText(strTitle);
		songDuration.setText(strDuration);
		if (playOrPause) {
			buttonPlay.setImageResource(R.drawable.play);
		} else {
			buttonPlay.setImageResource(R.drawable.pause);
		}
		buttonPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPause();
			}
		});
		ViewGroup parent = (ViewGroup) view.getParent();
		if (null != parent) {
			parent.removeView(view);
		}
		return view;
	}

	public void onPrepared() {
		int duration = mediaPlayer.getDuration();
		if (duration == -1) {
			songProgress.setIndeterminate(true);
			songProgress.setVisibility(View.INVISIBLE);
		} else {
			songProgress.setVisibility(View.VISIBLE);
			songDuration.setText(formatTime(duration));
			songProgress.setIndeterminate(false);
			songProgress.setProgress(0);
			songProgress.setMax(duration);
			songProgress.postDelayed(progressAction, 1000);
		}
	}

	public void onPaused() {
		buttonPlay.setImageResource(R.drawable.play);
	}

	public void onResumed() {
		buttonPlay.setImageResource(R.drawable.pause);
	}

	public void onFinished() {
		songProgress.setIndeterminate(false);
		songProgress.setProgress(100);
		songProgress.setMax(100);
		songProgress.removeCallbacks(progressAction);
	}

	public void playPause() {
		if (null == mediaPlayer)
			return;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			playOrPause = true;
			onPaused();
		} else {
			songProgress.setVisibility(View.VISIBLE);
			mediaPlayer.start();
			playOrPause = false;
			onResumed();
		}
	}

	private String formatTime(int duration) {
		duration /= 1000;
		int min = duration / 60;
		int sec = duration % 60;
		return String.format("%d:%02d", min, sec);
	}

	private class PlaySong extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				switch (from) {
				case PrefKeys.CALL_FROM_LIBRARY:
					songFile = new File(path);
					FileInputStream inputStream = new FileInputStream(songFile);
					mediaPlayer.setDataSource(inputStream.getFD());
					inputStream.close();
					break;
				case PrefKeys.CALL_FROM_SERCH:
					mediaPlayer.setDataSource(path);
					break;
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
			if (result) {
				mediaPlayer.start();
				if(path.equals(PrefKeys.CALL_FROM_SERCH)){
					int duration = mediaPlayer.getDuration();
					songProgress.removeCallbacks(progressAction);
					songProgress = null;
					songProgress = (ProgressBar) view.findViewById(R.id.player_progress_song);
					songProgress.setIndeterminate(false);
					songProgress.setProgress(0);
					songProgress.setMax(duration);
					songProgress.post(progressAction);
				}
				playOrPause = false;
				onResumed();
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						releasePlayer();
						onFinished();
					}
					
				});
				onPrepared();
			}
		}
	}
	
}