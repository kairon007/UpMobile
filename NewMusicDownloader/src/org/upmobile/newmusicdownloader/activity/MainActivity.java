package org.upmobile.newmusicdownloader.activity;

import java.io.File;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.newmusicdownloader.fragment.LibraryFragment;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.fragment.SearchFragment;
import org.upmobile.newmusicdownloader.service.PlayerService;
import org.upmobile.newmusicdownloader.ui.NavigationDrawerFragment;
import org.upmobile.newmusicdownloader.ui.NavigationDrawerFragment.NavigationDrawerCallbacks;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;

public class MainActivity extends Activity implements NavigationDrawerCallbacks {

	private final String folderPath = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new Thread(new Runnable() {

			@Override
			public void run() {
				PlayerService.get(MainActivity.this);
			}
			
		}).start();
        navigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		File file = new File(folderPath);
		if (!file.exists()) file.mkdirs();
		fileObserver.startWatching();
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
			changeFragment(new PlayerFragment());
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
			finish();
		}
	}
	
	public void showPlayerElement(boolean flag) {
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setAdapter(flag);
		}
	}

}