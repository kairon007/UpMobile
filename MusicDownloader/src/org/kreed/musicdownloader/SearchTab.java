package org.kreed.musicdownloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.musicdownloader.engines.BaseSearchTask;
import org.kreed.musicdownloader.engines.FinishedParsingSongs;
import org.kreed.musicdownloader.engines.GrooveSong;
import org.kreed.musicdownloader.engines.RemoteSong;
import org.kreed.musicdownloader.engines.cover.CoverLoaderTask;
import org.kreed.musicdownloader.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import org.kreed.musicdownloader.engines.cover.GrooveSharkCoverLoaderTask;
import org.kreed.musicdownloader.engines.cover.MuzicBrainzCoverLoaderTask;
import org.kreed.musicdownloader.engines.cover.MuzicBrainzCoverLoaderTask.Size;
import org.kreed.musicdownloader.ui.AdapterHelper;
import org.kreed.musicdownloader.ui.AdapterHelper.ViewBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SearchTab {
	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.musicdownloader";//vanilla";
	private static SearchTab instance;
	private static List<Class<? extends BaseSearchTask>> engines;
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
	private Player player;
	private CoverLoaderTask coverLoader;
	private AsyncTask<Void, Void, String> getUrlTask;
	private boolean searchStopped = true;

	@SuppressWarnings("unchecked")
	public static final SearchTab getInstance(LayoutInflater inflater, Activity activity) {
		if (null == instance) {
			instance = new SearchTab(inflater.inflate(R.layout.search, null), inflater, activity);
			Context context = inflater.getContext();
			if (null == engines) {
				String[] engineNames = context.getResources().getStringArray(R.array.search_engines);
				engines = new ArrayList<Class<? extends BaseSearchTask>>(engineNames.length);
				for (int i=0; i<engineNames.length; i++) {
					try {
						engines.add((Class<? extends BaseSearchTask>) Class.forName("org.kreed.musicdownloader.engines."+engineNames[i]));
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

	public static final View getInstanceView(LayoutInflater inflater, Activity activity) {
		View instanceView = getInstance(inflater, activity).view;
		ViewGroup parent = (ViewGroup)instanceView.getParent();
		if (null != parent) {
			parent.removeView(instanceView);
		}
		return instanceView;
	}

	private static class DownloadClickListener implements AlertDialog.OnClickListener, OnBitmapReadyListener,LoadPercentageInterface, MusicDataInterface {
		private final Context context;
		private final String songTitle;
		private String songArtist;
		private Bitmap cover;
		private boolean waitingForCover = true;
		private String downloadUrl;
		private String duration;

		private DownloadClickListener(Context context, String songTitle, String songArtist, String downloadUrl, String duration) {
			this.context = context;
			this.songTitle = songTitle;
			this.songArtist = songArtist;
			this.downloadUrl = downloadUrl;
			this.duration = duration;
		}
		@SuppressLint("NewApi") public void downloadFile() {
			final File musicDir = new File(Environment.getExternalStorageDirectory()
	                + "/MusicDownloader");
			if (!musicDir.exists()) {
				musicDir.mkdirs();
			}
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
			final String fileName = songTitle+" - "+songArtist+".mp3";
			request.
				setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE).
				setAllowedOverRoaming(false).
				setTitle(songTitle).
				setDestinationInExternalPublicDir("/MusicDownloader/", fileName);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {	
				request.allowScanningByMediaScanner();
			}
			final long downloadId = manager.enqueue(request);
			final DownloadsTab downloadsTab = DownloadsTab.getInstance();
			InsertDownloadItem insertDownloadItem = new InsertDownloadItem(songTitle, 
					songArtist,duration, downloadsTab, downloadId);
			insertDownloadItem.insertData();
			GetCurrentProgress getCurrentProgress = new GetCurrentProgress(manager, downloadId, downloadsTab);
			getCurrentProgress.execute();

			final TimerTask progresUpdateTask = new TimerTask() {
				private File src;
				
				@Override
				public void run() {
					if (downloadsTab.getCancelledId() == downloadId) {
						Log.d("logd", "cancel");
						this.cancel();
					}
					if (waitingForCover) {
						Log.d("logd", "waitingForCover");
//						return;
					}
					Cursor c = null;
					c = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
					if (c == null || !c.moveToFirst()) {
						if (c != null) {
							c.close();
							Log.d("logd", "c.close();");
						}
						return;
					}
					String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
					c.close();
					if(!c.isClosed()) {
						c.close();
					}
					src = new File(path);
					try {
						MusicMetadataSet src_set = new MyID3().read(src); // read metadata
						if (src_set == null) {
							return;
						}
						MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
						metadata.setSongTitle(songTitle);
						metadata.setArtist(songArtist);
						metadata.clearComment();
						metadata.setComment(duration);//this is reading duration into metadata
						if (null != cover) {
							ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
							cover.compress(CompressFormat.JPEG, 85, out);
							metadata.addPicture(
								new ImageData(out.toByteArray(), "image/jpeg", "cover", 3)
							);
						}
						File dst = new File(src.getParentFile(), src.getName()+"-1");
						new MyID3().write(src, dst, src_set, metadata);  // write updated metadata
						dst.renameTo(src);
						this.cancel();
					} catch (IOException e) {
						Log.e(getClass().getSimpleName(), "error writing ID3", e);
					} catch (ID3WriteException e) {
						Log.e(getClass().getSimpleName(), "error writing ID3", e);
					}
				}
//				private void notifyMediascanner() {
//					Uri uri = Uri.fromFile(src.getParentFile());
//					Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, uri);
//					context.sendBroadcast(intent);
//					MediaScannerConnection.scanFile(context,
//						new String[] { dst.getAbsolutePath() }, null,
//						new MediaScannerConnection.OnScanCompletedListener() {
//							public void onScanCompleted(String path, Uri uri) {
//								Log.i("TAG", "Finished scanning " + path);
//							}
//						});
//				}
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

		@Override
		public void insertProgress(String progress) {
		}

		@Override
		public void insertData(ArrayList<MusicData> musicDatas) {
		}

		@Override
		public void insertCover(Bitmap cover) {
		}
	}

	private final class SongSearchAdapter extends ArrayAdapter<Song> {
		private LayoutInflater inflater;
		private FrameLayout footer;
		private ProgressBar refreshSpinner;
		private DownloadClickListener downloadClick;
		Context context;
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

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Song song = getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setButtonVisible(false).setLongClickable(false).setExpandable(false).setLine1(song.getTitle()).setLine2(song.getArtist())
			// .setNumber(String.valueOf(position+1), 0)
					.setId(position).setIcon(R.drawable.fallback_cover);
				builder.download.setOnClickListener(new OnClickListener() {
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
							downloadClick = new DownloadClickListener(context, song.getTitle(), song.getArtist(), downloadUrl, "10");
							downloadClick.downloadFile();
							}
						};
						getUrlTask.execute(NO_PARAMS);
						
					}
				});
			if (song instanceof GrooveSong) {
				if (bitmaps.containsKey(position)) {
					builder.setIcon(bitmaps.get(position));
				} else {
					String urlSmallImage = ((GrooveSong) song).getUrlSmallImage();
					CoverLoaderTask coverLoader = new GrooveSharkCoverLoaderTask(urlSmallImage);
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

	private SearchTab(final View instanceView, final LayoutInflater inflater, Activity libraryActivity) {
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

			@SuppressWarnings("deprecation")
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == resultAdapter.getCount()) return; //progress click
				Bundle bundle = new Bundle(0);
				bundle.putInt(KEY_POSITION, position);
				activity.showDialog(STREAM_DIALOG_ID, bundle);
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
	
//	@SuppressLint("NewApi")
//	public Dialog createStreamDialog(Bundle args) {
//		if(!(args.containsKey(KEY_POSITION))) {
//			return null;
//		}
//		final Song song = resultAdapter.getItem(args.getInt(KEY_POSITION));
//		final String artist = song.getTitle();
//		final String title = song.getArtist();
//		if (null == player) {
//			player = new Player(inflater.inflate(R.layout.download_dialog, null));
//			getUrlTask = new AsyncTask<Void, Void, String>() {
//				@Override
//				protected String doInBackground(Void... params) {
//					return ((RemoteSong) song).getDownloadUrl();
//				}
//				@Override
//				protected void onPostExecute(String downloadUrl) {
//					if(null != player) {
//						player.setDownloadUrl(downloadUrl);
//						player.execute();
//					}
//				}	
//			};
//			
//			getUrlTask.execute(NO_PARAMS);
//			if (song instanceof GrooveSong) {
//				String urlLargeImage = ((GrooveSong) song).getUrlLargeImage();
//				coverLoader = new GrooveSharkCoverLoaderTask(urlLargeImage);
//			} else {
//				coverLoader = new MuzicBrainzCoverLoaderTask(artist, title, Size.large);
//			}
//			coverLoader.addListener(new OnBitmapReadyListener() {
//				@Override
//				public void onBitmapReady(Bitmap bmp) {
//					if (null != player) {
//						player.setCover(bmp);
//					}
//				}
//			});
//			coverLoader.execute(NO_PARAMS);
//		} 
//		final String duration = player.formatTime(player.mediaPlayer.getDuration());
//		final Context context = view.getContext();
//		final Runnable dialogDismisser = new Runnable() {
//			@Override
//			public void run() {
//				if(player != null) {
//					player.cancel();
//					player = null;
//				}
//				getUrlTask.cancel(true);
////				coverLoader.cancel(true);
//				activity.removeDialog(STREAM_DIALOG_ID);
//			}
//		};	
////		DownloadClickListener downloadClickListener = new DownloadClickListener(context, title, artist, player,duration) {
////			@Override
////			public void onClick(DialogInterface dialog, int which) {
////				super.onClick(dialog, which);
////				dialogDismisser.run();
////			}
////		};
////		coverLoader.addListener(downloadClickListener);
//		AlertDialog.Builder b = new AlertDialog.Builder(context)
//			.setTitle(title)
//			.setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialogDismisser.run();
//				}
//			})
//			.setOnCancelListener(new OnCancelListener() {						
//				@Override
//				public void onCancel(DialogInterface dialog) {
//					dialogDismisser.run();
//				}
//			})
//			.setPositiveButton(
//				R.string.download, 
//				downloadClickListener
//			)
//			.setView(player.getView());
//		return b.create();
//	}
	
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
		private View view;
		
		public Player(View view) {
			super();
			this.view = view;
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			spinner = (ProgressBar) view.findViewById(R.id.spinner);
			button = (ImageButton) view.findViewById(R.id.pause);
			progress = (ProgressBar) view.findViewById(R.id.progress);
			time = (TextView) view.findViewById(R.id.time);
			coverImage = (ImageView) view.findViewById(R.id.cover);
			coverProgress = (ProgressBar) view.findViewById(R.id.coverProgress);
			button.setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					playPause();
				}
			});
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

		public View getView() {
			ViewGroup parent = (ViewGroup) view.getParent();
			Log.d(getClass().getSimpleName(), "getView()");
			if (null != parent) {
				parent.removeView(view);
				Log.d(getClass().getSimpleName(), "...removed from parent");
			}
			return view;
		}

		private Runnable progressAction = new Runnable() {
			@Override
			public void run() {
				try {
					int current = mediaPlayer.getCurrentPosition();
					int total = mediaPlayer.getDuration();
					progress.setProgress(current);
					time.setText(formatTime(current)+" / "+formatTime(total));
					progress.postDelayed(this, 1000);
				} catch (NullPointerException e) {
					//terminate
				}
			}
		};
		
		public void onPrepared() {
			spinner.setVisibility(View.GONE);
			button.setVisibility(View.VISIBLE);
			Intent i = new Intent("org.kreed.musicdownloader.action.PAUSE");
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
		
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				mediaPlayer.setDataSource(url);
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
					//do nothing
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
			if (!prepared || null == mediaPlayer) return;
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				onPaused();
			} else {
				mediaPlayer.start();
				onResumed();
			}
		}
		
	}
	
	
	private static class InsertDownloadItem {
		private MusicDataInterface musicDataInterface;
		private String songTitle;
		private String songArtist;
		private String duration;
		private long downloadId;

		public InsertDownloadItem(String songTitle, String songArtist,
				String formatTime, MusicDataInterface musicDataInterface, long downloadId) {
			// TODO Auto-generated constructor stub
			this.songArtist = songArtist;
			this.songTitle = songTitle;
			this.duration = formatTime;
			this.musicDataInterface = musicDataInterface;
			this.downloadId = downloadId;
		}

		public void insertData() {
			ArrayList<MusicData> mData = new ArrayList<MusicData>();
			MusicData mItem = new MusicData();
			mItem.setSongArtist(songArtist);
			mItem.setSongTitle(songTitle);
			mItem.setSongDuration(String.valueOf(duration));
			// mItem.setSongBitmap(BitmapFactory.decodeResource(getc, id));
			mItem.setDownloadId(downloadId);
			mItem.setDownloadProgress("0");
			mData.add(mItem);
			musicDataInterface.insertData(mData);
		}
	}
	private static class GetCurrentProgress extends AsyncTask<Integer, Integer, Integer> implements OnBitmapReadyListener {
		private double progress = 0.0;
		private int currentProgress;
		private DownloadManager manager;
		long downloadId;
		private Bitmap cover;
		private boolean waitingForCover = false;
		private LoadPercentageInterface loadPercentageInterface;
		
		public GetCurrentProgress(DownloadManager manager, long downloadId, LoadPercentageInterface loadPercentageInterface) {
			this.manager = manager;
			this.downloadId = downloadId;
			this.loadPercentageInterface = loadPercentageInterface;
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			Cursor c = null;
			while (currentProgress != 100) {
				c = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_RUNNING));
				if (c.moveToFirst()) {
				  int sizeIndex = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
				  int downloadedIndex = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
				  long size = c.getInt(sizeIndex);
				  long downloaded = c.getInt(downloadedIndex);
				  if (size != -1) progress = downloaded*100.0/size; 
				  c.close();
					if (!c.isClosed()) {
						c.close();
					}
				  publishProgress((int) progress);
				}
				currentProgress = (int) progress;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			loadPercentageInterface.insertProgress(String.valueOf(progress[0]));
			if (waitingForCover) {
				loadPercentageInterface.insertCover(cover);
			}
		}

		@Override
		public void onBitmapReady(Bitmap bmp) {
			// TODO Auto-generated method stub
			this.cover = bmp;
			this.waitingForCover = true;
		}
	}
}
