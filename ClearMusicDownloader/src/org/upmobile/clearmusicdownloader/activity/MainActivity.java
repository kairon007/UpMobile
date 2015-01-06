package org.upmobile.clearmusicdownloader.activity;

import java.io.File;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.fragment.SearchFragment;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Window;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenuItem;

public class MainActivity extends BaseClearActivity {

	private Fragment[] fragments;
	private ResideMenuItem[] items;
	private String[] titles;
	private PlayerService player;
	private FileObserver fileObserver = new FileObserver(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX) {
		
		@Override
		public void onEvent(int event, String path) {
			if(event == FileObserver.DELETE_SELF) {
				String folder_path = Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
				File file = new File(folder_path);
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + folder_path + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				player = PlayerService.get(MainActivity.this);
			}
		}).start();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		File file = new File(Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX);
		if (!file.exists()){
			file.mkdirs();
		}
		fileObserver.startWatching();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Fragment[] getFragments() {
		fragments = new Fragment[4];
		fragments[0] = new SearchFragment();
		fragments[1] = new DownloadsFragment();
		fragments[2] = new LibraryFragment();
		fragments[3] = new PlayerFragment();
		return fragments;
	}
	
	@Override
	protected void onResume() {
		if (null != player && player.isPlaying()) {
			showPlayerElement();
		}
		super.onResume();
	}

	@Override
	protected ResideMenuItem[] getMenuItems() {
		items = new ResideMenuItem[4];
		items[0] = new ResideMenuItem(this, R.drawable.ic_search, R.string.navigation_search);
		items[1] = new ResideMenuItem(this, R.drawable.ic_downloads, R.string.navigation_downloads);
		items[2] = new ResideMenuItem(this, R.drawable.ic_library, R.string.navigation_library);
		items[3] = new ResideMenuItem(this, R.drawable.ic_player, R.string.navigation_player);
		return items;
	}
	
	@Override
	protected String[] getTitlePage() {
		titles = getResources().getStringArray(R.array.titles);
		return titles;
	}
	
	@Override
	public void showTopFrame() {
		super.showTopFrame();
	}
	
	@Override
	public void hideTopFrame() {
		super.hideTopFrame();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}
	
	@Override
	public void hidePlayerElement() {
		super.hidePlayerElement();
	}
	
	@Override
	public void showPlayerElement() {
		super.showPlayerElement();
	}

}