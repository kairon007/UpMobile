package org.upmobile.clearmusicdownloader.fragment;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.io.File;
import java.text.MessageFormat;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.DownloadListener;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.AdvancedUndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.special.utils.UIParallaxScroll;

public class PlayerFragment  extends Fragment implements OnClickListener, OnSeekBarChangeListener, OnCheckedChangeListener, PlaybackService.OnErrorListener {

	private final int MESSAGE_DURATION = 5000;
    public static final int DURATION = 500; // in ms
    private String PACKAGE = "IDENTIFY";
	private AbstractSong song;
	private RenameTask renameTask;
	private PlaybackService player;
	private DownloadListener downloadListener;
	private LyricsFetcher lyricsFetcher;
	private View parentView;
	private SeekBar playerProgress;
	
	private UndoBar undo;
	
	private FrameLayout playerTitleBar;
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private CheckBox useCover;
	private FrameLayout playerBtnTitle;
	private FrameLayout playerBtnArtist;
	private EditText playerEtTitle;
	private EditText playerEtArtist;
	private TextView playerTvTitle;
	private TextView playerTvArtist;
	private ImageView playerCover;
	private ImageView lyricsLoader;
	private Button download;
	private Button playerTitleBarBack;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TextView playerLyricsView;
	private TextView playerTitleBarTitle;
	private TextView playerTitleBarArtis;
	private String title;
	private String artist;
    private int delta_top;
    private int delta_left;
	private int top;
	private int left;
	private int width;
	private int height;
	private int deltaLeftTitleBar;
	private int deltaTopTitleBar;
	private int currLyricsFetchedId;
	private int checkIdCover;
	private float maxTranslationX;
	private float maxTranslationY;
	private float deltaScale = 0;
	private float scaleWidthTitleBar;
	private float scaleHeightTitleBar;
    private float scale_width;
    private float scale_height;
	private float ratio;
    private boolean isDestroy;
    private boolean isUseAlbumCover = true;
    private boolean isNeedCalculateCover = true;
    
    private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void start(AbstractSong s) {
			downloadButtonState(true);
			if (s.getClass() != MusicData.class) {
				song = ((RemoteSong) s).cloneSong();
			} else {
				song = s;
			}
			if (isDestroy) return;
			setClickablePlayerElement(true);
			showLyrics();
			setElementsView(0);
			playerProgress.post(progressAction);
		}

		@Override
		public void play(AbstractSong s) {
			if (isDestroy) return;
			changePlayPauseView(false);
		}

		@Override
		public void pause(AbstractSong s) {
			if (isDestroy) return;
			changePlayPauseView(true);
		}

		@Override
		public void stop(AbstractSong s) {
			playerProgress.removeCallbacks(progressAction);
			playerProgress.setProgress(0);
			setClickablePlayerElement(player.isPrepared());
		}
		
		@Override
		public void stopPressed() {};
		
