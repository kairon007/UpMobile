package org.upmobile.newmaterialmusicdownloader.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.upmobile.newmaterialmusicdownloader.BaseDownloadListener;
import org.upmobile.newmaterialmusicdownloader.BaseDownloadListener.OnCancelDownload;
import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

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
import ru.johnlife.uilibrary.widget.customviews.playpause.PlayPauseView;
import ru.johnlife.uilibrary.widget.customviews.visualizer.SimpleVisualizerView;
import ru.johnlife.uilibrary.widget.form.deeskretseekbar.DiscreteSeekBar;
import ru.johnlife.uilibrary.widget.form.deeskretseekbar.DiscreteSeekBar.OnProgressChangeListener;
import ru.johnlife.uilibrary.widget.layouts.NotifyingScrollView;
import ru.johnlife.uilibrary.widget.layouts.NotifyingScrollView.OnScrollChangedListener;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.AdvancedUndoListener;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoBar;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoListener;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;
import ru.johnlife.uilibrary.widget.textviews.digitalclock.DigitalClockView;
import ru.johnlife.uilibrary.widget.textviews.digitalclock.font.DFont;

public class PlayerFragment extends Fragment implements Constants, OnClickListener , OnCheckedChangeListener, OnEditorActionListener {

	private static final int MESSAGE_DURATION = 5000;
	private AbstractSong song;
	private AsyncTask<Long,Integer,String> progressUpdater;
	private AsyncTask<Bitmap, Void, Palette> paletteGenerator;
	private RenameTask renameTask;
	private PlaybackService player;
	private BaseDownloadListener downloadListener;
	private ProgressUpdaterListener progressListener;
	private Bitmap bigDefaultCover;

	private View contentView;

	private NotifyingScrollView scrollView;
	private ActionProcessButton download;
	private LinearLayout artistBox;
	private LinearLayout titleBox;
	
	private UndoBar undo;
	private int undoMessage = 0;

	// lyric sections
	private SearchLyrics lyricsFetcher;
	private TextView playerLyricsView;
	
	// visualizer section
	private SimpleVisualizerView visualizerView;
	private Visualizer visualizer;
	private CheckBox cbShowVisualizer;
	private Boolean visualizerIsBroken = false;
	
	//custom check box
	private CheckBox cbUseCover;
	private Boolean isUseAlbumCover = false;

	// playback sections
	private PlayPauseView play;
	private ImageView previous;
	private ImageView forward;
	private ImageView shuffle;
	private ImageView repeat;
	private TextView shuffleMode;

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private DigitalClockView playerCurrTime;
	private DigitalClockView playerTotalTime;
	private DiscreteSeekBar playerProgress;
	private ActionProcessButton showInLib;

	private int checkIdCover;
	private int checkIdLyrics;
	private double percent = 0;
	
	private String lastArtist = EMPTY_STRING;
	private String lastTitle = EMPTY_STRING;
	
	private int primaryColor;

