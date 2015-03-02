package org.upmobile.clearmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
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

	private final String ARRAY_SAVE = "extras_array_save";
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
		
		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Fragment[] getFragments() {
		fragments = new Fragment[5];
		fragments[0] = new SearchFragment();
		fragments[1] = new DownloadsFragment();
		fragments[2] = new LibraryFragment();
		fragments[3] = new PlayerFragment();
		fragments[4] = new Fragment();
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
		items = new ResideMenuItem[5];
		items[0] = new ResideMenuItem(this, R.drawable.ic_search, R.string.tab_search, ResideMenuItem.Types.TYPE_MENU);
		items[1] = new ResideMenuItem(this, R.drawable.ic_downloads, R.string.tab_downloads, ResideMenuItem.Types.TYPE_MENU);
		items[2] = new ResideMenuItem(this, R.drawable.ic_library, R.string.tab_library, ResideMenuItem.Types.TYPE_MENU);
		items[3] = new ResideMenuItem(this, R.drawable.ic_player, R.string.tab_now_plaing, ResideMenuItem.Types.TYPE_MENU);
		items[4] = new ResideMenuItem(this, R.drawable.ic_search, R.string.tab_settings, ClearMusicDownloaderApp.getDirectory(), ResideMenuItem.Types.TYPE_SETTINGS);
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
		if (null != service && service.getPlayingSong().getClass() == MusicData.class) {
			Bundle args = new Bundle();
			args.putParcelable(Constants.KEY_SELECTED_SONG, (MusicData) service.getPlayingSong());
			return args;
		} else
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
		return new PlayerFragment();
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
				editor.putString(PREF_DIRECTORY_PREFIX, file.getAbsoluteFile().getName());
				editor.commit();
				reDrawMenu();
			}
		});
		directoryChooserDialog.chooseDirectory();
	}
	
	@Override
	protected int getMiniPlayerID() {
		// TODO Auto-generated method stub
		return 0;
	}
}