		@Override
		public void error() {
			if (isDestroy) return;
			Toast.makeText(getActivity(), ru.johnlife.lifetoolsmp3.R.string.file_is_bad, Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void update(AbstractSong s) {
			if (isDestroy) return;
			song = s;
			setElementsView(0);
			setClickablePlayerElement(false);
		}

		@Override
		public void onTrackTimeChanged(int time, boolean isOverBuffer) {}

		@Override
		public void onBufferingUpdate(double percent) {}
	};
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		parentView = inflater.inflate(R.layout.player, container, false);
		((MainActivity) getActivity()).hideTopFrame();
		((MainActivity) getActivity()).showPlayerElement();
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setOnScrollChangedListener(mOnScrollChangedListener);
		init();
		setListener();
		player = PlaybackService.get(getActivity());
		player.addStatePlayerListener(stateListener);
		player.setOnErrorListener(this);
		downloadButtonState(!player.isGettingURl());
		playerTitleBar.getBackground().setAlpha(0);
		playerTitleBarArtis.setVisibility(View.INVISIBLE);
		playerTitleBarTitle.setVisibility(View.INVISIBLE);
		playerCover.bringToFront();	
		((MainActivity) getActivity()).getResideMenu().addIgnoredView(playerProgress);
		((MainActivity) getActivity()).getResideMenu().addIgnoredView(playerEtTitle);
		((MainActivity) getActivity()).getResideMenu().addIgnoredView(playerEtArtist);
		isUseAlbumCover = ((MainActivity) getActivity()).stateCoverHelper();
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			song = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			top = getArguments().getInt(PACKAGE + ".top");
			left = getArguments().getInt(PACKAGE + ".left");
			width = getArguments().getInt(PACKAGE + ".width");
			height = getArguments().getInt(PACKAGE + ".height");
			if (song.equals(player.getPlayingSong()) && player.isPlaying()) {
				player.play();
			} else if (!song.equals(player.getPlayingSong())){
				play(0);
			}
		} else {
			song = player.getPlayingSong();
			parentView.findViewById(R.id.title_bar_left_menu).setBackgroundDrawable(getResources().getDrawable(R.drawable.titlebar_menu_selector));
		}
		boolean prepared = player.isPrepared();
		setClickablePlayerElement(prepared);
		if (prepared) {
			changePlayPauseView(!player.isPlaying());
		} else {
			changePlayPauseView(false);
		}
		downloadButtonState(!player.isGettingURl());
		getCover(song);
		showLyrics();
		startImageAnimation(playerCover);
		setElementsView(player.getCurrentPosition());
		return parentView;
	}
	
	private void coverTitleBarLocation() {
		maxTranslationX = Util.dpToPx(getActivity(), 48) - playerCover.getLeft();
		maxTranslationY = 0 - playerCover.getTop() + Util.dpToPx(getActivity(), 4) - playerCover.getTranslationY();
		deltaScale = 1 - (float) Util.dpToPx(getActivity(), 48) / (float) playerCover.getMeasuredHeight();
	}
	
	@Override
	public void onDestroyView() {
		player.removeStatePlayerListener(stateListener);
		super.onDestroyView();
	}
	

	@Override
	public void onResume() {
		super.onResume();
		((MainActivity) getActivity()).showMiniPlayer(false);
		setKeyListener();
	}

	private void setKeyListener() {
		getView().setFocusableInTouchMode(true);
		getView().requestFocus();
		getView().setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					if (!closeEditViews()) {
						onBackPress();
					}
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
	
	@Override
	public void onPause() {
		if (null != lyricsFetcher) {
			lyricsFetcher.cancel();
		}
		if (!isUseAlbumCover && song.isHasCover()) {
			undo.clear();
			clearCover();
		}
		super.onPause();
	}

	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		playerProgress.setEnabled(isClickable);
		if (isClickable) {
			play.setImageResource(R.drawable.pause);
			play.setVisibility(View.VISIBLE);
			parentView.findViewById(R.id.player_play_progress).clearAnimation();
			parentView.findViewById(R.id.player_play_progress).setVisibility(View.GONE);
		} else {
			playerCurrTime.setText("0:00");
			playerProgress.setProgress(0);
			play.setVisibility(View.GONE);
			parentView.findViewById(R.id.player_play_progress).setVisibility(View.VISIBLE);
			parentView.findViewById(R.id.player_play_progress).startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
		}
	}
	
	/**
	 * @param isPlaying - If "true", the button changes the picture to "play", if "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.play);
		} else {
			play.setImageResource(R.drawable.pause);
		}
	}
	
	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.player_play);
		previous = (ImageButton) parentView.findViewById(R.id.player_previous);
		forward = (ImageButton) parentView.findViewById(R.id.player_forward);
		download = (Button) parentView.findViewById(R.id.player_download);
		lyricsLoader= (ImageView) parentView.findViewById(R.id.lyrics_load_image);
		playerTitleBarBack = (Button) parentView.findViewById(R.id.title_bar_left_menu);
		playerProgress = (SeekBar) parentView.findViewById(R.id.player_progress);
		playerTvTitle = (TextView) parentView.findViewById(R.id.player_title);
		playerEtTitle = (EditText) parentView.findViewById(R.id.player_title_edit_view);
		playerBtnTitle = (FrameLayout) parentView.findViewById(R.id.player_edit_title);
		playerTvArtist = (TextView) parentView.findViewById(R.id.player_artist);
		playerEtArtist = (EditText) parentView.findViewById(R.id.player_artist_edit_view);
		playerBtnArtist = (FrameLayout) parentView.findViewById(R.id.player_edit_artist);
		playerCurrTime = (TextView) parentView.findViewById(R.id.player_current_time);
		playerTotalTime = (TextView) parentView.findViewById(R.id.player_total_time);
	    playerTitleBar = (FrameLayout) parentView.findViewById(R.id.layout_top);   
	    playerTitleBarArtis = (TextView) parentView.findViewById(R.id.titleBarArtist);
	    playerTitleBarTitle = (TextView) parentView.findViewById(R.id.titleBarTitle);
		playerLyricsView = (TextView) parentView.findViewById(R.id.player_lyrics_view);
		playerCover = (ImageView) parentView.findViewById(R.id.player_cover);
		useCover = (CheckBox) parentView.findViewById(R.id.use_cover);
		undo = new UndoBar(getActivity());
	}

	private void setListener() {
		playerProgress.setOnSeekBarChangeListener(this);
		play.setOnClickListener(this);
		previous.setOnClickListener(this);
		forward.setOnClickListener(this);
		playerBtnArtist.setOnClickListener(this);
		playerBtnTitle.setOnClickListener(this);
		download.setOnClickListener(this);
		playerTitleBarBack.setOnClickListener(this);
		useCover.setOnCheckedChangeListener(this);
		playerEtArtist.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					closeEditViews();
					return true;
				}
				return false;
			}
		});
		playerEtTitle.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
					closeEditViews();
					return true;
				}
				return false;
			}
		});
		((UIParallaxScroll) parentView.findViewById(R.id.scroller)).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				editTag();
				return false;
			}
		});
		parentView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			
			@Override
			public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, 
					int oldTop, int oldRight, int oldBottom) {
				if (isNeedCalculateCover) {
					moveCover(ratio);
				}
			}
		});
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((MainActivity) getActivity()).setCoverHelper(isChecked);
		isUseAlbumCover = isChecked;
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
					playerCover.setImageBitmap(song.getCover(getActivity()));
					isUseAlbumCover = true;
					setCheckBoxState(true);
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
	
	private boolean closeEditViews() {
		hideKeyboard();
		if (playerEtArtist.getVisibility() == View.VISIBLE || playerEtTitle.getVisibility() == View.VISIBLE) {
			playerTvArtist.setVisibility(View.VISIBLE);
			playerTvTitle.setVisibility(View.VISIBLE);
			playerBtnArtist.setVisibility(View.VISIBLE);
			playerBtnTitle.setVisibility(View.VISIBLE);
			playerEtArtist.setVisibility(View.GONE);
			playerEtTitle.setVisibility(View.GONE);
			return true;
		} 
		return false;
	}
	
	private void clearCover() {
		setCheckBoxState(false);
		if (MusicData.class == song.getClass()) {
			playerCover.setImageResource(R.drawable.def_cover_circle_web);
			((MusicData) song).clearCover();
			RenameTask.deleteCoverFromFile(new File(song.getPath()));
		}
	}
	
	private void hideKeyboard() {
		View hideView = parentView.findViewById(R.id.scroller);
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(hideView.getWindowToken(), 0);
	}
	
	private void setElementsView(int progress) {
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		}
		playerProgress.removeCallbacks(progressAction);
		playerTvArtist.setText(song.getArtist());
		playerTvTitle.setText(song.getTitle());
		playerTitleBarArtis.setText(song.getArtist().trim());
		playerTitleBarTitle.setText(song.getTitle().trim());
		long totalTime = song.getDuration();
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		playerProgress.setMax((int) totalTime);
		if (progress > 0) {
			playerProgress.setProgress(progress);
			playerCurrTime.setText(Util.getFormatedStrDuration(progress));
			playerProgress.post(progressAction);
		}
	}
		
	private void updateObject() {
		playerTvArtist.setText(song.getArtist());
		playerTvTitle.setText(song.getTitle());
		playerTitleBarArtis.setText(song.getArtist());
		playerTitleBarTitle.setText(song.getTitle());
		if (song.getClass() != MusicData.class) {
			((RemoteSong) song).setHasCover(true);
			player.update(song.getTitle(), song.getArtist(), null);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.title_bar_left_menu) {
			if (!closeEditViews()) {
				if (null != getArguments()) {
					onBackPress();
				} else {
					((MainActivity) getActivity()).openMenu();
				}
			}
			return;
		}
		editTag();
		switch (v.getId()) {
		case R.id.player_play:
			play(0);
			break;
		case R.id.player_previous:
			play(-1);
			break;
		case R.id.player_forward:
			play(1);
			break;
		case R.id.player_download:
			download();
			break;
		case R.id.player_edit_title:
			openTitleField();
			break;
		case R.id.player_edit_artist:
			openArtistField();
			break;
		default:
			break;
		}
	}

	private void openArtistField() {
		if (playerTvArtist.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			playerTvArtist.setVisibility(View.GONE);
			playerEtArtist.setVisibility(View.VISIBLE);
			playerEtArtist.requestFocus();
			playerEtArtist.setText(song.getArtist());
			int size = song.getArtist().length();
			playerEtArtist.setSelection(size);
			playerBtnArtist.setVisibility(View.GONE);
		} else {
			playerTvArtist.setVisibility(View.VISIBLE);
			playerEtArtist.setVisibility(View.GONE);
		}
	}

	private void openTitleField() {
		if (playerTvTitle.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			playerTvTitle.setVisibility(View.GONE);
			playerEtTitle.setVisibility(View.VISIBLE);
			playerEtTitle.requestFocus();
			playerEtTitle.setText(song.getTitle());
			int size = song.getTitle().length();
			playerEtTitle.setSelection(size);
			playerBtnTitle.setVisibility(View.GONE);
		} else {
			playerTvTitle.setVisibility(View.VISIBLE);
			playerEtTitle.setVisibility(View.GONE);
		}
	}
	
	private void editTag() {
        if (playerEtArtist.getVisibility() == View.VISIBLE) {
        	playerTvArtist.setVisibility(View.VISIBLE);
			playerEtArtist.setVisibility(View.GONE);
			playerBtnArtist.setVisibility(View.VISIBLE);
			hideKeyboard();
			artist = Util.removeSpecialCharacters(playerEtArtist.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : playerEtArtist.getText().toString());
			if (!artist.equals(song.getArtist())){
				if (checkUnique(song.getTitle(),artist)) {
					Toast toast = Toast.makeText(getActivity(), R.string.file_already_exists, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				song.setArtist(artist);
				saveTag();
			}
        } else if (playerEtTitle.getVisibility() == View.VISIBLE) {
        	playerTvTitle.setVisibility(View.VISIBLE);
			playerEtTitle.setVisibility(View.GONE);
			playerBtnTitle.setVisibility(View.VISIBLE);
			hideKeyboard();
			title = Util.removeSpecialCharacters(playerEtTitle.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : playerEtTitle.getText().toString());
			if (!title.equals(song.getTitle())){ 
				if (checkUnique(title, song.getArtist())) {
					Toast toast = Toast.makeText(getActivity(), R.string.file_already_exists, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				song.setTitle(title);
				saveTag();
			}
        }
	}
	
	private boolean checkUnique(String title, String artist) {
		if (song.getClass() == MusicData.class) {
			return new File(MessageFormat.format("{0}/{1} - {2}.mp3", new File(song.getPath()).getParentFile(), artist, title)).exists();
		}
		return false;
	}
	
	private void saveTag() {
		updateObject();
		if (song.getClass() != MusicData.class) return;
		RenameTaskSuccessListener renameListener = new RenameTaskSuccessListener() {

			@Override
			public void success(String path) {
				song.setPath(path);
				renameTask.cancelProgress();
				player.update(song.getTitle(), song.getArtist(), song.getPath());
			}

			@Override
			public void error() {
			}
		};
		renameTask = new RenameTask(new File(song.getPath()), getActivity(), renameListener, song.getArtist(), song.getTitle(), song.getAlbum());
		renameTask.start(true, false);
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
			try {
				player.seekTo(progress);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		}
		parentView.findViewById(R.id.player_lyrics_frame).setVisibility(View.VISIBLE);
		playerLyricsView.setText("");
		lyricsLoader.setVisibility(View.VISIBLE);
		lyricsLoader.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
		lyricsFetcher = new LyricsFetcher(getActivity());
		lyricsFetcher.fetchLyrics(song.getTitle(), song.getArtist());
		OnLyricsFetchedListener fetchedListener = new OnLyricsFetchedListener() {
			
			@Override
			public void onLyricsFetched(boolean foundLyrics, String lyrics) {
				if (hashCode() != currLyricsFetchedId) return;
				lyricsLoader.clearAnimation();
				lyricsLoader.setVisibility(View.GONE);
				try {
					if (foundLyrics) {
						playerLyricsView.setVisibility(View.VISIBLE);
						playerLyricsView.setText(Html.fromHtml(lyrics));
					} else {
						playerLyricsView.setVisibility(View.VISIBLE);
						playerLyricsView.setText(String.format(getString(R.string.download_dialog_no_lyrics), song.getTitle() + " - " + song.getArtist()));
					}
				} catch (Exception e) {
				}
			}
			
		};
		currLyricsFetchedId = fetchedListener.hashCode();
		lyricsFetcher.setOnLyricsFetchedListener(fetchedListener);
	}
	
	/**
	 * @param delta
	 *  - 
	 * delta must be 1 or -1 or 0, 1 - next, -1 - previous, 0 - current song
	 */
	private void play(int delta) throws IllegalArgumentException {
		if (!isUseAlbumCover) {
			if (null != undo) {
				undo.clear();
			}
			clearCover();
		}
		switch (delta) {
		case 0:
			if (player.isPlaying()) {
				player.pause();
			} else {
				player.play(song);
			}
			break;
		case 1:
			player.shift(1);
			getCover(player.getPlayingSong());
			downloadButtonState(!player.isGettingURl());
			break;
		case -1:
			player.shift(-1);
			getCover(player.getPlayingSong());
			downloadButtonState(!player.isGettingURl());
			break;
		default:
			throw new IllegalArgumentException("Delta must be -1 < delta < 1");
		}
	}
	
	private void getCover(final AbstractSong song) {
		playerCover.setImageResource(R.drawable.def_cover_circle_web);
		setCheckBoxState(false);
		if (song.getClass() != MusicData.class) {
			OnBitmapReadyListener idBmpListener = new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {
					if(this.hashCode() != checkIdCover) return;
					if (null != bmp) {
						((RemoteSong) song).setHasCover(true);
						playerCover.setImageBitmap(bmp);
						setCheckBoxState(true);
					}
				}
			};
			checkIdCover = idBmpListener.hashCode();
			((RemoteSong) song).getCover(idBmpListener);
		} else {
			Bitmap bitmap = ((MusicData) song).getCover(getActivity());
			if (bitmap != null) {
				playerCover.setImageBitmap(bitmap);
				setCheckBoxState(true);
			}
		}
	}
	
	private void setCheckBoxState(boolean state) {
		useCover.setOnCheckedChangeListener(null);
		useCover.setEnabled(state);
		useCover.setChecked(state);
		useCover.setClickable(state);
		useCover.setOnCheckedChangeListener(this);
	}

	private void download() {
		((MainActivity)getActivity()).setCoverHelper(true);
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
				StateKeeper.getInstance().putSongInfo(url, StateKeeper.DOWNLOADING);
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
	
	private void startImageAnimation(ImageView view) {
		ViewTreeObserver observer = playerCover.getViewTreeObserver();
		observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				playerCover.getViewTreeObserver().removeOnPreDrawListener(this);
				int[] screen_location = new int[2];
				playerCover.getLocationOnScreen(screen_location);
				delta_left = left - screen_location[0];
				delta_top = top - screen_location[1];
				scale_width = (float) width / playerCover.getWidth();
				scale_height = (float) height / playerCover.getHeight();
				runEnterAnimation(playerCover, false);
				return true;
			}
		});
	}
	
	private void downloadButtonState(boolean state) {
		download.setClickable(state);
		download.setEnabled(state);
	}
	
	private void onBackPress() {
		runExitAnimation(new Runnable() {
			
			@Override
			public void run() {
				if (isDestroy) return;
				((MainActivity) getActivity()).onBackPressed();
				((MainActivity) getActivity()).overridePendingTransition(0, 0);
			}
		}, playerCover, false);
	}
	
    private void runEnterAnimation(ImageView view, boolean flag) {
        ViewHelper.setPivotX(view, 0.f);
        ViewHelper.setPivotY(view, 0.f);
        ViewHelper.setScaleX(view, flag ? scaleWidthTitleBar : scale_width);
        ViewHelper.setScaleY(view, flag ? scaleHeightTitleBar : scale_height);
        ViewHelper.setTranslationX(view,flag ? deltaLeftTitleBar : delta_left);
        ViewHelper.setTranslationY(view, flag ? deltaTopTitleBar : delta_top);
        animate(view).
                setDuration(DURATION).
                scaleX(1.f).
                scaleY(1.f).
                translationX(0.f).
                translationY(0.f).
                setInterpolator(new DecelerateInterpolator()).
        		setListener(new AnimatorListenerAdapter() {  

        			@Override
	        	    public void onAnimationEnd(Animator animation) {        	    
        			}
        		});

        ObjectAnimator bg_anim = ObjectAnimator.ofFloat(((RelativeLayout) parentView.findViewById(R.id.bg_layout)), "alpha", flag ? 1f : 0f, 1f);
        bg_anim.setDuration(DURATION);
        bg_anim.start();
    }
    
    private void runExitAnimation(final Runnable end_action, ImageView view, boolean flag) {
    	ViewHelper.setPivotX(view, 0.f);
    	ViewHelper.setPivotY(view, 0.f);
    	ViewHelper.setScaleX(view, 1.f);
    	ViewHelper.setScaleY(view, 1.f);
    	ViewHelper.setTranslationX(view, 0.f);
    	ViewHelper.setTranslationY(view, 0.f);

    	animate(view).
                setDuration(DURATION).
                scaleX(flag ? scaleWidthTitleBar : scale_width).
                scaleY(flag ? scaleHeightTitleBar : scale_height).
                translationX(flag ? deltaLeftTitleBar : delta_left).
                translationY(flag ? deltaTopTitleBar : delta_top).
                setInterpolator(new DecelerateInterpolator()).
                setListener(new AnimatorListenerAdapter() {  

                	@Override
                	    public void onAnimationEnd(Animator animation) {
                		if (null == end_action) return;
                	    end_action.run();
                	}
                });

        ObjectAnimator bg_anim = ObjectAnimator.ofFloat(((RelativeLayout) parentView.findViewById(R.id.bg_layout)), "alpha", 1f, flag ? 1f : 0f);
        bg_anim.setDuration(DURATION);
        bg_anim.start();
    }
    
    private UIParallaxScroll.OnScrollChangedListener mOnScrollChangedListener = new UIParallaxScroll.OnScrollChangedListener() {

		public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
        	final float headerHeight = ViewHelper.getY(parentView.findViewById(R.id.player_artist_frame)) - (playerTitleBar.getHeight() - parentView.findViewById(R.id.player_artist_frame).getHeight());
            ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
            final int newAlpha = (int) (ratio * 255);
            playerTitleBar.getBackground().setAlpha(newAlpha);
            Animation animationFadeIn = AnimationUtils.loadAnimation(getActivity(),R.anim.fadein);
            Animation animationFadeOut = AnimationUtils.loadAnimation(getActivity(),R.anim.fadeout);
            if (newAlpha == 255 && playerTitleBarArtis.getVisibility() != View.VISIBLE && !animationFadeIn.hasStarted()){
            	playerTitleBarArtis.setVisibility(View.VISIBLE);
            	playerTitleBarTitle.setVisibility(View.VISIBLE);
            	playerTitleBarArtis.startAnimation(animationFadeIn);
            	playerTitleBarTitle.startAnimation(animationFadeIn);
            } else if (newAlpha < 255 && !animationFadeOut.hasStarted() && playerTitleBarArtis.getVisibility() != View.INVISIBLE)  { 	
            	playerTitleBarArtis.startAnimation(animationFadeOut);
            	playerTitleBarArtis.setVisibility(View.INVISIBLE);
            	playerTitleBarTitle.startAnimation(animationFadeOut);
            	playerTitleBarTitle.setVisibility(View.INVISIBLE);
            }
            moveCover(ratio);
        }
    };
    
	private void moveCover(final float ratio) {
		if (isNeedCalculateCover) {
			coverTitleBarLocation();
			isNeedCalculateCover = false;
		}
		playerCover.setTranslationX(maxTranslationX * ratio);
        playerCover.setTranslationY(maxTranslationY * ratio);
		ViewHelper.setScaleX(playerCover, 1.f - deltaScale * ratio);
        ViewHelper.setScaleY(playerCover, 1.f - deltaScale * ratio);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		isNeedCalculateCover = true;
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void error(final String error) {
		((MainActivity) getActivity()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT);
				player.stopPressed();
			}
		});
	}
}
