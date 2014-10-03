package ru.johnlife.lifetoolsmp3.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RefreshListener;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper.ViewBuilder;
import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.SearchWithPages;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.task.DownloadUrlGetterTask;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public abstract class OnlineSearchView extends View {

	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.vanilla";
	public static List<Engine> engines = null;
	private AlertDialog alertDialog;
	private RemoteSong downloadSong;
	private LayoutInflater inflater;
	private Iterator<Engine> taskIterator;
	private String currentName = null;
	private SongSearchAdapter resultAdapter;
	private TextView message;
	private View progress;
	private TextView searchField;
	private View view;
	private Player player;
	private boolean searchStopped = true;
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";
	private ListView listView;
	private DownloadClickListener downloadClickListener;
	private Runnable dialogDismisser;
	private String extraSearch = null;
	
	OnShowListener dialogShowListener = new OnShowListener() {

		@Override
		public void onShow(DialogInterface dialog) {
			float textSize = 16f;
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(textSize);
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(textSize);
		}
	};

	protected abstract BaseSettings getSettings();

	protected abstract Advertisment getAdvertisment();
	
	public abstract void refreshLibrary();
		
	protected abstract void stopSystemPlayer(Context context);

	public OnlineSearchView(final LayoutInflater inflater) {
		super(inflater.getContext());
		this.inflater = inflater;
		this.view = inflater.inflate(R.layout.search, null);
	}

	public View getView() {
		final boolean fullAction = showFullElement();
		resultAdapter = new SongSearchAdapter(getContext(), inflater, fullAction);
		if (!fullAction) {
			view.findViewById(R.id.downloads).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDownloadsList();
				}
			});
		}
		initSearchEngines(getContext());
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
				if (position == resultAdapter.getCount())
					return; // progress click
				Bundle bundle = new Bundle(0);
				bundle.putInt(KEY_POSITION, position);
				if (fullAction) {
					prepareSong(bundle, false);
				} else {
					click(view, position);
				}
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
		if (extraSearch != null) {
			setSearchField(extraSearch);
			setCurrentName(extraSearch);
			trySearch();
			extraSearch = null;
			return view;
		}
		if (SongArrayHolder.getInstance().getResults() != null) {
			for (Song song : SongArrayHolder.getInstance().getResults()) {
				getResultAdapter().add(song);
			}
			if (SongArrayHolder.getInstance().getSongName() != null) {
				setTaskIterator(SongArrayHolder.getInstance().getTaskIterator());
				setSearchField(SongArrayHolder.getInstance().getSongName().toString());
				setCurrentName(SongArrayHolder.getInstance().getSongName().toString());
				getResultAdapter().notifyDataSetChanged();
				setSearchStopped(false);
				listView.setSelection(SongArrayHolder.getInstance().getListViewPosition());
			}
		}
		SongArrayHolder.getInstance().restoreState(this);
		if (SongArrayHolder.getInstance().isSearchExecute() && resultAdapter.isEmpty()) {
			search(SongArrayHolder.getInstance().getSongName());
		}
		return view;
	}

	public void initSearchEngines(Context context) {
		if (null != engines)
			return;
		String[][] engineArray = getSettings().getSearchEnginesArray(context);
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

	protected boolean showFullElement() {
		return true;
	}

	protected void click(View view, int position) {

	}
	

	// protected DownloadClickListener createListener(RemoteSong song, Bitmap
	// bitmap) {
	// return new DownloadClickListener(getContext(), song, bitmap);
	// }

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

	public SongSearchAdapter getResultAdapter() {
		return resultAdapter;
	}

	public Iterator<Engine> getTaskIterator() {
		return taskIterator;
	}

	public void setTaskIterator(Iterator<Engine> taskIterator) {
		this.taskIterator = taskIterator;
	}

	public void setResultAdapter(SongSearchAdapter resultAdapter) {
		this.resultAdapter = resultAdapter;
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

	public class SongSearchAdapter extends ArrayAdapter<Song> {

		private LayoutInflater inflater;
		private FrameLayout footer;
		private ProgressBar refreshSpinner;
		private boolean fullAction;

		private SongSearchAdapter(Context context, LayoutInflater inflater, boolean fullAction) {
			super(context, -1, new ArrayList<Song>());
			this.inflater = inflater;
			this.footer = new FrameLayout(context);
			this.refreshSpinner = new ProgressBar(context);
			this.fullAction = fullAction;
			refreshSpinner.setIndeterminate(true);
			footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
			refreshSpinner.setVisibility(View.GONE);
		}

		@Override
		public View getView(final int position, final View convertView, ViewGroup parent) {
			final Song song = getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater);
			builder.setLine1(song.getTitle(), fullAction ? null : Util.formatTimeSimple((int) song.getDuration()))
					.setLongClickable(false)
					.setExpandable(false)
					.setLine2(song.getArtist())
					.setId(position)
					.setIcon(R.drawable.fallback_cover)
					.setButtonVisible(fullAction ? false : true);
			if (getSettings().getIsCoversEnabled(getContext())) {
				((RemoteSong) song).getSmallCover(false, new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (builder != null && builder.getId() == position) {
							builder.setIcon(bmp);
							if (bmp == null) {
								builder.setIcon(R.drawable.fallback_cover);
							}
						}
					}
				});
			}
			if (position == getCount() - 1) {
				refreshSpinner.setVisibility(View.VISIBLE);
				getNextResults();
			}
			View v = builder.build();
			v.findViewById(R.id.boxInfoItem).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listView.performItemClick(v, position, v.getId());
				}
			});
			if (!fullAction) {
				v.findViewById(R.id.btnDownload).setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						listView.performItemClick(v, position, v.getId());
					}
				});
			}
			return v;
		}

		public View getProgress() {
			return footer;
		}

		public void hideProgress() {
			refreshSpinner.setVisibility(View.GONE);
		}
	}

	FinishedParsingSongs resultsListener = new FinishedParsingSongs() {
		@Override
		public void onFinishParsing(List<Song> songsList) {
			SongArrayHolder.getInstance().setSearchExecute(false);
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
			if (getAdvertisment().isOnlineLib(getContext())) {
				getAdvertisment().searchStartLib(getContext());
			}
		} catch (Exception e) {

		}
	}

	public void search(String songName) {
		searchStopped = false;
		taskIterator = engines.iterator();
		resultAdapter.clear();
		currentName = songName;
		message.setText("");
		progress.setVisibility(View.VISIBLE);
		getNextResults();
	}

	private void getNextResults() {
		SongArrayHolder.getInstance().setSearchExecute(true);
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

	public void prepareSong(final Bundle args, boolean force) {
		if(isOffline(getContext())) {
			Toast.makeText(view.getContext(), view.getContext().getString(R.string.search_message_no_internet), Toast.LENGTH_LONG).show();
			return;
		}
		if (!(args.containsKey(KEY_POSITION)) || resultAdapter.isEmpty()) {
			return;
		}
		downloadSong = (RemoteSong) resultAdapter.getItem(args.getInt(KEY_POSITION));
		final String title = downloadSong.getTitle();
		final String artist = downloadSong.getArtist();
		final Context context = view.getContext();
		final DownloadUrlGetterTask urlTask = new DownloadUrlGetterTask() {
			private ProgressDialog progressDialog;

			@Override
			protected void onPostExecute(String downloadUrl) {
				loadSong(downloadUrl);
				progressDialog.cancel();
				createStreamDialog(args).show();
			}

			@Override
			protected void onPreExecute() {
				  progressDialog = new ProgressDialog(getContext());
			      progressDialog.setTitle(R.string.please_wait);
			      progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			      progressDialog.setMessage(getContext().getString(R.string.loading_song_details));
			      progressDialog.setOnCancelListener(new OnCancelListener() {	
					@Override
					public void onCancel(DialogInterface dialog) {
						dialogDismisser.run();
					}
				});
			      progressDialog.show();
				
			}
		};
		if (force) {
			player = SongArrayHolder.getInstance().getPlayerInstance();
			player.initView(inflate(context, R.layout.download_dialog, null));
		}
		if (null == player) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			player = new Player(inflater.inflate(R.layout.download_dialog, null), title, artist);
			if (downloadSong instanceof GrooveSong) {
				player.setSongId(((GrooveSong) downloadSong).getSongId());
			}
			try {
				String url = downloadSong.getParentUrl();
				if (url != null) {
					loadSong(url);
					createStreamDialog(args).show();
				} else {
					urlTask.execute(downloadSong);
				}
			} catch (ClassCastException ex) {
				Log.e(getClass().getSimpleName(), ex.getMessage());
			}
			if (getSettings().getIsCoversEnabled(context)) {
				player.setCoverFromSong(downloadSong);
			} else {
				player.hideCoverProgress();
			}
		} else {
			createStreamDialog(args).show();
		}
		dialogDismisser = new Runnable() {
			@Override
			public void run() {
				SongArrayHolder.getInstance().setStreamDialogOpened(false, null, null);
				if (player != null) {
					player.cancel();
					player = null;
				}
				urlTask.cancel(true);
			}
		};
		downloadClickListener = new DownloadClickListener(context, (RemoteSong) downloadSong, new RefreshListener() {
			
			@Override
			public void success() {
				refreshLibrary();
			}
		}) {
			@Override
			public void onClick(View v) {
				super.onClick(v);
				dialogDismisser.run();
			}
		};
		if (getSettings().getIsCoversEnabled(context)) {
			boolean hasCover = ((RemoteSong) downloadSong).getCover(true, downloadClickListener);
			if (!hasCover) {	
					player.hideCoverProgress();
					player.setCover(null);
				}
		}
		player.setTitle(artist + " - " + title);
	}
	
	public Dialog createStreamDialog(Bundle args) {
		stopSystemPlayer(getContext());
		AlertDialog.Builder b = new AlertDialog.Builder(getContext()).setView(player.getView());
		b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialogDismisser.run();
			}
		});
		b.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String title = player.getTitle();
				String artist = player.getArtist();
				boolean useCover = player.isUseCover();
				downloadClickListener.setUseAlbumCover(useCover);
				downloadClickListener.downloadSond(artist, title, useCover);
				player.cancel();
				dialogDismisser.run();
			}

		});
		alertDialog = b.create();
		alertDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDismisser.run();
			}
		});
		alertDialog.setOnShowListener(dialogShowListener);
		SongArrayHolder.getInstance().setStreamDialogOpened(true, args, player);
		return alertDialog;
	}

	private void loadSong(String downloadUrl) {
		if (player != null) {
			player.setDownloadUrl(downloadUrl);
			player.execute();
		}
	}

	private void showDownloadsList() {
		final Context context = message.getContext();
		Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(dm);
	}

	public void createId3Dialog(String[] fields, boolean enableCover) {
		if (null == player)
			return;
		player.createId3dialog(fields, enableCover, true);
	}

	public void createLyricsDialog(String[] titleArtist, String lyrics) {
		if (null == player)
			return;
		player.createLyricsDialog(titleArtist[0], titleArtist[1], lyrics);
	}

	public void setSearchField(String str) {
		searchField.setText(str);
	}

	public TextView getSearchField() {
		return searchField;
	}

	public String getCurrentName() {
		return currentName;
	}

	public void setCurrentName(String currentName) {
		this.currentName = currentName;
	}

	public boolean isSearchStopped() {
		return searchStopped;
	}

	public void setSearchStopped(boolean searchStopped) {
		this.searchStopped = searchStopped;
	}

	public static String getDOWNLOAD_DETAIL() {
		return DOWNLOAD_DETAIL;
	}

	public static String getDOWNLOAD_DIR() {
		return DOWNLOAD_DIR;
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setExtraSearch(String extraSearch) {
		this.extraSearch = extraSearch;
	}

	public ListView getListView() {
		return listView;
	}
	
	public void notifyAdapter() {
		resultAdapter.notifyDataSetChanged();
	}
}
