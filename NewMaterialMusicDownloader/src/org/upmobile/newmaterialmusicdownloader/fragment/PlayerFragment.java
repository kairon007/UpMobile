package org.upmobile.newmaterialmusicdownloader.fragment;

import java.io.File;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.DownloadListener;
import org.upmobile.newmaterialmusicdownloader.DownloadListener.OnCancelDownload;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.ProgressUpdaterTask;
import ru.johnlife.lifetoolsmp3.ProgressUpdaterTask.ProgressUpdaterListener;
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
import ru.johnlife.lifetoolsmp3.ui.widget.NotifyingScrollView;
import ru.johnlife.lifetoolsmp3.ui.widget.NotifyingScrollView.OnScrollChangedListener;
import ru.johnlife.lifetoolsmp3.ui.widget.PlayPauseView;
import ru.johnlife.lifetoolsmp3.ui.widget.RippleView;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.AdvancedUndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import ru.johnlife.lifetoolsmp3.ui.widget.digitalclock.DigitalClockView;
import ru.johnlife.lifetoolsmp3.ui.widget.digitalclock.font.DFont;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.DiscreteSeekBar;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.DiscreteSeekBar.OnProgressChangeListener;
import ru.johnlife.lifetoolsmp3.ui.widget.progressbutton.CircularProgressButton;
import ru.johnlife.lifetoolsmp3.ui.widget.visualizer.SimpleVisualizerView;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class PlayerFragment extends Fragment implements Constants, OnClickListener , OnCheckedChangeListener, OnEditorActionListener {

	private final int MESSAGE_DURATION = 5000;
	private final int DEFAULT_SONG = 7340032; // 7 Mb
	private final int BAD_SONG = 2048; // 200kb
	private AbstractSong song;
	private AsyncTask<Long,Integer,String> progressUpdater;
	private AsyncTask<Bitmap, Void, Palette> paletteGenerator;
	private RenameTask renameTask;
	private PlaybackService player;
	private DownloadListener downloadListener;

	private View contentView;

	private NotifyingScrollView scrollView;
	private CircularProgressButton download;
	private LinearLayout artistBox;
	private LinearLayout titleBox;
	
	private UndoBar undo;
	private int undoMessage = 0;

	// lyric sections
	private LyricsFetcher lyricsFetcher;
	private TextView playerLyricsView;
	
	// visualizer section
	private SimpleVisualizerView visualizerView;
	private Visualizer visualizer;
	private CheckBox cbShowVisualizer;
	private boolean visualizerIsBroken = false;
	
	//custom check box
	private CheckBox cbUseCover;
	private boolean isUseAlbumCover = false;

	// playback sections
	private PlayPauseView play;
	private ImageView previous;
	private ImageView forward;
	private ImageView shuffle;
	private ImageView repeat;

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private DigitalClockView playerCurrTime;
	private DigitalClockView playerTotalTime;
	private DiscreteSeekBar playerProgress;

	private int checkIdCover;
	private int checkIdLyrics;
	private double percent = 0;
	
	private String lastArtist = "";
	private String lastTitle = "";
	
	private int primaryColor;

	private boolean isDestroy;
	
	private ProgressUpdaterListener progressListener = new ProgressUpdaterListener() {

		private static final String FAILURE = "failure";
		boolean canceled = false;

		@Override
		public void onProgressUpdate(Integer... values) {
			if (canceled) return;
			int progress = values[0];
			download.setIndeterminateProgressMode(false);
			download.setProgress(progress > 0 ? progress : 1);
			download.setClickable(false);
			((RippleView) download.getParent()).setEnabled(false);
		}

		@Override
		public void onCancelled() {
			canceled = true;
			download.setOnClickListener(PlayerFragment.this);
			download.setIndeterminateProgressMode(false);
			download.setProgress(0);
		}

		@Override
		public void onPostExecute(String params) {
			if (FAILURE.equals(params)) {
				((MainActivity) getActivity()).showMessage(R.string.download_failed);
				download.setProgress(0);
				download.setOnClickListener(PlayerFragment.this);
				setDownloadButtonState(true);
			} else {
				download.setProgress(canceled ? 0 : 100);
			}
		}

		@Override
		public void onPreExecute() {
			canceled = false;
			((RippleView) download.getParent()).setEnabled(false);
			download.setClickable(false);
			download.setOnClickListener(null);
			download.setIndeterminateProgressMode(true);
			download.setProgress(50);
		}

	};
	
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
		setImageButton();
		showLyrics();
		setElementsView(player.getCurrentPosition());
		boolean prepared = player.isPrepared();
		setClickablePlayerElement(prepared);
		changePlayPauseView(prepared && player.isPlaying());
		playerProgress.setMax(prepared ? (int)song.getDuration() : 100);
		contentView.findViewById(R.id.controlPane).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		scrollView.recalculateCover(R.id.controlPane, R.id.visualizer);
		playerProgress.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				playerProgress.setIndeterminate(!player.isPrepared());
			}
		}, 1000);
		return contentView;
	}
	
	private void init() {
		scrollView = (NotifyingScrollView)contentView.findViewById(R.id.scroll_view);
		scrollView.setImageResource(R.id.scrollable_cover);
		play = (PlayPauseView) contentView.findViewById(R.id.playpause);
		previous = (ImageView) contentView.findViewById(R.id.prev);
		forward = (ImageView) contentView.findViewById(R.id.next);
		shuffle = (ImageView) contentView.findViewById(R.id.shuffle);
		repeat = (ImageView) contentView.findViewById(R.id.repeat);
		download = (CircularProgressButton) contentView.findViewById(R.id.download);
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
		cbUseCover.setOnCheckedChangeListener(this);
		cbShowVisualizer.setOnCheckedChangeListener(this);
		playerProgress.setOnProgressChangeListener(new OnProgressChangeListener() {
			
			@Override
			public void onStopTrackingTouch(DiscreteSeekBar seekBar) { }
			
			@Override
			public void onStartTrackingTouch(DiscreteSeekBar seekBar) { }
			
			@Override
			public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
				if (fromUser) {
					try {
						player.seekTo(value);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		scrollView.setOnScrollChangedListener(new OnScrollChangedListener() {
			
			@Override
			public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt, int alpha) {
				((MainActivity)getActivity()).setToolbarAlpha(alpha);
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
		case R.id.shuffle: 
			Drawable drawableShuffle = shuffle.getDrawable();
			drawableShuffle.setAlpha(player.offOnShuffle() ? 255 : 125);
			shuffle.setImageDrawable(drawableShuffle);
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
	public void onResume() {
		((MainActivity) getActivity()).setCurrentFragmentId(ManagerFragmentId.playerFragment());
		((MainActivity) getActivity()).setDraverEnabled(false);
		((MainActivity) getActivity()).setTitle(R.string.tab_now_plaing);
		((MainActivity) getActivity()).invalidateOptionsMenu();
		((MainActivity) getActivity()).setToolbarOverlay(true);
		((MainActivity) getActivity()).setToolbarAlpha(scrollView.getToolbarAlpha());
		((MainActivity) getActivity()).showToolbarShadow(false);
		SharedPreferences sp = NewMaterialApp.getSharedPreferences();
		boolean stateVisualizer = sp.getBoolean(PREF_VISUALIZER, false);
		cbShowVisualizer.setChecked(stateVisualizer);
		if (!stateVisualizer) {
			setupVisualizerFxAndUI(stateVisualizer);
		}
		if (StateKeeper.getInstance().checkSongInfo(song.getComment()).getStatus() == SongInfo.DOWNLOADED) {
			((RippleView) download.getParent()).setVisibility(View.GONE);
		} else {
			((RippleView) download.getParent()).setVisibility(View.VISIBLE);
		}
		showLyrics();
		cancelProgressTask();
		thatSongIsDownloaded();
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
		if (null != lyricsFetcher) {
			lyricsFetcher.cancel();
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

					public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
					}

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
		public void pause(AbstractSong song) {
			if (isDestroy) return;
			changePlayPauseView(false);
		}

		@Override
		public void play(AbstractSong song) {
			if (isDestroy) return;
			changePlayPauseView(true);
		}

		@Override
		public void stop(AbstractSong s) {
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
		public void start(AbstractSong s) {
			song = s;
			if (isDestroy) return;
			((MainActivity) getActivity()).showPlayerElement(true);
			if (visualizerIsBroken) setupVisualizerFxAndUI(cbShowVisualizer.isChecked());
			setDownloadButtonState(true);
			setClickablePlayerElement(true);
			play.toggle(true);
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
					playerProgress.setIndeterminate(isOverBuffer);
				}
			});
		}

		@Override
		public void onBufferingUpdate(double percent) {
			PlayerFragment.this.percent = percent;
		}
	};
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		closeEditViews();
		Util.hideKeyboard(getActivity(), buttonView);
		if (buttonView.getId() == R.id.cbShowEqualizer) {
			Editor editor = NewMaterialApp.getSharedPreferences().edit();
			editor.putBoolean(PREF_VISUALIZER, isChecked);
			editor.commit();
			setupVisualizerFxAndUI(isChecked);
			return;
		}
		if (!buttonView.isEnabled()) {
			return;
		}
		isUseAlbumCover = isChecked;
		cbUseCover.setChecked(isChecked);
		if (song.getClass() != MusicData.class) {
			return;
		}
		if (isChecked) {
			undoMessage = MSG_UNDO_NOTHING_DO;
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
	private void play(int delta) throws IllegalArgumentException {
		if (delta == 0) {
			player.play(song);
			return;
		}
		if (!isUseAlbumCover && ((View) cbUseCover.getParent()).getVisibility() == View.VISIBLE) {
			undoMessage = MSG_UNDO_REMOVE;
			undo.clear();
		}
		player.stop();
		setClickablePlayerElement(false);
		player.shift(delta);
		setDownloadButtonState(!player.isGettingURl());
		playerProgress.setProgress(0);
		playerProgress.setSecondaryProgress(0);
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
			((RippleView) download.getParent()).setVisibility(View.GONE);
		} else {
			((RippleView) download.getParent()).setVisibility(View.VISIBLE);
		}
		cancelProgressTask();
		thatSongIsDownloaded();
	}

	private void cancelProgressTask() {
		if (null != progressUpdater) {
			progressUpdater.cancel(true);
		}
	}

	private void setElementsView(int progress) {
		download.setVisibility(song.getClass() == MusicData.class ? View.GONE : View.VISIBLE);
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		playerTotalTime.setTime(Util.getFormatedStrDuration(song.getDuration()));
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
		if (player.enabledShuffle()) {
			drawableShuffle.setAlpha(255);
			shuffle.setImageDrawable(drawableShuffle);
		} else {
			drawableShuffle.setAlpha(125);
			shuffle.setImageDrawable(drawableShuffle);
		}
	}

	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		if (!isClickable) {
			playerCurrTime.setTime("0:00");
		}
	}

	/**
	 * @param isPlaying
	 *            - If "true", the button changes the picture to "play", if
	 *            "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (play.isPlay() != isPlaying) {
			return;
		}
		play.toggle(isPlaying);
	}

	private void setDownloadButtonState(boolean state) {
		download.setClickable(state);
		download.setEnabled(state);
		((RippleView) download.getParent()).setEnabled(state);
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
			boolean isSameWord = false;
			Util.hideKeyboard(getActivity(), contentView);
			if (etTitle.getVisibility() == View.VISIBLE && !song.getTitle().equals(etTitle.getText().toString())) {
				String title = Util.removeSpecialCharacters(etTitle.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : etTitle.getText().toString());
				song.setTitle(title);
				tvTitle.setText(title);
			} else if (etArtist.getVisibility() == View.VISIBLE && !song.getArtist().equals(etArtist.getText().toString())) {
				String artist = Util.removeSpecialCharacters(etArtist.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : etArtist.getText().toString());
				song.setArtist(artist);
				tvArtist.setText(artist);
			} else {
				isSameWord = true;
			}
			contentView.findViewById(R.id.artistNameBox).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.songNameBox).setVisibility(View.VISIBLE);
			etArtist.setVisibility(View.GONE);
			etTitle.setVisibility(View.GONE);
			if (song.getClass() != MusicData.class) {
				player.update(song.getTitle(), song.getArtist(), null);
			} else if (!isSameWord){
				saveTags();
			}
		}
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

	private void showLyrics() {
		if (lastArtist.equals(song.getArtist()) && lastTitle.equals(song.getTitle())
				&& !playerLyricsView.getText().toString().isEmpty()) return;
		contentView.findViewById(R.id.lyrics_progress).setVisibility(View.VISIBLE);
		playerLyricsView.setVisibility(View.GONE);
		lastArtist = song.getArtist();
		lastTitle = song.getTitle();
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

	private void getCover(final AbstractSong s) {
		setCheckBoxState(false);
		if (s.isHasCover()) {
			Bitmap bitmap = s.getCover(getActivity());
			if (null != bitmap) {
				setCoverToZoomView(bitmap);
				setCheckBoxState(true);
				((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
				return;
			}
		}
		((View) cbUseCover.getParent()).setVisibility(View.INVISIBLE);
		if (s.getClass() != MusicData.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {

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
			scrollView.setImageBitmap(bitmap);
		} else {
			visualizerView.setUpVizualizerColor(-1, -1);
			scrollView.setImageBitmap(R.drawable.big_album);
		}
	}
	
	private void setCheckBoxState(boolean state) {
		if (isAdded()) {
			isUseAlbumCover = state;
			cbUseCover.setOnCheckedChangeListener(null);
			cbUseCover.setChecked(state);
			cbUseCover.setClickable(state);
			cbUseCover.setEnabled(state);
			cbUseCover.setOnCheckedChangeListener(this);
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
							progressUpdater = new ProgressUpdaterTask(progressListener, getActivity());
							progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,downloadListener.getDownloadId());
						}
					}
				});
			}

			@Override
			public void error(String error) {
				((MainActivity) getActivity()).runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						((MainActivity) getActivity()).showMessage(R.string.download_failed);
						download.setProgress(0);
						download.setOnClickListener(PlayerFragment.this);
						setDownloadButtonState(true);
					}
				});
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
							progressUpdater = new ProgressUpdaterTask(progressListener, getActivity());
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
	public void onConfigurationChanged(Configuration newConfig) {
		scrollView.recalculateCover(R.id.controlPane, R.id.visualizer);
		super.onConfigurationChanged(newConfig);
	}
	
}