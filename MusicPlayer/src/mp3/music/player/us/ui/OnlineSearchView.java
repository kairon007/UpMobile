package mp3.music.player.us.ui;

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

import mp3.music.player.us.R;
import mp3.music.player.us.adapters.AdapterHelper;
import mp3.music.player.us.adapters.AdapterHelper.ViewBuilder;
import mp3.music.player.us.engines.BaseSearchTask;
import mp3.music.player.us.engines.FinishedParsingSongs;
import mp3.music.player.us.engines.GrooveSong;
import mp3.music.player.us.engines.OnlineSong;
import mp3.music.player.us.engines.RemoteSong;
import mp3.music.player.us.engines.cover.CoverLoaderTask;
import mp3.music.player.us.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import mp3.music.player.us.engines.cover.GrooveSharkCoverLoaderTask;
import mp3.music.player.us.engines.cover.MuzicBrainzCoverLoaderTask;
import mp3.music.player.us.engines.cover.MuzicBrainzCoverLoaderTask.Size;
import mp3.music.player.us.ui.activities.HomeActivity;
import mp3.music.player.us.ui.fragments.phone.MusicBrowserPhoneFragment;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaScannerConnection;
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

public class OnlineSearchView {
	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.music.player";
	public static final String ACTION_PAUSE = "mp3.music.player.us.action.PAUSE";
	private static OnlineSearchView instance;
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
	private HomeActivity activity;
	private Player player;
	private CoverLoaderTask coverLoader;
	private AsyncTask<Void, Void, String> getUrlTask;
	private boolean searchStopped = true;
	public static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	public static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";

	@SuppressWarnings("unchecked")
	public static final OnlineSearchView getInstance(LayoutInflater inflater, HomeActivity activity) {
		if (null == instance) {
			instance = new OnlineSearchView(inflater.inflate(R.layout.search, null), inflater, activity);
			Context context = inflater.getContext();
			if (null == engines) {
				String[] engineNames = context.getResources().getStringArray(R.array.search_engines);
				engines = new ArrayList<Class<? extends BaseSearchTask>>(engineNames.length);
				for (int i = 0; i < engineNames.length; i++) {
					try {
						engines.add((Class<? extends BaseSearchTask>) Class.forName("mp3.music.player.us.engines." + engineNames[i]));
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

	public static String getDownloadPath(Context context) {
		SharedPreferences downloadDetails = context.getSharedPreferences(OnlineSearchView.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
		String downloadPath = downloadDetails.getString(OnlineSearchView.DOWNLOAD_DIR, "");
		if (downloadPath.equals("")) {
			downloadPath = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_MUSIC;
			Editor edit = downloadDetails.edit();
			edit.clear();
			edit.putString(OnlineSearchView.DOWNLOAD_DIR, downloadPath);
			edit.commit();
		}
		return downloadPath;
	}

	public static String getSimpleDownloadPath(String absPath) {
		return absPath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
	}

	public static void setDownloadPath(Context context, String downloadPath) {
		SharedPreferences downloadDetails = context.getSharedPreferences(OnlineSearchView.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
		Editor edit = downloadDetails.edit();
		edit.clear();
		edit.putString(OnlineSearchView.DOWNLOAD_DIR, downloadPath);
		edit.commit();
	}

	public static final View getInstanceView(LayoutInflater inflater, HomeActivity activity) {
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
			if (downloadUrl == null || downloadUrl.equals("")) {
				Toast.makeText(context, R.string.download_error, Toast.LENGTH_SHORT).show();
				return;
			}
			player.cancel();

			final File musicDir = new File(getDownloadPath(context));
			if (!musicDir.exists()) {
				musicDir.mkdirs();
			}
			final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
			StringBuilder sb = new StringBuilder(songTitle).append(" - ").append(songArtist);
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
					if (waitingForCover) {
						return;
					}
					Cursor c = manager.query(new DownloadManager.Query().setFilterById(downloadId).setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL));
					if (c == null || !c.moveToFirst()) return;						
					String path = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
					c.close();
					src = new File(path);
					try {
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
						notifyMediascanner();
						this.cancel();
					} catch (IOException e) {
						Log.e(getClass().getSimpleName(), "error writing ID3", e);
					} catch (ID3WriteException e) {
						Log.e(getClass().getSimpleName(), "error writing ID3", e);
					}
				}

				 private void notifyMediascanner() {
				 Uri uri = Uri.fromFile(src.getParentFile());
				 Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, uri);
				 Log.d("logd", "notifyMediascanner");
				 context.sendBroadcast(intent);
				 MediaScannerConnection.scanFile(context, new String[] {getDownloadPath(context)}, null,
						 new MediaScannerConnection.OnScanCompletedListener() {
					 		public void onScanCompleted(String path, Uri uri) {
					 			Log.i("logd", "Finished scanning " + path);
								Intent intent = new Intent(MusicBrowserPhoneFragment.ACTION_UPDATE);
								context.sendBroadcast(intent);
								Log.d("logd", "broadcast sended");
					 		}
				 		});
				 }
			};
			new Timer().schedule(progresUpdateTask, 1000, 1000);
		}

		@Override
		public void onBitmapReady(Bitmap bmp) {
			this.cover = bmp;
			this.waitingForCover = false;
		}
	}

	private final class SongSearchAdapter extends ArrayAdapter<OnlineSong> {
		private LayoutInflater inflater;
		private FrameLayout footer;
		private ProgressBar refreshSpinner;
		private Map<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>(0);

		private SongSearchAdapter(Context context, LayoutInflater inflater) {
			super(context, -1, new ArrayList<OnlineSong>());
			this.inflater = inflater;
			this.footer = new FrameLayout(context);
			this.refreshSpinner = new ProgressBar(context);
			refreshSpinner.setIndeterminate(true);
			footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
			refreshSpinner.setVisibility(View.GONE);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			OnlineSong song = getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setButtonVisible(false).setLongClickable(false).setExpandable(false).setLine1(song.getTitle()).setLine2(song.getArtist())
			// .setNumber(String.valueOf(position+1), 0)
					.setId(position).setIcon(R.drawable.fallback_cover);
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
		public void onFinishParsing(List<OnlineSong> songsList) {
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
				for (OnlineSong song : songsList) {
					resultAdapter.add(song);
				}
			}
		}
	};

	public OnlineSearchView(final View instanceView, final LayoutInflater inflater, HomeActivity homeActivity) {
		this.view = instanceView;
		this.inflater = inflater;
		this.activity = homeActivity;

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
				if (position == resultAdapter.getCount())
					return; // progress click
				Bundle bundle = new Bundle(0);
				bundle.putInt(KEY_POSITION, position);
				activity.showDialog(STREAM_DIALOG_ID, bundle);
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
				progress.setVisibility(View.GONE);
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
			engine = engineClass.getConstructor(BaseSearchTask.PARAMETER_TYPES).newInstance(new Object[] { resultsListener, currentName, activity });
		} catch (Exception e) {
			getNextResults();
			return;
		}
		engine.execute(NO_PARAMS);
	}

	@SuppressLint("NewApi")
	public Dialog createStreamDialog(Bundle args) {
		if (!(args.containsKey(KEY_POSITION))) {
			return null;
		}
		final OnlineSong song = resultAdapter.getItem(args.getInt(KEY_POSITION));
		final String artist = song.getTitle();
		final String title = song.getArtist();

		if (null == player) {
			player = new Player(inflater.inflate(R.layout.download_dialog, null));
			getUrlTask = new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					return ((RemoteSong) song).getDownloadUrl();
				}

				@Override
				protected void onPostExecute(String downloadUrl) {
					if (null != player) {
						player.setDownloadUrl(downloadUrl);
						player.execute();
					}
				}
			};
			getUrlTask.execute(NO_PARAMS);
			if (song instanceof GrooveSong) {
				String urlLargeImage = ((GrooveSong) song).getUrlLargeImage();
				coverLoader = new GrooveSharkCoverLoaderTask(urlLargeImage);
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
//				coverLoader.cancel(true);
				activity.removeDialog(STREAM_DIALOG_ID);
			}
		};
		DownloadClickListener downloadClickListener = new DownloadClickListener(context, title, artist, player) {
			@Override
			public void onClick(View v) {
				super.onClick(v);
				if (v.getTag() != null) {
					Object tag = v.getTag();
					if (tag instanceof AlertDialog) {
						((AlertDialog) tag).cancel();
					}
				}
				dialogDismisser.run();
			}
		};
		coverLoader.addListener(downloadClickListener);

