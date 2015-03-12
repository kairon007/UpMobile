package org.upmobile.newmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;
import org.upmobile.newmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmusicdownloader.fragment.NavigationDrawerFragment;
import org.upmobile.newmusicdownloader.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.newmusicdownloader.fragment.SearchFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends BaseMiniPlayerActivity implements NavigationDrawerCallbacks, Constants {

	private final String APP_THEME_WHITE_BLACK_ACTION_BAR = "AppThemeWhite.BlackActionBar";
	private final String APP_THEME_WHITE = "AppThemeWhite";
	private final String folderPath = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	private SearchView searchView;
	private NavigationDrawerFragment navigationDrawerFragment;
	private String currentTag;
	private boolean isVisibleSearchView = false;

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
		getActionBar().setIcon(android.R.color.transparent);
        navigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		File file = new File(folderPath);
		if (!file.exists()) file.mkdirs();
		if (null != service) {
			if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE)) {
				ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
				service.setArrayPlayback(list);
			}
			if (service.isPlaying()) showPlayerElement(true);
		}
		fileObserver.startWatching();
		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
		searchView.setOnSearchClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (navigationDrawerFragment.isDrawerOpen()) {
					navigationDrawerFragment.closeDrawer();
					searchView.onActionViewExpanded();
				}
			}
		});
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				hideKeyboard();
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
			public boolean onQueryTextChange(String newText) {
				if ("".equals(newText)) {
					String fragmentName =  getPreviousFragmentName(1);
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
				return false;
			}
			
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view  = findViewById(R.id.drawer_layout);
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
	protected void onResume() {
		if (null != service && service.isPlaying()) {
			showPlayerElement(true);
		} else if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(this);
		}
		super.onResume();
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null != searchView)
			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		if (position == PLAYER_FRAGMENT) {
			showMiniPlayer(false);
		} else if (position <= LIBRARY_FRAGMENT){
			showMiniPlayer(true);
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
		    	Fragment fragment = new PlayerFragment();
		    	if (null == service) {
		    		service = PlaybackService.get(this);
		    	}
		    	changeFragment(fragment, true);
		    }
			break;
		case SETTINGS_FRAGMENT:
		case 6:
	        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	    	if (null == service) {
	    		service = PlaybackService.get(this);
	    	}
			DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this, isWhiteTheme(this), new DirectoryChooserDialog.ChosenDirectoryListener() {
				
				@Override
				public void onChosenDir(String chDir) {
					File file = new File(chDir);
					Editor editor = sp.edit();
					editor.putString(PREF_DIRECTORY, chDir);
					editor.putString(PREF_DIRECTORY_PREFIX, File.separator + file.getAbsoluteFile().getName() + File.separator);
					editor.commit();
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
		getActionBar().setTitle(title);
	}
	
	public void setActionBarTitle(int title) {
		setActionBarTitle(getResources().getString(title));
	}
	
	public void changeFragment(Fragment targetFragment, boolean isAnimate) {
		setSearchViewVisibility(targetFragment.getClass().getSimpleName());
		currentTag = targetFragment.getClass().getSimpleName();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (isAnimate) {
			transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down);
		}
		transaction.replace(R.id.content_frame, targetFragment, currentTag)
		.addToBackStack(targetFragment.getClass().getSimpleName())
		.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		.commit();
	}

	private void setSearchViewVisibility(String fragmentName) {
		isVisibleSearchView = (fragmentName.equals(LibraryFragment.class.getSimpleName())) || (fragmentName.equals(PlaylistFragment.class.getSimpleName()));
	}
	
	private String getPreviousFragmentName(int position) {
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
		String previousFragmentName = backEntry.getName();
		return previousFragmentName;
	}
	
	@Override
	public void onBackPressed() {
		Fragment currentFragment = getFragmentManager().findFragmentByTag(currentTag);
		setSearchViewVisibility(getPreviousFragmentName(2));
		if (null != currentFragment && currentFragment.isVisible() && currentFragment.getClass() == PlayerFragment.class) {
			getFragmentManager().popBackStack();
			invalidateOptionsMenu();
			showMiniPlayer(true);
		} else {
			if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
				showPlayerElement(false);
			} else {
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
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (service == null) {
			service = PlaybackService.get(this);
		}
		if (service.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, service.getArrayPlayback());
		}
	}
	
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
		showMiniPlayer(false);
		Fragment fragment = new PlayerFragment();
    	Bundle args = new Bundle();
    	if (null != service.getPlayingSong()) {
			args.putParcelable(Constants.KEY_SELECTED_SONG, service.getPlayingSong());
		} else {
			args = null;
		}
    	fragment.setArguments(args);
    	changeFragment(fragment, true);
	}
	
	public boolean isWhiteTheme(Context context) {
		    PackageInfo packageInfo;
		    try {
		        packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		        int themeResId = packageInfo.applicationInfo.theme;
		        return context.getResources().getResourceEntryName(themeResId).equals(APP_THEME_WHITE) || context.getResources().getResourceEntryName(themeResId).equals(APP_THEME_WHITE_BLACK_ACTION_BAR);
		    } catch (Exception e) {
		    	Log.e(Util.class.getSimpleName(), e.getMessage());
		        return false;
		    }
	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}
	
	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectory();
	}
}