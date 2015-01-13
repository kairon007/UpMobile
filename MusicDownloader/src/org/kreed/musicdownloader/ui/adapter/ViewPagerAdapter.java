package org.kreed.musicdownloader.ui.adapter;

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
import java.util.ArrayList;
import java.util.Arrays;

import org.kreed.musicdownloader.PrefKeys;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.tab.DownloadsTab;
import org.kreed.musicdownloader.ui.tab.SearchView;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * PagerAdapter that manages the library media ListViews.
 */
public class ViewPagerAdapter extends PagerAdapter implements Handler.Callback, ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener, MediaScannerConnectionClient {
	
	private static final String CURRENT_POSITION = "current_position_bundle";
	/**
	 * The number of unique list types. The number of visible lists may be
	 * smaller.
	 */
	public static final int MAX_ADAPTER_COUNT = 3;// 7;
	/**
	 * The human-readable title for each list. The positions correspond to the
	 * MediaUtils ids, so e.g. TITLES[MediaUtils.TYPE_SONG] = R.string.songs
	 */
	public static final int[] TITLES = { R.string.tab_search, R.string.tab_downloads, R.string.tab_library };
	/**
	 * Default tab order.
	 */
	public static final int[] DEFAULT_ORDER = {0,1,2};
	/**
	 * The user-chosen tab order.
	 */
	public int[] mTabOrder;

	private int mTabCount;
	/**
	 * The ListView for each adapter. Each index corresponds to that list's
	 * MediaUtils id.
	 */
	private final ListView[] mLists = new ListView[MAX_ADAPTER_COUNT];
	/**
	 * The index of the current page.
	 */
	private int mCurrentPage;
	/**
	 * The MainActivity that owns this adapter. The adapter will be notified of
	 * changes in the current page.
	 */
	private final MainActivity mActivity;
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
	public LibraryTabAdapter adapterLibrary = null;

	/**
	 * Create the LibraryPager.
	 * 
	 * @param activity
	 *            The MainActivity that will own this adapter. The activity will
	 *            receive callbacks from the ListViews.
	 * @param workerLooper
	 *            A Looper running on a worker thread.
	 */
	public ViewPagerAdapter(MainActivity activity) {
		mActivity = activity;
		mCurrentPage = -1;
	}

