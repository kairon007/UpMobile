package org.kreed.musicdownloader.ui.activity;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.kreed.musicdownloader.Advertisement;
import org.kreed.musicdownloader.CompatHoneycomb;
import org.kreed.musicdownloader.Constants;
import org.kreed.musicdownloader.CustomEqualizer;
import org.kreed.musicdownloader.PrefKeys;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.Settings;
import org.kreed.musicdownloader.app.MusicDownloaderApp;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.Player;
import org.kreed.musicdownloader.ui.adapter.ViewPagerAdapter;
import org.kreed.musicdownloader.ui.tab.DownloadsTab;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.RenameTask;
import ru.johnlife.lifetoolsmp3.RenameTaskSuccessListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends Activity {

	private static final String RENAME_PROGRESS_VISIBLE = "RENAME_PROGRESS_VISIBLE";
	private static final String SAVE_PLAYER_VIEW = "SAVE_PLAYER_VIEW";
	private static final String SAVE_BUTTONPLAY_PROGRESS = "SAVE_BUTTONPLAY_PROGRESS";
	private static final String SAVE_SEEKBAR_PROGRESS = "SAVE_SEEKBAR_PROGRESS";
	private final static int DELETE = 1;
	private final static int EDIT_TAG = 2;

	public ViewPagerAdapter mPagerAdapter;
	private ApplicationInfo mFakeInfo;
	protected Looper mLooper;
	private MusicData music;
	private TelephonyManager telephonyManager;
	private HeadphonesReceiver headphonesReceiver;

	private ViewGroup mLimiterViews;
	private Player player;
	private HorizontalScrollView mLimiterScroller;
	private FrameLayout footer;
	public ViewPager mViewPager;
	private TabPageIndicator mTabs;
	private EditText mTextFilter;
	private CustomTextWatcher textWatcher;
	private LinearLayout searchLayout;
	private ImageButton clearAll;
	private ImageButton mClearFilterEditText;

	private String textFilterDownload = "";
	private String textFilterLibrary = "";
	private int page;
	private SelectedData selectedItem;
	private int lastPage = -1;
	private boolean mSearchBoxVisible;
	public boolean mFakeTarget;
	private MP3Editor editor;
	private ArrayList<String> mStrings;
	private boolean showDialog = false;
	private boolean useCover = false;
	private boolean isPlayerHide;
	private StateKeeper keeper;
	private RenameTask renameTask;
	

	// -------------------------------------------------------------------------

	public static void validateAdUnitId(String adUnitId) throws IllegalArgumentException {
		if (adUnitId == null) {
			throw new IllegalArgumentException("Invalid Ad Unit ID: null ad unit.");
		} else if (adUnitId.length() == 0) {
			throw new IllegalArgumentException("Invalid Ad Unit Id: empty ad unit.");
		} else if (adUnitId.length() > 256) {
			throw new IllegalArgumentException("Invalid Ad Unit Id: length too long.");
		} else if (!isAlphaNumeric(adUnitId)) {
			throw new IllegalArgumentException("Invalid Ad Unit Id: contains non-alphanumeric characters.");
		}
	}

	public static boolean isAlphaNumeric(String input) {
		return input.matches("^[a-zA-Z0-9-_]*$");
	}
	
	FileObserver observer; {
		setFileObserver();
	}
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		private boolean flag = false;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				if (MusicDownloaderApp.getService().containsPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()) {
					MediaPlayer mediaPlayer = MusicDownloaderApp.getService().getPlayer().getMediaPlayer();
					if (mediaPlayer.isPlaying()) {
						MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.PAUSE);
						flag = true;
					}
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (null != MusicDownloaderApp.getService() && MusicDownloaderApp.getService().containsPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()) {
					if (flag) {
						MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.CONTINUE_PLAY);
						flag = false;
					}
				}
				break;
			default:
				break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}

	};
	
	private class HeadphonesReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String strAction = intent.getAction();
			if(player == null) return;
			boolean isPlaying = player.getPlayerState() == Constants.PLAY;
			boolean isContinuePlaying = player.getPlayerState() == Constants.CONTINUE_PLAY;
			if(strAction.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
				if (player != null && isPlaying || isContinuePlaying) {
					pausePlayer();
				}
			}
		}
	}
	
	public void pausePlayer() {
		if (null != player) {
			player.stateManagementPlayer(Constants.PAUSE);
		}
	}
	
	public static void logToast(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void hideSoftKeyboard(final EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	@Override
	public void onDestroy() {
		if (telephonyManager != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		unregisterReceiver(headphonesReceiver);
		if (null != renameTask) {
			renameTask.cancelProgress();
		}
		Advertisement.onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		textFilterLibrary = mTextFilter.getText().toString();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (page == 1 && null != textFilterDownload && !textFilterDownload.equals("")) {
			mTextFilter.setText(textFilterDownload);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		headphonesReceiver = new HeadphonesReceiver();
		IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(headphonesReceiver, filter);
		HandlerThread thread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_LOWEST);
		thread.start();
		mLooper = thread.getLooper();
		setContentView(R.layout.library_content);
		init();
		keeper = StateKeeper.getInstance();
		textWatcher = new CustomTextWatcher();
		if (Util.getThemeName(this).equals(Util.WHITE_THEME)) {
			findViewById(R.id.search_box).setBackgroundResource(R.drawable.search_background_white);
			clearAll.setImageResource(R.drawable.icon_cancel_black);
		}
		mTextFilter.addTextChangedListener(textWatcher);
		mClearFilterEditText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mTextFilter.setText("");
			}
		});
		ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);
		mPagerAdapter = pagerAdapter;
		mViewPager.setAdapter(pagerAdapter);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mViewPager.setOnPageChangeListener(pagerAdapter);
		} else {
			TabPageIndicator tabs = new TabPageIndicator(this);
			tabs.setViewPager(mViewPager);
			tabs.setOnPageChangeListener(pagerAdapter);
			mTabs = tabs;
			LinearLayout content = (LinearLayout) findViewById(R.id.content);
			content.addView(tabs, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		loadTabOrder();
		page = settings.getInt(PrefKeys.LIBRARY_PAGE, 0);
		if (page != 0) {
			mViewPager.setCurrentItem(page);
		}
		if (mViewPager.getCurrentItem() == 0) {
			searchLayout.setVisibility(View.GONE);
		}
		if (mViewPager.getCurrentItem() == 1) {
			clearAll.setVisibility(View.VISIBLE);
		}
		if (mViewPager.getCurrentItem() == 2) {
			clearAll.setVisibility(View.GONE);
		}
		if (null != state) {
			isPlayerHide = state.getBoolean(SAVE_PLAYER_VIEW);
			if (state.getBoolean(RENAME_PROGRESS_VISIBLE)) {
				new RenameTask(this).showProgress();				
			}
		}
		if (null != MusicDownloaderApp.getService() && MusicDownloaderApp.getService().containsPlayer()) {
			if (isPlayerHide) {
				MusicDownloaderApp.getService().getPlayer().getView(footer);
			}
			player = MusicDownloaderApp.getService().getPlayer();
			player.setEqualizer(this);
		}
		// show cross promo box
		try {
			LinearLayout downloadsLayout = (LinearLayout) findViewById(R.id.content);
			if (downloadsLayout != null) {
				if (Settings.getIsBlacklisted(this)) {
					Advertisement.hideCrossPromoBox(this, downloadsLayout);
				} else {
					Advertisement.showCrossPromoBox(this, downloadsLayout);
				}
			}
		} catch (Exception e) {
		}
		// show or hide disclaimer
		TextView editTextDisclaimer = (TextView) findViewById(R.id.editTextDisclaimer);
		if (editTextDisclaimer != null) {
			if (Settings.getIsBlacklisted(this)) {
				editTextDisclaimer.setVisibility(View.VISIBLE);
			} else {
				editTextDisclaimer.setVisibility(View.GONE);
			}
		}
		// load banner ad
		try {
			if (Settings.ENABLE_ADS) {
				Advertisement.showBanner(this);
			}
		} catch (Exception e) {

		}
		// initialize ad networks
		try {
			if (!Settings.getIsBlacklisted(this)) {
				// Advertisement.start(this, false);
			} else {
				Advertisement.showDisclaimer(this);
			}
		} catch (Exception e) {
		}
	}

	private void init() {
		mLimiterScroller = (HorizontalScrollView) findViewById(R.id.limiter_scroller);
		mLimiterViews = (ViewGroup) findViewById(R.id.limiter_layout);
		mClearFilterEditText = (ImageButton) findViewById(R.id.clear_filter);
		mTextFilter = (EditText) findViewById(R.id.filter_text);
		clearAll = (ImageButton) findViewById(R.id.clear_all_button);
		footer = (FrameLayout) findViewById(R.id.footer);
		searchLayout = (LinearLayout) findViewById(R.id.search_box);
		mViewPager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		mPagerAdapter.notifyDataSetChanged();
		loadTabOrder();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * Load the tab order and update the tab bars if needed.
	 */
	private void loadTabOrder() {
		if (mPagerAdapter.loadTabOrder()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				CompatHoneycomb.addActionBarTabs(this);
			} else {
				mTabs.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle in) {
		String uri = in.getString(Constants.MUSIC_URI);
		in.remove(Constants.MUSIC_URI);
		mStrings = in.getStringArrayList(Constants.EDITOR_FIELDS);
		if (null != uri) {
			File musicUri = new File(uri);
			music = new MusicData(musicUri);
			useCover = in.getBoolean(Constants.USE_COVER, false);
			File file = new File(in.getString(Constants.FILE_PATH_BUNDLE));
			MusicData data = new MusicData(file);
			int i = in.getInt(Constants.ITEM_BUNDLE);
			showEditDialog(true, new SelectedData(i, data));
		}
		textFilterDownload = in.getString(Constants.FILTER_TEXT_DOWNLOAD);
		textFilterLibrary = in.getString(Constants.FILTER_TEXT_LIBRARY);
		if (null != player) {
			player.setSongProgressIndeterminate(in.getBoolean(SAVE_SEEKBAR_PROGRESS, false));
			player.setButtonProgressVisibility(in.getInt(SAVE_BUTTONPLAY_PROGRESS, View.VISIBLE));
		}
		super.onRestoreInstanceState(in);
	}

	@Override
	protected void onSaveInstanceState(Bundle out) {
		if (showDialog && null != music) {
			String mUri = music.getFileUri();
			out.putString(Constants.MUSIC_URI, mUri);
			ArrayList<String> strings = new ArrayList<String>();
			strings.add(editor.getStrings()[0]);
			strings.add(editor.getStrings()[1]);
			strings.add(editor.getStrings()[2]);
			out.putStringArrayList(Constants.EDITOR_FIELDS, strings);
			out.putBoolean(Constants.USE_COVER, editor.useAlbumCover());
			out.putString(Constants.FILE_PATH_BUNDLE, selectedItem.data.getFileUri());
		}
		out.putBoolean(Constants.SEARCH_BOX_VISIBLE, mSearchBoxVisible);
		if (page == 1) {
			out.putString(Constants.FILTER_TEXT_DOWNLOAD, textFilterDownload);
		} else if (page == 2) {
			out.putString(Constants.FILTER_TEXT_LIBRARY, textFilterLibrary);
		}
		if (null != player && player.getCustomAudioSessionId() == -1) {
			out.putBoolean(SAVE_SEEKBAR_PROGRESS, player.isSongProgressIndeterminate());
			out.putInt(SAVE_BUTTONPLAY_PROGRESS, player.getButtonProgressVisibility());
			out.putBoolean(SAVE_PLAYER_VIEW, player.getPlayerVisibility() == View.VISIBLE);
		}
		if (lastPage == 0) {
			StateKeeper.getInstance().saveStateAdapter(mPagerAdapter.getSearchView());
		}
		if (null != renameTask && renameTask.isShow()) {
			out.putBoolean(RENAME_PROGRESS_VISIBLE, true);
		}
		super.onSaveInstanceState(out);
	}

	/**
	 * Create or recreate the limiter breadcrumbs.
	 */
	public void updateLimiterViews() {
		mLimiterViews.removeAllViews();
		mLimiterScroller.setVisibility(View.VISIBLE);
	}

	public void onPageChanged(int position) {
		// mCurrentAdapter = adapter;
		updateLimiterViews();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			CompatHoneycomb.selectTab(this, position);
		}
		if (lastPage != position && lastPage != -1) {
			// mTextFilter.removeTextChangedListener(textWatcher);
			mTextFilter.setText("");
			// mTextFilter.addTextChangedListener(textWatcher);
		}
		if (lastPage == 0) {
			StateKeeper.getInstance().saveStateAdapter(((ViewPagerAdapter) mViewPager.getAdapter()).getSearchView());
		}
		lastPage = position;
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		ApplicationInfo info;
		if (mFakeTarget) {
			info = mFakeInfo;
			if (info == null) {
				info = new ApplicationInfo(super.getApplicationInfo());
				info.targetSdkVersion = Build.VERSION_CODES.GINGERBREAD;
				mFakeInfo = info;
			}
		} else {
			info = super.getApplicationInfo();
		}
		return info;
	}

	@Override
	public void onBackPressed() {
		Intent showOptions = new Intent(Intent.ACTION_MAIN);
		showOptions.addCategory(Intent.CATEGORY_HOME);
		startActivity(showOptions);
	}

	public String getTextFilterLibrary() {
		if (null == textFilterLibrary || textFilterLibrary.equals("")) {
			return "";
		}
		return textFilterLibrary;
	}

	public void setActivatedPlayButton(boolean value) {
		if (null != player) {
			player.setActivatedButton(value);
		}
	}

	public boolean hasConnection() {
		ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiInfo != null && wifiInfo.isConnected()) {
			return true;
		}
		wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (wifiInfo != null && wifiInfo.isConnected()) {
			return true;
		}
		wifiInfo = cm.getActiveNetworkInfo();
		if (wifiInfo != null && wifiInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public void play(ArrayList<String[]> headers, MusicData musicData) {
		music = musicData;
		if (player != null && player.getCustomAudioSessionId() == -1 && player.getData().equals(musicData)) {
			player.stateManagementPlayer(Constants.RESTART);
			return;
		}
		if (player == null) {
			player = new Player(headers, musicData);
			MusicDownloaderApp.getService().setPlayer(player);
		} else {
			player.setData(headers, musicData);
			MusicDownloaderApp.getService().getPlayer().setData(headers, musicData);
		}
		player.getView(footer);
		player.stateManagementPlayer(Constants.PLAY);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (MusicDownloaderApp.getService().containsPlayer()) {
			Intent intent = new Intent(this, CustomEqualizer.class);
			startActivity(intent);
		} else {
			player = new Player();
			player.setCustomAudioSessionId((int) System.currentTimeMillis());
			MusicDownloaderApp.getService().setPlayer(player);
			Intent intent = new Intent(this, CustomEqualizer.class);
			startActivity(intent);
		}
			return super.onOptionsItemSelected(item);
	}

	public LinearLayout getSearchLayout() {
		return searchLayout;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE:
			new DeleteTask().execute();
			break;
		case EDIT_TAG:
			showEditDialog(false, selectedItem);
			if (null != selectedItem.data.getSongBitmap()) {
				useCover = true;
			}
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(music.getSongArtist() + " - " + music.getSongTitle());
		menu.add(0, DELETE, 0, getResources().getString(R.string.delete_song));
		menu.add(0, EDIT_TAG, 0, getResources().getString(R.string.edit_mp3));
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public void setMusic(MusicData music) {
		this.music = music;
	}

	public SelectedData getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(int selectedItem, MusicData data) {
		this.selectedItem = new SelectedData(selectedItem, data);
	}

	private class DeleteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(), "File " + music.getSongArtist() + " - " + music.getSongTitle() + " does not exist", Toast.LENGTH_LONG).show();
			mPagerAdapter.removeMusicData(music);
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			File file = new File(music.getFileUri());
			if (!file.exists()) {
				File file2 = new File(BaseConstants.DOWNLOAD_DIR + music.getSongTitle() + " - " + music.getSongArtist() + ".mp3");
				file.renameTo(file2);
			}
			if (!file.exists()) {
				cancel(true);
			} else {
				file.delete();
				notifyMediascanner(file);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(), music.getSongArtist() + " - " + music.getSongTitle() + " has been removed", Toast.LENGTH_LONG).show();
			mPagerAdapter.removeMusicData(music);
			if (MusicDownloaderApp.getService().containsPlayer() && MusicDownloaderApp.getService().getPlayer().getData().getFileUri().equals(music.getFileUri())) {
				MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.STOP);
				MusicDownloaderApp.getService().getPlayer().hidePlayerView();
				player = null;
			}
			super.onPostExecute(result);
		}

	}

	@SuppressLint("NewApi")
	public void showEditDialog(boolean forse, final SelectedData selectedData) {
		final File file = new File(selectedData.data.getFileUri());
		final Context context = this;
		boolean isWhiteTheme = Util.getThemeName(this).equals(Util.WHITE_THEME);
		editor = new MP3Editor(this, isWhiteTheme);
		showDialog = true;
		if (null != mStrings) {
			String[] filds = mStrings.toArray(new String[mStrings.size()]);
			editor.setStrings(filds);
			mStrings = null;
		} else {
			String[] filds = { music.getSongArtist(), music.getSongTitle(), "" };
			editor.setStrings(filds);
		}
		editor.setSearchView(false);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(editor.getView());
		if (forse) {
			editor.setUseCover(useCover);
		}
		if (null == music.getSongBitmap()) {
			editor.disableChekBox();
		}
		final boolean defCover = editor.useAlbumCover();
		observer.stopWatching();
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				keeper.closeDialog(StateKeeper.EDITTAG_DIALOG);
				final String artistName = editor.getNewArtistName();
				final String albumTitle = editor.getNewAlbumTitle();
				final String songTitle = editor.getNewSongTitle();
				if (!editor.manipulateText()) {
					return;
				}
				if (new File(file.getParentFile() + "/" + artistName + " - " + songTitle + ".mp3").exists() && defCover == editor.useAlbumCover()) {
					Toast toast = Toast.makeText(editor.getView().getContext(), R.string.file_with_the_same_name_already_exists, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				renameTask = new RenameTask(file, context, artistName, songTitle, albumTitle, editor.useAlbumCover(), new RenameTaskSuccessListener() {
					
					@Override
					public void success() {
						MusicData newData = new MusicData(new File(MessageFormat.format("{0}/{1} - {2}.mp3", file.getParentFile(), artistName, songTitle)));
						mPagerAdapter.updateMusicData(selectedData.data, newData);
						observer.startWatching();
						if (MusicDownloaderApp.getService().containsPlayer() && MusicDownloaderApp.getService().getPlayer().getData().getFileUri().equals(newData.getFileUri())) {
							MusicDownloaderApp.getService().getPlayer().setNewName(artistName, songTitle);
						}
					}
				});
				renameTask.execute();
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			builder.setOnDismissListener(new OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					dialog.dismiss();
					showDialog = false;
				}
			});
		}
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				showDialog = false;
				observer.startWatching();
			}

		});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		alertDialog.show();
	}

	private void notifyMediascanner(final File file) {
		MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
			}
		});
	}

	public class CustomTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			textFilterDownload = mTextFilter.getText().toString().toLowerCase(Locale.ENGLISH);
			DownloadsTab.getInstance().setFilter(textFilterDownload);
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}
	
	public void setFileObserver() {
		File musicDir = new File(Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX);
		if (!musicDir.exists()) {
			musicDir.mkdirs();
		}
		if (observer != null)
			observer.stopWatching();
		observer = new FileObserver(Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX) {
			@Override
			public void onEvent(int event, String file) {
				String filePath = Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX + file;
				if (mPagerAdapter != null) {
					switch (event) {
					case FileObserver.DELETE:
					case FileObserver.MOVED_FROM:
						mPagerAdapter.removeDataByPath(file);
						break;
					case FileObserver.DELETE_SELF:
						mPagerAdapter.cleanLibrary();
						/**
						 * if user delete folder while program is working, all
						 * files will be deleted, but folder will be recreated
						 */
						setFileObserver();
						break;
					case FileObserver.MOVED_TO:
						if (filePath.endsWith(".mp3") || filePath.endsWith(".MP3"))
							mPagerAdapter.addMusicData(new MusicData(new File(filePath)));
						break;
					}
				}
			}
		};
		observer.startWatching();
	}
	
	private class SelectedData {
		
		MusicData data;
		
		public SelectedData(int position, MusicData data) {
			this.data = data;
		}
	}
}