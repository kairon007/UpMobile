package org.upmobile.materialmusicdownloader.fragment;

import java.io.File;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.DownloadListener.OnCancelDownload;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.StateKeeper.SongInfo;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import ru.johnlife.lifetoolsmp3.ui.widget.CheckBox;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.AdvancedUndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import ru.johnlife.lifetoolsmp3.ui.widget.progressbutton.CircularProgressButton;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.view.MaterialRippleLayout;
import com.csform.android.uiapptemplate.view.PullToZoomScrollView;

public class PlayerFragment extends Fragment implements OnClickListener, BaseMaterialFragment, OnCheckedChangeListener, PlaybackService.OnErrorListener, OnEditorActionListener {

	private final int MESSAGE_DURATION = 5000;
	private final int DEFAULT_SONG = 7340032; // 7 Mb
	private final int BAD_SONG = 2048; // 200kb
	private AbstractSong song;
	private AsyncTask<Long,Integer,String> progressUpdater;
	private RenameTask renameTask;
	private PlaybackService player;
	private DownloadListener downloadListener;

	private PullToZoomScrollView scrollView;
	private View contentView;

	private CircularProgressButton download;
	private UndoBar undo;
	private LinearLayout artistBox;
	private LinearLayout titleBox;

	// lyric sections
	private LyricsFetcher lyricsFetcher;
	private TextView playerLyricsView;
	
	//custom check box
	private CheckBox cbUseCover;
	private boolean isUseAlbumCover = false;

	// playback sections
	private TextView play;
	private TextView previous;
	private TextView forward;
	private TextView shuffle;
	private TextView repeat;

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private SeekBar playerProgress;
	private ImageView imageView;

	private int checkIdCover;
	private int checkIdLyrics;
	private double percent = 0;

	private boolean isDestroy;
	
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

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
		public void stop(AbstractSong s) {
			if (isDestroy) return;
			changePlayPauseView(true);
			setElementsView(0);
		}
		
		@Override
		public void stopPressed(){}

		@Override
		public void error() {
		}

		@Override
		public void start(AbstractSong s) {
			song = s;
			if (isDestroy) return;
			((MainActivity) getActivity()).showPlayerElement(true);
			setDownloadButtonState(true);
			setClickablePlayerElement(true);
			changePlayPauseView(true);
			getCover(song);
			setElementsView(0);
			playerProgress.setIndeterminate(false);
			playerProgress.setMax((int)s.getDuration());
			StateKeeper.getInstance().setPlayingSong(song);
			song.getSpecial().setChecked(true);
		}

