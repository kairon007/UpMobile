package org.kreed.musicdownloader;
/*
 * Copyright (C) 2012 Christopher Eby <kreed@kreed.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.musicdownloader.app.MusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

/**
 * The library activity where songs to play can be selected from the library.
 */
public class MainActivity extends Activity {
	/**
	 * Action for row click: play the row.
	 */
	public static final int ACTION_PLAY = 0;
	/**
	 * Action for row click: enqueue the row.
	 */
	public static final int ACTION_ENQUEUE = 1;
	/**
	 * Action for row click: perform the last used action.
	 */
	public static final int ACTION_LAST_USED = 2;
	/**
	 * Action for row click: play all the songs in the adapter, starting with
	 * the current row.
	 */
	public static final int ACTION_PLAY_ALL = 3;
	/**
	 * Action for row click: enqueue all the songs in the adapter, starting with
	 * the current row.
	 */
	public static final int ACTION_ENQUEUE_ALL = 4;
	/**
	 * Action for row click: do nothing.
	 */
	public static final int ACTION_DO_NOTHING = 5;
	/**
	 * Action for row click: expand the row.
	 */
	public static final int ACTION_EXPAND = 6;
	/**
	 * Action for row click: play if paused or enqueue if playing.
	 */
	public static final int ACTION_PLAY_OR_ENQUEUE = 7;
	
	public static final String EDIT_ALBUM_TITLE = "edit_album_title";
	public static final String EDIT_SONG_TITLE = "edit_song_title";
	public static final String EDIT_ARTIST_NAME = "edit_artist_name";
	/**
	 * The SongTimeline add song modes corresponding to each relevant action.
	 */
	private static final int[] modeForAction =
		{ SongTimeline.MODE_PLAY, SongTimeline.MODE_ENQUEUE, -1,
		  SongTimeline.MODE_PLAY_ID_FIRST, SongTimeline.MODE_ENQUEUE_ID_FIRST };

	private static final String SEARCH_BOX_VISIBLE = "search_box_visible";
	private static final String PLAY_ROW_NUMBER = "search_play_row";

	public ViewPager mViewPager;
	private TabPageIndicator mTabs;

	private View mSearchBox;
	private boolean mSearchBoxVisible;

	private EditText mTextFilter;
	private CustomTextWatcher textWatcher;
	private LinearLayout searchLayout;
	
	private ImageButton clearAll;


	private ImageButton mClearFilterEditText;

	private HorizontalScrollView mLimiterScroller;
	private ViewGroup mLimiterViews;
	Intent serviceIntent;
	
	private int page;

	/**
	 * The action to execute when a row is tapped.
	 */
	private int mDefaultAction;
	/**
	 * The last used action from the menu. Used with ACTION_LAST_USED.
	 */
	private int mLastAction = ACTION_PLAY;
	/**
	 * The id of the media that was last pressed in the current adapter. Used to
	 * open the playback activity when an item is pressed twice.
	 */
	private long mLastActedId;
	/**
	 * The pager adapter that manages each media ListView.
	 */
	public LibraryPagerAdapter mPagerAdapter;
	/**
	 * The adapter for the currently visible list.
	 */
	private LibraryAdapter mCurrentAdapter;
	/**
	 * If true, return target GINGERBREAD from getApplicationInfo().
	 */
	boolean mFakeTarget;
	/**
	 * ApplicationInfo with targetSdkVersion set to Gingerbread.
	 */
	private ApplicationInfo mFakeInfo;
	private int itemPosition;
	private String songArtist;
	private String songTitle;
	private String songDuration;
	private MusicData music;
	private int selectedItem;
	private final static int DELETE = 1;
	private final static int EDIT_TAG = 2;
	
	
	int lastPage = -1;
	protected Looper mLooper;
	private TelephonyManager telephonyManager;
	private Player player;
	private FrameLayout footer;
	//-------------------------------------------------------------------------
	
	public static void validateAdUnitId(String adUnitId) throws IllegalArgumentException {
		if (adUnitId == null) {
			throw new IllegalArgumentException(
					"Invalid Ad Unit ID: null ad unit.");
		} else if (adUnitId.length() == 0) {
			throw new IllegalArgumentException(
					"Invalid Ad Unit Id: empty ad unit.");
		} else if (adUnitId.length() > 256) {
			throw new IllegalArgumentException(
					"Invalid Ad Unit Id: length too long.");
		} else if (!isAlphaNumeric(adUnitId)) {
			throw new IllegalArgumentException(
					"Invalid Ad Unit Id: contains non-alphanumeric characters.");
		}
	}
	
