package org.kreed.musicdownloader;

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
import org.kreed.musicdownloader.ui.AdapterHelper;
import org.kreed.musicdownloader.ui.AdapterHelper.ViewBuilder;

import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.cover.GrooveSharkCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.LastFmCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask.Size;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
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
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.musicdownloader";//vanilla";
	private static SearchTab instance;
	private static List<Class<? extends BaseSearchTask>> engines;
	private static LibraryPagerAdapter parentAdapter;
	private Iterator<Class<? extends BaseSearchTask>> taskIterator;
	private String currentName = null;
	private SongSearchAdapter resultAdapter;
	private TextView message;
	private String searchString;
	private View progress;
	private TextView searchField;
	
	private LayoutInflater inflater;
	private View view;
	private Activity activity;
	private CoverLoaderTask coverLoader;
	private AsyncTask<Void, Void, String> getUrlTask;
	private boolean searchStopped = true;

	@SuppressWarnings("unchecked")
	public static final SearchTab getInstance(LayoutInflater inflater, Activity activity, LibraryPagerAdapter adapter) {
		if (null == instance) {
			instance = new SearchTab(inflater.inflate(R.layout.search, null), inflater, activity, adapter);
			Context context = inflater.getContext();
			if (null == engines) {
				String[] engineNames = context.getResources().getStringArray(R.array.search_engines);
				engines = new ArrayList<Class<? extends BaseSearchTask>>(engineNames.length);
				for (int i=0; i<engineNames.length; i++) {
					try {
						engines.add((Class<? extends BaseSearchTask>) Class.forName("ru.johnlife.lifetoolsmp3.engines."+engineNames[i]));
					} catch (ClassNotFoundException e) {
						Log.e("SearchTab", "Unknown engine", e);
					}
				}
			}
		} else {
			instance.activity = activity;
		}
		return instance;
	}

	public static final View getInstanceView(LayoutInflater inflater, Activity activity, LibraryPagerAdapter adapter) {
		View instanceView = getInstance(inflater, activity, adapter).view;
		ViewGroup parent = (ViewGroup)instanceView.getParent();
		if (null != parent) {
			parent.removeView(instanceView);
		}
		return instanceView;
	}

	private static class DownloadClickListener implements AlertDialog.OnClickListener, OnBitmapReadyListener {
		private final Context context;
		private final String songTitle;
		private String songArtist;
		private String songPathSD;
		private Bitmap cover;
		private boolean waitingForCover = true;
		private String downloadUrl;
		private String duration;
		private DownloadsTab downloadsTab;
		private double progress = 0.0;
		private String currentDownloadingSongTitle;
		private Long currentDownloadingID;

		private DownloadClickListener(Context context, String songTitle, String songArtist, String downloadUrl, 
				String duration, Bitmap cover) {
			this.context = context;
			this.songTitle = songTitle;
			this.songArtist = songArtist;
			this.downloadUrl = downloadUrl;
			this.duration = duration;
			this.cover = cover;
		}

		@SuppressLint("NewApi")
		public void downloadFile() {
			final File musicDir = new File(Environment.getExternalStorageDirectory() + PrefKeys.DIRECTORY_PREFIX);
			if (!musicDir.exists()) {
				musicDir.mkdirs();
			}
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl))
					.addRequestHeader(
							"User-Agent",
							"2.0.0.6 â Debian GNU/Linux 4.0 — Mozilla/5.0 (X11; U; Linux i686 (x86_64); en-US; rv:1.8.1.6) Gecko/2007072300 Iceweasel/2.0.0.6 (Debian-2.0.0.6-0etch1+lenny1)");
			final String fileName = songTitle + " - " + songArtist + ".mp3";
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
					.setAllowedOverRoaming(false).setTitle(songTitle).setDestinationInExternalPublicDir(PrefKeys.DIRECTORY_PREFIX, fileName);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.allowScanningByMediaScanner();
			}
			Toast.makeText(context, String.format(context.getString(R.string.download_started), fileName.replace(".mp3", "")), Toast.LENGTH_LONG).show();
			final long downloadId = manager.enqueue(request);
			downloadsTab = DownloadsTab.getInstance();
			InsertDownloadItem insertDownloadItem = new InsertDownloadItem(songTitle, 
					songArtist,duration, downloadsTab, downloadId, cover);
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
						currentDownloadingSongTitle= cs.getString(cs.getColumnIndex(DownloadManager.COLUMN_TITLE));
						if (size != -1) {
							progress = downloaded * 100.0 / size;
						}
						cs.close();
						updateProgress();
					}
					
					Cursor completeCursor = manager.query(new DownloadManager.Query()
					.setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
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
//					if (waitingForCover) {
//						return;
//					}
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
					if (columnIndex == -1) return;
					String path = c.getString(columnIndex);
					c.close();
					if(!c.isClosed()) {
						c.close();
					}					
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
						path = cutPath(path);
					}
					src = new File(path);
					try {
						MusicMetadataSet src_set = new MyID3().read(src); // read metadata
						if (src_set == null) {
							return;
						}
						MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
						metadata.setSongTitle(songTitle + '/' + duration);
						metadata.setArtist(songArtist);
//						metadata.clearComposer2();
//						metadata.setComposer2(duration);//this is reading duration into metadata
						if (null != cover) {
							ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
							cover.compress(CompressFormat.JPEG, 85, out);
							metadata.addPicture(
								new ImageData(out.toByteArray(), "image/jpeg", "cover", 3)
							);
						}
						File dst = new File(src.getParentFile(), src.getName()+ "-1");
						new MyID3().write(src, dst, src_set, metadata);  // write updated metadata
						dst.renameTo(src);
						notifyMediascanner();
						progress = 100;
						updateProgress();
						MusicData song = new MusicData();
						song.setSongArtist(songArtist);
						song.setSongTitle(songTitle);
						song.setSongDuration(duration);
						song.setFileUri(dst.getPath());
						DBHelper.getInstance(context).insert(song);
						downloadsTab.setFileUri(dst.getPath(), downloadId);
						songPathSD = dst.getAbsolutePath();
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

			 private void notifyMediascanner() {
				 File file = new File(Environment.getExternalStorageDirectory() + PrefKeys.DIRECTORY_PREFIX);
				 Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, 
						 Uri.parse("file:/"+ file));
				 context.sendBroadcast(intent);
				 MediaScannerConnection.scanFile(context,
						 new String[] { file.getAbsolutePath() }, null,
						 new MediaScannerConnection.OnScanCompletedListener() {
			 
			 public void onScanCompleted(String path, Uri uri) {
				 MusicData song = new MusicData();
					song.setSongArtist(songArtist);
					song.setSongTitle(songTitle);
					song.setSongDuration(duration);
					song.setSongBitmap(cover);
					song.setFilePathSD(songPathSD);
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
//			DownloadFile downloadFile = new DownloadFile(songTitle, songArtist,player.formatTime(player.mediaPlayer.getDuration()), dt,dt);
//			downloadFile.execute(downloadUrl);

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
		private DownloadClickListener downloadClick;
		private Context context;
		private Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>(0);

		private SongSearchAdapter(Context context, LayoutInflater inflater) {
			super(context, -1, new ArrayList<Song>());
			this.inflater = inflater;
			this.context = context;
			this.footer = new FrameLayout(context);
    		this.refreshSpinner = new ProgressBar(context);
    		refreshSpinner.setIndeterminate(true);
    		footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
    		refreshSpinner.setVisibility(View.GONE);
		}
		
		private String formatTime(long duration) {
			if (duration == 0) return null;
			duration /= 1000;
			int min = (int)duration / 60;
			int sec = (int)duration % 60;
			return String.format("%d:%02d", min, sec);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Song song = getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setButtonVisible(false).setLongClickable(false).setExpandable(false).setLine1(song.getTitle()).setLine2(song.getArtist()).setTime(formatTime(song.getDuration()))
			// .setNumber(String.valueOf(position+1), 0)
					.setId(position).setIcon(R.drawable.fallback_cover);
				builder.getDownload().setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle(0);
					bundle.putInt(KEY_POSITION, position);
					final Song song = resultAdapter.getItem(position);
					Log.d("name", song.getArtist());
					getUrlTask = new AsyncTask<Void, Void, String>() {
						@Override
						protected String doInBackground(Void... params) {
							return ((RemoteSong) song).getDownloadUrl();
						}

						@Override
						protected void onPostExecute(String downloadUrl) {
							loadSong(downloadUrl);
						}
					};
					try {
						RemoteSong remSong = (RemoteSong) song;
						String url = remSong.getParentUrl();
						if (url != null) {
							loadSong(url);
						} else {
							getUrlTask.execute(NO_PARAMS);
						}
					} catch (ClassCastException ex) {
						Log.e(getClass().getSimpleName(), ex.getMessage());
					}
				}
				
				private void loadSong(String url) {
					Bitmap bmp = bitmaps.get(position);
					if (bmp != null) {
						downloadClick = new DownloadClickListener(context, song.getTitle(), song.getArtist(), url, 
								formatDate(song.getDuration()), bitmaps.get(position));
					} else {
						downloadClick = new DownloadClickListener(context, song.getTitle(), song.getArtist(), url, 
								formatDate(song.getDuration()), null);
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
				
			if (position == getCount()-1) {
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
			if(searchStopped) return; 
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
		ListView listView = (ListView) instanceView.findViewById(R.id.list);
		listView.addFooterView(resultAdapter.getProgress());
		listView.setAdapter(resultAdapter);
		listView.setEmptyView(message);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == resultAdapter.getCount()) return; //progress click
				final Song song = resultAdapter.getItem(position);
				final String artist = song.getTitle();
				final String title = song.getArtist();
				final String duration = formatTime((int)song.getDuration());
				final  String key = PrefKeys.CALL_FROM_SERCH;
				getUrlTask = new AsyncTask<Void, Void, String>() {
					@Override
					protected String doInBackground(Void... params) {
						return ((RemoteSong) song).getDownloadUrl();
					}

					@Override
					protected void onPostExecute(String downloadUrl) {
						((MainActivity) activity).play(downloadUrl, artist, title, duration, key);
					}
				};
				getUrlTask.execute(NO_PARAMS);

			}
			
		});
		searchField = (TextView)instanceView.findViewById(R.id.text);
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
		InputMethodManager imm = (InputMethodManager)searchField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
		Class<? extends BaseSearchTask> engineClass = taskIterator.next();
		BaseSearchTask engine;
		try {
			engine = engineClass.getConstructor(BaseSearchTask.PARAMETER_TYPES).newInstance(new Object[]{resultsListener, currentName});
		} catch (Exception e) {
			getNextResults();
			return;
		}
		engine.execute(NO_PARAMS);
	}
	
	private static class InsertDownloadItem {
		private MusicDataInterface musicDataInterface;
		private String songTitle;
		private String songArtist;
		private String duration;
		private long downloadId;
		private Bitmap cover;

		public InsertDownloadItem(String songTitle, String songArtist,
				String formatTime, MusicDataInterface musicDataInterface, long downloadId, Bitmap cover) {
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

		public Bitmap getCover() {
			return cover;
		}

		public void setCover(Bitmap cover) {
			this.cover = cover;
		}
	}
}
