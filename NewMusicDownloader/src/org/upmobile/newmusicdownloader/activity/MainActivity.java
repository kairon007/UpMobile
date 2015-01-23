package org.upmobile.newmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmusicdownloader.service.PlayerService;
import org.upmobile.newmusicdownloader.ui.NavigationDrawerFragment;
import org.upmobile.newmusicdownloader.ui.NavigationDrawerFragment.NavigationDrawerCallbacks;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity implements NavigationDrawerCallbacks {

	private final String ARRAY_SAVE = "extras_array_save";
	private final String folderPath = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	private PlayerService service;
	
	private NavigationDrawerFragment navigationDrawerFragment;

	private FileObserver fileObserver = new FileObserver(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX) {

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
		new Thread(new Runnable() {

			@Override
			public void run() {
				service = PlayerService.get(MainActivity.this);
				if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE)) {
					ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
					service.setArrayPlayback(list);
				}
				if (service.isPlaying()) showPlayerElement(true);
			}
			
		}).start();
        navigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		File file = new File(folderPath);
		if (!file.exists()) file.mkdirs();
		fileObserver.startWatching();
	}
	
	@Override
	protected void onResume() {
		if (null != service && service.isPlaying()) {
			showPlayerElement(true);
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
				if (service.getPlayingSong().getClass() == MusicData.class) {
					args.putParcelable(Constants.KEY_SELECTED_SONG, (MusicData) service.getPlayingSong());
				} else {
					args = null;
				}
		    	fragment.setArguments(args);
		    	changeFragment(fragment);
		    }
			break;
		default:
			break;
		}
	}
	
	public void changeFragment(Fragment targetFragment) {
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
}