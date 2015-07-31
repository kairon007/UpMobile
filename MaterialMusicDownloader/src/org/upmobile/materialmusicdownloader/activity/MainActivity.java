package org.upmobile.materialmusicdownloader.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.upmobile.materialmusicdownloader.BaseDownloadListener;
import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;
import org.upmobile.materialmusicdownloader.font.MusicTextView;
import org.upmobile.materialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.materialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.materialmusicdownloader.fragment.NavigationDrawerFragment;
import org.upmobile.materialmusicdownloader.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import org.upmobile.materialmusicdownloader.fragment.NavigationDrawerFragment.OnNavigationDrawerState;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.materialmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.materialmusicdownloader.fragment.SearchFragment;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import java.io.File;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoBar;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class MainActivity extends BaseMiniPlayerActivity implements FolderSelectCallback, NavigationDrawerCallbacks, Constants {

	private int currentFragmentId = -1;
	private CharSequence mTitle;
	private String query = null;
	private SearchView searchView;
	private NavigationDrawerFragment navigationDrawerFragment;
	private boolean isVisibleSearchView = false;
	protected boolean isEnabledFilter = false;
	protected boolean hadClosedDraver = false;
	private int currentPosition = -1;
	private Bitmap defaultCover;

	private FileObserver fileObserver = new FileObserver(MaterialMusicDownloaderApp.getDirectory()) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
				File file = new File(MaterialMusicDownloaderApp.getDirectory());
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + MaterialMusicDownloaderApp.getDirectory() + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_main);
		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		navigationDrawerFragment.setDrawerState(new OnNavigationDrawerState() {

			@Override
			public void onDrawerOpen() {
				navigationDrawerOpened();
			}
		});
		File file = new File(MaterialMusicDownloaderApp.getDirectory());
		if (!file.exists())
			file.mkdirs();
		fileObserver.startWatching();
		// Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}

	private void configCurrentId (BaseMaterialFragment baseMaterialFragment) {
		if (baseMaterialFragment.getClass() == SearchFragment.class) {
			currentFragmentId = SEARCH_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == DownloadsFragment.class){
			currentFragmentId = DOWNLOADS_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == LibraryFragment.class){
			currentFragmentId = LIBRARY_FRAGMENT;
		}else if (baseMaterialFragment.getClass() == PlaylistFragment.class){
			currentFragmentId = PLAYLIST_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == PlayerFragment.class){
			currentFragmentId = PLAYER_FRAGMENT;
		} else {
			currentFragmentId = SEARCH_FRAGMENT;
		}
	}

	public void changeFragment(BaseMaterialFragment baseMaterialFragment, boolean isAnimate) {
		changeFragment(baseMaterialFragment, isAnimate, null);
	}

	public void changeFragment(BaseMaterialFragment baseMaterialFragment, boolean isAnimate, AbstractSong song) {
		configCurrentId(baseMaterialFragment);
		if (null != searchView) {
			Util.hideKeyboard(this, searchView);
		}
		isEnabledFilter = false;
		if (null != searchView) {
			searchView.setIconified(true);
			searchView.setIconified(true);
		}
		if (((Fragment) baseMaterialFragment).isAdded()) {
			((Fragment) baseMaterialFragment).onResume();
		}
		if (baseMaterialFragment.getClass() == LibraryFragment.class) {
			Bundle b = new Bundle();
			b.putParcelable("KEY_SELECTED_SONG", song);
			((Fragment) baseMaterialFragment).setArguments(b);
		}
		FragmentTransaction tr = getFragmentManager().beginTransaction();
		if (isAnimate && isAnimationEnabled()) {
			tr.setCustomAnimations(R.anim.fragment_slide_in_up, R.anim.fragment_slide_out_up, R.anim.fragment_slide_in_down, R.anim.fragment_slide_out_down);
		}
		tr.replace(R.id.content_frame, (Fragment) baseMaterialFragment, baseMaterialFragment.getClass().getSimpleName()).addToBackStack(baseMaterialFragment.getClass().getSimpleName()).commit();
	}

	protected String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position) return SearchFragment.class.getSimpleName();
		FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
		return backEntry.getName();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_search).setVisible(isVisibleSearchView);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view = findViewById(R.id.drawer_layout);
		Util.hideKeyboard(this, view);
		int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			searchView.setIconified(false);// to Expand the SearchView when clicked
			return true;
		}
		return false;
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		if (position == currentPosition && (position != SETTINGS_FRAGMENT && position != 6)) {
			return;
		}
		if (position == PLAYER_FRAGMENT) {
			showMiniPlayer(false);
		} else if (position <= LIBRARY_FRAGMENT) {
			showMiniPlayer(null != service ? service.isEnqueueToStream() : false);
		}
		getSupportActionBar().setElevation(position == SEARCH_FRAGMENT ? 0 : position != SETTINGS_FRAGMENT && position != 6 ? 16 : 0);
		currentFragmentIsPlayer = false;
		switch (position) {
		case SEARCH_FRAGMENT:
			changeFragment(new SearchFragment(), false);
			break;
		case DOWNLOADS_FRAGMENT:
			changeFragment(new DownloadsFragment(), false);
			break;
		case PLAYLIST_FRAGMENT:
			changeFragment(new PlaylistFragment(), false);
			break;
		case LIBRARY_FRAGMENT:
			changeFragment(new LibraryFragment(), false);
			break;
		case PLAYER_FRAGMENT:
			android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
			String lastFragmentName = backEntry.getName();
			currentFragmentIsPlayer = true;
			BaseMaterialFragment fragment = new PlayerFragment();
			if (!lastFragmentName.equals(fragment.getClass().getSimpleName())) {
				changeFragment(fragment, true);
			}
			break;
		case SETTINGS_FRAGMENT:
		case 6:
			showDialog();
			break;
		default:
			break;
		}
		currentPosition = position;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		View v = findViewById(android.R.id.home);
		if (null != v) {
			if (useOldToggle()) {
				((View) v.getParent().getParent()).setPadding(32, 0, 0, 0);
			}
		}
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) searchItem.getActionView();
		searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
		searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				return false;
			}
		});
		((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (PLAYLIST_FRAGMENT == currentPosition) {
					PlaylistFragment playlist = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
					playlist.forceDelete();
				}
				if (LIBRARY_FRAGMENT == currentPosition) {
					LibraryFragment library = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
					library.forceDelete();
				}
			}
		});
		searchView.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (navigationDrawerFragment.isDrawerOpen()) {
					navigationDrawerFragment.closeDrawer();
				}
				if (PLAYLIST_FRAGMENT == currentPosition) {
					PlaylistFragment playlist = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
					playlist.forceDelete();
				}
				if (LIBRARY_FRAGMENT == currentPosition) {
					LibraryFragment library = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
					library.forceDelete();
				}
			}
		});
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String q) {
				searchView.clearFocus();
				Util.hideKeyboard(MainActivity.this, searchView);
				onQueryTextSubmitAct(q);
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				onQueryTextChangeAct(newText);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	public void closeSearchView(boolean close) {
		if(null == searchView) return;
		searchView.setIconified(close);
	}

	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
	}

	@Override
	protected void checkOnStart(boolean showMiniPlayer) {
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		super.checkOnStart(null == player || !player.isVisible());
	}

	@Override
	public void onBackPressed() {
		setVisibleSearchView(getPreviousFragmentName(2));
		if (getPreviousFragmentName(2).equals(SearchFragment.class.getSimpleName())) {
			getSupportActionBar().setElevation(0);
		}
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (navigationDrawerFragment.isVisible()) {
			navigationDrawerFragment.closeDrawer();
		} else if (null != player && player.isVisible()) {
			showMiniPlayer(service.isEnqueueToStream());
			getFragmentManager().popBackStack();
			setPlayerFragmentVisible(false);
		} else {
			if (LIBRARY_FRAGMENT == getCurrentFragmentId() && !navigationDrawerFragment.isDrawerIndicatorEnabled()) {
				showMiniPlayer(false);
				getFragmentManager().popBackStack();
			} else if (PLAYER_FRAGMENT == getCurrentFragmentId()) {
				service.stopPressed();
				stopService(new Intent(this, PlaybackService.class));
				finish();
			} else if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
			} else {
				stopService(new Intent(this, PlaybackService.class));
				finish();
			}
		}
	}

	public void showPlayerElement(boolean flag) {
		addPlayerElement(flag);
	}

	public void showMessage(String message) {
		UndoBar undoBar = new UndoBar(this);
		undoBar.clear();
		undoBar.message(message);
		undoBar.style(UndoBarController.MESSAGESTYLE);
		undoBar.show(false);
	}

	public void showMessage(int message) {
		showMessage(getString(message));
	}

	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image) {
		return property < 0 ? null : getDefaultBitmapCover(outWidth, outHeight, property, image, 0, 0, 0);
	}

	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image, int customColor, int pL, int pT) {
		MusicTextView textCover = new MusicTextView(this);
		textCover.setText(image);
		textCover.setTextColor(getResources().getColor(customColor == 0 ? R.color.main_color_500 : customColor));
		Rect bounds = new Rect();
		Paint textPaint = textCover.getPaint();
		textPaint.getTextBounds(image, 0, image.length(), bounds);
		int height = bounds.height();
		int width = bounds.width();
		boolean defaultIsLarger = true;
		while (height < property && width < property) {
			defaultIsLarger = false;
			textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, textCover.getTextSize()) + 1f);
			bounds = new Rect();
			textPaint = textCover.getPaint();
			textPaint.getTextBounds(image, 0, image.length(), bounds);
			height = bounds.height();
			width = bounds.width();
		}
		if (defaultIsLarger) {
			while (height > property && width > property) {
				textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, textCover.getTextSize()) - 1f);
				bounds = new Rect();
				textPaint = textCover.getPaint();
				textPaint.getTextBounds(image, 0, image.length(), bounds);
				height = bounds.height();
				width = bounds.width();
			}
		}
		return Util.textViewToBitmap(textCover, outWidth, outHeight);
	}

	@Override
	public void setCover(Bitmap bmp) {
		if (null == defaultCover) {
			String cover = getString(R.string.font_musics);
			defaultCover = getDefaultBitmapCover(Util.dpToPx(this,64), Util.dpToPx(this,62), Util.dpToPx(this,60), cover);
		}
		if (null ==  bmp) {
			bmp = defaultCover;
		}
		service.updatePictureNotification(bmp);
		((ImageView) findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}

	@Override
	protected void setPlayPauseMini(boolean playPayse) {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 48), Util.dpToPx(this, 48), Util.dpToPx(this, 44), playPayse ? getString(R.string.font_pause_mini) : getString(R.string.font_play_mini));
		((ImageView) findViewById(R.id.mini_player_play_pause)).setImageBitmap(bmp);
	}

	@Override
	protected void setImageDownloadButton() {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 48), Util.dpToPx(this, 48), Util.dpToPx(this, 44), getString(R.string.font_download_button));
		((ImageView) findViewById(R.id.mini_player_download)).setImageBitmap(bmp);
	}

	public void setupDownloadBtn() { // in PlayerFragment
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (null != player && player.isVisible()) {
			((PlayerFragment) player).setupDownloadButton();
		}
	}

	@Override
	public void onFolderSelection(final File folder) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putString(PREF_DIRECTORY, folder.getAbsolutePath());
		editor.putString(PREF_DIRECTORY_PREFIX, File.separator + folder.getAbsoluteFile().getName() + File.separator);
		editor.commit();
		showPlayerElement(PlaybackService.get(this).isPlaying());
		new Thread(new Runnable() {

			@Override
			public void run() {
				StateKeeper.getInstance().notifyLable(false);
				StateKeeper.getInstance().initSongHolder(folder.getAbsolutePath());
				checkDownloadingUrl(false);
				StateKeeper.getInstance().notifyLable(true);
			}
		}).start();
	}

	protected void navigationDrawerOpened() {
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			fragment.forceDelete();
		}
	}

	public void onQueryTextSubmitAct(String query) {
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		} else if (lastFragmentName.equals(PlaylistFragment.class.getSimpleName())) {
			PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				fragment.collapseAll();
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		}
	}

	public void onQueryTextChangeAct(String newText) {
		if ("".equals(newText)) {
			String fragmentName = getPreviousFragmentName(1);
			Fragment fragment = getFragmentManager().findFragmentByTag(fragmentName);
			if (LibraryFragment.class == fragment.getClass()) {
				if (fragment.isVisible()) {
					((LibraryFragment) fragment).clearFilter();
				}
			} else if (PlaylistFragment.class == fragment.getClass()) {
				if (fragment.isVisible()) {
					((PlaylistFragment) fragment).clearFilter();
				}
			}
		}
	}

	@Override
	protected void showPlayerFragment() {
		setDrawerEnabled(false);
		onNavigationDrawerItemSelected(PLAYER_FRAGMENT);
	}

	public boolean isThisSongDownloaded(AbstractSong song) {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
	}

	@Override
	public void download(final RemoteSong song) {
		if (isThisSongDownloaded(song)) {
			UndoBarController.clear(this);
			UndoBar undo = new UndoBar(this);
			undo.message(R.string.has_been_downloaded);
			undo.duration(5000);
			undo.noicon(true);
			undo.style(new UndoBarStyle(-1, R.string.download_anyway));
			undo.listener(new UndoBarController.UndoListener() {

				@Override
				public void onUndo(Parcelable token) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							BaseDownloadListener downloadListener = new BaseDownloadListener(MainActivity.this, song, 0, true);
							downloadListener.setDownloadPath(getDirectory());
							downloadListener.setUseAlbumCover(true);
							downloadListener.downloadSong(false);
						}
					});
				}
			});
			undo.show();
		} else {
			BaseDownloadListener downloadListener = new BaseDownloadListener(this, song, 0, true);
			downloadListener.setDownloadPath(getDirectory());
			downloadListener.setUseAlbumCover(true);
			downloadListener.downloadSong(false);
		}
	}

	public boolean isPlayerFragment() {
		FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
		return backEntry.getName().equals(PlayerFragment.class.getSimpleName());
	}

	@Override
	protected BaseDownloadSongTask createDownloadListener(RemoteSong song) {
		return new BaseDownloadListener(this, song, 0, true);
	}
	
	public void addPlayerElement(boolean flag) {
		if (null == navigationDrawerFragment) return;
		navigationDrawerFragment.setAdapter(flag);
	}

	public void setDrawerEnabled(boolean isEnabled) {
		navigationDrawerFragment.setEnabled(isEnabled);
	}

	protected boolean isDraverClosed() {
		return navigationDrawerFragment.isDrawerIndicatorEnabled();
	}

	public String getQuery() {
		return query;
	}

	public void setSelectedItem(int position) {
		currentPosition = position;
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setSelectedItem(position);
		}
		setCurrentFragmentId(position);
	}

	public void setVisibleSearchView(String fragmentName) {
		boolean flag = (fragmentName.equals(LibraryFragment.class.getSimpleName()))
				|| (fragmentName.equals(PlaylistFragment.class.getSimpleName()));
		if (flag != isVisibleSearchView) {
			invalidateOptionsMenu();
		}
		isVisibleSearchView = flag;
	}

	public SearchView getSearchView() {
		return searchView;
	}

	private boolean useOldToggle() {
		return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
	}
	
	@Override
	protected int getMiniPlayerDuplicateID() {
		return R.id.mini_player_duplicate;
	}
	
	public int getSettingsIcon() {
		return R.string.font_play_settings;
	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}
	
	@Override
	protected boolean isAnimationEnabled() {
		return Nulldroid_Settings.ENABLE_ANIMATIONS;
	}

	@Override
	public String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
	}

	public void showDialog() {
		new FolderSelectorDialog().show(this);
	}

	@Override
	protected int getMiniPlayerID() {
		return R.id.mini_player;
	}

	@Override
	protected int getMiniPlayerClickableID() {
		return R.id.mini_player_main;
	}
	
	@Override
	public void setTitle(int titleId) {
		setTitle(getString(titleId));
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		navigationDrawerFragment.setTitle(mTitle);
	}

	public int getCurrentFragmentId() {	return currentFragmentId; }

	public void setCurrentFragmentId(int currentFragmentId) {
		this.currentFragmentId = currentFragmentId;
	}
}