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
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

public class MainActivity extends BaseClearActivity implements Constants {

	private Fragment[] fragments;
	private ResideMenuItem[] items;
	private SearchView searchView;
	private String[] titles;
	private String query;
	private boolean useCoverHelper = true;
	private String folder_path = ClearMusicDownloaderApp.getDirectory();
	private FileObserver fileObserver = new FileObserver(folder_path) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
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
		if (!file.exists()) {
			file.mkdirs();
		}
		fileObserver.startWatching();
		super.onCreate(savedInstanceState);
		initSearchView();
		
		// Nulldroid_Advertisement.startIfNotBlacklisted(this, false);

	}

	private void initSearchView() {
		searchView = (SearchView) findViewById(R.id.ab_search);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String q) {
				query = q;
				hideKeyboard();
				android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
				String lastFragmentName = backEntry.getName();
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
				} else {
					((TextView) findViewById(R.id.page_title)).setText(titles[SEARCH_FRAGMENT]);
					changeFragment(new SearchFragment(), false);
					searchView.setIconified(true);
					searchView.setIconified(true);
				}
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if ("".equals(newText)) {
					LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
					if (fragment.isVisible()) {
						fragment.clearFilter();
					}
				}
				return false;
			}
		});
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null != searchView)
			imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	}

	public String getQuery() {
		return query;
	}

	@Override
	protected void manageSearchView(String targetFragment) {
		if (null == searchView) {
			return;
		}
		if (targetFragment.equals(LibraryFragment.class.getSimpleName())) {
			searchView.setVisibility(View.VISIBLE);
		} else if (targetFragment.equals(DownloadsFragment.class.getSimpleName()) || targetFragment.equals(PlaylistFragment.class.getSimpleName())) {
			searchView.setVisibility(View.VISIBLE);
		} else {
			searchView.setVisibility(View.GONE);
		}
	}

	@Override
	protected Fragment[] getFragments() {
		fragments = new Fragment[COUNT_FRAGMENT];
		fragments[SEARCH_FRAGMENT] = new SearchFragment();
		fragments[DOWNLOADS_FRAGMENT] = new DownloadsFragment();
		fragments[PLAYLIST_FRAGMENT] = new PlaylistFragment();
		fragments[LIBRARY_FRAGMENT] = new LibraryFragment();
		fragments[PLAYER_FRAGMENT] = new PlayerFragment();
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
		items[PLAYLIST_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_playlist, R.string.tab_playlist, ResideMenuItem.Types.TYPE_MENU);
		items[LIBRARY_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_library, R.string.tab_library, ResideMenuItem.Types.TYPE_MENU);
		items[PLAYER_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_player, R.string.tab_now_plaing, ResideMenuItem.Types.TYPE_MENU);
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
		return fragments[PLAYER_FRAGMENT];
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
		if (!fragment.isAdded()) {
			fragment.setArguments(getArguments());
			changeFragment(fragment, true);
		}
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
	protected void setCover(Bitmap bmp) {
		if (null == bmp) {
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.def_cover_circle);
		}
		((ImageView) findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}

}