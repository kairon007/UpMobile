package org.upmobile.materialmusicdownloader.fragment;

import java.io.File;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.DownloadListener.OnCancelDownload;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.ui.TrueSeekBar;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.ProgressUpdaterTask;
import ru.johnlife.lifetoolsmp3.ProgressUpdaterTask.ProgressUpdaterListener;
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
import ru.johnlife.lifetoolsmp3.ui.widget.CheckBox;
import ru.johnlife.lifetoolsmp3.ui.widget.RippleView;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.AdvancedUndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import ru.johnlife.lifetoolsmp3.ui.widget.progressbutton.CircularProgressButton;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.view.PullToZoomScrollView;

public class PlayerFragment extends Fragment implements OnClickListener, BaseMaterialFragment, OnCheckedChangeListener, PlaybackService.OnErrorListener, OnEditorActionListener {

	private static final int MESSAGE_DURATION = 5000;
	private AbstractSong song;
	private AsyncTask<Long,Integer,String> progressUpdater;
	private RenameTask renameTask;
	private PlaybackService player;
	private DownloadListener downloadListener;

	private PullToZoomScrollView scrollView;
	private View contentView;

	private CircularProgressButton download;
	private RippleView ciRippleView;
	private UndoBar undo;
	private LinearLayout artistBox;
	private LinearLayout titleBox;

	// lyric sections
	private LyricsFetcher lyricsFetcher;
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

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private TrueSeekBar playerProgress;
	private ImageView imageView;
	private Bitmap defaultCover;