		@Override
		public void update(AbstractSong current) {
			if (isDestroy) return;
			download.setOnClickListener(PlayerFragment.this);
			download.setIndeterminateProgressMode(false);
			download.setProgress(0);
			song = current;
			showLyrics();
			setCoverToZoomView(null);
			setElementsView(0);
			contentView.findViewById(R.id.lyrics_progress).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.lyrics_text).setVisibility(View.GONE);
		}

		@Override
		public void onBufferingUpdate(double percent) {
			PlayerFragment.this.percent = percent;
		}

		@Override
		public void onTrackTimeChanged(final int time, final boolean isOverBuffer) {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					playerProgress.setProgress(time);
					if (time < playerProgress.getMax() * percent) {
						playerProgress.setSecondaryProgress(0);
						playerProgress.setSecondaryProgress((int)(playerProgress.getMax() * percent));
					}
					playerCurrTime.setText(Util.getFormatedStrDuration(time));
					playerProgress.setIndeterminate(isOverBuffer);
				}
			});
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		setHasOptionsMenu(true);
		scrollView = new PullToZoomScrollView(getActivity());
		contentView = inflater.inflate(R.layout.player_fragment, container, false);
		contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		init();
		setListeners();
		player = PlaybackService.get(getActivity());
		player.addStatePlayerListener(stateListener);
		player.setOnErrorListener(this);
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			song = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			if (song.equals(player.getPlayingSong()) && player.isPrepared()) {
				player.play();
			} else {
				player.play(song);
			}
			((MainActivity) getActivity()).setDrawerEnabled(false);
		} else {
			song = player.getPlayingSong();
		}
		setCoverToZoomView(null);
		getCover(song);
		setImageButton();
		showLyrics();
		setElementsView(player.getCurrentPosition());
		boolean prepared = player.isPrepared();
		setClickablePlayerElement(prepared);
		if (prepared) {
			changePlayPauseView(!player.isPlaying());
			playerProgress.setIndeterminate(false);
			playerProgress.setMax((int)song.getDuration());
		} else {
			changePlayPauseView(prepared);
		}
		scrollView.setContentContainerView(contentView);
		return scrollView;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		getActivity().onBackPressed();
		((MainActivity)getActivity()).setDrawerEnabled(true);
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		thatSongIsDownloaded();
		((UIMainActivity) getActivity()).setSelectedItem(Constants.PLAYER_FRAGMENT);
		((UIMainActivity) getActivity()).setTitle(getDrawerTitle());
		((UIMainActivity) getActivity()).invalidateOptionsMenu();
		if (StateKeeper.getInstance().checkSongInfo(song.getComment()).getStatus() == SongInfo.DOWNLOADED) {
			((MaterialRippleLayout) download.getParent()).setVisibility(View.GONE);
		} else {
			((MaterialRippleLayout) download.getParent()).setVisibility(View.VISIBLE);
		}
		setCheckBoxState(true);
		cbUseCover.setOnCheckedChangeListener(this);
		cancelProgressTask();
		thatSongIsDownloaded();
		super.onResume();
	}

	@Override
	public void onDestroyView() {
		player.removeStatePlayerListener(stateListener);
		cancelProgressTask();
		super.onDestroyView();
	}

	private void cancelProgressTask() {
		if (null != progressUpdater) {
			progressUpdater.cancel(true);
		}
	}

	@Override
	public void onPause() {
		if (null != lyricsFetcher) {
			lyricsFetcher.cancel();
		}
		cbUseCover.setOnCheckedChangeListener(null);
		if (!isUseAlbumCover && song.isHasCover()) {
			undo.clear();
			clearCover();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		isDestroy = true;
		((MainActivity)getActivity()).setDrawerEnabled(true);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		closeEditViews();
		switch (v.getId()) {
		case R.id.playpause:
			play(0);
			break;
		case R.id.prev:
			play(-1);
			break;
		case R.id.next:
			play(1);
			break;
		case R.id.repeat:
			if (!player.offOnRepeat()) {
				repeat.setAlpha((float) 0.5);
			} else {
				repeat.setAlpha(1);
			}
			break;
		case R.id.shuffle:
			if (player.offOnShuffle()) {
				shuffle.setAlpha(1);
			} else {
				shuffle.setAlpha((float) 0.5);
			}
			break;
		case R.id.download:
			download();
			break;
		case R.id.songNameBox:
			openTitleField();
			break;
		case R.id.artistNameBox:
			openArtistField();
			break;
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!buttonView.isEnabled()) {
			return;
		}
		isUseAlbumCover = isChecked;
		cbUseCover.setChecked(isChecked);
		closeEditViews();
		if (song.getClass() != MusicData.class) {
			return;
		}
		if (isChecked) {
			undo.clear();
		} else {
			AdvancedUndoListener listener = new AdvancedUndoListener() {

				@Override
				public void onUndo(@Nullable Parcelable token) {
					setCoverToZoomView(song.getCover(getActivity()));
					cbUseCover.setChecked(true);
					isUseAlbumCover = true;
				}

				@Override
				public void onHide(@Nullable Parcelable token) {
					clearCover();
				}

				@Override
				public void onClear(@NonNull Parcelable[] token) {
				}

			};
			undo.message(R.string.message_undo_bar);
			undo.duration(MESSAGE_DURATION);
			undo.listener(listener);
			undo.show();
		}
	}

	private void clearCover() {
		if (MusicData.class == song.getClass()) {
			((View)cbUseCover.getParent()).setVisibility(View.GONE);
			setCoverToZoomView(null);
			((MusicData) song).clearCover();
			RenameTask.deleteCoverFromFile(new File(song.getPath()));
		}
	}

	private void init() {
		play = (TextView) contentView.findViewById(R.id.playpause);
		previous = (TextView) contentView.findViewById(R.id.prev);
		forward = (TextView) contentView.findViewById(R.id.next);
		shuffle = (TextView) contentView.findViewById(R.id.shuffle);
		repeat = (TextView) contentView.findViewById(R.id.repeat);
		download = (CircularProgressButton) contentView.findViewById(R.id.download);
		playerProgress = (SeekBar) contentView.findViewById(R.id.progress_track);
		int colorAccent = getActivity().getResources().getColor(R.color.material_accent);
		Drawable drawable = playerProgress.getIndeterminateDrawable();
		drawable.setColorFilter(new PorterDuffColorFilter(colorAccent, PorterDuff.Mode.SRC_ATOP));
		tvTitle = (TextView) contentView.findViewById(R.id.songName);
		etTitle = (EditText) contentView.findViewById(R.id.songNameEdit);
		tvArtist = (TextView) contentView.findViewById(R.id.artistName);
		etArtist = (EditText) contentView.findViewById(R.id.artistNameEdit);
		playerCurrTime = (TextView) contentView.findViewById(R.id.trackTime);
		playerTotalTime = (TextView) contentView.findViewById(R.id.trackTotalTime);
		playerLyricsView = (TextView) contentView.findViewById(R.id.lyrics_text);
		cbUseCover = (CheckBox) contentView.findViewById(R.id.cbUseCover);
		artistBox = (LinearLayout) contentView.findViewById(R.id.artistNameBox);
		titleBox = (LinearLayout) contentView.findViewById(R.id.songNameBox);
		undo = new UndoBar(getActivity());
	}

	private void setListeners() {
		play.setOnClickListener(this);
		shuffle.setOnClickListener(this);
		repeat.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		tvTitle.setOnClickListener(this);
		tvArtist.setOnClickListener(this);
		download.setOnClickListener(this);
		artistBox.setOnClickListener(this);
		titleBox.setOnClickListener(this);
		playerProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					try {
						playerProgress.setSecondaryProgress(0);
						playerProgress.setSecondaryProgress(Math.max(progress, (int)(playerProgress.getMax() * percent)));
						player.seekTo(progress);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
		scrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				closeEditViews();
				return v.performClick();
			}

		});
		etArtist.setOnEditorActionListener(this);
		etTitle.setOnEditorActionListener(this);
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if(actionId == EditorInfo.IME_ACTION_DONE) {
			closeEditViews();
        }
		return false;
	}

	/**
	 * @param delta
	 *            - delta must be 1 or -1 or 0, 1 - next, -1 - previous, 0 -
	 *            current song
	 */
	private void play(int delta) throws IllegalArgumentException {
		if (delta == 0) {
			player.play(song);
			return;
		}
		if (!isUseAlbumCover) {
			if (null != undo) {
				undo.clear();
			}
			clearCover();
		}
		cbUseCover.setOnCheckedChangeListener(null);
		player.stop();
		setClickablePlayerElement(false);
		player.shift(delta);
		setDownloadButtonState(!player.isGettingURl());
		playerProgress.setProgress(1);
		playerProgress.setIndeterminate(true);
		StateKeeper.getInstance().setPlayingSong(player.getPlayingSong());
		player.getPlayingSong().getSpecial().setChecked(true);
		if (!player.enabledRepeat()) {
			setCheckBoxState(false);
			cancelProgressTask();
			download.setProgress(0);
			download.setOnClickListener(this);
		}
		if (StateKeeper.getInstance().checkSongInfo(player.getPlayingSong().getComment()).getStatus() == SongInfo.DOWNLOADED) {
			((MaterialRippleLayout) download.getParent()).setVisibility(View.GONE);
		} else {
			((MaterialRippleLayout) download.getParent()).setVisibility(View.VISIBLE);
		}
		cancelProgressTask();
		thatSongIsDownloaded();
		cbUseCover.setOnCheckedChangeListener(this);
	}

	private void setElementsView(int progress) {
		download.setVisibility(song.getClass() == MusicData.class ? View.GONE : View.VISIBLE);
		tvArtist.setText(song.getArtist());
		tvArtist.requestLayout();
		tvTitle.setText(song.getTitle());
		tvTitle.requestLayout();
		playerTotalTime.setText(Util.getFormatedStrDuration(song.getDuration()));
		playerCurrTime.setText(Util.getFormatedStrDuration(progress));
	}

	private void setImageButton() {
		if (!player.enabledRepeat()) {
			repeat.setAlpha((float) 0.5);
		} else {
			repeat.setAlpha(1);
		}
		if (player.enabledShuffle()) {
			shuffle.setAlpha(1);
		} else {
			shuffle.setAlpha((float) 0.5);
		}
	}

	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		if (!isClickable) {
			playerCurrTime.setText("0:00");
		}
	}

	/**
	 * @param isPlaying
	 *            - If "true", the button changes the picture to "play", if
	 *            "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		play.setText(!player.isPlaying() ? getString(R.string.font_play) : getString(R.string.font_pause));
	}

	private void setDownloadButtonState(boolean state) {
		download.setClickable(state);
		download.setEnabled(state);
		((MaterialRippleLayout) download.getParent()).setEnabled(state);
	}

	private void openArtistField() {
		if (tvArtist.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			contentView.findViewById(R.id.artistNameBox).setVisibility(View.GONE);
			etArtist.setVisibility(View.VISIBLE);
			etArtist.requestFocus();
			etArtist.setText(song.getArtist());
			int size = song.getArtist().length();
			etArtist.setSelection(size);
		} else {
			tvArtist.setVisibility(View.VISIBLE);
			etArtist.setVisibility(View.GONE);
		}
	}

	private void openTitleField() {
		if (tvTitle.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			contentView.findViewById(R.id.songNameBox).setVisibility(View.GONE);
			etTitle.setVisibility(View.VISIBLE);
			etTitle.requestFocus();
			etTitle.setText(song.getTitle());
			int size = song.getTitle().length();
			etTitle.setSelection(size);
		} else {
			tvTitle.setVisibility(View.VISIBLE);
			etTitle.setVisibility(View.GONE);
		}
	}

	private void closeEditViews() {
		if (etArtist.getVisibility() == View.VISIBLE || etTitle.getVisibility() == View.VISIBLE) {
			Util.hideKeyboard(getActivity(), contentView);
			if (etTitle.getVisibility() == View.VISIBLE && !song.getTitle().equals(etTitle.getText().toString())) {
				String title = Util.removeSpecialCharacters(etTitle.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : etTitle.getText().toString());
				song.setTitle(title);
				tvTitle.setText(title);
			} else if (etArtist.getVisibility() == View.VISIBLE && !song.getArtist().equals(etArtist.getText().toString())) {
				String artist = Util.removeSpecialCharacters(etArtist.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : etArtist.getText().toString());
				song.setArtist(artist);
				tvArtist.setText(artist);
			}
			contentView.findViewById(R.id.artistNameBox).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.songNameBox).setVisibility(View.VISIBLE);
			etArtist.setVisibility(View.GONE);
			etTitle.setVisibility(View.GONE);
			if (song.getClass() != MusicData.class) {
				player.update(song.getTitle(), song.getArtist(), null);
			} else {
				saveTags();
			}
		}
	}

	private void showLyrics() {
		if (null != lyricsFetcher) {
			lyricsFetcher.cancel();
			playerLyricsView.setText("");
		}
		lyricsFetcher = new LyricsFetcher(getActivity());
		lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
		OnLyricsFetchedListener fetchedListener = new OnLyricsFetchedListener() {

			@Override
			public void onLyricsFetched(boolean foundLyrics, String lyrics) {
				if (hashCode() != checkIdLyrics) {
					return;
				}
				contentView.findViewById(R.id.lyrics_progress).setVisibility(View.GONE);
				contentView.findViewById(R.id.lyrics_text).setVisibility(View.VISIBLE);
				if (foundLyrics) {
					playerLyricsView.setText(Html.fromHtml(lyrics));
				} else {
					String songName = song.getArtist() + " - " + song.getTitle();
					playerLyricsView.setText(getResources().getString(R.string.download_dialog_no_lyrics, songName));
				}
			}
		};
		lyricsFetcher.setOnLyricsFetchedListener(fetchedListener);
		checkIdLyrics = fetchedListener.hashCode();
	}

	private void saveTags() {
		File f = new File(song.getPath());
		if (new File(f.getParentFile() + "/" + song.getArtist() + " - " + song.getTitle() + ".mp3").exists()) {
			((MainActivity) getActivity()).showMessage(R.string.file_already_exists);
			return;
		}
		RenameTaskSuccessListener renameListener = new RenameTaskSuccessListener() {

			@Override
			public void success(String path) {
				song.setPath(path);
				renameTask.cancelProgress();
				player.update(song.getTitle(), song.getArtist(), path);
			}

			@Override
			public void error() {
			}

		};
		renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, song.getArtist(), song.getTitle(), song.getAlbum());
		renameTask.start(true, false);
	}

	private void getCover(final AbstractSong song) {
		cbUseCover.setOnCheckedChangeListener(null);
		setCheckBoxState(false);
		if (song.isHasCover()) {
			Bitmap bitmap = song.getCover(getActivity());
			if (null != bitmap) {
				setCoverToZoomView(bitmap);
				setCheckBoxState(true);
				((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
				return;
			}
		}
		((View) cbUseCover.getParent()).setVisibility(View.GONE);
		if (song.getClass() != MusicData.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (hashCode() != checkIdCover)
						return;
					if (null != bmp) {
						((RemoteSong) song).setHasCover(true);
						((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
						setCoverToZoomView(bmp);
						player.updatePictureNotification(bmp);
						setCheckBoxState(true);
					}
				}
			};
			checkIdCover = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);
		}
		cbUseCover.setOnCheckedChangeListener(this);
	}

	/**
	 * @param bitmap
	 *            - set to image view, if bitmap == null then use default cover
	 */
	private void setCoverToZoomView(Bitmap bitmap) {
		if (isDestroy) return;
		imageView = new ImageView(getActivity());
		imageView.setPadding(8, 8, 8, 8);
		String cover =  getResources().getString(R.string.font_musics);
		Display display = getActivity().getWindowManager().getDefaultDisplay(); 
		int width = display.getWidth(); 
		int height = display.getHeight();
		int coverHeight = Math.abs(height - contentView.getMeasuredHeight() - Util.dpToPx(getActivity(), 72));
		int minHeight = (coverHeight > width )? width : coverHeight;
		imageView.setImageBitmap(null == bitmap ? ((MainActivity) getActivity()).getDefaultBitmapCover(minHeight, minHeight, minHeight - 32, cover) : bitmap);
		imageView.setMinimumHeight(minHeight);
		imageView.setMinimumWidth(minHeight);
		imageView.setMaxHeight(minHeight + Util.dpToPx(getActivity(), 8)); 
		imageView.setMaxWidth(minHeight + Util.dpToPx(getActivity(), 8));
		scrollView.setZoomView(imageView);
	}
	
	private void setCheckBoxState(boolean state) {
		if (isAdded()) {
			isUseAlbumCover = state;
			cbUseCover.setChecked(state);
			cbUseCover.setClickable(state);
			cbUseCover.setEnabled(state);
		}
	}

	private void download() {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		downloadListener = new DownloadListener(getActivity(), (RemoteSong) song, id);
		if (downloadListener.isBadInet()) {
			((MainActivity) getActivity()).showMessage(R.string.search_message_no_internet);
			return;
		}
		if (downloadListener.getSongID() == -1) {
			download.setIndeterminateProgressMode(true);
			download.setProgress(50);
		}
		downloadListener.setUseAlbumCover(isUseAlbumCover);
		((RemoteSong) song).getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (!url.startsWith("http")) {
					((MainActivity) getActivity()).showMessage(R.string.error_retrieving_the_url);
					return;
				}
				((RemoteSong) song).setDownloadUrl(url);
				StateKeeper.getInstance().putSongInfo(url, new SongInfo(SongInfo.DOWNLOADING, ((RemoteSong) song)));
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						downloadListener.onClick(contentView);
						downloadListener.setCancelCallback(new OnCancelDownload() {
							@Override
							public void onCancel() {
								cancelProgressTask();
							}
						});
						if (downloadListener.getSongID() == -1) {
							progressUpdater = new ProgressUpdater();
							progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,downloadListener.getDownloadId());
						}
					}
				});
			}

			@Override
			public void error(String error) {
			}
		});
	}
	
	private void thatSongIsDownloaded() {
		if (player.getPlayingSong().getClass() == MusicData.class) return;
		player.getPlayingSong().getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (isDestroy) return;
				DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
				Cursor running = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
				if (running != null) {
					while (running.moveToNext()) {
						if (null != url && url.equals(running.getString(running.getColumnIndex(DownloadManager.COLUMN_URI)))) {
							long id = running.getLong(running.getColumnIndex(DownloadManager.COLUMN_ID));
							progressUpdater = new ProgressUpdater();
							progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
						} else {
							download.setProgress(0);
							download.setOnClickListener(PlayerFragment.this);
							setDownloadButtonState(true);
						}
					}
					running.close();
				}
			}

			@Override
			public void error(String error) {
			}
		});
	}

	@Override
	public int getDrawerIcon() {
		return R.string.font_play;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.tab_now_plaing;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}

	@Override
	public void error(final String error) {
		((MainActivity) getActivity()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				((MainActivity) getActivity()).showMessage(error);
				player.stopPressed();
				download.setProgress(0);
				download.setIndeterminateProgressMode(false);
				download.setOnClickListener(PlayerFragment.this);
				setDownloadButtonState(true);
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
		if (null != bitmap) {
			setCoverToZoomView(bitmap);
		}
		super.onConfigurationChanged(newConfig);
	}
	
	private class ProgressUpdater extends AsyncTask<Long, Integer, String> {

		private static final String FAILURE = "failure";

		@Override
		protected void onPreExecute() {
			((MaterialRippleLayout)download.getParent()).setEnabled(false);
			download.setClickable(false);
			download.setOnClickListener(null);
			download.setIndeterminateProgressMode(true);
			download.setProgress(50);
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Long... params) {
			if (isDestroy) return null;
			DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
			int progress = 0;
			do {		
				if (isCancelled()) return null;
				if (params[0] != -1) {
					Cursor c = manager.query(new DownloadManager.Query().setFilterById(params[0]));
					if (c.moveToNext()) {
						int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
						int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
						int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
						int size = c.getInt(sizeIndex);
						int downloaded = c.getInt(downloadedIndex);
						switch (status) {
						case DownloadManager.STATUS_FAILED:
						case DownloadManager.ERROR_CANNOT_RESUME:
						case DownloadManager.ERROR_FILE_ERROR:
						case DownloadManager.ERROR_HTTP_DATA_ERROR:
						case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
						case DownloadManager.ERROR_UNKNOWN:
							return FAILURE;
						case DownloadManager.STATUS_RUNNING:
							if (size != -1 && size != 0) {
								progress = downloaded * 100 / size;
							} else {
								progress = downloaded * 100 / DEFAULT_SONG;
							}
							publishProgress(progress);
							break;
						case DownloadManager.STATUS_SUCCESSFUL:
							File file = new File(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
							if (downloaded < BAD_SONG || downloaded != size) {
								file.delete();
								return FAILURE;
							}
							progress = 100;
							publishProgress(100);
							break;
						}
					}
					c.close();
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (progress < 100);
			return null;
		}

		@Override
		protected void onPostExecute(String params) {
			if(FAILURE.equals(params)) {
				((MainActivity) getActivity()).showMessage(R.string.download_failed);
				download.setProgress(0);
				download.setOnClickListener(PlayerFragment.this);
				setDownloadButtonState(true);
				this.cancel(true);
			} else {
				download.setProgress(isCancelled() ? 0 : 100);
			}
		}
		
		@Override
		protected void onCancelled() {
			download.setOnClickListener(PlayerFragment.this);
			download.setIndeterminateProgressMode(false);
			download.setProgress(0);
			cancel(true);
			super.onCancelled();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (isCancelled()) return;
			int progress = values[0];
			download.setIndeterminateProgressMode(false);
			download.setProgress(progress > 0 ? progress : 1);
			download.setClickable(false);
			((MaterialRippleLayout)download.getParent()).setEnabled(false);
		}
	}
}