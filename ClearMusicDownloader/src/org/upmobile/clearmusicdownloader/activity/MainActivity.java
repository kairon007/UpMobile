package org.upmobile.clearmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.clearmusicdownloader.fragment.SearchFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Window;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

public class MainActivity extends BaseClearActivity implements Constants {

	private static final int COUNT_FRAGMENT = 6;
	
	private static final int SEARCH_FRAGMENT = 0;
	private static final int DOWNLOADS_FRAGMENT = 1;
	private static final int PLAYLIST_FRAMGNET = 2;
	private static final int LIBRARY_FRAGMENT = 3;
	private static final int PLAYER_FRAMGNET = 4;
	private static final int SETTINGS_FRAGMENT = 5;
	
	private Fragment[] fragments;
	private ResideMenuItem[] items;
	private String[] titles;
	private boolean useCoverHelper = true;
	private String folder_path = ClearMusicDownloaderApp.getDirectory();
	private FileObserver fileObserver = new FileObserver(folder_path) {
		
		@Override
		public void onEvent(int event, String path) {
			if(event == FileObserver.DELETE_SELF) {
				File file = new File(folder_path);
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + folder_path + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(MainActivity.this);
			if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE)) {
				ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
				service.setArrayPlayback(list);
			}
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		File file = new File(folder_path);
		if (!file.exists()){
			file.mkdirs();
		}
		fileObserver.startWatching();
		
//		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Fragment[] getFragments() {
		fragments = new Fragment[COUNT_FRAGMENT];
		fragments[SEARCH_FRAGMENT] = new SearchFragment();
		fragments[DOWNLOADS_FRAGMENT] = new DownloadsFragment();
		fragments[PLAYLIST_FRAMGNET] = new PlaylistFragment();
		fragments[LIBRARY_FRAGMENT] = new LibraryFragment();
		fragments[PLAYER_FRAMGNET] = new PlayerFragment();
		fragments[SETTINGS_FRAGMENT] = new Fragment();
		return fragments;
	}
	
	@Override
	protected void onResume() {
		checkService();
		if (null != service && service.isPlaying()) {
			showPlayerElement();
		}
		super.onResume();
	}

	private void checkService() {
		if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(this);
		}
	}

	@Override
	protected ResideMenuItem[] getMenuItems() {
		items = new ResideMenuItem[COUNT_FRAGMENT];
		items[SEARCH_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_search, R.string.tab_search, ResideMenuItem.Types.TYPE_MENU);
		items[DOWNLOADS_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_downloads, R.string.tab_downloads, ResideMenuItem.Types.TYPE_MENU);
		items[PLAYLIST_FRAMGNET] = new ResideMenuItem(this, R.drawable.ic_playlist, R.string.tab_playlist, ResideMenuItem.Types.TYPE_MENU);
		items[LIBRARY_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_library, R.string.tab_library, ResideMenuItem.Types.TYPE_MENU);
		items[PLAYER_FRAMGNET] = new ResideMenuItem(this, R.drawable.ic_player, R.string.tab_now_plaing, ResideMenuItem.Types.TYPE_MENU);
		items[SETTINGS_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_settings, R.string.download_dialog_download_location, ClearMusicDownloaderApp.getDirectory(), ResideMenuItem.Types.TYPE_SETTINGS);
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
		checkService();
		if (service.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, service.getArrayPlayback());
		}
	}
	
	@Override
	protected Bundle getArguments() {
		if (null != service) {
			Bundle args = new Bundle();
			args.putParcelable(Constants.KEY_SELECTED_SONG, (AbstractSong) service.getPlayingSong());
			return args;
		}
		return null;
	}
	
	public void setCoverHelper(boolean val) {
		useCoverHelper = val;
	}
	
	public boolean stateCoverHelper() {
		return useCoverHelper;
	}

	@Override
	protected Fragment getPlayerFragment() {
		return fragments[PLAYER_FRAMGNET];
	}
	
	public void setResideMenuListener(ResideMenu.OnMenuListener listener) {
		getResideMenu().setMenuListener(listener);
	}
	
	@Override
	public void stopChildsServices() {
		PlaybackService.get(this).reset();
	}

	@Override
	protected void showDialog() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this, true, new DirectoryChooserDialog.ChosenDirectoryListener() {
			
			@Override
			public void onChosenDir(String chDir) {
				File file = new File(chDir);
				Editor editor = sp.edit();
				editor.putString(PREF_DIRECTORY, chDir);
				editor.putString(PREF_DIRECTORY_PREFIX, File.separator + file.getAbsoluteFile().getName() + File.separator);
				editor.commit();
				reDrawMenu();
			}
		});
		directoryChooserDialog.chooseDirectory();
	}

	@Override
	protected boolean isPlaying() {
		return PlaybackService.get(this).isPlaying();
	}
	
	@Override
	protected void showPlayerFragment() {
		Fragment fragment = getPlayerFragment();
		fragment.setArguments(getArguments());
		changeFragment(fragment);
	}
	
	@Override
	protected int getMiniPlayerID() {
		return R.id.mini_player;
	}

	@Override
	protected int getMiniPlayerClickableID() {
		return R.id.mini_player_main;
	}

}
