package org.upmobile.newmusicdownloader.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import org.upmobile.newmusicdownloader.BaseDownloadListener;
import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;
import org.upmobile.newmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmusicdownloader.fragment.NavigationDrawerFragment;
import org.upmobile.newmusicdownloader.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.newmusicdownloader.fragment.SearchFragment;

import java.io.File;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class MainActivity extends BaseMiniPlayerActivity implements NavigationDrawerCallbacks, OnQueryTextListener, Constants {

	private final String folderPath = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	private SearchView searchView;
	private MenuItem searchItem;
	private NavigationDrawerFragment navigationDrawerFragment;
	private String currentTag;
	private boolean isVisibleSearchView = false;
	private boolean buttonBackEnabled = false;

	private FileObserver fileObserver = new FileObserver(folderPath) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
				File file = new File(folderPath);
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + folderPath + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setIcon(android.R.color.transparent);
		getSupportActionBar().setElevation(0);
        navigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		File file = new File(folderPath);
		if (!file.exists()) file.mkdirs();
		fileObserver.startWatching();
		int resId = getResources().getIdentifier("action_bar_title", "id", "android");
		View v1 = findViewById(resId);
		if (null != v1) {
			((View) v1.getParent().getParent().getParent().getParent()).setBackgroundColor(getResources().getColor(Util.getResIdFromAttribute(this, R.attr.actionBarTitleBg)));
		}
//		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}
	
	
	@Override
	public void setCover(Bitmap bmp) {
		super.setCover(bmp);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
		searchView.setOnQueryTextListener(this);
		searchView.setOnSearchClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (navigationDrawerFragment.isDrawerOpen()) {
					navigationDrawerFragment.closeDrawer();
					searchView.onActionViewExpanded();
				}
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onQueryTextChange(String query) {
		if (query.isEmpty()) {
			String fragmentName = getPreviousFragmentName(1);
			Fragment fragment = getFragmentManager().findFragmentByTag(fragmentName);
			if (null != fragment) {
                if (LibraryFragment.class == fragment.getClass() && fragment.isVisible()) {
                    ((LibraryFragment) fragment).clearFilter();
                } else if (PlaylistFragment.class == fragment.getClass() && fragment.isVisible()) {
                    ((PlaylistFragment) fragment).clearFilter();
                }
            }
		}
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		Util.hideKeyboard(this, searchView);
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			searchView.clearFocus();
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		} else if(lastFragmentName.equals(PlaylistFragment.class.getSimpleName())){
			searchView.clearFocus();
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
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view  = findViewById(R.id.drawer_layout);
		Util.hideKeyboard(this, view);
		switch(item.getItemId()) {
        case R.id.action_search:
            searchView.setIconified(false);// to Expand the SearchView when clicked
            return true;
	    }
		return false;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_search).setVisible(isVisibleSearchView);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		buttonBackEnabled = true;
		if (position == PLAYER_FRAGMENT) {
			showMiniPlayer(false);
		} else if (position <= LIBRARY_FRAGMENT){
			showMiniPlayer(null != service && service.isEnqueueToStream());
		}
		switch (position) {
		case SEARCH_FRAGMENT:
	        changeFragment(new SearchFragment(), false);
			break;
		case DOWNLOADS_FRAGMENT:
	        changeFragment(new DownloadsFragment(), false);
			break;
		case LIBRARY_FRAGMENT:
	        changeFragment(new LibraryFragment(), false);
			break;
		case PLAYLIST_FRAGMENT:
			changeFragment(new PlaylistFragment(), false);
			break;
		case PLAYER_FRAGMENT:
			android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
			String lastFragmentName = backEntry.getName();
		    if (!lastFragmentName.equals(PlayerFragment.class.getSimpleName())) {
		    	changeFragment(new PlayerFragment(), true);
		    }
			break;
		case SETTINGS_FRAGMENT:
		case 6:
	        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this, new DirectoryChooserDialog.ChosenDirectoryListener() {
				
				@Override
				public void onChosenDir(String chDir) {
					File file = new File(chDir);
					Editor editor = sp.edit();
					editor.putString(PREF_DIRECTORY, chDir);
					editor.putString(PREF_DIRECTORY_PREFIX, File.separator + file.getAbsoluteFile().getName() + File.separator);
					editor.apply();
					if (null != navigationDrawerFragment && null != service) {
						navigationDrawerFragment.setAdapter(service.isPlaying());
					}
				}
			});
			directoryChooserDialog.chooseDirectory();
			break;
		}
	}
	
	public void setActionBarTitle(String title) {
		getSupportActionBar().setTitle(title);
	}
	
	public void setActionBarTitle(int title) {
		setActionBarTitle(getResources().getString(title));
	}
	
	public void changeFragment(Fragment targetFragment, boolean isAnimate) {
		currentTag = targetFragment.getClass().getSimpleName();
		setSearchViewVisibility(currentTag);
		currentFragmentIsPlayer = currentTag.equals(PlayerFragment.class.getSimpleName());
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (isAnimate && Nulldroid_Settings.ENABLE_ANIMATIONS) {
			transaction.setCustomAnimations(R.anim.fragment_slide_in_up, R.anim.fragment_slide_out_up, R.anim.fragment_slide_in_down, R.anim.fragment_slide_out_down);
		}
		transaction.replace(R.id.content_frame, targetFragment, currentTag)
				   .addToBackStack(targetFragment.getClass()
				   .getSimpleName())
				   .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				   .commit();
	}

	private void setSearchViewVisibility(String fragmentName) {
		isVisibleSearchView = (fragmentName.equals(LibraryFragment.class.getSimpleName())) || (fragmentName.equals(PlaylistFragment.class.getSimpleName()));
	}
	
	public boolean isButtonBackEnabled() {
		return buttonBackEnabled;
	}
	
	private String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position) return SearchFragment.class.getSimpleName();
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
        return backEntry.getName();
	}
	
	@Override
	public void onBackPressed() {
		if (navigationDrawerFragment.isVisible()) {
			navigationDrawerFragment.closeDrawer();
			return;
		}
		Fragment currentFragment = getFragmentManager().findFragmentByTag(currentTag);
		setSearchViewVisibility(getPreviousFragmentName(2));
		currentFragmentIsPlayer = false;
		if (null != currentFragment && currentFragment.isVisible() && currentFragment.getClass() == PlayerFragment.class) {
			getFragmentManager().popBackStack();
			invalidateOptionsMenu();
			showMiniPlayer(service.isEnqueueToStream());
		} else {
			if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
			} else {
				stopService(new Intent(this, PlaybackService.class));
				finish();
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		invalidateOptionsMenu();
	}
	
	@Override
	public void showPlayerElement(boolean flag) {
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setAdapter(flag);
		}
	}
	
	public void setSelectedItem(int position) {
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setSelectedItem(position);
		}
	}

	@Override
	public void download(RemoteSong song) {
		download(song, true);
	}

	public void download (final RemoteSong song, final boolean useCover) {
		if (isThisSongDownloaded(song)) {
			final View v = LayoutInflater.from(this).inflate(R.layout.notification_view, null);
			WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, PixelFormat.TRANSLUCENT);
			layoutParams.gravity = Gravity.BOTTOM;
			layoutParams.windowAnimations = android.R.style.Animation_InputMethod;
			final WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			v.findViewById(R.id.msgButton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if (v.getVisibility() == View.GONE) return;
					manager.removeView(v);
					v.setVisibility(View.GONE);
					int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
					BaseDownloadListener downloadListener = new BaseDownloadListener(MainActivity.this, song, id);
					downloadListener.setDownloadPath(NewMusicDownloaderApp.getDirectory());
					downloadListener.setUseAlbumCover(useCover);
					downloadListener.downloadSong(false);
				}
			});
			v.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (v.getVisibility() == View.GONE) return;
					manager.removeView(v);
					v.setVisibility(View.GONE);
				}
			}, 3000);
			manager.addView(v,layoutParams);
		} else {
			int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
			BaseDownloadListener downloadListener = new BaseDownloadListener(this, song, id);
			downloadListener.setDownloadPath(NewMusicDownloaderApp.getDirectory());
			downloadListener.setUseAlbumCover(useCover);
			downloadListener.downloadSong(false);
		}
	}

	public boolean isThisSongDownloaded(AbstractSong song) {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
	}

	public void setDrawerEnabled(boolean isEnabled) {
		navigationDrawerFragment.setEnabled(isEnabled);
	}
	
	public void setCurrentTag(String currentTag) {
		this.currentTag = currentTag;
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
	protected void showPlayerFragment() {
		buttonBackEnabled = false;
		showMiniPlayer(false);
		Fragment fragment = new PlayerFragment();
    	changeFragment(fragment, true);
	}

	@Override
	public void showMiniPlayer(boolean isShow) {
		super.showMiniPlayer(currentFragmentIsPlayer ? false : isShow);
	}

	@Override
	protected boolean isAnimationEnabled() {
		return Nulldroid_Settings.ENABLE_ANIMATIONS;
	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}
	
	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectory();
	}

	@Override
	protected int getMiniPlayerDuplicateID() {
		return R.id.mini_player_duplicate;
	}
	
	@Override
	protected BaseDownloadSongTask createDownloadListener(RemoteSong song) {
		return new BaseDownloadListener(this, song, 0);
	}
	
	public SearchView getSearchView() {
		return searchView;
	}
	
	public MenuItem getSearchItem() {
		return searchItem;
	}

}