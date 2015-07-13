package ru.johnlife.lifetoolsmp3.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity.DownloadPressListener;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSpinnerAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.SearchGoearV2;
import ru.johnlife.lifetoolsmp3.engines.SearchGrooveshark;
import ru.johnlife.lifetoolsmp3.engines.SearchHulkShare;
import ru.johnlife.lifetoolsmp3.engines.SearchJamendo;
import ru.johnlife.lifetoolsmp3.engines.SearchKugou;
import ru.johnlife.lifetoolsmp3.engines.SearchMp3World;
import ru.johnlife.lifetoolsmp3.engines.SearchMp3skull;
import ru.johnlife.lifetoolsmp3.engines.SearchMyFreeMp3;
import ru.johnlife.lifetoolsmp3.engines.SearchPleer;
import ru.johnlife.lifetoolsmp3.engines.SearchPleerV2;
import ru.johnlife.lifetoolsmp3.engines.SearchPoisk;
import ru.johnlife.lifetoolsmp3.engines.SearchSoArdIyyin;
import ru.johnlife.lifetoolsmp3.engines.SearchSogou;
import ru.johnlife.lifetoolsmp3.engines.SearchSoundCloud;
import ru.johnlife.lifetoolsmp3.engines.SearchTaringaMp3;
import ru.johnlife.lifetoolsmp3.engines.SearchTing;
import ru.johnlife.lifetoolsmp3.engines.SearchVK;
import ru.johnlife.lifetoolsmp3.engines.SearchVmusice;
import ru.johnlife.lifetoolsmp3.engines.SearchWithPages;
import ru.johnlife.lifetoolsmp3.engines.SearchYouTube;
import ru.johnlife.lifetoolsmp3.engines.SearchYouTubeMusic;
import ru.johnlife.lifetoolsmp3.engines.SearchZvukoff;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.GrooveSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import ru.johnlife.lifetoolsmp3.ui.Player;
import android.annotation.SuppressLint;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class BaseSearchView extends View implements OnTouchListener, OnClickListener, OnItemClickListener {

	private static final Void[] NO_PARAMS = {};
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";

	public static final String EMPTY_DIRECTORY = "directory.not.create.for.application";
	public static List<Engine> engines = Collections.emptyList();
	
	private AlertDialog alertDialog;
	protected AlertDialog.Builder progressDialog;
	protected AlertDialog alertProgressDialog;

	protected DownloadClickListener downloadListener;
	protected PlaybackService service;// init in subclasses
	
	/*!!! ADAPTERS !!!*/
	private BaseSearchAdapter adapter;
	private ArrayAdapter<String> enginesAdapter;
	
	private final String SPREF_CURRENT_ENGINES = "pref_key_current_engines_array";
	private int clickPosition;
	private boolean isRestored = false;
	private String lastSearchString = "";
	private String extraSearch = null;
	private String keyEngines;
	private Iterator<Engine> taskIterator;
	private SharedPreferences sPref;
	private StateKeeper keeper;
	private Player player;
	private RemoteSong downloadSong;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private Bitmap listViewImage;
	private BaseSearchTask searchTask;
	
	/*!!! VIEWS !!!*/
	private ViewGroup view;
	protected ListView listView;
	private View clearBtn;
	private View downloads;
	private View searchBtn;
	private View spEnginesChoiserScroll;
	private View emptyHeader;
	private View progress;
	private View viewItem;
	private TextView message;
	private TextView searchField;
	private Spinner spEnginesChoiser;
	protected ProgressDialog progressSecond;	// For PtMusicAppOffline
	private View touchInterceptor;
	
	public void specialInit(View view) {}
	
	protected abstract BaseSettings getSettings();
	protected abstract Nulldroid_Advertisment getAdvertisment();
	protected abstract void stopSystemPlayer(Context context);
	public abstract BaseSearchAdapter getAdapter();
	protected abstract ListView getListView(View v);
	protected void hideView () { }	//hide player in MusicDownloder application
	protected void click(View view, int position) { Util.hideKeyboard(getContext(), view); }
	protected boolean showDownloadLabel() { return false; }
	protected boolean showFullElement() { return true; }
	protected boolean showPopupMenu() { return false; }
	protected boolean isAppPT () { return false; }
	protected boolean onlyOnWifi() { return true; }
	protected void showShadow (boolean visible) { }

	public BaseSearchView(LayoutInflater inflater) {
		super(inflater.getContext());
		init(inflater);
		initSearchEngines(getContext(), null);
		keeper = StateKeeper.getInstance();
		keeper.restoreState(this);
		sPref = MusicApp.getSharedPreferences();
		keyEngines = sPref.getString(SPREF_CURRENT_ENGINES, getTitleSearchEngine());
		float width = searchField.getPaint().measureText(getResources().getString(R.string.hint_main_search));
		if (searchField.getWidth() - view.findViewById(R.id.clear).getWidth() < width) {
			searchField.setHint(Html.fromHtml("<small>" + getResources().getString(R.string.hint_main_search) + "</small>"));
		} else {
			searchField.setHint(R.string.hint_main_search);
		}
		if (keeper.checkState(StateKeeper.SEARCH_EXE_OPTION) && adapter.isEmpty()) {
			search(searchField.getText().toString());
		}
		setMessage(getResources().getString(R.string.search_your_results_appear_here));
		initBoxEngines();
		if (showDownloadLabel()) {
			((BaseMiniPlayerActivity) getContext()).setDownloadPressListener(downloadPressListener);
		}
	}
	
	OnShowListener dialogShowListener = new OnShowListener() {
		
		@Override
		public void onShow(final DialogInterface dialog) {
			float textSize = 16f;
			final Button positive = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
			positive.setTextSize(textSize);
			positive.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					downloadListener.setSong(keeper.getDownloadSong());
					downloadListener.setUseAlbumCover(keeper.isUseCover());
					downloadListener.downloadSong(false);
					positive.setEnabled(false);
				}
			});
			Button negative = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
			negative.setTextSize(textSize);
			negative.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.cancel();
				}
			});
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
				keeper.activateOptions(StateKeeper.IS_PLAYING_OPTION);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if(keeper.checkState(StateKeeper.IS_PLAYING_OPTION)) player.play();
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	};

	DownloadPressListener downloadPressListener = new DownloadPressListener() {
		
		@Override
		public void downloadButtonPressed(final RemoteSong song) {
			((Activity) getContext()).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					View view = getViewByPosition(adapter.getPosition(song));
					TextView infoView = (TextView) view.findViewById(R.id.infoView);
					if (null == infoView) return;
					infoView.setVisibility(View.VISIBLE);
					infoView.setText(R.string.downloading);
					infoView.setTextColor(Color.RED);
				}
			});
		}
	};

	FinishedParsingSongs resultsListener = new FinishedParsingSongs() {

		@Override
		public void onFinishParsing(List<Song> songsList) {
			adapter.setVisibilityProgress(false);
			if (keeper.checkState(StateKeeper.SEARCH_STOP_OPTION)) {
				adapter.clear();
				return;
			}
			if (songsList.isEmpty()) {
				getNextResults(false);
				if (!taskIterator.hasNext() && adapter.isEmpty()) {
					try {
						String src = getContext().getResources().getText(R.string.search_no_results_for).toString() + " " + searchField.getText().toString();
						message.setText(src);
					} catch(Exception e) {
						
					}
					hideBaseProgress();
				}
			} else {
				hideBaseProgress();
				try {
					for (Song song : songsList) {
						if (!contains(song)) {
							adapter.add(song);
						}
					}
					if (adapter.getCount() <= 3 && !(adapter.getCount() > 12) && taskIterator.hasNext()) {
						getNextResults(false);
					}
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), e.toString());
				}
			}
		}
	};
	
	private boolean contains (Song song) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (((Song) adapter.getItem(i)).getComment().equals(song.getComment())) {
				return true;
			}
		}
		return false;
	}
	
	public View getView() {
		if (!showFullElement()) {
			downloads.setVisibility(View.GONE);
		} else {
			downloads.setOnClickListener(this);
		}
		if (!isRestored) {
			listView.addHeaderView(emptyHeader);
			listView.addFooterView(adapter.getProgressView(), null, false);
			listView.setAdapter(adapter);
			animateListView(false);
		}
		listView.setEmptyView(message);
		listView.setOnScrollListener(new OnScrollListener() {
			
			int lastScroll = getScrollListView();
			int maxScroll = spEnginesChoiserScroll.getLayoutParams().height;
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount == totalItemCount - 1) {
					getNextResults(false);
				}
				int scrollBy = getScrollListView() - lastScroll;
				lastScroll = getScrollListView();
				int resultScroll = spEnginesChoiserScroll.getScrollY() + scrollBy;
				if (resultScroll < 0) {
					spEnginesChoiserScroll.scrollTo(0, 0);
					showShadow(false);
				} else if (resultScroll > maxScroll) {
					showShadow(true);
					spEnginesChoiserScroll.scrollTo(0, maxScroll);
				} else {
					spEnginesChoiserScroll.scrollBy(0, scrollBy);
					if (0 != scrollBy) {
						Util.hideKeyboard(getContext(), view);
						showShadow(false);
					}
				}
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
		if (extraSearch != null) {
			searchField.setText(extraSearch);
			trySearch();
			return view;
		}
		if (keeper.checkState(StateKeeper.SEARCH_EXE_OPTION) && adapter.isEmpty()) {
			showBaseProgress();
			message.setVisibility(View.GONE);
		} else {
			hideBaseProgress();
		}
		searchField.setFocusable(false);
		return view;
	}
	
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.clear) {
			Util.hideKeyboard(getContext(), view);
			searchField.setText(null);
			setMessage(getResources().getString(R.string.search_your_results_appear_here));
			getNextResults(true);
			adapter.clear();
			keeper.activateOptions(StateKeeper.SEARCH_STOP_OPTION);
			keeper.deactivateOptions(StateKeeper.SEARCH_EXE_OPTION);
			hideBaseProgress();
		} else if (id == R.id.search) {
			Util.hideKeyboard(getContext(), view);
			ImageLoader.getInstance().stop();
			trySearch();
		} else if (id == R.id.downloads) {
			showDownloadsList();
		}
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		int id = view.getId();
		if (id == R.id.touch_interceptor) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				Util.hideKeyboard(getContext(), view);
				searchField.setFocusable(false);
			}
			return view.performClick();
		} else if (id == R.id.text) {
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				view.setFocusableInTouchMode(true);
			}
			return view.performClick();
		} else if (id == R.id.choise_engines) {
			if (((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight() < 400) {
				Util.hideKeyboard(getContext(), view);
			}
			return view.performClick();
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		try {
			adapter.notifyDataSetChanged();
			viewItem = view;
			clickPosition = position - 1;
			getDownloadUrl(view, (position - 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init(LayoutInflater inflater) {
		view = (ViewGroup) inflater.inflate(R.layout.search, null);
		listView = getListView(view);
		adapter = getAdapter();
		message = (TextView) view.findViewById(R.id.message);
		progress = view.findViewById(R.id.progress);
		clearBtn = view.findViewById(R.id.clear);
		searchBtn = view.findViewById(R.id.search);
		downloads = view.findViewById(R.id.downloads);
		touchInterceptor = view.findViewById(R.id.touch_interceptor);
		searchField = (TextView) view.findViewById(R.id.text);
		spEnginesChoiser = (Spinner) view.findViewById(R.id.choise_engines);
		spEnginesChoiserScroll = view.findViewById(R.id.search_scroll);
		emptyHeader = inflate(getContext(), R.layout.empty_header, null);
		specialInit(view);
		setListeners();
	}
	
	private void setListeners() {
		clearBtn.setOnClickListener(this);
		searchBtn.setOnClickListener(this);
		touchInterceptor.setOnTouchListener(this);
		searchField.setOnTouchListener(this);
		listView.setOnItemClickListener(this);
	}
	
	public int getScrollListView() {
	    View c = listView.getChildAt(1);
	    if (c == null) return 0;
	    int firstVisiblePosition = listView.getFirstVisiblePosition();
	    int top = c.getTop();
	    return -top + firstVisiblePosition * c.getHeight();
	}
	
	public View getViewByPosition(int pos) {
		pos++;
	    final int firstListItemPosition = listView.getFirstVisiblePosition();
	    final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
	    if (pos < firstListItemPosition || pos > lastListItemPosition ) {
	        return listView.getAdapter().getView(pos, null, listView);
	    }
		return listView.getChildAt(pos - firstListItemPosition);
	}

	public void initSearchEngines(Context context, String valueEngines) {
		if (null == valueEngines) {
			sPref = MusicApp.getSharedPreferences();
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
		engines = new ArrayList<Engine>(engineArray.length);
		for (int i = 0; i < engineArray.length; i++) {
			Class<? extends BaseSearchTask> engineClass = getSearchEngineClass(engineArray[i][0]);
			int maxPages = Integer.parseInt(engineArray[i][1]);
			for (int page = 1; page <= maxPages; page++) {
				engines.add(new Engine(engineClass, page));
			}
		}
	}
	
	private void initBoxEngines() {
		ArrayList<String> list = getSettings().getEnginesArray(getContext());
		if (list.size() > 1) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				enginesAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_of_engine, list);
			} else {
				enginesAdapter = new CustomSpinnerAdapter(getContext(), R.layout.item_of_engine, list);	
			}
			int dropDownViewRes = Util.getResIdFromAttribute((Activity) getContext(), R.attr.dropDownViewResource);
			if (dropDownViewRes != 0) {
				enginesAdapter.setDropDownViewResource(dropDownViewRes);
			}
			spEnginesChoiser.setAdapter(enginesAdapter);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.view.ViewGroup.LayoutParams params = spEnginesChoiser.getLayoutParams();
				params.width = 170;
				spEnginesChoiser.setLayoutParams(params);
			}
			int id = enginesAdapter.getPosition(keyEngines);
			spEnginesChoiser.setSelection(id);
			spEnginesChoiser.setOnTouchListener(this);
			spEnginesChoiser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					lastSearchString = searchField.getText().toString();
					keyEngines = enginesAdapter.getItem(position);
					sPref = MusicApp.getSharedPreferences();
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
					
					// start search with new engine
					lastSearchString = "";
					String value = keyEngines;
					initSearchEngines(getContext(), value);
					String str = Util.removeSpecialCharacters(searchField.getText().toString());
					if (!str.isEmpty()) {
						trySearch();
					}
				} 

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
		} else {
			spEnginesChoiser.setVisibility(View.GONE);
		}
	}

	public void hideBaseProgress() {
		progress.setVisibility(View.GONE);
		if (null != progressSecond) {
			progressSecond.hide();
		}
	}
	
	public void showBaseProgress() {
		if (null != progressSecond) {
			progressSecond.show();
		} else {
			progress.setVisibility(View.VISIBLE);
		}
	}

	public static String getDownloadPath(Context context) {
		String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
		if (context != null) {
			SharedPreferences downloadDetails = context.getSharedPreferences(BaseSearchView.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
			String sharedDownloadPath = downloadDetails.getString(BaseSearchView.DOWNLOAD_DIR, "");
			if (sharedDownloadPath.equals("")) {
				Editor edit = downloadDetails.edit();
				edit.clear();
				edit.putString(BaseSearchView.DOWNLOAD_DIR, downloadPath);
				edit.commit();
			} else if (!new File(sharedDownloadPath).exists()) {
				return downloadPath;
			} else {
				return sharedDownloadPath;
			}
		}
		return downloadPath;
	}

	public Iterator<Engine> getTaskIterator() {
		return taskIterator;
	}

	public void setTaskIterator(Iterator<Engine> taskIterator) {
		this.taskIterator = taskIterator;
	}

	public static String getSimpleDownloadPath(String absPath) {
		return absPath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
	}

	public static void setDownloadPath(Context context, String downloadPath) {
		SharedPreferences downloadDetails = context.getSharedPreferences(BaseSearchView.DOWNLOAD_DETAIL, Context.MODE_PRIVATE);
		Editor edit = downloadDetails.edit();
		edit.clear();
		edit.putString(BaseSearchView.DOWNLOAD_DIR, downloadPath);
		edit.commit();
	}
	
	public static boolean isOffline(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo == null;
	}
	
	public void trySearch() {
		Util.hideKeyboard(getContext(), view);
		String searchString = searchField.getText().toString();
		if (searchString.equals(lastSearchString) && message.getVisibility() != View.VISIBLE) return;
		lastSearchString = searchString;
		if (isOffline(getContext())) {
			message.setText(R.string.search_message_no_internet);
			adapter.clear();
		} else if ((null == searchString) || (searchString.isEmpty())) {
			adapter.clear();
			message.setText(R.string.search_please_enter_query);
		} else {
			search(searchString);
		}
		try {
			if (getAdvertisment().isOnlineLib(getContext())) {
				getAdvertisment().searchStartLib(getContext());
			}
		} catch (Exception e) { }
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
				} catch (Exception e) {
				}
			}
		} catch (Exception e) { }
		return searchEngines;
	}
	
	@SuppressLint("DefaultLocale")
	public boolean isBlacklistedQuery(String songName) {
		List<String> dmcaSearchQueryBlacklist = getDMCABlacklistedItems("dmca_searchquery_blacklist");
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
		keeper.activateOptions(StateKeeper.SEARCH_EXE_OPTION);
		keeper.deactivateOptions(StateKeeper.SEARCH_STOP_OPTION);
		if (isBlacklistedQuery(songName)) {
			List<Engine> nothingSearch = new ArrayList<Engine>();
			Class<? extends BaseSearchTask> engineClass = getSearchEngineClass("SearchNothing");
			nothingSearch.add(new Engine(engineClass, 1));
			taskIterator = nothingSearch.iterator();
		} else {
			taskIterator = engines.iterator();
		}
		adapter.clear();
		setMessage("");
		showBaseProgress();
		getNextResults(true);
	}

	private void getNextResults(boolean cancel) {
		if (null == taskIterator) return;
		adapter.setVisibilityProgress(true);
		if (!taskIterator.hasNext()) {
			adapter.setVisibilityProgress(false);
			keeper.deactivateOptions(StateKeeper.SEARCH_EXE_OPTION);
			return;
		}
		if (null != searchTask && cancel) {
			searchTask.cancel(true);
		}
		try {
			Engine engine = taskIterator.next();
			String str = null != extraSearch ? extraSearch : searchField.getText().toString();
			extraSearch = null;
			searchTask = engine.getEngineClass().getConstructor(BaseSearchTask.PARAMETER_TYPES).newInstance(new Object[] { resultsListener, str});
			if (searchTask instanceof SearchWithPages) {
				int page = engine.getPage();
				((SearchWithPages) searchTask).setPage(page);
			}
			searchTask.execute(NO_PARAMS);
		} catch (Exception e) {
			getNextResults(false);
		}
	}

	public void getDownloadUrl(final View view, final int position) {
		if (isOffline(getContext())) {
			String msg =  getContext().getString(R.string.search_message_no_internet);
			showMessage(msg);
			return;
		}
		if (!onlyOnWifi()) {
			Toast.makeText(getContext(), getContext().getString(R.string.no_wi_fi), Toast.LENGTH_LONG).show();
			return;
		}
		downloadSong = (RemoteSong) adapter.getItem(position);
		if (null != alertProgressDialog && alertProgressDialog.isShowing()) {
			alertProgressDialog.cancel();
		}
		if (!showFullElement()) {
			click(view, position);
			return;
		}
		listViewImage = null;
		if (!((ImageView) view.findViewById(R.id.cover)).getContentDescription().equals(getResources().getString(R.string.default_cover))) {
			Drawable draw = ((ImageView) view.findViewById(R.id.cover)).getDrawable();
			if (null != listViewImage) {
				listViewImage.recycle();
			}
			listViewImage = ((BitmapDrawable) draw).getBitmap();
		}
		boolean isRestored = keeper.checkState(StateKeeper.PROGRESS_DIALOG);
		if (view.getId() != R.id.btnDownload) {
			stopSystemPlayer(getContext());
			dismissDialog();
			dismissProgressDialog();
			showProgressDialog(view, downloadSong, position);
		}
		if (!isRestored) {
			downloadSong.getDownloadUrl(new DownloadUrlListener() {

				@Override
				public void success(final String url) {
					downloadSong.setDownloadUrl(url);
					keeper.closeDialog(StateKeeper.PROGRESS_DIALOG);
					((Activity) getContext()).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							dismissProgressDialog();
							if (showFullElement()) {
								prepareSong(downloadSong, false, listViewImage);
							}
						}
					});
				}

				@Override
				public void error(final String error) {
					((Activity) getContext()).runOnUiThread(new Runnable() {

						@Override
						public void run() {
							dismissProgressDialog();
							keeper.closeDialog(StateKeeper.PROGRESS_DIALOG);
							Toast toast = Toast.makeText(getContext(), R.string.error_getting_url_songs, Toast.LENGTH_SHORT);
							toast.show();
						}
					});

				}
			});
		} else {
			downloadSong.addListener(new DownloadUrlListener() {

				@Override
				public void success(String url) {
					dismissProgressDialog();
				}

				@Override
				public void error(String error) {
					dismissProgressDialog();
				}
			});
		}
	}

	protected void dismissDialog(){
		if (null != alertDialog && alertDialog.isShowing()) {
			try {
				alertDialog.cancel();
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}
	}
	
	protected void dismissProgressDialog() {
		if (null != alertProgressDialog && alertProgressDialog.isShowing()) {
			try {
				alertProgressDialog.cancel();
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	protected void showProgressDialog(final View view, final RemoteSong downloadSong, final int position) {
		keeper.openDialog(StateKeeper.PROGRESS_DIALOG);
		View dialoglayout = LayoutInflater.from(getContext()).inflate(R.layout.progress_dialog, null);
		progressDialog = new AlertDialog.Builder(getContext());
		progressDialog.setView(dialoglayout);
		progressDialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface paramDialogInterface) {
				downloadSong.cancelTasks();
				hideView();
				keeper.closeDialog(StateKeeper.PROGRESS_DIALOG);
			}
		});
		alertProgressDialog = progressDialog.create();
		alertProgressDialog.show();
	}

	public void prepareSong(final RemoteSong remoteSong, boolean force, Bitmap listViewImage) {
		if (keeper.checkState(StateKeeper.STREAM_DIALOG) && !force) return;
		RemoteSong song = remoteSong.cloneSong();
		keeper.setDownloadSong(song);
		AlertDialog streamDialog = null;
		if (null == player) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			int layoutId = Util.getResIdFromAttribute(((Activity)getContext()), R.attr.download_dialog);
			View v = inflater.inflate(layoutId > 0 ? layoutId : R.layout.download_dialog, null);
			player = new Player(v, song);
			player.setTitle(song.getArtist() + " - " + song.getTitle());
			player.setIsAppPT(isAppPT());
			player.execute(song.getUrl());
			if (song instanceof GrooveSong) {
				player.setSongId(((GrooveSong) song).getSongId());
			}
			streamDialog = (AlertDialog) createStreamDialog(song);
			streamDialog.show();
			
			if (getSettings().getIsCoversEnabled(getContext())) {
				if (null != listViewImage) {
					player.setCover(getResizedBitmap(listViewImage, 200, 200));
					listViewImage = null;
				} else {
					player.setCoverFromSong(song);
				}
			} else {
				player.hideCoverProgress();
			}
		} else if (keeper.checkState(StateKeeper.STREAM_DIALOG)) {
			streamDialog = (AlertDialog) createStreamDialog(song);
			streamDialog.show();
			player.setTitle(song.getArtist() + " - " + song.getTitle());
		}
		downloadListener = new DownloadClickListener(getContext(), song, 0);
		if (getSettings().getIsCoversEnabled(getContext())) {
			boolean hasCover = ((RemoteSong) song).getCover(new OnBitmapReadyListener() {
				
				@Override
				public void onBitmapReady(Bitmap bmp) {}
			});
			if (!hasCover) {
				player.hideCoverProgress();
				player.setCover(null);
			}
		}
		
		int lableStatus = keeper.checkSongInfo(song.getComment());
		if (lableStatus == StateKeeper.DOWNLOADED) {
			song.setPath(keeper.getSongPath(song.getComment()));
		}
		if (lableStatus != -1) {
			streamDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);	
		}
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth = ((float) newWidth) / width;
	    float scaleHeight = ((float) newHeight) / height;
	    Matrix matrix = new Matrix();
	    matrix.postScale(scaleWidth, scaleHeight);
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
	    return resizedBitmap;
	}
	
	@SuppressLint("NewApi")
	public Dialog createStreamDialog(final RemoteSong song) {
		keeper.openDialog(StateKeeper.STREAM_DIALOG);
		keeper.setTitleArtistLyrics( new String[] {song.getTitle(), song.getArtist()});
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
		b.setNegativeButton(R.string.download_dialog_cancel, null);
		b.setPositiveButton(R.string.download_dialog_download, null);
		alertDialog = b.create();
		alertDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				keeper.closeDialog(StateKeeper.STREAM_DIALOG);
				getContext().unregisterReceiver(headsetReceiver);
				if (telephonyManager != null) {
					telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
				}
				if (player != null) {
					player.cancel();
					player = null;
				}
				dialog.dismiss();
			}
		});
		alertDialog.setOnShowListener(dialogShowListener);
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		return alertDialog;
	}

	private void showDownloadsList() {
		Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getContext().startActivity(dm);
	}

	public void setSearchField(String str) {
		lastSearchString = str;
		searchField.setText(str);
	}
	
	public int getClickPosition() {
		return clickPosition;
	}
	
	protected void showMessage(String msg) {
		Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	protected void animateListView(boolean isRestored) { } // Animate list view in childs, if need
	
	public void restoreAdapter(ArrayList<Song> list, int position) {
		adapter.add(list);
		listView.addHeaderView(emptyHeader);
		listView.addFooterView(adapter.getProgressView(), null, false);
		listView.setAdapter(adapter);
		animateListView(true);
		listView.setSelection(position);
		isRestored = true;
	}
	
	public RemoteSong getDownloadSong() {
		return downloadSong;
	}
	
	public void setDownloadSong(RemoteSong downloadSong) {
		this.downloadSong = downloadSong;
	}

	public TextView getSearchField() {
		return searchField;
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
	
	public void setPlayer(Player player) {
		this.player = player;
	}

	public View getViewItem() {
		return viewItem;
	}

	public void setExtraSearch(String extraSearch) {
		this.extraSearch = extraSearch;
	}

	public int getListViewPosition() {
		return listView.getFirstVisiblePosition();
	}

	public void notifyAdapter() {
		adapter.notifyDataSetChanged();
	}

	public static String getTitleSearchEngine() {
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		return prefs.getString("search_engines_title", "Search Engine 1");
	}
	
	public String getMessage() {
		return message.getText().toString();
	}
	
	public void setMessage(String msg) {
		message.setText(msg);
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

	public Class<? extends BaseSearchTask> getSearchEngineClass(String searchEngineName) {
		if (null != searchEngineName) {
			switch (searchEngineName) {
			case "SearchVmusice":
				return SearchVmusice.class;
			case "SearchZvukoff":
				return SearchZvukoff.class;
			case "SearchPleer":
				return SearchPleer.class;
			case "SearchPleerV2":
				return SearchPleerV2.class;
			case "SearchSoArdIyyin":
				return SearchSoArdIyyin.class;
			case "SearchMyFreeMp3":
				return SearchMyFreeMp3.class;
			case "SearchPoisk":
				return SearchPoisk.class;
			case "SearchHulkShare":
				return SearchHulkShare.class;
			case "SearchMp3skull":
				return SearchMp3skull.class;
			case "SearchGrooveshark":
				return SearchGrooveshark.class;
			case "SearchTing":
				return SearchTing.class;
			case "SearchJamendo":
				return SearchJamendo.class;
			case "SearchYouTube":
				return SearchYouTube.class;
			case "SearchVK":
				return SearchVK.class;
			case "SearchTaringaMp3":
				return SearchTaringaMp3.class;
			case "SearchKugou":
				return SearchKugou.class;
			case "SearchGoearV2":
				return SearchGoearV2.class;
			case "SearchSogou":
				return SearchSogou.class;
			case "SearchYouTubeMusic":
				return SearchYouTubeMusic.class;
			case "SearchSoundCloud":
				return SearchSoundCloud.class;
			case "SearchMp3World":
				return SearchMp3World.class;
			}
		}
		return SearchPleer.class;
	}
}
