package ru.johnlife.lifetoolsmp3.ui;

import java.util.HashMap;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public final class Player extends AsyncTask<String, Void, Boolean> {

	private Runnable action;
	private DirectoryChooserDialog directoryChooserDialog;
	private MediaPlayer mediaPlayer;
	private Bitmap coverBitmap;
	private View view;
	private LinearLayout boxPlayer;
	private ProgressBar coverProgress;
	private ProgressBar spinner;
	private ProgressBar progress;
	private ImageButton button;
	private ImageView coverImage;
	private TableRow rowLirycs;
	private TableRow rowTags;
	private TextView tvTags;
	private TextView tvLyrics;
	private TextView time;
	private TextView textPath;
	private String url = null;
	private String title, artist;
	private String timeText;
	private int current;
	private int songId;
	private int duration;
	private int imagePause;
	private boolean isId3Show = false;
	private boolean coverProgressVisible = true;
	private boolean prepared = false;
	private boolean indeterminate;
	private boolean buttonVisible = false;
	private boolean spinnerVisible = true;
	private boolean useCover = true;

	public void setSongId(Integer songId) {
		this.songId = songId;
	}

	public void setTitle(String title) {
		ScrollingTextView textView = (ScrollingTextView) view.findViewById(R.id.download_title);
		if (textView != null)
			textView.setText(title);
	}

	public Player(final View view, final String title, final String artist) {
		super();
		this.artist = artist;
		this.title = title;
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		initView(view);
	}

	public void initView(final View view) {
		this.view = view;
		songId = -1;
		init(view);
		if (!spinnerVisible) {
			spinner.setVisibility(View.GONE);
		}
		if (buttonVisible) {
			boxPlayer.setVisibility(View.VISIBLE);
			button.setImageResource(imagePause);
			tvLyrics.setVisibility(View.GONE);
			tvTags.setVisibility(View.GONE);
		} else {
			boxPlayer.setVisibility(View.GONE);
			tvLyrics.setVisibility(View.VISIBLE);
			tvTags.setVisibility(View.VISIBLE);
		}
		progress.setProgress(current);
		progress.postDelayed(action, 1000);
		progress.setIndeterminate(indeterminate);
		progress.setMax(duration);
		time.setText(timeText);
		textPath.setText(OnlineSearchView.getDownloadPath(view.getContext()));
		if (coverBitmap != null) {
			coverImage.setImageBitmap(coverBitmap);
		}
		if (!coverProgressVisible) {
			coverProgress.setVisibility(View.GONE);
		}
		rowLirycs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createLyricsDialog(title, artist, null);
			}
		});
		rowTags.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] arrayField = { artist, title, "" };
				createId3dialog(arrayField, true, false);
			}
		});
		textPath.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createDirectoryChooserDialog();
			}
		});
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPause();
			}
		});
	}

	private void init(final View view) {
		spinner = (ProgressBar) view.findViewById(R.id.spinner);
		button = (ImageButton) view.findViewById(R.id.pause);
		progress = (ProgressBar) view.findViewById(R.id.progress);
		time = (TextView) view.findViewById(R.id.time);
		coverImage = (ImageView) view.findViewById(R.id.cover);
		coverProgress = (ProgressBar) view.findViewById(R.id.coverProgress);
		textPath = (TextView) view.findViewById(R.id.text_path_download);
		boxPlayer = (LinearLayout) view.findViewById(R.id.box_player);
		rowTags = (TableRow) view.findViewById(R.id.row_tags);
		rowLirycs = (TableRow) view.findViewById(R.id.row_lyrics);
		tvLyrics = (TextView) view.findViewById(R.id.tv_show_lyrics);
		tvTags = (TextView) view.findViewById(R.id.tv_edit_mp3_tag);
	}

	public void createDirectoryChooserDialog() {
		SongArrayHolder.getInstance().setDirectoryChooserOpened(true);
		directoryChooserDialog = new DirectoryChooserDialog(view.getContext(), new DirectoryChooserDialog.ChosenDirectoryListener() {
			@Override
			public void onChosenDir(String chDir) {
				textPath.setText(chDir);
				OnlineSearchView.setDownloadPath(view.getContext(), chDir);
			}
		});
		if (null != SongArrayHolder.getInstance().getDirectoryChooserPath()) {
			directoryChooserDialog.chooseDirectory(SongArrayHolder.getInstance().getDirectoryChooserPath());
		} else {
			directoryChooserDialog.chooseDirectory(OnlineSearchView.getDownloadPath(view.getContext()));
		}
	}

	public void createNewDirDialog(String name) {
		directoryChooserDialog.createNewDirDialog(name);
	}

	public void createLyricsDialog(final String title, final String artist, String lyrics) {
		SongArrayHolder.getInstance().setLyricsOpened(true, new String[] { title, artist });
		LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		if (OnlineSearchView.isOffline(view.getContext())) {
			Toast.makeText(view.getContext(), view.getContext().getString(R.string.search_message_no_internet), Toast.LENGTH_LONG).show();
			return;
		}
		final View lyricsView = inflater.inflate(R.layout.lyrics_view, null);
		AlertDialog.Builder b = new Builder(view.getContext());
		b.setView(lyricsView);
		b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				cancelLirycs();
			}
		});
		b.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				cancelLirycs();
			}
		});
		b.create().show();
		LyricsFetcher lyricsFetcher = new LyricsFetcher(view.getContext());
		lyricsFetcher.fetchLyrics(title, artist);
		final TextView lyricsTextView = (TextView) lyricsView.findViewById(R.id.lyrics);
		final LinearLayout progressLayout = (LinearLayout) lyricsView.findViewById(R.id.download_progress);
		if (null == lyrics) {
			lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {
				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
					progressLayout.setVisibility(View.GONE);
					if (foundLyrics) {
						lyricsTextView.setText(Html.fromHtml(lyrics));
						SongArrayHolder.getInstance().setLyricsString(lyrics);
					} else {
						String songName = artist + " - " + title;
						lyricsTextView.setText(view.getContext().getResources().getString(R.string.lyric_not_found, songName));
					}
				}
			});
		} else {
			if (lyrics.equals("")) {
				String songName = artist + " - " + title;
				lyricsTextView.setText(view.getContext().getResources().getString(R.string.lyric_not_found, songName));
			} else {
				lyricsTextView.setText(Html.fromHtml(lyrics));
			}
			progressLayout.setVisibility(View.GONE);
		}
	}

	private void cancelLirycs() {
		SongArrayHolder.getInstance().setLyricsOpened(false, null);
		SongArrayHolder.getInstance().setLyricsString(null);
	}

	public void createId3dialog(String[] fields, boolean enableCover, boolean forse) {
		String[] arrayField = { artist, title, "" };
		SongArrayHolder.getInstance().setID3DialogOpened(true, fields, SongArrayHolder.getInstance().isCoverEnabled());
		final MP3Editor editor = new MP3Editor(view.getContext(), enableCover);
		editor.setStrings(fields);
		if (!forse) {
			editor.setShowCover(useCover);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext()).setView(editor.getView());
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				artist = editor.getNewArtistName();
				title = editor.getNewSongTitle();
				useCover = editor.useAlbumCover();
				SongArrayHolder.getInstance().setID3DialogOpened(false, null, useCover);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelMP3editor();
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				cancelMP3editor();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	private void cancelMP3editor() {
		SongArrayHolder.getInstance().setID3DialogOpened(false, null, useCover);
	}

	public String getArtist() {
		return artist == null ? "" : artist;
	}

	public String getTitle() {
		return title == null ? "" : title;
	}

	public boolean isUseCover() {
		return useCover;
	}

	public Integer getSongId() {
		return songId;
	}

	void setDownloadUrl(String downloadUrl) {
		url = downloadUrl;
	}

	String getDownloadUrl() {
		return url;
	}

	public void setCover(Bitmap bmp) {
		coverProgress.setVisibility(View.GONE);
		coverProgressVisible = false;
		if (null != bmp) {
			coverImage.setImageBitmap(bmp);
			coverBitmap = bmp;
		}
	}

	public void hideCoverProgress() {
		coverProgress.setVisibility(View.GONE);
	}

	public View getView() {
		ViewGroup parent = (ViewGroup) view.getParent();
		Log.d(getClass().getSimpleName(), "getView()");
		if (null != parent) {
			parent.removeView(view);
			Log.d(getClass().getSimpleName(), "...removed from parent");
		}
		return view;
	}

	public void setBlackTheme() {
		LinearLayout ll = (LinearLayout) view.findViewById(R.id.download_dialog);
		ll.setBackgroundColor(Color.parseColor("#ff101010"));
	}

	private Runnable progressAction = new Runnable() {
		@Override
		public void run() {
			try {
				current = mediaPlayer.getCurrentPosition();
				int total = mediaPlayer.getDuration();
				progress.setProgress(current);
				timeText = Util.formatTimeSimple(current) + " / " + Util.formatTimeSimple(total);
				time.setText(timeText);
				action = this;
				progress.postDelayed(action, 1000);
			} catch (NullPointerException e) {
				// terminate
			}
		}
	};

	public void onPrepared() {
		spinnerVisible = false;
		rowLirycs.removeView(tvLyrics);
		rowTags.removeView(tvTags);
		spinner.setVisibility(View.GONE);
		boxPlayer.setVisibility(View.VISIBLE);
		buttonVisible = true;
		duration = mediaPlayer.getDuration();
		if (duration == -1) {
			progress.setIndeterminate(true);
		} else {
			time.setText(Util.formatTimeSimple(duration));
			progress.setIndeterminate(false);
			current = 0;
			progress.setProgress(current);
			progress.setMax(duration);
			action = progressAction;
			progress.postDelayed(action, 1000);
		}
	}

	public void onPaused() {
		imagePause = R.drawable.play;
		button.setImageResource(R.drawable.play);
	}

	public void onResumed() {
		imagePause = R.drawable.pause;
		button.setImageResource(R.drawable.pause);
	}

	public void onFinished() {
		buttonVisible = false;
		button.setVisibility(View.INVISIBLE);
		indeterminate = false;
		progress.setIndeterminate(indeterminate);
		current = 100;
		progress.setProgress(current);
		duration = 100;
		progress.setMax(duration);
		progress.removeCallbacks(progressAction);
	}

	@SuppressLint("NewApi")
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			// mediaPlayer.setDataSource(url);
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("User-Agent",
					"2.0.0.6 –≤ Debian GNU/Linux 4.0 ‚Äî Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
			mediaPlayer.setDataSource(view.getContext(), Uri.parse(url), headers);
			mediaPlayer.prepare();
			prepared = true;
			if (isCancelled()) {
				releasePlayer();
			} else {
				return true;
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error buffering song", e);
		}
		return false;
	}

	private void releasePlayer() {
		if (null != mediaPlayer && prepared) {
			progress.removeCallbacks(progressAction);
			try {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
			} catch (IllegalStateException e) {
				// do nothing
			}
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (result && prepared) {
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					releasePlayer();
					onFinished();
				}
			});
			rowLirycs.postDelayed(new Runnable() {
				public void run() {
					onPrepared();
				}
			}, 1000);

		}
	}

	public void cancel() {
		super.cancel(true);
		releasePlayer();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		releasePlayer();
	}

	public void playPause() {
		if (!prepared || null == mediaPlayer)
			return;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			onPaused();
		} else {
			mediaPlayer.start();
			onResumed();
		}
	}

}