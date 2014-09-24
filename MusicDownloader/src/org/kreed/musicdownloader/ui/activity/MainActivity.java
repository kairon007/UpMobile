package org.kreed.musicdownloader.ui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import org.kreed.musicdownloader.Advertisement;
import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.PrefKeys;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.app.MusicDownloaderApp;
import org.kreed.musicdownloader.ballast.CompatHoneycomb;
import org.kreed.musicdownloader.ballast.LibraryAdapter;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.engines.Settings;
import org.kreed.musicdownloader.ui.Player;
import org.kreed.musicdownloader.ui.adapter.LibraryPagerAdapter;
import org.kreed.musicdownloader.ui.tab.DownloadsTab;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

public class MainActivity extends Activity {

	private final static int DELETE = 1;
	private final static int EDIT_TAG = 2;

	public LibraryPagerAdapter mPagerAdapter;
	private ApplicationInfo mFakeInfo;
	protected Looper mLooper;
	private MusicData music;
	private TelephonyManager telephonyManager;

	private ViewGroup mLimiterViews;
	private Player player;
	private HorizontalScrollView mLimiterScroller;
	private FrameLayout footer;
	public ViewPager mViewPager;
	private TabPageIndicator mTabs;
	private View mSearchBox;
	private EditText mTextFilter;
	private CustomTextWatcher textWatcher;
	private LinearLayout searchLayout;
	private ImageButton clearAll;
	private ImageButton mClearFilterEditText;

	private String textFilterDownload = "";
	private String textFilterLibrary = "";
	private long mLastActedId;
	private int page;
	private int mDefaultAction;
	private int selectedItem;
	private int lastPage = -1;
	private boolean mSearchBoxVisible;
	public boolean mFakeTarget;
	private FillAdapterReceiver receiver;

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

