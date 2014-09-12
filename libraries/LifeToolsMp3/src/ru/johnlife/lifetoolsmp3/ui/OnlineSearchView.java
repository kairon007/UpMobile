package ru.johnlife.lifetoolsmp3.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper.ViewBuilder;
import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.SearchWithPages;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.cover.LastFmCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadUrlGetterTask;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.song.SongWithCover;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
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
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
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

public abstract class OnlineSearchView extends View {
	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.vanilla";
	private static List<Engine> engines = null;
	private Iterator<Engine> taskIterator;
	private String currentName = null;
	private SongSearchAdapter resultAdapter;
	private TextView message;
	private View progress;
	private TextView searchField;
	private View view;
	private Player player;
	private CoverLoaderTask coverLoader;
	private boolean searchStopped = true;
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";
	private ListView listView;


	protected abstract BaseSettings getSettings();
	protected abstract Advertisment getAdvertisment();
	
	public void initSearchEngines(Context context) {
		if (null != engines) return;
		String[][] engineArray = getSettings().getSearchEnginesArray(context);
		engines = new ArrayList<Engine>(engineArray.length);
		for (int i=0; i<engineArray.length; i++) {
			try {
				Class<? extends BaseSearchTask> engineClass = 
						(Class<? extends BaseSearchTask>) Class.forName("ru.johnlife.lifetoolsmp3.engines." + engineArray[i][0]);
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
			SharedPreferences downloadDetails = context.getSharedPreferences(OnlineSearchView.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
			String sharedDownloadPath = downloadDetails.getString(OnlineSearchView.DOWNLOAD_DIR, "");
			if (sharedDownloadPath.equals("")) {
				Editor edit = downloadDetails.edit();
				edit.clear();
				edit.putString(OnlineSearchView.DOWNLOAD_DIR, downloadPath);
				edit.commit();
			} else
				return sharedDownloadPath;
		} 
		return downloadPath; 
	}
	
	public View getView() {
		return this.view;
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

//	public static final View getInstanceView(LayoutInflater inflater, SearchBrowserActivity activity) {
//		View instanceView = getInstance(inflater, activity).view;
//		ViewGroup parent = (ViewGroup) instanceView.getParent();
//		if (null != parent) {
//			parent.removeView(instanceView);
//		}
//		return instanceView;
//	}

	private final class SongSearchAdapter extends ArrayAdapter<Song> {
		private LayoutInflater inflater;
		private FrameLayout footer;
		private ProgressBar refreshSpinner;
		private SparseArray<Bitmap> bitmaps = new SparseArray<Bitmap>(0);

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
		public View getView(final int position, final View convertView, ViewGroup parent) {
			Song song = getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setButtonVisible(false).setLongClickable(false).setExpandable(false).setLine1(song.getTitle()).setLine2(song.getArtist())
			.setId(position).setIcon(R.drawable.fallback_cover);
			//TODO: remove double-cacheing
			Bitmap cover = bitmaps.get(position); 
			if (cover != null) {
				builder.setIcon(bitmaps.get(position));
			} else {
				((RemoteSong)song).getCover(new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
						bitmaps.put(position, bmp);
						if (builder != null && builder.getId() == position) {
							builder.setIcon(bmp);
						}
					}
				});
			}
			if (position == getCount() - 1) {
				refreshSpinner.setVisibility(View.VISIBLE);
				getNextResults();
			}
			View v = builder.build();
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					listView.performItemClick(v, position, v.getId());
				}
			});
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
					message.setText(String.format(message.getContext().getString(R.string.search_message_empty), searchField.getText()));
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

	public OnlineSearchView(final LayoutInflater inflater) {
		super(inflater.getContext());
		this.view = inflater.inflate(R.layout.search, null);

		resultAdapter = new SongSearchAdapter(getContext(), inflater);
		message = (TextView) view.findViewById(R.id.message);
		progress = view.findViewById(R.id.progress);
		progress.setVisibility(View.GONE);
		listView = (ListView) view.findViewById(R.id.list);
		listView.addFooterView(resultAdapter.getProgress());
		listView.setAdapter(resultAdapter);
		listView.setEmptyView(message);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (position == resultAdapter.getCount()) return; // progress click
				Bundle bundle = new Bundle(0);
				bundle.putInt(KEY_POSITION, position);
				createStreamDialog(bundle).show();
			}
		});
		searchField = (TextView) view.findViewById(R.id.text);
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
		view.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				trySearch();
			}

		});
		view.findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDownloadsList();
			}
		});
		view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchField.setText(null);
				message.setText(R.string.search_message_default);
				resultAdapter.clear();
				searchStopped = true;
				progress.setVisibility(View.GONE);
			}
		});
	}

	public static boolean isOffline(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo == null;
	}

	public void trySearch() {
		InputMethodManager imm = (InputMethodManager) searchField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
		String searchString = searchField.getText().toString();
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
			if (getAdvertisment().isOnline(getContext())) {
				getAdvertisment().searchStart(getContext());
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
		final RemoteSong song = (RemoteSong) resultAdapter.getItem(args.getInt(KEY_POSITION));
		final String title = song.getTitle();
		final String artist = song.getArtist();
		final Context context = view.getContext();
		final DownloadUrlGetterTask urlTask = new DownloadUrlGetterTask(){
			@Override
			protected void onPostExecute(String downloadUrl) {
				loadSong(downloadUrl);
			}
		};
		if (null == player) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			player = new Player(inflater.inflate(R.layout.download_dialog, null), title, artist);
			if (song instanceof GrooveSong) {
				player.setSongId(((GrooveSong) song).getSongId());
			}
			try {
				String url = song.getParentUrl();
				if (url != null) {
					loadSong(url);
				} else {
					player.showProgressDialog(true);
					urlTask.execute(song);
				}
			} catch (ClassCastException ex) {
				Log.e(getClass().getSimpleName(), ex.getMessage());
			}
			if (getSettings().getIsCoversEnabled(context)) {
				song.getCover(new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (null != player) {
							player.setCover(bmp);
						}
					}
				}); 
			} else {
				player.hideCoverProgress();
			}
		}
		final Runnable dialogDismisser = new Runnable() {
			@Override
			public void run() {
				if (player != null) {
					player.cancel();
					player = null;
				}
				urlTask.cancel(true);
				//TODO: dismiss dialog here!
//				activity.removeDialog(STREAM_DIALOG_ID);
			}
		};
		final DownloadClickListener downloadClickListener = new DownloadClickListener(context, title, artist, player) {
			@Override
			public void onClick(View v) {
				super.onClick(v);
				dialogDismisser.run();
			}
		};
		if (getSettings().getIsCoversEnabled(context)) {
			coverLoader.addListener(downloadClickListener);
		}
		player.setTitle(artist + " - " + title);
		//TODO
		AlertDialog.Builder b = new AlertDialog.Builder(context).setView(player.getView());
		b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogDismisser.run();
			}
		});
		b.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadClickListener.onClick(new View(getContext()));
			}
		});
		AlertDialog alertDialog = b.create();
		alertDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDismisser.run();
			}
		});
		return alertDialog;
	}
	
	private void loadSong(String downloadUrl) {
		if (player != null) {
			player.setDownloadUrl(downloadUrl);
			player.showProgressDialog(false);
			player.execute();
		}
	}

	private void showDownloadsList() {
		final Context context = message.getContext();
		Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(dm);
	}
	
	public boolean isId3Show() {
		if (null == player) return false;
		return player.isId3Show;
	}
	
	public void createId3Dialog(String[] fields) {
		if (null == player) return;
		player.createId3dialog(fields);
	}
}
