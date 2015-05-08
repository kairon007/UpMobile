package ru.johnlife.lifetoolsmp3.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity.DownloadPressListener;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper;
import ru.johnlife.lifetoolsmp3.adapter.AdapterHelper.ViewBuilder;
import ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter;
import ru.johnlife.lifetoolsmp3.adapter.CustomSpinnerAdapter;
import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.engines.BaseSearchTask;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.engines.FinishedParsingSongs;
import ru.johnlife.lifetoolsmp3.engines.SearchWithPages;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class OnlineSearchView extends View {

	public static final String EMPTY_DIRECTORY = "directory.not.create.for.application";
	public static List<Engine> engines = null;
	private static final Void[] NO_PARAMS = {};
	private static String DOWNLOAD_DIR = "DOWNLOAD_DIR";
	private static String DOWNLOAD_DETAIL = "DOWNLOAD_DETAIL";
	
	protected AlertDialog.Builder progressDialog;
	protected AlertDialog alertProgressDialog;
	protected DownloadClickListener downloadListener;
	protected ListView listView;
	protected ProgressDialog progressSecond;	// For PtMusicAppOffline

	private final String SPREF_CURRENT_ENGINES = "pref_key_current_engines_array";
	private int clickPosition;
	private boolean isRestored = false;
	private String extraSearch = null;
	private String keyEngines;
	private ArrayAdapter<String> adapter;
	private Iterator<Engine> taskIterator;
	private SharedPreferences sPref;
	private StateKeeper keeper;
	private Player player;
	private RemoteSong downloadSong;
	private TelephonyManager telephonyManager;
	private HeadsetIntentReceiver headsetReceiver;
	private SongSearchAdapter resultAdapter;
	private ViewGroup view;
	private View spEnginesChoiserLayout;
	private View spEnginesChoiserScroll;
	private View emptyHeader;
	private View progress;
	private View viewItem;
	private FrameLayout footer;
	private TextView message;
	private TextView searchField;
	private Spinner spEnginesChoiser;
	private AlertDialog alertDialog;
	private Bitmap listViewImage;
	private BaseSearchTask searchTask;
	
	OnShowListener dialogShowListener = new OnShowListener() {

		@Override
		public void onShow(DialogInterface dialog) {
			float textSize = 16f;
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setTextSize(textSize);
			((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setTextSize(textSize);
		}
	};
	
	public abstract void refreshLibrary();
	public abstract boolean isWhiteTheme(Context context);
	public void specialInit(View view) {}
	public boolean isUseDefaultSpinner(){ return false; }
	
	protected abstract BaseSettings getSettings();
	protected abstract Nulldroid_Advertisment getAdvertisment();
	protected abstract void stopSystemPlayer(Context context);
	protected void hideView () { }	//hide player in MusicDownloder application
	protected void click(View view, int position) { Util.hideKeyboard(getContext(), view); }
	protected boolean showDownloadLabel() { return false; }
	protected boolean showFullElement() { return true; }
	protected boolean showPopupMenu() { return false; }
	protected boolean showDownloadButton() { return showFullElement() ? false : true; }
	protected boolean isAppPT () { return false; }
	protected boolean onlyOnWifi() { return true; }
	protected boolean usePlayingIndicator() { return false; }
	protected int getDropDownViewResource() { return 0;	}
	protected int getAdapterBackground () { return 0; }
	protected int  getIdCustomView() { return 0; }
	protected int getCustomColor() {return 0;}
	protected String getDirectory() { return null; }
	protected void showShadow (boolean visible) { }

	public OnlineSearchView(LayoutInflater inflater) {
		super(inflater.getContext());
		init(inflater);
		initSearchEngines(getContext(), null);
		keeper = StateKeeper.getInstance();
		keeper.restoreState(this);
		if (resultAdapter == null) {
			resultAdapter = new SongSearchAdapter(getContext());
		}
		sPref = MusicApp.getSharedPreferences();
		keyEngines = sPref.getString(SPREF_CURRENT_ENGINES, getTitleSearchEngine());
		sPref.registerOnSharedPreferenceChangeListener(sPrefListener);
		float width = searchField.getPaint().measureText(getResources().getString(R.string.hint_main_search));
		if (searchField.getWidth() - view.findViewById(R.id.clear).getWidth() < width) {
			searchField.setHint(Html.fromHtml("<small>" + getResources().getString(R.string.hint_main_search) + "</small>"));
		} else searchField.setHint(R.string.hint_main_search);
		if (keeper.checkState(StateKeeper.SEARCH_EXE_OPTION) && resultAdapter.isEmpty()) search(searchField.getText().toString());
		setMessage(getResources().getString(R.string.search_your_results_appear_here));
		initBoxEngines();
		if (showDownloadLabel()) {
			((BaseMiniPlayerActivity) getContext()).setDownloadPressListener(downloadPressListener);
		}
	}
	
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

	
	private OnSharedPreferenceChangeListener sPrefListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.contains(SPREF_CURRENT_ENGINES)) {
				lastSearchString = "";
				String value = sharedPreferences.getString(key, null);
				initSearchEngines(getContext(), value);
				String str = Util.removeSpecialCharacters(searchField.getText().toString());
				if (!resultAdapter.isEmpty() && !str.equals("")) {
					trySearch();
				}
			}
		}
	};
	
	DownloadPressListener downloadPressListener = new DownloadPressListener() {
		
		@Override
		public void downloadButtonPressed(final RemoteSong song) {
			((Activity) getContext()).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					View v = getViewByPosition(getResultAdapter().getPosition(song));
					if (null == ((TextView) v.findViewById(R.id.infoView))) return;
					((TextView) v.findViewById(R.id.infoView)).setVisibility(View.VISIBLE);
					((TextView) v.findViewById(R.id.infoView)).setText(R.string.downloading);
					((TextView) v.findViewById(R.id.infoView)).setTextColor(Color.RED);
				}
			});
		}
	};

	FinishedParsingSongs resultsListener = new FinishedParsingSongs() {

		@Override
		public void onFinishParsing(List<Song> songsList) {
			hideRefreshProgress();
			if (keeper.checkState(StateKeeper.SEARCH_STOP_OPTION)) {
				resultAdapter.clear();
				return;
			}
			if (songsList.isEmpty()) {
				getNextResults(false);
				if (!taskIterator.hasNext() && resultAdapter.isEmpty()) {
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
							resultAdapter.add(song);
						}
					}
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), e + "");
				}
			}
		}
	};
	
	private boolean contains (Song song) {
		for (int i = 0; i < resultAdapter.getCount(); i++) {
			if (((Song) resultAdapter.getItem(i)).getComment().equals(song.getComment())) {
				return true;
			}
		}
		return false;
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
		if (!isRestored) {
			listView.addHeaderView(emptyHeader);
			listView.addFooterView(resultAdapter.getProgress(), null, false);
			listView.setAdapter(resultAdapter);
			animateListView(false);
		}
		if (isWhiteTheme(getContext()) || Util.getThemeName(getContext()).equals(Util.WHITE_THEME)) {
			if (isWhiteTheme(getContext())) {
				listView.setDividerHeight(0);
			} else {
				listView.setDivider(getContext().getResources().getDrawable(R.drawable.layout_divider));
			}
			listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
			view.setBackgroundColor(getContext().getResources().getColor(android.R.color.white));
			int color = getContext().getResources().getColor(android.R.color.black);
			searchField.setTextColor(color);
			message.setTextColor(color);
			if (null != spEnginesChoiserLayout) {
				spEnginesChoiserLayout.setBackgroundResource(R.drawable.spinner_background);
			}
		}
		listView.setEmptyView(message);
		listView.setOnScrollListener(new OnScrollListener() {
			
			int lastScroll = getScrollListView();
			int maxScroll = spEnginesChoiserScroll.getLayoutParams().height;
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
						showShadow(false);
					}
				}
			}
			
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				try {
					if (position == resultAdapter.getCount()) return; // progress click
//					if (null != keeper.getPlayingSong()) {
//						keeper.getPlayingSong().getSpecial().setChecked(false);
//					}
					((AbstractSong) resultAdapter.getItem(position)).getSpecial().setChecked(true);
					keeper.setPlayingSong(((AbstractSong) resultAdapter.getItem(position)));
					getResultAdapter().notifyDataSetChanged();
					viewItem = view;
					clickPosition = position;
					getDownloadUrl(view, position);
				} catch(Exception e) {
					e.printStackTrace();
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
		view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.hideKeyboard(getContext(), v);
				searchField.setText(null);
				setMessage(getResources().getString(R.string.search_your_results_appear_here));
				resultAdapter.clear();
				keeper.activateOptions(StateKeeper.SEARCH_STOP_OPTION);
				keeper.deactivateOptions(StateKeeper.SEARCH_EXE_OPTION);
				hideBaseProgress();
			}
		});
		view.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.hideKeyboard(getContext(), v);
				ImageLoader.getInstance().stop();
				trySearch();
			}

		});
		view.findViewById(R.id.touch_interceptor).setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN && v.getId() != R.id.text) {
					Util.hideKeyboard(getContext(), v);
					searchField.setFocusable(false);
				}
				return v.performClick();
			}
		});
		searchField.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					v.setFocusableInTouchMode(true);
				}
				return v.performClick();
			}
		});
		if (extraSearch != null) {
			searchField.setText(extraSearch);
			trySearch();
			return view;
		}
		if (keeper.checkState(StateKeeper.SEARCH_EXE_OPTION) && resultAdapter.isEmpty()) {
			showBaseProgress();
			message.setVisibility(View.GONE);
		} else {
			hideBaseProgress();
		}
		searchField.setFocusable(false);
		return view;
	} 
	
	private void init(LayoutInflater inflater) {
		view = (ViewGroup) inflater.inflate(R.layout.search, null);
		message = (TextView) view.findViewById(R.id.message);
		progress = view.findViewById(R.id.progress);
		listView = (ListView) view.findViewById(R.id.list);
		searchField = (TextView) view.findViewById(R.id.text);
		spEnginesChoiser = (Spinner) view.findViewById(R.id.choise_engines);
		spEnginesChoiserLayout = view.findViewById(R.id.choise_engines_layout);
		spEnginesChoiserScroll = view.findViewById(R.id.search_scroll);
		emptyHeader = inflate(getContext(), R.layout.empty_header, null);
		specialInit(view);
	}
	
	public int getScrollListView() {
	    View c = listView.getChildAt(1);
	    if (c == null) return 0;
	    int firstVisiblePosition = listView.getFirstVisiblePosition();
	    int top = c.getTop();
	    return -top + firstVisiblePosition * c.getHeight();
	}
	
	@SuppressLint("NewApi")
	public void showMenu(final View v) {
		PopupMenu menu = new PopupMenu(getContext(), v);
		final int position = (Integer) v.getTag();
		menu.getMenuInflater().inflate(R.menu.search_menu, menu.getMenu());
		boolean isDownloaded = StateKeeper.NOT_DOWNLOAD != keeper.checkSongInfo(((RemoteSong) getResultAdapter().getItem((Integer) v.getTag())).getComment());
		if (isDownloaded) {
			menu.getMenu().getItem(1).setVisible(false);
			
		}
		menu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem paramMenuItem) {
				if (paramMenuItem.getItemId() == R.id.search_menu_play) {
					((AbstractSong) resultAdapter.getItem(position)).getSpecial().setChecked(true);
					keeper.setPlayingSong(((AbstractSong) resultAdapter.getItem(position)));
					getResultAdapter().notifyDataSetChanged();
					click(null, position);
				}
				if (paramMenuItem.getItemId() == R.id.search_menu_download) {
					((BaseMiniPlayerActivity) getContext()).hideDownloadButton(true);
					((RemoteSong) getResultAdapter().getItem((Integer) v.getTag())).getDownloadUrl(new DownloadUrlListener() {
						
						@Override
						public void success(String url) {
							((RemoteSong) getResultAdapter().getItem((Integer) v.getTag())).setDownloadUrl(url);
							View viewByPosition = getViewByPosition(getResultAdapter().getPosition(((RemoteSong) getResultAdapter().getItem((Integer) v.getTag()))));
							download(viewByPosition,((RemoteSong) getResultAdapter().getItem((Integer) v.getTag())) , position);
						}
						
						@Override
						public void error(String error) {
						}
					});
					
				}
				return false;
			}
		});
		menu.show();
	}
	
	protected void download(final View v, RemoteSong song, final int position) {
		downloadListener = new DownloadClickListener(getContext(), song, 0);
		downloadListener.setDownloadPath(getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
	}
	
	public View getViewByPosition(int pos) {
		pos = pos + 1;
	    final int firstListItemPosition = listView.getFirstVisiblePosition();
	    final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

	    if (pos < firstListItemPosition || pos > lastListItemPosition ) {
	        return listView.getAdapter().getView(pos, null, listView);
	    }
		final int childIndex = pos - firstListItemPosition;
		return listView.getChildAt(childIndex);
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
				adapter = new ArrayAdapter<String>(getContext(), R.layout.item_of_engine, list);
			} else {
				adapter = new CustomSpinnerAdapter(getContext(), R.layout.item_of_engine, list, isWhiteTheme(getContext()));	
			}
			if (!isUseDefaultSpinner()) {
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			} else {
				if (getDropDownViewResource() > 0) {
					adapter.setDropDownViewResource(getDropDownViewResource());
				}
			}
			spEnginesChoiser.setAdapter(adapter);
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.view.ViewGroup.LayoutParams params = spEnginesChoiser.getLayoutParams();
				params.width = 170;
				spEnginesChoiser.setLayoutParams(params);
			}
			int id = adapter.getPosition(keyEngines);
			spEnginesChoiser.setSelection(id);
			spEnginesChoiser.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight() < 400) {
						Util.hideKeyboard(getContext(), v);
					}
					return false;
				}
			});
			spEnginesChoiser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					lastSearchString = searchField.getText().toString();
					keyEngines = adapter.getItem(position);
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
	
	private View refreshSpinner;
	private String lastSearchString = "";
	
	public Object initRefreshProgress() {
		return new View(getContext());
	}
	
	public void showRefreshProgress() {
		refreshSpinner.setVisibility(View.VISIBLE);
		footer.setVisibility(View.VISIBLE);
	}
	
	public void hideRefreshProgress() {
		refreshSpinner.setVisibility(View.GONE);
		footer.setVisibility(View.GONE);
	} 
	
	public int defaultCover() {
		return R.drawable.fallback_cover;
	}
	
	public Bitmap getDeafultBitmapCover() {
		return ((BitmapDrawable) getResources().getDrawable(defaultCover())).getBitmap();
	}
	
	public class SongSearchAdapter extends BaseAbstractAdapter<Song> {

		private LayoutInflater inflater;

		private SongSearchAdapter(Context context) {
			super(context, -1, new ArrayList<Song>());
			refreshSpinner = (View) initRefreshProgress();
			this.inflater = LayoutInflater.from(getContext());
			footer = new FrameLayout(context);
			int footerHeight = Util.dpToPx(getContext(), 72);
			int progressSize = Util.dpToPx(getContext(), 48);
			footer.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, footerHeight));
			footer.addView(refreshSpinner, new FrameLayout.LayoutParams(progressSize, progressSize, Gravity.CENTER));
			hideRefreshProgress();
		}
		
		public ArrayList<Song> getAll() {
			ArrayList<Song> list = new ArrayList<Song>();
			for (int i = 0; i < getCount(); i++) {
				list.add((Song) getItem(i));
			}
			return list;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Song song = (Song) getItem(position);
			final ViewBuilder builder = AdapterHelper.getViewBuilder(convertView, inflater, isWhiteTheme(getContext()), getIdCustomView(), usePlayingIndicator());
			String title = song.getTitle().replace("&#039;", "'");
			String artist = song.getArtist().replace("&#039;", "'");
			String comment = song.getComment();
			song.getSpecial().setChecked(((RemoteSong) song).equals(StateKeeper.getInstance().getPlayingSong()));
//			if (((RemoteSong) song).equals(StateKeeper.getInstance().getPlayingSong())) {
//				song.getSpecial().setChecked(true);
//			} else {
//				song.getSpecial().setChecked(false);
//			}
			song.setTitle(title);
			song.setArtist(artist);
			int lableStatus = keeper.checkSongInfo(comment);
			if (lableStatus == StateKeeper.DOWNLOADED) {
				song.setPath(keeper.getSongPath(comment));
			}
			builder.setLine1(artist, Util.getFormatedStrDuration(song.getDuration()))
				   .setLongClickable(false)
				   .setExpandable(false)
				   .setLine2(title)
				   .setDownloadLable(showDownloadLabel() ? lableStatus : -1)
				   .setId(position)
				   .showPlayingIndicator(((AbstractSong) song).getSpecial().getIsChecked())
				   .setIcon(isWhiteTheme(getContext()) ? R.drawable.fallback_cover_white : defaultCover() > 0 ? defaultCover() : getDeafultBitmapCover())
				   .setButtonVisible(showDownloadButton() ? true : false);
			if (getSettings().getIsCoversEnabled(getContext()) && ((RemoteSong)song).isHasCoverFromSearch()) {
				((RemoteSong) song).getSmallCover(false, new OnBitmapReadyListener() {
					
					@Override
					public void onBitmapReady(Bitmap bmp) {
						if (builder != null && builder.getId() == position) {
							builder.setIcon(bmp);
						}
					}
				});
			}
			
			if (getCustomColor() != 0) {
				builder.setCustomColor(getCustomColor());
			}
			if (position == getCount() - 1) {
				showRefreshProgress();
				getNextResults(false);
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
				v.findViewById(R.id.threeDot).setTag(position);
				v.findViewById(R.id.threeDot).setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View paramView) {
						showMenu(paramView);
					}
				});
			}
			if (getAdapterBackground() > 0) {
				if (position % 2 == 0) {
					v.setBackgroundDrawable(getContext().getResources().getDrawable(getAdapterBackground()));
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
			return v;
		}

		public View getProgress() {
			return footer;
		}

		@Override
		protected ru.johnlife.lifetoolsmp3.adapter.BaseAbstractAdapter.ViewHolder<Song> createViewHolder(View v) {
			return null;
		}
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
			resultAdapter.clear();
		} else if ((null == searchString) || (searchString.isEmpty())) {
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
			Class<? extends BaseSearchTask> engineClass = getSearchEngineClass("SearchNothing");
			nothingSearch.add(new Engine(engineClass, 1));
			taskIterator = nothingSearch.iterator();
		} else {
			taskIterator = engines.iterator();
		}
		resultAdapter.clear();
		setMessage("");
		showBaseProgress();
		getNextResults(true);
	}

	private void getNextResults(boolean cancel) {
		showRefreshProgress();
		if (!taskIterator.hasNext()) {
			hideRefreshProgress();
			keeper.deactivateOptions(StateKeeper.SEARCH_EXE_OPTION);
			return;
		}
		if (null != searchTask && cancel) {
			searchTask.cancel(false);
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
		downloadSong = (RemoteSong) resultAdapter.getItem(position);
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
		if (null == player) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(isWhiteTheme(getContext()) ? R.layout.download_dialog_white : R.layout.download_dialog, null);
			player = new Player(v, song, isWhiteTheme(getContext()));
			player.setTitle(song.getArtist() + " - " + song.getTitle());
			player.setIsAppPT(isAppPT());
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
		downloadListener = new DownloadClickListener(getContext(), song, 0);
		if (getSettings().getIsCoversEnabled(getContext())) {
			boolean hasCover = ((RemoteSong) song).getCover(downloadListener);
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
				downloadListener.setSong(keeper.getDownloadSong());
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
	
	protected void animateListView(boolean isRestored) {
		// Animate list view in childs, if need
	}
	
	public void restoreAdapter(ArrayList<Song> list, int position) {
		resultAdapter = new SongSearchAdapter(getContext());
		resultAdapter.add(list);
		listView.addHeaderView(emptyHeader);
		listView.addFooterView(resultAdapter.getProgress(), null, false);
		listView.setAdapter(resultAdapter);
		animateListView(true);
		listView.setSelection(position);
		isRestored = true;
	}
	
	public void unregisterObserver() {
		sPref.unregisterOnSharedPreferenceChangeListener(sPrefListener);
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
	
	public Class<? extends BaseSearchTask> getSearchEngineClass(String searchEngineName) {
		if (searchEngineName != null) {
			if (searchEngineName.equals("SearchVmusice")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchVmusice.class;
			} else if (searchEngineName.equals("SearchZvukoff")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchZvukoff.class;
			} else if (searchEngineName.equals("SearchPleer")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchPleer.class;
			} else if (searchEngineName.equals("SearchPleerV2")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchPleerV2.class;
			} else if (searchEngineName.equals("SearchZvukoff")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchZvukoff.class;
			} else if (searchEngineName.equals("SearchSoArdIyyin")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchSoArdIyyin.class;
			} else if (searchEngineName.equals("SearchMyFreeMp3")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchMyFreeMp3.class;
			} else if (searchEngineName.equals("SearchPleer")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchPleer.class;
			} else if (searchEngineName.equals("SearchPoisk")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchPoisk.class;
			} else if (searchEngineName.equals("SearchHulkShare")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchHulkShare.class;
			} else if (searchEngineName.equals("SearchMp3skull")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchMp3skull.class;
			} else if (searchEngineName.equals("SearchGrooveshark")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchGrooveshark.class;
			} else if (searchEngineName.equals("SearchTing")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchTing.class;
			} else if (searchEngineName.equals("SearchJamendo")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchJamendo.class;
			} else if (searchEngineName.equals("SearchYouTube")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchYouTube.class;
			} else if (searchEngineName.equals("SearchVK")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchVK.class;
			} else if (searchEngineName.equals("SearchTaringaMp3")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchTaringaMp3.class;
			} else if (searchEngineName.equals("SearchKugou")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchKugou.class;
			} else if (searchEngineName.equals("SearchGoearV2")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchGoearV2.class;
			} else if (searchEngineName.equals("SearchSogou")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchSogou.class;
			} else if (searchEngineName.equals("SearchYouTubeMusic")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchYouTubeMusic.class; 
			} else if (searchEngineName.equals("SearchSoundCloud")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchSoundCloud.class;
			} else if (searchEngineName.equals("SearchMp3World")) {
				return ru.johnlife.lifetoolsmp3.engines.SearchMp3World.class;
			} 
		}
		return ru.johnlife.lifetoolsmp3.engines.SearchPleer.class;
	}
}