		final View downLoadDialog = inflater.inflate(R.layout.download_dialog_chosen, null);
		final TextView textPath = (TextView) downLoadDialog.findViewById(R.id.text_path_download);
		textPath.setText(getDownloadPath(context));
		LinearLayout viewChooser = (LinearLayout) downLoadDialog.findViewById(R.id.path_download);
		Button startDownload = (Button) downLoadDialog.findViewById(R.id.b_download);
		startDownload.setOnClickListener(downloadClickListener);
		AlertDialog.Builder showDownLoadOtionsBuilder = new AlertDialog.Builder(context);

		showDownLoadOtionsBuilder.setView(downLoadDialog);
		final AlertDialog aDialogDownLoadOtions = showDownLoadOtionsBuilder.create();
		startDownload.setTag(aDialogDownLoadOtions);
		viewChooser.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(v.getContext(), new DirectoryChooserDialog.ChosenDirectoryListener() {
					@Override
					public void onChosenDir(String chosenDir) {
						textPath.setText(chosenDir);
						OnlineSearchView.setDownloadPath(context, chosenDir);
					}
				});
				directoryChooserDialog.chooseDirectory(OnlineSearchView.getDownloadPath(context));
			}
		});

		AlertDialog.Builder b = new AlertDialog.Builder(context).setTitle(title + " - " + artist).setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogDismisser.run();
			}
		}).setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDismisser.run();
			}
		}).setPositiveButton(R.string.download,
		//
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						aDialogDownLoadOtions.show();
					}
				}).setView(player.getView());
		return b.create();
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
			Intent i = new Intent(ACTION_PAUSE);
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
