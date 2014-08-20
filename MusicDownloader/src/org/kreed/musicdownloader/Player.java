package org.kreed.musicdownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
	private boolean playFinish = false;
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
			} catch (Exception e) {
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
		if (null != mediaPlayer) {
			mediaPlayer.stop();
			mediaPlayer = null;
		}
	}
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
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
		songTitle.startAnimation(AnimationUtils.loadAnimation(context, R.anim.move_text));
		songDuration.setText(strDuration);
		if (playOrPause) {
			buttonPlay.setImageResource(R.drawable.play);
		} else {
			buttonPlay.setImageResource(R.drawable.pause);
		}
		buttonPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (playFinish) {
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					PlaySong task = new PlaySong();
					playFinish = false;
					if (task.getStatus() == Status.PENDING) {
						task.execute("");
					}
				}
				playPause();
			}

		});
		ViewGroup parent = (ViewGroup) view.getParent();
		if (null != parent) {
			parent.removeView(view);
		}
		return view;
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
			songProgress.setProgress(0);
			songProgress.setMax(duration);
			songProgress.postDelayed(progressAction, 1000);
		}
	}

	private void onPaused() {
		buttonPlay.setImageResource(R.drawable.play);
	}

	private void onResumed() {
		buttonPlay.setImageResource(R.drawable.pause);
	}

	private void onFinished() {
		songProgress.setIndeterminate(false);
		songProgress.setProgress(100);
		songProgress.setMax(100);
		songProgress.removeCallbacks(progressAction);
	}

	public void playPause() {
		if (null == mediaPlayer) {
			return;
		}
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

		@SuppressLint("NewApi")
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				if(from.equals(PrefKeys.CALL_FROM_LIBRARY)){
					songFile = new File(path);
					FileInputStream inputStream = new FileInputStream(songFile);
					mediaPlayer.setDataSource(inputStream.getFD());
					inputStream.close();
				} else if (from.equals(PrefKeys.CALL_FROM_SERCH)) {
					HashMap<String, String> headers = new HashMap<String,String>();
					headers.put("User-Agent", "2.0.0.6 â Debian GNU/Linux 4.0 — Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
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
				Log.d("log", "e = "+e);
				if (from == PrefKeys.CALL_FROM_LIBRARY && !songFile.exists()){
					String str = songFile.getAbsolutePath();
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
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						e1.printStackTrace();
					} catch (IllegalStateException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
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
						buttonPlay.setImageResource(R.drawable.play);
						playFinish = true;
					}
					
				});
				onPrepared();
			}
		}
	}
	
}