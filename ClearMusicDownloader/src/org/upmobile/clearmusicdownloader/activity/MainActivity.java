package org.upmobile.clearmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.data.MusicData;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.fragment.SearchFragment;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Window;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

public class MainActivity extends BaseClearActivity {

	private final String ARRAY_SAVE = "extras_array_save";
	private Fragment[] fragments;
	private ResideMenuItem[] items;
	private String[] titles;
	private PlayerService player;
	private OrientationListener orientationListener;
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
	public void onCreate(final Bundle savedInstanceState) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				player = PlayerService.get(MainActivity.this);
				if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE)) {
					ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
					player.setArrayPlayback(list);
				}
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
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		if (player.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, player.getArrayPlayback());
		}
	}
	
	@Override
	protected Bundle getArguments() {
		if (null != player && player.getPlayingSong().getClass() == MusicData.class) {
			Bundle args = new Bundle();
			args.putParcelable(Constants.KEY_SELECTED_SONG, (MusicData) player.getPlayingSong());
			args.putInt(Constants.KEY_SELECTED_POSITION, player.getPlayingPosition());
			return args;
		} else
			return null;
	}

	@Override
	protected Fragment getPlayerFragment() {
		return new PlayerFragment();
	}
	
	public void setResideMenuListener(ResideMenu.OnMenuListener listener) {
		getResideMenu().setMenuListener(listener);
	}
	
	@Override
	public void stopChildsServices() {
		if (null != player) {
			player.reset();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		int orientation = newConfig.orientation;
		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			if (null != orientationListener) {
				orientationListener.landscapeOrientation();
			}
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			if (null != orientationListener) {
				orientationListener.portraitOrientation();
			}
			break;
		}
		super.onConfigurationChanged(newConfig);
	}
	
	public OrientationListener getOrientationListener() {
		return orientationListener;
	}

	public void setOrientationListener(OrientationListener orientationListener) {
		this.orientationListener = orientationListener;
	}

	public interface OrientationListener {
		public void landscapeOrientation();
		public void portraitOrientation();
	}
}