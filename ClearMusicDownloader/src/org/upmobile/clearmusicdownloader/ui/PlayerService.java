package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;

import ru.johnlife.lifetoolsmp3.Util;
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
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerService extends Service implements OnCompletionListener, OnErrorListener, OnPreparedListener, OnClickListener, OnSeekBarChangeListener{
	
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
	private String title;
	private String artist;
	private long totalTime;
	private String currenTime;
	private String path;
	private View playerView;
	private ImageButton play, previous, forward, editTag, showLyrics;
	private Button download;
	private SeekBar playerProgress;
	private TextView playerTitle, playerArtist, playerTotalTime, playerCurrTime;
	
	
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
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnPreparedListener(this);
		super.onCreate();
	}
	
	private void init() {
		play = (ImageButton) playerView.findViewById(R.id.player_play);
		previous = (ImageButton) playerView.findViewById(R.id.player_previous);
		forward = (ImageButton) playerView.findViewById(R.id.player_forward);
		download = (Button) playerView.findViewById(R.id.player_download);
		showLyrics = (ImageButton) playerView.findViewById(R.id.player_lyrics);
		editTag = (ImageButton) playerView.findViewById(R.id.player_edit_tags);
		playerProgress = (SeekBar) playerView.findViewById(R.id.player_progress);
		playerTitle = (TextView) playerView.findViewById(R.id.player_title);
		playerArtist = (TextView) playerView.findViewById(R.id.player_artist);
		playerCurrTime = (TextView) playerView.findViewById(R.id.player_current_time);
		playerTotalTime = (TextView) playerView.findViewById(R.id.player_total_time);
		playerProgress.setOnSeekBarChangeListener(this);
		play.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		download.setOnClickListener(this);
		editTag.setOnClickListener(this);
		showLyrics.setOnClickListener(this);
	}
	
	public void setSongObject(Object obj) {
		if(null == obj) return;
		if (obj.getClass().equals(RemoteSong.class)) {
		title = ((RemoteSong) obj).getTitle();
		artist = ((RemoteSong) obj).getArtist();
		totalTime = ((RemoteSong) obj).getDuration();
		((RemoteSong) obj).getDownloadUrl(new DownloadUrlListener() {
			
			@Override
			public void success(String url) {
				path = url;
			}
			
			@Override
			public void error(String error) {
				// TODO Auto-generated method stub
			}
		});
		} else if (obj.getClass().equals(MusicData.class)) {
			title = ((MusicData) obj).getTitle();
			artist = ((MusicData) obj).getArtist();
			totalTime = ((MusicData) obj).getDuration();
			path = ((MusicData) obj).getPath();
		}
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
		player.start();
		changePlayPauseView(true);
	}

	private void restart() {
		player.seekTo(0);
		player.start();
		changePlayPauseView(true);
	}

	private void stop() {
		player.stop();
		player.reset();
		playerProgress.removeCallbacks(progressAction);
	}

	private void pause() {
		player.pause();
		changePlayPauseView(false);
	}

	private void play() {
		playerArtist.setText(artist);
		playerTitle.setText(title);
		playerProgress.setProgress(0);
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		try {
			stop();
			player.setDataSource(sInstance, Uri.parse(path));
			player.prepareAsync();
		} catch (Exception e) {

		}
	}

	@Override
	public void onPrepared(MediaPlayer paramMediaPlayer) {
		player = paramMediaPlayer;
		player.start();
		changePlayPauseView(true);
		playerProgress.setMax(player.getDuration());
		playerProgress.post(progressAction);
	}

	@Override
	public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer paramMediaPlayer) {
		playerProgress.setProgress(0);
		player.seekTo(0);
	}

	public void setPlayerView(View parentView) {
		this.playerView = parentView;
		init();
	}
	
	private Runnable progressAction = new Runnable() {

		@Override
		public void run() {
			try {
				if (player != null) {
					int current = player.getCurrentPosition();
					int total = player.getDuration();
					playerProgress.setProgress(current);
					playerCurrTime.setText(Util.getFormatedStrDuration(current));
					playerTotalTime.setText(Util.getFormatedStrDuration(total));
					playerProgress.postDelayed(this, 1000);
				}
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e.getMessage());
			}
		}

	};
	//TODO improve the method
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.pause);
		} else {
			play.setImageResource(R.drawable.play);
		}
	}

	@Override
	public void onClick(View paramView) {
		//TODO for all buttons
		switch (paramView.getId()) {
		case R.id.player_play:
			Toast toast = Toast.makeText(sInstance, "onClick play", Toast.LENGTH_SHORT);
			toast.show();
			stateManagementPlayer((byte) 1);
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onProgressChanged(SeekBar paramSeekBar, int paramInt, boolean paramBoolean) {
		if (paramBoolean) {
			player.seekTo(paramInt);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar paramSeekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar paramSeekBar) {
		// TODO Auto-generated method stub
	}
}
