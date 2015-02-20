package org.upmobile.materialmusicdownloader.fragment;

import java.io.File;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.widget.UndoBarController.AdvancedUndoListener;
import org.upmobile.materialmusicdownloader.widget.UndoBarController.UndoBar;

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
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.csform.android.uiapptemplate.font.MusicTextView;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.view.PullToZoomScrollView;
import com.csform.android.uiapptemplate.view.cpb.CircularProgressButton;

public class PlayerFragment extends Fragment implements OnClickListener, BaseMaterialFragment, OnCheckedChangeListener {

	private final int MESSAGE_DURATION = 5000;
	private final int DEFAULT_SONG = 7340032; // 7 Mb
	private AbstractSong song;
	private AsyncTask<Long, Integer, Void> progressUpdater;
	private RenameTask renameTask;
	private PlaybackService player;
	private DownloadListener downloadListener;

	private PullToZoomScrollView scrollView;
	private View contentView;

	private CircularProgressButton download;
	private UndoBar undo;
	private CheckBox cbUseCover;
	private LinearLayout artistBox;
	private LinearLayout titleBox;

	// lyric sections
	private LyricsFetcher lyricsFetcher;
	private TextView playerLyricsView;

	// playback sections
	private TextView play;
	private TextView previous;
	private TextView forward;
	private TextView shuffle;
	private TextView repeat;
	private TextView stop;

	// info sections
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;
	private SeekBar playerProgress;
	private ProgressBar wait;

	private int checkIdCover;
	private int checkIdLyrics;

	private boolean isDestroy;
	private boolean isUseAlbumCover = true;

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

	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void pause(AbstractSong song) {
			if (isDestroy) {
				return;
			}
			changePlayPauseView(true);
		}

		@Override
		public void play(AbstractSong song) {
			if (isDestroy) {
				return;
			}
			changePlayPauseView(false);
		}

