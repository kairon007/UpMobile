package org.upmobile.newmusicdownloader.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.upmobile.newmusicdownloader.BaseDownloadListener;
import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import java.io.File;

import ru.johnlife.lifetoolsmp3.engines.lyric.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.SearchLyrics;
import ru.johnlife.lifetoolsmp3.listeners.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.services.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.tasks.ProgressUpdaterTask;
import ru.johnlife.lifetoolsmp3.tasks.RenameTask;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import ru.johnlife.lifetoolsmp3.utils.DownloadCache;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class PlayerFragment  extends Fragment implements OnClickListener, OnSeekBarChangeListener, OnStatePlayerListener, PlaybackService.OnErrorListener{

	private static final String ANDROID_MEDIA_EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
	private static final String ANDROID_MEDIA_VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
	private AbstractSong song;
	private AudioManager audio;
	private ProgressUpdaterTask progressUpdater;
	private ProgressUpdaterTask.ProgressUpdaterListener progressListener;
	private RenameTask renameTask;
	private PlaybackService player;
	private SearchLyrics lyricsFetcher;
	private IntentFilter filter;
	private View parentView;
	private SeekBar playerProgress;
	private SeekBar volume;
	private CheckBox playerTagsCheckBox;
	private ImageButton play;
	private View wait;
	private ImageButton shuffle;
	private ImageButton repeat;
	private ImageButton stop;
	private TextView shuffleMode;
	private ImageView playerCover;
	private Button btnDownload;
	private Button showInLibrary;
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
    private ProgressBar progressLyrics;
	private ProgressBar downloadProgress;
    private MenuItem showLyrics;
    private MenuItem editTag;
    private View playerEditTagDialog;
    private View playerLyricsFrame;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		parentView = inflater.inflate(R.layout.player_fragment, container, false);
		player = PlaybackService.get(getActivity());
		isDestroy = false;
		setHasOptionsMenu(true);
		init(parentView);
		playerProgress.setVisibility(View.INVISIBLE);
		filter = new IntentFilter();
		filter.addAction(ANDROID_MEDIA_VOLUME_CHANGED_ACTION);
		audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		volume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
		song = player.getPlayingSong();
		setImageButton();
		getCover(song);
		setElementsView(player.getCurrentPosition());
		boolean prepared = player.isPrepared();
		setClickablePlayerElement(prepared);
		downloadButtonState(!player.isGettingURl());
		changePlayPauseView(prepared ? !player.isPlaying() : prepared);
		btnDownload.setVisibility(song.getClass() == MusicData.class ? View.GONE : View.VISIBLE);
		return parentView;
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.player_menu, menu);
        showLyrics = menu.findItem(R.id.action_show_lyrics);
        editTag = menu.findItem(R.id.action_edit_tag);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_lyrics:
                showLyrics();
                return true;
            case R.id.action_edit_tag:
                showEditTagDialog();
                return true;
            default:
                getActivity().onBackPressed();
                ((MainActivity)getActivity()).setDrawerEnabled(true);
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onDestroyView() {
		player.removeStatePlayerListener(this);
		((MainActivity)getActivity()).setDrawerEnabled(true);
		super.onDestroyView();
	}

	private void initUpdater() {
		progressListener = new ProgressUpdaterTask.ProgressUpdaterListener() {

			private static final String FAILURE = "failure";
			boolean canceled = false;

			@Override
			public void onProgressUpdate(Integer... values) {
				if (canceled) return;
				int progress = values[0];
				((TextView) parentView.findViewById(R.id.downloadedText)).setText(R.string.downloading);
				parentView.findViewById(R.id.downloadedText).setVisibility(View.VISIBLE);
				downloadProgress.setIndeterminate(false);
				downloadProgress.setProgress(progress);
			}

			@Override
			public void onCancelled() {
				canceled = true;
			}

			@Override
			public void onPostExecute(String params) {
				if (FAILURE.equals(params)) {
					((MainActivity) getActivity()).showMessage(R.string.download_failed);
					showDownloadProgress(false);
					btnDownload.setVisibility(View.VISIBLE);
				} else {
					btnDownload.setVisibility(View.VISIBLE);
					btnDownload.setText(R.string.download_anyway);
					showDownloadProgress(false);
					parentView.findViewById(R.id.downloadedText).setVisibility(View.VISIBLE);
					showInLibrary.setVisibility(View.VISIBLE);
					showInLibrary.setEnabled(false);
					showInLibrary.setClickable(false);
					showInLibrary.postDelayed(new Runnable() {
						@Override
						public void run() {
							showInLibrary.setEnabled(true);
							showInLibrary.setClickable(true);
						}
					}, 1500);
				}
			}

			@Override
			public void onPreExecute() {
				btnDownload.setVisibility(View.GONE);
				canceled = false;
				showDownloadProgress(true);
				downloadProgress.setIndeterminate(true);
				downloadProgress.setMax(100);
			}

		};
	}

	private void cancelProgressTask() {
		if (null != progressUpdater) {
			progressUpdater.cancel(true);
		}
	}
	
	@Override
	public void start(AbstractSong s) {
		song = s;
		downloadButtonState(true);
		if (isDestroy) return;
		setImageButton();
		setClickablePlayerElement(true);
		changePlayPauseView(!player.isPlaying());
		setElementsView(0);
		if (song.getClass() == MusicData.class) {
			btnDownload.setVisibility(View.GONE);
		}
		showDownloadedLabel();
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
		setClickablePlayerElement(true);
		playerProgress.setEnabled(false);
		playerProgress.setClickable(false);
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
		showDownloadedLabel();
		getCover(current);
		thatSongIsDownloaded(current);
	}

	private void showDownloadedLabel() {
		boolean show = ((MainActivity) getActivity()).isThisSongDownloaded(song) && song.getClass() != MusicData.class;
		((TextView) parentView.findViewById(R.id.downloadedText)).setText(R.string.has_been_downloaded);
		parentView.findViewById(R.id.downloadedText).setVisibility(show ? View.VISIBLE : View.GONE);
		showInLibrary.setVisibility(show ? View.VISIBLE : View.GONE);
		btnDownload.setText(show ? R.string.download_anyway : R.string.download_dialog_download);
	}

	@Override
	public void onPause() {
		((MainActivity) getActivity()).showPlayerElement(player.isPrepared());
		if (null != lyricsFetcher) lyricsFetcher.cancelSearch();
		if (null != volumeReceiver) getActivity().unregisterReceiver(volumeReceiver);
		super.onPause();
	}

	private void setImageButton() {
		if(player.enabledRepeat()) {
			repeat.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaRepeatOn));
		} else {
			repeat.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaRepeat));
		}
		if (player.enabledShuffleAll()) {
			shuffleMode.setVisibility(View.VISIBLE);
			if (player.enabledShuffleAuto()) {
				shuffleMode.setText("A");
			} else {
				shuffleMode.setText("M");
			}
			shuffle.setImageResource(getResIdFromAttribute(getActivity(), R.attr.mediaShuffleOn));
		} else {
			shuffleMode.setVisibility(View.INVISIBLE);
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
		MainActivity activity = ((MainActivity) getActivity());
		activity.registerReceiver(volumeReceiver, filter); 
		activity.setSelectedItem(Constants.PLAYER_FRAGMENT);
		activity.invalidateOptionsMenu();
		activity.setDrawerEnabled(activity.isButtonBackEnabled());
		activity.setTitle(R.string.tab_now_plaing);
		if (song.getClass() == MusicData.class) {
			btnDownload.setVisibility(View.GONE);
		} else {
			btnDownload.setVisibility(View.VISIBLE);
		}
		showDownloadedLabel();
		cancelProgressTask();
		initUpdater();
		thatSongIsDownloaded(song);
	}
	
	@Override
	public void onDestroy() {
		isDestroy = true;
		((MainActivity) getActivity()).setDrawerEnabled(true);
		player.removeStatePlayerListener(this);
		cancelProgressTask();
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
		play.setImageResource(getResIdFromAttribute(getActivity(), isPlaying ? R.attr.mediaPlay : R.attr.mediaPause));
	}
	
	private void init(final View view) {
		play = (ImageButton) view.findViewById(R.id.playpause);
		wait = view.findViewById(R.id.player_wait_song);
		ImageButton previous = (ImageButton) view.findViewById(R.id.prev);
		ImageButton forward = (ImageButton) view.findViewById(R.id.next);
		shuffle = (ImageButton) view.findViewById(R.id.shuffle);
		repeat = (ImageButton) view.findViewById(R.id.repeat);
		stop = (ImageButton) view.findViewById(R.id.stop);
		showInLibrary = (Button) view.findViewById(R.id.showButton);
		btnDownload = (Button) view.findViewById(R.id.btn_download);
		volume = (SeekBar) view.findViewById(R.id.progress_volume);
		shuffleMode = (TextView) view.findViewById(R.id.shuffleMode);
		playerProgress = (SeekBar) view.findViewById(R.id.progress_track);
        playerLyricsFrame = view.findViewById(R.id.player_lyrics_frame);
        playerEditTagDialog = view.findViewById(R.id.player_edit_tag_dialog);
		playerTitle = (TextView) view.findViewById(R.id.songName);
		playerArtist = (TextView) view.findViewById(R.id.artistName);
		playerCurrTime = (TextView) view.findViewById(R.id.trackTime);
		playerTotalTime = (TextView) view.findViewById(R.id.trackTotalTime);
        progressLyrics = (ProgressBar) view.findViewById(R.id.progress_lyrics);
		playerLyricsView = (TextView) view.findViewById(R.id.player_lyrics_view);
		playerSaveTags = (Button) view.findViewById(R.id.player_save_tags);
		playerCancelTags = (Button) view.findViewById(R.id.player_cancel_tags);
		playerTagsArtist = (EditText) view.findViewById(R.id.editTextArtist);
		playerTagsTitle = (EditText) view.findViewById(R.id.editTextTitle);
		playerTagsAlbum = (EditText) view.findViewById(R.id.editTextAlbum);
		playerTagsCheckBox = (CheckBox) view.findViewById(R.id.isUseCover);
		downloadProgress = (ProgressBar) view.findViewById(R.id.downloadProgress);
		playerCover = (ImageView) view.findViewById(R.id.albumCover);
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = metrics.heightPixels;
		playerCover.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (height * 0.6)));
		playerProgress.setOnSeekBarChangeListener(this);
		volume.setOnSeekBarChangeListener(this);
		play.setOnClickListener(this);
		btnDownload.setOnClickListener(this);
		stop.setOnClickListener(this);
		(view.findViewById(R.id.shuffleParent)).setOnClickListener(this);
		repeat.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		playerCancelTags.setOnClickListener(this);
		playerSaveTags.setOnClickListener(this);
		player.setOnErrorListener(this);
		player.addStatePlayerListener(this);
		showInLibrary.setOnClickListener(this);
	}
	
	private void setElementsView(int progress) {
		playerProgress.removeCallbacks(progressAction);
		playerArtist.setText(song.getArtist());
		playerTitle.setText(song.getTitle());
		long totalTime = (int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration());
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
			if (song.getClass() != MusicData.class) {
				((RemoteSong) song).setHasCover(false);
			}
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
			repeat.setImageResource(getResIdFromAttribute(getActivity(),
					player.offOnRepeat() ? R.attr.mediaRepeatOn : R.attr.mediaRepeat));
			break;
		case R.id.shuffleParent:
			shuffle.setImageResource(getResIdFromAttribute(getActivity(),
					player.offOnShuffle() ? R.attr.mediaShuffleOn : R.attr.mediaShuffle));
			if (player.enabledShuffleAll()) {
				shuffleMode.setVisibility(View.VISIBLE);
				shuffleMode.setText(player.enabledShuffleAuto() ? "A" : "M");
			} else {
				shuffleMode.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.stop:
			player.stopPressed();
			break;
		case R.id.player_save_tags:
			saveTags();
		case R.id.player_cancel_tags:
			Util.hideKeyboard(getActivity(), v);
			playerEditTagDialog.setVisibility(View.GONE);
			if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
				playerTagsCheckBox.setChecked(true);
			}
			editTag.setIcon(R.drawable.ic_player_edit_grey);
			break;
		case R.id.showButton:
			showInLibrary();
			break;
		default:
			break;
		}
	}

	private void showInLibrary() {
		((MainActivity) getActivity()).changeFragment(new LibraryFragment(), false, song);
	}

	private void hideOpenViews() {
		if (playerEditTagDialog.getVisibility() == View.VISIBLE) {
			playerEditTagDialog.setVisibility(View.GONE);
			if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
				playerTagsCheckBox.setChecked(true);
			}
		}
		if (playerLyricsFrame.getVisibility() == View.VISIBLE) {
			playerLyricsFrame.setVisibility(View.GONE);
		}
        editTag.setIcon(R.drawable.ic_player_edit_grey);
		showLyrics.setIcon(R.drawable.ic_player_lyrics_grey);
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
		if (fromUser) {
			switch (seekBar.getId()) {
			case R.id.progress_volume:
				audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				break;
			case R.id.progress_track:
				playerCurrTime.setText(Util.getFormatedStrDuration(progress));
				break;
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar.getId() == R.id.progress_track) {
			try {
				player.seekTo(seekBar.getProgress());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void showLyrics() {
		if (null != lyricsFetcher) {
			lyricsFetcher.cancelSearch();
			playerLyricsView.setText("");
		}
		if (playerLyricsFrame.getVisibility() == View.GONE) {
            showLyrics.setIcon(Util.getResIdFromAttribute(getActivity(), R.attr.lyricsIndicator));
            playerLyricsFrame.setVisibility(View.VISIBLE);
			final int [] location = new int[2];
			playerLyricsView.getLocationOnScreen(location);
			parentView.post(new Runnable() {
                @Override
                public void run() {
                    ((ScrollView) parentView.findViewById(R.id.scrollView2)).smoothScrollTo(location[0] * 2, location[1]);
                }
            });
			lyricsFetcher = new SearchLyrics(new OnLyricsFetchedListener() {

				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
                    progressLyrics.setVisibility(View.GONE);
					try {
						if (foundLyrics) {
							playerLyricsView.setText(Html.fromHtml(lyrics));
						} else {
							String songName = song.getArtist() + " - " + song.getTitle();
							playerLyricsView.setText(getResources().getString(R.string.download_dialog_no_lyrics, songName));
						}
					} catch (Exception ignored) {
					}
				}
			}, song.getArtist(), song.getTitle());
			lyricsFetcher.startSerach();
            progressLyrics.setVisibility(View.VISIBLE);
		} else {
            showLyrics.setIcon(R.drawable.ic_player_lyrics_grey);
            playerLyricsFrame.setVisibility(View.GONE);
		}
	}

	private void showEditTagDialog() {
		if (playerEditTagDialog.getVisibility() == View.VISIBLE) {
            editTag.setIcon(R.drawable.ic_player_edit_grey);
			Util.hideKeyboard(getActivity(), parentView);
			playerEditTagDialog.setVisibility(View.GONE);
			if (playerTagsCheckBox.isClickable() && playerTagsCheckBox.isEnabled()) {
				playerTagsCheckBox.setChecked(true);
			}
		} else {
            editTag.setIcon(Util.getResIdFromAttribute(getActivity(), R.attr.editIndicator));
            playerEditTagDialog.setVisibility(View.VISIBLE);
			playerTagsArtist.setText(song.getArtist());
			playerTagsTitle.setText(song.getTitle());
			playerTagsAlbum.setText(song.getAlbum());
			final int [] location = new int[2];
			playerCancelTags.getLocationInWindow(location);
			parentView.post(new Runnable() {
				@Override
				public void run() {
					((ScrollView) parentView.findViewById(R.id.scrollView2)).smoothScrollTo(location[0], location[1]);
				}
			});
		}
	}
	
	private void saveTags() {
		Util.hideKeyboard(getActivity(), parentView);
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
				playerEditTagDialog.setVisibility(View.GONE);
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
				((MusicData) song).clearCover();
				((MainActivity) getActivity()).setCover(null);
			}
		} else if (manipulate && !playerTagsCheckBox.isChecked()) { 	// if we change cover and fields
			if (song.getClass() == MusicData.class) {
				renameTask.start(false, false);
				((MusicData) song).clearCover();
				((MainActivity) getActivity()).setCover(null);
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
		if (player.isStoped()) {
			playerProgress.setVisibility(View.GONE);
			wait.setVisibility(View.VISIBLE);
		}
		if (delta == 0) {
			player.play(song);
			return;
		}
		playerCover.invalidate();
		player.pause();
		player.shift(delta, true);
		downloadButtonState(!player.isGettingURl());
		if (player.enabledRepeat()) return;
		btnDownload.setVisibility(player.getPlayingSong().getClass() != MusicData.class ? View.VISIBLE : View.GONE);
		showDownloadProgress(false);
		cancelProgressTask();
	}

	private void showDownloadProgress(boolean show) {
		((TextView) parentView.findViewById(R.id.downloadedText)).setText(show ? R.string.downloading : R.string.has_been_downloaded);
		parentView.findViewById(R.id.downloadedText).setVisibility(show ? View.VISIBLE : View.GONE);
		downloadProgress.setVisibility(show ? View.VISIBLE :View.GONE);
	}

	private void getCover(final AbstractSong song) {
		checkBoxState(false);
		playerCover.setImageResource(R.drawable.no_cover_art_big);
		final Bitmap bitmap = song.getCover();
		if (null != bitmap) {
			playerCover.post(new Runnable() {

				@Override
				public void run() {
					checkBoxState(true);
					playerCover.setImageBitmap(bitmap);
				}
			});
			return;
		}
		if (song.getClass() != MusicData.class) {
			RemoteSong.OnBitmapReadyListener readyListener = new RemoteSong.OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (hashCode() != checkIdCover || null == bmp) return;
					if (null != bmp) {
						checkBoxState(true);
						RemoteSong ssong = ((RemoteSong) song);
						ssong.setHasCover(true);
						ssong.setCover(bmp);
						playerCover.setImageBitmap(bmp);
						player.updatePictureNotification(bmp);
					}
				}
			};
			checkIdCover  = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);
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

	private void thatSongIsDownloaded(final AbstractSong checkingSong) {
		if (checkingSong.getClass() == MusicData.class) return;
		int status = StateKeeper.getInstance().checkSongInfo(checkingSong.getComment());
		if (status == StateKeeper.NOT_DOWNLOAD) {
			showDownloadProgress(false);
			btnDownload.setVisibility(View.VISIBLE);
			return;
		}
		checkingSong.getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (isDestroy) return;
				int[] statuses = {DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_SUCCESSFUL};
				DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
				for (int statusDownload : statuses) {
					Cursor cursor = manager.query(new DownloadManager.Query().setFilterByStatus(statusDownload));
					if (cursor != null) {
						while (cursor.moveToNext()) {
							if (null != url && url.equals(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)))) {
								long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
								progressUpdater = new ProgressUpdaterTask(progressListener, getActivity());
								progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
								showDownloadProgress(true);
								downloadProgress.setIndeterminate(true);
								cursor.close();
								return;
							}
						}
						cursor.close();
					}
				}
				boolean isCached = DownloadCache.getInstanse().getCachedItem(checkingSong.getTitle(), checkingSong.getArtist()) != null;
				if (StateKeeper.getInstance().checkSongInfo(checkingSong.getComment()) == StateKeeper.DOWNLOADING && isCached) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {

						@Override
						public void run() {
						showDownloadProgress(true);
						downloadProgress.setIndeterminate(true);
						}
					});
				}
			}

			@Override
			public void error(String error) {
			}
		});
	}
	
	private void download() {
		btnDownload.setVisibility(View.GONE);
		showDownloadProgress(true);
		downloadProgress.setIndeterminate(true);
		song.getDownloadUrl(new DownloadUrlListener() {

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
						int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
						BaseDownloadListener downloadListener = new BaseDownloadListener(getActivity(), (RemoteSong) song, id);
						downloadListener.setDownloadPath(NewMusicDownloaderApp.getDirectory());
						downloadListener.setUseAlbumCover(isUseAlbumCover);
						downloadListener.downloadSong(false);
						thatSongIsDownloaded(song);
					}
				};
				new Handler(Looper.getMainLooper()).post(callbackRun);

			}

			@Override
			public void error(String error) {
				btnDownload.setVisibility(View.VISIBLE);
				showDownloadProgress(false);
			}
		});
	}

	@Override
	public void stopPressed() {}
	
	@Override
	public void error(final String error) {
		if (null == getActivity()) return;
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
				player.stopPressed();
			}
		});
	}

	@Override
	public void onTrackTimeChanged(int time, boolean isOverBuffer) {}

	@Override
	public void onBufferingUpdate(double percent) {}
}
