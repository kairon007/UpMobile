package org.kreed.musicdownloader.ui;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class Player implements SeekBar.OnSeekBarChangeListener, OnClickListener {
	
	private static final int IMAGE_PLAY = R.drawable.play;
	private static final int IMAGE_PAUSE = R.drawable.pause;
	private ArrayList<String[]> header;
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
	private LinearLayout playerLayout;
	
	private String title;
	private String artist;
	private String duration;
	private Integer currentImageButton;
	private boolean prepared = false;
	private byte playerState = 0;
	
	private Runnable progressAction = new Runnable() {
		
		@Override
		public void run() {
			try { 
				if (mediaPlayer != null) {
					int current = mediaPlayer.getCurrentPosition();
					int total = mediaPlayer.getDuration();
					songProgress.setProgress(current);
					songDuration.setText(Util.formatTimeSimple(current) + " / " + Util.formatTimeSimple(total));
					songProgress.postDelayed(this, 1000);
				}
			} catch (Exception e) {
				Log.d("logd", "" + e);
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
	
	public void setData(ArrayList<String[]> headers, MusicData musicData) {
		if (null != downloadSong && downloadSong.getStatus() != Status.PENDING) {
			downloadSong.cancel(true);
		}
		title = data.getSongTitle();
		artist = data.getSongArtist();
		duration = data.getSongDuration();
		this.data = musicData;
		this.header = headers;
	}
	
	public void setActivatedButton(boolean value) {
		if (value) {
			songProgress.setIndeterminate(false);
			buttonPlay.setVisibility(View.VISIBLE);
			buttonPlay.setImageResource(IMAGE_PAUSE);
			currentImageButton = IMAGE_PAUSE;
			buttonProgress.setVisibility(View.GONE);
		} else {
			songProgress.setProgress(0);
			songProgress.setIndeterminate(true);
			buttonPlay.setVisibility(View.GONE);
			buttonProgress.setVisibility(View.VISIBLE);
		}
	}
	
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}
	
	public MusicData getData() {
		return data;
	}
	

	public void stopDownloadSong() {
		if (downloadSong != null && downloadSong.getStatus() != Status.PENDING) {
			downloadSong.cancel(true);
		}
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
		if(null != currentImageButton) {
			buttonPlay.setImageResource(currentImageButton);
		}
	}
	
	private void init(FrameLayout view) {
		playerLayout = (LinearLayout) view.findViewById(R.id.player_layout);
		playerLayout.setVisibility(View.VISIBLE);
		songProgress = (SeekBar) view.findViewById(R.id.player_progress_song);
		buttonProgress = (ProgressBar) view.findViewById(R.id.player_progress_play);
		buttonPlay = (ImageButton) view.findViewById(R.id.player_play_song);
		songArtist = (TextView) view.findViewById(R.id.player_artist);
		songTitle = (TextView) view.findViewById(R.id.player_title);
		songDuration = (TextView) view.findViewById(R.id.player_duration_song);
		buttonPlay.setOnClickListener(this);
	}

	private void onPrepared() {
		int duration = mediaPlayer.getDuration();
		if (duration == -1) {
			songProgress.setIndeterminate(true);
			songProgress.setVisibility(View.INVISIBLE);
		} else {
			songProgress.setVisibility(View.VISIBLE);
			songDuration.setText(Util.formatTimeSimple(duration));
			songProgress.setIndeterminate(false);
			songProgress.setMax(duration);
			songProgress.postDelayed(progressAction, 1000);
		}
	}

	private class PlaySong extends AsyncTask<String, Void, Boolean> {

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
				} else {
					HashMap<String, String> headers = new HashMap<String, String>();
					headers.put(
							"User-Agent",
							"2.0.0.6 � Debian GNU/Linux 4.0 � Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
					if (header != null && header.get(0) != null) {
						for (int i = 0; i < header.size(); i++) {
							headers.put(header.get(i)[0], header.get(i)[1]);
						}
					}
					if (isCancelled()) return false;
					mediaPlayer.setDataSource(view.getContext(), Uri.parse(path), headers);
				}
				mediaPlayer.prepare();
				prepared = true;
				return true;
			} catch (Exception e) {
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result && mediaPlayer!=null) {
				onPrepared();
				mediaPlayer.start();
				setActivatedButton(true);
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						mp.seekTo(0);
						try {
							mp.prepare();
						} catch (Exception e) {
							android.util.Log.d("log", "Appear problem: " + e);
						}
						mediaPlayer = mp;
						playerState = Constans.PAUSE;
						buttonPlay.setImageResource(IMAGE_PLAY);
						currentImageButton = IMAGE_PLAY;
					}
					
				});
			}
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
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

	public byte getPlayerState() {
		return playerState;
	}

	public void setPlayerState(byte playerState) {
		this.playerState = playerState;
	}
	
	public void stateManagementPlayer(byte state) {
		this.playerState = state;
		switch (state) {
		case 0: //in progress
			break;
		case 1: play();
			break;
		case 2: pause();
			break;
		case 3: stop();
			break;
		case 4: restart();
			break;
		case 5: continuePlaying();
		}
	}
	
	private void play() {
		stopDownloadSong();
		stop();
		songArtist.setText(data.getSongArtist());
		songTitle.setText(data.getSongTitle());
		songDuration.setText("");
		songProgress.setIndeterminate(true);
		buttonPlay.setVisibility(View.GONE);
		buttonProgress.setVisibility(View.VISIBLE);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		downloadSong = new PlaySong();
		downloadSong.execute();
	}
	
	private void pause() {
		buttonPlay.setImageResource(IMAGE_PLAY);
		currentImageButton = IMAGE_PLAY;
		mediaPlayer.pause();
	}
	
	private void restart() {
		if (mediaPlayer != null && (getPlayerState() == Constans.PLAY || getPlayerState() == Constans.CONTINUE_PLAY || getPlayerState() == Constans.RESTART)) {
			if (prepared) {
				mediaPlayer.seekTo(0);
				mediaPlayer.start();
				setActivatedButton(true);
			}  else setActivatedButton(false);
		} else setActivatedButton(false);
	}
	
	private void continuePlaying() {
		mediaPlayer.start();
		buttonPlay.setImageResource(IMAGE_PAUSE);
		currentImageButton = IMAGE_PAUSE;
	}

	private void stop() {
		if (mediaPlayer != null) {
			songProgress.setProgress(0);
			if (prepared) {
				mediaPlayer.seekTo(0);
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
			mediaPlayer = null;
			songProgress.removeCallbacks(progressAction);
			prepared = false;
		}
	}
	
	public void hidePlayerView() {
		playerLayout.setVisibility(View.GONE);
	}

	public boolean isSongProgressIndeterminate() {
		return songProgress.isIndeterminate();
	}

	public void setSongProgressIndeterminate(boolean ind) {
		songProgress.setIndeterminate(ind);

	}

	public int getButtonProgressVisibility() {
		return buttonProgress.getVisibility();
	}

	public void setButtonProgressVisibility(int visibility) {
		buttonProgress.setVisibility(visibility);
		buttonPlay.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.player_play_song) {
			if (getPlayerState() == Constans.PLAY || getPlayerState() == Constans.CONTINUE_PLAY || getPlayerState() == Constans.RESTART) {
				stateManagementPlayer(Constans.PAUSE);
			} else if (getPlayerState() == Constans.PAUSE) {
				stateManagementPlayer(Constans.CONTINUE_PLAY);
			}
		}
	}

}