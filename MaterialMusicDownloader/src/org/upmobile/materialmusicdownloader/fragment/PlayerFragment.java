package org.upmobile.materialmusicdownloader.fragment;

import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import org.upmobile.materialmusicdownloader.BaseDownloadListener;
import org.upmobile.materialmusicdownloader.BaseDownloadListener.OnCancelDownload;
import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.ui.TrueSeekBar;

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
import ru.johnlife.lifetoolsmp3.tasks.ProgressUpdaterTask.ProgressUpdaterListener;
import ru.johnlife.lifetoolsmp3.tasks.RenameTask;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import ru.johnlife.lifetoolsmp3.utils.DownloadCache;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.buttons.CheckBox;
import ru.johnlife.uilibrary.widget.buttons.processbutton.iml.ActionProcessButton;
import ru.johnlife.uilibrary.widget.customviews.RippleView;
import ru.johnlife.uilibrary.widget.layouts.pulltozoomview.PullToZoomScrollViewEx;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.AdvancedUndoListener;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoBar;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoListener;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class PlayerFragment extends Fragment implements OnClickListener, BaseMaterialFragment, OnCheckedChangeListener, PlaybackService.OnErrorListener, OnEditorActionListener {

	private static final int MESSAGE_DURATION = 5000;
	private AbstractSong song;
	private AsyncTask<Long,Integer,String> progressUpdater;
	private RenameTask renameTask;
	private PlaybackService player;
	private BaseDownloadListener downloadListener;
	private ProgressUpdaterListener progressListener;
	private RemoteSong.OnBitmapReadyListener readyListener;

	private View contentView;
	private View headView;
	private ImageView coverView;
	private View playerContent;
	private PullToZoomScrollViewEx scrollView;

	private ActionProcessButton download;
	private RippleView ciRippleView;
	private UndoBar undo;
	private LinearLayout artistBox;
	private LinearLayout titleBox;

	// lyric sections
	private SearchLyrics lyricsFetcher;
	private TextView playerLyricsView;
	
	//custom check box
	private CheckBox cbUseCover;
	private Boolean isUseAlbumCover = false;

	// playback sections
	private TextView play;
	private TextView previous;
	private TextView forward;
	private TextView shuffle;
	private TextView repeat;
	private Bitmap bitmap;
	private TextView shuffleMode;

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TrueSeekBar playerProgress;
	private Bitmap defaultCover;
	private ActionProcessButton showInLib;

	private int checkIdCover;
	private int checkIdLyrics;
	private double percent = 0;

	private boolean isDestroy;
	private boolean hasPost;
	private boolean showInfoMessage = false;
	
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
			setElementsView(0, s);
		}
		
		@Override
		public void stopPressed(){}

		@Override
		public void error() {}

		@Override
		public void start(AbstractSong s) {
			song = s;
			if (isDestroy) return;
			setDownloadButtonState(true);
			changePlayPauseView(true);
			setElementsView(0, s);
			play.setClickable(true);
			playerProgress.setMax((int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
			showDownloadedLabel();
		}

		@Override
		public void update(AbstractSong current) {
			if (isDestroy) return;
			download.setMode(ActionProcessButton.Mode.PROGRESS);
			download.setProgress(0);
			playerProgress.setProgress(0);
			playerProgress.setSecondaryProgress(0);
			song = current;
			setElementsView(0, current);
			showLyrics();
			getCover(song);
			thatSongIsDownloaded(current);
			playerProgress.setIndeterminate(true);
			contentView.findViewById(R.id.lyrics_progress).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.lyrics_text).setVisibility(View.GONE);
			showDownloadedLabel();
		}

		@Override
		public void onBufferingUpdate(final double percent) {
			PlayerFragment.this.percent = percent;
		}

		@Override
		public void onTrackTimeChanged(final int time, final boolean isOverBuffer) {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					playerProgress.setIndeterminate(isOverBuffer);
					playerProgress.setProgress(time);
					if (time < playerProgress.getMax() * percent) {
						playerProgress.setSecondaryProgress(0);
						playerProgress.setSecondaryProgress((int) (playerProgress.getMax() * percent));
					}
					playerCurrTime.setText(Util.getFormatedStrDuration(time));
				}
			});
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		contentView = inflater.inflate(R.layout.player_fragment, container, false);
		scrollView = (PullToZoomScrollViewEx) contentView.findViewById(R.id.scroll_view);
		loadViewForCode();
		isDestroy = false;
		player = PlaybackService.get(getActivity());
		song = player.getPlayingSong();
		init();
		setListeners();
		initHeaderSize();
		showInfoMessage = MaterialMusicDownloaderApp.getSharedPreferences().getBoolean(ru.johnlife.lifetoolsmp3.Constants.PREF_SHOW_INFO_MESSAGE, true);
		return contentView;
	}

	private void initHeaderSize() {
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
		int mScreenWidth = localDisplayMetrics.widthPixels;
		int orientation = getActivity().getResources().getConfiguration().orientation;
		int outWidth = (int) ((orientation == Configuration.ORIENTATION_PORTRAIT ? 10.0F : 4.0F) * mScreenWidth / 16.0F);
		LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(mScreenWidth, outWidth);
		String cover =  getResources().getString(R.string.font_musics);
		defaultCover = ((MainActivity) getActivity()).getDefaultBitmapCover(outWidth, outWidth, outWidth - 16, cover);
		defaultCover = addBorder(defaultCover, outWidth / 2.6f);
		scrollView.setHeaderLayoutParams(localObject);
	}
	
	private Bitmap addBorder(Bitmap bmp, float borderSize) {
	    try {
			Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + (int)borderSize * 2, bmp.getHeight() + (int)borderSize * 2, bmp.getConfig());
			Canvas canvas = new Canvas(bmpWithBorder);
			canvas.drawColor(Color.TRANSPARENT);
			canvas.drawBitmap(bmp, borderSize, borderSize, null);
			return bmpWithBorder;
		} catch (OutOfMemoryError e) {
			Log.e(getClass().getSimpleName(), e + "");
		}
	    return bmp;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		getActivity().onBackPressed();
		((MainActivity) getActivity()).setDrawerEnabled(true);
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onResume() {
		MainActivity activity = ((MainActivity) getActivity());
		activity.setSelectedItem(Constants.PLAYER_FRAGMENT);
		activity.setTitle(getDrawerTitle());
		activity.setVisibleSearchView(false);
		setHasOptionsMenu(true);
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		if (song.getClass() != MusicData.class) {
			if (StateKeeper.DOWNLOADING == state) {
				((RippleView) download.getParent()).setEnabled(false);
				download.setClickable(false);
				download.setMode(ActionProcessButton.Mode.ENDLESS);
				download.setProgress(50);
			}
		} else {
			download.setVisibility(View.GONE);
			ciRippleView.setVisibility(View.GONE);
		}
		showDownloadedLabel();
		percent = 0;
		setCoverToZoomView(null);
		getCover(song);
		setImageButton();
		if (playerLyricsView.getText().toString().isEmpty()) {
			showLyrics();
		}
		setElementsView(player.getCurrentPosition(), song);
		boolean prepared = player.isPrepared();
		playerProgress.setIndeterminate(prepared);
		if (prepared) {
			changePlayPauseView(!player.isPlaying());
			playerProgress.setMax((int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
		} else {
			changePlayPauseView(prepared);
			play.setClickable(false);
			playerProgress.setProgress(0);
			playerProgress.setSecondaryProgress(0);
			playerCurrTime.setText("0:00");
		}
		setCheckBoxState(true);
		cbUseCover.setOnCheckedChangeListener(this);
		cancelProgressTask();
		initUpdater();
		thatSongIsDownloaded(song);
		super.onResume();
	}

	private void showDownloadedLabel() {
		boolean show = isThisSongDownloaded() && song.getClass() != MusicData.class;
		scrollView.findViewById(R.id.downloadedText).setVisibility(show ? View.VISIBLE : View.GONE);
		((View) showInLib.getParent()).setVisibility(show ? View.VISIBLE : View.GONE);
		download.setText(show ? R.string.download_anyway : R.string.download_dialog_download);
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
		((MainActivity) getActivity()).showPlayerElement(player.isPrepared());
		if (null != lyricsFetcher) {
			lyricsFetcher.cancelSearch();
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
		((MainActivity) getActivity()).setDrawerEnabled(true);
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
			repeat.setAlpha(player.offOnRepeat() ? 1 : (float) 0.5);
			break;
		case R.id.shuffleParent:
			shuffle.setAlpha(player.offOnShuffle() ? 1 : (float) 0.5);
			if (player.enabledShuffleAll()) {
				shuffleMode.setVisibility(View.VISIBLE);
				if (player.enabledShuffleAuto()) {
					shuffleMode.setText("A");
				} else {
					shuffleMode.setText("M");
				}
			} else {
				shuffleMode.setVisibility(View.INVISIBLE);
			}
			break;
		case R.id.showButton:
			showSongInLibrary();
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

	private void showSongInLibrary() {
		((MainActivity)getActivity()).changeFragment(new LibraryFragment(), false, song);
	}

	private boolean isThisSongDownloaded() {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
	}
	
	private void showInfoMessage() {
		if (showInfoMessage) {
			undo.clear();
			undo.message(ru.johnlife.lifetoolsmp3.R.string.changes_relevant_when_downloading);
			undo.duration(MESSAGE_DURATION);
			undo.style(new UndoBarStyle(-1, ru.johnlife.lifetoolsmp3.R.string.not_show_again));
			undo.listener(new UndoListener() {
				
				@Override
				public void onUndo(Parcelable token) {
					MaterialMusicDownloaderApp.getSharedPreferences().edit().putBoolean(ru.johnlife.lifetoolsmp3.Constants.PREF_SHOW_INFO_MESSAGE, false).apply();
					showInfoMessage = false;
				}
			});
			undo.show();
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
		if (!buttonView.isEnabled()) return;
		isUseAlbumCover = isChecked;
		cbUseCover.setChecked(isChecked);
		closeEditViews();
		if (song.getClass() != MusicData.class) return;
		if (isChecked) {
			undo.clear();
		} else {
			AdvancedUndoListener listener = new AdvancedUndoListener() {

				@Override
				public void onUndo(@Nullable Parcelable token) {
					getCover(song);
					cbUseCover.setOnCheckedChangeListener(null);
					cbUseCover.setChecked(true);
					isUseAlbumCover = true;
					cbUseCover.setOnCheckedChangeListener(PlayerFragment.this);
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
			((MainActivity) getActivity()).setCover(null);
			((View)cbUseCover.getParent()).setVisibility(View.INVISIBLE);
			setCoverToZoomView(null);
			((MusicData) song).clearCover();
			RenameTask.deleteCoverFromFile(new File(song.getPath()));
		}
	}

	private void init() {
		play = (TextView) playerContent.findViewById(R.id.playpause);
		previous = (TextView) playerContent.findViewById(R.id.prev);
		forward = (TextView) playerContent.findViewById(R.id.next);
		shuffle = (TextView) playerContent.findViewById(R.id.shuffle);
		repeat = (TextView) playerContent.findViewById(R.id.repeat);
		download = (ActionProcessButton) playerContent.findViewById(R.id.download);
		showInLib = (ActionProcessButton) contentView.findViewById(R.id.showButton);
		ciRippleView = (RippleView) playerContent.findViewById(R.id.circularRipple);
		playerProgress = (TrueSeekBar) playerContent.findViewById(R.id.progress_track);
		tvTitle = (TextView) playerContent.findViewById(R.id.songName);
		etTitle = (EditText) playerContent.findViewById(R.id.songNameEdit);
		tvArtist = (TextView) playerContent.findViewById(R.id.artistName);
		etArtist = (EditText) playerContent.findViewById(R.id.artistNameEdit);
		playerCurrTime = (TextView) playerContent.findViewById(R.id.trackTime);
		playerTotalTime = (TextView) playerContent.findViewById(R.id.trackTotalTime);
		playerLyricsView = (TextView) playerContent.findViewById(R.id.lyrics_text);
		cbUseCover = (CheckBox) playerContent.findViewById(R.id.cbUseCover);
		artistBox = (LinearLayout) playerContent.findViewById(R.id.artistNameBox);
		titleBox = (LinearLayout) playerContent.findViewById(R.id.songNameBox);
		shuffleMode = (TextView) contentView.findViewById(R.id.shuffleMode);
		undo = new UndoBar(getActivity());
	}

	private void setListeners() {
		play.setOnClickListener(this);
		repeat.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		tvTitle.setOnClickListener(this);
		tvArtist.setOnClickListener(this);
		download.setOnClickListener(this);
		artistBox.setOnClickListener(this);
		titleBox.setOnClickListener(this);
		showInLib.setOnClickListener(this);
		playerContent.findViewById(R.id.shuffleParent).setOnClickListener(this);
		playerProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					int progress = seekBar.getProgress();
					seekBar.setSecondaryProgress(0);
					seekBar.setSecondaryProgress(Math.max(progress, (int) (playerProgress.getMax() * percent)));
					player.seekTo(progress);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					playerCurrTime.setText(Util.getFormatedStrDuration(progress));
				}
			}

		});
		playerContent.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				closeEditViews();
				return v.performClick();
			}

		});
		etArtist.setOnEditorActionListener(this);
		etTitle.setOnEditorActionListener(this);
		player.addStatePlayerListener(stateListener);
		player.setOnErrorListener(this);
	}
	
	private void initUpdater() {
		progressListener = new ProgressUpdaterListener() {

			private static final String FAILURE = "failure";
			boolean canceled = false;

			@Override
			public void onProgressUpdate(Integer... values) {
				if (canceled) return;
				int progress = values[0];
				download.setMode(ActionProcessButton.Mode.PROGRESS);
				download.setProgress(progress > 0 ? progress : 1);
				download.setClickable(false);
				((RippleView) download.getParent()).setEnabled(false);
			}

			@Override
			public void onCancelled() {
				canceled = true;
				((RippleView) download.getParent()).setEnabled(true);
				download.setProgress(0);
				download.setClickable(true);
				download.setEnabled(true);
			}

			@Override
			public void onPostExecute(String params) {
				if (FAILURE.equals(params)) {
					((MainActivity) getActivity()).showMessage(R.string.download_failed);
					download.setProgress(0);
					setDownloadButtonState(true);
				} else {
					((View) showInLib.getParent()).setVisibility(song.getClass() != MusicData.class ? View.VISIBLE : View.GONE);
					download.setProgress(canceled ? 0 : 100);
				}
			}

			@Override
			public void onPreExecute() {
				canceled = false;
				((RippleView) download.getParent()).setEnabled(false);
				download.setClickable(false);
				download.setMode(ActionProcessButton.Mode.ENDLESS);
				download.setProgress(50);
			}

		};
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
			if (!player.isPrepared()){
				playerProgress.setIndeterminate(true);
			}
			player.play(song);
			return;
		}
		if (!isUseAlbumCover && song.isHasCover()) {
			if (null != undo) {
				undo.clear();
			}
			clearCover();
		}
		if (hasPost) {
			download.removeCallbacks(postDownload);
			forceDownload();
			hasPost = false;
		}
		player.pause();
		player.shift(delta, true);
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
			ciRippleView.setVisibility(View.GONE);
		} else {
			download.setVisibility(View.VISIBLE);
			ciRippleView.setVisibility(View.VISIBLE);
		}
		setCoverToZoomView(null);
		showDownloadedLabel();
		cbUseCover.setOnCheckedChangeListener(null);
		setDownloadButtonState(!player.isGettingURl());
		play.setClickable(false);
		playerProgress.setProgress(0);
		playerProgress.setIndeterminate(true);
		if (!player.enabledRepeat()) {
			setCheckBoxState(false);
			download.setProgress(0);
		}
		cancelProgressTask();
		cbUseCover.setOnCheckedChangeListener(this);
	}

	private void setElementsView(int progress, AbstractSong song) {
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		playerTotalTime.setText(Util.getFormatedStrDuration(song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
		playerCurrTime.setText(Util.getFormatedStrDuration(progress));
	}

	private void setImageButton() {
		repeat.setAlpha(player.enabledRepeat() ? 1 : (float) 0.5);
		shuffle.setAlpha(player.enabledShuffleAll() ? 1 : (float) 0.5);
		if (player.enabledShuffleAll()) {
			shuffleMode.setVisibility(View.VISIBLE);
			if (player.enabledShuffleAuto()) {
				shuffleMode.setText("A");
			} else {
				shuffleMode.setText("M");
			}
		} else {
			shuffleMode.setVisibility(View.INVISIBLE);
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
		ciRippleView.setEnabled(state);
	}
	
	public void setupDownloadButton() {
		download.setProgress(0);
		setDownloadButtonState(true);
	}

	private void openArtistField() {
		if (tvArtist.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			contentView.findViewById(R.id.artistNameBox).setVisibility(View.GONE);
			etArtist.setVisibility(View.VISIBLE);
			etArtist.requestFocus();
			etArtist.setText(song.getArtist());
			sizeWatcher(etArtist);
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
			sizeWatcher(etTitle);
			int size = song.getTitle().length();
			etTitle.setSelection(size);
		} else {
			tvTitle.setVisibility(View.VISIBLE);
			etTitle.setVisibility(View.GONE);
		}
	}
	
	private void sizeWatcher(final EditText editText) {
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				LayoutParams params = editText.getLayoutParams();
				params.width = LayoutParams.WRAP_CONTENT;
				editText.setLayoutParams(params);
				etTitle.invalidate();
			}
		});
	}

	private void closeEditViews() {
		if (etArtist.getVisibility() == View.VISIBLE || etTitle.getVisibility() == View.VISIBLE) {
			Boolean isSameWord = false;
			Util.hideKeyboard(getActivity(), getActivity().getCurrentFocus());
			String title = song.getTitle();
			String artist = song.getArtist();
            String changedTitle = Util.removeSpecialCharacters(etTitle.getText().toString());
            String changedArtist = Util.removeSpecialCharacters(etArtist.getText().toString());
			if (etTitle.getVisibility() == View.VISIBLE && !title.equals(changedTitle)) {
				String newTitle = Util.removeSpecialCharacters(changedTitle.isEmpty() ? MP3Editor.UNKNOWN : changedTitle);
				song.setTitle(newTitle);
				tvTitle.setText(newTitle);
			} else if (etArtist.getVisibility() == View.VISIBLE && !artist.equals(changedArtist)) {
				String newArtist = Util.removeSpecialCharacters(changedArtist.isEmpty() ? MP3Editor.UNKNOWN : changedArtist);
				song.setArtist(newArtist);
				tvArtist.setText(newArtist);
			} else {
				isSameWord = true;
			}
			if (!isSameWord && song.getClass() == MusicData.class) {
				if (etTitle.getVisibility() == View.VISIBLE && isSameNameExists(artist, changedTitle)) {
					song.setTitle(title);
					tvTitle.setText(title);
				} else if (etArtist.getVisibility() == View.VISIBLE && isSameNameExists(changedArtist, title)) {
					song.setArtist(artist);
					tvArtist.setText(artist);
				}
			}
			
			contentView.findViewById(R.id.artistNameBox).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.songNameBox).setVisibility(View.VISIBLE);
			etArtist.setVisibility(View.GONE);
			etTitle.setVisibility(View.GONE);
			if (song.getClass() != MusicData.class) {
				player.update(song.getTitle(), song.getArtist(), null);
				showInfoMessage();
			} else if (!isSameWord){
				saveTags();
			}
		}
	}
	
	private boolean isSameNameExists(String artist, String title) {
		StringBuilder path = new StringBuilder(new File(song.getPath()).getParentFile().toString())
				.append("/").append(artist).append(" - ").append(title).append(".mp3");
		if (new File(path.toString()).exists()) {
			((MainActivity) getActivity()).showMessage(R.string.file_already_exists);
			return true;
		}
		return false;
	}

	private void showLyrics() {
		if (null != lyricsFetcher) {
			lyricsFetcher.cancelSearch();
			playerLyricsView.setText("");
		}
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
		lyricsFetcher = new SearchLyrics(fetchedListener,song.getArtist(),song.getTitle());
		lyricsFetcher.startSearch();
		checkIdLyrics = fetchedListener.hashCode();
	}

	private void saveTags() {
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
		final Animation blind = AnimationUtils.loadAnimation(getActivity(), R.anim.blind);
		coverView.post(new Runnable() {
			@Override
			public void run() {
				blind.cancel();
				coverView.clearAnimation();
				coverView.setAnimation(blind);
				blind.start();
			}
		});
		((View) cbUseCover.getParent()).setVisibility(View.INVISIBLE);
		cbUseCover.setOnCheckedChangeListener(null);
		setCheckBoxState(false);
		readyListener = new RemoteSong.OnBitmapReadyListener() {

			@Override
			public void onBitmapReady(final Bitmap bmp) {
				if (hashCode() != checkIdCover) {
					cbUseCover.setOnCheckedChangeListener(PlayerFragment.this);
					return;
				}
				coverView.post(new Runnable() {
					@Override
					public void run() {
						blind.cancel();
						coverView.clearAnimation();
						if (null != bmp) {
							if (song.getClass() != MusicData.class) {
								((RemoteSong) song).setHasCover(true);
							}
							((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
							setCoverToZoomView(bmp);
							player.updatePictureNotification(bmp);
							setCheckBoxState(true);
						}
						cbUseCover.setOnCheckedChangeListener(PlayerFragment.this);
					}
				});
			}
		};
		checkIdCover = readyListener.hashCode();
		if (song.getClass() == MusicData.class) {
			((MusicData) song).getCover(readyListener);
		} else {
			((RemoteSong) song).getCover(readyListener);
		}
	}

	/**
	 * @param bitmap - set to image view, if bitmap == null then use default cover
	 */
	private void setCoverToZoomView(Bitmap bitmap) {
		this.bitmap = bitmap;
		if (isDestroy) return;
		coverView.setImageBitmap(null == bitmap ? defaultCover : bitmap);
	}
	
	private void setCheckBoxState(boolean state) {
		if (isAdded()) {
			isUseAlbumCover = state;
			cbUseCover.setChecked(state);
			cbUseCover.setClickable(state);
			cbUseCover.setEnabled(state);
		}
	}
	
	private void forceDownload() {
		song.getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (!url.startsWith("http")) return;
				((RemoteSong) song).setDownloadUrl(url);
				downloadListener.onClick(contentView);
			}

			@Override
			public void error(String error) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						((MainActivity) getActivity()).showMessage(R.string.download_failed);
						download.setProgress(0);
						setDownloadButtonState(true);
					}
				});
			}
		});
	}
	
	Runnable postDownload = new Runnable() {
		public void run() {
			hasPost = false;
			downloadListener.onClick(contentView);
			song.getDownloadUrl(new DownloadUrlListener() {

				@Override
				public void success(String url) {
					if (!url.startsWith("http")) return;
					((RemoteSong) song).setDownloadUrl(url);
					new Handler(Looper.getMainLooper()).post(new Runnable() {

						@Override
						public void run() {
							if (downloadListener.getSongID() == -1) {
								progressUpdater = new ProgressUpdaterTask(progressListener, getActivity());
								progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadListener.getDownloadId());
								download.removeCallbacks(postDownload);
							}
						}
					});
				}

				@Override
				public void error(String error) {
					new Handler(Looper.getMainLooper()).post((new Runnable() {

						@Override
						public void run() {
							((MainActivity) getActivity()).showMessage(R.string.download_failed);
							download.setProgress(0);
							setDownloadButtonState(true);
						}
					}));
				}
			});
		}
	};

	private void download() {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		downloadListener = new BaseDownloadListener(getActivity(), (RemoteSong) song, id, true);
		if (downloadListener.isBadInet()) {
			((MainActivity) getActivity()).showMessage(R.string.search_message_no_internet);
			return;
		}
		if (downloadListener.getSongID() == -1) {
			download.setMode(ActionProcessButton.Mode.ENDLESS);
			download.setProgress(50);
		}
		hasPost = true;
		downloadListener.setUseAlbumCover(isUseAlbumCover);
		downloadListener.setCancelCallback(new OnCancelDownload() {
			@Override
			public void onCancel() {
				cancelProgressTask();
				download.removeCallbacks(postDownload);
			}
		});
		download.postDelayed(postDownload, 2000);
	}
	
	private void thatSongIsDownloaded(final AbstractSong checkingSong) {
		if (checkingSong.getClass() == MusicData.class) return;
		int status = StateKeeper.getInstance().checkSongInfo(checkingSong.getComment());
		ViewGroup parent = (ViewGroup) download.getParent();
		 if (status == StateKeeper.NOT_DOWNLOAD) {
			download.setVisibility(View.VISIBLE);
			parent.setVisibility(View.VISIBLE);
			download.setProgress(0);
			setDownloadButtonState(true);
			return;
		}
		checkingSong.getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (isDestroy) return;
				int [] statuses = {DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_SUCCESSFUL};
				DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
				for (int statusDownload : statuses) {
					Cursor cursor = manager.query(new DownloadManager.Query().setFilterByStatus(statusDownload));
					if (cursor != null) {
						while (cursor.moveToNext()) {
							if (null != url && url.equals(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)))) {
								long id = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
								progressUpdater = new ProgressUpdaterTask(progressListener, getActivity());
								progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, id);
								download.setMode(ActionProcessButton.Mode.PROGRESS);
								download.setProgress(50);
								cursor.close();
								return;
							} 
						} 
						cursor.close();
					}
				}
				boolean isCahed = DownloadCache.getInstanse().getCachedItem(checkingSong.getTitle(), checkingSong.getArtist()) != null;
				if (StateKeeper.getInstance().checkSongInfo(checkingSong.getComment()) == StateKeeper.DOWNLOADING && isCahed) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
				
						@Override
						public void run() {
							setDownloadButtonState(false);
							download.setMode(ActionProcessButton.Mode.ENDLESS);
							download.setProgress(50);
						}
					});
				}
			}

			@Override
			public void error(String error) {}
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
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((MainActivity) getActivity()).showMessage(error);
				player.stopPressed();
				download.setProgress(0);
				setDownloadButtonState(true);
			}
		});
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setCoverToZoomView(bitmap);
		contentView.invalidate();
		initHeaderSize();
		super.onConfigurationChanged(newConfig);
	}
	
    private void loadViewForCode() {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		headView = inflater.inflate(R.layout.player_header_view, null, false);
        coverView = (ImageView) inflater.inflate(R.layout.player_cover, null, false);
        playerContent = inflater.inflate(R.layout.player_fragment_content, null, false);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(coverView);
        scrollView.setScrollContentView(playerContent);
    }

}