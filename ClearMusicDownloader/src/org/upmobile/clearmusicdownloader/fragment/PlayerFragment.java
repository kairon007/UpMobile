package org.upmobile.clearmusicdownloader.fragment;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.DownloadListener;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.RefreshListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerFragment  extends Fragment implements OnClickListener, OnSeekBarChangeListener {

	private View parentView;
	private Object selectedSong;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private Button download;
	private ImageButton showLyrics;
	private ImageButton editTag;
	private SeekBar playerProgress;
	private TextView playerTitle;
	private TextView playerArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private String title;
	private String artist;
	private long totalTime;
	private String path;
	private PlayerService player;
	private Button playerSaveTags;
	private Button playerCancelTags;
	protected DownloadListener downloadListener;
	private ImageView playerCover;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		player = PlayerService.get(getActivity());
		selectedSong = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
		int position = getArguments().getInt(Constants.KEY_SELECTED_POSITION);
		play(position);
		parentView = inflater.inflate(R.layout.player, container, false);
		init();
		setSongObject(selectedSong);
		return parentView;
	}
	
	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.player_play);
		previous = (ImageButton) parentView.findViewById(R.id.player_previous);
		forward = (ImageButton) parentView.findViewById(R.id.player_forward);
		download = (Button) parentView.findViewById(R.id.player_download);
		showLyrics = (ImageButton) parentView.findViewById(R.id.player_lyrics);
		editTag = (ImageButton) parentView.findViewById(R.id.player_edit_tags);
		playerProgress = (SeekBar) parentView.findViewById(R.id.player_progress);
		playerTitle = (TextView) parentView.findViewById(R.id.player_title);
		playerArtist = (TextView) parentView.findViewById(R.id.player_artist);
		playerCurrTime = (TextView) parentView.findViewById(R.id.player_current_time);
		playerTotalTime = (TextView) parentView.findViewById(R.id.player_total_time);
		playerSaveTags = (Button) parentView.findViewById(R.id.player_save_tags);
		playerCancelTags = (Button) parentView.findViewById(R.id.player_cancel_tags);
		playerCover = (ImageView) parentView.findViewById(R.id.player_cover);
		playerProgress.setOnSeekBarChangeListener(this);
		play.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		download.setOnClickListener(this);
		editTag.setOnClickListener(this);
		showLyrics.setOnClickListener(this);
		playerCancelTags.setOnClickListener(this);
		playerSaveTags.setOnClickListener(this);
	}
	
	public void setSongObject(Object obj) {
		if(null == obj) return;
		if (obj.getClass().equals(RemoteSong.class)) {
		initPAramsForOnlineSong(((RemoteSong) obj));
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
			download.setVisibility(View.GONE);
			title = ((MusicData) obj).getTitle();
			artist = ((MusicData) obj).getArtist();
			totalTime = ((MusicData) obj).getDuration();
			path = ((MusicData) obj).getPath();
		}
	}

	private void initPAramsForOnlineSong(RemoteSong remoteSong) {
		remoteSong.getCover(true, new OnBitmapReadyListener() {
			@Override
			public void onBitmapReady(Bitmap bmp) {
				if (null != bmp) {
					playerCover.setImageBitmap(bmp);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.player_play:
			play(PlayerService.CURRENT_SONG);
			//TODO start play
			break;
		case R.id.player_previous:
			shift(-1);
			//TODO start previous
			break;
		case R.id.player_forward:
			shift(1);
			//TODO start forward
			break;
		case R.id.player_download:
			download();
			break;
		case R.id.player_lyrics:
			//TODO show lyrics
			break;
		case R.id.player_edit_tags:
			showEditTagDialog();
			break;
		case R.id.player_save_tags:
			//TODO save tags
		case R.id.player_cancel_tags:
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			break;
		default:
			break;
		}
		
	}

	private void showEditTagDialog() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
		} else {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.VISIBLE);
		}
	}
	
	private void shift(int delta) {
		// delta must be 1 or -1, 1 - next, -1 - previous
		switch (delta) {
		case 1:
			
			break;
		case -1:
			
			break;
		default:
			break;
		}
	}

	private void download() {
		int id = artist.hashCode() * title.hashCode() * (int) System.currentTimeMillis();
		downloadListener = new DownloadListener(getActivity(), (RemoteSong) selectedSong, id);
		if (downloadListener.isBadInet()) return;
		downloadListener.onClick(parentView);
	}

	private void play(int position) {
		playerArtist.setText(artist);
		playerTitle.setText(title);
		playerTotalTime.setText("");
		playerProgress.post(progressAction);
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		player.play(position);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			player.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	
	//TODO improve the method
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.pause);
		} else {
			play.setImageResource(R.drawable.play);
		}
	}
	
	private Runnable progressAction = new Runnable() {

		@Override
		public void run() {
			try {
				if (player.isPrepared()) {
					changePlayPauseView(true);
					int current = player.getCurrentPosition();
					playerProgress.setProgress(current);
					playerProgress.setMax(player.getDuratioun());
					playerCurrTime.setText(Util.getFormatedStrDuration(current));
				}
				if (player.isComplete()) {
					playerProgress.setProgress(0);
					playerCurrTime.setText("");
					changePlayPauseView(false);
				}
				playerProgress.postDelayed(this, 1000);
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e.getMessage());
			}
		}

	};
	
}
