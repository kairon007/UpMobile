package org.kreed.musicdownloader.ui.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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
	private MusicData deletedItem;
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
	private ArrayList<String> uriDownloadedFilesBefore;
	private ArrayList<String> uriDownloadedFilesAfter; 
	private int lastPage = -1;
	private boolean mSearchBoxVisible;
	public boolean mFakeTarget;
	private MP3Editor editor;
	private boolean isPlayerHide;
	private StateKeeper keeper;
	

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
	
	public void stopPlayer() {
		if (null != player) {
			player.stateManagementPlayer(Constants.STOP);
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
		if (keeper.checkState(StateKeeper.EDITTAG_DIALOG)){
			showEditDialog();
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
		super.onSaveInstanceState(out);
	}

	public void onPageChanged(int position) {
		keeper = StateKeeper.resetState(); 
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
		if (null != MusicDownloaderApp.getService().getPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()) {
			MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.STOP);
			MusicDownloaderApp.getService().getPlayer().hidePlayerView();
		} else {
			Intent showOptions = new Intent(Intent.ACTION_MAIN);
			showOptions.addCategory(Intent.CATEGORY_HOME);
			startActivity(showOptions);
		}
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
			new DeleteTask(deletedItem).execute();
			break;
		case EDIT_TAG:
			showEditDialog();
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(deletedItem.getSongArtist() + " - " + deletedItem.getSongTitle());
		menu.add(0, DELETE, 0, getResources().getString(R.string.delete_song));
		menu.add(0, EDIT_TAG, 0, getResources().getString(R.string.edit_mp3));
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public void setDeletedItem(MusicData music) {
		deletedItem = music;
	}

	public void setSelectedItem(MusicData data) {
		keeper.setTag(data);
		String[] strArray = new String[] { data.getSongArtist(), data.getSongTitle(), data.getSongAlbum() };
		keeper.setTempID3Fields(strArray);
	}

	private class DeleteTask extends AsyncTask<Void, Void, Void> {
		
		private MusicData item;
		
		public DeleteTask(MusicData item) {
			this.item = item;
		}

		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(), "File " + item.getSongArtist() + " - " + item.getSongTitle() + " does not exist", Toast.LENGTH_LONG).show();
			mPagerAdapter.removeMusicData(item);
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(Void... params) {
			File file = new File(item.getFileUri());
			if (!file.exists()) {
				File file2 = new File(BaseConstants.DOWNLOAD_DIR + item.getSongTitle() + " - " + item.getSongArtist() + ".mp3");
				file.renameTo(file2);
			}
			if (!file.exists()) {
				cancel(true);
			} else {
				file.delete();
				ContentResolver resolver = getContentResolver();
				String[] projection = new String [] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA,  MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE};
				Cursor c = resolver.query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
				c.moveToFirst();
				while (c.moveToNext()) {
					if (file.getPath().equals(c.getString(1))) {
						int id = c.getInt(0);
						String where = MediaStore.Audio.Media._ID + "=" + id;
						String[] whereArgs = new String[] { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE };
						resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);
						break;
					}
				}
				c.close();
				Cursor cr = resolver.query( MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(), item.getSongArtist() + " - " + item.getSongTitle() + " has been removed", Toast.LENGTH_LONG).show();
			mPagerAdapter.removeMusicData(item);
			if (MusicDownloaderApp.getService().containsPlayer() && MusicDownloaderApp.getService().getPlayer().getData().getFileUri().equals(item.getFileUri())) {
				MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.STOP);
				MusicDownloaderApp.getService().getPlayer().hidePlayerView();
				player = null;
			}
			super.onPostExecute(result);
		}

	}

	@SuppressLint("NewApi")
	public void showEditDialog() {
		keeper.openDialog(StateKeeper.EDITTAG_DIALOG);
		final MusicData item = (MusicData) keeper.getTag();
		boolean isWhiteTheme = Util.getThemeName(this).equals(Util.WHITE_THEME);
		editor = new MP3Editor(this, isWhiteTheme);
		editor.setStrings(keeper.getTempID3Fields());
		View dialogView = editor.getView();
		if (keeper.getTempID3UseCover() != 0) {
			editor.setUseCover(keeper.getTempID3UseCover() > 0);
		} else {
			if (item.getSongBitmap() == null) {
				editor.disableChekBox();
			} else {
				editor.setUseCover(true);
			}
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(dialogView);
		uriDownloadedFilesAfter = new ArrayList<String>();
		uriDownloadedFilesBefore = new ArrayList<String>();
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			private boolean useCover;
			RenameTaskSuccessListener renameListener = new RenameTaskSuccessListener() {

				@Override
				public void success(String path) {
					String artistName = editor.getNewArtistName();
					String albumTitle = editor.getNewAlbumTitle();
					String songTitle = editor.getNewSongTitle();
					MusicData newData = new MusicData(artistName, songTitle, albumTitle, path);
					newData.setUseCover(useCover);
					mPagerAdapter.updateMusicData(item, newData);
					observer.startWatching();
					checkDownloads(uriDownloadedFilesAfter, true);
					if (MusicDownloaderApp.getService().containsPlayer() && MusicDownloaderApp.getService().getPlayer().getData().getFileUri().equals(newData.getFileUri())) {
						MusicDownloaderApp.getService().getPlayer().setNewName(artistName, songTitle, false);
					}
				}

				@Override
				public void error() {
					observer.startWatching();
				}
			};

			@Override
			public void onClick(DialogInterface dialog, int which) {
				useCover = keeper.getTempID3UseCover() >= 0;
				File file = new File(item.getFileUri());
				if (!keeper.checkState(StateKeeper.MANIPULATE_TEXT_OPTION) && keeper.getTempID3UseCover() >= 0) {
					return;
				} else if (!keeper.checkState(StateKeeper.MANIPULATE_TEXT_OPTION) && keeper.getTempID3UseCover() < 0) {
					observer.stopWatching();
					RenameTask task = new RenameTask(file, MainActivity.this, renameListener, null, null, null);
					task.execute(false, true);
					return;
				}
				File f = new File(item.getFileUri());
				if (new File(f.getParentFile() + "/" + editor.getNewArtistName() + " - " + editor.getNewSongTitle() + ".mp3").exists()) {
					Toast toast = Toast.makeText(editor.getView().getContext(), R.string.file_with_the_same_name_already_exists, Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				observer.stopWatching();
				checkDownloads(uriDownloadedFilesBefore, false);
				new RenameTask(file, MainActivity.this, renameListener, editor.getStrings()).execute(useCover, false);
				keeper.closeDialog(StateKeeper.EDITTAG_DIALOG);
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			builder.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					keeper.closeDialog(StateKeeper.EDITTAG_DIALOG);
					dialog.dismiss();
				}
			});
		}
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				keeper.deactivateOptions(StateKeeper.EDITTAG_DIALOG);
			}

		});
		AlertDialog alertDialog = builder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		alertDialog.show();
	}

	private void checkDownloads(ArrayList<String> uriDownloadedFiles, boolean check) {
		DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor c = manager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_RUNNING));
		while (c.moveToNext()) {
			uriDownloadedFiles.add(c.getString(14));
		}
		if (check && !uriDownloadedFilesAfter.equals(uriDownloadedFilesBefore)) {
			for (String str : uriDownloadedFilesBefore) {
				if (!uriDownloadedFilesAfter.contains(str)) {
					if (str.endsWith(".mp3") || str.endsWith(".MP3"))
						try {
							mPagerAdapter.addMusicData(new MusicData(new File(URLDecoder.decode(str.replace("file://", ""), "UTF-8"))));
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
				}
			}
		}
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
		final File musicDir = new File(Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX);
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
					case FileObserver.MOVE_SELF:
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
	
}