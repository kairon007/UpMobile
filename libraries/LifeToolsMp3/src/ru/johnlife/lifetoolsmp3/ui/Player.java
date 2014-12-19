package ru.johnlife.lifetoolsmp3.ui;

import java.util.HashMap;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.dialog.CustomDialogBuilder;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends AsyncTask<String, Void, Boolean> {

	private Runnable action;
	private ViewGroup rowLirycs;
	private ViewGroup rowTags;
	private DirectoryChooserDialog directoryChooserDialog;
	private AlertDialog id3Dialog;
	private MP3Editor editor;
	private StateKeeper keeper;
	private RemoteSong downloadSong;
	private MediaPlayer mediaPlayer;
	private Bitmap coverBitmap;
	private View view;
	private View editorView;
	private LinearLayout boxPlayer;
	private ProgressBar coverProgress;
	private ProgressBar spinner;
	private ProgressBar progress;
	private ImageButton button;
	private ImageView coverImage;
	private TextView tvTags;
	private TextView tvLyrics;
	private TextView time;
	private TextView textPath;
	private String timeText;
	private int current;
	private int songId;
	private int duration;
	private int imagePause;
	private boolean coverProgressVisible = true;
	private boolean prepared = false;
	private boolean indeterminate;
	private boolean buttonVisible = false;
	private boolean spinnerVisible = true;
	private boolean isWhiteTheme;
	private boolean isDefaultCover = true;

	OnShowListener dialogShowListener = new OnShowListener() {

		@Override
		public void onShow(DialogInterface dialog) {
			float textSize = 16f;
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(textSize);
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(textSize);
		}
	};
	

	public void setSongId(Integer songId) {
		this.songId = songId;
	}

	public void setTitle(String title) {
		TextView textView = (TextView) view.findViewById(R.id.download_title);
		if (textView != null) {
			textView.setText(title);
		}
	}
	
	public void setDownloadSong(RemoteSong downloadSong) {
		this.downloadSong = downloadSong;
	}

	public Player(final View view, RemoteSong song, boolean isWhiteTheme) {
		super();
		keeper = StateKeeper.getInstance();
		this.downloadSong = song;
		this.isWhiteTheme = isWhiteTheme;
		mediaPlayer = new MediaPlayer();
		keeper.setCurrentPlayersId(mediaPlayer.hashCode());
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		initView(view);
	}
	
	public void initView(final View view) {
		this.view = view;
		songId = -1;
		init(view);
		if(current == 0 && mediaPlayer.getCurrentPosition() > 0) current = mediaPlayer.getCurrentPosition();
		if (!spinnerVisible) {
			spinner.setVisibility(View.GONE);
		}
		if (buttonVisible) {
			boxPlayer.setVisibility(View.VISIBLE);
			button.setImageResource(imagePause);
			if (tvLyrics != null && tvTags != null) {
				tvLyrics.setVisibility(View.GONE);
				tvTags.setVisibility(View.GONE);
			}
		} else {
			boxPlayer.setVisibility(View.GONE);
			if (tvLyrics != null && tvTags != null) {
				tvLyrics.setVisibility(View.VISIBLE);
				tvTags.setVisibility(View.VISIBLE);
			}
		}
		progress.setProgress(current);
		progress.postDelayed(action, 1000);
		progress.setIndeterminate(indeterminate);
		progress.setMax(duration);
		progress.setOnTouchListener(touchListener);
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
				createLyricsDialog();
			}
		});
		rowTags.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				createId3dialog(new String[] {downloadSong.getArtist(), downloadSong.getTitle(), downloadSong.album});
			}
		});
		view.findViewById(R.id.download_location).setOnClickListener(new View.OnClickListener() {
			
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
		rowTags = (ViewGroup) view.findViewById(R.id.row_tags);
		rowLirycs = (ViewGroup) view.findViewById(R.id.row_lyrics);
		tvLyrics = (TextView) view.findViewById(R.id.tv_show_lyrics);
		tvTags = (TextView) view.findViewById(R.id.tv_edit_mp3_tag);
	}

	public void createDirectoryChooserDialog() {
		keeper.openDialog(StateKeeper.DIRCHOOSE_DIALOG);
		directoryChooserDialog = new DirectoryChooserDialog(view.getContext(), isWhiteTheme, new DirectoryChooserDialog.ChosenDirectoryListener() {
			
			@Override
			public void onChosenDir(String chDir) {
				textPath.setText(chDir);
				OnlineSearchView.setDownloadPath(view.getContext(), chDir);
			}
		});
		if (null != keeper.getDirectoryChooserPath()) {
			directoryChooserDialog.chooseDirectory(StateKeeper.getInstance().getDirectoryChooserPath());
		} else {
			directoryChooserDialog.chooseDirectory(OnlineSearchView.getDownloadPath(view.getContext()));
		}
	}

	public void createNewDirDialog(String name) {
		directoryChooserDialog.createNewDirDialog(name);
	}
	
	public void createLyricsDialog() {
		keeper.openDialog(StateKeeper.LYRICS_DIALOG);
		LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		if (OnlineSearchView.isOffline(view.getContext())) {
			Toast.makeText(view.getContext(), view.getContext().getString(R.string.search_message_no_internet), Toast.LENGTH_LONG).show();
			return;
		}
		final View lyricsView;
		if (isWhiteTheme) {
			lyricsView = inflater.inflate(R.layout.lyrics_view_white, null);
		} else {
			lyricsView = inflater.inflate(R.layout.lyrics_view, null);
		}
		LyricsFetcher lyricsFetcher = new LyricsFetcher(view.getContext());
		AlertDialog.Builder b = CustomDialogBuilder.getBuilder(view.getContext(), isWhiteTheme);
		b.setView(lyricsView);
		b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelDialog(dialog, StateKeeper.LYRICS_DIALOG);
			}
		});
		b.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				cancelDialog(dialog, StateKeeper.LYRICS_DIALOG);
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			b.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					cancelDialog(dialog, StateKeeper.LYRICS_DIALOG);
				}
			});
		}
		final AlertDialog dialog = b.create();
		dialog.setOnShowListener(dialogShowListener);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dialog.show();
		final TextView lyricsTextView = (TextView) lyricsView.findViewById(R.id.lyrics);
		final LinearLayout progressLayout = (LinearLayout) lyricsView.findViewById(R.id.download_progress);
		String lyrics = keeper.getLyrics();
		final String strTitle = keeper.getTitleArtistLyrics()[0];
		final String strArtist = keeper.getTitleArtistLyrics()[1];
		if (null == lyrics) {
		lyricsFetcher.fetchLyrics(strTitle, strArtist);
			lyricsFetcher.setOnLyricsFetchedListener(new OnLyricsFetchedListener() {

				@Override
				public void onLyricsFetched(boolean foundLyrics, String lyrics) {
					progressLayout.setVisibility(View.GONE);
					if (foundLyrics && null != dialog && dialog.isShowing()) {
						lyricsTextView.setText(Html.fromHtml(lyrics));
						StateKeeper.getInstance().setLyricsString(lyrics);
					} else {
						if (null != dialog && dialog.isShowing()) {
							String message = String.format(view.getContext().getResources().getString(R.string.download_dialog_no_lyrics), strArtist + " - " + strTitle);
							lyricsTextView.setText(message);
						}
					}
				}
			});
		} else {
			progressLayout.setVisibility(View.GONE);
			if (lyrics.equals("")) {
				String message = String.format(view.getContext().getResources().getString(R.string.download_dialog_no_lyrics),strArtist + " - " + strTitle);
				lyricsTextView.setText(message);
			} else {
				lyricsTextView.setText(Html.fromHtml(lyrics));
			}
		}
	}

	public void createId3dialog(String[] fields) {
		keeper.openDialog(StateKeeper.EDITTAG_DIALOG);
		editor = new MP3Editor(view.getContext(), isWhiteTheme);
		editor.setStrings(fields);
		editorView = editor.getView();
		if (isDefaultCover) {
			editor.disableChekBox();
		} else {
			if (keeper.getTempID3UseCover() != 0) {
				editor.setUseCover(keeper.getTempID3UseCover() > 0);
			} else {
				editor.setUseCover(keeper.isUseCover());
			}
		}
		AlertDialog.Builder builder = CustomDialogBuilder.getBuilder(view.getContext(), isWhiteTheme).setView(editorView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(keeper.checkState(StateKeeper.MANIPULATE_TEXT_OPTION)) {
					downloadSong.setArtistName(editor.getNewArtistName());
					downloadSong.setSongTitle(editor.getNewSongTitle());
					keeper.setDownloadSong(downloadSong);
					setTitle(downloadSong.getArtist() + " - " + downloadSong.getTitle());
				}
				keeper.setUseCover(editor.useAlbumCover());
				cancelDialog(dialog, StateKeeper.EDITTAG_DIALOG);
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelDialog(dialog, StateKeeper.EDITTAG_DIALOG);
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				cancelDialog(dialog, StateKeeper.EDITTAG_DIALOG);
			}
		});
		id3Dialog = builder.create();
		id3Dialog.setOnShowListener(dialogShowListener);
		id3Dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		id3Dialog.show();
	}
	
	private void cancelDialog(DialogInterface dialog, int flag) {
		keeper.closeDialog(flag);
		dialog.dismiss();
	}

	public Integer getSongId() {
		return songId;
	}

	@SuppressLint("NewApi")
	public void setCover(Bitmap bmp) {
		coverProgress.setVisibility(View.GONE);
		coverProgressVisible = false;
		if (null != bmp) {
			isDefaultCover = false;
			coverImage.setImageBitmap(bmp);
			if (isWhiteTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && view != null) {
				float h = view.getContext().getResources().getDimension(R.dimen.min_hight_layout_player);
				float scale = (float) h / bmp.getHeight();
				coverImage.setScaleX(scale);
				coverImage.setScaleY(scale);
			}
			coverBitmap = bmp;
			if (id3Dialog !=null && id3Dialog.isShowing()) {
				editor.enableChekBox();
				editorView.invalidate();
			}
		}
	}

	public void setCoverFromSong(RemoteSong song) {
		song.getCover(new OnBitmapReadyListener() {

			@Override
			public void onBitmapReady(Bitmap bmp) {
				setCover(bmp);
			}
		});
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
				timeText = Util.getFormatedStrDuration(current) + " / " + Util.getFormatedStrDuration(total);
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
		if (tvLyrics != null && tvTags != null) {
			tvLyrics.setVisibility(View.GONE);
			tvTags.setVisibility(View.GONE);
		}
		spinner.setVisibility(View.GONE);
		boxPlayer.setVisibility(View.VISIBLE);
		buttonVisible = true;
		duration = mediaPlayer.getDuration();
		if (duration == -1) {
			progress.setIndeterminate(true);
		} else {
			time.setText(Util.getFormatedStrDuration(duration));
			progress.setIndeterminate(false);
			current = 0;
			progress.setProgress(current);
			progress.setMax(duration);
			action = progressAction;
			progress.postDelayed(action, 1000);
		}
	}

	public void onPaused() {
		imagePause = (!Util.isDifferentApp(view.getContext()) && Util.getThemeName(view.getContext()).equals("AppTheme.White")) ? R.drawable.play_white : R.drawable.play;
		button.setImageResource(imagePause);
	}

	public void onResumed() {
		imagePause = (!Util.isDifferentApp(view.getContext()) && Util.getThemeName(view.getContext()).equals("AppTheme.White")) ? R.drawable.pause_white : R.drawable.pause;
		button.setImageResource(imagePause);
	}


	@SuppressLint("NewApi")
	@Override
	protected Boolean doInBackground(String... params) {
		try {
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("User-Agent",
					"2.0.0.6 –≤ Debian GNU/Linux 4.0 ‚Äî Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
			mediaPlayer.setDataSource(view.getContext(), Uri.parse(params[0]), headers);
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
	
	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (null == mediaPlayer){
			return;
		}
		if (result) {
			mediaPlayer.prepareAsync();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					int last = StateKeeper.getInstance().getCurrentPlayersId();
					int current = mp.hashCode();
					if (keeper.checkState(StateKeeper.STREAM_DIALOG) && last == current) {
						prepared = true;
						imagePause = !Util.isDifferentApp(view.getContext()) && Util.getThemeName(view.getContext()).equals("AppTheme.White") ? R.drawable.pause_white : R.drawable.pause;
						button.setImageResource(imagePause);
						mp.start();
						keeper.activateOptions(StateKeeper.IS_PLAYING_OPTION);
						mp.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.seekTo(0);
								imagePause = !Util.isDifferentApp(view.getContext()) && Util.getThemeName(view.getContext()).equals("AppTheme.White") ? R.drawable.play_white : R.drawable.play;
								button.setImageResource(imagePause);
								progress.setProgress(0);
								keeper.deactivateOptions(StateKeeper.IS_PLAYING_OPTION);
							}
						});
						rowLirycs.postDelayed(new Runnable() {

							public void run() {
								if (null != mediaPlayer) {
									Player.this.onPrepared();
								}
							}
						}, 1000);
					} else {
						if(null!=mediaPlayer) {
							mediaPlayer.reset();
							mediaPlayer = null;
						}
					}
				}
			});
		}
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

	

	public boolean cancel() {
		boolean result; 
		result = super.cancel(true);
		releasePlayer();
		view = null;
		return result;
	}

	@Override
	protected void onCancelled() {
		
		releasePlayer();
		super.onCancelled();
	}

	public void playPause() {
		if (!prepared || null == mediaPlayer)
			return;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			keeper.deactivateOptions(StateKeeper.IS_PLAYING_OPTION);
			onPaused();
		} else {
			mediaPlayer.start();
			keeper.activateOptions(StateKeeper.IS_PLAYING_OPTION);
			onResumed();
		}
	}
	
	public void pause() {
		if (!prepared || null == mediaPlayer)
			return;
		if (mediaPlayer.isPlaying()) {
			keeper.deactivateOptions(StateKeeper.IS_PLAYING_OPTION);
			mediaPlayer.pause();
			onPaused();
		}
	}
	
	public void play() {
		if (!prepared || null == mediaPlayer)
			return;
		if (!mediaPlayer.isPlaying()) {
			keeper.activateOptions(StateKeeper.IS_PLAYING_OPTION);
			mediaPlayer.start();
			onResumed();
		}
	}
	
	private OnTouchListener touchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int currentPosition = (int)(((double)event.getX()/(double)v.getWidth())*((int)((ProgressBar)v).getMax()));
			mediaPlayer.seekTo(currentPosition);
			progress.setProgress(currentPosition);
			return true;
		}
	};
}