	/**
	 * Load the tab order from SharedPreferences.
	 * 
	 * @return True if order has changed.
	 */
	public boolean loadTabOrder() {
		String in = PreferenceManager.getDefaultSharedPreferences(mActivity).getString(PrefKeys.TAB_ORDER, null);
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
					if (v >= 3) {
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
			case 0:
				downloadsPosition = i;
				break;
			case 1:
				musicPosition = i;
				break;
			case 2:
				libraryPosition = i;
				break;
			}
		}
		mMusicPosition = musicPosition;
		mDownloadsPosition = downloadsPosition;
		mLibraryPosition = libraryPosition;
	}

	public void addMusicData(final MusicData musicData) {
		if (null != adapterLibrary) {
			adapterLibrary.add(musicData);
		}

	}

	public void removeMusicData(final MusicData musicData) {
		if (null != adapterLibrary) {
			adapterLibrary.remove(musicData);
		}
	}

	public void updateMusicData(MusicData oldData, MusicData newData) {
		if (null != adapterLibrary) {
			adapterLibrary.updateItem(oldData, newData);
		}
	}
	
	public void removeDataByPath(String filePath) {	
		adapterLibrary.removeByUri(filePath);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		int type = mTabOrder[position];
		ListView view = mLists[type];
		if (view == null) {
			MainActivity activity = mActivity;
			LayoutInflater inflater = activity.getLayoutInflater();
			switch (type) {
			case 0:
				if (searchView == null) {
					searchView = new SearchView(inflater, this, mActivity);
					viewSearchActivity = searchView.getView();
				}
				container.addView(viewSearchActivity);
				return viewSearchActivity;
			case 1:
				View downloadView = DownloadsTab.getInstanceView(inflater, activity);
				if (null != downloadView) {
					container.addView(downloadView);
				}
				return downloadView;
			case 2:
				adapterLibrary = new LibraryTabAdapter(0, activity);
				view = (ListView) inflater.inflate(R.layout.listview, null);
				view.setAdapter(adapterLibrary);
				view.setDivider(mActivity.getResources().getDrawable(R.drawable.layout_divider));
				if (adapterLibrary.checkDeployFilter()) {
					view.setVisibility(View.INVISIBLE);
				}
				fillLibrary();
				break;				
			default:
				break;
			}
			view.setOnItemClickListener(this);
			view.setTag(type);
			enableFastScroll(view);
			mLists[type] = view;
		}
		container.addView(view);
		return view;
	}

	public void fillLibrary() {
		File contentFile = new File(Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX);
		if (!contentFile.exists()) {
			contentFile.mkdirs();
		}
		adapterLibrary.clear();
		querySong();
		android.util.Log.d("logd", "fillLibrary: ");
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				adapterLibrary.notifyDataSetChanged();
			}
		});
	}
	
	private void querySong() {
		Cursor cursor = buildQuery(mActivity.getContentResolver());
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			return;
		}
		MusicData d = new MusicData();
		d.populate(cursor);
		adapterLibrary.add(d);
		while (cursor.moveToNext()) {
			MusicData data = new MusicData();
			data.populate(cursor);
			adapterLibrary.add(data);
		}
		cursor.close();
	}
	
	private Cursor buildQuery(ContentResolver resolver) {
		String selection =  MediaStore.MediaColumns.DATA + " LIKE '" + Environment.getExternalStorageDirectory() + "/MusicDownloader" + "%'" ;
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicData.FILLED_PROJECTION, selection, null, null);
		return cursor;
	}
	
	public void cleanLibrary() {
		adapterLibrary.clear();
	}

	@Override
	public int getItemPosition(Object item) {
		return POSITION_NONE;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mActivity.getResources().getText(TITLES[mTabOrder[position]]);
	}

	@Override
	public int getCount() { //
		return mTabCount;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		int type = mTabOrder[position];
		if (position != mCurrentPage) {
			mCurrentPage = position;
			mActivity.onPageChanged(position);
		}
		currentType = type;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public Parcelable saveState() {
		Bundle out = new Bundle(10);
		int[] savedPositions = new int[MAX_ADAPTER_COUNT];
		ListView[] lists = mLists;
		for (int i = MAX_ADAPTER_COUNT; --i != -1;) {
			if (lists[i] != null) {
				savedPositions[i] = lists[i].getFirstVisiblePosition();
			}
		}
		out.putIntArray(CURRENT_POSITION, savedPositions);
		return out;
	}
	private SearchView searchView;
	private View viewSearchActivity;

	@Override
	public boolean handleMessage(Message message) {
		return true;
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	public int getCurrentType() {
		return currentType;
	}

	@Override
	public void onPageSelected(int position) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PrefKeys.LIBRARY_PAGE, position);
		editor.commit();
		// onPageSelected and setPrimaryItem are called in similar cases, and it
		// would be nice to use just one of them, but each has caveats:
		// - onPageSelected isn't called when the ViewPager is first
		// initialized
		// - setPrimaryItem isn't called until scrolling is complete, which
		// makes tab bar and limiter updates look bad
		// So we use both.
		setPrimaryItem(null, position, null);
		mActivity.getSearchLayout().setVisibility(position == 0 ? View.GONE : View.VISIBLE);
		mActivity.getSearchLayout().findViewById(R.id.clear_all_button).setVisibility(position == 0 || position == 2 ? View.GONE : View.VISIBLE);
		if (position == 0) {
			searchView.notifyAdapter();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> list, View view, int position, long id) {
	}

	/**
	 * Enables FastScroller on the given list, ensuring it is always visible and
	 * suppresses the match drag position feature in newer versions of Android.
	 * 
	 * @param list
	 *            The list to enable.
	 */
	private void enableFastScroll(ListView list) {
		mActivity.setFakeTarget(true);
		list.setFastScrollEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// CompatHoneycomb.setFastScrollAlwaysVisible(list);
		}
		mActivity.setFakeTarget(false);
	}

	public SearchView getSearchView() {
		return searchView;
	}
	
	public ListView getListView(){
		return mLists[mCurrentPage];
	}

	@Override
	public void onMediaScannerConnected() {
		android.util.Log.d("logd", "onMediaScannerConnected: ");
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		android.util.Log.d("logd", "onScanCompleted: " + path  + " - "  + uri);
		
	}
}