	public static boolean isAlphaNumeric(String input) {
		return input.matches("^[a-zA-Z0-9-_]*$");
	}
	
	PhoneStateListener phoneStateListener = new PhoneStateListener() {
		
		private boolean flag = false;
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				if(MusicDownloaderApp.getService().conteinsPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()){
					MediaPlayer mediaPlayer = MusicDownloaderApp.getService().getPlayer().getMediaPlayer();
					if (mediaPlayer.isPlaying()) {
						MusicDownloaderApp.getService().getPlayer().playPause();
						flag = true;
					}
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				if(null != MusicDownloaderApp.getService() && MusicDownloaderApp.getService().conteinsPlayer() && null != MusicDownloaderApp.getService().getPlayer().getMediaPlayer()){
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
		Log.d("", message);
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
	
	public static void hideSoftKeyboard(final EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager) editText
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager
				.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	
	@Override
	public void onDestroy() {
		if(telephonyManager != null) {
			telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
	//	PlayerService.removeActivity(this);
		super.onDestroy();
	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	Intent intent1 = getIntent();
        	pickSongs(intent1,ACTION_PLAY);
        }
    };
	
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
	public void onResume() {
		super.onResume();
	}
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);	
		if(android.os.Build.VERSION.SDK_INT < 11) { 
		    requestWindowFeature(Window.FEATURE_NO_TITLE); 
		} 
		//PlayerService.addActivity(this);
		File file = new File(Environment.getExternalStorageDirectory() + PrefKeys.DIRECTORY_PREFIX);
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
		setContentView(Settings.SHOW_BANNER_ON_TOP?R.layout.library_content_top:R.layout.library_content);
		mTextFilter = (EditText)findViewById(R.id.filter_text);
		textWatcher = new CustomTextWatcher();
		mTextFilter.addTextChangedListener(textWatcher);
		mClearFilterEditText = (ImageButton)findViewById(R.id.clear_filter);
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
				DBHelper.getInstance().deleteAll();
			}
			
		});
		footer = (FrameLayout) findViewById(R.id.footer);

		mLimiterScroller = (HorizontalScrollView)findViewById(R.id.limiter_scroller);
		mLimiterViews = (ViewGroup)findViewById(R.id.limiter_layout);

		LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this, mLooper);
		mPagerAdapter = pagerAdapter;

		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);
		mViewPager = pager;
		searchLayout = (LinearLayout)findViewById(R.id.search_box);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			pager.setOnPageChangeListener(pagerAdapter);
		} else {
			TabPageIndicator tabs = new TabPageIndicator(this);
			tabs.setViewPager(pager);
			tabs.setOnPageChangeListener(pagerAdapter);
			mTabs = tabs;
			LinearLayout content = (LinearLayout)findViewById(R.id.content);
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
	public void onRestart()
	{
		super.onRestart();
		mPagerAdapter.notifyDataSetChanged();
		loadTabOrder();
	}
	
	public void onMediaChange() {
		mPagerAdapter.invalidateData();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	//	if (settings.getBoolean(PrefKeys.CONTROLS_IN_SELECTOR, false) != (mControls != null)) {
	//		finish();
	//		startActivity(new Intent(this, LibraryActivity.class));
	//	}
		mDefaultAction = Integer.parseInt(settings.getString(PrefKeys.DEFAULT_ACTION_INT, "7"));
		mLastActedId = LibraryAdapter.INVALID_ID;
		updateHeaders();
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
	public void onRestoreInstanceState(Bundle in)
	{
		SearchTab.setPlayingPosition(in.getInt(PLAY_ROW_NUMBER,-1));
		if (in.getBoolean(SEARCH_BOX_VISIBLE))
//			setSearchBoxVisible(true);
		super.onRestoreInstanceState(in);
	}

	@Override
	protected void onSaveInstanceState(Bundle out)
	{
		super.onSaveInstanceState(out);
		out.putBoolean(SEARCH_BOX_VISIBLE, mSearchBoxVisible);
		int i = SearchTab.getPlayingPosition();
		if (i != -1) {
			out.putInt(PLAY_ROW_NUMBER, i);
		}
	}

	/**
	 * Update the first row of the lists with the appropriate action (play all
	 * or enqueue all).
	 */
	private void updateHeaders()
	{ 
		int action = mDefaultAction;
		if (action == ACTION_LAST_USED)
			action = mLastAction;
		boolean isEnqueue = action == ACTION_ENQUEUE || action == ACTION_ENQUEUE_ALL;
		String text = getString(isEnqueue ? R.string.enqueue_all : R.string.play_all);
//		mPagerAdapter.setHeaderText(text);
	}

	/**
	 * Adds songs matching the data from the given intent to the song timelime.
	 *
	 * @param intent An intent created with
	 * {@link LibraryAdapter#createData(View)}.
	 * @param action One of LibraryActivity.ACTION_*
	 */
	private void pickSongs(Intent intent, int action)
	{ 
		long id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);

		boolean all = false;
		int mode = action;
//		if (action == ACTION_PLAY_ALL || action == ACTION_ENQUEUE_ALL) {
//			int type = mCurrentAdapter.getMediaType();
//			boolean notPlayAllAdapter = type > MediaUtils.TYPE_SONG || id == LibraryAdapter.HEADER_ID;
//			if (mode == ACTION_ENQUEUE_ALL && notPlayAllAdapter) {
//				mode = ACTION_ENQUEUE;
//			} else if (mode == ACTION_PLAY_ALL && notPlayAllAdapter) {
//				mode = ACTION_PLAY;
//			} else { 
//				all = true;
//			}
//		}
		
		QueryTask query = buildQueryFromIntent(intent, false, all);
		query.mode = modeForAction[mode];
//		PlaybackService.get(this).addSongs(query);

		mLastActedId = id;

		if (mDefaultAction == ACTION_LAST_USED && mLastAction != action) {
			mLastAction = action;
			updateHeaders();
		}
	}

	/**
	 * Called by LibraryAdapters when a row has been clicked.
	 *
	 * @param rowData The data for the row that was clicked.
	 */
	public void onItemClicked(Intent rowData)
	{ 
		int action = mDefaultAction;
		if (action == ACTION_LAST_USED){
			action = mLastAction;}

//		if (action == ACTION_EXPAND && rowData.getBooleanExtra(LibraryAdapter.DATA_EXPANDABLE, false)) {
//			onItemExpanded(rowData);
//		} else 
			if (rowData.getLongExtra(LibraryAdapter.DATA_ID, LibraryAdapter.INVALID_ID) == mLastActedId) {
//			openPlaybackActivity();
		} else if (action != ACTION_DO_NOTHING) {
			if (action == ACTION_EXPAND) {
				// default to playing when trying to expand something that can't
				// be expanded
				action = ACTION_PLAY;
			} else if (action == ACTION_PLAY_OR_ENQUEUE) {
				//action = (mState & PlaybackService.FLAG_PLAYING) == 0 ? ACTION_PLAY : ACTION_ENQUEUE;
			   action = ACTION_PLAY;
			}
			pickSongs(rowData, action);
		}
	}

	/**
	 * Create or recreate the limiter breadcrumbs.
	 */
	public void updateLimiterViews()
	{
		mLimiterViews.removeAllViews();
			mLimiterScroller.setVisibility(View.VISIBLE);
	}

	/**
	 * Builds a media query based off the data stored in the given intent.
	 *
	 * @param intent An intent created with
	 * {@link LibraryAdapter#createData(View)}.
	 * @param empty If true, use the empty projection (only query id).
	 * @param all If true query all songs in the adapter; otherwise query based
	 * on the row selected.
	 */
	private QueryTask buildQueryFromIntent(Intent intent, boolean empty, boolean all)
	{
		int type = intent.getIntExtra("type", MediaUtils.TYPE_INVALID);

		String[] projection;
//		if (type == MediaUtils.TYPE_PLAYLIST)
//			projection = empty ? Song.EMPTY_PLAYLIST_PROJECTION : Song.FILLED_PLAYLIST_PROJECTION;
//		else
			projection = empty ? Song.EMPTY_PROJECTION : Song.FILLED_PROJECTION;

		long id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);
		QueryTask query;
//		if (type == MediaUtils.TYPE_FILE) {
//			query = MediaUtils.buildFileQuery(intent.getStringExtra("file"), projection);
//		} else 
			if (all || id == LibraryAdapter.HEADER_ID) {
			query = ((MediaAdapter)mPagerAdapter.mAdapters[type]).buildSongQuery(projection);
			query.data = id;
		} else {
			query = MediaUtils.buildQuery(type, id, projection, null);
		}

		return query;
	}


	/**
	 * Called when a new page becomes visible.
	 *
	 * @param position The position of the new page.
	 * @param adapter The new visible adapter.
	 */
	public void onPageChanged(int position, LibraryAdapter adapter) {
		mCurrentAdapter = adapter;
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

//	public EditText getTextFilter() {
//		return mTextFilter;
//	}
	
	@Override
	public ApplicationInfo getApplicationInfo()
	{
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
		TextView tv = (TextView) findViewById(R.id.player_title_song);
		tv.setText(strTitle);
		SeekBar sb = (SeekBar) findViewById(R.id.player_progress_song);
		sb.setProgress(0);
		ImageButton ib = (ImageButton) findViewById(R.id.player_play_song);
		ib.setVisibility(View.GONE);
		ProgressBar pb = (ProgressBar) findViewById(R.id.player_progress_play);
		pb.setVisibility(View.VISIBLE);
	}
	
	public void resetPlayer() {
		if(player != null) {
			player.stopTask();
			player.remove();
			player = null;
		}
	}
	
	public void play(String path, String strArtist, String strTitle, String strDuration, String from, int position) {
		if (player != null && player.getPosition() == position) {
			player.restart();
			return;
		} else if (player != null && player.getPosition() != position) {
			player.remove();
			player = null;
		}
		player = new Player(path, strArtist, strTitle, strDuration, from, position);
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
			showEditDialog();
			break;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle(music.getSongArtist() + " - " + music.getSongTitle());															
				menu.add(0,DELETE,0,getResources().getString(R.string.delete_song));
				menu.add(0,EDIT_TAG,0,getResources().getString(R.string.edit_mp3_tags));
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
			File file = new File(Environment.getExternalStorageDirectory() + PrefKeys.DIRECTORY_PREFIX + music.getSongTitle() + " - " + music.getSongArtist() + ".mp3" );
			if(!file.exists()) {
				Log.d("file do not", "exists");
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
	 public void showEditDialog() {
			final File file = new File(music.getFileUri());
			final MP3Editor editor = new MP3Editor(this);
			AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(editor.getView());
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d("log", "on start");
							String artistName = editor.getNewArtistName();
							String albumTitle =  editor.getNewAlbumTitle();
							String songTitle = editor.getNewSongTitle();
							boolean useAlbumCover = editor.useAlbumCover();
							rename(file, artistName, albumTitle, songTitle, useAlbumCover);
							
						}

					});
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
						
					});
			
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
	 }
	 
	 private void rename(File file, String artist, String album, String song, boolean useAlbumCover) {
	     Bitmap cover = null;
	     try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set == null) {
				return;
			}
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			if (!album.equals("")) {
				metadata.clearAlbum();
				metadata.setAlbum(album);
			}
			if (!song.equals("")) {
				metadata.clearSongTitle();
				metadata.setSongTitle(song);
			}
			if (!artist.equals("")) {
				metadata.clearArtist();
				metadata.setArtist(artist);
			}
			if (!useAlbumCover) {
				metadata.clearPictureList();
				cover = BitmapFactory.decodeResource(getResources(), R.drawable.fallback_cover);
				ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
				cover.compress(CompressFormat.JPEG, 85, out);
				metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
			}
			new MyID3().update(file, src_set, metadata);
			notifyMediascanner(file.getAbsolutePath(), artist, song, cover);
		} catch (UnsupportedEncodingException e) {
		} catch (ID3WriteException e) {
		} catch (IOException e) {
		}
	}
	 
	private void notifyMediascanner(String path, final String artist, final String title, final Bitmap cover) {
		File file = new File(path);
		MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
				int i = getSelectedItem();
				mPagerAdapter.updateMusicData(i, artist, title, cover);
			}

		});
	}	
	
	public class CustomTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String text = mTextFilter.getText().toString().toLowerCase(Locale.ENGLISH);
			DownloadsTab.getInstance().setFilter(text);
		}

		@Override
		public void afterTextChanged(Editable s) {
			
		}
	}
}