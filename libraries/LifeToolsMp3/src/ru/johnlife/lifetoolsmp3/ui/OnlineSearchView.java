package ru.johnlife.lifetoolsmp3.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RefreshListener;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper.ViewBuilder;
import ru.johnlife.lifetoolsmp3.adapter.CustomSpinnerAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.SearchWithPages;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public abstract class OnlineSearchView extends View {

	public static List<Engine> engines = null;
	private static final Void[] NO_PARAMS = {};
	public static final int STREAM_DIALOG_ID = 1;
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";
	private String SPREF_ENGINES ="shar_pref_key_engines_array";
	private String SPREF_CURRENT_ENGINES ="pref_key_current_engines_array";
	private Iterator<Engine> taskIterator;
	private SharedPreferences sPref;
	private AlertDialog alertDialog;
	private RemoteSong downloadSong;
	private ArrayAdapter adapter;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private LayoutInflater inflater;
	private String currentName = null;
	private SongSearchAdapter resultAdapter;
	private TextView message;
	private Spinner spEnginesChoiser;
	private View progress;
	private TextView searchField;
	private ViewGroup view;
	private Player player;
	private boolean searchStopped = true;
	private ListView listView;
	private DownloadClickListener downloadClickListener;
	private Runnable dialogDismisser;
	private String extraSearch = null;
	private String keyEngines;
	private OnlineSearchView onlineView = this;
	private boolean switchMode = false;
	private SongArrayHolder holder = SongArrayHolder.getInstance();
	
	OnShowListener dialogShowListener = new OnShowListener() {

		@Override
		public void onShow(DialogInterface dialog) {
			float textSize = 16f;
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(textSize);
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(textSize);
		}
	};
	
	private class HeadsetIntentReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
				player.pause();
			}
		}
	};
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				player.pause();
				holder.setPlaying(true);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if(holder.isPlaying()) player.play();
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};
	
	OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			String value = sharedPreferences.getString(key, null);
			initSearchEngines(getContext(), value);
			if (!resultAdapter.isEmpty() || switchMode){
				trySearch();
			}
		}
	};

	protected abstract BaseSettings getSettings();

	protected abstract Advertisment getAdvertisment();
	
	public abstract void refreshLibrary();
		
	protected abstract void stopSystemPlayer(Context context);
	
	protected abstract boolean isWhiteTheme(Context context);

	public OnlineSearchView(final LayoutInflater inflater) {
		super(inflater.getContext());
		this.inflater = inflater;
		this.view = (ViewGroup) inflater.inflate(R.layout.search, null);
		initSearchEngines(getContext(), null);
		sPref = getContext().getSharedPreferences(SPREF_ENGINES, Context.MODE_PRIVATE);
		keyEngines = sPref.getString(SPREF_CURRENT_ENGINES, getTitleSearchEngine());
		sPref.registerOnSharedPreferenceChangeListener(sPrefListener);
	}

	public View getView() {
		final boolean fullAction = showFullElement();
		resultAdapter = new SongSearchAdapter(getContext(), inflater, fullAction);
		init();
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
		progress.setVisibility(View.GONE);
		listView.addFooterView(resultAdapter.getProgress());
		listView.setAdapter(resultAdapter);
		if (isWhiteTheme(getContext())) {
			listView.setDividerHeight(0);
			listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
			view.findViewById(R.id.search_field).setBackgroundResource(R.drawable.search_background_white);
			view.findViewById(R.id.choise_engines_layout).setBackgroundResource(R.drawable.spinner_background);
			view.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
		}
		listView.setEmptyView(message);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (position == resultAdapter.getCount()) return; // progress click
				getDownloadUrl(fullAction, view, position);
			}
		});
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
		ArrayList<String> list = getSettings().getEnginesArray(getContext());
		if (list.size() > 1) {
			
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				adapter = new ArrayAdapter<String>(getContext(), R.layout.item_of_engine, list);
			}
			else
				adapter = new CustomSpinnerAdapter(getContext(), 0, list);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spEnginesChoiser.setAdapter(adapter);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.view.ViewGroup.LayoutParams params = spEnginesChoiser.getLayoutParams();
				params.width = 300;
				spEnginesChoiser.setLayoutParams(params);
			}
			int id = adapter.getPosition(keyEngines);
			spEnginesChoiser.setSelection(id);
			final Context context = getContext();
			spEnginesChoiser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					// TODO
					keyEngines = (String) adapter.getItem(position);
					sPref = context.getSharedPreferences(SPREF_ENGINES, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sPref.edit();
					if (keyEngines.equals(getTitleSearchEngine())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine());
						keyEngines = getTitleSearchEngine();
					} else if (keyEngines.equals(getTitleSearchEngine2())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine2());
						keyEngines = getTitleSearchEngine2();
					} else if (keyEngines.equals(getTitleSearchEngine3())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine3());
						keyEngines = getTitleSearchEngine3();
					} else if (keyEngines.equals(getTitleSearchEngine4())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine4());
						keyEngines = getTitleSearchEngine4();
					} else if (keyEngines.equals(getTitleSearchEngine5())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine5());
						keyEngines = getTitleSearchEngine5();
					} else if (keyEngines.equals(getTitleSearchEngine6())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine6());
						keyEngines = getTitleSearchEngine6();
					} else if (keyEngines.equals(getTitleSearchEngine7())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine7());
						keyEngines = getTitleSearchEngine7();
					} else if (keyEngines.equals(getTitleSearchEngine8())) {
						editor.putString(SPREF_CURRENT_ENGINES, getTitleSearchEngine8());
						keyEngines = getTitleSearchEngine8();
					}
					
					editor.commit();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

		} else {
			spEnginesChoiser.setVisibility(View.GONE);
			sPref.unregisterOnSharedPreferenceChangeListener(sPrefListener);
		}
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
				message.setText(R.string.search_your_results_appear_here);
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
		if (holder.getResults() != null) {
			for (Song song : holder.getResults()) {
				getResultAdapter().add(song);
			}
			if (holder.getSongName() != null) {
				setTaskIterator(holder.getTaskIterator());
				setSearchField(holder.getSongName().toString());
				setCurrentName(holder.getSongName().toString());
				getResultAdapter().notifyDataSetChanged();
				setSearchStopped(false);
				message.setText(holder.getMessage());
				listView.setSelection(holder.getListViewPosition());
			}
		}
		holder.restoreState(this);
		if (holder.isSearchExecute() && resultAdapter.isEmpty()) {
			search(holder.getSongName());
		}
		return view;
	}

	private void init() {
		message = (TextView) view.findViewById(R.id.message);
		progress = view.findViewById(R.id.progress);
		listView = (ListView) view.findViewById(R.id.list);
		searchField = (TextView) view.findViewById(R.id.text);
		spEnginesChoiser = (Spinner) view.findViewById(R.id.choise_engines);
	}
	
	public static String getTitleSearchEngine() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title", "Search Engine 1");
	}

	public static String getTitleSearchEngine2() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_2", "Search Engine 2");
	}

	public static String getTitleSearchEngine3() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_3", "Search Engine 3");
	}
	
	public static String getTitleSearchEngine4() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_4", "Search Engine 4");
	}
	
	public static String getTitleSearchEngine5() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_5", "Search Engine 5");
	}
	public static String getTitleSearchEngine6() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_6", "Search Engine 6");
	}
	public static String getTitleSearchEngine7() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_7", "Search Engine 7");
	}
	public static String getTitleSearchEngine8() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title_8", "Search Engine 8");
	}
	

	public void initSearchEngines(Context context, String valueEngines) {
		//TODO this set number engines
		if (null == valueEngines) {
			sPref = getContext().getSharedPreferences(SPREF_ENGINES, Context.MODE_PRIVATE);
			keyEngines = sPref.getString(SPREF_CURRENT_ENGINES, getTitleSearchEngine());
		} else {
			keyEngines = valueEngines;
		}
		String[][] engineArray = null;
		if (keyEngines.equals(getTitleSearchEngine())) {
			engineArray = getSettings().getSearchEnginesArray(context);
		} else if (keyEngines.equals(getTitleSearchEngine2())) {
			engineArray = getSettings().getSearchEnginesArray2(context);	
		} else if (keyEngines.equals(getTitleSearchEngine3())) {
			engineArray = getSettings().getSearchEnginesArray3(context);
		} else if (keyEngines.equals(getTitleSearchEngine4())) {
			engineArray = getSettings().getSearchEnginesArray4(context);
		} else if (keyEngines.equals(getTitleSearchEngine5())) {
			engineArray = getSettings().getSearchEnginesArray5(context);
		} else if (keyEngines.equals(getTitleSearchEngine6())) {
			engineArray = getSettings().getSearchEnginesArray6(context);
		} else if (keyEngines.equals(getTitleSearchEngine7())) {
			engineArray = getSettings().getSearchEnginesArray7(context);
		} else if (keyEngines.equals(getTitleSearchEngine8())) {
			engineArray = getSettings().getSearchEnginesArray8(context);
		}
		
		for (int i = 0; i < engineArray.length; i++) {
			for (int j = 0; j < engineArray[i].length; j++) {
			}
		}
		engines = new ArrayList<Engine>(engineArray.length);
		for (int i = 0; i < engineArray.length; i++) {
			try {
				Class<? extends BaseSearchTask> engineClass = (Class<? extends BaseSearchTask>) Class.forName("ru.johnlife.lifetoolsmp3.engines." + engineArray[i][0]);
				int maxPages = Integer.parseInt(engineArray[i][1]);
				for (int page = 1; page <= maxPages; page++) {
					engines.add(new Engine(engineClass, page));
				}
			} catch (ClassNotFoundException e) {
				
			}
		}
	}

	protected boolean showFullElement() {
		return true;
	}

	protected void click(View view, int position) {

	}
	
	protected boolean onlyOnWifi() {
		return true;
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
			} else if (!new File(sharedDownloadPath).exists()) {
				return downloadPath;
			} else {
				return sharedDownloadPath;
			}
		}
		return downloadPath;
	}

	public SongSearchAdapter getResultAdapter() {
		return resultAdapter;
	}

	public Iterator<Engine> getTaskIterator() {
		return taskIterator;
	}
	
	public boolean isSwitchMode() {
		return switchMode;
	}
	
	public void setSwitchMode(boolean switchMode) {
		this.switchMode = switchMode;
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
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater, isWhiteTheme(getContext()));
			builder.setLine1(song.getTitle(),Util.getFormatedStrDuration(song.getDuration()))
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
			holder.setSearchExecute(false);
			resultAdapter.hideProgress();
			if (searchStopped) return;
			//TODO: set result
			if (songsList.isEmpty()) {
				getNextResults();
				if (!taskIterator.hasNext() && resultAdapter.isEmpty()) {
					try {
						switchMode = true;
						String src = getContext().getResources().getText(R.string.search_no_results_for).toString() + " " + searchField.getText().toString();
						message.setText(src);
					} catch(Exception e) {
						
					}
					progress.setVisibility(View.GONE);
				}
			} else {
				progress.setVisibility(View.GONE);
				for (Song song : songsList) {
					resultAdapter.add(song);
				}
				holder.saveStateAdapter(onlineView);
			}
		}
	};
	protected ProgressDialog progressDialog;

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
			message.setText(R.string.search_please_enter_query);
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
	
	public ArrayList<String> getDMCABlacklistedItems(String remoteSetting) {
		ArrayList<String> searchEngines = new ArrayList<String>();
		try {
			SharedPreferences prefs = MusicApp.getSharedPreferences();
			String remoteSettingSearchEngines = prefs.getString(remoteSetting, null);
			JSONArray jsonArray = new JSONArray(remoteSettingSearchEngines);
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					String searchEngine = jsonArray.getString(i);
					searchEngines.add(searchEngine); 
				}catch(Exception e) { 
					
				}
			}
		}catch(Exception e) {
		}
		
		return searchEngines;
	}
	

	public boolean isBlacklistedQuery(String songName) {

		ArrayList<String> dmcaSearchQueryBlacklist = getDMCABlacklistedItems("dmca_searchquery_blacklist");

		if (songName != null) {
			// serach blacklist
			for (String blacklistedSearchQuery : BaseSearchTask.blacklist) {
				blacklistedSearchQuery = blacklistedSearchQuery.toLowerCase();
				if (songName.contains(blacklistedSearchQuery)) {
					return true;
				}
			}

			// serach dmca_searchquery blacklist
			for (String blacklistedSearchQuery : dmcaSearchQueryBlacklist) {
				blacklistedSearchQuery = blacklistedSearchQuery.toLowerCase();
				if (songName.contains(blacklistedSearchQuery)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void search(String songName) {
		holder.setSearchExecute(true);
		searchStopped = false;
		if (isBlacklistedQuery(songName)) {
			// if blacklisted query, then searchNothing			
			ArrayList<Engine> nothingSearch = new ArrayList<Engine>();
			try {
				Class<? extends BaseSearchTask> engineClass = (Class<? extends BaseSearchTask>) Class.forName("ru.johnlife.lifetoolsmp3.engines.SearchNothing");
				nothingSearch.add(new Engine(engineClass, 1));
			} catch (ClassNotFoundException e) {
				Log.e("SearchTab", "Unknown engine", e);
			}
			taskIterator = nothingSearch.iterator();
		} else {
			taskIterator = engines.iterator();
		}
		resultAdapter.clear();
		currentName = songName;
		message.setText("");
		progress.setVisibility(View.VISIBLE);
		getNextResults();
	}

	private void getNextResults() {
		resultAdapter.refreshSpinner.setVisibility(View.VISIBLE);
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
	
	public void getDownloadUrl(final boolean fullAction, final View view, final int position) {
		if(isOffline(getContext())) {
			Toast.makeText(getContext(), getContext().getString(R.string.search_message_no_internet), Toast.LENGTH_LONG).show();
			return;
		}
		if(!onlyOnWifi()){
			Toast.makeText(getContext(), getContext().getString(R.string.no_wi_fi), Toast.LENGTH_LONG).show();
			return;
		}
		holder.setProgressDialogOpened(true, fullAction, view);
		stopSystemPlayer(getContext());
		final RemoteSong downloadSong = (RemoteSong) resultAdapter.getItem(position);
		if (view.getId() != R.id.btnDownload) {
			showProgressDialog(fullAction, view, downloadSong);
		}
		if (fullAction) {
			downloadSong.getDownloadUrl(new DownloadUrlListener() {
	
				@Override
				public void success(final String url) {
					if (null != progressDialog) {
						progressDialog.dismiss();
					}
					downloadSong.setDownloadUrl(url);
					holder.setProgressDialogOpened(false, fullAction, view);
					((Activity)getContext()).runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if (fullAction) {
								prepareSong(downloadSong, false);
							}
						}
					});
				}
	
				@Override
				public void error(String error) {
					if (null != progressDialog) {
						progressDialog.dismiss();
					}
					holder.setProgressDialogOpened(false, fullAction, view);
					Toast toast = Toast.makeText(getContext(), R.string.error_getting_url_songs, Toast.LENGTH_SHORT);
					switchMode = true;
					toast.show();
				}
			});
		} else {
			click(view, position);
		}
	}

	protected void showProgressDialog(final boolean fullAction, final View view, final RemoteSong downloadSong) {
		progressDialog = new ProgressDialog(getContext());
		progressDialog.setTitle(R.string.message_please_wait);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getContext().getString(R.string.message_loading));
		progressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				downloadSong.cancelTasks();
				holder.setProgressDialogOpened(false, fullAction, view);
			}
		});
		progressDialog.show();
	}
	

	public void prepareSong(final RemoteSong remoteSong, boolean force) {
		RemoteSong song = remoteSong.cloneSong();
		String title = song.getTitle();
		String artist = song.getArtist();
		final boolean isDialogOpened = holder.isStremDialogOpened();
		if (force) {
			player = holder.getPlayerInstance();
			player.initView(inflate(getContext(), isWhiteTheme(getContext()) ? R.layout.download_dialog_white : R.layout.download_dialog, null));
		}
		if (null == player) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(isWhiteTheme(getContext()) ? R.layout.download_dialog_white : R.layout.download_dialog, null);
			player = new Player(v, song);
			if (song instanceof GrooveSong) {
				player.setSongId(((GrooveSong) song).getSongId());
			}
			loadSong(song.getUrl());
			createStreamDialog(song).show();
			if (getSettings().getIsCoversEnabled(getContext())) {
				player.setCoverFromSong(song);
			} else {
				player.hideCoverProgress();
			}
		} else {
			if (force || !isDialogOpened) {
				createStreamDialog(song).show();
			}
		}
		dialogDismisser = new Runnable() {
			@Override
			public void run() {
				holder.setStreamDialogOpened(false, null, null);
				holder.setCoverEnabled(true);
				if (player != null) {
					player.cancel();
					player = null;
				}
			}
		};
		downloadClickListener = new DownloadClickListener(getContext(), song, new RefreshListener() {
			
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
		if (getSettings().getIsCoversEnabled(getContext())) {
			boolean hasCover = ((RemoteSong) song).getCover(true, downloadClickListener);
			if (!hasCover) {	
					player.hideCoverProgress();
					player.setCover(null);
				}
		}
		player.setTitle(artist + " - " + title);
	}
	
	public Dialog createStreamDialog(final RemoteSong song) {
		headsetReceiver = new HeadsetIntentReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		getContext().registerReceiver(headsetReceiver, filter);
		telephonyManager = (TelephonyManager) getContext().getSystemService(Service.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		stopSystemPlayer(getContext());
		AlertDialog.Builder b = new AlertDialog.Builder(getContext()).setView(player.getView());
		b.setNegativeButton(R.string.download_dialog_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialogDismisser != null) {
					dialogDismisser.run();
				} else {
					dialog.dismiss();
				}
				getContext().unregisterReceiver(headsetReceiver);
				if (telephonyManager != null) {
					telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
				}
			}

		});
		b.setPositiveButton(R.string.download_dialog_download, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadClickListener.setSong(song);
				boolean useCover = holder.isCoverEnabled();
				downloadClickListener.setUseAlbumCover(useCover);
				downloadClickListener.downloadSong(false);
				player.cancel();
				dialogDismisser.run();
				getContext().unregisterReceiver(headsetReceiver);
				if (telephonyManager != null) {
					telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
				}
				holder.setCoverEnabled(true);
			}
		});
		alertDialog = b.create();
		alertDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDismisser.run();
				getContext().unregisterReceiver(headsetReceiver);
				if (telephonyManager != null) {
					telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
				}
			}
		});
		alertDialog.setOnShowListener(dialogShowListener);
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		holder.setStreamDialogOpened(true, song, player);
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

	public void createId3Dialog(String[] fields) {
		if (null == player)
			return;
		player.createId3dialog(fields);
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

	public int getListViewPosition() {
		return listView.getFirstVisiblePosition();
	}
	
	public void notifyAdapter() {
		resultAdapter.notifyDataSetChanged();
	}
	
	public String getMessage() {
		if (null != message) {
			return message.getText().toString();
		} else return getContext().getResources().getString(R.string.search_your_results_appear_here);
	}
}