	private int checkIdCover;
	private int checkIdLyrics;
	private int minHeight;
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
			getCover(song);
			setElementsView(0, s);
			play.setClickable(true);
			playerProgress.setMax((int) (song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
		}

		@Override
		public void update(AbstractSong current) {
			if (isDestroy) return;
			download.setIndeterminateProgressMode(false);
			download.setProgress(0);
			playerProgress.setProgress(0);
			playerProgress.setSecondaryProgress(0);
			song = current;
			setElementsView(0, current);
			setCoverToZoomView(null);
			showLyrics();
			contentView.findViewById(R.id.lyrics_progress).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.lyrics_text).setVisibility(View.GONE);
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
	
	private ProgressUpdaterListener progressListener = new ProgressUpdaterListener() {

		private static final String FAILURE = "failure";
		Boolean canceled = false;

		@Override
		public void onProgressUpdate(Integer... values) {
			if (canceled) return;
			int progress = values[0];
			download.setIndeterminateProgressMode(false);
			download.setProgress(progress > 0 ? progress : 1);
			download.setClickable(false);
			ciRippleView.setEnabled(false);
		}

		@Override
		public void onCancelled() {
			canceled = true;
			download.setProgress(CircularProgressButton.IDLE_STATE_PROGRESS);
			setDownloadButtonState(true);
			ciRippleView.setEnabled(true);
		}

		@Override
		public void onPostExecute(String params) {
			if (FAILURE.equals(params)) {
				((MainActivity) getActivity()).showMessage(R.string.download_failed);
				download.setProgress(CircularProgressButton.IDLE_STATE_PROGRESS);
				setDownloadButtonState(true);
			} else {
				download.setProgress(canceled ? CircularProgressButton.IDLE_STATE_PROGRESS : CircularProgressButton.SUCCESS_STATE_PROGRESS);
			}
		}

		@Override
		public void onPreExecute() {
			canceled = false;
			ciRippleView.setEnabled(false);
			download.setClickable(false);
			download.setIndeterminateProgressMode(true);
			download.setProgress(CircularProgressButton.INDETERMINATE_STATE_PROGRESS);
		}

	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		contentView = inflater.inflate(R.layout.player_fragment, container, false);
		contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		isDestroy = false;
		scrollView = new PullToZoomScrollView(getActivity());
		player = PlaybackService.get(getActivity());
		song = player.getPlayingSong();
		init(contentView);
		setListeners();
		scrollView.setContentContainerView(contentView);
		return scrollView;
	}
	
	private void initCover() {
		String cover =  getResources().getString(R.string.font_musics);
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		int coverHeight = Math.abs(height - contentView.getMeasuredHeight() - Util.dpToPx(getActivity(), 16));
		minHeight = (coverHeight > width) ? width : coverHeight;
		imageView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		imageView.setPadding(0, 8, 0, 8);
		defaultCover = ((MainActivity) getActivity()).getDefaultBitmapCover(minHeight, minHeight, minHeight - 16, cover);
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
		if (song.getClass() == MusicData.class && StateKeeper.DOWNLOADED != state) {
			if (StateKeeper.DOWNLOADING == state) {
				((RippleView) download.getParent()).setEnabled(false);
				download.setClickable(false);
				download.setIndeterminateProgressMode(true);
				download.setProgress(CircularProgressButton.INDETERMINATE_STATE_PROGRESS);
			}
		}
		percent = 0;
		initCover();
		setCoverToZoomView(null);
		getCover(song);
		setImageButton();
		showLyrics();
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
		((MainActivity) getActivity()).showPlayerElement(player.isPrepared());
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
		case R.id.shuffle:
			shuffle.setAlpha(player.offOnShuffle() ? 1 : (float) 0.5);
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

	private void init(final View view) {
		play = (TextView) view.findViewById(R.id.playpause);
		previous = (TextView) view.findViewById(R.id.prev);
		forward = (TextView) view.findViewById(R.id.next);
		shuffle = (TextView) view.findViewById(R.id.shuffle);
		repeat = (TextView) view.findViewById(R.id.repeat);
		download = (CircularProgressButton) view.findViewById(R.id.download);
		ciRippleView = (RippleView) view.findViewById(R.id.circularRipple);
		playerProgress = (TrueSeekBar) view.findViewById(R.id.progress_track);
		tvTitle = (TextView) view.findViewById(R.id.songName);
		etTitle = (EditText) view.findViewById(R.id.songNameEdit);
		tvArtist = (TextView) view.findViewById(R.id.artistName);
		etArtist = (EditText) view.findViewById(R.id.artistNameEdit);
		playerCurrTime = (TextView) view.findViewById(R.id.trackTime);
		playerTotalTime = (TextView) view.findViewById(R.id.trackTotalTime);
		playerLyricsView = (TextView) view.findViewById(R.id.lyrics_text);
		cbUseCover = (CheckBox) view.findViewById(R.id.cbUseCover);
		artistBox = (LinearLayout) view.findViewById(R.id.artistNameBox);
		titleBox = (LinearLayout) view.findViewById(R.id.songNameBox);
		undo = new UndoBar(getActivity());
		imageView = new ImageView(getActivity());
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
			public void onStopTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					try {
						seekBar.setSecondaryProgress(0);
						seekBar.setSecondaryProgress(Math.max(progress, (int) (playerProgress.getMax() * percent)));
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
		player.addStatePlayerListener(stateListener);
		player.setOnErrorListener(this);
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
		player.shift(delta);
		player.pause();
		cbUseCover.setOnCheckedChangeListener(null);
		setDownloadButtonState(!player.isGettingURl());
		play.setClickable(false);
		playerProgress.setProgress(0);
		playerProgress.setIndeterminate(true);
		if (!player.enabledRepeat()) {
			setCheckBoxState(false);
			download.setProgress(CircularProgressButton.IDLE_STATE_PROGRESS);
		}
		cancelProgressTask();
		thatSongIsDownloaded();
		cbUseCover.setOnCheckedChangeListener(this);
	}

	private void setElementsView(int progress, AbstractSong song) {
		boolean isDownloaded = StateKeeper.DOWNLOADED == StateKeeper.getInstance().checkSongInfo(song.getComment());
		if (song.getClass() == MusicData.class || isDownloaded) {
			download.setVisibility(View.GONE);
			ciRippleView.setVisibility(View.GONE);
		} else {
			download.setVisibility(View.VISIBLE);
			ciRippleView.setVisibility(View.VISIBLE);
		}
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		playerTotalTime.setText(Util.getFormatedStrDuration(song.getDuration() == 0 ? player.getDuration() : song.getDuration()));
		playerCurrTime.setText(Util.getFormatedStrDuration(progress));
	}

	private void setImageButton() {
		repeat.setAlpha(player.enabledRepeat() ? 1 : (float) 0.5);
		shuffle.setAlpha(player.enabledShuffle() ? 1 : (float) 0.5);
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
			Util.hideKeyboard(getActivity(), getActivity().getCurrentFocus());
			contentView.findViewById(R.id.artistNameBox).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.songNameBox).setVisibility(View.VISIBLE);
			etArtist.setVisibility(View.GONE);
			etTitle.setVisibility(View.GONE);
			if (etTitle.getVisibility() == View.VISIBLE && !song.getTitle().equals(etTitle.getText().toString())) {
				String title = Util.removeSpecialCharacters(etTitle.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : etTitle.getText().toString());
				song.setTitle(title);
				tvTitle.setText(title);
			} else if (etArtist.getVisibility() == View.VISIBLE && !song.getArtist().equals(etArtist.getText().toString())) {
				String artist = Util.removeSpecialCharacters(etArtist.getText().toString().isEmpty() ? MP3Editor.UNKNOWN : etArtist.getText().toString());
				song.setArtist(artist);
				tvArtist.setText(artist);
			} else return;
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
			Bitmap bitmap = song.getCover();
			if (null != bitmap) {
				setCoverToZoomView(bitmap);
				setCheckBoxState(true);
				((View) cbUseCover.getParent()).setVisibility(View.VISIBLE);
				cbUseCover.setOnCheckedChangeListener(this);
				return;
			}
		}
		((View) cbUseCover.getParent()).setVisibility(View.INVISIBLE);
		if (song.getClass() != MusicData.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (hashCode() != checkIdCover) {
						cbUseCover.setOnCheckedChangeListener(PlayerFragment.this);
						return;
					}
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
	 * @param bitmap - set to image view, if bitmap == null then use default cover
	 */
	private void setCoverToZoomView(Bitmap bitmap) {
		if (isDestroy) return;
		imageView.setImageBitmap(Bitmap.createScaledBitmap(null == bitmap ? defaultCover : bitmap, minHeight, minHeight, false));
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
			download.setProgress(CircularProgressButton.INDETERMINATE_STATE_PROGRESS);
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
					        new Handler(Looper.getMainLooper()).post(new Runnable() {
								
								@Override
								public void run() {
									download.setProgress(CircularProgressButton.IDLE_STATE_PROGRESS);
									setDownloadButtonState(true);
								}
							});
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
				download.setProgress(CircularProgressButton.IDLE_STATE_PROGRESS);
				download.setIndeterminateProgressMode(false);
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
	
}