package org.kreed.musicdownloader.ui.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

import org.kreed.musicdownloader.CompatHoneycomb;
import org.kreed.musicdownloader.Constants;
import org.kreed.musicdownloader.CustomEqualizer;
import org.kreed.musicdownloader.Nulldroid_Advertisement;
import org.kreed.musicdownloader.Nulldroid_Settings;
import org.kreed.musicdownloader.PrefKeys;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.app.MusicDownloaderApp;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.services.PlayerService;
import org.kreed.musicdownloader.ui.Player;
import org.kreed.musicdownloader.ui.tabs.DownloadsTab;
import org.kreed.musicdownloader.ui.viewpager.ViewPagerAdapter;

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
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends Activity {

	private static final String SAVE_PLAYER_STATE = "save_player_state";
	private final static int DELETE = 1;
	private final static int EDIT_TAG = 2;
	private ArrayList<String> uriDownloadedFilesBefore;
	private ArrayList<String> uriDownloadedFilesAfter;
	
	private MusicData deletedItem;
	private Player player;
	private MP3Editor editor;
	private StateKeeper keeper;
	private CustomTextWatcher textWatcher;
	
	private ApplicationInfo mFakeInfo;
	private TelephonyManager telephonyManager;
	private HeadphonesReceiver headphonesReceiver;
	
	private FrameLayout footer;
	private LinearLayout searchLayout;
	private ViewPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	private TabPageIndicator mTabs;
	private EditText mTextFilter;
	private ImageButton clearAll;
	private ImageButton clearTextFilter;

	private String textFilterDownload = "";
	private String textFilterLibrary = "";
	private int page;
	private int lastPage = -1;
	private boolean mFakeTarget;
	private boolean isHidePlayer = true;
	private PlayerService service;
	
	
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
	
	@Override
	public void onDestroy() {
		if (telephonyManager != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
		unregisterReceiver(headphonesReceiver);
		Nulldroid_Advertisement.onDestroy(this);
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
	public void onRestart() {
		super.onRestart();
		pagerAdapter.notifyDataSetChanged();
		loadTabOrder();
	}

	@Override
	public void onStart() {
		super.onStart();
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
		setContentView(R.layout.library_content);
		init();
		keeper = StateKeeper.getInstance();
		textWatcher = new CustomTextWatcher();
		if (Util.getThemeName(this).equals(Util.WHITE_THEME)) {
			findViewById(R.id.search_box).setBackgroundResource(R.drawable.search_background_white);
			clearAll.setImageResource(R.drawable.icon_cancel_black);
		}
		mTextFilter.addTextChangedListener(textWatcher);
		clearTextFilter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mTextFilter.setText("");
			}
		});
		ViewPagerAdapter pa = new ViewPagerAdapter(this);
		pagerAdapter = pa;
		viewPager.setAdapter(pa);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			viewPager.setOnPageChangeListener(pa);
		} else {
			TabPageIndicator tabs = new TabPageIndicator(this);
			tabs.setViewPager(viewPager);
			tabs.setOnPageChangeListener(pa);
			mTabs = tabs;
			LinearLayout content = (LinearLayout) findViewById(R.id.content);
			content.addView(tabs, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		loadTabOrder();
		page = settings.getInt(PrefKeys.LIBRARY_PAGE, 0);
		if (page != 0) {
			viewPager.setCurrentItem(page);
		}
		if (viewPager.getCurrentItem() == 0) {
			searchLayout.setVisibility(View.GONE);
		}
		if (viewPager.getCurrentItem() == 1) {
			clearAll.setVisibility(View.VISIBLE);
		}
		if (viewPager.getCurrentItem() == 2) {
			clearAll.setVisibility(View.GONE);
		}
		if (null != state && state.containsKey(SAVE_PLAYER_STATE)) isHidePlayer = state.getBoolean(SAVE_PLAYER_STATE);
		if (!isHidePlayer && null != MusicDownloaderApp.getService() ) {
			player = MusicDownloaderApp.getService().getPlayer();
			player.setEqualizer(this);
			player.getView(footer);
		}
		
		
		initCrossPromoBoxAndDisclaimer();
		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}

	@Override
	public void onRestoreInstanceState(Bundle in) {
		isHidePlayer = in.getBoolean(SAVE_PLAYER_STATE);
		if (keeper.checkState(StateKeeper.EDITTAG_DIALOG)){
			showEditDialog();
		}
		textFilterDownload = in.getString(Constants.FILTER_TEXT_DOWNLOAD);
		textFilterLibrary = in.getString(Constants.FILTER_TEXT_LIBRARY);
		super.onRestoreInstanceState(in);
	}

	@Override
	protected void onSaveInstanceState(Bundle out) {
		out.putBoolean(SAVE_PLAYER_STATE, isHidePlayer);
		if (page == 1) {
			out.putString(Constants.FILTER_TEXT_DOWNLOAD, textFilterDownload);
		} else if (page == 2) {
			out.putString(Constants.FILTER_TEXT_LIBRARY, textFilterLibrary);
		}
		if (lastPage == 0) {
			StateKeeper.getInstance().saveStateAdapter(pagerAdapter.getSearchView());
		}
		super.onSaveInstanceState(out);
	}
	

	/**
	 * Load the tab order and update the tab bars if needed.
	 */
	private void loadTabOrder() {
		if (pagerAdapter.loadTabOrder()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				CompatHoneycomb.addActionBarTabs(this);
			} else {
				mTabs.notifyDataSetChanged();
			}
		}
	}

	public void onPageChanged(int position) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			CompatHoneycomb.selectTab(this, position);
		}
		if (lastPage != position && lastPage != -1) {
			// mTextFilter.removeTextChangedListener(textWatcher);
			mTextFilter.setText("");
			// mTextFilter.addTextChangedListener(textWatcher);
		}
		if (lastPage == 0) {
			StateKeeper.getInstance().saveStateAdapter(((ViewPagerAdapter) viewPager.getAdapter()).getSearchView());
		}
		lastPage = position;
	}
	
	private void initCrossPromoBoxAndDisclaimer() {
		// show cross promo box
		try {
			LinearLayout downloadsLayout = (LinearLayout) findViewById(R.id.content);
			if (downloadsLayout != null) {
				if (Nulldroid_Settings.getIsBlacklisted(this) || Nulldroid_Settings.getIsSuperBlacklisted(this)) {
					Nulldroid_Advertisement.hideCrossPromoBox(this, downloadsLayout);
				} else {
					Nulldroid_Advertisement.showCrossPromoBox(this, downloadsLayout);
				}
			}
		} catch (Exception e) {
		}
		// show or hide disclaimer
		try {
			
			TextView editTextDisclaimer = (TextView) findViewById(R.id.editTextDisclaimer);
			if (editTextDisclaimer != null) {
				if (Nulldroid_Advertisement.isOnline(this) && (Nulldroid_Settings.getIsBlacklisted(this) || Nulldroid_Settings.getIsSuperBlacklisted(this))) {
					editTextDisclaimer.setVisibility(View.VISIBLE);
				} else {
					editTextDisclaimer.setVisibility(View.GONE);
				}
			}

		} catch(Exception e) {
			
		}
		
	}


	private void init() {
		clearTextFilter = (ImageButton) findViewById(R.id.clear_filter);
		mTextFilter = (EditText) findViewById(R.id.filter_text);
		clearAll = (ImageButton) findViewById(R.id.clear_all_button);
		footer = (FrameLayout) findViewById(R.id.footer);
		searchLayout = (LinearLayout) findViewById(R.id.search_box);
		viewPager = (ViewPager) findViewById(R.id.pager);
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
		
		if (!isHidePlayer || keeper.checkState(StateKeeper.PROGRESS_DIALOG)) {
			if (null != MusicDownloaderApp.getService().getPlayer()) {
				MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.STOP);
				MusicDownloaderApp.getService().getPlayer().hidePlayerView();
			}
			isHidePlayer = true;
		} else {
			
			super.onBackPressed();
			/*
			Intent showOptions = new Intent(Intent.ACTION_MAIN);
			showOptions.addCategory(Intent.CATEGORY_HOME);
			startActivity(showOptions);
			*/
		}
	}


	public String getTextFilterLibrary() {
		if (null == textFilterLibrary || textFilterLibrary.equals("")) {
			return "";
		}
		return textFilterLibrary;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//TODO call equalazer
		if (MusicDownloaderApp.getService().containsPlayer()) {
			Intent intent = new Intent(this, CustomEqualizer.class);
			startActivity(intent);
		} else {
			isHidePlayer = true;
			player = new Player();
			player.setCustomAudioSessionId((int) System.currentTimeMillis());
			MusicDownloaderApp.getService().setPlayer(player);
			Intent intent = new Intent(this, CustomEqualizer.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DELETE:
			checkFile();
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

	public boolean hasConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
	
	public void setActivatedPlayButton(boolean value) {
		if (null != player) {
			player.setActivatedButton(value);
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
		isHidePlayer = false;
		player.getView(footer);
		player.stateManagementPlayer(Constants.PLAY);
	}
	
	public void setCoverToPlayer(Bitmap cover) {
		player.setCover(cover);
	}
	

	private void checkFile() {
		File file = new File(deletedItem.getFileUri());
		if (!file.exists()) {
			File file2 = new File(BaseConstants.DOWNLOAD_DIR + deletedItem.getSongTitle() + " - " + deletedItem.getSongArtist() + ".mp3");
			file.renameTo(file2);
		}
		if (!file.exists()) {
			Toast.makeText(getApplicationContext(), "File " + deletedItem.getSongArtist() + " - " + deletedItem.getSongTitle() + " does not exist", Toast.LENGTH_LONG).show();
			pagerAdapter.removeMusicData(deletedItem);
		} else {
			Toast.makeText(getApplicationContext(), deletedItem.getSongArtist() + " - " + deletedItem.getSongTitle() + " has been removed",
					Toast.LENGTH_LONG).show();
			pagerAdapter.removeMusicData(deletedItem);
			if (MusicDownloaderApp.getService().containsPlayer() && MusicDownloaderApp.getService().getPlayer().getData().getFileUri().equals(deletedItem.getFileUri())) {
				MusicDownloaderApp.getService().getPlayer().stateManagementPlayer(Constants.STOP);
				MusicDownloaderApp.getService().getPlayer().hidePlayerView();
				isHidePlayer = true;
			}
			new DeleteTask(file).execute();
		}
	}

	private class DeleteTask extends AsyncTask<Void, Void, Void> {
		
		private File file;
		
		public DeleteTask(File file) {
			this.file = file;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				file.delete();
				ContentResolver resolver = getContentResolver();
				String[] projection = new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE };
				Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
				c.moveToFirst();
				while (c.moveToNext()) {
					if (file.getPath().equals(c.getString(1))) {
						int id = c.getInt(0);
						String where = MediaStore.Audio.Media._ID + "=" + id;
						resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);
						break;
					}
				}
				c.close();
			} catch (Exception e) {
				Log.d(getClass().getSimpleName(), e.getMessage());
			}
			return null;
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
					pagerAdapter.updateMusicData(item, newData);
					observer.startWatching();
					checkDownloads(uriDownloadedFilesAfter, true);
					if (MusicDownloaderApp.getService().containsPlayer() && MusicDownloaderApp.getService().getPlayer().getData().getFileUri().equals(newData.getFileUri())) {
						MusicDownloaderApp.getService().getPlayer().setNewName(artistName, songTitle, false, useCover ? newData.getSongBitmap() : null);
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
				boolean manipulate = keeper.checkState(StateKeeper.MANIPULATE_TEXT_OPTION);
				keeper.closeDialog(StateKeeper.EDITTAG_DIALOG);
				File file = new File(item.getFileUri());
				if (!manipulate && useCover) {
					return;
				} else if (!manipulate && !useCover) {
					observer.stopWatching();
					RenameTask task = new RenameTask(file, MainActivity.this, renameListener, null, null, null);
					task.start(false, true);
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
				new RenameTask(file, MainActivity.this, renameListener, editor.getStrings()).start(useCover, false);
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
					if (str.endsWith(".mp3") || str.endsWith(".MP3") || str.endsWith(".m4a") || str.endsWith(".M4A"))
						try {
							pagerAdapter.addMusicData(new MusicData(new File(URLDecoder.decode(str.replace("file://", ""), "UTF-8"))));
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
				if (pagerAdapter != null) {
					switch (event) {
					case FileObserver.DELETE:
					case FileObserver.MOVED_FROM:
						pagerAdapter.removeDataByPath(file);
						break;
					case FileObserver.MOVE_SELF:
					case FileObserver.DELETE_SELF:
						pagerAdapter.cleanLibrary();
						/**
						 * if user delete folder while program is working, all
						 * files will be deleted, but folder will be recreated
						 */
						setFileObserver();
						break;
					case FileObserver.MOVED_TO:
						if (filePath.endsWith(".mp3") || filePath.endsWith(".MP3") || filePath.endsWith(".m4a") || filePath.endsWith(".M4A"))
							pagerAdapter.addMusicData(new MusicData(new File(filePath)));
						break;
					}
				}
			}
		};
		observer.startWatching();
	}
	
	public void setDeletedItem(MusicData music) {
		deletedItem = music;
	}

	public void setSelectedItem(MusicData data) {
		keeper.setTag(data);
		String[] strArray = new String[] { data.getSongArtist(), data.getSongTitle(), data.getSongAlbum() };
		keeper.setTempID3Fields(strArray);
	}
	
	public ViewPager getViewPager() {
		return viewPager;
	}
	
	public ViewPagerAdapter getPagerAdapter() {
		return pagerAdapter;
	}
	
	public boolean isFakeTarget() {
		return mFakeTarget;
	}
	
	public void setFakeTarget(boolean mFakeTarget) {
		this.mFakeTarget = mFakeTarget;
	}
	
	public LinearLayout getSearchLayout() {
		return searchLayout;
	}
}