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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.cmc.music.common.ID3v1Genre;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.musicdownloader.app.MusicDownloaderApp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * PagerAdapter that manages the library media ListViews.
 */
public class LibraryPagerAdapter
	extends PagerAdapter
	implements Handler.Callback
	         , ViewPager.OnPageChangeListener
//	         , View.OnCreateContextMenuListener
	         , AdapterView.OnItemClickListener
{
	/**
	 * The number of unique list types. The number of visible lists may be
	 * smaller.
	 */
	public static final int MAX_ADAPTER_COUNT = 3;//7;
	/**
	 * The human-readable title for each list. The positions correspond to the
	 * MediaUtils ids, so e.g. TITLES[MediaUtils.TYPE_SONG] = R.string.songs
	 */
	public static final int[] TITLES = { R.string.search, R.string.downloads,
			R.string.library };
	/**
	 * Default tab order.
	 */
	public static final int[] DEFAULT_ORDER = { MediaUtils.TYPE_SEARCH,
			MediaUtils.TYPE_DOWNLOADS, MediaUtils.TYPE_LIBRARY };
	/**
	 * The user-chosen tab order.
	 */
	int[] mTabOrder;
	/**
	 * The number of visible tabs.
	 */
	private Cursor mCursor;
	
	private int mTabCount;
	/**
	 * The ListView for each adapter. Each index corresponds to that list's
	 * MediaUtils id.
	 */
	private final ListView[] mLists = new ListView[MAX_ADAPTER_COUNT];
	/**
	 * The adapters. Each index corresponds to that adapter's MediaUtils id.
	 */
	public LibraryAdapter[] mAdapters = new LibraryAdapter[MAX_ADAPTER_COUNT];
	/**
	 * Whether the adapter corresponding to each index has stale data.
	 */
	private final boolean[] mRequeryNeeded = new boolean[MAX_ADAPTER_COUNT];
	/**
	 * The artist adapter instance, also stored at mAdapters[MediaUtils.TYPE_ARTIST].
	 */
	private MediaAdapter mDownloadsAdapter;
	/**
	 * The album adapter instance, also stored at mAdapters[MediaUtils.TYPE_ALBUM].
	 */
	private MediaAdapter mLibraryAdapter;
	/**
	 * The song adapter instance, also stored at mAdapters[MediaUtils.TYPE_SONG].
	 */
	private MediaAdapter mMusicAdapter;
	/**
	 * The adapter of the currently visible list.
	 */
	private LibraryAdapter mCurrentAdapter;
	/**
	 * The index of the current page.
	 */
	private int mCurrentPage;
	/**
	 * List positions stored in the saved state, or null if none were stored.
	 */
	private int[] mSavedPositions;
	/**
	 * The MainActivity that owns this adapter. The adapter will be notified
	 * of changes in the current page.
	 */
	private final MainActivity mActivity;
	/**
	 * A Handler running on the UI thread.
	 */
	private final Handler mUiHandler;
	/**
	 * A Handler running on a worker thread.
	 */
	private final Handler mWorkerHandler;
	/**
	 * The text to be displayed in the first row of the artist, album, and
	 * song limiters.
	 */
	private String mHeaderText;
	private TextView mArtistHeader;
	private TextView mAlbumHeader;
	private TextView mSongHeader;
	/**
	 * The current filter text, or null if none.
	 */
	private String mFilter;
	/**
	 * The position of the songs page, or -1 if it is hidden.
	 */
	public int mMusicPosition = -1;
	/**
	 * The position of the albums page, or -1 if it is hidden.
	 */
	public int mDownloadsPosition = -1;
	/**
	 * The position of the artists page, or -1 if it is hidden.
	 */
	public int mLibraryPosition = -1;

	private int currentType = -1;
	private Context context;
	private LibraryPagerAdapter parentAdapter = this;
	public LibraryPageAdapter adapterLibrary = null;
	private ArrayList<MusicData> array = new ArrayList<MusicData>();
	private boolean isAdded;
	/**
	 * Create the LibraryPager.
	 *
	 * @param activity The MainActivity that will own this adapter. The activity
	 * will receive callbacks from the ListViews.
	 * @param workerLooper A Looper running on a worker thread.
	 */
	public LibraryPagerAdapter(MainActivity activity, Looper workerLooper) 	{  
		mActivity = activity;
		context = activity.getBaseContext();
		mUiHandler = new Handler(this);
		mWorkerHandler = new Handler(workerLooper, this);
		mCurrentPage = -1;
//		activity.getContentResolver().registerContentObserver(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, true, mPlaylistObserver);
	}
	
	/**
	 * Load the tab order from SharedPreferences.
	 *
	 * @return True if order has changed.
	 */
	public boolean loadTabOrder() {
		String in = PreferenceManager.getDefaultSharedPreferences(context).getString( PrefKeys.TAB_ORDER, null);
		int[] order;
		int count;
		if (in == null || in.length() != MAX_ADAPTER_COUNT) {
			order = DEFAULT_ORDER;
			count = MAX_ADAPTER_COUNT;
		} else {
			char[] chars = in.toCharArray();
			order = new int[MAX_ADAPTER_COUNT];
			count = 0;
			for (int i = 0; i != MAX_ADAPTER_COUNT; ++i) {
				char v = chars[i];
				if (v >= 128) {
					v -= 128;
					if (v >= MediaUtils.TYPE_COUNT) {
						// invalid media type; use default order
						order = DEFAULT_ORDER;
						count = MAX_ADAPTER_COUNT;
						break;
					}
					order[count++] = v;
				}
			}
		}

		if (count != mTabCount || !Arrays.equals(order, mTabOrder)) {
			mTabOrder = order;
			mTabCount = count;
			notifyDataSetChanged();
			computeExpansions();
			return true;
		}

		return false;
	}

	public void computeExpansions() {
		int[] order = mTabOrder;
		int musicPosition = -1;
		int downloadsPosition = -1;
		int libraryPosition = -1;
		for (int i = mTabCount - 1; --i > -1;) {
			switch (order[i]) {
			case MediaUtils.TYPE_SEARCH:
				downloadsPosition = i;
				break;
			case MediaUtils.TYPE_DOWNLOADS:
				musicPosition = i;
				break;
			case MediaUtils.TYPE_LIBRARY:
				libraryPosition = i;
				break;
			}
		}

		if (mDownloadsAdapter != null)
			mDownloadsAdapter.setExpandable(musicPosition != -1 || downloadsPosition != -1);
		if (mLibraryAdapter != null)
			mLibraryAdapter.setExpandable(musicPosition != -1);
		if (mMusicAdapter != null)
			mMusicAdapter.setExpandable(musicPosition != -1);

		mMusicPosition = musicPosition;
		mDownloadsPosition = downloadsPosition;
		mLibraryPosition = libraryPosition;
	}
	
	public void changeArrayMusicData(final MusicData musicData) {
		if (null != adapterLibrary) {
			
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adapterLibrary.add(musicData);
					isAdded = true;
				}
			});
			
		}
		
	}
	
	public void removeMusicData(final MusicData musicData) {
		if (null != adapterLibrary) {
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					adapterLibrary.remove(musicData);
				}
			});
		}
	}
	
	public void updateMusicData(final int i, final String artist, final String title) {
		if (null != adapterLibrary) {
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adapterLibrary.updateItem(i, artist, title);
					adapterLibrary.setNotifyOnChange(true);
					Log.d("log", ";;;;;");
				}
				
			});
		}
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{ 
		int type = mTabOrder[position];
		ListView view = mLists[type];

		if (view == null) {
			MainActivity activity = mActivity;
			LayoutInflater inflater = activity.getLayoutInflater();
			TextView header = null;
			Typeface font = MusicDownloaderApp.FONT_LIGHT;
			switch (type) {
			case MediaUtils.TYPE_SEARCH: 
				View searchView = SearchTab.getInstanceView(inflater, activity, parentAdapter);
				container.addView(searchView);
				return searchView;
			case MediaUtils.TYPE_DOWNLOADS:
				View downloadView = DownloadsTab.getInstanceView(inflater, activity);
				container.addView(downloadView);
//				mLists[type] = (ListView) downloadView;
				return downloadView;
			case MediaUtils.TYPE_LIBRARY:
				ArrayList<MusicData> arrayMusic = new ArrayList<MusicData>();
				File contentFile = new File( Environment.getExternalStorageDirectory() + PrefKeys.DIRECTORY_PREFIX);
				long contentFileLength = contentFile.length();
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					contentFileLength = 1;
				}
				if (contentFileLength > 0) {
					File[] files = contentFile.listFiles();
					for (int i = 0; i < files.length; i++) {
						try {
							MusicMetadataSet src_set = new MyID3().read(files[i]);
							if (src_set != null) {
								MusicMetadata metadata = src_set.merged;
								if (metadata.isEmpty()) continue;
								String strArtist = metadata.getArtist();
								String strDuration = "";
								String strTitle = "";
								String strPath = "";
								if (metadata.getSongTitle() != null) {
									int index = metadata.getSongTitle().indexOf('/');
									strPath = files[i].getAbsolutePath();
									if (index != -1) {
										strTitle = metadata.getSongTitle().substring(0, index);
										strDuration = metadata.getSongTitle().substring(index + 1);
									} else {
										strTitle = metadata.getSongTitle();
									}
								}
								Bitmap bitmap = DBHelper.getArtworkImage(2, metadata);
								String strGenre;
								if (metadata.containsKey("genre_id")) {
									int genre_id = (Integer) metadata
											.get("genre_id");
									strGenre = ID3v1Genre.get(genre_id);
								} else {
									strGenre = "unknown";
								}
								if (!isAdded) {
									MusicData data = new MusicData(strArtist, strTitle, strDuration, bitmap, strGenre);
									data.setFileUri(strPath);
									arrayMusic.add(data);
								}
							} else {
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					adapterLibrary = new LibraryPageAdapter(mActivity, 0, arrayMusic, activity);
					view = (ListView) inflater.inflate(R.layout.listview, null);
					view.setAdapter(adapterLibrary);
				} else {
					view = (ListView) inflater.inflate(R.layout.listview, null);
					view.setAdapter(adapterLibrary);
				}
				break;
			default:
				break;
			} 
			if ("AppTheme.White".equals(Util.getThemeName(mActivity))) {
				view.setDivider(new ColorDrawable
						(activity.getResources().getColor(R.color.divider_color_light)));
				view.setDividerHeight(1);
			}
			view.setOnItemClickListener(this);
			view.setTag(type);
			enableFastScroll(view);
			mLists[type] = view;
			mRequeryNeeded[type] = true;
		}
		requeryIfNeeded(type);
		container.addView(view);
		return view;
	}
	
	public Bitmap getArtworkImage(int maxWidth, MusicMetadata metadata) {
		if (maxWidth == 0) {
			return null;
		}
		Vector<ImageData> pictureList = metadata.getPictureList();
		if ((pictureList == null) || (pictureList.size() == 0)) {
			return null;
		}
		ImageData imageData = (ImageData) pictureList.get(0);
		if (imageData == null) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		int scale = 1;
		if ((maxWidth != -1) && (opts.outWidth > maxWidth)) {
			// Find the correct scale value. It should be the power of 2.
			int scaleWidth = opts.outWidth;
			while (scaleWidth > maxWidth) {
				scaleWidth /= 2;
				scale *= 2;
			}
		}
		opts = new BitmapFactory.Options();
		opts.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeByteArray(imageData.imageData,
				0, imageData.imageData.length, opts);
		return bitmap;
	}

	@Override
	public int getItemPosition(Object item) {
//		int type = (Integer)((ListView)item).getTag();
//		Log.d("ffuu", ""+type);
//		int[] order = mTabOrder;
//		for (int i = mTabCount; --i != -1; ) {
//			if (order[i] == type)
//				return i;
//		}
		return POSITION_NONE;
	}
 
	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View)object); 
	}

	@Override
	public CharSequence getPageTitle(int position)
	{
		return mActivity.getResources().getText(TITLES[mTabOrder[position]]);
	}

	@Override
	public int getCount()
	{ //
		return mTabCount; 
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == object;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object)
	{
		int type = mTabOrder[position];
		LibraryAdapter adapter = mAdapters[type];
		if (position != mCurrentPage || adapter != mCurrentAdapter) {
			requeryIfNeeded(type);
			mCurrentAdapter = adapter;
			mCurrentPage = position;
			mActivity.onPageChanged(position, adapter);
		}
		currentType = type;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader)
	{
		Bundle in = (Bundle)state;
//		mPendingLibraryLimiter = (Limiter)in.getSerializable("limiter_albums");
//		mPendingSongLimiter = (Limiter)in.getSerializable("limiter_songs");
//		mPendingFileLimiter = (Limiter)in.getSerializable("limiter_files");
		mSavedPositions = in.getIntArray("pos");
	}

	@Override
	public Parcelable saveState()
	{
		Bundle out = new Bundle(10);
		int[] savedPositions = new int[MAX_ADAPTER_COUNT];
		ListView[] lists = mLists;
		for (int i = MAX_ADAPTER_COUNT; --i != -1; ) {
			if (lists[i] != null) {
				savedPositions[i] = lists[i].getFirstVisiblePosition();
			}
		}
		out.putIntArray("pos", savedPositions);
		return out;
	}


