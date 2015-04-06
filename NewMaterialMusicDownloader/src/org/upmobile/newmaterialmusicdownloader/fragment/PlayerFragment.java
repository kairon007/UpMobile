package org.upmobile.newmaterialmusicdownloader.fragment;

import java.io.File;

import org.upmobile.newmaterialmusicdownloader.Constants;
import org.upmobile.newmaterialmusicdownloader.DownloadListener;
import org.upmobile.newmaterialmusicdownloader.ManagerFragmentId;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

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
import ru.johnlife.lifetoolsmp3.ui.widget.NotifyingScrollView;
import ru.johnlife.lifetoolsmp3.ui.widget.NotifyingScrollView.OnScrollChangedListener;
import ru.johnlife.lifetoolsmp3.ui.widget.PlayPauseView;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.AdvancedUndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import ru.johnlife.lifetoolsmp3.ui.widget.digitalclock.DigitalClockView;
import ru.johnlife.lifetoolsmp3.ui.widget.digitalclock.font.DFont;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.DiscreteSeekBar;
import ru.johnlife.lifetoolsmp3.ui.widget.dsb.DiscreteSeekBar.OnProgressChangeListener;
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
import android.util.Log;
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

import com.csform.android.uiapptemplate.view.cb.CheckBox;
import com.csform.android.uiapptemplate.view.cpb.CircularProgressButton;
import com.csform.android.uiapptemplate.view.spb.SmoothProgressBar;

public class PlayerFragment extends Fragment implements Constants, OnClickListener , OnCheckedChangeListener, OnEditorActionListener{

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
	private UndoBar undo;
	private LinearLayout artistBox;
	private LinearLayout titleBox;

	// lyric sections
	private LyricsFetcher lyricsFetcher;
	private TextView playerLyricsView;
	
	// visualizer sectoin
	private SimpleVisualizerView visualizerView;
	private Visualizer mVisualizer;
	private CheckBox cbShowVisualizer;
	
	//custom check box
	private CheckBox cbUseCover;
	private boolean isUseAlbumCover = false;

	// playback sections
	private PlayPauseView play;
	private ImageView previous;
	private ImageView forward;
	private ImageView shuffle;
	private ImageView repeat;
	private ImageView stop;

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private DigitalClockView playerCurrTime;
	private DigitalClockView playerTotalTime;
	private DiscreteSeekBar playerProgress;
	private SmoothProgressBar wait;

	private int checkIdCover;
	private int checkIdLyrics;
	
	private String lastArtist = "";
	private String lastTitle = ""; 
	
	private int primaryColor;

