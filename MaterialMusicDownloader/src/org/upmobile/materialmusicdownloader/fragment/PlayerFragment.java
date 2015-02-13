package org.upmobile.materialmusicdownloader.fragment;

import java.io.File;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.widget.UndoBarController;

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
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.view.cpb.CircularProgressButton;

public class PlayerFragment extends Fragment implements OnClickListener, BaseMaterialFragment {

	private static final String ANDROID_MEDIA_EXTRA_VOLUME_STREAM_VALUE = "android.media.EXTRA_VOLUME_STREAM_VALUE";
	private static final String ANDROID_MEDIA_VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
	private AbstractSong song;
	private RenameTask renameTask;
	private PlaybackService player;
	private LyricsFetcher lyricsFetcher;
	private DownloadListener downloadListener;

	private View parentView;
	private SeekBar playerProgress;
	private ImageView playerCover;
	private CircularProgressButton download;

	private TextView playerLyricsView;

	// playback section
	private ImageButton play;
	private ImageButton previous;
	private ImageButton forward;
	private ImageButton shuffle;
	private ImageButton repeat;
	private ImageButton stop;

	// info
	private TextView tvTitle;
	private TextView tvArtist;
	private EditText etTitle;
	private EditText etArtist;
	private TextView playerCurrTime;
	private TextView playerTotalTime;

