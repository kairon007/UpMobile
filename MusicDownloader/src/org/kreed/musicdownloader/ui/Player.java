package org.kreed.musicdownloader.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kreed.musicdownloader.Constants;
import org.kreed.musicdownloader.CustomEqualizer;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.equalizer.ProgressClass;
import ru.johnlife.lifetoolsmp3.equalizer.ProgressDataSource;
import ru.johnlife.lifetoolsmp3.equalizer.widget.Utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Player implements SeekBar.OnSeekBarChangeListener, OnClickListener, OnPreparedListener, OnCompletionListener {

	private static final int IMAGE_PLAY = R.drawable.play;
	private static final int IMAGE_PAUSE = R.drawable.pause;
	private ArrayList<String[]> header;
	private MediaPlayer mediaPlayer;
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
	private Integer currentImageButton;
	private boolean prepared = false;
	private byte playerState = 0;
	private int customAudioSessionId = -1;

	private Runnable progressAction = new Runnable() {

		@Override
		public void run() {
			try {
				if (mediaPlayer != null) {
					int current = mediaPlayer.getCurrentPosition();
					int total = mediaPlayer.getDuration();
					songProgress.setProgress(current);
					songDuration.setText(Util.getFormatedStrDuration(current) + " / " + Util.getFormatedStrDuration(total));
					songProgress.postDelayed(this, 1000);
				}
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e.getMessage());
			}
		}

	};

	public Player(ArrayList<String[]> header, MusicData data) {
		title = data.getSongTitle();
		artist = data.getSongArtist();
		this.data = data;
		this.header = header;
	}

	public Player() {
	}

	public void setData(ArrayList<String[]> headers, MusicData musicData) {
		stateManagementPlayer(Constants.STOP);
		this.data = musicData;
		this.header = headers;
		title = data.getSongTitle();
		artist = data.getSongArtist();
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

	public void getView(FrameLayout footer) {
		view = footer;
		init(view);
		playerLayout.setVisibility(View.VISIBLE);
		buttonPlay.setVisibility(View.VISIBLE);
		buttonPlay.setOnClickListener(this);
		if (prepared) {
			int duration = mediaPlayer.getDuration();
			songProgress.removeCallbacks(progressAction);
			songProgress.setIndeterminate(false);
			songProgress.setMax(duration);
			songProgress.post(progressAction);
		}
		songProgress.setOnSeekBarChangeListener(this);
		songTitle.setText(title);
		songArtist.setText(artist);
		if (null != currentImageButton) {
			buttonPlay.setImageResource(currentImageButton);
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			Context context = view.getContext();
			buttonPlay.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.selectable_item_bg_honeycomb));
			
		}
	}

	private void init(FrameLayout view) {
		playerLayout = (LinearLayout) view.findViewById(R.id.player_layout);
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
			songDuration.setText(Util.getFormatedStrDuration(duration));
			songProgress.setIndeterminate(false);
			songProgress.setMax(duration);
			songProgress.postDelayed(progressAction, 1000);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		android.util.Log.d("logd", "onProgressChanged: "  + mediaPlayer.getAudioSessionId());
			if (mediaPlayer != null && prepared && fromUser) {
				mediaPlayer.seekTo(progress);
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

	@SuppressLint("NewApi") 
	private void play() {
		stop();
		songArtist.setText(data.getSongArtist());
		songTitle.setText(data.getSongTitle());
		songDuration.setText("");
		songProgress.setIndeterminate(true);
		buttonPlay.setVisibility(View.GONE);
		buttonProgress.setVisibility(View.VISIBLE);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		onPrepareSong();
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnPreparedListener(this);
	}

	private void pause() {
		buttonPlay.setImageResource(IMAGE_PLAY);
		currentImageButton = IMAGE_PLAY;
		mediaPlayer.pause();
	}

	private void restart() {
		if (mediaPlayer != null && prepared && getPlayerState() == Constants.RESTART) {
			mediaPlayer.seekTo(0);
			mediaPlayer.start();
			setActivatedButton(true);
		} else
			setActivatedButton(false);
	}

	private void continuePlaying() {
		mediaPlayer.start();
		buttonPlay.setImageResource(IMAGE_PAUSE);
		currentImageButton = IMAGE_PAUSE;
	}

	private void stop() {
		if (mediaPlayer != null) {
			songProgress.setProgress(0);
			try {
				if (prepared) {
					mediaPlayer.seekTo(0);
					mediaPlayer.stop();
					mediaPlayer.reset();
				}
				mediaPlayer.release();
			} catch (Exception e) {
			}
			mediaPlayer = null; 
			songProgress.removeCallbacks(progressAction);
			prepared = false;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.player_play_song) {
			if (getPlayerState() == Constants.PLAY || getPlayerState() == Constants.CONTINUE_PLAY || getPlayerState() == Constants.RESTART) {
				stateManagementPlayer(Constants.PAUSE);
			} else if (getPlayerState() == Constants.PAUSE) {
				stateManagementPlayer(Constants.CONTINUE_PLAY);
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.seekTo(0);
		try {
			mp.prepare();
		} catch (Exception e) {
		}
		mediaPlayer = mp;
		playerState = Constants.PAUSE;
		buttonPlay.setImageResource(IMAGE_PLAY);
		currentImageButton = IMAGE_PLAY;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mediaPlayer = mp;
		setEqualizer(view.getContext());
		Player.this.onPrepared();
//		if (customAudioSessionId != -1) {
//			mp.setAudioSessionId(customAudioSessionId);
//		}
		prepared = true;
		mp.start();
		if (data.getFileUri().contains("http")) {
			Toast.makeText(view.getContext(), MessageFormat.format("{0} - {1} is playing", artist, title), Toast.LENGTH_SHORT).show();
		}
		setActivatedButton(true);
	}
	
	@SuppressLint("NewApi") 
	public void onPrepareSong() {
		try {
			String path = data.getFileUri();
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("User-Agent", "2.0.0.6 � Debian GNU/Linux 4.0 � Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
				if (header != null && header.get(0) != null) {
					for (int i = 0; i < header.size(); i++) {
						headers.put(header.get(i)[0], header.get(i)[1]);
					}
				}
				mediaPlayer.setDataSource(view.getContext(), Uri.parse(path), headers);
				mediaPlayer.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Equalizer getEqualizer() {
		if (null == mediaPlayer) {
			return new Equalizer(1, customAudioSessionId);
		} else return new Equalizer(1, mediaPlayer.getAudioSessionId());
	}
	
	public BassBoost getBassBoost() {
		if (null == mediaPlayer) {
			return new BassBoost(2, customAudioSessionId);
		} else return new BassBoost(2, mediaPlayer.getAudioSessionId());
	}
	 	
	public Virtualizer getVirtualizer() {
		if (null == mediaPlayer) {
			return new Virtualizer(3, customAudioSessionId);
		} else return new Virtualizer(3, mediaPlayer.getAudioSessionId());
	}
	
	public void setEqualizer(Context context) {
		if (Utils.getEqPrefs(context)) {
			Equalizer equalizer = getEqualizer();
			BassBoost bassBoost = getBassBoost();
			Virtualizer virtualizer = getVirtualizer();
			android.util.Log.d("logd", "setEqualizer: " + virtualizer.getId());
			equalizer.setEnabled(true);
			bassBoost.setEnabled(true);
			virtualizer.setEnabled(true);
			ProgressDataSource myProgressDataSource = new ProgressDataSource(context);
			myProgressDataSource.open();
			List<ProgressClass> values = myProgressDataSource.getAllPgs();
			if (values.size() == 0)
				myProgressDataSource.createProgress(0, 0, 0, 0, 0, "Custom", 0, 0);
			else {
				//Set equalizer
				Utils.changeAtBand(equalizer, (short) 0, values.get(0).getProgress(1) - 15);
				Utils.changeAtBand(equalizer, (short) 1, values.get(0).getProgress(2) - 15);
				Utils.changeAtBand(equalizer, (short) 2, values.get(0).getProgress(3) - 15);
				Utils.changeAtBand(equalizer, (short) 3, values.get(0).getProgress(4) - 15);
				Utils.changeAtBand(equalizer, (short) 4, values.get(0).getProgress(5) - 15);
				//Set bassboost
				bassBoost.setStrength((short)(values.get(0).getArc(1) * 10));
				//Set virtualizer
				virtualizer.setStrength((short)(values.get(0).getArc(2) * 10));
			}
		}
	}
	
	public int getPlayerVisibility() {
		return playerLayout.getVisibility();
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
	
	public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}

	public MusicData getData() {
		return data;
	}

	public void setCustomAudioSessionId(int customAudioSessionId) {
		this.customAudioSessionId = customAudioSessionId;
	}
	
	public int getCustomAudioSessionId() {
		return customAudioSessionId;
	}
}