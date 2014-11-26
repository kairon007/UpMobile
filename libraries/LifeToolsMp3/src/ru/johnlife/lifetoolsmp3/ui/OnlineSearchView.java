package ru.johnlife.lifetoolsmp3.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.RefreshListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
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
import ru.johnlife.lifetoolsmp3.ui.dialog.CustomDialogBuilder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public abstract class OnlineSearchView extends View {

	public static List<Engine> engines = null;
	private static final Void[] NO_PARAMS = {};
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";
	private final String SPREF_ENGINES = "shar_pref_key_engines_array";
	private final String SPREF_CURRENT_ENGINES = "pref_key_current_engines_array";
	private ArrayAdapter<String> adapter;
	private Iterator<Engine> taskIterator;
	private SharedPreferences sPref;
	protected DownloadClickListener downloadListener;
	private StateKeeper keeper;
	private Player player;
	private RemoteSong downloadSong;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private SongSearchAdapter resultAdapter;
	private ViewGroup view;
	private View spEnginesChoiserLayout;
	private View progress;
	private View viewItem;
	private TextView message;
	private TextView searchField;
	private Spinner spEnginesChoiser;
	private ListView listView;
	protected AlertDialog.Builder progressDialog;
	protected AlertDialog alertProgressDialog;
	private AlertDialog alertDialog;
	private Bitmap listViewImage;
	private String extraSearch = null;
	private String keyEngines;
	private int initialHeight;
	private int clickPosition;
	private boolean isRestored = false;
	
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

	
	OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			String value = sharedPreferences.getString(key, null);
			initSearchEngines(getContext(), value);
			String str = Util.removeSpecialCharacters(searchField.getText().toString());
			if (!resultAdapter.isEmpty() && !str.equals("")) {
				trySearch();
			}
		}
	};

	FinishedParsingSongs resultsListener = new FinishedParsingSongs() {

		@Override
		public void onFinishParsing(List<Song> songsList) {
			resultAdapter.hideProgress();
			if (keeper.checkState(StateKeeper.SEARCH_STOP_OPTION)) return;
			//TODO: set result
			if (songsList.isEmpty()) {
				getNextResults();
				if (!taskIterator.hasNext() && resultAdapter.isEmpty()) {
					try {
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
			}
		}
	};

	protected abstract BaseSettings getSettings();

	protected abstract Advertisment getAdvertisment();

	protected abstract void stopSystemPlayer(Context context);
	
	public abstract void refreshLibrary();

	public abstract boolean isWhiteTheme(Context context);

	protected boolean showFullElement() {
		return true;
	}

	protected void click(View view, int position) {

	}

	protected boolean onlyOnWifi() {
		return true;
	}
	
	public OnlineSearchView(LayoutInflater inflater) {
		super(inflater.getContext());
		init(inflater);
		initSearchEngines(getContext(), null);
		keeper = StateKeeper.getInstance();
		keeper.restoreState(this);
		if (resultAdapter == null) {
			resultAdapter = new SongSearchAdapter(getContext());
		}
		sPref = getContext().getSharedPreferences(SPREF_ENGINES, Context.MODE_PRIVATE);
		keyEngines = sPref.getString(SPREF_CURRENT_ENGINES, getTitleSearchEngine());
		sPref.registerOnSharedPreferenceChangeListener(sPrefListener);
		float width = searchField.getPaint().measureText(getResources().getString(R.string.hint_main_search));
		if (searchField.getWidth() - ((ImageView) view.findViewById(R.id.clear)).getWidth() < width) {
			searchField.setHint(Html.fromHtml("<small>" + getResources().getString(R.string.hint_main_search) + "</small>"));
		} else searchField.setHint(R.string.hint_main_search);
		if (keeper.checkState(StateKeeper.SEARCH_EXE_OPTION) && resultAdapter.isEmpty()) search(searchField.getText().toString());
		initBoxEngines();
	}

	public View getView() {
		if (!showFullElement()) {
			view.findViewById(R.id.downloads).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.downloads).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					showDownloadsList();
				}
			});
		}
		listView.addFooterView(resultAdapter.getProgress());
		if (!isRestored) listView.setAdapter(resultAdapter);
		if (isWhiteTheme(getContext()) || Util.getThemeName(getContext()).equals(Util.WHITE_THEME)) {
			if (isWhiteTheme(getContext())) {
				listView.setDividerHeight(0);
			} else {
				listView.setDivider(getContext().getResources().getDrawable(R.drawable.layout_divider));
			}
			listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
			view.findViewById(R.id.search_field).setBackgroundResource(R.drawable.search_background_white);
			view.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
			int color = getContext().getResources().getColor(android.R.color.black);
			searchField.setTextColor(color);
			message.setTextColor(color);
			if (null != spEnginesChoiserLayout) {
				spEnginesChoiserLayout.setBackgroundResource(R.drawable.spinner_background);
			}
		}
		listView.setEmptyView(message);
		initialHeight = spEnginesChoiserLayout.getLayoutParams().height;
		listView.setOnScrollListener(new OnScrollListener() {
			
			int lastScroll = 0;
			int lastVisibleItem = 0;
			int defaultHeight = spEnginesChoiserLayout.getLayoutParams().height;
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				View firstItem = listView.getChildAt(0);
				if (null == firstItem) return;
				int itemHeight = firstItem.getHeight();
				int scrollBy = 0;
				if (firstVisibleItem == lastVisibleItem) {
					scrollBy = firstItem.getTop() - lastScroll;
				} else if (firstVisibleItem < lastVisibleItem) {
					scrollBy = firstItem.getTop() - lastScroll + itemHeight;
				} else if (firstVisibleItem > lastVisibleItem) {
					scrollBy = firstItem.getTop() - lastScroll - itemHeight;
				}
				int resultHeight = spEnginesChoiserLayout.getLayoutParams().height + scrollBy;
				if (resultHeight < 0) {
					spEnginesChoiserLayout.getLayoutParams().height = 0;
				} else if (resultHeight > defaultHeight) {
					spEnginesChoiserLayout.getLayoutParams().height = defaultHeight;
				} else {
					spEnginesChoiserLayout.getLayoutParams().height += scrollBy;
				}
				spEnginesChoiserLayout.requestLayout();
				lastVisibleItem = firstVisibleItem;
				lastScroll = firstItem.getTop();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				if (position == resultAdapter.getCount()) return; // progress click
				viewItem = view;
				clickPosition = position;
				getDownloadUrl(view, position);
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
		searchField.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				expandEngines();
			}
		});
		view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchField.setText(null);
				message.setText(R.string.search_your_results_appear_here);
				resultAdapter.clear();
				keeper.activateOptions(StateKeeper.SEARCH_STOP_OPTION);
				keeper.deactivateOptions(StateKeeper.SEARCH_EXE_OPTION);
				progress.setVisibility(View.GONE);
				expandEngines();
			}
		});
		view.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				trySearch();
				collapseEngines();
			}
		});
		if (extraSearch != null) {
			trySearch();
			return view;
		}
		if (keeper.checkState(StateKeeper.SEARCH_EXE_OPTION) && resultAdapter.isEmpty()) progress.setVisibility(View.VISIBLE);
		else progress.setVisibility(View.GONE);
		return view;
	}

	public void initSearchEngines(Context context, String valueEngines) {
		// TODO this set number engines
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
	
	private void initBoxEngines() {
		ArrayList<String> list = getSettings().getEnginesArray(getContext());
		if (list.size() > 1) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				adapter = new ArrayAdapter<String>(getContext(), R.layout.item_of_engine, list);
			} else {
				adapter = new CustomSpinnerAdapter(getContext(), 0, list, isWhiteTheme(getContext()));
			}
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spEnginesChoiser.setAdapter(adapter);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.view.ViewGroup.LayoutParams params = spEnginesChoiser.getLayoutParams();
				params.width = 170;
				spEnginesChoiser.setLayoutParams(params);
			}
			int id = adapter.getPosition(keyEngines);
			spEnginesChoiser.setSelection(id);
			spEnginesChoiser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					collapseEngines();
					keyEngines = (String) adapter.getItem(position);
					sPref = getContext().getSharedPreferences(SPREF_ENGINES, Context.MODE_PRIVATE);
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
	}

	private void init(LayoutInflater inflater) {
		view = (ViewGroup) inflater.inflate(R.layout.search, null);
		message = (TextView) view.findViewById(R.id.message);
		progress = view.findViewById(R.id.progress);
		listView = (ListView) view.findViewById(R.id.list);
		searchField = (TextView) view.findViewById(R.id.text);
		spEnginesChoiser = (Spinner) view.findViewById(R.id.choise_engines);
		spEnginesChoiserLayout = (View) view.findViewById(R.id.choise_engines_layout);
	}

	private void collapseEngines() {
		keeper.deactivateOptions(StateKeeper.IS_EXPANDING_OPTION);
		if (!searchField.getText().toString().isEmpty()) {
			Animation anim = new Animation() {

				@Override
				protected void applyTransformation(float interpolatedTime, Transformation t) {
					if (interpolatedTime == 1) {
						spEnginesChoiserLayout.getLayoutParams().height = 0;
					} else {
						spEnginesChoiserLayout.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
						spEnginesChoiserLayout.requestLayout();
					}
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};
			anim.setDuration(200);
			spEnginesChoiserLayout.startAnimation(anim);
		}
	}

	private void expandEngines() {
		keeper.activateOptions(StateKeeper.IS_EXPANDING_OPTION);
		Animation anim = new Animation() {

			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				spEnginesChoiserLayout.getLayoutParams().height = (int)(initialHeight * interpolatedTime);
				spEnginesChoiserLayout.requestLayout();
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};
		anim.setDuration(200);
		spEnginesChoiserLayout.startAnimation(anim);
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

		private SongSearchAdapter(Context context) {
			super(context, -1, new ArrayList<Song>());
			this.inflater = LayoutInflater.from(getContext());
			this.footer = new FrameLayout(context);
			this.refreshSpinner = new ProgressBar(context);
			refreshSpinner.setIndeterminate(true);
			footer.addView(refreshSpinner, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
			refreshSpinner.setVisibility(View.GONE);
		}
		
		public ArrayList<Song> getAll() {
			ArrayList<Song> list = new ArrayList<Song>();
			for (int i = 0; i < getCount(); i++) {
				list.add(getItem(i));
			}
			return list;
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
					.setIcon(isWhiteTheme(getContext()) ? R.drawable.fallback_cover_white : R.drawable.fallback_cover)
					.setButtonVisible(showFullElement() ? false : true);
			if (getSettings().getIsCoversEnabled(getContext())) {
				((RemoteSong) song).getSmallCover(false, new OnBitmapReadyListener() {
					@Override
					public void onBitmapReady(Bitmap bmp) {
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
			v.findViewById(R.id.boxInfoItem).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					listView.performItemClick(v, position, v.getId());
				}
			});
			if (!showFullElement()) {
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
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
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
		keeper.activateOptions(StateKeeper.SEARCH_EXE_OPTION);
		keeper.deactivateOptions(StateKeeper.SEARCH_STOP_OPTION);
		if (isBlacklistedQuery(songName)) {
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
		message.setText("");
		progress.setVisibility(View.VISIBLE);
		getNextResults();
	}

	private void getNextResults() {
		resultAdapter.refreshSpinner.setVisibility(View.VISIBLE);
		if (!taskIterator.hasNext()) {
			resultAdapter.hideProgress();
			keeper.deactivateOptions(StateKeeper.SEARCH_EXE_OPTION);
			return;
		}
		try {
			Engine engine = taskIterator.next();
			String str = null != extraSearch ? extraSearch : searchField.getText().toString();
			extraSearch = null;
			BaseSearchTask searchTask = engine.getEngineClass().getConstructor(BaseSearchTask.PARAMETER_TYPES).newInstance(new Object[] { resultsListener, str});
			if (searchTask instanceof SearchWithPages) {
				int page = engine.getPage();
				((SearchWithPages) searchTask).setPage(page);
			}
			searchTask.execute(NO_PARAMS);
		} catch (Exception e) {
			getNextResults();
		}
	}

	public void getDownloadUrl(final View view, final int position) {
		if (isOffline(getContext())) {
			Toast.makeText(getContext(), getContext().getString(R.string.search_message_no_internet), Toast.LENGTH_LONG).show();
			return;
		}
		if (!onlyOnWifi()) {
			Toast.makeText(getContext(), getContext().getString(R.string.no_wi_fi), Toast.LENGTH_LONG).show();
			return;
		}
		if (!showFullElement()) {
			click(view, position);
			return;
		}
		listViewImage = null;
		if (!((ImageView) view.findViewById(R.id.cover)).getContentDescription().equals(getResources().getString(R.string.default_cover))) {
			Drawable draw = ((ImageView) view.findViewById(R.id.cover)).getDrawable();
			listViewImage = ((BitmapDrawable) draw).getBitmap();
		} 
		boolean isRestored = keeper.checkState(StateKeeper.PROGRESS_DIALOG);
		downloadSong = (RemoteSong) resultAdapter.getItem(position);
		if (view.getId() != R.id.btnDownload) {
			stopSystemPlayer(getContext());
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
				public void error(String error) {
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

	protected void dismissProgressDialog() {
		keeper.closeDialog(StateKeeper.PROGRESS_DIALOG);
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
		if (null == player) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(isWhiteTheme(getContext()) ? R.layout.download_dialog_white : R.layout.download_dialog, null);
			player = new Player(v, song, isWhiteTheme(getContext()));
			player.setTitle(song.getArtist() + " - " + song.getTitle());
			player.execute(song.getUrl());
			if (song instanceof GrooveSong) {
				player.setSongId(((GrooveSong) song).getSongId());
			}
			createStreamDialog(song).show();
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
		} else {
			if (keeper.checkState(StateKeeper.STREAM_DIALOG)) {
				createStreamDialog(song).show();
				player.setTitle(song.getArtist() + " - " + song.getTitle());
			}
		}
		downloadListener = new DownloadClickListener(getContext(), song, new RefreshListener() {

			@Override
			public void success() {
				refreshLibrary();
			}
		});
		if (getSettings().getIsCoversEnabled(getContext())) {
			boolean hasCover = ((RemoteSong) song).getCover(true, downloadListener);
			if (!hasCover) {
				player.hideCoverProgress();
				player.setCover(null);
			}
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
		AlertDialog.Builder b = CustomDialogBuilder.getBuilder(getContext(), isWhiteTheme(getContext())).setView(player.getView());
		b.setNegativeButton(R.string.download_dialog_cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
			}

		});
		b.setPositiveButton(R.string.download_dialog_download, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadListener.setSong(song);
				downloadListener.setUseAlbumCover(keeper.isUseCover());
				downloadListener.downloadSong(false);
				dialog.cancel();
			}
		});
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
		final Context context = message.getContext();
		Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(dm);
	}

	public void setSearchField(String str) {
		searchField.setText(str);
	}
	
	public int getClickPosition() {
		return clickPosition;
	}
	
	public void restoreAdapter(ArrayList<Song> list, int position) {
		resultAdapter = new SongSearchAdapter(getContext());
		for (Song song : list) {
			resultAdapter.add(song);
		}
		listView.setAdapter(resultAdapter);
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
		resultAdapter.notifyDataSetChanged();
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
}
