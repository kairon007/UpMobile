package org.kreed.musicdownloader.ui.tab;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.PrefKeys;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.R.drawable;
import org.kreed.musicdownloader.R.id;
import org.kreed.musicdownloader.R.layout;
import org.kreed.musicdownloader.R.string;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.engines.Settings;
import org.kreed.musicdownloader.interfaces.MusicDataInterface;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.adapter.AdapterHelper;
import org.kreed.musicdownloader.ui.adapter.LibraryPagerAdapter;
import org.kreed.musicdownloader.ui.adapter.AdapterHelper.ViewBuilder;

import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.SearchWithPages;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.cover.GrooveSharkCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.LastFmCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask.Size;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SearchTab {

	private final DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
	private static final Void[] NO_PARAMS = {};
	private static final String KEY_POSITION = "position.song.musicdownloader";// vanilla";
	private static SearchTab instance;
	private static List<Engine> engines;
	private static int playingPosition = -1;
	private static LibraryPagerAdapter parentAdapter;

	private Iterator<Engine> taskIterator;
	private AsyncTask<Void, Void, CustomStructure> getUrlTask;
	private AsyncTask<Void, Void, CustomStructure> getUrlTaskForPlayer;
	private SongSearchAdapter resultAdapter;
	private CoverLoaderTask coverLoader;

	private Activity activity;
	private LayoutInflater inflater;
	private View view;
	private TextView message;
	private View progress;
	private TextView searchField;

	private String currentName = null;
	private String searchString;
	private boolean searchStopped = true;

	public static int getPlayingPosition() {
		return playingPosition;
	}

	public static void setPlayingPosition(int playingPosition) {
		SearchTab.playingPosition = playingPosition;
	}

	public static final SearchTab getInstance(LayoutInflater inflater, Activity activity, LibraryPagerAdapter adapter) {
		if (null == instance) {
			instance = new SearchTab(inflater.inflate(R.layout.search, null), inflater, activity, adapter);
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
		for (int i = 0; i < engineArray.length; i++) {
			try {
				Class<? extends BaseSearchTask> engineClass = (Class<? extends BaseSearchTask>) Class.forName("ru.johnlife.lifetoolsmp3.engines." + engineArray[i][0]);
				int maxPages = Integer.parseInt(engineArray[i][1]);
				for (int page = 1; page <= maxPages; page++) {
					engines.add(new Engine(engineClass, page));
				}
			} catch (ClassNotFoundException e) {
				Log.e("SearchTab", "Unknown engine", e);
			}
		}
	}

	public static final View getInstanceView(LayoutInflater inflater, MainActivity activity, LibraryPagerAdapter adapter) {
		View instanceView = getInstance(inflater, activity, adapter).view;
		ViewGroup parent = (ViewGroup) instanceView.getParent();
		if (null != parent) {
			parent.removeView(instanceView);
		}
		return instanceView;
	}

	private static class DownloadClickListener implements AlertDialog.OnClickListener, OnBitmapReadyListener {
		private final Context context;
		private final String songTitle;
		private String songArtist;
		private Bitmap cover;
		private String downloadUrl;
		private String duration;
		private DownloadsTab downloadsTab;
		private double progress = 0.0;
		private String currentDownloadingSongTitle;
		private Long currentDownloadingID;
		private ArrayList<String[]> headers;

		private DownloadClickListener(Context context, String songTitle, String songArtist, String downloadUrl, String duration, Bitmap cover, ArrayList<String[]> headers) {
			this.context = context;
			this.songTitle = songTitle;
			this.songArtist = songArtist;
			this.downloadUrl = downloadUrl;
			this.duration = duration;
			this.cover = cover;
			this.headers = headers;
		}

		@SuppressLint("NewApi")
		public void downloadFile() {
			final File musicDir = new File(Environment.getExternalStorageDirectory() + Constans.DIRECTORY_PREFIX);
			if (!musicDir.exists()) {
				musicDir.mkdirs();
			}
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request;
			if (headers != null && headers.get(0) != null) {
				request = new DownloadManager.Request(Uri.parse(downloadUrl))
						.addRequestHeader(headers.get(0)[0],headers.get(0)[1])
						.addRequestHeader(
								"User-Agent",
								"2.0.0.6 â Debian GNU/Linux 4.0 — Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
			} else {
				request = new DownloadManager.Request(Uri.parse(downloadUrl))
						.addRequestHeader(
								"User-Agent",
								"2.0.0.6 â Debian GNU/Linux 4.0 — Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
			}
			final String fileName = songTitle + " - " + songArtist + ".mp3";
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle(songTitle)
					.setDestinationInExternalPublicDir(Constans.DIRECTORY_PREFIX, fileName);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
			}
			Toast.makeText(context, String.format(context.getString(R.string.download_started), fileName.replace(".mp3", "")), Toast.LENGTH_LONG).show();
			final long downloadId = manager.enqueue(request);
			downloadsTab = DownloadsTab.getInstance();
			InsertDownloadItem insertDownloadItem = new InsertDownloadItem(songTitle, songArtist, duration, downloadsTab, downloadId, cover);
			insertDownloadItem.insertData();

			final TimerTask progresUpdateTask = new TimerTask() {
				private File src;

				private void updateProgress() {
					instance.activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							downloadsTab.currentDownloadingID(currentDownloadingID);
							downloadsTab.currentDownloadingSongTitle(currentDownloadingSongTitle);
							downloadsTab.insertProgress(String.valueOf(progress));
						}
					});
				}

				@Override
				public void run() {
					if (downloadsTab.getCancelledId() == downloadId) {
						this.cancel();
					}
					Cursor cs = null;
					cs = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_RUNNING));
					if (cs.moveToFirst()) {
						int sizeIndex = cs.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
						int downloadedIndex = cs.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
						long size = cs.getInt(sizeIndex);
						long downloaded = cs.getInt(downloadedIndex);
						currentDownloadingID = downloadId;
						currentDownloadingSongTitle = cs.getString(cs.getColumnIndex(DownloadManager.COLUMN_TITLE));
						if (size != -1) {
							progress = downloaded * 100.0 / size;
						}
						cs.close();
						updateProgress();
					}

					Cursor completeCursor = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
					if (completeCursor.moveToFirst()) {
						if (currentDownloadingSongTitle.equalsIgnoreCase(completeCursor.getString(completeCursor.getColumnIndex(DownloadManager.COLUMN_TITLE)))) {
							progress = 100;
							updateProgress();
						}
					}
					completeCursor.close();
					if (!completeCursor.isClosed()) {
						completeCursor.close();
					}
					if (!cs.isClosed()) {
						cs.close();
					}
					// if (waitingForCover) {
					// return;
					// }
					Cursor c = null;
					c = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
					if (c == null || !c.moveToFirst()) {
						if (c != null) {
							c.close();
						}
						return;
					}
					int columnIndex = 0;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						columnIndex = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
					} else if (columnIndex != -1) {
						columnIndex = c.getColumnIndex("local_uri");
					}
					if (columnIndex == -1)
						return;
					String path = c.getString(columnIndex);
					c.close();
					if (!c.isClosed()) {
						c.close();
					}
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						path = cutPath(path);
					}
					src = new File(path);
					try {
						MusicData song = new MusicData();
						song.setSongArtist(songArtist);
						song.setSongTitle(songTitle);
						song.setSongDuration(duration);
						song.setSongBitmap(cover);
						MusicMetadataSet src_set = new MyID3().read(src); 
						if (src_set == null) {
							return;
						}
						MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
						metadata.clearPictureList();
						metadata.setSongTitle(songTitle + '/' + duration);
						metadata.setArtist(songArtist);
						if (null != cover) {
							ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
							cover.compress(CompressFormat.JPEG, 85, out);
							metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
						}
						File dst = new File(src.getParentFile(), src.getName() + "-1");
						new MyID3().write(src, dst, src_set, metadata); 
						dst.renameTo(src);
						progress = 100;
						updateProgress();
						song.setFileUri(dst.getAbsolutePath());
						notifyMediascanner(song);
						DBHelper.getInstance(context).insert(song);
						downloadsTab.setFileUri(dst.getAbsolutePath(), downloadId);
						this.cancel();
					} catch (IOException e) {
						Log.e(getClass().getSimpleName(), "error writing ID3", e);
					} catch (ID3WriteException e) {
						Log.e(getClass().getSimpleName(), "error writing ID3", e);
					}
				}

				private String cutPath(String s) {
					int index = s.indexOf('m');
					return s.substring(index - 1);
				}

				private void notifyMediascanner(final MusicData song) {
					File file = new File(Environment.getExternalStorageDirectory() + Constans.DIRECTORY_PREFIX);
					// Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED,
					// Uri.parse("file:/"+ file));
					// context.sendBroadcast(intent);
					MediaScannerConnection.scanFile(context, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

						public void onScanCompleted(String path, Uri uri) {
							parentAdapter.changeArrayMusicData(song);
						}
					});
				}
			};
			new Timer().schedule(progresUpdateTask, 1000, 1000);
		}

		@SuppressLint("NewApi")
		@Override
		public void onClick(DialogInterface dialog, int which) {

		}

		@Override
		public void onBitmapReady(Bitmap bmp) {
			this.cover = bmp;
		}
	}

	private class SongSearchItem {

		public Song song;
		public boolean isHighlight = false;

		public SongSearchItem(Song song) {
			this.song = song;
		}
	}

	private class CustomStructure {
		public String downloadUrl;
		public ArrayList<String[]> headers;
	}
	
	private final class SongSearchAdapter extends ArrayAdapter<SongSearchItem> {
		private LayoutInflater inflater;
		private FrameLayout footer;
		private ProgressBar refreshSpinner;
		private DownloadClickListener downloadClick;
		private Context context;
		private Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>(0);

		private SongSearchAdapter(Context context, LayoutInflater inflater) {
			super(context, -1, new ArrayList<SongSearchItem>());
			this.inflater = inflater;
			this.context = context;
			this.footer = new FrameLayout(context);
			this.refreshSpinner = new ProgressBar(context);
			refreshSpinner.setIndeterminate(true);
			footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
			refreshSpinner.setVisibility(View.GONE);
		}

		private String formatTime(long duration) {
			if (duration == 0)
				return null;
			duration /= 1000;
			int min = (int) duration / 60;
			int sec = (int) duration % 60;
			return String.format("%d:%02d", min, sec);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Song song = getItem(position).song;
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setButtonVisible(false).setLongClickable(false).setExpandable(false).setLine1(song.getTitle()).setLine2(song.getArtist()).setTime(formatTime(song.getDuration())).setId(position)
					.setIcon(R.drawable.fallback_cover);
			builder.getDownload().setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle(0);
					bundle.putInt(KEY_POSITION, position);
					final Song song = resultAdapter.getItem(position).song;
					Log.d("name", song.getArtist());
					getUrlTask = new AsyncTask<Void, Void, CustomStructure>() {
						@Override
						protected CustomStructure doInBackground(Void... params) {
							CustomStructure struct = new CustomStructure();
							struct.headers = ((RemoteSong) song).getHeaders();
							struct.downloadUrl = ((RemoteSong) song).getDownloadUrl();
							return struct;
						}

						@Override
						protected void onPostExecute(CustomStructure struct) {
							loadSong(struct.downloadUrl, struct.headers);
						}
					};
					try {
						RemoteSong remSong = (RemoteSong) song;
						String url = remSong.getParentUrl();
						if (url != null) {
							loadSong(url, remSong.getHeaders());
						} else {
							getUrlTask.execute(NO_PARAMS);
						}
					} catch (ClassCastException ex) {
						Log.e(getClass().getSimpleName(), ex.getMessage());
					}
				}

				private void loadSong(String url, ArrayList<String[]> headers) {
					Bitmap bmp = bitmaps.get(position);
					if (bmp != null) {
						downloadClick = new DownloadClickListener(context, song.getTitle(), song.getArtist(), url, formatDate(song.getDuration()), bitmaps.get(position), headers);
					} else {
						downloadClick = new DownloadClickListener(context, song.getTitle(), song.getArtist(), url, formatDate(song.getDuration()), null, headers);
						if (song instanceof GrooveSong) {
							String urlSmallImage = ((GrooveSong) song).getSmallCoverUrl();
							coverLoader = new GrooveSharkCoverLoaderTask(urlSmallImage);
						} else {
							coverLoader = new MuzicBrainzCoverLoaderTask(song.getArtist(), song.getTitle(), Size.small);
						}
						coverLoader.addListener(downloadClick);
						coverLoader.execute(NO_PARAMS);
					}
					downloadClick.downloadFile();
				}

			});

			if (bitmaps.containsKey(position) && bitmaps.get(position) != null) {
				builder.setIcon(bitmaps.get(position));
			} else {
				if (song instanceof GrooveSong) {
					String urlSmallImage = ((GrooveSong) song).getSmallCoverUrl();
					coverLoader = new GrooveSharkCoverLoaderTask(urlSmallImage);
				} else {
					coverLoader = new LastFmCoverLoaderTask(song.getArtist(), song.getTitle());
				}
				coverLoader.addListener(new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
						bitmaps.put(position, bmp);
						if (builder != null && builder.getId() == position && bmp != null) {
							builder.setIcon(bmp);
						}
					}
				});
			}

			if (coverLoader.getStatus() == Status.PENDING) {
				coverLoader.execute(NO_PARAMS);
			}

			if (position == getCount() - 1) {
				refreshSpinner.setVisibility(View.VISIBLE);
				getNextResults();
			}
			View v = builder.build();
			v.setBackgroundColor(getItem(position).isHighlight ? activity.getResources().getColor(R.color.holo_blue_light) : activity.getResources().getColor(android.R.color.transparent));
			return v;
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
					resultAdapter.add(new SongSearchItem(song));
				}
			}
		}
	};

	private final String formatDate(long date) {
		return isoDateFormat.format(new Date(date));
	}

	private SearchTab(final View instanceView, final LayoutInflater inflater, Activity libraryActivity, LibraryPagerAdapter adapter) {
		this.view = instanceView;
		this.inflater = inflater;
		this.activity = libraryActivity;
		parentAdapter = adapter;
		resultAdapter = new SongSearchAdapter(instanceView.getContext(), inflater);
		message = (TextView) instanceView.findViewById(R.id.message);
		progress = instanceView.findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		final ListView listView = (ListView) instanceView.findViewById(R.id.list);
		listView.addFooterView(resultAdapter.getProgress());
		listView.setAdapter(resultAdapter);
		listView.setEmptyView(message);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO
				if (!((MainActivity) activity).hasConnection()) {
					Toast.makeText(activity, R.string.check_intertet_access, 2000).show();
				}
				if (playingPosition != -1) {
					if (playingPosition == position)
						return;
				} else {// enter here first time
					playingPosition = position;
					Toast.makeText(activity, R.string.toast_loading, 2000).show();
					((MainActivity) activity).setActivatedPlayButton(false);
					final Song song = resultAdapter.getItem(position).song;
					final String artist = song.getTitle();
					final String title = song.getArtist();
					final String duration = formatTime((int) song.getDuration());
					((MainActivity) activity).setFooterView(artist + "-" + title);
					initTask(song, artist, title, duration, position);
					getUrlTaskForPlayer.execute(NO_PARAMS);
					for (int i = 0; i < resultAdapter.getCount(); i++) {
						resultAdapter.getItem(i).isHighlight = false;
						resultAdapter.notifyDataSetInvalidated();
					}
					resultAdapter.getItem(position).isHighlight = true;
					view.setBackgroundColor(activity.getResources().getColor(R.color.holo_blue_light));
					return;
				}
				if (position == resultAdapter.getCount())
					return; // progress click
				Toast.makeText(activity, R.string.toast_loading, 2000).show();
				((MainActivity) activity).setActivatedPlayButton(false);
				for (int i = 0; i < resultAdapter.getCount(); i++) {
					resultAdapter.getItem(i).isHighlight = false;
					resultAdapter.notifyDataSetInvalidated();
				}
				resultAdapter.getItem(position).isHighlight = true;
				view.setBackgroundColor(activity.getResources().getColor(R.color.holo_blue_light));
				final Song song = resultAdapter.getItem(position).song;
				final String artist = song.getTitle();
				final String title = song.getArtist();
				final String duration = formatTime((int) song.getDuration());
				((MainActivity) activity).setFooterView(artist + "-" + title);
				if (getUrlTaskForPlayer.getStatus() == Status.PENDING) {
					initTask(song, artist, title, duration, position);
					getUrlTaskForPlayer.execute(NO_PARAMS);
				} else {
					getUrlTaskForPlayer.cancel(true);
					initTask(song, artist, title, duration, position);
					((MainActivity) activity).resetPlayer();
					getUrlTaskForPlayer.execute(NO_PARAMS);
				}
			}

		});
		searchField = (TextView) instanceView.findViewById(R.id.firstLine);
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

	private void initTask(final Song song, final String artist, final String title, final String duration, final int pos) {
		final String key = Constans.CALL_FROM_SERCH;
		getUrlTaskForPlayer = new AsyncTask<Void, Void, CustomStructure>() {

			@Override
			protected CustomStructure doInBackground(Void... params) {
				CustomStructure structure = new CustomStructure();
				structure.headers = ((RemoteSong) song).getHeaders();
				structure.downloadUrl = ((RemoteSong) song).getDownloadUrl();
				return structure;
			}

			@Override
			protected void onPostExecute(CustomStructure structure) {
				playingPosition = pos;
				((MainActivity) activity).play(structure.downloadUrl, structure.headers, artist, title, duration, key, pos);
			}
			
		};
	}

	private String formatTime(int duration) {
		duration /= 1000;
		int min = duration / 60;
		int sec = duration % 60;
		return String.format("%d:%02d", min, sec);
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
			BaseSearchTask searchTask = engine.getEngineClass().getConstructor(BaseSearchTask.PARAMETER_TYPES).newInstance(new Object[] { resultsListener, currentName });
			if (searchTask instanceof SearchWithPages) {
				int page = engine.getPage();
				((SearchWithPages) searchTask).setPage(page);
			}
			searchTask.execute(NO_PARAMS);
		} catch (Exception e) {
			getNextResults();
		}
	}

	private static class InsertDownloadItem {
		private MusicDataInterface musicDataInterface;
		private String songTitle;
		private String songArtist;
		private String duration;
		private long downloadId;
		private Bitmap cover;

		public InsertDownloadItem(String songTitle, String songArtist, String formatTime, MusicDataInterface musicDataInterface, long downloadId, Bitmap cover) {
			this.songArtist = songArtist;
			this.songTitle = songTitle;
			this.duration = formatTime;
			this.musicDataInterface = musicDataInterface;
			this.downloadId = downloadId;
			this.cover = cover;
		}

		public void insertData() {
			ArrayList<MusicData> mData = new ArrayList<MusicData>();
			MusicData mItem = new MusicData();
			mItem.setSongArtist(songArtist);
			mItem.setSongTitle(songTitle);
			mItem.setSongDuration(String.valueOf(duration));
			mItem.setSongBitmap(cover);
			mItem.setDownloadId(downloadId);
			mItem.setDownloadProgress("0");
			mData.add(mItem);
			musicDataInterface.insertData(mData);
		}
	}
	
}
