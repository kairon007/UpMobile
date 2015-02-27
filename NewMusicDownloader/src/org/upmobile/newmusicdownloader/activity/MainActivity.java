package org.upmobile.newmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;
import org.upmobile.newmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmusicdownloader.ui.NavigationDrawerFragment;
import org.upmobile.newmusicdownloader.ui.NavigationDrawerFragment.NavigationDrawerCallbacks;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity implements NavigationDrawerCallbacks, Constants {

	private final String ARRAY_SAVE = "extras_array_save";
	private final String folderPath = NewMusicDownloaderApp.getDirectory();
	private PlaybackService service;
	private SearchView searchView;
	private NavigationDrawerFragment navigationDrawerFragment;
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
				changeFragment(new SearchFragment(query));
				searchView.setIconified(true);
				searchView.setIconified(true);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
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
	    switch(item.getItemId()){

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
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
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
		switch (position) {
		case 0:
	        changeFragment(new SearchFragment());
			break;
		case 1:
	        changeFragment(new DownloadsFragment());
			break;
		case 2:
	        changeFragment(new LibraryFragment());
			break;
		case 3:
			android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
			String lastFragmentName = backEntry.getName();
		    if (!lastFragmentName.equals(PlayerFragment.class.getSimpleName())) {
		    	Fragment fragment = new PlayerFragment();
		    	Bundle args = new Bundle();
		    	if (null == service) {
		    		service = PlaybackService.get(this);
		    	}
				if (service.getPlayingSong().getClass() == MusicData.class) {
					args.putParcelable(Constants.KEY_SELECTED_SONG, (MusicData) service.getPlayingSong());
				} else {
					args = null;
				}
		    	fragment.setArguments(args);
		    	changeFragment(fragment);
		    }
			break;
		case 4:
		case 5:
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
					editor.putString(PREF_DIRECTORY_PREFIX, file.getAbsoluteFile().getName());
					editor.commit();
					if (null != navigationDrawerFragment && null != service) {
						navigationDrawerFragment.setAdapter(service.isPlaying());
					}
				}
			});
			directoryChooserDialog.chooseDirectory();
			break;
		default:
			break;
		}
	}
	
	public void changeFragment(Fragment targetFragment) {
		isVisibleSearchView = targetFragment.getClass().getSimpleName().equals(SearchFragment.class.getSimpleName()) ? false : true;
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.main_fragment, targetFragment, targetFragment.getClass().getSimpleName())
		.addToBackStack(targetFragment.getClass().getSimpleName())
		.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		.commit();
	}
	
	@Override
	public void onBackPressed() {
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (null != player && player.isVisible()) {
			getFragmentManager().popBackStack();
			isVisibleSearchView = false;
			invalidateOptionsMenu();
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
	
}