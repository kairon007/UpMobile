package org.kreed.vanilla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.vanilla.engines.BaseSearchTask;
import org.kreed.vanilla.engines.Engine;
import org.kreed.vanilla.engines.FinishedParsingSongs;
import org.kreed.vanilla.engines.SearchWithPages;
import org.kreed.vanilla.engines.cover.CoverLoaderTask;
import org.kreed.vanilla.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import org.kreed.vanilla.engines.cover.MuzicBrainzCoverLoaderTask;
import org.kreed.vanilla.engines.cover.MuzicBrainzCoverLoaderTask.Size;
import org.kreed.vanilla.song.GrooveSong;
import org.kreed.vanilla.song.RemoteSong;
import org.kreed.vanilla.song.Song;
import org.kreed.vanilla.song.SongWithCover;
import org.kreed.vanilla.ui.AdapterHelper;
import org.kreed.vanilla.ui.AdapterHelper.ViewBuilder;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchTab {
	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.vanilla";
	private static SearchTab instance;
	private static List<Engine> engines;
	private Iterator<Engine> taskIterator;
	private String currentName = null;
	private SongSearchAdapter resultAdapter;
	private TextView message;
	private String searchString;
	private View progress;
	private TextView searchField;
	private LayoutInflater inflater;
	private View view;
	private LibraryActivity activity;
	private Player player;
	private CoverLoaderTask coverLoader;
	private AsyncTask<Void, Void, String> getUrlTask;
	private boolean searchStopped = true;
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";

	@SuppressWarnings("unchecked")
	public static final SearchTab getInstance(LayoutInflater inflater, LibraryActivity activity) {
		if (null == instance) {
			instance = new SearchTab(inflater.inflate(R.layout.search, null), inflater, activity);
			if (null == engines) {
				refreshSearchEngines(activity);
			}
		} else {
			instance.activity = activity;
		}
		return instance;
	}
	
	public static void refreshSearchEngines(Context context) { 
		String[][] engineArray = Settings.GET_SEARCH_ENGINES(context);
		engines = new ArrayList<Engine>(engineArray.length);
		for (int i=0; i<engineArray.length; i++) {
			try {
				Class<? extends BaseSearchTask> engineClass = 
						(Class<? extends BaseSearchTask>) Class.forName("org.kreed.vanilla.engines." + engineArray[i][0]);
				int maxPages = Integer.parseInt(engineArray[i][1]);
				for(int page=1; page<=maxPages; page++) {
					engines.add(new Engine(engineClass, page));
				}
			} catch (ClassNotFoundException e) {
				Log.e("SearchTab", "Unknown engine", e);
			}
		}
	}

	public static String getDownloadPath(Context context) {
		String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
		if (context != null) {
			SharedPreferences downloadDetails = context.getSharedPreferences(SearchTab.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
			String sharedDownloadPath = downloadDetails.getString(SearchTab.DOWNLOAD_DIR, "");
			if (sharedDownloadPath.equals("")) {
				Editor edit = downloadDetails.edit();
				edit.clear();
				edit.putString(SearchTab.DOWNLOAD_DIR, downloadPath);
				edit.commit();
			} else
				return sharedDownloadPath;
		} 
		return downloadPath; 
	}

	public static String getSimpleDownloadPath(String absPath) {
		return absPath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
	}

	public static void setDownloadPath(Context context, String downloadPath) {
		SharedPreferences downloadDetails = context.getSharedPreferences(SearchTab.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
		Editor edit = downloadDetails.edit();
		edit.clear();
		edit.putString(SearchTab.DOWNLOAD_DIR, downloadPath);
		edit.commit();
	}

	public static final View getInstanceView(LayoutInflater inflater, LibraryActivity activity) {
		View instanceView = getInstance(inflater, activity).view;
		ViewGroup parent = (ViewGroup) instanceView.getParent();
		if (null != parent) {
			parent.removeView(instanceView);
		}
		return instanceView;
	}

	private static class DownloadClickListener implements View.OnClickListener, OnBitmapReadyListener {
		private final Context context;
		private final String songTitle;
		private final Player player;
		private String songArtist;
		private Bitmap cover;
		private boolean waitingForCover = true;

		private DownloadClickListener(Context context, String songTitle, String songArtist, Player player) {
			this.context = context;
			this.songTitle = songTitle;
			this.songArtist = songArtist;
			this.player = player;
		}

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			if (player == null)
				return;
			String downloadUrl = player.getDownloadUrl();
			Integer songId = player.getSongId();
			if (downloadUrl == null || downloadUrl.equals("")) {
				Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
				return;
			}
			player.cancel();

			final File musicDir = new File(getDownloadPath(context));
			if (!musicDir.exists()) {
				musicDir.mkdirs();
			}
			StringBuilder sb = new StringBuilder(songArtist).append(" - ").append(songTitle);
			if (songId != -1) {
				Log.d("GroovesharkClient", "Its GrooveSharkDownloader. SongID: " + songId);
				DownloadGrooveshark manager = new DownloadGrooveshark(songId, getDownloadPath(context), sb.append(".mp3").toString(), context);
				manager.execute();
			} else {
				final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

				final String fileName = sb.toString();
				request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(fileName);
				request.setDestinationInExternalPublicDir(getSimpleDownloadPath(getDownloadPath(context)), sb.append(".mp3").toString());
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					request.allowScanningByMediaScanner();
				}
				final long downloadId = manager.enqueue(request);
				Toast.makeText(context, String.format(context.getString(R.string.download_started), fileName), Toast.LENGTH_SHORT).show();
				final TimerTask progresUpdateTask = new TimerTask() {
					private File src;

					@Override
					public void run() {
						try {
							if (waitingForCover)
								return;
							Cursor c = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
							if (c == null || !c.moveToFirst())
								return;
							int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
							if (columnIndex == -1)
								return;
							String path = c.getString(columnIndex);
							c.close();
							src = new File(path);
							MusicMetadataSet src_set = new MyID3().read(src); // read
																				// metadata
							if (src_set == null) {
								return;
							}
							MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
							metadata.setSongTitle(songTitle);
							metadata.setArtist(songArtist);
							if (null != cover) {
								ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
								cover.compress(CompressFormat.JPEG, 85, out);
								metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
							}
							File dst = new File(src.getParentFile(), src.getName() + "-1");
							new MyID3().write(src, dst, src_set, metadata); // write
																			// updated
																			// metadata
							dst.renameTo(src);
							this.cancel();
						} catch (Exception e) {
							Log.e(getClass().getSimpleName(), "error writing ID3", e);
						}
					}
					// private void notifyMediascanner() {
					// Uri uri = Uri.fromFile(src.getParentFile());
					// Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED,
					// uri);
					// context.sendBroadcast(intent);
					// MediaScannerConnection.scanFile(context,
					// new String[] { dst.getAbsolutePath() }, null,
					// new MediaScannerConnection.OnScanCompletedListener() {
					// public void onScanCompleted(String path, Uri uri) {
					// Log.i("TAG", "Finished scanning " + path);
					// }
					// });
					// }
				};
				new Timer().schedule(progresUpdateTask, 1000, 1000);
			}
		}

		@Override
		public void onBitmapReady(Bitmap bmp) {
			this.cover = bmp;
			this.waitingForCover = false;
		}

	}

	private final class SongSearchAdapter extends ArrayAdapter<Song> {
		private LayoutInflater inflater;
		private FrameLayout footer;
		private ProgressBar refreshSpinner;
		private Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>(0);

		private SongSearchAdapter(Context context, LayoutInflater inflater) {
			super(context, -1, new ArrayList<Song>());
			this.inflater = inflater;
			this.footer = new FrameLayout(context);
			this.refreshSpinner = new ProgressBar(context);
			refreshSpinner.setIndeterminate(true);
			footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
			refreshSpinner.setVisibility(View.GONE);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Song song = getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setButtonVisible(false).setLongClickable(false).setExpandable(false).setLine1(song.getTitle()).setLine2(song.getArtist())
			// .setNumber(String.valueOf(position+1), 0)
					.setId(position).setIcon(R.drawable.fallback_cover);
			if (song instanceof SongWithCover) {
				if (bitmaps.containsKey(position)) {
					builder.setIcon(bitmaps.get(position));
				} else {
					String smallCoverUrl = ((SongWithCover) song).getSmallCoverUrl();
					if (smallCoverUrl != null) {
						CoverLoaderTask coverLoader = new CoverLoaderTask(smallCoverUrl);
						coverLoader.addListener(new OnBitmapReadyListener() {
							@Override
							public void onBitmapReady(Bitmap bmp) {
								bitmaps.put(position, bmp);
								if (builder != null && builder.getId() == position) {
									builder.setIcon(bmp);
								}
							}
						});
						coverLoader.execute(NO_PARAMS);
					}
				}
			}
			if (position == getCount() - 1) {
				refreshSpinner.setVisibility(View.VISIBLE);
				getNextResults();
			}
			return builder.build();
		}

		public View getProgress() {
			return footer;
		}

		public void hideProgress() {
			refreshSpinner.setVisibility(View.GONE);
		}

		@Override
		public void clear() {
			super.clear();
			bitmaps.clear();
		}

	}

	FinishedParsingSongs resultsListener = new FinishedParsingSongs() {
		@Override
		public void onFinishParsing(List<Song> songsList) {
			resultAdapter.hideProgress();
			if (searchStopped)
				return;
			if (songsList.isEmpty()) {
				getNextResults();
				if (!taskIterator.hasNext() && resultAdapter.isEmpty()) {
					message.setText(String.format(message.getContext().getString(R.string.search_message_empty), searchString));
					progress.setVisibility(View.GONE);
				}
			} else {
				progress.setVisibility(View.GONE);
				for (Song song : songsList) {
					resultAdapter.add(song);
				}
			}
		}
	};

	public SearchTab(final View instanceView, final LayoutInflater inflater, LibraryActivity libraryActivity) {
		this.view = instanceView;
		this.inflater = inflater;
		this.activity = libraryActivity;

		resultAdapter = new SongSearchAdapter(instanceView.getContext(), inflater);
		message = (TextView) instanceView.findViewById(R.id.message);
		progress = instanceView.findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		ListView listView = (ListView) instanceView.findViewById(R.id.list);
		listView.addFooterView(resultAdapter.getProgress());
		listView.setAdapter(resultAdapter);
		listView.setEmptyView(message);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (position == resultAdapter.getCount())
					return; // progress click
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!activity.isFinishing()) {
							Bundle bundle = new Bundle(0);
							bundle.putInt(KEY_POSITION, position);
							activity.showDialog(STREAM_DIALOG_ID, bundle);
						}
					}
				});
			}
		});
		searchField = (TextView) instanceView.findViewById(R.id.text);
		searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					trySearch();
					return true;
				}
				return false;
			}
		});
		instanceView.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				trySearch();
			}

		});
		instanceView.findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDownloadsList();
			}
		});
		instanceView.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchField.setText(null);
				message.setText(R.string.search_message_default);
				resultAdapter.clear();
				searchStopped = true;
			}
		});
	}

	public static boolean isOffline(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo == null;
	}

	private void trySearch() {
		
		InputMethodManager imm = (InputMethodManager) searchField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
		searchString = searchField.getText().toString();
		if (isOffline(searchField.getContext())) {
			message.setText(R.string.search_message_no_internet);
			resultAdapter.clear();
		} else if ((null == searchString) || ("".equals(searchString))) {
			resultAdapter.clear();
			message.setText(R.string.search_message_nothing);
		} else {
			search(searchString);
		}
		
		
		try {
			if (Advertisement.isOnline(activity)) {
				Advertisement.searchStart(activity);
			}
		} catch(Exception e) {
			
		}
		
	}

	public void search(String songName) {
		searchStopped = false;
		taskIterator = engines.iterator();
		resultAdapter.clear();
		currentName = songName;
		message.setText(null);
		progress.setVisibility(View.VISIBLE);
		getNextResults();
	}

	private void getNextResults() {
		if (!taskIterator.hasNext()) {
			resultAdapter.hideProgress();
			return;
		}
		try {
			Engine engine = taskIterator.next();
			BaseSearchTask searchTask = engine.getEngineClass()
					.getConstructor(BaseSearchTask.PARAMETER_TYPES).newInstance(new Object[] { resultsListener, currentName});
			if (searchTask instanceof SearchWithPages) {
				int page = engine.getPage();
				((SearchWithPages) searchTask).setPage(page);
			}
			searchTask.execute(NO_PARAMS);
		} catch (Exception e) {
			getNextResults();
		}
	}
	@SuppressLint("NewApi")
	public Dialog createStreamDialog(Bundle args) {
		if (!(args.containsKey(KEY_POSITION)) || resultAdapter.isEmpty()) {
			return null;
		}
		final Song song = resultAdapter.getItem(args.getInt(KEY_POSITION));
		final String title = song.getTitle();
		final String artist = song.getArtist();

		if (null == player) {
			player = new Player(inflater.inflate(R.layout.download_dialog, null));
			if ("AppTheme.Black".equals(Util.getThemeName(activity))) {
				player.setBlackTheme();
			}
			if (song instanceof GrooveSong) {
				player.setSongId(((GrooveSong) song).getSongId());
			}
			getUrlTask = new AsyncTask<Void, Void, String>() {
				@Override
				protected void onPreExecute() {
					player.getUrlDownloadComplite(false);
				}

				@Override
				protected String doInBackground(Void... params) {
					return ((RemoteSong) song).getDownloadUrl();
				}

				@Override
				protected void onPostExecute(String downloadUrl) {
					if (null != player) {
						player.setDownloadUrl(downloadUrl);
						player.getUrlDownloadComplite(true);
						player.execute();
					}
				}
			};
			getUrlTask.execute(NO_PARAMS);
			if (Settings.getIsAlbumCoversEnabled(activity)) {
				if (song instanceof SongWithCover) {
					String largeCoverUrl = ((SongWithCover) song).getLargeCoverUrl();
					coverLoader = new CoverLoaderTask(largeCoverUrl);
				} else {
					coverLoader = new MuzicBrainzCoverLoaderTask(artist, title, Size.large);
				}
				coverLoader.addListener(new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (null != player) {
							player.setCover(bmp);
						}
					}
				});
				coverLoader.execute(NO_PARAMS);
			} else {
				player.hideCoverProgress();
			}
		}
		final Context context = view.getContext();
		final Runnable dialogDismisser = new Runnable() {
			@Override
			public void run() {
				if (player != null) {
					player.cancel();
					player = null;
				}
				getUrlTask.cancel(true);
				if (Settings.getIsAlbumCoversEnabled(activity))
					coverLoader.cancel(true);
				activity.removeDialog(STREAM_DIALOG_ID);
				
			}
		};
		final DownloadClickListener downloadClickListener = new DownloadClickListener(context, title, artist, player) {
			@Override
			public void onClick(View v) {
				super.onClick(v);
				
				dialogDismisser.run();
			}
		};
		if (Settings.getIsAlbumCoversEnabled(activity))
			coverLoader.addListener(downloadClickListener);
		player.setTitle(artist + " - " + title);
		player.setOnButtonClicListener(downloadClickListener, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogDismisser.run();
			}
		});
		AlertDialog.Builder b = new AlertDialog.Builder(context).setView(player.getView());
		AlertDialog alertDialog = b.create();
		alertDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDismisser.run();
			}
		});
		return alertDialog;
	}

	private void showDownloadsList() {
		// final Context context = message.getContext();
		// final DownloadManager manager = (DownloadManager)
		// context.getSystemService(Context.DOWNLOAD_SERVICE);
		// Cursor c = manager.query(new DownloadManager.Query());
		// SimpleCursorAdapter adapter = new SimpleCursorAdapter(
		// context, R.layout.download_item, c,
		// new String[]{DownloadManager.COLUMN_LOCAL_FILENAME},
		// new int[]{R.id.filename}, 0
		// ) {
		// @Override
		// public void setViewText(TextView v, String text) {
		// File f = new File(text);
		// v.setText(f.getName());
		// }
		// };
		// new AlertDialog.Builder(context)
		// .setTitle(R.string.downloads)
		// .setAdapter(adapter, null)
		// .show();
		final Context context = message.getContext();
		Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(dm);
	}

	private final static class Player extends AsyncTask<String, Void, Boolean> {
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
		private LinearLayout spinerPath;
		private View view;
		private int songId;
		private LinearLayout downloadProgress;
		private LinearLayout playerLayout;
		private Button download;
		private Button cancel;

		public void setOnButtonClicListener(View.OnClickListener downloadClickListener, View.OnClickListener cancelClickListener) {
			if (download != null && downloadClickListener != null)
				download.setOnClickListener(downloadClickListener);
			if (cancel != null && cancelClickListener != null)
				cancel.setOnClickListener(cancelClickListener);
		}

		public void setSongId(Integer songId) {
			this.songId = songId;

		}

		public void getUrlDownloadComplite(boolean result) {
			if (downloadProgress != null && playerLayout != null)
				if (result) {
					downloadProgress.setVisibility(View.GONE);
					playerLayout.setVisibility(View.VISIBLE);
				} else {
					downloadProgress.setVisibility(View.VISIBLE);
					playerLayout.setVisibility(View.GONE);
				}
		}

		public void setTitle(String title) {
			ScrollingTextView textView = (ScrollingTextView) view.findViewById(R.id.download_title);
			if (textView != null)
				textView.setText(title);
		}

		public Player(final View view) {
			super();
			this.view = view;
			songId = -1;
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			spinner = (ProgressBar) view.findViewById(R.id.spinner);
			button = (ImageButton) view.findViewById(R.id.pause);
			progress = (ProgressBar) view.findViewById(R.id.progress);
			time = (TextView) view.findViewById(R.id.time);
			coverImage = (ImageView) view.findViewById(R.id.cover);
			coverProgress = (ProgressBar) view.findViewById(R.id.coverProgress);
			textPath = (TextView) view.findViewById(R.id.text_path_download);
			textPath.setText(getDownloadPath(view.getContext()));
			viewChooser = (LinearLayout) view.findViewById(R.id.path_download);
			spinerPath = (LinearLayout) view.findViewById(R.id.spiner_path_download);
			downloadProgress = (LinearLayout) view.findViewById(R.id.download_progress);
			playerLayout = (LinearLayout) view.findViewById(R.id.player_layout);
			download = (Button) view.findViewById(R.id.b_positiv);
			cancel = (Button) view.findViewById(R.id.b_negativ);

			spinerPath.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					showHideChooser();
				}
			});
			viewChooser.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(v.getContext(), new DirectoryChooserDialog.ChosenDirectoryListener() {
						@Override
						public void onChosenDir(String chosenDir) {
							textPath.setText(chosenDir);
							SearchTab.setDownloadPath(view.getContext(), chosenDir);
						}
					});
					directoryChooserDialog.chooseDirectory(SearchTab.getDownloadPath(view.getContext()));
				}
			});

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					playPause();
				}
			});
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

		private void setDownloadUrl(String downloadUrl) {
			url = downloadUrl;
		}

		private String getDownloadUrl() {
			return url;
		}

		public void setCover(Bitmap bmp) {
			coverProgress.setVisibility(View.GONE);
			if (null != bmp) {
				coverImage.setImageBitmap(bmp);
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
		
		public void setBlackTheme(){
			LinearLayout ll = (LinearLayout)view.findViewById(R.id.download_dialog);
			ll.setBackgroundColor(Color.parseColor("#ff101010"));
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				download.setTextColor(Color.LTGRAY);
				cancel.setTextColor(Color.LTGRAY);
			}
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
			Intent i = new Intent(PlaybackService.ACTION_PAUSE);
			spinner.getContext().startService(i);
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

}