	private boolean isDestroy;
	
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
		contentView.findViewById(R.id.controlPane).measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		scrollView.recalculateCover(R.id.controlPane, R.id.visualizer);
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
		stop = (ImageView) contentView.findViewById(R.id.stop);
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
		wait = (SmoothProgressBar) contentView.findViewById(R.id.player_wait_song);
		visualizerView = (SimpleVisualizerView) contentView.findViewById(R.id.visualizer);
		cbShowVisualizer = (CheckBox) contentView.findViewById(R.id.cbShowEqualizer);
		DFont font = new DFont(Util.dpToPx(getActivity(), 12), 2);
		font.setColor(getResources().getColor(Util.getResIdFromAttribute(getActivity(), R.attr.colorTextSecondary)));
		playerCurrTime.setFont(font);
		playerTotalTime.setFont(font); 
		playerProgress.setUseAnimateTextView(true);
		undo = new UndoBar(getActivity());
		stop.setColorFilter(primaryColor);
		previous.setColorFilter(primaryColor);
		forward.setColorFilter(primaryColor);
		shuffle.setColorFilter(primaryColor);
		repeat.setColorFilter(primaryColor);
	}

	private void setListeners() {
		play.setOnClickListener(this);
		stop.setOnClickListener(this);
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
			if (!player.offOnRepeat()) {
				drawableRepeat.setAlpha(125);
				repeat.setImageDrawable(drawableRepeat);
			} else {
				drawableRepeat.setAlpha(255);
				repeat.setImageDrawable(drawableRepeat);
			}
			break;
		case R.id.shuffle: 
			Drawable drawableShuffle = shuffle.getDrawable();
			if (player.offOnShuffle()) {
				drawableShuffle.setAlpha(255);
				shuffle.setImageDrawable(drawableShuffle);
			} else {
				drawableShuffle.setAlpha(125);
				shuffle.setImageDrawable(drawableShuffle);
			}
			break;
		case R.id.stop:
			player.stopPressed();
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
		thatSongIsDownloaded();
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
		showLyrics();
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
			undo.clear();
			clearCover();
		}
		if(null != mVisualizer) {
			mVisualizer.setEnabled(false);
			mVisualizer.release();
			mVisualizer = null;
		}
		super.onPause();
	}
	
	private void setupVisualizerFxAndUI(boolean isShowVisualizer) {
		visualizerView.setVisibility(isShowVisualizer ? View.VISIBLE : View.INVISIBLE);
		if (null == mVisualizer) {
			try {
				mVisualizer = new Visualizer(player.getAudioSessionId());
				mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
				mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

					public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
					}

					public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
						visualizerView.updateVisualizer(bytes);
					}
				}, Visualizer.getMaxCaptureRate() / 2, false, true);
				mVisualizer.setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mVisualizer.setEnabled(isShowVisualizer);
	}
	
	private Runnable progressAction = new Runnable() {

		@Override
		public void run() {
			try {
				playerProgress.removeCallbacks(this);
				if (player.isPrepared()) {
					int current = player.getCurrentPosition();
					playerProgress.setProgress(current);
					playerCurrTime.setTime(Util.getFormatedStrDuration(current));
				}
				playerProgress.postDelayed(this, 1000);
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e + "");
			}
		}
	};

	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void pause(AbstractSong song) {
			if (isDestroy) {
				return;
			}
			changePlayPauseView(false);
		}

		@Override
		public void play(AbstractSong song) {
			if (isDestroy) {
				return;
			}
			changePlayPauseView(true);
		}

		@Override
		public void stop(AbstractSong s) {
			if (isDestroy) {
				return;
			}
			changePlayPauseView(false);
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
			if (isDestroy) {
				return;
			}
			((MainActivity) getActivity()).showPlayerElement(true);
			setDownloadButtonState(true);
			setClickablePlayerElement(true);
			play.toggle(true);
			setElementsView(0);
			cancelProgressTask();
			thatSongIsDownloaded();
		}

		@Override
		public void update(AbstractSong current) {
			if (isDestroy) {
				return;
			}
			cancelProgressTask();
			song = current;
			getCover(song);
			showLyrics();
			setElementsView(0);
			playerProgress.setVisibility(View.GONE);
			wait.setVisibility(View.VISIBLE);
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
		getCover(song);
		player.stop();
		setClickablePlayerElement(false);
		player.shift(delta);
		setDownloadButtonState(!player.isGettingURl());
		if (!player.enabledRepeat()) {
			setCheckBoxState(false);
			cancelProgressTask();
			download.setProgress(0);
			download.setOnClickListener(this);
		}
	}

	private void cancelProgressTask() {
		if (null != progressUpdater) {
			progressUpdater.cancel(true);
		}
	}

	private void setElementsView(int progress) {
		settingPlayerProgress();
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		} else {
			download.setVisibility(View.VISIBLE);
		}
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		playerTotalTime.setTime(Util.getFormatedStrDuration(song.getDuration()));
		playerCurrTime.setTime(Util.getFormatedStrDuration(progress));
	}

	private void settingPlayerProgress() {
		if (player.isPrepared()) {
			playerProgress.setMax((int) song.getDuration());
			playerProgress.setProgress(player.getCurrentPosition());
			secondSetting(true);
			playerProgress.post(progressAction);
		} else {
			playerProgress.setProgress(0);
			secondSetting(false);
		}
	}

	private void secondSetting(boolean b) {
		playerProgress.setEnabled(b);
		playerProgress.setClickable(b);
		if (b || player.isStoped()) {
			playerProgress.setVisibility(View.VISIBLE);
			wait.setVisibility(View.GONE);
		} else {
			playerProgress.setVisibility(View.GONE);
			wait.setVisibility(View.VISIBLE);
		}
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
		stop.setClickable(isClickable);
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
			hideKeyboard();
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
		setCoverToZoomView(null);
		((View)cbUseCover.getParent()).setVisibility(View.GONE);
		if (s.getClass() != MusicData.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover) {
						return;
					}
					if (null != bmp) {
						((View)cbUseCover.getParent()).setVisibility(View.VISIBLE);
						((RemoteSong) s).setHasCover(true);
						setCoverToZoomView(bmp);
						player.updatePictureNotification(bmp);
						setCheckBoxState(true);
					} 
				}
			};
			checkIdCover = readyListener.hashCode();
			((RemoteSong) s).getCover(readyListener);
		} else {
			Bitmap bitmap = ((MusicData) s).getCover(getActivity());
			if (bitmap != null) {
				((View)cbUseCover.getParent()).setVisibility(View.VISIBLE);
				setCoverToZoomView(bitmap);
				setCheckBoxState(true);
			} 
		}
	}

	private void clearCover() {
		setCheckBoxState(false);
		if (MusicData.class == song.getClass()) {
			((View)cbUseCover.getParent()).setVisibility(View.GONE);
			setCoverToZoomView(null);
			((MusicData) song).clearCover();
			RenameTask.deleteCoverFromFile(new File(song.getPath()));
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

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(contentView.getWindowToken(), 0);
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
						if (downloadListener.getSongID() == -1) {
							progressUpdater = new ProgressUpdater();
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
		player.getPlayingSong().getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
				if (isDestroy) return;
				DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
				Cursor running = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
				if (running != null) {
					while (running.moveToNext()) {
						if (null != url && null != downloadListener && url.equals(running.getString(running.getColumnIndex(DownloadManager.COLUMN_URI)))) {
							progressUpdater = new ProgressUpdater();
							progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,downloadListener.getDownloadId());
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
	
	private class ProgressUpdater extends AsyncTask<Long, Integer, String> {

		private static final String FAILURE = "failure";

		@Override
		protected void onPreExecute() {
			download.setClickable(false);
			download.setOnClickListener(null);
			download.setIndeterminateProgressMode(true);
			download.setProgress(50);
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Long... params) {
			if (isDestroy) return null;
			DownloadManager manager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
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
							return FAILURE;
						case DownloadManager.ERROR_CANNOT_RESUME:
							return FAILURE;
						case DownloadManager.ERROR_FILE_ERROR:
							return FAILURE;
						case DownloadManager.ERROR_HTTP_DATA_ERROR:
							return FAILURE;
						case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
							return FAILURE;
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
							} else {
								progress = 100;
								publishProgress(100);
							}
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
			this.cancel(true);
			super.onCancelled();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (isCancelled()) return;
			int progress = values[0];
			download.setIndeterminateProgressMode(false);
			download.setProgress(progress > 0 ? progress : 1);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		scrollView.recalculateCover(R.id.controlPane, R.id.visualizer);
		super.onConfigurationChanged(newConfig);
	}

}