//	/**
//	 * Clear a limiter.
//	 * 
//	 * @param type
//	 *            Which type of limiter to clear.
//	 */
//	public void clearLimiter(int type) {
//		if (mLibraryAdapter == null) {
//			mPendingLibraryLimiter = null;
//		} else {
//			mLibraryAdapter.setLimiter(null);
//			loadSortOrder(mLibraryAdapter);
//			requestRequery(mLibraryAdapter);
//		}
//		if (mSongAdapter == null) {
//			mPendingSongLimiter = null;
//		} else {
//			mSongAdapter.setLimiter(null);
//			loadSortOrder(mSongAdapter);
//			requestRequery(mSongAdapter);
//		}
//	}

	/**
	 * Update the adapters with the given limiter.
	 *
	 * @param limiter The limiter to set.
	 * @return The tab type that should be switched to to expand the row.
	 */
//	public int setLimiter(Limiter limiter)
//	{
//		int tab;
//
//		switch (limiter.type) {
//		case MediaUtils.TYPE_ALBUM: 
//			if (mSongAdapter == null) {
//				mPendingSongLimiter = limiter;
//			} else {
//				mSongAdapter.setLimiter(limiter);
//				loadSortOrder(mSongAdapter);
//				requestRequery(mSongAdapter);
//			}
//			tab = mSongsPosition;
//			break;
//		case MediaUtils.TYPE_ARTIST: 
//			if (mLibraryAdapter == null) {
//				mPendingLibraryLimiter = limiter;
//			} else {
//				mLibraryAdapter.setLimiter(limiter);
//				loadSortOrder(mLibraryAdapter);
//				requestRequery(mLibraryAdapter);
//			}
//			if (mSongAdapter == null) {
//				mPendingSongLimiter = limiter;
//			} else {
//				mSongAdapter.setLimiter(limiter);
//				loadSortOrder(mSongAdapter);
//				requestRequery(mSongAdapter);
//			}
//			tab = mDownloadsPosition;
//			if (tab == -1)
//				tab = mSongsPosition;
//			break;
//		case MediaUtils.TYPE_LIBRARY: 
//			if (mLibraryAdapter == null) {
//				mPendingLibraryLimiter = limiter;
//			} else {
//				mLibraryAdapter.setLimiter(limiter);
//				loadSortOrder(mLibraryAdapter);
//				requestRequery(mLibraryAdapter);
//			}
//			if (mSongAdapter == null) {
//				mPendingSongLimiter = null;
//			} else {
//				mSongAdapter.setLimiter(limiter);
//				loadSortOrder(mSongAdapter);
//				requestRequery(mSongAdapter);
//			}
//			tab = mSongsPosition;
//			break;
//		case MediaUtils.TYPE_FILE: 
//			if (mFilesAdapter == null) {
//				mPendingFileLimiter = limiter;
//			} else {
//				mFilesAdapter.setLimiter(limiter);
//				requestRequery(mFilesAdapter);
//			}
//			tab = -1;
//			break;
//		default:
//			throw new IllegalArgumentException("Unsupported limiter type: " + limiter.type);
//		}
//
//		return tab;
//	}

	/**
	 * Returns the limiter set on the current adapter or null if there is none.
//	 */
//	public Limiter getCurrentLimiter()
//	{
//		LibraryAdapter current = mCurrentAdapter;
//		if (current == null)
//			return null;
//		return current.getLimiter();
//	}

	/**
	 * Run on query on the adapter passed in obj.
	 *
	 * Runs on worker thread.
	 */
	private static final int MSG_RUN_QUERY = 0;
	/**
	 * Save the sort mode for the adapter passed in obj.
	 *
	 * Runs on worker thread.
	 */
	private static final int MSG_SAVE_SORT = 1;
	/**
	 * Call {@link LibraryPagerAdapter#requestRequery(LibraryAdapter)} on the adapter
	 * passed in obj.
	 *
	 * Runs on worker thread.
	 */
	private static final int MSG_REQUEST_REQUERY = 2;
	/**
	 * Commit the cursor passed in obj to the adapter at the index passed in
	 * arg1.
	 *
	 * Runs on UI thread.
	 */
	private static final int MSG_COMMIT_QUERY = 3;

	@Override
	public boolean handleMessage(Message message)
	{ 
		switch (message.what) {
		case MSG_RUN_QUERY: {
			SortAdapter adapter = (SortAdapter)message.obj;
			int index = adapter.getMediaType();
			Handler handler = mUiHandler;
			handler.sendMessage(handler.obtainMessage(MSG_COMMIT_QUERY, index, 0, adapter.query()));
			break;
		}
		case MSG_COMMIT_QUERY: {
			int index = message.arg1;
			mAdapters[index].commitQuery(message.obj);
			int pos;
			if (mSavedPositions == null) {
				pos = 0;
			} else {
				pos = mSavedPositions[index];
				mSavedPositions[index] = 0;
			}
			mLists[index].setSelection(pos);
			break;
		}
		case MSG_SAVE_SORT: {
			SortAdapter adapter = (SortAdapter)message.obj;
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putInt(String.format("sort_%d_%d", adapter.getMediaType(), adapter.getLimiterType()), adapter.getSortMode());
			editor.commit();
			break;
		}
		case MSG_REQUEST_REQUERY:
			requestRequery((LibraryAdapter)message.obj);
			break;
		default:
			return false;
		}

		return true;
	}

	/**
	 * Requery the given adapter. If it is the current adapter, requery
	 * immediately. Otherwise, mark the adapter as needing a requery and requery
	 * when its tab is selected.
	 *
	 * Must be called on the UI thread.
	 */
	public void requestRequery(LibraryAdapter adapter)
	{
		if (adapter == mCurrentAdapter) {
			postRunQuery(adapter);
		} else {
			mRequeryNeeded[adapter.getMediaType()] = true;
			// Clear the data for non-visible adapters (so we don't show the old
			// data briefly when we later switch to that adapter)
			adapter.clear();
		}
	}

	/**
	 * Call {@link LibraryPagerAdapter#requestRequery(LibraryAdapter)} on the UI
	 * thread.
	 *
	 * @param adapter The adapter, passed to requestRequery.
	 */
	public void postRequestRequery(LibraryAdapter adapter)
	{
		Handler handler = mUiHandler;
		handler.sendMessage(handler.obtainMessage(MSG_REQUEST_REQUERY, adapter));
	}

	/**
	 * Schedule a query to be run for the given adapter on the worker thread.
	 *
	 * @param adapter The adapter to run the query for.
	 */
	private void postRunQuery(LibraryAdapter adapter)
	{
		mRequeryNeeded[adapter.getMediaType()] = false;
		Handler handler = mWorkerHandler;
		handler.removeMessages(MSG_RUN_QUERY, adapter);
		handler.sendMessage(handler.obtainMessage(MSG_RUN_QUERY, adapter));
	}

	/**
	 * Requery the adapter of the given type if it exists and needs a requery.
	 *
	 * @param type One of MediaUtils.TYPE_*
	 */
	private void requeryIfNeeded(int type)
	{
		LibraryAdapter adapter = mAdapters[type];
		if (adapter != null && mRequeryNeeded[type]) {
			postRunQuery(adapter);
		}
	}

	/**
	 * Invalidate the data for all adapters.
	 */
	public void invalidateData()
	{
		for (LibraryAdapter adapter : mAdapters) {
			if (adapter != null) {
				postRequestRequery(adapter);
			}
		}
	}

	/**
	 * Set the saved sort mode for the given adapter. The adapter should
	 * be re-queried after calling this.
	 *
	 * @param adapter The adapter to load for.
	 */
	public void loadSortOrder(SortAdapter adapter)
	{
		String key = String.format("sort_%d_%d", adapter.getMediaType(), adapter.getLimiterType());
		int def = adapter.getDefaultSortMode();
		int sort = PreferenceManager.getDefaultSharedPreferences(context).getInt(key, def);
		adapter.setSortMode(sort);
	}

	/**
	 * Set the sort mode for the current adapter. Current adapter must be a
	 * MediaAdapter. Saves this sort mode to preferences and updates the list
	 * associated with the adapter to display the new sort mode.
	 *
	 * @param mode The sort mode. See {@link MediaAdapter#setSortMode(int)} for
	 * details.
	 */
	public void setSortMode(int mode)
	{
		SortAdapter adapter = (SortAdapter)mCurrentAdapter;//MediaAdapter adapter = (MediaAdapter)mCurrentAdapter;
		if (mode == adapter.getSortMode())
			return;

		adapter.setSortMode(mode);
		requestRequery(adapter);

		// Force a new FastScroller to be created so the scroll sections
		// are updated.
		ListView view = mLists[mTabOrder[mCurrentPage]];
		view.setFastScrollEnabled(false);
		enableFastScroll(view);

		Handler handler = mWorkerHandler;
		handler.sendMessage(handler.obtainMessage(MSG_SAVE_SORT, adapter));
	}

	/**
	 * Set a new filter on all the adapters.
	 */
	public void setFilter(String text)
	{
		if (text.length() == 0)
			text = null;

		mFilter = text;
		for (LibraryAdapter adapter : mAdapters) {
			if (adapter != null) {
				adapter.setFilter(text);
				requestRequery(adapter);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}
	
	public int getCurrentType() {
		return currentType;
	}
	
	@Override
	public void onPageSelected(int position)
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PrefKeys.LIBRARY_PAGE, position);
		editor.commit();
		// onPageSelected and setPrimaryItem are called in similar cases, and it
		// would be nice to use just one of them, but each has caveats:
		// - onPageSelected isn't called when the ViewPager is first
		//   initialized
		// - setPrimaryItem isn't called until scrolling is complete, which
		//   makes tab bar and limiter updates look bad
		// So we use both.
		setPrimaryItem(null, position, null);
		mActivity.getSearchLayout().setVisibility(position == 0 ? View.GONE : View.VISIBLE);
		mActivity.getSearchLayout().findViewById(R.id.clear_all_button).setVisibility(position == 0 || position == 2 ? View.GONE : View.VISIBLE);
	}

	/**
	 * Creates the row data used by MainActivity.
	 */
	private static Intent createHeaderIntent(View header)
	{
		int type = (Integer)header.getTag();
		Intent intent = new Intent();
		intent.putExtra(LibraryAdapter.DATA_ID, LibraryAdapter.HEADER_ID);
		intent.putExtra(LibraryAdapter.DATA_TYPE, type);
		return intent;
	}

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
//	{
//		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
//		View targetView = info.targetView;
//		Intent intent = info.id == -1 ? createHeaderIntent(targetView) : mCurrentAdapter.createData(targetView);
//		mActivity.onCreateContextMenu(menu, intent);
//	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id)
	{
		Intent intent = id == -1 ? createHeaderIntent(view) : mCurrentAdapter.createData(view);
		mActivity.onItemClicked(intent);
	}

	/**
	 * Enables FastScroller on the given list, ensuring it is always visible
	 * and suppresses the match drag position feature in newer versions of
	 * Android.
	 *
	 * @param list The list to enable.
	 */
	private void enableFastScroll(ListView list)
	{
		mActivity.mFakeTarget = true;
		list.setFastScrollEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//CompatHoneycomb.setFastScrollAlwaysVisible(list);
		}
		mActivity.mFakeTarget = false;
	}
}

