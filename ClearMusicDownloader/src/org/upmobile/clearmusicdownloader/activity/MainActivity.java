package org.upmobile.clearmusicdownloader.activity;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenu;
import com.special.menu.ResideMenuItem;

import org.upmobile.clearmusicdownloader.Constants;
import org.upmobile.clearmusicdownloader.DownloadListener;
import org.upmobile.clearmusicdownloader.Nulldroid_Settings;
import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.app.ClearMusicDownloaderApp;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.clearmusicdownloader.fragment.SearchFragment;

import java.io.File;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import ru.johnlife.lifetoolsmp3.ui.dialog.DirectoryChooserDialog;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class MainActivity extends BaseClearActivity implements Constants {

	private Fragment[] fragments;
    private SearchView searchView;
    private String query;
	private boolean useCoverHelper = true;
	private String folder_path = ClearMusicDownloaderApp.getDirectory();
	private FileObserver fileObserver = new FileObserver(folder_path) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
				File file = new File(folder_path);
				file.mkdirs();
				Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + " LIKE '" + folder_path + "%'", null);
				getContentResolver().notifyChange(uri, null);
			}
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		File file = new File(folder_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		fileObserver.startWatching();
		super.onCreate(savedInstanceState);
		initSearchView();
		
//		 Nulldroid_Advertisement.startIfNotBlacklisted(this, false);

	}
	
	@Override
	protected void onStart() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				checkOnStart(true);
			}
		}).start();
		super.onStart();
	}
	
	@Override
	protected void checkOnStart(boolean showMiniPlayer) {
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		super.checkOnStart(null == player || !player.isVisible());
	}
	
	private void initSearchView() {
		searchView = (SearchView) findViewById(R.id.ab_search);
		AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
		mQueryTextView.setTextColor(Color.WHITE);
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String q) {
				query = q;
				Util.hideKeyboard(MainActivity.this, searchView);
				String lastFragmentName = getLastFragmentName();
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
				} else if (lastFragmentName.equals(PlaylistFragment.class.getSimpleName())) {
					searchView.clearFocus();
					PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
					if (fragment.isVisible()) {
						fragment.collapseAll();
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
					String fragmentName = getLastFragmentName();
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
	}
	
	private String getLastFragmentName() {
		android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
        return backEntry.getName();
	}

	public String getQuery() {
		return query;
	}

	@Override
	protected void manageSearchView(String targetFragment) {
		if (null == searchView) return;
		if (targetFragment.equals(LibraryFragment.class.getSimpleName())) {
			searchView.setVisibility(View.VISIBLE);
		} else if (targetFragment.equals(PlaylistFragment.class.getSimpleName())) {
			searchView.setVisibility(View.VISIBLE);
		} else {
			searchView.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void download(RemoteSong song) {
		download(song, true);
	}

	public void download (final RemoteSong song, boolean useCover) {
		if (isThisSongDownloaded(song)) {
			UndoBarController.clear(this);
			UndoBarController.UndoBar undo = new UndoBarController.UndoBar(this);
			undo.message(R.string.has_been_downloaded);
			undo.duration(5000);
			undo.noicon(true);
			undo.style(new UndoBarStyle(-1, R.string.download_anyway));
			undo.listener(new UndoBarController.UndoListener() {

				@Override
				public void onUndo(Parcelable token) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DownloadListener downloadListener = new DownloadListener(MainActivity.this, song, 0);
							downloadListener.setDownloadPath(getDirectory());
							downloadListener.setUseAlbumCover(true);
							downloadListener.downloadSong(false);
						}
					});
				}
			});
			undo.show();
		} else {
			int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
			DownloadListener downloadListener = new DownloadListener(this, song, id);
			downloadListener.setDownloadPath(ClearMusicDownloaderApp.getDirectory());
			downloadListener.setUseAlbumCover(useCover);
			downloadListener.downloadSong(false);
		}
	}

	public boolean isThisSongDownloaded(AbstractSong song) {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
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
	protected ResideMenuItem[] getMenuItems() {
        ResideMenuItem[] items = new ResideMenuItem[COUNT_FRAGMENT];
		items[SEARCH_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_search, R.string.tab_search, ResideMenuItem.Types.TYPE_MENU);
		items[DOWNLOADS_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_downloads, R.string.tab_downloads, ResideMenuItem.Types.TYPE_MENU);
		items[PLAYLIST_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_playlist, R.string.tab_playlist, ResideMenuItem.Types.TYPE_MENU);
		items[LIBRARY_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_library, R.string.tab_library, ResideMenuItem.Types.TYPE_MENU);
		items[PLAYER_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_player, R.string.tab_now_plaing, ResideMenuItem.Types.TYPE_MENU);
		items[SETTINGS_FRAGMENT] = new ResideMenuItem(this, R.drawable.ic_settings, R.string.tab_download_location, ClearMusicDownloaderApp.getDirectory(), ResideMenuItem.Types.TYPE_SETTINGS);
		return items;
	}

	@Override
	protected String[] getTitlePage() {
        return getResources().getStringArray(R.array.titles);
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
		PlaybackService.get(this).stopPressed();
		hidePlayerElement();
	}

	@Override
	protected void showDialog() {
		final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(this, new DirectoryChooserDialog.ChosenDirectoryListener() {

			@Override
			public void onChosenDir(String chDir) {
				File file = new File(chDir);
				Editor editor = sp.edit();
				editor.putString(PREF_DIRECTORY, chDir);
				editor.putString(PREF_DIRECTORY_PREFIX, File.separator + file.getAbsoluteFile().getName() + File.separator);
				editor.apply();
				reDrawMenu();
			}
		});
		directoryChooserDialog.chooseDirectory();
	}

	@Override
	protected boolean isPlaying() {
		return PlaybackService.get(this).isPrepared();
	}

	@Override
	protected void showPlayerFragment() {
		Fragment fragment = getPlayerFragment();
		isBackButtonEnabled = false;
		changeFragment(fragment, true);
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
	public void setCover(Bitmap bmp) {
		if (null == bmp) {
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.def_cover_circle);
		}
		service.updatePictureNotification(bmp);
		((ImageView) findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}

	@Override
	protected void showPlayerElement(boolean flag) {
		reDrawMenu();
	}
	
	@Override
	protected void showProgress(boolean flag) {
		progress = findViewById(R.id.mini_player_progress);
		super.showProgress(flag);
		if (flag) {
			progress.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
		} else {
			progress.clearAnimation();
		}
	}

	@Override
	protected boolean isAnimationEnabled() {
		return Nulldroid_Settings.ENABLE_ANIMATIONS;
	}
	
	@Override
	protected String getDirectory() {
		return ClearMusicDownloaderApp.getDirectory();
	}

	@Override
	protected int getMiniPlayerDuplicateID() {
		return R.id.mini_player_duplicate;
	}
	
	@Override
	protected DownloadClickListener createDownloadListener(RemoteSong song) {
		return new DownloadListener(this, song, 0);
	}
	
}