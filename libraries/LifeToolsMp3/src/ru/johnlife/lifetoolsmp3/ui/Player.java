package ru.johnlife.lifetoolsmp3.ui;

import java.util.ArrayList;
import java.util.HashMap;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher;
import ru.johnlife.lifetoolsmp3.engines.lyric.LyricsFetcher.OnLyricsFetchedListener;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public final class Player extends AsyncTask<String, Void, Boolean> {
		private String url = null;
		private MediaPlayer mediaPlayer;
		private boolean prepared = false;
		private ProgressBar spinner;
		private ImageButton button;
		private ProgressBar progress;
		private TextView time;
		private ImageView coverImage;
		private ProgressBar coverProgress;
		private TextView textPath;
		private LinearLayout viewChooser;
		private TextView spinerPath;
		private TextView buttonShowLyrics;
		private TextView buttonEditMp3Tag;
		private RelativeLayout rlCoverProgress;
		private View view;
		private int songId;
		boolean isId3Show = false;
		private ArrayList <String> sFields;


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
			this.view = view;
			final String[] arrayField = {artist, title, ""};
			songId = -1;
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			spinner = (ProgressBar) view.findViewById(R.id.spinner);
			button = (ImageButton) view.findViewById(R.id.pause);
			progress = (ProgressBar) view.findViewById(R.id.progress);
			time = (TextView) view.findViewById(R.id.time);
			coverImage = (ImageView) view.findViewById(R.id.cover);
			coverProgress = (ProgressBar) view.findViewById(R.id.coverProgress);
			rlCoverProgress = (RelativeLayout) view.findViewById(R.id.rlCoverProgress);
			textPath = (TextView) view.findViewById(R.id.text_path_download);
			textPath.setText(OnlineSearchView.getDownloadPath(view.getContext()));
			viewChooser = (LinearLayout) view.findViewById(R.id.path_download);
			spinerPath = (TextView) view.findViewById(R.id.spiner_path_download);
			spinerPath.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1Event) {
					switch (arg1Event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						spinerPath.setBackgroundColor(Color.parseColor("#33777777"));
						break;
					default:
						spinerPath.setBackgroundColor(Color.parseColor("#00000000"));
						break;
					}
					return false;
				}
			});
			buttonShowLyrics = (TextView) view.findViewById(R.id.button_show_lyrics);
			buttonShowLyrics.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1Event) {
					switch (arg1Event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						buttonShowLyrics.setBackgroundColor(Color.parseColor("#33777777"));
						break;
					default:
						buttonShowLyrics.setBackgroundColor(Color.parseColor("#00000000"));
						break;
					}
					return false;
				}
			});
			buttonEditMp3Tag = (TextView) view.findViewById(R.id.button_edit_mp3_tag);
			buttonEditMp3Tag.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1Event) {
					switch (arg1Event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						buttonEditMp3Tag.setBackgroundColor(Color.parseColor("#33777777"));
						break;
					default:
						buttonEditMp3Tag.setBackgroundColor(Color.parseColor("#00000000"));
						break;
					}
					return false;
				}
			});
			spinerPath.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showHideChooser();
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
			buttonEditMp3Tag.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					createId3dialog(arrayField, true);
				}
			});
			buttonShowLyrics.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					createLyricsDialog(title, artist, null);
				}
			});
		}

		public void createDirectoryChooserDialog() {
			DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(view.getContext(), new DirectoryChooserDialog.ChosenDirectoryListener() {
				@Override
				public void onChosenDir(String chosenDir) {
					textPath.setText(chosenDir);
					OnlineSearchView.setDownloadPath(view.getContext(), chosenDir);
				}
			});
			if (null != SongArrayHolder.getInstance().getDirectoryChooserPath()) {
				directoryChooserDialog.chooseDirectory(SongArrayHolder.getInstance().getDirectoryChooserPath());
			} else {
				directoryChooserDialog.chooseDirectory(OnlineSearchView.getDownloadPath(view.getContext()));
			}
		}
		
		public void createLyricsDialog(final String title, final String artist, String lyrics) {
			SongArrayHolder.getInstance().setLyricsOpened(true, new String[] {title, artist});
			LayoutInflater inflater = (LayoutInflater)view.getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
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
					SongArrayHolder.getInstance().setLyricsOpened(false, null);
					SongArrayHolder.getInstance().setLyricsString(null);
				}
			});
			b.create().show();
			LyricsFetcher lyricsFetcher = new LyricsFetcher(buttonShowLyrics.getContext());
			lyricsFetcher.fetchLyrics(title, artist);
			final TextView lyricsTextView = (TextView)lyricsView.findViewById(R.id.lyrics);
			final LinearLayout progressLayout = (LinearLayout)lyricsView.findViewById(R.id.download_progress);
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
							lyricsTextView.setText(buttonShowLyrics.getContext().getResources().getString(R.string.lyric_not_found, songName));
						}
					}
				});
			} else {
				if (lyrics.equals("")) {
					String songName = artist + " - " + title;
					lyricsTextView.setText(buttonShowLyrics.getContext().getResources().getString(R.string.lyric_not_found, songName));
				} else {
					lyricsTextView.setText(Html.fromHtml(lyrics));
				}
				progressLayout.setVisibility(View.GONE);
			}
		}

		public void createId3dialog(String[] fields, boolean enableCover) {
			//TODO: bug here!!!
			SongArrayHolder.getInstance().setID3DialogOpened(true, fields, 
					SongArrayHolder.getInstance().isCoverEnabled());
			final MP3Editor editor = new MP3Editor(view.getContext(), enableCover);
			editor.setStrings(fields);
			AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext()).setView(editor.getView());
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor settingsEditor = PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit();
					String artistName = editor.getNewArtistName();
					String albumTitle =  editor.getNewAlbumTitle();
					String songTitle = editor.getNewSongTitle();
					
					boolean useAlbumCover = editor.useAlbumCover();
					settingsEditor.putString(BaseConstants.EDIT_ARTIST_NAME, artistName);
					settingsEditor.putString(BaseConstants.EDIT_ALBUM_TITLE, albumTitle);
					settingsEditor.putString(BaseConstants.EDIT_SONG_TITLE, songTitle);
					settingsEditor.putBoolean(BaseConstants.USE_ALBUM_COVER, useAlbumCover);
					settingsEditor.commit();
					SongArrayHolder.getInstance().setID3DialogOpened(false, null, useAlbumCover);
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences.Editor settingsEditor = PreferenceManager.getDefaultSharedPreferences(view.getContext()).edit();
					settingsEditor.putString(BaseConstants.EDIT_ARTIST_NAME, "");
					settingsEditor.putString(BaseConstants.EDIT_ALBUM_TITLE, "");
					settingsEditor.putString(BaseConstants.EDIT_SONG_TITLE, "");
					settingsEditor.putBoolean(BaseConstants.USE_ALBUM_COVER, true);
					settingsEditor.commit();
					SongArrayHolder.getInstance().setID3DialogOpened(false, null, true);
				}
			});
			AlertDialog alertDialog = builder.create();  
			alertDialog.show();
		}
		
		public ArrayList <String> getFields () {
			return sFields;
		}
		
		public Integer getSongId() {
			return songId;
		}

		private void showHideChooser() {
			if (viewChooser.getVisibility() == View.VISIBLE)
				viewChooser.setVisibility(View.GONE);
			else
				viewChooser.setVisibility(View.VISIBLE);
		}

		void setDownloadUrl(String downloadUrl) {
			url = downloadUrl;
		}

		String getDownloadUrl() {
			return url;
		}

		public void setCover(Bitmap bmp) {
			coverProgress.setVisibility(View.GONE);
			rlCoverProgress.setVisibility(View.GONE);
			if (null != bmp) {
				coverImage.setImageBitmap(bmp);
			}
		}

		public void hideCoverProgress() {
			coverProgress.setVisibility(View.GONE);
			rlCoverProgress.setVisibility(View.GONE);
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

		public void setBlackTheme(){
			LinearLayout ll = (LinearLayout)view.findViewById(R.id.download_dialog);
			ll.setBackgroundColor(Color.parseColor("#ff101010"));
		}

		private Runnable progressAction = new Runnable() {
			@Override
			public void run() {
				try {
					int current = mediaPlayer.getCurrentPosition();
					int total = mediaPlayer.getDuration();
					progress.setProgress(current);
					time.setText(formatTime(current) + " / " + formatTime(total));
					progress.postDelayed(this, 1000);
				} catch (NullPointerException e) {
					// terminate
				}
			}
		};

		public void onPrepared() {
			spinner.setVisibility(View.GONE);
			button.setVisibility(View.VISIBLE);
//			Intent i = new Intent(PlaybackService.ACTION_PAUSE);
//			spinner.getContext().startService(i);
			int duration = mediaPlayer.getDuration();
			if (duration == -1) {
				progress.setIndeterminate(true);
			} else {
				time.setText(formatTime(duration));
				progress.setIndeterminate(false);
				progress.setProgress(0);
				progress.setMax(duration);
				progress.postDelayed(progressAction, 1000);
			}
		}

		private String formatTime(int duration) {
			duration /= 1000;
			int min = duration / 60;
			int sec = duration % 60;
			return String.format("%d:%02d", min, sec);
		}

		public void onPaused() {
			button.setImageResource(R.drawable.play);
		}

		public void onResumed() {
			button.setImageResource(R.drawable.pause);
		}

		public void onFinished() {
			button.setVisibility(View.INVISIBLE);
			progress.setIndeterminate(false);
			progress.setProgress(100);
			progress.setMax(100);
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
				onPrepared();
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