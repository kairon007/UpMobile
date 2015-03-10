package org.upmobile.newmusicdownloader.fragment;

import java.io.File;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.DownloadListener;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerFragment  extends Fragment implements OnClickListener, OnSeekBarChangeListener, OnStatePlayerListener {

	private static final String ANDROID_MEDIA_EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
	private static final String ANDROID_MEDIA_VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
	private AbstractSong song;
	private AudioManager audio;
	private RenameTask renameTask;
	private PlaybackService player;
	private LyricsFetcher lyricsFetcher;
	private DownloadListener downloadListener;
	private IntentFilter filter;
	private View parentView;
	private SeekBar playerProgress;
	private SeekBar volume;
	private CheckBox playerTagsCheckBox;
	private ImageButton play;
	private View wait;
	private ImageButton previous;
	private ImageButton forward;
	private ImageButton editTag;
	private ImageButton showLyrics;
	private ImageButton shuffle;
	private ImageButton repeat;
	private ImageButton stop;
	private ImageView playerCover;
	private Button btnDownload;
	private Button playerSaveTags;
	private Button playerCancelTags;
	private TextView playerTitle;
	private TextView playerArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TextView playerLyricsView;
	private EditText playerTagsAlbum;
	private EditText playerTagsTitle;
	private EditText playerTagsArtist;
	private int checkIdCover;
    private boolean isDestroy;
    private boolean isUseAlbumCover = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		setHasOptionsMenu(true);
		parentView = inflater.inflate(R.layout.player_fragment, container, false);
		init();
		playerProgress.setVisibility(View.INVISIBLE);
		filter = new IntentFilter();
		filter.addAction(ANDROID_MEDIA_VOLUME_CHANGED_ACTION);
		audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		volume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
		player = PlaybackService.get(getActivity());
		player.addStatePlayerListener(this);
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			song = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			if (song.equals(player.getPlayingSong()) && player.isPlaying()) {
				player.play();
			} else if (!song.equals(player.getPlayingSong())){
				player.play(song);
			}
			((MainActivity) getActivity()).setDrawerEnabled(false);
		} else {
			song = player.getPlayingSong();
		}
		setImageButton();
		getCover(song);
		setElementsView(player.getCurrentPosition());
		boolean prepared = player.isPrepared();
		setClickablePlayerElement(prepared);
		downloadButtonState(!player.isGettingURl());
		if (prepared) {
			changePlayPauseView(!player.isPlaying());
		} else {
			changePlayPauseView(prepared);
		}
		return parentView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		getActivity().onBackPressed();
		((MainActivity)getActivity()).setDrawerEnabled(true);
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onDestroyView() {
		player.removeStatePlayerListener(this);
		((MainActivity)getActivity()).setDrawerEnabled(true);
		super.onDestroyView();
	}
	
	@Override
	public void start(AbstractSong s) {
		song = s;
		downloadButtonState(true);
		if (isDestroy) return;
		((MainActivity) getActivity()).showPlayerElement(true);
		setImageButton();
		setClickablePlayerElement(true);
		changePlayPauseView(!player.isPlaying());
		setElementsView(0);
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
		wait.setVisibility(View.INVISIBLE);
		playerProgress.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void stop(AbstractSong s) {
		if (isDestroy) return;
		setElementsView(0);
		changePlayPauseView(true);
		setClickablePlayerElement(player.isPrepared());
	}
	
	@Override
	public void error() {
		if (isDestroy) return;
		Toast.makeText(getActivity(), ru.johnlife.lifetoolsmp3.R.string.file_is_bad, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void update(AbstractSong current) {
		if (isDestroy) return;
		song = current;
		setElementsView(0);
		setClickablePlayerElement(false);
		playerProgress.setVisibility(View.INVISIBLE);
		wait.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onPause() {
		if (null != lyricsFetcher) lyricsFetcher.cancel();
		if (null != volumeReceiver) getActivity().unregisterReceiver(volumeReceiver);
		super.onPause();
	}

	private void setImageButton() {
		if(!player.enabledRepeat()) {
			repeat.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaRepeat));
		} else {
			repeat.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaRepeatOn));
		}
		if (player.enabledShuffle()) {
			shuffle.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaShuffle));
		} else {
			shuffle.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaShuffle));
		}
	}
	
	private int getResIdFromAttribute(final Activity activity, final int attr) {
		if (attr == 0) return 0;
		final TypedValue typedvalueattr = new TypedValue();
		activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
		return typedvalueattr.resourceId;
	}

	private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null != intent.getAction() && ANDROID_MEDIA_VOLUME_CHANGED_ACTION.equals(intent.getAction())) {
				if ((Integer) intent.getExtras().get(ANDROID_MEDIA_EXTRA_VOLUME_STREAM_VALUE) == volume.getProgress()) return;
				volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(volumeReceiver, filter); 
		((MainActivity) getActivity()).setSelectedItem(Constants.PLAYER_FRAGMENT);
		((MainActivity) getActivity()).invalidateOptionsMenu();
//		getView().setFocusableInTouchMode(true);
//		getView().requestFocus();
//		getView().setOnKeyListener(new View.OnKeyListener() {
//			
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				switch (keyCode) {
//				case KeyEvent.KEYCODE_VOLUME_DOWN:
//					volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
//					break;
//				case KeyEvent.KEYCODE_VOLUME_UP:
//					volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
//					break;
//				default:
//					break;
//				}
//				return false;
//			}
//		});
	}
	
	@Override
	public void onDestroy() {
		isDestroy = true;
		((MainActivity)getActivity()).setDrawerEnabled(true);
		super.onDestroy();
	}

	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		stop.setClickable(isClickable);
		playerProgress.setEnabled(isClickable);
		playerProgress.setClickable(isClickable);
		if (!isClickable) {
			playerCurrTime.setText("0:00");
			playerProgress.setProgress(0);
			playerProgress.setVisibility(View.INVISIBLE);
			wait.setVisibility(View.VISIBLE);
		} else {
			playerProgress.setVisibility(View.VISIBLE);
			wait.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * @param isPlaying - If "true", the button changes the picture to "play", if "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaPlay));
		} else {
			play.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaPause));
		}
	}
	
	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.playpause);
		wait = parentView.findViewById(R.id.player_wait_song);
		previous = (ImageButton) parentView.findViewById(R.id.prev);
		forward = (ImageButton) parentView.findViewById(R.id.next);
		shuffle = (ImageButton) parentView.findViewById(R.id.shuffle);
		repeat = (ImageButton) parentView.findViewById(R.id.repeat);
		stop = (ImageButton) parentView.findViewById(R.id.stop);
		btnDownload = (Button) parentView.findViewById(R.id.btn_download);
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
		playerTagsArtist = (EditText) parentView.findViewById(R.id.editTextArtist);
		playerTagsTitle = (EditText) parentView.findViewById(R.id.editTextTitle);
		playerTagsAlbum = (EditText) parentView.findViewById(R.id.editTextAlbum);
		playerTagsCheckBox = (CheckBox) parentView.findViewById(R.id.isUseCover);
		playerProgress.setOnSeekBarChangeListener(this);
		volume.setOnSeekBarChangeListener(this);
		play.setOnClickListener(this);
		btnDownload.setOnClickListener(this);
		stop.setOnClickListener(this);
		shuffle.setOnClickListener(this);
		repeat.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		editTag.setOnClickListener(this);
		showLyrics.setOnClickListener(this);
		playerCancelTags.setOnClickListener(this);
		playerSaveTags.setOnClickListener(this);
	}
	
	private void setElementsView(int progress) {
		if (song.getClass() == MusicData.class) {
			btnDownload.setVisibility(View.GONE);
		} else {
			btnDownload.setVisibility(View.VISIBLE);
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
			if (song.getClass() != MusicData.class) ((RemoteSong) song).setHasCover(false); 
			playerCover.setImageResource(R.drawable.no_cover_art_big);
			checkBoxState(false);
		}
		if (song.getClass() != MusicData.class) {
			player.update(song.getTitle(), song.getArtist(), null);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_download:
			download();
			break;
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
			if (!player.offOnRepeat()){
				repeat.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaRepeat));
			} else {
				repeat.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaRepeatOn));
			}
			break;
		case R.id.shuffle:
			if (player.offOnShuffle()) {
				shuffle.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaShuffleOn));
			} else {
				shuffle.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaShuffle));
			}
			break;
		case R.id.stop:
			player.stop();
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
			hideKeyboard();
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
				playerTagsCheckBox.setChecked(true);
			}
			break;
		default:
			break;
		}
	}

	private void hideOpenViews() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
				playerTagsCheckBox.setChecked(true);
			}
		}
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.VISIBLE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
		}
		isUseAlbumCover = true;
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
		if (null != lyricsFetcher) {
			lyricsFetcher.cancel();
			playerLyricsView.setText("");
		}
		if (parentView.findViewById(R.id.player_lyrics_frame).getVisibility() == View.GONE) {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.VISIBLE);
			lyricsFetcher = new LyricsFetcher(getActivity());
			lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
			lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {

				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
					try {
						if (foundLyrics) {
							playerLyricsView.setText(Html.fromHtml(lyrics));
						} else {
							String songName = song.getArtist() + " - " + song.getTitle();
							playerLyricsView.setText(getResources().getString(R.string.download_dialog_no_lyrics, songName));
						}
					} catch (Exception e) {
					}
				}
			});
		} else {
			parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.GONE);
		}
	}

	private void showEditTagDialog() {
		if (parentView.findViewById(R.id.player_edit_tag_dialog).getVisibility() == View.VISIBLE) {
			hideKeyboard();
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.GONE);
			if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
				playerTagsCheckBox.setChecked(true);
			}
		} else {
			parentView.findViewById(R.id.player_edit_tag_dialog).setVisibility(View.VISIBLE);
			playerTagsArtist.setText(song.getArtist());
			playerTagsTitle.setText(song.getTitle());
			playerTagsAlbum.setText(song.getAlbum());
		}
	}

	private void hideKeyboard() {
		View hideVeiw = parentView.findViewById(R.id.player_edit_tag_dialog);
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(hideVeiw.getWindowToken(), 0);
	}
	
	private void saveTags() {
		hideKeyboard();
		boolean manipulate = manipulateText();
		isUseAlbumCover = playerTagsCheckBox.isChecked();
		if (!manipulate && playerTagsCheckBox.isChecked()) {
			return;
		}
		updateObject();
		if (song.getClass() != MusicData.class) return;
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
				if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
					playerTagsCheckBox.setChecked(true);
				}
				player.update(song.getTitle(), song.getArtist(), song.getPath());
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
			song.setTitle(playerTagsTitle.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : Util.removeSpecialCharacters(playerTagsTitle.getText().toString()));
			result = true;
		}
		if (!song.getArtist().equals(playerTagsArtist.getText().toString())) {
			song.setArtist(playerTagsArtist.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : Util.removeSpecialCharacters(playerTagsArtist.getText().toString()));
			result = true;
		}
		if (song.getAlbum() != null && !song.getAlbum().equals(playerTagsAlbum.getText().toString())) {
			song.setAlbum(playerTagsAlbum.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : Util.removeSpecialCharacters(playerTagsAlbum.getText().toString()));
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
		if (delta == 0) {
			player.play(song);
			return;
		}
		player.stop();
		playerCover.invalidate();
		if (delta > 0) {
			player.shift(1);
			getCover(player.getPlayingSong());
			downloadButtonState(!player.isGettingURl());
		} else if (delta < 0) {
			player.shift(-1);
			getCover(player.getPlayingSong());
			downloadButtonState(!player.isGettingURl());
		}
	}

	private void getCover(final AbstractSong song) {
		if (song.getClass() != MusicData.class) {
			playerCover.setImageResource(R.drawable.no_cover_art_big);
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover)return;
					if (null != bmp) {
						checkBoxState(true);
						((RemoteSong) song).setHasCover(true);
						playerCover.setImageResource(0);
						playerCover.setImageBitmap(bmp);
					} else {
						playerCover.setImageResource(R.drawable.no_cover_art_big);
						checkBoxState(false);
					}
				}
			};
			checkIdCover  = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);
		} else {
			final Bitmap bitmap = ((MusicData) song).getCover(getActivity());
			if (bitmap != null) {
				playerCover.post(new Runnable() {
					
					@Override
					public void run() {
						playerCover.setImageResource(0);
						checkBoxState(true);
						playerCover.setImageBitmap(bitmap);
					}
				});
			} else {
				playerCover.setImageResource(R.drawable.no_cover_art_big);
				checkBoxState(false);
			}
		}
	}
	
	private void checkBoxState(boolean state) {
		playerTagsCheckBox.setEnabled(state);
		playerTagsCheckBox.setChecked(state);
		playerTagsCheckBox.setClickable(state);
	}
	
	private void downloadButtonState(boolean state) {
		btnDownload.setClickable(state);
		btnDownload.setEnabled(state);
	}
	
	private void download() {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		downloadListener = new DownloadListener(getActivity(), (RemoteSong) song, id);
		if (downloadListener.isBadInet()) {
			Toast.makeText(getActivity(), ru.johnlife.lifetoolsmp3.R.string.search_message_no_internet, Toast.LENGTH_SHORT).show();
			return;
		}
		downloadListener.setUseAlbumCover(isUseAlbumCover);
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

	@Override
	public void stopPressed() {
		// TODO Auto-generated method stub
		
	}
}