	private boolean isDestroy;
	private boolean hasPost;
	private boolean showInfoMessage = false;
    private ImageView scrollableCover;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		contentView = inflater.inflate(R.layout.player_fragment, container, false);
		contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		init();
		setListeners();
		player = PlaybackService.get(getActivity());
		player.addStatePlayerListener(stateListener);
		song = player.getPlayingSong();
		getCover(song);
		playerProgress.setMax((int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
		contentView.findViewById(R.id.controlPane).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		scrollView.recalculateCover(R.id.controlPane, R.id.visualizer);
		showInfoMessage = NewMaterialApp.getSharedPreferences().getBoolean(ru.johnlife.lifetoolsmp3.Constants.PREF_SHOW_INFO_MESSAGE, true);
		return contentView;
	}
	
	private void init() {
		scrollView = (NotifyingScrollView)contentView.findViewById(R.id.scroll_view);
        scrollableCover = (ImageView) contentView.findViewById(R.id.scrollable_cover);
		scrollView.setImageResource(R.id.scrollable_cover);
		generateDefaultCover();
		scrollView.setImageBitmap(bigDefaultCover);
		play = (PlayPauseView) contentView.findViewById(R.id.playpause);
		previous = (ImageView) contentView.findViewById(R.id.prev);
		forward = (ImageView) contentView.findViewById(R.id.next);
		shuffle = (ImageView) contentView.findViewById(R.id.shuffle);
		repeat = (ImageView) contentView.findViewById(R.id.repeat);
		shuffleMode = (TextView) contentView.findViewById(R.id.shuffleMode);
		download = (ActionProcessButton) contentView.findViewById(R.id.download);
		showInLib = (ActionProcessButton) contentView.findViewById(R.id.showButton);
		playerProgress = (DiscreteSeekBar) contentView.findViewById(R.id.progress_track);
		tvTitle = (TextView) contentView.findViewById(R.id.songName);
		etTitle = (EditText) contentView.findViewById(R.id.songNameEdit);
		tvArtist = (TextView) contentView.findViewById(R.id.artistName);
		etArtist = (EditText) contentView.findViewById(R.id.artistNameEdit);
		playerCurrTime = (DigitalClockView ) contentView.findViewById(R.id.trackTime);
		playerTotalTime = (DigitalClockView ) contentView.findViewById(R.id.trackTotalTime);
		playerLyricsView = (TextView) contentView.findViewById(R.id.lyrics_text);
		cbUseCover = (CheckBox) contentView.findViewById(R.id.cbUseCover);
		artistBox = (LinearLayout) contentView.findViewById(R.id.artistNameBox);
		titleBox = (LinearLayout) contentView.findViewById(R.id.songNameBox);
		visualizerView = (SimpleVisualizerView) contentView.findViewById(R.id.visualizer);
		cbShowVisualizer = (CheckBox) contentView.findViewById(R.id.cbShowEqualizer);
		DFont font = new DFont(Util.dpToPx(getActivity(), 12), 2);
		font.setColor(getResources().getColor(Util.getResIdFromAttribute(getActivity(), R.attr.colorTextSecondary)));
		playerCurrTime.setFont(font);
		playerTotalTime.setFont(font); 
		undo = new UndoBar(getActivity());
		previous.setColorFilter(primaryColor);
		forward.setColorFilter(primaryColor);
		shuffle.setColorFilter(primaryColor);
		repeat.setColorFilter(primaryColor);
	}	

	private void setListeners() {
		contentView.findViewById(R.id.shuffleParent).setOnClickListener(this);
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
		cbUseCover.setOnCheckedChangeListener(this);
		cbShowVisualizer.setOnCheckedChangeListener(this);
		playerProgress.setOnProgressChangeListener(new OnProgressChangeListener() {

			@Override
			public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
				try {
					player.seekTo(seekBar.getProgress());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
				if (fromUser && !player.isPlaying()) {
					playerCurrTime.setText(Util.getFormatedStrDuration(player.getCurrentPosition()));
				}
			}
		});
		scrollView.setOnScrollChangedListener(new OnScrollChangedListener() {

			@Override
			public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt, int alpha) {
				((MainActivity) getActivity()).setToolbarAlpha(alpha);
				playerProgress.forceHideFloater();
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
			Drawable drawableRepeat = repeat.getDrawable();
			drawableRepeat.setAlpha(!player.offOnRepeat() ? 125 : 255);
			repeat.setImageDrawable(drawableRepeat);
			break;
		case R.id.shuffleParent: 
			Drawable drawableShuffle = shuffle.getDrawable();
			drawableShuffle.setAlpha(player.offOnShuffle() ? 255 : 125);
			shuffle.setImageDrawable(drawableShuffle);
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
		((MainActivity)getActivity()).changeFragment(ManagerFragmentId.libraryFragment(), false, song);
	}

	private boolean isThisSongDownloaded(AbstractSong song) {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
	}

	private boolean isThisSongDownloaded() {
		return isThisSongDownloaded(song);
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
					NewMaterialApp.getSharedPreferences().edit().putBoolean(PREF_SHOW_INFO_MESSAGE, false).apply();
					showInfoMessage = false;
				}
			});
			undo.show();
		}
	}
	
	@Override
	public void onResume() {
		MainActivity act = (MainActivity) getActivity();
		act.setCurrentFragmentId(ManagerFragmentId.playerFragment());
		act.setDraverEnabled(false);
		act.setTitle(R.string.tab_now_plaing);
		act.invalidateOptionsMenu();
		act.setToolbarOverlay(true);
		act.setToolbarAlpha(scrollView.getToolbarAlpha());
		act.showToolbarShadow(true);
		SharedPreferences sp = NewMaterialApp.getSharedPreferences();
		boolean stateVisualizer = sp.getBoolean(PREF_VISUALIZER, false);
		cbShowVisualizer.setChecked(stateVisualizer);
		setupVisualizerFxAndUI(stateVisualizer);
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		if (song.getClass() != MusicData.class) {
			if (StateKeeper.DOWNLOADING == state) {
				((RippleView) download.getParent()).setEnabled(false);
				download.setClickable(false);
				download.setMode(ActionProcessButton.Mode.ENDLESS);
				download.setProgress(50);
			}
		} else {
			((View) showInLib.getParent()).setVisibility(View.GONE);
			((RippleView) download.getParent()).setVisibility(View.GONE);
		}
		showDownloadedLabel();
		final boolean prepared = player.isPrepared();
		if (!prepared) {
			playerProgress.setProgress(0);
			playerCurrTime.setTime("0:00");
		}
		changePlayPauseView(prepared && player.isPlaying());
		playerProgress.postDelayed(new Runnable() {

			@Override
			public void run() {
				playerProgress.setIndeterminate(!prepared);
			}

		}, 1000);
		setElementsView(player.getCurrentPosition());
		showLyrics();
		setImageButton();
		cancelProgressTask();
		initUpdater();
		thatSongIsDownloaded(song);
		super.onResume();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		primaryColor = getResources().getColor(Util.getResIdFromAttribute(activity, R.attr.colorPrimary));
	}

	@Override
	public void onDestroyView() {
		((MainActivity) getActivity()).setToolbarOverlay(false);
		player.removeStatePlayerListener(stateListener);
		isDestroy = true;
		cancelProgressTask();
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		playerProgress.forceHideFloater();
		((MainActivity) getActivity()).showPlayerElement(player.isPrepared());
		if (null != lyricsFetcher) {
			lyricsFetcher.cancelSearch();
		}
		if (!isUseAlbumCover && song.isHasCover()) {
			undoMessage = MSG_UNDO_REMOVE;
			undo.clear();
		}
		if (null != visualizer) {
			visualizer.setEnabled(false);
			visualizer.release();
			visualizer = null;
		}
		super.onPause();
	}
	
	private void setupVisualizerFxAndUI(boolean isShowVisualizer) {
		visualizerView.setVisibility(isShowVisualizer ? View.VISIBLE : View.INVISIBLE);
		if (null == visualizer && null != visualizerView && null != player) {
			try {
				visualizer = new Visualizer(player.getAudioSessionId());
				visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
				visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

					public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}

					public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
						visualizerView.updateVisualizer(bytes);
					}
				}, Visualizer.getMaxCaptureRate() / 2, false, true);
				visualizer.setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		visualizer.setEnabled(isShowVisualizer);
	}
	
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void pause(final AbstractSong song) {
			if (isDestroy) return;
			changePlayPauseView(false);
		}

		@Override
		public void play(final AbstractSong song) {
			if (isDestroy) return;
			changePlayPauseView(true);
		}

		@Override
		public void stop(final AbstractSong s) {
			if (isDestroy) return;
			changePlayPauseView(false);
			setElementsView(0);
		}
		
		@Override
		public void stopPressed(){}

		@Override
		public void error() {
			visualizerIsBroken = true;
			if (null != visualizer) {
				visualizer.release();
				visualizer = null;
			}
		}

		@Override
		public void start(final AbstractSong s) {
			song = s;
			if (isDestroy) return;
			if (visualizerIsBroken) setupVisualizerFxAndUI(cbShowVisualizer.isChecked());
			setDownloadButtonState(true);
			play.setClickable(true);
			play.toggle(true);
			setElementsView(0);
			showDownloadedLabel();
		}

		@Override
		public void update(AbstractSong current) {
			if (isDestroy) return;
			setDownloadButtonState(true);
			download.setProgress(0);
			song = current;
			setCoverToZoomView(null);
			getCover(song);
			showLyrics();
			setElementsView(0);
			thatSongIsDownloaded(current);
			showDownloadedLabel();
			playerProgress.setIndeterminate(true);
			playerProgress.setMax((int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
		}

		@Override
		public void onTrackTimeChanged(final int time, final boolean isOverBuffer) {
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					playerProgress.setProgress(time);
					if (time < playerProgress.getMax() * percent) {
						playerProgress.setSecondaryProgress((int)(playerProgress.getMax() * percent));
					}
					playerCurrTime.setText(Util.getFormatedStrDuration(time));
					if (!playerProgress.isPressed()) {
						playerProgress.setIndeterminate(isOverBuffer);
					}
				}
			});
		}

		@Override
		public void onBufferingUpdate(double percent) {
			PlayerFragment.this.percent = percent;
		}
	};
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
		closeEditViews();
		Util.hideKeyboard(getActivity(), buttonView);
		if (buttonView.getId() == R.id.cbShowEqualizer) {
			Editor editor = NewMaterialApp.getSharedPreferences().edit();
            editor.putBoolean(PREF_VISUALIZER, isChecked);
			editor.apply();
			setupVisualizerFxAndUI(isChecked);
			return;
		}
		if (!buttonView.isEnabled()) return;
		isUseAlbumCover = isChecked;
		cbUseCover.setChecked(isChecked);
		if (song.getClass() != MusicData.class) return;
		if (isChecked) {
			undoMessage = MSG_UNDO_NOTHING_DO;
			undo.clear();
		} else {
			AdvancedUndoListener listener = new AdvancedUndoListener() {

				@Override
				public void onUndo(@Nullable Parcelable token) {
					setCoverToZoomView(song.getCover());
					cbUseCover.setChecked(true);
					isUseAlbumCover = true;
				}

				@Override
				public void onHide(@Nullable Parcelable token) {
					clearCover();
				}

				@Override
				public void onClear(@NonNull Parcelable[] token) {
					switch (undoMessage) {
					case MSG_UNDO_NOTHING_DO:
						break;
					case MSG_UNDO_REMOVE:
						clearCover();
						break;
					}
					undoMessage = 0;
				}

			};
			undo.message(R.string.message_undo_bar);
			undo.duration(MESSAGE_DURATION);
			undo.listener(listener);
			undo.show();
		}
	}

	/**
	 * @param delta
	 *            - delta must be 1 or -1 or 0, 1 - next, -1 - previous, 0 -
	 *            current song
	 */
	private void play(final int delta) throws IllegalArgumentException {
		if (delta == 0) {
			if (!player.isPrepared()){
				playerProgress.setIndeterminate(true);
			}
			player.play(song);
			return;
		}
		if (!isUseAlbumCover && ((View) cbUseCover.getParent()).getVisibility() == View.VISIBLE) {
			undoMessage = MSG_UNDO_REMOVE;
			undo.clear();
		}
		if (hasPost) {
			download.removeCallbacks(postDownload);
			forceDownload();
			hasPost = false;
		}
		play.setClickable(false);
		player.pause();
		player.shift(delta, true);
		setDownloadButtonState(!player.isGettingURl());
		playerProgress.setProgress(0);
		playerProgress.setSecondaryProgress(0);
		playerProgress.setIndeterminate(true);
		if (!player.enabledRepeat()) {
			setCheckBoxState(false);
			cancelProgressTask();
			download.setProgress(0);
			download.setOnClickListener(this);
		}
		cancelProgressTask();
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
			((ViewGroup) download.getParent()).setVisibility(View.GONE);
		} else {
			download.setVisibility(View.VISIBLE);
			((ViewGroup) download.getParent()).setVisibility(View.VISIBLE);
		}
		showDownloadedLabel();
	}

	private void showDownloadedLabel() {
		boolean show = isThisSongDownloaded() && song.getClass() != MusicData.class;
		contentView.findViewById(R.id.downloadedText).setVisibility(show ? View.VISIBLE : View.GONE);
		((View) showInLib.getParent()).setVisibility(show ? View.VISIBLE : View.GONE);
		download.setText(show ? R.string.download_anyway : R.string.download_dialog_download);
	}

	private void cancelProgressTask() {
		if (null != progressUpdater) {
			progressUpdater.cancel(true);
		}
	}

	private void setElementsView(int progress) {
		playerProgress.setProgress(progress);
		download.setVisibility(song.getClass() == MusicData.class ? View.GONE : View.VISIBLE);
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		playerTotalTime.setTime(Util.getFormatedStrDuration((int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration())));
		playerCurrTime.setTime(Util.getFormatedStrDuration(progress));
	}

	private void setImageButton() {
		Drawable drawableRepeat = repeat.getDrawable();
		Drawable drawableShuffle = shuffle.getDrawable();
		if (!player.enabledRepeat()) {
			drawableRepeat.setAlpha(125);
			repeat.setImageDrawable(drawableRepeat);
		} else {
			drawableRepeat.setAlpha(255);
			repeat.setImageDrawable(drawableRepeat);
		}
		if (player.enabledShuffleAll()) {
			drawableShuffle.setAlpha(255);
			shuffle.setImageDrawable(drawableShuffle); 
			shuffleMode.setVisibility(View.VISIBLE);
			if (player.enabledShuffleAuto()) {
				shuffleMode.setText("A");
			} else {
				shuffleMode.setText("M");
			}
		} else {
			drawableShuffle.setAlpha(125);
			shuffle.setImageDrawable(drawableShuffle);
			shuffleMode.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * @param isPlaying
	 *            - If "true", the button changes the picture to "play", if
	 *            "false" changes to "pause"
	 */
	private void changePlayPauseView(final boolean isPlaying) {
		if (play.isPlay() != isPlaying) return;
		play.toggle(isPlaying);
	}

	private void setDownloadButtonState(final boolean state) {
		download.setClickable(state);
		download.setEnabled(state);
		((RippleView) download.getParent()).setEnabled(state);
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
			Util.hideKeyboard(getActivity(), contentView);
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
		StringBuilder path = new StringBuilder(new File(song.getPath()).getParentFile().getPath()).append("/").append(artist).append(" - ").append(title).append(".mp3");
		if (new File(path.toString()).exists()) {
			((MainActivity) getActivity()).showMessage(R.string.file_already_exists);
			return true;
		}
		return false;
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
			public void error() { }	
		};
		renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, song.getArtist(), song.getTitle(), song.getAlbum());
		renameTask.start(true, false);
	}

	private void showLyrics() {
		if (lastArtist.equals(song.getArtist()) && lastTitle.equals(song.getTitle()) && !playerLyricsView.getText().toString().isEmpty()) return;
		contentView.findViewById(R.id.lyrics_progress).setVisibility(View.VISIBLE);
		playerLyricsView.setVisibility(View.GONE);
		lastArtist = song.getArtist();
		lastTitle = song.getTitle();
		if (null != lyricsFetcher) {
			lyricsFetcher.cancelSearch();
			playerLyricsView.setText(EMPTY_STRING);
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
					StringBuilder songName = new StringBuilder(song.getArtist()).append(" - ").append(song.getTitle());
					playerLyricsView.setText(getResources().getString(R.string.download_dialog_no_lyrics, songName));
				}
			}
		};
		lyricsFetcher = new SearchLyrics(fetchedListener, song.getArtist(), song.getTitle());
		lyricsFetcher.startSearch();
		checkIdLyrics = fetchedListener.hashCode();
	}

	private void getCover(final AbstractSong s) {
		if (null == s) return;
		setCheckBoxState(false);
		Bitmap bitmap = s.getCover();
		if (null != bitmap) {
			setCoverToZoomView(bitmap);
			setCheckBoxState(true);
			((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
			return;
		}
		((View) cbUseCover.getParent()).setVisibility(View.INVISIBLE);
		if (s.getClass() != MusicData.class) {
			RemoteSong.OnBitmapReadyListener readyListener = new RemoteSong.OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover) return;
					if (null != bmp) {
						((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
						((RemoteSong) s).setHasCover(true);
						setCoverToZoomView(bmp);
						player.updatePictureNotification(bmp);
						setCheckBoxState(true);
					}
				}
			};
			checkIdCover = readyListener.hashCode();
			((RemoteSong) s).getCover(readyListener);
		}
	}

	private void clearCover() {
		setCheckBoxState(false);
		if (MusicData.class == song.getClass()) {
			((View) cbUseCover.getParent()).setVisibility(View.INVISIBLE);
			setCoverToZoomView(null);
			((MusicData) song).clearCover();
			((MainActivity) getActivity()).setCover(null);
			new Thread(new Runnable() {

				@Override
				public void run() {
					RenameTask.deleteCoverFromFile(new File(song.getPath()));
				}
				
			}).start();
		}
	}

	/**
	 * @param bitmap
	 *            - set to image view, if bitmap == null then use default cover
	 */
	private void setCoverToZoomView(Bitmap bitmap) {
		if (isDestroy) return;
		if (null != paletteGenerator) {
			paletteGenerator.cancel(true);
		}
		visualizerView.setUpVizualizerColor(-1, -1);
        if (null != bitmap) {
            paletteGenerator = Palette.generateAsync(bitmap, new PaletteAsyncListener() {


                @Override
                public void onGenerated(Palette palette) {
                    if (null == palette || null == palette.getVibrantSwatch() || null == palette.getMutedSwatch()) {
                        visualizerView.setUpVizualizerColor(-1, -1);
                        return;
                    }
                    visualizerView.setUpVizualizerColor(palette.getVibrantSwatch().getRgb(), palette.getMutedSwatch().getRgb());
                }
            });
            scrollableCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            scrollView.setImageBitmap(bitmap);
        } else {
            visualizerView.setUpVizualizerColor(-1, -1);
            scrollableCover.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            scrollView.setImageBitmap(bigDefaultCover);
        }
    }

	private void setCheckBoxState(final boolean state) {
		if (isAdded()) {
			isUseAlbumCover = state;
			cbUseCover.setOnCheckedChangeListener(null);
			cbUseCover.setChecked(state);
			cbUseCover.setClickable(state);
			cbUseCover.setEnabled(state);
			cbUseCover.setOnCheckedChangeListener(this);
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
			song.getDownloadUrl(new DownloadUrlListener() {

                @Override
                public void success(String url) {
                    if (!url.startsWith("http")) return;
                    ((RemoteSong) song).setDownloadUrl(url);
                    downloadListener.onClick(contentView);
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
                    if (null == getActivity()) return;
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
	public void onConfigurationChanged(Configuration newConfig) {
		scrollView.recalculateCover(R.id.controlPane, R.id.visualizer);
		super.onConfigurationChanged(newConfig);
	}
	
	public void hideIndicator() {
		playerProgress.forceHideFloater();
	}
	
	public void showIndicator() {
		if (playerProgress.isIndeterminate()) {
			playerProgress.showFloater();	
		}	
	}
	
	private void generateDefaultCover() {		
		Display display = getActivity().getWindowManager().getDefaultDisplay(); 
		int height = display.getHeight()/2;

		bigDefaultCover = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bigDefaultCover);
		canvas.drawARGB(0, 255, 255, 255);
		
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		p.setColor(getResources().getColor(R.color.def_cover_grey));
		p.setStyle(Style.STROKE);
		p.setStrokeWidth(height/5);
		canvas.drawCircle(height/2, height/2, height/2 - height/6, p);				
		
		p.setStyle(Style.FILL);
		p.setColor(getResources().getColor(R.color.def_cover_grey));
		canvas.drawCircle(height/2, height/2, height/20, p);
		
		canvas.setBitmap(bigDefaultCover);
	}
	
}