	PhoneStateListener phoneStateListener = new PhoneStateListener() {

		private boolean flag = false;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				if (MusicDownloaderApp.getService().conteinsPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()) {
					MediaPlayer mediaPlayer = MusicDownloaderApp.getService().getPlayer().getMediaPlayer();
					if (mediaPlayer.isPlaying()) {
						MusicDownloaderApp.getService().getPlayer().playPause();
						flag = true;
					}
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if (null != MusicDownloaderApp.getService() && MusicDownloaderApp.getService().conteinsPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()) {
					MediaPlayer mediaPlayer = MusicDownloaderApp.getService().getPlayer().getMediaPlayer();
					if (flag) {
						MusicDownloaderApp.getService().getPlayer().playPause();
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
		unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		textFilterLibrary = mTextFilter.getText().toString();
		SongArrayHolder.getInstance().saveStateAdapter(mPagerAdapter.getSearchView());
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
		IntentFilter filter = new IntentFilter(BaseConstants.FILL_NEW_ADAPTER);
		receiver = new FillAdapterReceiver(); 
		registerReceiver(receiver, filter);
		if (android.os.Build.VERSION.SDK_INT < 11) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		File file = new File(BaseConstants.DOWNLOAD_DIR);
		if (!file.exists()) {
			file.mkdirs();
		}
		telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
		HandlerThread thread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_LOWEST);
		thread.start();
		mLooper = thread.getLooper();
		mSearchBox = findViewById(R.id.search_box);
		setContentView(Settings.SHOW_BANNER_ON_TOP ? R.layout.library_content_top : R.layout.library_content);
		mTextFilter = (EditText) findViewById(R.id.filter_text);
		textWatcher = new CustomTextWatcher();
		mTextFilter.addTextChangedListener(textWatcher);
		mClearFilterEditText = (ImageButton) findViewById(R.id.clear_filter);
		mClearFilterEditText.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mTextFilter.setText("");
			}

		});
		clearAll = (ImageButton) findViewById(R.id.clear_all_button);
		clearAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DownloadsTab.getInstance().recreateAdaper();
			}

		});
		footer = (FrameLayout) findViewById(R.id.footer);

		mLimiterScroller = (HorizontalScrollView) findViewById(R.id.limiter_scroller);
		mLimiterViews = (ViewGroup) findViewById(R.id.limiter_layout);

		LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this, mLooper);
		mPagerAdapter = pagerAdapter;

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);
		mViewPager = pager;
		searchLayout = (LinearLayout) findViewById(R.id.search_box);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			pager.setOnPageChangeListener(pagerAdapter);
		} else {
			TabPageIndicator tabs = new TabPageIndicator(this);
			tabs.setViewPager(pager);
			tabs.setOnPageChangeListener(pagerAdapter);
			mTabs = tabs;
			LinearLayout content = (LinearLayout) findViewById(R.id.content);
			content.addView(tabs, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		}
		loadTabOrder();
		page = settings.getInt(PrefKeys.LIBRARY_PAGE, 0);
		if (page != 0) {
			pager.setCurrentItem(page);
		}
		if (pager.getCurrentItem() == 0) {
			searchLayout.setVisibility(View.GONE);
		}
		if (pager.getCurrentItem() == 1) {
			clearAll.setVisibility(View.VISIBLE);
		}
		if (pager.getCurrentItem() == 2) {
			clearAll.setVisibility(View.GONE);
		}
		if (null != MusicDownloaderApp.getService() && MusicDownloaderApp.getService().getPlayer() != null) {
			MusicDownloaderApp.getService().getPlayer().getView(footer);
			if (MusicDownloaderApp.getService().conteinsPlayer()) {
				player = MusicDownloaderApp.getService().getPlayer();
			}
		}
		if (Settings.ENABLE_ADS) {
			Advertisement.mopubShowBanner(this);
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		mPagerAdapter.notifyDataSetChanged();
		loadTabOrder();
	}

	public void onMediaChange() {
		mPagerAdapter.invalidateData();
	}

	@Override
	public void onStart() {
		super.onStart();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		mDefaultAction = Integer.parseInt(settings.getString(PrefKeys.DEFAULT_ACTION_INT, "7"));
		mLastActedId = LibraryAdapter.INVALID_ID;
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
		textFilterDownload = in.getString(Constans.FILTER_TEXT_DOWNLOAD);
		textFilterLibrary = in.getString(Constans.FILTER_TEXT_LIBRARY);
		if (in.getBoolean(Constans.SEARCH_BOX_VISIBLE))
			super.onRestoreInstanceState(in);
	}

	@Override
	protected void onSaveInstanceState(Bundle out) {
		out.putBoolean(Constans.SEARCH_BOX_VISIBLE, mSearchBoxVisible);
		if (page == 1) {
			out.putString(Constans.FILTER_TEXT_DOWNLOAD, textFilterDownload);
		} else if (page == 2) {
			out.putString(Constans.FILTER_TEXT_LIBRARY, textFilterLibrary);
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

	/**
	 * Called when a new page becomes visible.
	 * 
	 * @param position
	 *            The position of the new page.
	 * @param adapter
	 *            The new visible adapter.
	 */
	public void onPageChanged(int position, LibraryAdapter adapter) {
		// mCurrentAdapter = adapter;
		mLastActedId = LibraryAdapter.INVALID_ID;
		updateLimiterViews();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			CompatHoneycomb.selectTab(this, position);
		}
		if (lastPage != position && lastPage != -1) {
			mTextFilter.removeTextChangedListener(textWatcher);
			mTextFilter.setText("");
			mTextFilter.addTextChangedListener(textWatcher);
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

	public void setFooterView(String strTitle) {
		TextView tv = (TextView) findViewById(R.id.player_artist);
		tv.setText(strTitle);
		SeekBar sb = (SeekBar) findViewById(R.id.player_progress_song);
		sb.setProgress(0);
		ImageButton ib = (ImageButton) findViewById(R.id.player_play_song);
		ib.setVisibility(View.GONE);
		ProgressBar pb = (ProgressBar) findViewById(R.id.player_progress_play);
		pb.setVisibility(View.VISIBLE);
	}

	public void resetPlayer() {
		if (player != null) {
			player.stopTask();
			player.remove();
			player = null;
		}
	}
	
	public void play(ArrayList<String[]> headers, MusicData musicData, String from) {
		music = musicData;
		if (player != null && player.getData().equals(musicData)) {
			player.restart();
			return;
		} else if (player != null && !player.getData().equals(musicData)) {
			player.remove();
			player = null;
		}
		if (!Constans.CALL_FROM_SEARCH.equals(from)) {
			Log.d("logd", "stopPlayerTask");
//			SongSearchView.stopPlayerTask();
			resetPlayer();
		}
		player = new Player(headers, musicData);
		MusicDownloaderApp.getService().setPlayer(player);
		player.getView(footer);
		player.play();
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
			showEditDialog(false);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(music.getSongArtist() + " - " + music.getSongTitle());
		menu.add(0, DELETE, 0, getResources().getString(R.string.delete_song));
		menu.add(0, EDIT_TAG, 0, getResources().getString(R.string.edit_mp3_tags));
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public MusicData getMusic() {
		return music;
	}

	public void setMusic(MusicData music) {
		this.music = music;
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(int selectedItem) {
		this.selectedItem = selectedItem;
	}

	private class DeleteTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(), "File " + music.getSongArtist() + " - " + music.getSongTitle() + " do not exists", Toast.LENGTH_LONG).show();
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
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(getApplicationContext(), music.getSongArtist() + " - " + music.getSongTitle() + " has been removed", Toast.LENGTH_LONG).show();
			mPagerAdapter.removeMusicData(music);
			super.onPostExecute(result);
		}

	}

	public void showEditDialog(boolean forse) {
		final File file = new File(music.getFileUri());
		final MP3Editor editor = new MP3Editor(this, forse);
		MusicData data = MusicData.getFromFile(file);
		String[] filds = {data.getSongArtist(), data.getSongTitle(), ""};
		if(null == data.getSongBitmap()) {
			editor.setShowCover(false);
		}
		editor.setStrings(filds);
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(editor.getView());
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						MusicData musicData = new MusicData(file);
						String artistName = editor.getNewArtistName();
						String albumTitle = editor.getNewAlbumTitle();
						String songTitle = editor.getNewSongTitle();
						boolean showCover = editor.useAlbumCover();
						musicData.setUseCover(showCover);
						if (!editor.manipulateText() && showCover) {
							cancel(true);
							return null;
						}
						MusicData data = new MusicData(artistName, songTitle, null, null);
						data.setSongAlbum(albumTitle);
						musicData.rename(data);
						notifyMediascanner(musicData);
						return null;
					}

				}.execute();
			}

		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}

		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	private void notifyMediascanner(final MusicData musicData) {
		File file = new File(musicData.getFileUri());
		MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
				int i = getSelectedItem();
				mPagerAdapter.updateMusicData(i, musicData);
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
	private class FillAdapterReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			mPagerAdapter.recreate ();
		}
	}
}