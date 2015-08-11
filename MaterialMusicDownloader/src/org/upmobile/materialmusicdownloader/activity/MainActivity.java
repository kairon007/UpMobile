package org.upmobile.materialmusicdownloader.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.upmobile.materialmusicdownloader.BaseDownloadListener;
import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;
import org.upmobile.materialmusicdownloader.font.MusicTextView;
import org.upmobile.materialmusicdownloader.fragment.ArtistFragment;
import org.upmobile.materialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.materialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.materialmusicdownloader.fragment.NavigationDrawerFragment;
import org.upmobile.materialmusicdownloader.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import org.upmobile.materialmusicdownloader.fragment.NavigationDrawerFragment.OnNavigationDrawerState;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.materialmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.materialmusicdownloader.fragment.SearchFragment;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import java.io.File;

import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoBar;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class MainActivity extends BaseMiniPlayerActivity implements FolderSelectCallback, NavigationDrawerCallbacks, Constants {

	private int currentFragmentId = -1;
	private CharSequence mTitle;
	private NavigationDrawerFragment navigationDrawerFragment;
	private boolean isVisibleSearchView = false;
	protected boolean isEnabledFilter = false;
	private int currentPosition = -1;
	private Bitmap defaultCover;

	private FileObserver fileObserver = new FileObserver(MaterialMusicDownloaderApp.getDirectory()) {

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.DELETE_SELF) {
				File file = new File(MaterialMusicDownloaderApp.getDirectory());
				file.mkdirs();
				getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns.DATA + " LIKE '" + MaterialMusicDownloaderApp.getDirectory() + "%'", null);
				getContentResolver().notifyChange(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null);
			}
		}
	};
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_main);
		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		navigationDrawerFragment.setDrawerState(new OnNavigationDrawerState() {

			@Override
			public void onDrawerOpen() {
				navigationDrawerOpened();
			}
		});
		File file = new File(MaterialMusicDownloaderApp.getDirectory());
		if (!file.exists())
			file.mkdirs();
		fileObserver.startWatching();
		// Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}

	private void configCurrentId (BaseMaterialFragment baseMaterialFragment) {
		if (baseMaterialFragment.getClass() == SearchFragment.class) {
			currentFragmentId = SEARCH_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == DownloadsFragment.class){
			currentFragmentId = DOWNLOADS_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == LibraryFragment.class){
			currentFragmentId = SONGS_FRAGMENT;
		}else if (baseMaterialFragment.getClass() == PlaylistFragment.class){
			currentFragmentId = PLAYLIST_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == PlayerFragment.class){
			currentFragmentId = PLAYER_FRAGMENT;
		} else if (baseMaterialFragment.getClass() == ArtistFragment.class) {
			currentFragmentId = ARTIST_FRAGMENT;
		} else {
			currentFragmentId = SEARCH_FRAGMENT;
		}
	}

	public void changeFragment(BaseMaterialFragment baseMaterialFragment, boolean isAnimate) {
		changeFragment(baseMaterialFragment, isAnimate, null);
	}

	public void changeFragment(BaseMaterialFragment baseMaterialFragment, boolean isAnimate, AbstractSong song) {
		configCurrentId(baseMaterialFragment);
		isEnabledFilter = false;
		if (((Fragment) baseMaterialFragment).isAdded()) {
			((Fragment) baseMaterialFragment).onResume();
		}
		if (baseMaterialFragment.getClass() == LibraryFragment.class) {
			Bundle b = new Bundle();
			b.putParcelable("KEY_SELECTED_SONG", song);
			((Fragment) baseMaterialFragment).setArguments(b);
		}
		FragmentTransaction tr = getFragmentManager().beginTransaction();
		if (isAnimate && isAnimationEnabled()) {
			tr.setCustomAnimations(R.anim.fragment_slide_in_up, R.anim.fragment_slide_out_up, R.anim.fragment_slide_in_down, R.anim.fragment_slide_out_down);
		}
		tr.replace(R.id.content_frame, (Fragment) baseMaterialFragment, baseMaterialFragment.getClass().getSimpleName()).addToBackStack(baseMaterialFragment.getClass().getSimpleName()).commit();
	}

	protected String getPreviousFragmentName(int position) {
		if (getFragmentManager().getBackStackEntryCount() < position) return SearchFragment.class.getSimpleName();
		FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - position);
		return backEntry.getName();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view = findViewById(R.id.drawer_layout);
		Util.hideKeyboard(this, view);
		return false;
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		if (position == currentPosition && (position != SETTINGS_FRAGMENT && position != 7)) {
			return;
		}
		if (position == PLAYER_FRAGMENT) {
			showMiniPlayer(false);
		} else if (position <= SONGS_FRAGMENT) {
			showMiniPlayer(null != service && service.isEnqueueToStream());
		}
		getSupportActionBar().setElevation(position == SEARCH_FRAGMENT ? 0 : position != SETTINGS_FRAGMENT && position != 6 ? 16 : 0);
		currentFragmentIsPlayer = false;
		switch (position) {
			case SEARCH_FRAGMENT:
				changeFragment(new SearchFragment(), false);
				break;
			case DOWNLOADS_FRAGMENT:
				changeFragment(new DownloadsFragment(), false);
				break;
			case PLAYLIST_FRAGMENT:
				changeFragment(new PlaylistFragment(), false);
				break;
			case ARTIST_FRAGMENT:
				changeFragment(new ArtistFragment(), false);
				break;
			case SONGS_FRAGMENT:
				changeFragment(new LibraryFragment(), false);
				break;
			case PLAYER_FRAGMENT:
				android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
				String lastFragmentName = backEntry.getName();
				currentFragmentIsPlayer = true;
				BaseMaterialFragment fragment = new PlayerFragment();
				if (!lastFragmentName.equals(fragment.getClass().getSimpleName())) {
					changeFragment(fragment, true);
				}
				break;
			case SETTINGS_FRAGMENT:
			case 7:
				showDialog();
				break;
			default:
				break;
		}
		currentPosition = position;
	}

	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
	}

	@Override
	protected void checkOnStart(boolean showMiniPlayer) {
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		super.checkOnStart(null == player || !player.isVisible());
	}

	@Override
	public void onBackPressed() {
		if (getPreviousFragmentName(2).equals(SearchFragment.class.getSimpleName())) {
			getSupportActionBar().setElevation(0);
		}
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (navigationDrawerFragment.isVisible()) {
			navigationDrawerFragment.closeDrawer();
		} else if (null != player && player.isVisible()) {
			currentFragmentIsPlayer = false;
			showMiniPlayer(service.isEnqueueToStream());
			getFragmentManager().popBackStack();
			setPlayerFragmentVisible(false);
		} else {
			if (SONGS_FRAGMENT == getCurrentFragmentId() && !navigationDrawerFragment.isDrawerIndicatorEnabled()) {
				showMiniPlayer(false);
				getFragmentManager().popBackStack();
			} else if (PLAYER_FRAGMENT == getCurrentFragmentId()) {
				service.stopPressed();
				stopService(new Intent(this, PlaybackService.class));
				finish();
			} else if (null != service && isMiniPlayerPrepared()) {
				currentFragmentIsPlayer = false;
				service.stopPressed();
			} else {
				stopService(new Intent(this, PlaybackService.class));
				finish();
			}
		}
	}

	public void showPlayerElement(boolean flag) {
		addPlayerElement(flag);
	}

	public void showMessage(String message) {
		UndoBar undoBar = new UndoBar(this);
		undoBar.clear();
		undoBar.message(message);
		undoBar.style(UndoBarController.MESSAGESTYLE);
		showUndoBar(undoBar, false);
	}

	private void showUndoBar(UndoBar undoBar, boolean anim) {
		if (getMiniPlayer().getVisibility() == View.VISIBLE) {
			undoBar.show(anim, 0, 0, 0, getMiniPlayer().getHeight());
		} else {
			undoBar.show(anim);
		}
	}

	public void showMessage(int message) {
		showMessage(getString(message));
	}

	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image) {
		return property < 0 ? null : getDefaultBitmapCover(outWidth, outHeight, property, image, 0, 0, 0);
	}

	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image, int customColor, int pL, int pT) {
		MusicTextView textCover = new MusicTextView(this);
		textCover.setText(image);
		textCover.setTextColor(getResources().getColor(customColor == 0 ? R.color.main_color_500 : customColor));
		Rect bounds = new Rect();
		Paint textPaint = textCover.getPaint();
		textPaint.getTextBounds(image, 0, image.length(), bounds);
		int height = bounds.height();
		int width = bounds.width();
		boolean defaultIsLarger = true;
		while (height < property && width < property) {
			defaultIsLarger = false;
			textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, textCover.getTextSize()) + 1f);
			bounds = new Rect();
			textPaint = textCover.getPaint();
			textPaint.getTextBounds(image, 0, image.length(), bounds);
			height = bounds.height();
			width = bounds.width();
		}
		if (defaultIsLarger) {
			while (height > property && width > property) {
				textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, textCover.getTextSize()) - 1f);
				bounds = new Rect();
				textPaint = textCover.getPaint();
				textPaint.getTextBounds(image, 0, image.length(), bounds);
				height = bounds.height();
				width = bounds.width();
			}
		}
		return Util.textViewToBitmap(textCover, outWidth, outHeight);
	}

	@Override
	public void setCover(Bitmap bmp) {
		if (null == defaultCover) {
			String cover = getString(R.string.font_musics);
			defaultCover = getDefaultBitmapCover(Util.dpToPx(this,64), Util.dpToPx(this,62), Util.dpToPx(this,60), cover);
		}
		if (null ==  bmp) {
			bmp = defaultCover;
		}
		service.updatePictureNotification(bmp);
		((ImageView) findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}

	@Override
	protected void setPlayPauseMini(boolean playPayse) {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 48), Util.dpToPx(this, 48), Util.dpToPx(this, 44), playPayse ? getString(R.string.font_pause_mini) : getString(R.string.font_play_mini));
		((ImageView) findViewById(R.id.mini_player_play_pause)).setImageBitmap(bmp);
	}

	@Override
	protected void setImageDownloadButton() {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 48), Util.dpToPx(this, 48), Util.dpToPx(this, 44), getString(R.string.font_download_button));
		((ImageView) findViewById(R.id.mini_player_download)).setImageBitmap(bmp);
	}

	public void setupDownloadBtn() { // in PlayerFragment
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (null != player && player.isVisible()) {
			((PlayerFragment) player).setupDownloadButton();
		}
	}

	@Override
	public void onFolderSelection(final File folder) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putString(PREF_DIRECTORY, folder.getAbsolutePath());
		editor.putString(PREF_DIRECTORY_PREFIX, File.separator + folder.getAbsoluteFile().getName() + File.separator);
		editor.apply();
		showPlayerElement(PlaybackService.get(this).isPlaying());
		new Thread(new Runnable() {

			@Override
			public void run() {
				StateKeeper.getInstance().notifyLable(false);
				StateKeeper.getInstance().initSongHolder(folder.getAbsolutePath());
				checkDownloadingUrl(false);
				StateKeeper.getInstance().notifyLable(true);
			}
		}).start();
	}

	protected void navigationDrawerOpened() {
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			fragment.forceDelete();
		}
		if (lastFragmentName.equals(PlaylistFragment.class.getSimpleName())) {
			PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			fragment.forceDelete();
		}
		if (lastFragmentName.equals(ArtistFragment.class.getSimpleName())) {
			ArtistFragment fragment = (ArtistFragment) getFragmentManager().findFragmentByTag(ArtistFragment.class.getSimpleName());
			fragment.forceDelete();
		}
	}

	@Override
	protected void showPlayerFragment() {
		setDrawerEnabled(false);
		onNavigationDrawerItemSelected(PLAYER_FRAGMENT);
	}

	public boolean isThisSongDownloaded(AbstractSong song) {
		int state = StateKeeper.getInstance().checkSongInfo(song.getComment());
		return !(song.getClass() != MusicData.class && StateKeeper.DOWNLOADED != state);
	}

	@Override
	public void download(final RemoteSong song) {
		if (isThisSongDownloaded(song)) {
			UndoBarController.clear(this);
			UndoBar undo = new UndoBar(this);
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
							BaseDownloadListener downloadListener = new BaseDownloadListener(MainActivity.this, song, 0, true);
							downloadListener.setDownloadPath(getDirectory());
							downloadListener.setUseAlbumCover(true);
							downloadListener.downloadSong(false);
						}
					});
				}
			});
			showUndoBar(undo, true);
		} else {
			BaseDownloadListener downloadListener = new BaseDownloadListener(this, song, 0, true);
			downloadListener.setDownloadPath(getDirectory());
			downloadListener.setUseAlbumCover(true);
			downloadListener.downloadSong(false);
		}
	}

	public boolean isPlayerFragment() {
		FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
		return backEntry.getName().equals(PlayerFragment.class.getSimpleName());
	}

	@Override
	protected BaseDownloadSongTask createDownloadListener(RemoteSong song) {
		return new BaseDownloadListener(this, song, 0, true);
	}
	
	public void addPlayerElement(boolean flag) {
		if (null == navigationDrawerFragment) return;
		navigationDrawerFragment.setAdapter(flag);
	}

	public void setDrawerEnabled(boolean isEnabled) {
		navigationDrawerFragment.setEnabled(isEnabled);
	}

	protected boolean isDraverClosed() {
		return navigationDrawerFragment.isDrawerIndicatorEnabled();
	}

	public void setSelectedItem(int position) {
		currentPosition = position;
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setSelectedItem(position);
		}
		setCurrentFragmentId(position);
	}

	@Override
	protected int getMiniPlayerDuplicateID() {
		return R.id.mini_player_duplicate;
	}
	
	public int getSettingsIcon() {
		return R.string.font_play_settings;
	}

	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}
	
	@Override
	protected boolean isAnimationEnabled() {
		return Nulldroid_Settings.ENABLE_ANIMATIONS;
	}

	@Override
	public String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
	}

	public void showDialog() {
		new FolderSelectorDialog().show(this);
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
	public void setTitle(int titleId) {
		setTitle(getString(titleId));
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		navigationDrawerFragment.setTitle(mTitle);
	}

	public int getCurrentFragmentId() {	return currentFragmentId; }

	public void setCurrentFragmentId(int currentFragmentId) {
		this.currentFragmentId = currentFragmentId;
	}
}