	private int checkIdCover;
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
			if (isDestroy)
				return;
			changePlayPauseView(true);
		}

		@Override
		public void play(AbstractSong song) {
			if (isDestroy)
				return;
			changePlayPauseView(false);
			playerProgress.setIndeterminate(false);
		}

		@Override
		public void stop(AbstractSong s) {
			if (isDestroy)
				return;
			setElementsView(0);
			changePlayPauseView(true);
			setClickablePlayerElement(player.isPrepared());
		}

		@Override
		public void error() {
			if (isDestroy)
				return;
			Toast.makeText(getActivity(), ru.johnlife.lifetoolsmp3.R.string.file_is_bad, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void start(AbstractSong s) {
			song = s;
			if (isDestroy)
				return;
			((MainActivity) getActivity()).showPlayerElement(true);
			downloadButtonState(true);
			showLyrics();
			setImageButton();
			setClickablePlayerElement(true);
			changePlayPauseView(!player.isPlaying());
			setElementsView(0);
		}

		@Override
		public void update(AbstractSong current) {
			if (isDestroy)
				return;
			song = current;
			setElementsView(0);
			setClickablePlayerElement(false);
			playerProgress.setIndeterminate(true);
		}
	};

	private class FalseProgress extends AsyncTask<Integer, Integer, Integer> {

		private CircularProgressButton cpb;

		public FalseProgress(CircularProgressButton cpb) {
			this.cpb = cpb;
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			for (int progress = 0; progress < 100; progress += 5) {
				publishProgress(progress);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return params[0];
		}

		@Override
		protected void onPostExecute(Integer result) {
			cpb.setProgress(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int progress = values[0];
			cpb.setProgress(progress);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		isDestroy = false;
		parentView = inflater.inflate(R.layout.player_fragment, container, false);
		init();
		setListeners();
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
		setImageButton();
		getCover(song);
		setElementsView(player.getCurrentPosition());
		boolean prepared = player.isPrepared();
		setClickablePlayerElement(prepared);
		downloadButtonState(prepared);
		if (prepared) {
			changePlayPauseView(!player.isPlaying());
		} else {
			changePlayPauseView(prepared);
		}
		return parentView;
	}

	@Override
	public void onDestroyView() {
		player.removeStatePlayerListener(stateListener);
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		if (null != lyricsFetcher)
			lyricsFetcher.cancel();
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
				repeat.setImageResource(R.drawable.ic_media_repeat);
			} else {
				repeat.setImageResource(R.drawable.ic_media_repeat_on);
			}
			break;
		case R.id.shuffle:
			if (player.offOnShuffle()) {
				shuffle.setImageResource(R.drawable.ic_media_shuffle_on);
			} else {
				shuffle.setImageResource(R.drawable.ic_media_shuffle);
			}
			break;
		case R.id.stop:
			player.stop();
			break;
		case R.id.download:
			new FalseProgress((CircularProgressButton) v).execute(100);
			download();
			break;
		case R.id.artistName:
			openArtistField();
			break;
		case R.id.songName:
			openTitleField();
			break;
		}
	}

	private void setImageButton() {
		if (!player.enabledRepeat()) {
			repeat.setImageResource(R.drawable.ic_media_repeat);
		} else {
			repeat.setImageResource(R.drawable.ic_media_repeat_on);
		}
		if (player.enabledShuffle()) {
			shuffle.setImageResource(R.drawable.ic_media_shuffle_on);
		} else {
			shuffle.setImageResource(R.drawable.ic_media_shuffle);
		}
	}

	private void setClickablePlayerElement(boolean isClickable) {
		play.setClickable(isClickable);
		stop.setClickable(isClickable);
		playerProgress.setEnabled(isClickable);
		playerProgress.setClickable(isClickable);
		playerProgress.setIndeterminate(!isClickable);
		if (!isClickable) {
			playerCurrTime.setText("0:00");
			playerProgress.setProgress(0);
		}
	}

	/**
	 * @param isPlaying
	 *            - If "true", the button changes the picture to "play", if
	 *            "false" changes to "pause"
	 */
	private void changePlayPauseView(boolean isPlaying) {
		if (isPlaying) {
			play.setImageResource(R.drawable.ic_media_play);
		} else {
			play.setImageResource(R.drawable.ic_media_pause);
		}
	}

	private void init() {
		play = (ImageButton) parentView.findViewById(R.id.playpause);
		previous = (ImageButton) parentView.findViewById(R.id.prev);
		forward = (ImageButton) parentView.findViewById(R.id.next);
		shuffle = (ImageButton) parentView.findViewById(R.id.shuffle);
		repeat = (ImageButton) parentView.findViewById(R.id.repeat);
		stop = (ImageButton) parentView.findViewById(R.id.stop);
		download = (CircularProgressButton) parentView.findViewById(R.id.download);
		playerProgress = (SeekBar) parentView.findViewById(R.id.progress_track);
		tvTitle = (TextView) parentView.findViewById(R.id.songName);
		etTitle = (EditText) parentView.findViewById(R.id.songNameEdit);
		tvArtist = (TextView) parentView.findViewById(R.id.artistName);
		etArtist = (EditText) parentView.findViewById(R.id.artistNameEdit);
		playerCurrTime = (TextView) parentView.findViewById(R.id.trackTime);
		playerTotalTime = (TextView) parentView.findViewById(R.id.trackTotalTime);
		playerLyricsView = (TextView) parentView.findViewById(R.id.player_lyrics_view);
		playerCover = (ImageView) parentView.findViewById(R.id.albumCover);
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
		playerProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	}
			
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
		parentView.findViewById(R.id.scroll).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				closeEditViews();
				return false;
			}

		});
	}

	private void openArtistField() {
		if (tvArtist.getVisibility() == View.VISIBLE) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
			parentView.findViewById(R.id.artistNameBox).setVisibility(View.GONE);
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
			parentView.findViewById(R.id.songNameBox).setVisibility(View.GONE);
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
		// TODO closeEditViews
		if (etArtist.getVisibility() == View.VISIBLE || etTitle.getVisibility() == View.VISIBLE) {
			hideKeyboard();
			if (etTitle.getVisibility() == View.VISIBLE && !song.getTitle().equals(etTitle.getText().toString())) {
				song.setTitle(etTitle.getText().toString());
				tvTitle.setText(song.getTitle());
			} else if (etArtist.getVisibility() == View.VISIBLE && !song.getArtist().equals(etArtist.getText().toString())) {
				song.setArtist(etArtist.getText().toString());
				tvArtist.setText(song.getArtist());
			}
			parentView.findViewById(R.id.artistNameBox).setVisibility(View.VISIBLE);
			parentView.findViewById(R.id.songNameBox).setVisibility(View.VISIBLE);
			etArtist.setVisibility(View.GONE);
			etTitle.setVisibility(View.GONE);
			if (song.getClass() != MusicData.class) {
				player.update(song.getTitle(), song.getArtist(), null);
			} else {
				saveTags();
			}
		}
	}

	private void setElementsView(int progress) {
		if (song.getClass() == MusicData.class) {
			download.setVisibility(View.GONE);
		} else {
			download.setVisibility(View.VISIBLE);
		}
		playerProgress.removeCallbacks(progressAction);
		tvArtist.setText(song.getArtist());
		tvTitle.setText(song.getTitle());
		long totalTime = song.getDuration();
		playerTotalTime.setText(Util.getFormatedStrDuration(totalTime));
		playerProgress.setMax((int) totalTime);
		playerProgress.setProgress(progress);
		playerCurrTime.setText(Util.getFormatedStrDuration(progress));
		playerProgress.post(progressAction);
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

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(parentView.getWindowToken(), 0);
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
		playerCover.invalidate();
		if (delta > 0) {
			player.shift(1);
			getCover(player.getPlayingSong());
			downloadButtonState(false);
		} else if (delta < 0) {
			player.shift(-1);
			getCover(player.getPlayingSong());
			downloadButtonState(false);
		}
	}

	private void getCover(final AbstractSong song) {
		if (song.getClass() != MusicData.class) {
			playerCover.setImageResource(R.drawable.no_cover_art_big);
			OnBitmapReadyListener readyListener = new OnBitmapReadyListener() {

				@Override
				public void onBitmapReady(Bitmap bmp) {
					if (this.hashCode() != checkIdCover)
						return;
					if (null != bmp) {
						((RemoteSong) song).setHasCover(true);
						playerCover.setImageResource(0);
						playerCover.setImageBitmap(bmp);
					} else {
						playerCover.setImageResource(R.drawable.no_cover_art_big);
					}
				}
			};
			checkIdCover = readyListener.hashCode();
			((RemoteSong) song).getCover(readyListener);
		} else {
			final Bitmap bitmap = ((MusicData) song).getCover(getActivity());
			if (bitmap != null) {
				playerCover.post(new Runnable() {

					@Override
					public void run() {
						playerCover.setImageResource(0);
						playerCover.setImageBitmap(bitmap);
					}
				});
			} else {
				playerCover.setImageResource(R.drawable.no_cover_art_big);
			}
		}
	}

	private void downloadButtonState(boolean state) {
		download.setClickable(state);
		download.setEnabled(state);
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
	public int getDrawerIcon() {
		return R.drawable.ic_launcher_playing;
	}

	@Override
	public int getDrawerTitle() {
		return R.string.navigation_player;
	}

	@Override
	public int getDrawerTag() {
		return getClass().getSimpleName().hashCode();
	}
}
