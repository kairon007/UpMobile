 package ru.johnlife.lifetoolsmp3.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
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

public abstract class OnlineSearchView extends View {
	
	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static final String KEY_POSITION = "position.song.vanilla";
	public static List<Engine> engines = null;
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

	
	protected abstract BaseSettings getSettings();

	protected abstract Advertisment getAdvertisment();

	public OnlineSearchView(final LayoutInflater inflater) {
		super(inflater.getContext());
		this.inflater = inflater;
		this.view = inflater.inflate(R.layout.search, null);
	}

	public View getView() {
		final boolean fullAction = showFullElement();
		if (!fullAction) {
			android.util.Log.d("log", "gone");
			view.findViewById(R.id.downloads).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showDownloadsList();
				}
			});
		}
		resultAdapter = new SongSearchAdapter(getContext(), inflater, fullAction);
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
					createStreamDialog(bundle, false).show();
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
		if (SongArrayHolder.getInstance().getResults() != null) {
			for (Song song : SongArrayHolder.getInstance().getResults()) {
				getResultAdapter().add(song);
			}
			setTaskIterator(SongArrayHolder.getInstance().getTaskIterator());
			setSearchField(SongArrayHolder.getInstance().getSongName().toString());
			setCurrentName(SongArrayHolder.getInstance().getSongName().toString());
			getResultAdapter().notifyDataSetChanged();
			setSearchStopped(false);
		}
		if (SongArrayHolder.getInstance().isStreamDialogOpened()) {
			Bundle args = SongArrayHolder.getInstance().getStreamDialogArgs();
			createStreamDialog(args, true).show();
		}
		if (SongArrayHolder.getInstance().isID3DialogOpened()) {
			createId3Dialog(SongArrayHolder.getInstance().getID3Fields(),
					SongArrayHolder.getInstance().isCoverEnabled());
		}
		if (SongArrayHolder.getInstance().isLyricsOpened()) {
			createLyricsDialog(SongArrayHolder.getInstance().getTitleArtistLyrics(), 
					SongArrayHolder.getInstance().getLyrics());
		}
		if (SongArrayHolder.getInstance().isDirectoryChooserOpened()) {
			player.createDirectoryChooserDialog();
		}
		return view;
	}
	
	protected View biuldCustomView() {
		return null;
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

//	protected DownloadClickListener createListener(RemoteSong song, Bitmap bitmap) {
//		return new DownloadClickListener(getContext(), song, bitmap);
//	}

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

	public final class SongSearchAdapter extends ArrayAdapter<Song> {
		
		private SparseArray<Bitmap> bitmaps = new SparseArray<Bitmap>(0);
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
			builder.setLine1(song.getTitle(), fullAction ? null : Util.formatTimeSimple((int)song.getDuration()))
					.setLongClickable(false)
					.setExpandable(false)
					.setLine2(song.getArtist())
					.setId(position)
					.setIcon(song.getSongCover() == null ? R.drawable.fallback_cover : song.getSongCover())
					.setButtonVisible(fullAction ? false : true);
			// TODO: remove double-cacheing
			if (getSettings().getIsCoversEnabled(getContext())) {
				Bitmap cover = bitmaps.get(position);
				if (cover != null) {
					builder.setIcon(bitmaps.get(position));
				} else {
					((RemoteSong) song).getCover(new OnBitmapReadyListener() {
						@Override
						public void onBitmapReady(Bitmap bmp) {
							bitmaps.put(position, bmp);
							if (builder != null && builder.getId() == position) {
								builder.setIcon(bmp);
								((RemoteSong) song).setSongCover(bmp);
							}
						}
					});
				}
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
	
	@SuppressLint("NewApi")
	public Dialog createStreamDialog(Bundle args, boolean force) {
		if (!(args.containsKey(KEY_POSITION)) || resultAdapter.isEmpty()) {
			return null;
		}
		downloadSong = (RemoteSong) resultAdapter.getItem(args.getInt(KEY_POSITION));
		final String title = downloadSong.getTitle();
		final String artist = downloadSong.getArtist();
		final Context context = view.getContext();
		final DownloadUrlGetterTask urlTask = new DownloadUrlGetterTask() {
			@Override
			protected void onPostExecute(String downloadUrl) {
				loadSong(downloadUrl);
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
				} else {
					urlTask.execute(downloadSong);
				}
			} catch (ClassCastException ex) {
				Log.e(getClass().getSimpleName(), ex.getMessage());
			}
			if (getSettings().getIsCoversEnabled(context)) {
				downloadSong.getCover(new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (null != player) {
							player.setCover(bmp);
						}
					}
				});
			} else {
				if (downloadSong.getSongCover() != null) {
					player.setCover(downloadSong.getSongCover());
				} else
					player.hideCoverProgress();
			}
		}
		final Runnable dialogDismisser = new Runnable() {
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
		final DownloadClickListener downloadClickListener = new DownloadClickListener(context, (RemoteSong) downloadSong) {
			@Override
			public void onClick(View v) {
				super.onClick(v);
				dialogDismisser.run();
			}
		};
		if (getSettings().getIsCoversEnabled(context)) {
			boolean hasCover = ((RemoteSong) downloadSong).getCover(downloadClickListener);
			if (!hasCover)
				player.setCover(null);
		} else {
			if (downloadSong.getSongCover() != null) {
				player.setCover(downloadSong.getSongCover());
			}
		}
		player.setTitle(artist + " - " + title);
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
				ArrayList<String> sFields = player.getFields();
				boolean useCover = player.isUseCover();
				downloadClickListener.setUseAlbumCover(useCover);
				if (sFields.isEmpty()) {
					downloadClickListener.onClick(new View(getContext()));
					return;
				}
				String artist = sFields.get(0) != null ? sFields.get(0) : downloadSong.getArtist();
				String title = sFields.get(1) != null ? sFields.get(1) : downloadSong.getTitle();
				downloadClickListener.downloadSond(artist , title, useCover);
			}
			
		});
		AlertDialog alertDialog = b.create();
		alertDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDismisser.run();
			}
		});
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

	private void createId3Dialog(String[] fields, boolean enableCover) {
		if (null == player) return;
		player.createId3dialog(fields, enableCover);
	}
	
	private void createLyricsDialog(String[] titleArtist, String lyrics) {
		if (null == player) return;
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
}
