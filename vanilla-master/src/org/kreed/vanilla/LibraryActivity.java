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

package org.kreed.vanilla;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.UnknownFormatConversionException;

import junit.framework.Assert;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.kreed.vanilla.app.VanillaApp;
import org.kreed.vanilla.equalizer.MyEqualizer;

import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.dialog.MP3Editor;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

/**
 * The library activity where songs to play can be selected from the library.
 */
public class LibraryActivity extends PlaybackActivity implements TextWatcher, DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
	
	private static final String IS_FIRST_RUN = "is_first_run";
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
	/**
	 * The SongTimeline add song modes corresponding to each relevant action.
	 */
	private static final int[] modeForAction = { SongTimeline.MODE_PLAY, SongTimeline.MODE_ENQUEUE, -1, SongTimeline.MODE_PLAY_ID_FIRST, SongTimeline.MODE_ENQUEUE_ID_FIRST };

	private static final String SEARCH_BOX_VISIBLE = "search_box_visible";
	private static final String SWICH_SHOW_DIALOG_RATE = "swich_show_dialog_rate";
	private static final String TYPE_FILE = "type_file";
	private static final String ID_FILE = "id_file";

	public ViewPager mViewPager;
	private TabPageIndicator mTabs;

	private View mSearchBox;
	private boolean mSearchBoxVisible;
	private boolean swichShowDialogRate = true;

	private TextView mTextFilter;
	private View mSortButton;
	private View mEqualizerButton;

	private View mActionControls;
	private View mControls;
	private TextView mTitle;
	private TextView mArtist;
	private TextView mAlbum;
	private ImageView mCover;
	private View mEmptyQueue;
	private ImageButton mClearFilterEditText;
	private MP3Editor editor;
	private int type;
	private long id;
	private boolean isFirstRun = true;

	private HorizontalScrollView mLimiterScroller;
	private ViewGroup mLimiterViews;

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

	private int lastPage = -1;

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

	public static void logToast(Context context, String message) {
		Log.d("", message);
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void hideSoftKeyboard(final EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	// protected void loadMoPubView(MoPubView moPubView, String adUnitId, String
	// keywords) {
	// //TODO
	// if (moPubView == null) {
	// logToast(this, "Unable to inflate MoPubView from xml.");
	// return;
	// }
	//
	// try {
	// validateAdUnitId(adUnitId);
	// } catch (IllegalArgumentException exception) {
	// //logToast(BaseActivity.this, exception.getMessage());
	// return;
	// }
	//
	// // moPubView.setBannerAdListener(this);
	// moPubView.setAdUnitId(adUnitId);
	// moPubView.setKeywords(keywords);
	// moPubView.loadAd();
	// }

	@Override
	public void onDestroy() {
		Advertisement.onDestroy(this);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Advertisement.onPause(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Advertisement.onResume(this);
		updateEqualizerVisibility();
	}

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		if (state == null) {
			checkForLaunch(getIntent());

		} else {
			swichShowDialogRate = state.getBoolean(SWICH_SHOW_DIALOG_RATE);
		}
		if ("AppTheme.White".equals(Util.getThemeName(this))) {
			setTheme(R.style.Library_White);
		} else if ("AppTheme.Black".equals(Util.getThemeName(this))) {
			setTheme(R.style.Library_Black);
		} else {
			setTheme(R.style.Library);
		}
		setContentView(Settings.SHOW_BANNER_ON_TOP ? R.layout.library_content_top : R.layout.library_content);
		// Advertisement.startAppInit(this);
		// Advertisement.mobileCoreInit(this);
		// Advertisement.moPubInit(this);
		// Advertisement.airPushShow(this);
		mSearchBox = findViewById(R.id.search_box);
		if ("AppTheme.White".equals(Util.getThemeName(this))) {
			mSearchBox.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_background_white));
		} else if ("AppTheme.Black".equals(Util.getThemeName(this))) {
			mSearchBox.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_background_black));
		}
		mTextFilter = (TextView) findViewById(R.id.filter_text);
		mTextFilter.addTextChangedListener(this);
		mClearFilterEditText = (ImageButton) findViewById(R.id.clear_filter);
		mClearFilterEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mTextFilter.setText(null);
			}
		});
		// Intent intent = getIntent();
		// if
		// (intent.getAction().equals(Intent.ACTION_VIEW)){//startActivity(new
		// Intent(this, FullPlaybackActivity.class));
		// Uri playUri = Uri.parse("file:///sdcard/music/Cassie - Alex.mp3");
		// intent = new Intent(Intent.ACTION_VIEW, playUri);
		// startActivity(intent);
		// intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(intent);
		// }

		mSortButton = findViewById(R.id.sort_button);
		mSortButton.setOnClickListener(this);

		mEqualizerButton = findViewById(R.id.equalizer_button);
		mEqualizerButton.setOnClickListener(this);

		mLimiterScroller = (HorizontalScrollView) findViewById(R.id.limiter_scroller);
		mLimiterViews = (ViewGroup) findViewById(R.id.limiter_layout);

		LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this, mLooper);
		mPagerAdapter = pagerAdapter;

		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(pagerAdapter);
		mViewPager = pager;

		SharedPreferences settings = PlaybackService.getSettings(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			pager.setOnPageChangeListener(pagerAdapter);
			View controls = getLayoutInflater().inflate(R.layout.actionbar_controls, null);
			mTitle = (TextView) controls.findViewById(R.id.title);
			mArtist = (TextView) controls.findViewById(R.id.artist);
			mAlbum = (TextView) controls.findViewById(R.id.album);
			mCover = (ImageView) controls.findViewById(R.id.cover);
			// mAlbum.setTypeface(font);
			mArtist.setTypeface(VanillaApp.FONT_LIGHT);
			mTitle.setTypeface(VanillaApp.FONT_REGULAR);
			// mTitle.setTextColor(Color.WHITE);
			mTitle.setTextSize(16);
			mArtist.setTextSize(14);
			controls.setOnClickListener(this);
			mActionControls = controls;
		} else {
			if (null != state && state.containsKey(IS_FIRST_RUN)) {
				isFirstRun = state.getBoolean(IS_FIRST_RUN);
			}
			if (isFirstRun) {
				File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
				String[] files = fileDir.list();
				if (files != null) {
					String[] absPathFiles = new String[files.length];
					for (int i = 0; i < files.length; i++) {
						String absolutePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + files[i];
						absPathFiles[i] = absolutePath;
					}
					MediaScannerConnection.scanFile(this, absPathFiles, null, new MediaScannerConnection.OnScanCompletedListener() {
	
						public void onScanCompleted(String path, Uri uri) {
						}
	
					});
				}
			}
			isFirstRun = false;
			TabPageIndicator tabs = new TabPageIndicator(this);
			tabs.setViewPager(pager);
			tabs.setOnPageChangeListener(pagerAdapter);
			mTabs = tabs;

			LinearLayout content = (LinearLayout) findViewById(R.id.content);
			String switchPlace = settings.getString(PrefKeys.SHOW_TAB_POSITION, "top");
			if (switchPlace.equals("top")) {
				content.addView(tabs, 0, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			} else if (switchPlace.equals("bottom")) {
				int i = content.getChildCount();
				content.addView(tabs, i, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			// if (settings.getBoolean(PrefKeys.CONTROLS_IN_SELECTOR, false)) {
			getLayoutInflater().inflate(R.layout.library_controls, content, true);

			mControls = findViewById(R.id.controls);
			if ("AppTheme.White".equals(Util.getThemeName(this))) {
				mControls.setBackgroundResource(R.drawable.music_bottom_playback_bg_light);
			} else if ("AppTheme.Black".equals(Util.getThemeName(this))) {
				mControls.setBackgroundResource(R.drawable.music_bottom_playback_bg_black);
			} else {
				mControls.setBackgroundResource(R.drawable.music_bottom_playback_bg);
			}
			mTitle = (TextView) mControls.findViewById(R.id.title);
			mArtist = (TextView) mControls.findViewById(R.id.artist);
			mAlbum = (TextView) mControls.findViewById(R.id.album);
			mCover = (ImageView) mControls.findViewById(R.id.cover);
			View previous = mControls.findViewById(R.id.previous);
			mPlayPauseButton = (ImageButton) mControls.findViewById(R.id.play_pause);
			View next = mControls.findViewById(R.id.next);

			Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/ProximaNova-Bold.otf");

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

				mAlbum.setTypeface(font);
				mArtist.setTypeface(font);
				mTitle.setTypeface(font);

			}

			mCover.setOnClickListener(this);
			previous.setOnClickListener(this);
			mPlayPauseButton.setOnClickListener(this);
			next.setOnClickListener(this);

			mShuffleButton = (ImageButton) findViewById(R.id.shuffle);
			mShuffleButton.setOnClickListener(this);
			registerForContextMenu(mShuffleButton);
			mEndButton = (ImageButton) findViewById(R.id.end_action);
			mEndButton.setOnClickListener(this);
			registerForContextMenu(mEndButton);

			mEmptyQueue = findViewById(R.id.empty_queue);
			mEmptyQueue.setOnClickListener(this);
			// }
		}
		loadTabOrder();
		page = settings.getInt(PrefKeys.LIBRARY_PAGE, 0);
		if (page != 0) {
			pager.setCurrentItem(page);
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
		// initialize ad networks
		try {
			if (!Settings.getIsBlacklisted(this)) {
				Advertisement.start(this, swichShowDialogRate);
				swichShowDialogRate = false;
			} else {
				Advertisement.showDisclaimer(this);
			}
		} catch (Exception e) {

		}
		// load banner ad
		try {
			if (Settings.ENABLE_ADS) {
				Advertisement.showBanner(this);
			}
		} catch (Exception e) {

		}
	}

	public void setFilterHint(int type) {
		int[] hintResIds = new int[] { R.string.hint_filter_artists, R.string.hint_filter_songs, R.string.hint_filter_playlists, R.string.hint_filter_genres, R.string.hint_filter_files };
		float width = mTextFilter.getPaint().measureText(getResources().getString(hintResIds[type - 1]));
		if(mTextFilter.getWidth() - mClearFilterEditText.getWidth() < width) {
			mTextFilter.setHint(Html.fromHtml("<small>" + getResources().getString(hintResIds[type - 1]) + "</small>"));
		} else mTextFilter.setHint(hintResIds[type - 1]);
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
		SharedPreferences settings = PlaybackService.getSettings(this);
		// if (settings.getBoolean(PrefKeys.CONTROLS_IN_SELECTOR, false) !=
		// (mControls != null)) {
		// finish();
		// startActivity(new Intent(this, LibraryActivity.class));
		// }
		mDefaultAction = Integer.parseInt(settings.getString(PrefKeys.DEFAULT_ACTION_INT, "7"));
		mLastActedId = LibraryAdapter.INVALID_ID;
		updateHeaders();
		updateEqualizerVisibility();
	}

	private void updateEqualizerVisibility() {
		mEqualizerButton.setVisibility(Settings.ENABLE_EQUALIZER ? View.VISIBLE : View.GONE);
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

	/**
	 * If this intent looks like a launch from icon/widget/etc, perform launch
	 * actions.
	 */
	private void checkForLaunch(Intent intent) {
		SharedPreferences settings = PlaybackService.getSettings(this);
		if (settings.getBoolean(PrefKeys.PLAYBACK_ON_STARTUP, false) && Intent.ACTION_MAIN.equals(intent.getAction())) {
			startActivity(new Intent(this, FullPlaybackActivity.class));
		}
	}

	/**
	 * If the given intent has album data, set a limiter built from that data.
	 */
	private void loadAlbumIntent(Intent intent) {
		long albumId = intent.getLongExtra("albumId", -1);
		if (albumId != -1) {
			String[] fields = { intent.getStringExtra("artist"), intent.getStringExtra("album") };
			String data = String.format("album_id=%d", albumId);
			Limiter limiter = new Limiter(MediaUtils.TYPE_ALBUM, fields, data);
			int tab = mPagerAdapter.setLimiter(limiter);
			if (tab == -1 || tab == mViewPager.getCurrentItem())
				updateLimiterViews();
			else
				mViewPager.setCurrentItem(tab);

		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		if (intent == null)
			return;

		checkForLaunch(intent);
		loadAlbumIntent(intent);
	}

	@Override
	public void onRestoreInstanceState(Bundle in) {
			type = in.getInt(TYPE_FILE);
			id = in.getLong(ID_FILE);
			if (in.getBoolean(SEARCH_BOX_VISIBLE)) {
		 		setSearchBoxVisible(true);
			}
			if (SongArrayHolder.getInstance().isID3Opened() && mPagerAdapter.getCurrentType() == MediaUtils.TYPE_SONG) {
				createEditID3Dialog(type, id, null);
			}
			setSearchBoxVisible(true);
			super.onRestoreInstanceState(in);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (lastPage == 0) {
			SongArrayHolder.getInstance().saveStateAdapter(((LibraryPagerAdapter) mViewPager.getAdapter()).getSearchView());
		}
		outState.putInt(TYPE_FILE, type);
		outState.putLong(ID_FILE, id);
		outState.putBoolean(IS_FIRST_RUN, isFirstRun);
		super.onSaveInstanceState(outState);
	}

	public Activity getActivity() {
		return this;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			mTextFilter.setText("");
			//onBackPressed();
			
            try {
            	Advertisement.exit(getActivity());
            } catch(Exception e) {
            	
            }

			
			break;
		// case KeyEvent.KEYCODE_BACK:
		// if (mSearchBoxVisible) {
		// mTextFilter.setText("");
		// setSearchBoxVisible(false);
		// } else {
		// Limiter limiter = mPagerAdapter.getCurrentLimiter();
		// if (limiter != null && limiter.type != MediaUtils.TYPE_FILE) {
		// int pos = -1;
		// switch (limiter.type) {
		// case MediaUtils.TYPE_ALBUM:
		// setLimiter(MediaUtils.TYPE_ARTIST, limiter.data.toString());
		// pos = mPagerAdapter.mAlbumsPosition;
		// break;
		// case MediaUtils.TYPE_ARTIST:
		// mPagerAdapter.clearLimiter(MediaUtils.TYPE_ARTIST);
		// pos = mPagerAdapter.mArtistsPosition;
		// break;
		// case MediaUtils.TYPE_GENRE:
		// mPagerAdapter.clearLimiter(MediaUtils.TYPE_GENRE);
		// pos = mPagerAdapter.mGenresPosition;
		// break;
		// }
		// if (pos == -1) {
		// updateLimiterViews();
		// } else {
		// mViewPager.setCurrentItem(pos);
		// }
		// } else {
		// finish();
		// }
		// }
		// break;
		case KeyEvent.KEYCODE_SEARCH:
			setSearchBoxVisible(!mSearchBoxVisible);
			break;
		default:
			return false;
		}

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL)
			// On ICS, EditText reports backspace events as unhandled despite
			// actually handling them. To workaround, just assume the event was
			// handled if we get here.
			return true;

		if (super.onKeyDown(keyCode, event))
			return true;

		if (mTextFilter.onKeyDown(keyCode, event)) {
			if (!mSearchBoxVisible)
				setSearchBoxVisible(true);
			else
				mTextFilter.requestFocus();
			return true;
		}
		return false;
	}

	/**
	 * Update the first row of the lists with the appropriate action (play all
	 * or enqueue all).
	 */
	private void updateHeaders() {
		int action = mDefaultAction;
		if (action == ACTION_LAST_USED)
			action = mLastAction;
		boolean isEnqueue = action == ACTION_ENQUEUE || action == ACTION_ENQUEUE_ALL;
		String text = getString(isEnqueue ? R.string.enqueue_all : R.string.play_all);
		mPagerAdapter.setHeaderText(text);
	}

	/**
	 * Adds songs matching the data from the given intent to the song timelime.
	 * 
	 * @param intent
	 *            An intent created with {@link LibraryAdapter#createData(View)}
	 *            .
	 * @param action
	 *            One of LibraryActivity.ACTION_*
	 */
	private void pickSongs(Intent intent, int action) {
		long id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);

		boolean all = false;
		int mode = action;
		if (action == ACTION_PLAY_ALL || action == ACTION_ENQUEUE_ALL) {
			int type = mCurrentAdapter.getMediaType();
			boolean notPlayAllAdapter = type > MediaUtils.TYPE_SONG || id == LibraryAdapter.HEADER_ID;
			if (mode == ACTION_ENQUEUE_ALL && notPlayAllAdapter) {
				mode = ACTION_ENQUEUE;
			} else if (mode == ACTION_PLAY_ALL && notPlayAllAdapter) {
				mode = ACTION_PLAY;
			} else {
				all = true;
			}
		}

		QueryTask query = buildQueryFromIntent(intent, false, all);
		query.mode = modeForAction[mode];
		PlaybackService.get(this).addSongs(query);

		mLastActedId = id;

		if (mDefaultAction == ACTION_LAST_USED && mLastAction != action) {
			mLastAction = action;
			updateHeaders();
		}
	}

	/**
	 * "Expand" the view represented by the given intent by setting the limiter
	 * from the view and switching to the appropriate tab.
	 * 
	 * @param intent
	 *            An intent created with {@link LibraryAdapter#createData(View)}
	 *            .
	 */
	private void expand(Intent intent) {
		int type = intent.getIntExtra("type", MediaUtils.TYPE_INVALID);
		long id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);
		int tab = mPagerAdapter.setLimiter(mPagerAdapter.mAdapters[type].buildLimiter(id));
		if (tab == -1 || tab == mViewPager.getCurrentItem())
			updateLimiterViews();
		else
			mViewPager.setCurrentItem(tab);
	}

	/**
	 * Open the playback activity and close any activities above it in the
	 * stack.
	 */
	public void openPlaybackActivity() {
		startActivity(new Intent(this, FullPlaybackActivity.class));
	}

	/**
	 * Called by LibraryAdapters when a row has been clicked.
	 * 
	 * @param rowData
	 *            The data for the row that was clicked.
	 */
	public void onItemClicked(Intent rowData) {
		int action = mDefaultAction;
		if (action == ACTION_LAST_USED) {
			action = mLastAction;
		}

		if (action == ACTION_EXPAND && rowData.getBooleanExtra(LibraryAdapter.DATA_EXPANDABLE, false)) {
			onItemExpanded(rowData);
		} else if (rowData.getLongExtra(LibraryAdapter.DATA_ID, LibraryAdapter.INVALID_ID) == mLastActedId) {
			openPlaybackActivity();
		} else if (action != ACTION_DO_NOTHING) {
			if (action == ACTION_EXPAND) {
				// default to playing when trying to expand something that can't
				// be expanded
				action = ACTION_PLAY;
			} else if (action == ACTION_PLAY_OR_ENQUEUE) {
				// action = (mState & PlaybackService.FLAG_PLAYING) == 0 ?
				// ACTION_PLAY : ACTION_ENQUEUE;
				action = ACTION_PLAY;
			}
			pickSongs(rowData, action);
		}
	}

	/**
	 * Called by LibraryAdapters when a row's expand arrow has been clicked.
	 * 
	 * @param rowData
	 *            The data for the row that was clicked.
	 */
	public void onItemExpanded(Intent rowData) {
		int type = rowData.getIntExtra(LibraryAdapter.DATA_TYPE, MediaUtils.TYPE_INVALID);
		if (type == MediaUtils.TYPE_PLAYLIST)
			editPlaylist(rowData);
		else
			expand(rowData);
	}

	@Override
	public void afterTextChanged(Editable editable) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int before, int count) {
		mPagerAdapter.setFilter(text.toString());
	}

	/**
	 * Create or recreate the limiter breadcrumbs.
	 */
	public void updateLimiterViews() {
		mLimiterViews.removeAllViews();

		Limiter limiterData = mPagerAdapter.getCurrentLimiter();
		if (limiterData != null) {
			String[] limiter = limiterData.names;

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin = 5;
			for (int i = 0; i != limiter.length; ++i) {
				PaintDrawable background = new PaintDrawable(Color.GRAY);
				background.setCornerRadius(5);

				TextView view = new TextView(this);
				view.setSingleLine();
				view.setEllipsize(TextUtils.TruncateAt.MARQUEE);
				view.setText(limiter[i] + " | X");
				view.setTextColor(Color.WHITE);
				view.setBackgroundDrawable(background);
				view.setLayoutParams(params);
				view.setPadding(5, 2, 5, 2);
				view.setTag(i);
				view.setOnClickListener(this);
				mLimiterViews.addView(view);
			}

			mLimiterScroller.setVisibility(View.VISIBLE);
		} else {
			mLimiterScroller.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		if (view == mSortButton) {// mClearButton
			// if (mTextFilter.getText().length() == 0)
			// setSearchBoxVisible(false);
			// else
			// mTextFilter.setText("");
			openSortDialog();
		} else if (view == mEqualizerButton) {
			Intent intent = new Intent(this, MyEqualizer.class);
			startActivity(intent);
		} else if (view == mCover || view == mActionControls) {
			openPlaybackActivity();
		} else if (view == mEmptyQueue) {
			setState(PlaybackService.get(this).setFinishAction(SongTimeline.FINISH_RANDOM));
		} else if (view.getTag() != null) {
			// a limiter view was clicked
			int i = (Integer) view.getTag();

			Limiter limiter = mPagerAdapter.getCurrentLimiter();
			int type = limiter.type;
			if (i == 1 && type == MediaUtils.TYPE_ALBUM) {
				setLimiter(MediaUtils.TYPE_ARTIST, limiter.data.toString());
			} else if (i > 0) {
				Assert.assertEquals(MediaUtils.TYPE_FILE, limiter.type);
				File file = (File) limiter.data;
				int diff = limiter.names.length - i;
				while (--diff != -1) {
					file = file.getParentFile();
				}
				mPagerAdapter.setLimiter(FileSystemAdapter.buildLimiter(file));
			} else {
				mPagerAdapter.clearLimiter(type);
			}
			updateLimiterViews();
		} else {
			super.onClick(view);
		}
	}

	/**
	 * Set a new limiter of the given type built from the first
	 * MediaStore.Audio.Media row that matches the selection.
	 * 
	 * @param limiterType
	 *            The type of limiter to create. Must be either
	 *            MediaUtils.TYPE_ARTIST or MediaUtils.TYPE_ALBUM.
	 * @param selection
	 *            Selection to pass to the query.
	 */
	private void setLimiter(int limiterType, String selection) {
		ContentResolver resolver = getContentResolver();
		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] { MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM };
		Cursor cursor = resolver.query(uri, projection, selection, null, null);
		if (cursor != null) {
			if (cursor.moveToNext()) {
				String[] fields;
				String data;
				switch (limiterType) {
				case MediaUtils.TYPE_ARTIST:
					fields = new String[] { cursor.getString(2) };
					data = String.format("artist_id=%d", cursor.getLong(0));
					break;
				case MediaUtils.TYPE_ALBUM:
					fields = new String[] { cursor.getString(2), cursor.getString(3) };
					data = String.format("album_id=%d", cursor.getLong(1));
					break;
				default:
					throw new IllegalArgumentException("setLimiter() does not support limiter type " + limiterType);
				}
				mPagerAdapter.setLimiter(new Limiter(limiterType, fields, data));
			}
			cursor.close();
		}
	}

	/**
	 * Builds a media query based off the data stored in the given intent.
	 * 
	 * @param intent
	 *            An intent created with {@link LibraryAdapter#createData(View)}
	 *            .
	 * @param empty
	 *            If true, use the empty projection (only query id).
	 * @param all
	 *            If true query all songs in the adapter; otherwise query based
	 *            on the row selected.
	 */
	private QueryTask buildQueryFromIntent(Intent intent, boolean empty, boolean all) {
		int type = intent.getIntExtra("type", MediaUtils.TYPE_INVALID);

		String[] projection;
		if (type == MediaUtils.TYPE_PLAYLIST)
			projection = empty ? Song.EMPTY_PLAYLIST_PROJECTION : Song.FILLED_PLAYLIST_PROJECTION;
		else
			projection = empty ? Song.EMPTY_PROJECTION : Song.FILLED_PROJECTION;

		long id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);
		QueryTask query;
		if (type == MediaUtils.TYPE_FILE) {
			query = MediaUtils.buildFileQuery(intent.getStringExtra("file"), projection);
		} else if (all || id == LibraryAdapter.HEADER_ID) {
			query = ((MediaAdapter) mPagerAdapter.mAdapters[type]).buildSongQuery(projection);
			query.data = id;
		} else {
			query = MediaUtils.buildQuery(type, id, projection, null);
		}

		return query;
	}

	private static final int MENU_PLAY = 0;
	private static final int MENU_ENQUEUE = 1;
	private static final int MENU_EXPAND = 2;
	private static final int MENU_ADD_TO_PLAYLIST = 3;
	private static final int MENU_NEW_PLAYLIST = 4;
	private static final int MENU_DELETE = 5;
	private static final int MENU_RENAME_PLAYLIST = 7;
	private static final int MENU_SELECT_PLAYLIST = 8;
	private static final int MENU_PLAY_ALL = 9;
	private static final int MENU_ENQUEUE_ALL = 10;
	private static final int MENU_MORE_FROM_ALBUM = 11;
	private static final int MENU_MORE_FROM_ARTIST = 12;
	private static final int MENU_EDIT_MP3_TAGS = 13;
	private static final int MENU_REMOVE_ALBUM_COVER = 14;

	/**
	 * Creates a context menu for an adapter row.
	 * 
	 * @param menu
	 *            The menu to create.
	 * @param rowData
	 *            Data for the adapter row.
	 */
	public void onCreateContextMenu(ContextMenu menu, Intent rowData) {
		if (rowData.getLongExtra(LibraryAdapter.DATA_ID, LibraryAdapter.INVALID_ID) == LibraryAdapter.HEADER_ID) {
			menu.setHeaderTitle(getString(R.string.all_songs));
			menu.add(0, MENU_PLAY_ALL, 0, R.string.play_all).setIntent(rowData);
			menu.add(0, MENU_ENQUEUE_ALL, 0, R.string.enqueue_all).setIntent(rowData);
			menu.addSubMenu(0, MENU_ADD_TO_PLAYLIST, 0, R.string.add_to_playlist).getItem().setIntent(rowData);
		} else {
			int type = rowData.getIntExtra(LibraryAdapter.DATA_TYPE, MediaUtils.TYPE_INVALID);
			boolean isAllAdapter = type <= MediaUtils.TYPE_SONG;

			menu.setHeaderTitle(rowData.getStringExtra(LibraryAdapter.DATA_TITLE));
			menu.add(0, MENU_PLAY, 0, R.string.play).setIntent(rowData);
			if (isAllAdapter)
				menu.add(0, MENU_PLAY_ALL, 0, R.string.play_all).setIntent(rowData);
			menu.add(0, MENU_ENQUEUE, 0, R.string.enqueue).setIntent(rowData);
			if (isAllAdapter)
				menu.add(0, MENU_ENQUEUE_ALL, 0, R.string.enqueue_all).setIntent(rowData);
			if (type == MediaUtils.TYPE_PLAYLIST) {
				menu.add(0, MENU_RENAME_PLAYLIST, 0, R.string.rename).setIntent(rowData);
				menu.add(0, MENU_EXPAND, 0, R.string.edit).setIntent(rowData);
			} else if (rowData.getBooleanExtra(LibraryAdapter.DATA_EXPANDABLE, false)) {
				menu.add(0, MENU_EXPAND, 0, R.string.expand).setIntent(rowData);
			}
			if (type == MediaUtils.TYPE_ALBUM || type == MediaUtils.TYPE_SONG)
				menu.add(0, MENU_MORE_FROM_ARTIST, 0, R.string.more_from_artist).setIntent(rowData);
			if (type == MediaUtils.TYPE_SONG) {
				menu.add(0, MENU_MORE_FROM_ALBUM, 0, R.string.more_from_album).setIntent(rowData);
				menu.add(0, MENU_EDIT_MP3_TAGS, 0, R.string.edit_mp3).setIntent(rowData);
				menu.add(0, MENU_REMOVE_ALBUM_COVER, 0, R.string.remove_album_cover).setIntent(rowData);
			}
			menu.addSubMenu(0, MENU_ADD_TO_PLAYLIST, 0, R.string.add_to_playlist).getItem().setIntent(rowData);
			menu.add(0, MENU_DELETE, 0, R.string.delete).setIntent(rowData);
		}
	}

	/**
	 * Add a set of songs represented by the intent to a playlist. Displays a
	 * Toast notifying of success.
	 * 
	 * @param playlistId
	 *            The id of the playlist to add to.
	 * @param intent
	 *            An intent created with {@link LibraryAdapter#createData(View)}
	 *            .
	 */
	private void addToPlaylist(long playlistId, Intent intent) {
		QueryTask query = buildQueryFromIntent(intent, true, false);
		int count = Playlist.addToPlaylist(getContentResolver(), playlistId, query);

		String message = getResources().getQuantityString(R.plurals.added_to_playlist, count, count, intent.getStringExtra("playlistName"));
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Open the playlist editor for the playlist with the given id.
	 */
	private void editPlaylist(Intent rowData) {
		Intent launch = new Intent(this, PlaylistActivity.class);
		launch.putExtra("playlist", rowData.getLongExtra(LibraryAdapter.DATA_ID, LibraryAdapter.INVALID_ID));
		launch.putExtra("title", rowData.getStringExtra(LibraryAdapter.DATA_TITLE));
		startActivity(launch);
	}

	/**
	 * Delete the media represented by the given intent and show a Toast
	 * informing the user of this.
	 * 
	 * @param intent
	 *            An intent created with {@link LibraryAdapter#createData(View)}
	 *            .
	 */
	private void delete(Intent intent) {
		int type = intent.getIntExtra("type", MediaUtils.TYPE_INVALID);
		long id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);
		String message = null;
		Resources res = getResources();

		if (type == MediaUtils.TYPE_FILE) {
			String file = intent.getStringExtra("file");
			boolean success = MediaUtils.deleteFile(new File(file));
			if (!success) {
				message = res.getString(R.string.delete_file_failed, file);
			}
		} else if (type == MediaUtils.TYPE_PLAYLIST) {
			Playlist.deletePlaylist(getContentResolver(), id);
		} else {
			try {
				int count = PlaybackService.get(this).deleteMedia(type, id);
				message = res.getQuantityString(R.plurals.deleted, count, count);
			} catch (UnknownFormatConversionException ex) {
				Log.d(getClass().getSimpleName(), ex.getConversion() + "\n" + ex.getMessage());
			}
		}

		if (message == null) {
			message = res.getString(R.string.deleted, intent.getStringExtra("title"));
		}

		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() != 0)
			return super.onContextItemSelected(item);

		Intent intent = item.getIntent();

		switch (item.getItemId()) {
		case MENU_EXPAND:
			onItemExpanded(intent);
			break;
		case MENU_ENQUEUE:
			pickSongs(intent, ACTION_ENQUEUE);
			break;
		case MENU_PLAY:
			pickSongs(intent, ACTION_PLAY);
			break;
		case MENU_PLAY_ALL:
			pickSongs(intent, ACTION_PLAY_ALL);
			break;
		case MENU_ENQUEUE_ALL:
			pickSongs(intent, ACTION_ENQUEUE_ALL);
			break;
		case MENU_NEW_PLAYLIST: {
			NewPlaylistDialog dialog = new NewPlaylistDialog(this, null, R.string.create, intent);
			dialog.setDismissMessage(mHandler.obtainMessage(MSG_NEW_PLAYLIST, dialog));
			dialog.show();
			break;
		}
		case MENU_RENAME_PLAYLIST: {
			NewPlaylistDialog dialog = new NewPlaylistDialog(this, intent.getStringExtra("title"), R.string.rename, intent);
			dialog.setDismissMessage(mHandler.obtainMessage(MSG_RENAME_PLAYLIST, dialog));
			dialog.show();
			break;
		}
		case MENU_DELETE:
			mHandler.sendMessage(mHandler.obtainMessage(MSG_DELETE, intent));
			break;
		case MENU_ADD_TO_PLAYLIST: {
			SubMenu playlistMenu = item.getSubMenu();
			playlistMenu.add(0, MENU_NEW_PLAYLIST, 0, R.string.new_playlist).setIntent(intent);
			Cursor cursor = Playlist.queryPlaylists(getContentResolver());
			if (cursor != null) {
				for (int i = 0, count = cursor.getCount(); i != count; ++i) {
					cursor.moveToPosition(i);
					long id = cursor.getLong(0);
					String name = cursor.getString(1);
					Intent copy = new Intent(intent);
					copy.putExtra("playlist", id);
					copy.putExtra("playlistName", name);
					playlistMenu.add(0, MENU_SELECT_PLAYLIST, 0, name).setIntent(copy);
				}
				cursor.close();
			}
			break;
		}
		case MENU_SELECT_PLAYLIST:
			mHandler.sendMessage(mHandler.obtainMessage(MSG_ADD_TO_PLAYLIST, intent));
			break;
		case MENU_MORE_FROM_ARTIST: {
			String selection;
			if (intent.getIntExtra(LibraryAdapter.DATA_TYPE, -1) == MediaUtils.TYPE_ALBUM) {
				selection = "album_id=";
			} else {
				selection = "_id=";
			}
			selection += intent.getLongExtra(LibraryAdapter.DATA_ID, LibraryAdapter.INVALID_ID);
			setLimiter(MediaUtils.TYPE_ARTIST, selection);
			updateLimiterViews();
			break;
		}
		case MENU_MORE_FROM_ALBUM:
			setLimiter(MediaUtils.TYPE_ALBUM, "_id=" + intent.getLongExtra(LibraryAdapter.DATA_ID, LibraryAdapter.INVALID_ID));
			updateLimiterViews();
			break;
		case MENU_EDIT_MP3_TAGS:
			boolean isWhiteTheme = Util.getThemeName(this).equals(Util.WHITE_THEME);
			type = intent.getIntExtra("type", MediaUtils.TYPE_INVALID);
			id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);
			editor = new MP3Editor(this, isWhiteTheme);
			createEditID3Dialog(type, id, editor);
			break;
		case MENU_REMOVE_ALBUM_COVER:
			type = intent.getIntExtra("type", MediaUtils.TYPE_INVALID);
			id = intent.getLongExtra("id", LibraryAdapter.INVALID_ID);
			final File fileRAC = PlaybackService.get(this).getFilePath(type, id);
			AlertDialog.Builder builderRAC = new AlertDialog.Builder(this);
			builderRAC.setTitle(R.string.remove_album_cover_title);
			builderRAC.setMessage(R.string.remove_album_cover_complete);
			builderRAC.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					new AsyncTask<Void, Void, Void>() {

						@Override
						protected Void doInBackground(Void... params) {
							deleteCover(fileRAC);
							return null;
						}

					}.execute();

				}

			});
			builderRAC.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}

			});
			AlertDialog alertDialogRAC = builderRAC.create();
			alertDialogRAC.show();
			break;
		}

		return true;
	}

	@SuppressLint("NewApi") 
	private void createEditID3Dialog(int type, long id, MP3Editor view) {
		final File file = PlaybackService.get(this).getFilePath(type, id);
		if (null == view) {
			boolean isWhiteTheme = Util.getThemeName(this).equals(Util.WHITE_THEME);
			editor = new MP3Editor(this, isWhiteTheme);
		}
		String[] filds = { "", "", "" };
		MusicMetadata metadata = null;
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (null != src_set) {
				metadata = (MusicMetadata) src_set.getSimplified();
			}
			if (null != metadata) {
				filds[0] = metadata.getArtist();
				filds[1] = metadata.getSongTitle();
				filds[2] = metadata.getAlbum() == null ? "" : metadata.getAlbum();
			}
			editor.setStrings(filds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final SongArrayHolder holder = SongArrayHolder.getInstance();
		if (holder.isID3Opened()) {
			editor.setStrings(holder.getID3Fields());
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(editor.getView());
		editor.hideCheckBox(true);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				holder.setID3DialogOpened(false, null);
				final String artistName = editor.getNewArtistName();
				final String albumTitle = editor.getNewAlbumTitle();
				final String songTitle = editor.getNewSongTitle();
				if (!editor.manipulateText()) {
					return;
				}
				if(new File(file.getParentFile() + "/" + artistName + " - " + albumTitle + ".mp3").exists()) {
					Toast toast = Toast.makeText(editor.getView().getContext(), "File with this name is already exists", Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						rename(file, artistName, albumTitle, songTitle);
						return null;
					}
					
					@Override
					protected void onPostExecute(Void result) {
						((LibraryPagerAdapter) mViewPager.getAdapter()).notifySongAdapter();
					};

				}.execute();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				holder.setID3DialogOpened(false, null);
			}

		});
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			builder.setOnDismissListener(new OnDismissListener() {
				 
				@Override
				public void onDismiss(DialogInterface dialog) {
					holder.setID3DialogOpened(false, null);
				}
			});
		} else {
			builder.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						SongArrayHolder.getInstance().setID3DialogOpened(false, null);
					}
					return false;
				}
			});
		}
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		holder.setID3DialogOpened(true, editor.getStrings());
	}

	private void deleteCover(File f) {
		File file = new File(f.getParentFile(), f.getName());
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set == null) {
				return;
			}
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			metadata.clearPictureList();
			Bitmap cover = BitmapFactory.decodeResource(getResources(), R.drawable.fallback_cover);
			ByteArrayOutputStream out = new ByteArrayOutputStream(80000);
			cover.compress(CompressFormat.JPEG, 85, out);
			metadata.addPicture(new ImageData(out.toByteArray(), "image/jpeg", "cover", 3));
			new MyID3().update(file, src_set, metadata);
			notifyMediascanner(file, null, null);
		} catch (Exception e) {
		}
	}

	private void rename(File f, String artist, String album, String song) {
		File file = new File(f.getParentFile(), f.getName());
		boolean isChange = false;
		try {
			MusicMetadataSet src_set = new MyID3().read(file);
			if (src_set == null) {
				return;
			}
			MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
			if (!album.equals("")) {
				isChange = true;
				metadata.setAlbum(album);
			}
			if (!song.equals("")) {	
				isChange = true;
				metadata.setSongTitle(song);
			}
			if (!artist.equals("")) {
				isChange = true;
				metadata.setArtist(artist);
			}
			if (!isChange) {
				return;
			}
			File newFile = new File(file.getParentFile() + "/" + artist + " - " + album + ".mp3"); 
			file.renameTo(newFile);
			new MyID3().update(newFile, src_set, metadata);
			notifyMediascanner(newFile, artist, song);
		} catch (Exception e) {
		}
	}

	private void notifyMediascanner(File file, final String artist, final String title) {
		MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null, new MediaScannerConnection.OnScanCompletedListener() {

			public void onScanCompleted(String path, Uri uri) {
				Song song = new Song(id);
				if (null != artist || artist.equals("")) {
					song.artist = artist;
				}
				if (null != title || title.equals("")) {
					song.title = title;
				}
				onMediaChange();
				onSongChange(song);
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// if ("AppTheme.Black".equals(Util.getThemeName(this))) {
		// setMenuBackgroundBlack();
		// }
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			MenuItem controls = menu.add(null);
			CompatHoneycomb.setActionView(controls, mActionControls);
			CompatHoneycomb.setShowAsAction(controls, MenuItem.SHOW_AS_ACTION_ALWAYS);
			// removed
			// MenuItem search = menu.add(0, MENU_SEARCH, 0,
			// R.string.search).setIcon(R.drawable.ic_menu_search);
			// CompatHoneycomb.setShowAsAction(search,
			// MenuItem.SHOW_AS_ACTION_IF_ROOM);
		} else {
			// removed
			// menu.add(0, MENU_SEARCH, 0,
			// R.string.search).setIcon(R.drawable.ic_menu_search);
			menu.add(0, MENU_PLAYBACK, 0, R.string.playback_view).setIcon(R.drawable.ic_menu_gallery);
		}
		menu.add(0, MENU_SORT, 0, R.string.sort_by).setIcon(R.drawable.ic_menu_sort_alphabetically);
		return super.onCreateOptionsMenu(menu);
	}

	// protected void setMenuBackgroundBlack() {
	// Log.d("log", "setMenuBackgroundBlack");
	// getLayoutInflater().setFactory(new Factory() {
	// public View onCreateView(String name, Context context,
	// AttributeSet attrs) {
	// try {
	// LayoutInflater f = getLayoutInflater();
	// final View view = f.createView(name, null, attrs);
	// new Handler().post(new Runnable() {
	// public void run() {
	// view.setBackgroundResource(R.color.window_background_black);
	// }
	// });
	// return view;
	// } catch (InflateException e) {
	// } catch (ClassNotFoundException e) {
	// }
	// return null;
	// }
	// });
	// }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		LibraryAdapter adapter = mCurrentAdapter;
		menu.findItem(MENU_SORT).setEnabled(adapter != null);
		return super.onPrepareOptionsMenu(menu);
	}

	// removed
	// public boolean isSearchBoxVisible() {
	// return mSearchBoxVisible;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// removed
		// case MENU_SEARCH:
		// int position = mPagerAdapter.getCurrentType();
		// if (position == -1) {
		// position = page;
		// }
		// if (position != 0) {
		// setSearchBoxVisible(!mSearchBoxVisible);
		// }
		// return true;
		case MENU_PLAYBACK:
			openPlaybackActivity();
			return true;
		case MENU_SORT: {
			openSortDialog();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openSortDialog() {
		SortAdapter adapter = (SortAdapter) mCurrentAdapter;
		int mode = adapter.getSortMode();
		int[] itemIds = adapter.getSortEntries();

		int check;
		if (mode < 0) {
			check = R.id.descending;
			mode = ~mode;
		} else {
			check = R.id.ascending;
		}

		String[] items = new String[itemIds.length];
		Resources res = getResources();
		for (int i = itemIds.length; --i != -1;) {
			items[i] = res.getString(itemIds[i]);
		}

		RadioGroup header = (RadioGroup) getLayoutInflater().inflate(R.layout.sort_dialog, null);
		header.check(check);

		AlertDialog.Builder builder;

		if ("AppTheme.White".equals(Util.getThemeName(this))) {
			builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Dialog_White));
		} else {
			builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Dialog));
		}
		int position = mPagerAdapter.getCurrentType();
		if (position == -1) {
			position = page;
		}
		builder.setTitle(getResources().getString(R.string.sort_by) + " \"" + mPagerAdapter.getPageTitle(position) + "\"");
		builder.setSingleChoiceItems(items, mode + 1, this); // add 1 for header
		builder.setNeutralButton(R.string.done, null);

		AlertDialog dialog = builder.create();
		dialog.getListView().addHeaderView(header);
		dialog.setOnDismissListener(this);
		dialog.show();
	}

	/**
	 * Call addToPlaylist with the results from a NewPlaylistDialog stored in
	 * obj.
	 */
	private static final int MSG_NEW_PLAYLIST = 11;
	/**
	 * Delete the songs represented by the intent stored in obj.
	 */
	private static final int MSG_DELETE = 12;
	/**
	 * Call renamePlaylist with the results from a NewPlaylistDialog stored in
	 * obj.
	 */
	private static final int MSG_RENAME_PLAYLIST = 13;
	/**
	 * Call addToPlaylist with data from the intent in obj.
	 */
	private static final int MSG_ADD_TO_PLAYLIST = 15;
	/**
	 * Save the current page, passed in arg1, to SharedPreferences.
	 */
	private static final int MSG_SAVE_PAGE = 16;

	@Override
	public boolean handleMessage(Message message) {
		switch (message.what) {
		case MSG_ADD_TO_PLAYLIST: {
			Intent intent = (Intent) message.obj;
			addToPlaylist(intent.getLongExtra("playlist", -1), intent);
			break;
		}
		case MSG_NEW_PLAYLIST: {
			NewPlaylistDialog dialog = (NewPlaylistDialog) message.obj;
			if (dialog.isAccepted()) {
				String name = dialog.getText();
				long playlistId = Playlist.createPlaylist(getContentResolver(), name);
				Intent intent = dialog.getIntent();
				intent.putExtra("playlistName", name);
				addToPlaylist(playlistId, intent);
			}
			break;
		}
		case MSG_DELETE:
			delete((Intent) message.obj);
			break;
		case MSG_RENAME_PLAYLIST: {
			NewPlaylistDialog dialog = (NewPlaylistDialog) message.obj;
			if (dialog.isAccepted()) {
				long playlistId = dialog.getIntent().getLongExtra("id", -1);
				Playlist.renamePlaylist(getContentResolver(), playlistId, dialog.getText());
			}
			break;
		}
		case MSG_SAVE_PAGE: {
			SharedPreferences.Editor editor = PlaybackService.getSettings(this).edit();
			editor.putInt("library_page", message.arg1);
			editor.commit();
			break;
		}
		default:
			return super.handleMessage(message);
		}

		return true;
	}

	@Override
	public void onMediaChange() {
		mPagerAdapter.invalidateData();
	}

	protected void setSearchBoxVisible(boolean visible) {
		mSearchBoxVisible = visible;
		mSearchBox.setVisibility(visible ? View.VISIBLE : View.GONE);
		// if (mControls != null) {
		// mControls.setVisibility(visible || (mState &
		// PlaybackService.FLAG_NO_MEDIA) != 0 ? View.GONE : View.VISIBLE);
		// } else if (mActionControls != null) {
		// // try to hide the bottom action bar
		// ViewParent parent = mActionControls.getParent();
		// if (parent != null)
		// parent = parent.getParent();
		// if (parent != null && parent instanceof ViewGroup) {
		// ViewGroup ab = (ViewGroup)parent;
		// if (ab.getChildCount() == 1) {
		// ab.setVisibility(visible ? View.GONE : View.VISIBLE);
		// }
		// }
		// }

		// removed
		// if (visible) {
		// mTextFilter.requestFocus();
		// ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(mTextFilter,
		// 0);
		// }
	}

	@Override
	protected void onStateChange(int state, int toggled) {
		super.onStateChange(state, toggled);

		if ((toggled & PlaybackService.FLAG_NO_MEDIA) != 0) {
			// update visibility of controls
			setSearchBoxVisible(mSearchBoxVisible);
		}
		if ((toggled & PlaybackService.FLAG_EMPTY_QUEUE) != 0 && mEmptyQueue != null) {
			mEmptyQueue.setVisibility((state & PlaybackService.FLAG_EMPTY_QUEUE) == 0 ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	protected void onSongChange(Song song) {
		super.onSongChange(song);

		if (mTitle != null) {
			Bitmap cover = null;

			if (song == null) {
				if (mActionControls == null) {
					mTitle.setText(R.string.none);
					mArtist.setText(null);
					// mAlbum.setText(null);
				} else {
					mTitle.setText(null);
					mArtist.setText(null);
					// mAlbum.setText(null);
					mCover.setImageDrawable(null);
					return;
				}
			} else {
				Resources res = getResources();
				String title = song.title == null ? res.getString(R.string.unknown) : song.title;
				String artist = song.artist == null ? res.getString(R.string.unknown) : song.artist;
				// String album = song.album == null?
				// res.getString(R.string.unknown): song.album;
				mTitle.setText(title);
				mArtist.setText(artist);
				//mTitle.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move_text));
				//mArtist.startAnimation(AnimationUtils.loadAnimation(this, R.anim.move_text));
				if (song.path == null) return;
				File file = new File(song.path);
				try {
					MusicMetadataSet src_set = new MyID3().read(file);
					MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
					// mAlbum.setText(album);
					cover = ru.johnlife.lifetoolsmp3.Util.getArtworkImage(2, metadata);
				} catch (Exception exception) {
					cover = song.getCover(this);
				}
			}

			if (Song.mDisableCoverArt)
				mCover.setVisibility(View.GONE);
			else if (cover == null)
				mCover.setImageResource(R.drawable.fallback_cover);
			else
				mCover.setImageBitmap(cover);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		ListView list = ((AlertDialog) dialog).getListView();
		// subtract 1 for header
		int which = list.getCheckedItemPosition() - 1;

		RadioGroup group = (RadioGroup) list.findViewById(R.id.sort_direction);
		if (group.getCheckedRadioButtonId() == R.id.descending)
			which = ~which;

		mPagerAdapter.setSortMode(which);
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
		mCurrentAdapter = adapter;
		mLastActedId = LibraryAdapter.INVALID_ID;
		updateLimiterViews();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			CompatHoneycomb.selectTab(this, position);
		}
		if (adapter != null && adapter.getLimiter() == null) {
			// Save current page so it is opened on next startup. Don't save if
			// the page was expanded to, as the expanded page isn't the starting
			// point.
			Handler handler = mHandler;
			handler.sendMessage(mHandler.obtainMessage(MSG_SAVE_PAGE, position, 0));
		}
		if (lastPage != position && lastPage != -1) {
			mTextFilter.setText("");
		}
		if (lastPage == 0) {
			SongArrayHolder.getInstance().saveStateAdapter(((LibraryPagerAdapter) mViewPager.getAdapter()).getSearchView());
		}
		lastPage = position;
		if (position == 0) {
			((LibraryPagerAdapter) mViewPager.getAdapter()).getSearchView().notifyAdapter();
		}
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
}
