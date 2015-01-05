package org.upmobile.newmusicdownloader.fragment;

import java.io.File;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.DownloadListener;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.data.MusicData;
import org.upmobile.newmusicdownloader.service.PlayerService;
import org.upmobile.newmusicdownloader.service.PlayerService.OnStatePlayerListener;

import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerFragment  extends Fragment implements OnClickListener, OnSeekBarChangeListener {

	private AbstractSong song;
	private AudioManager audio;
	private RenameTask renameTask;
	private PlayerService player;
	private DownloadListener downloadListener;
	private View parentView;
	private SeekBar playerProgress;
	private SeekBar volume;
	private CheckBox playerTagsCheckBox;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private ImageButton editTag;
	private ImageButton showLyrics;
	private ImageButton shuffle;
	private ImageButton repeat;
	private ImageButton stop;
	private ImageView playerCover;
	private Button download;
	private Button playerSaveTags;
	private Button playerCancelTags;
	private Button playerCancelLyrics;
	private TextView playerTitle;
	private TextView playerArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TextView playerLyricsView;
	private EditText playerTagsAlbum;
	private EditText playerTagsTitle;
	private EditText playerTagsArtist;
	private int currentPosition;
    private boolean isDestroy;
    private boolean hadInstance;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		parentView = inflater.inflate(R.layout.main_fragment_port, container, false);
		init();
		audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		volume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			hadInstance = false;
			AbstractSong buf = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			if (buf.getClass() != MusicData.class) {
				song = ((RemoteSong) buf).cloneSong();
			} else {
				song = buf;
			}
			currentPosition = getArguments().getInt(Constants.KEY_SELECTED_POSITION);
			if (song.getClass() != MusicData.class) {
				((RemoteSong) song).getCover(new OnBitmapReadyListener() {
					
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (null != bmp) {
							playerCover.setImageBitmap(bmp);
						}
					}
				});
			} else {
				setCoverFromMusicData();
			}
		} else {
			hadInstance = true;
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				player = PlayerService.get(getActivity());
				bindToPlayer();
				final int current;
				final int mode;
				final boolean enabledPlayerElement;
				if (hadInstance) {
					AbstractSong buf = player.getPlayingSong();
					currentPosition = player.getPlayingPosition();
					if (buf.getClass() != MusicData.class) {
						song = ((RemoteSong) buf).cloneSong();
					} else {
						song = buf;
					}
					if (player.isGettingURl() || !player.isPrepared()) {
						enabledPlayerElement = false;
						current = 0;
						mode = 0;
					} else {
						boolean check = player.isPlaying();
						current = player.getCurrentPosition();
						if (check) {
							mode = -1;
						} else {
							mode = 1;
						}
						enabledPlayerElement = true;
					}
				} else {
					if (player.hasValidSong(song.getClass()) && player.getPlayingPosition() == currentPosition) {
						boolean check = player.isPlaying();
						current = player.getCurrentPosition();
						enabledPlayerElement = true;
						if (check) {
							mode = -1;
						} else {
							mode = 1;
						}
					} else {
						mode = 0;
						current = 0;
						enabledPlayerElement = false;
						if (player.isPlaying()) {
							player.reset();
						} 
						player.play(currentPosition);
					}
				}
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					
					@Override
					public void run() {
						setImageButton();
						if (!hadInstance) {
							if (mode == 0) {
								((MainActivity) getActivity()).showPlayerElement(true);
							}
						}
						setElementsView(current);
						if (!enabledPlayerElement) {
							setClickablePlayerElement(false);
						}
						if (mode > 0) {
							changePlayPauseView(true);
						} else if (mode < 0){
							changePlayPauseView(false);
						}
					}
				});
			}
		}).start();
		return parentView;
	}

	private void setImageButton() {
		Bitmap bmRepeat;
		Bitmap bmShuffle;
		if(!player.enabledRepeat()) {
			bmRepeat = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_repeat);
		} else {
			bmRepeat = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_repeat_on);
		}
		if (player.enabledShuffle()) {
			bmShuffle = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_shuffle_on);
		} else {
			bmShuffle = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_shuffle);
		}
		repeat.setImageBitmap(bmRepeat);
		shuffle.setImageBitmap(bmShuffle);
	}

	private void bindToPlayer() {
		player.setStatePlayerListener(new OnStatePlayerListener() {
			
			@Override
			public void start(AbstractSong s) {
				if (song.getClass() != MusicData.class) {
					song = ((RemoteSong) s).cloneSong();
				} else {
					song = s;
				}
				if (isDestroy) return;
				setClickablePlayerElement(true);
				setElementsView(0);
				playerProgress.post(progressAction);
			}
			
			@Override
			public void pause(AbstractSong song) {
				if (isDestroy) return;
				changePlayPauseView(true);
			}

			@Override
			public void play(AbstractSong song) {
				if (isDestroy) return;
				changePlayPauseView(false);
			}


			@Override
			public void update(AbstractSong current) {
				if (isDestroy) return;
				song = current;
				setElementsView(0);
				setClickablePlayerElement(false);
			}
			
			@Override
			public void stop(AbstractSong song) {
				if(song.equals(PlayerFragment.this.song)) return;
				setElementsView(0);
				changePlayPauseView(true);
				setClickablePlayerElement(false);
			}
			
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					return true;
				}
				return false;
			}
		});
	}
	
	@Override
	public void onDestroy() {
	    	isDestroy = true;
	    	super.onDestroy();
	    }
	
	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		stop.setClickable(isClickable);
		playerProgress.setEnabled(isClickable);
		playerProgress.setClickable(isClickable);
		if (isClickable) {
			play.setImageResource(R.drawable.ic_media_pause);
		} else {
			playerCurrTime.setText("0:00");
			playerProgress.setProgress(0);
		}
	}
	
	/**
	 * @param isPlaying - If "true", the button changes the picture to "play", if "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.ic_media_play);
		} else {
			play.setImageResource(R.drawable.ic_media_pause);
		}
	}
	
	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.playpause);
		previous = (ImageButton) parentView.findViewById(R.id.prev);
		forward = (ImageButton) parentView.findViewById(R.id.next);
		shuffle = (ImageButton) parentView.findViewById(R.id.shuffle);
		repeat = (ImageButton) parentView.findViewById(R.id.repeat);
		stop = (ImageButton) parentView.findViewById(R.id.stop);
		download = (Button) parentView.findViewById(R.id.download);
		showLyrics = (ImageButton) parentView.findViewById(R.id.player_lyrics);
		editTag = (ImageButton) parentView.findViewById(R.id.player_edit_tags);
		volume = (SeekBar) parentView.findViewById(R.id.progress_volume);
		playerProgress = (SeekBar) parentView.findViewById(R.id.progress_track);
		playerTitle = (TextView) parentView.findViewById(R.id.songName);
		playerArtist = (TextView) parentView.findViewById(R.id.artistName);
		playerCurrTime = (TextView) parentView.findViewById(R.id.trackTime);
		playerTotalTime = (TextView) parentView.findViewById(R.id.trackTotalTime);
		playerLyricsView = (TextView) parentView.findViewById(R.id.player_lyrics_view);
		playerSaveTags = (Button) parentView.findViewById(R.id.player_save_tags);
		playerCancelTags = (Button) parentView.findViewById(R.id.player_cancel_tags);
		playerCover = (ImageView) parentView.findViewById(R.id.albumCover);
		playerCancelLyrics = (Button) parentView.findViewById(R.id.player_cancel_lyrics);
		playerTagsArtist = (EditText) parentView.findViewById(R.id.editTextArtist);
		playerTagsTitle = (EditText) parentView.findViewById(R.id.editTextTitle);
		playerTagsAlbum = (EditText) parentView.findViewById(R.id.editTextAlbum);
		playerTagsCheckBox = (CheckBox) parentView.findViewById(R.id.isUseCover);
		playerProgress.setOnSeekBarChangeListener(this);
		volume.setOnSeekBarChangeListener(this);
		playerCancelLyrics.setOnClickListener(this);
		play.setOnClickListener(this);
		stop.setOnClickListener(this);
		shuffle.setOnClickListener(this);
		repeat.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		editTag.setOnClickListener(this);
		showLyrics.setOnClickListener(this);
		download.setOnClickListener(this);
		playerCancelTags.setOnClickListener(this);
		playerSaveTags.setOnClickListener(this);
	}
	
	private void setElementsView(int progress) {
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		}
		playerProgress.removeCallbacks(progressAction);
		playerArtist.setText(song.getArtist());
		playerTitle.setText(song.getTitle());
		long totalTime = song.getDuration();
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		playerProgress.setMax((int) totalTime);
		playerProgress.setProgress(progress);
		playerCurrTime.setText(Util.getFormatedStrDuration(progress));
		playerProgress.post(progressAction);
	}
		
	private void updateObject() {
		playerArtist.setText(song.getArtist());
		playerTitle.setText(song.getTitle());
		if (!playerTagsCheckBox.isChecked()) {
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.no_cover_art_big);
			playerCover.setImageBitmap(bmp);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.playpause:
			play(0);
			break;
		case R.id.prev:
			play(-1);	
			hideOpenViews();
			break;
		case R.id.next:
			play(1);
			hideOpenViews();
			break;
		case R.id.repeat:
			Bitmap bmRepeat;
			if (!player.offOnRepeat()){
				bmRepeat = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_repeat);
			} else {
				bmRepeat = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_repeat_on);
			}
			repeat.setImageBitmap(bmRepeat);
			break;
		case R.id.shuffle:
			Bitmap bmShuffle;
			if (player.offOnShuffle()) {
				bmShuffle = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_shuffle_on);
			} else {
				bmShuffle = BitmapFactory.decodeResource(getResources(), R.drawable.ic_media_shuffle);
			}
			shuffle.setImageBitmap(bmShuffle);
			break;
		case R.id.stop:
			player.stop();
			break;
		case R.id.download:
			download();
			break;
		case R.id.player_lyrics:
			showLyrics();
			break;
		case R.id.player_edit_tags:
			showEditTagDialog();
			break;
		case R.id.player_save_tags:
			saveTags();
		case R.id.player_cancel_tags:
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			break;
		case R.id.player_cancel_lyrics:
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	private void hideOpenViews() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
		}
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
		}
	}

	private Runnable progressAction = new Runnable() {

		@Override
		public void run() {
			try {
				playerProgress.removeCallbacks(this);
				if (player.isPrepared()) {
					int current = player.getCurrentPosition();
					playerProgress.setProgress(current);
					playerCurrTime.setText(Util.getFormatedStrDuration(current));
				} 
				playerProgress.postDelayed(this, 1000);
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e + "");
			}
		}

	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.progress_volume:
			if (fromUser) {
				audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			}
			break;
		case R.id.progress_track:
			if (fromUser) {
				try {
					player.seekTo(progress);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
	private void showLyrics() {
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.GONE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.VISIBLE);
			final int [] location = new int[2];
			playerLyricsView.getLocationOnScreen(location);
			LyricsFetcher lyricsFetcher = new LyricsFetcher(getActivity());
			lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
			lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {
				
				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
					if (foundLyrics) {
						playerLyricsView.setText(Html.fromHtml(lyrics));
					} else {
						String songName = song.getArtist() + " - " + song.getTitle();
						playerLyricsView.setText(getResources().getString(R.string.download_dialog_no_lyrics, songName));
					}
				}
			});
		} else {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
		}
	}

	private void showEditTagDialog() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
		} else {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.VISIBLE);
			playerTagsArtist.setText(song.getArtist());
			playerTagsTitle.setText(song.getTitle());
			playerTagsAlbum.setText(song.getAlbum());
			if (song.hasCover()) {
				playerTagsCheckBox.setChecked(true);
			} else {
				playerTagsCheckBox.setChecked(false);
				playerTagsCheckBox.setClickable(false);
			}
			final int [] location = new int[2];
			playerTagsArtist.getLocationOnScreen(location);
		}
	}

	private void saveTags() {
		boolean manipulate = manipulateText();
		if (!manipulate && playerTagsCheckBox.isChecked()) {
			return;
		} else {
			updateObject();
		}
		if (song.getClass() != MusicData.class)
			return;
		File f = new File(song.getPath());
		if (new File(f.getParentFile() + "/" + song.getArtist() + " - " + song.getTitle() + ".mp3").exists() && playerTagsCheckBox.isChecked()) {
			Toast toast = Toast.makeText(getActivity(), R.string.file_already_exists, Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		RenameTaskSuccessListener renameListener = new RenameTaskSuccessListener() {

			@Override
			public void success(String path) {
				song.setPath(path);
				renameTask.cancelProgress();
				parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			}

			@Override
			public void error() {

			}
		};
		renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, song.getArtist(), song.getTitle(), song.getAlbum());
		if (manipulate && playerTagsCheckBox.isChecked()) { 			//if we change only text
			if (song.getClass() == MusicData.class) {
				renameTask.start(true, false);
			}
		} else if (!manipulate && !playerTagsCheckBox.isChecked()) { 	// if we change only cover
			if (song.getClass() == MusicData.class) {
				renameTask.start(false, true);
			}
		} else if (manipulate && !playerTagsCheckBox.isChecked()) { 	// if we change cover and fields
			if (song.getClass() == MusicData.class) {
				renameTask.start(false, false);
			}
		}
	}

	public boolean manipulateText() {
		boolean result = false;
		if (!song.getTitle().equals(playerTagsTitle.getText().toString())) {
			song.setTitle(playerTagsTitle.getText().toString());
			result = true;
		}
		if (!song.getArtist().equals(playerTagsArtist.getText().toString())) {
			song.setArtist(playerTagsArtist.getText().toString());
			result = true;
		}
		if (song.getAlbum() != null && !song.getAlbum().equals(playerTagsAlbum.getText().toString())) {
			song.setAlbum(playerTagsAlbum.getText().toString());
			result = true;
		} 
		return result;
	}
	
	/**
	 * @param delta
	 *  - 
	 * delta must be 1 or -1 or 0, 1 - next, -1 - previous, 0 - current song
	 */
	private void play(int delta) throws IllegalArgumentException {
		if (delta > 0) player.shift(1);
		else if (delta < 0) player.shift(-1);
		else {
			player.play(player.getPlayingPosition());
		}
		if (delta != 0 && song.getClass() != MusicData.class) {
			((RemoteSong) song).getCover(new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (null != bmp) {
						playerCover.setImageBitmap(bmp);
					}
				}
				
			});
		} else if (song.getClass() == MusicData.class) {
			setCoverFromMusicData();
		}
	}

	private void setCoverFromMusicData() {
		Bitmap bitmap = ((MusicData)song).getCover(getActivity());
		if (bitmap != null) {
			playerCover.setImageBitmap(bitmap);
		}
	}
	
	private void download() {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		downloadListener = new DownloadListener(getActivity(), (RemoteSong) song, id);
		if (downloadListener.isBadInet()) return;
		((RemoteSong) song).getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (!url.startsWith("http")) {
					Toast toast = Toast.makeText(player, R.string.error_retrieving_the_url, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				((RemoteSong) song).setDownloadUrl(url);
				Runnable callbackRun = new Runnable() {

					@Override
					public void run() {
						downloadListener.onClick(parentView);
					}
				};
				new Handler(Looper.getMainLooper()).post(callbackRun);
			}

			@Override
			public void error(String error) {
			}
		});
	}
}