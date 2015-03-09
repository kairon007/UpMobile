package org.upmobile.newmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmusicdownloader.fragment.NavigationDrawerFragment;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.newmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmusicdownloader.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends BaseMiniPlayerActivity implements NavigationDrawerCallbacks, Constants {

	private final String folderPath = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	private SearchView searchView;
	private NavigationDrawerFragment navigationDrawerFragment;
	private String currentTag;
	private boolean isVisibleSearchView = false;
	protected boolean isEnabledFilter = false;

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
		 searchView.setOnQueryTextListener(new OnQueryTextListener() {
		
			@Override
			public boolean onQueryTextSubmit(String query) {
				android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() -1);
    			String lastFragmentName = backEntry.getName();
				if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
					LibraryFragment fragment = (LibraryFragment)getFragmentManager().findFragmentByTag(currentTag);
					if (fragment.isVisible()) {
						if ("".equals(query)) {
							fragment.clearFilter();
							isEnabledFilter = false;
						} else {
							isEnabledFilter = true;
							fragment.setFilter(query);
						}
					}
				} else {
					isEnabledFilter = false;
					changeFragment(new SearchFragment(query), false);
					searchView.setIconified(true);
					searchView.setIconified(true);
				}
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				if (isEnabledFilter && "".equals(newText)) {
					LibraryFragment fragment = (LibraryFragment)getFragmentManager().findFragmentByTag(currentTag);
					fragment.clearFilter();
            		isEnabledFilter = false;
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
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		isEnabledFilter = false;
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
			DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this, false, new DirectoryChooserDialog.ChosenDirectoryListener() {
				
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
	
	public void changeFragment(Fragment targetFragment, boolean isAnimate) {
		isVisibleSearchView = targetFragment.getClass() != SearchFragment.class;
		currentTag = targetFragment.getClass().getSimpleName();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (isAnimate) {
			transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down);
		}
		transaction.replace(R.id.main_fragment, targetFragment, currentTag)
		.addToBackStack(targetFragment.getClass().getSimpleName())
		.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		.commit();
	}
	
	@Override
	public void onBackPressed() {
		isEnabledFilter = false;
		Fragment player = getFragmentManager().findFragmentByTag(currentTag);
		if (null != player && player.isVisible() && player.getClass() == PlayerFragment.class) {
			AbstractSong plaingSong = PlaybackService.get(this).getPlayingSong();
			if (null != plaingSong) {
				isVisibleSearchView = plaingSong.getClass() == MusicData.class;
			}
			getFragmentManager().popBackStack();
			invalidateOptionsMenu();
			showMiniPlayer(true);
		} else {
			if (null != service) {
				service.reset();
			}
			finish();
		}
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
}