		@Override
		public void stop(AbstractSong s) {
			if (isDestroy) {
				return;
			}
			changePlayPauseView(true);
			setElementsView(0);
		}

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
			changePlayPauseView(true);
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
			contentView.findViewById(R.id.lyrics_progress).setVisibility(View.VISIBLE);
			contentView.findViewById(R.id.lyrics_text).setVisibility(View.GONE);
		}
	};
	
	private class ProgressUpdater extends AsyncTask<Long, Integer, Void> {

		@Override
		protected void onPreExecute() {
			download.setClickable(false);
			download.setOnClickListener(null);
			download.setIndeterminateProgressMode(true);
			download.setProgress(50);
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Long... params) {
			DownloadManager manager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
			int progress = 0;
			do {		
				if (isCancelled()) return null;
				if (params[0] != -1) {
					Cursor c = manager.query(new DownloadManager.Query().setFilterById(params[0]));
					if (c.moveToNext()) {
						int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
						int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
						int size = c.getInt(sizeIndex);
						int downloaded = c.getInt(downloadedIndex);
						if (size != -1 && size != 0) {
							progress = downloaded * 100 / size;
						} else {
							progress = downloaded * 100 / DEFAULT_SONG;
						}
						publishProgress(progress);
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
		protected void onPostExecute(Void params) {
			download.setProgress(isCancelled() ? 0 : 100);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		scrollView = new PullToZoomScrollView(getActivity());
		contentView = inflater.inflate(R.layout.player_fragment, container, false);
		init();
		setListeners();
		((MainActivity)getActivity()).setTitle(R.string.tab_now_plaing);
		player = PlaybackService.get(getActivity());
		player.addStatePlayerListener(stateListener);
		if (null != getArguments() && getArguments().containsKey(Constants.KEY_SELECTED_SONG)) {
			song = getArguments().getParcelable(Constants.KEY_SELECTED_SONG);
			if (song.equals(player.getPlayingSong()) && player.isPrepared()) {
				player.play();
			} else {
				player.play(song);
			}
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
		} else {
			changePlayPauseView(prepared);
		}
		scrollView.setContentContainerView(contentView);
		return scrollView;
	}
	
	@Override
	public void onResume() {
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
		super.onPause();
	}

	@Override
	public void onDestroy() {
		isDestroy = true;
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
		case R.id.stop:
			player.stop();
			break;
		case R.id.download:
			((CircularProgressButton)v).setIndeterminateProgressMode(true);
			((CircularProgressButton)v).setProgress(50);
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
					setCoverToZoomView(song.getCover(getActivity()));
					isUseAlbumCover = true;
					cbUseCover.setChecked(true);
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
		setCheckBoxState(false);
		if (MusicData.class == song.getClass()) {
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
		stop = (TextView) contentView.findViewById(R.id.stop);
		download = (CircularProgressButton) contentView.findViewById(R.id.download);
		playerProgress = (SeekBar) contentView.findViewById(R.id.progress_track);
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
		wait = (ProgressBar) contentView.findViewById(R.id.player_wait_song);
		undo = new UndoBar(getActivity());
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
				return false;
			}

		});
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

	private void setElementsView(int progress) {
		settingPlayerProgress();
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		} else {
			download.setVisibility(View.VISIBLE);
		}
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		playerTotalTime.setText(Util.getFormatedStrDuration(song.getDuration()));
		playerCurrTime.setText(Util.getFormatedStrDuration(progress));
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
		if (b) {
			playerProgress.setVisibility(View.VISIBLE);
			wait.setVisibility(View.GONE);
		} else {
			playerProgress.setVisibility(View.GONE);
			wait.setVisibility(View.VISIBLE);
		}
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
		stop.setClickable(isClickable);
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
			hideKeyboard();
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
			Toast toast = Toast.makeText(getActivity(), R.string.file_already_exists, Toast.LENGTH_SHORT);
			toast.show();
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
		setCheckBoxState(false);
		if (song.getClass() != MusicData.class) {
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover) {
						return;
					}
					if (null != bmp) {
						((RemoteSong) song).setHasCover(true);
						setCoverToZoomView(bmp);
						setCheckBoxState(true);
					} else {
						setCoverToZoomView(null);
						setCheckBoxState(false);
					}
				}
			};
			checkIdCover = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);
		} else {
			Bitmap bitmap = ((MusicData) song).getCover(getActivity());
			if (bitmap != null) {
				setCoverToZoomView(bitmap);
				setCheckBoxState(true);
			} else {
				setCoverToZoomView(null);
				setCheckBoxState(false);
			}
		}
	}

	/**
	 * @param bitmap
	 *            - set to image view, if bitmap == null then use default cover
	 */
	private void setCoverToZoomView(Bitmap bitmap) {
		if (isDestroy) return;
		ImageView imageView = new ImageView(getActivity());
		imageView.setPadding(8, 8, 8, 8);
		imageView.setImageBitmap(null == bitmap ? ((MainActivity) getActivity()).getDeafultBitmapCover(260, 260, 230) : bitmap);
		scrollView.setZoomView(imageView);
	}
	
	private void setCheckBoxState(boolean state) {
		cbUseCover.setOnCheckedChangeListener(null);
		cbUseCover.setEnabled(state);
		cbUseCover.setChecked(state);
		cbUseCover.setClickable(state);
		cbUseCover.setOnCheckedChangeListener(this);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(contentView.getWindowToken(), 0);
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
				new Handler(Looper.getMainLooper()).post(new Runnable() {

					@Override
					public void run() {
						downloadListener.onClick(contentView);
						progressUpdater = new ProgressUpdater();
						progressUpdater.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,downloadListener.getDownloadId());
					}
				});
			}

			@Override
			public void error(String error) {
			}
		});
	}
	
	private void thatSongIsDownloaded() {
		player.getPlayingSong().getDownloadUrl(new DownloadUrlListener() {

			@Override
			public void success(String url) {
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
				}
				running.close();
				
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
}
