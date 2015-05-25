package org.upmobile.materialmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;
import org.upmobile.materialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.materialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.materialmusicdownloader.fragment.PlaylistFragment;
import org.upmobile.materialmusicdownloader.fragment.SearchFragment;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.FileObserver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.widget.ImageView;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.font.MusicTextView;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class MainActivity extends UIMainActivity implements Constants, FolderSelectCallback {

	private final String folderPath = MaterialMusicDownloaderApp.getDirectory();
	private int currentFragmentID;

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
		File file = new File(folderPath);
		if (!file.exists()) file.mkdirs();
		fileObserver.startWatching();
//		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}

	@Override
	protected ArrayList<BaseMaterialFragment> getFragments() {
		ArrayList<BaseMaterialFragment> fragments = new ArrayList<BaseMaterialFragment>();
		fragments.add(new SearchFragment());
		fragments.add(new DownloadsFragment());
		fragments.add(new PlaylistFragment());
		fragments.add(new LibraryFragment());
		fragments.add(new PlayerFragment());
		return fragments;
	}
	
	@Override
	protected void clickOnSearchView(String message) {
		changeFragment(new SearchFragment(message), false);
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
		super.onBackPressed();
		if (hadClosedDraver) {
			hadClosedDraver = false;
			return;
		}
		boolean isDraverClose = isDraverClosed();
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		isEnabledFilter = false;
		currentFragmentIsPlayer = false;
		if (null != player && player.isVisible()) {
			if (isDraverClose) {
				service.stopPressed();
				stopService(new Intent(this, PlaybackService.class));
				finish();
			} else {
				showMiniPlayer(service.isEnqueueToStream());
				getFragmentManager().popBackStack();
			}
		} else if (currentFragmentID == LIBRARY_FRAGMENT){
			Class<? extends AbstractSong> current = PlaybackService.get(this).getPlayingSong().getClass();
			Fragment fragment;
			if (current == MusicData.class) {
				fragment = new LibraryFragment();
				currentFragmentID = LIBRARY_FRAGMENT;
			} else {
				setVisibleSearchView(false);
				fragment = new SearchFragment();
				currentFragmentID = SEARCH_FRAGMENT;
			}
			getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName()).commit();
		} else {
			if (null != service && isMiniPlayerPrepared()) {
				service.stopPressed();
				showPlayerElement(false);
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
		undoBar.show(false);
	}
	
	public void showMessage(int message) {
		showMessage(getString(message));
	}
	
	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image) {
			return property < 0 ? null : getDefaultBitmapCover(outWidth, outHeight, property, image, 0);
	}
	
	public Bitmap getDefaultBitmapCover(int outWidth, int outHeight, int property, String image, int customColor) {
		MusicTextView textCover = new MusicTextView(this);
		textCover.setText(image);
		textCover.setTextColor(getResources().getColor(customColor == 0 ? R.color.main_color_500 : customColor));
		Rect bounds = new Rect();
		Paint textPaint = textCover.getPaint();
		textPaint.getTextBounds(image, 0, image.length(), bounds);
		int height = bounds.height();
		int width = bounds.width();
		if (height < property && width < property) {
			textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, property) + 1f);
			bounds = new Rect();
			textPaint = textCover.getPaint();
			textPaint.getTextBounds(image, 0, image.length(), bounds);
			height = bounds.height();
			width = bounds.width();
		} else {
			textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, Util.pixelsToSp(this, property) - 1f);
			bounds = new Rect();
			textPaint = textCover.getPaint();
			textPaint.getTextBounds(image, 0, image.length(), bounds);
			height = bounds.height();
			width = bounds.width();
		}
		return Util.textViewToBitmap(textCover, outWidth, outHeight);
	}
	
	@Override
	protected boolean isAnimationEnabled() {
		return Nulldroid_Settings.ENABLE_ANIMATIONS;
	}

	@Override
	public String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
	}
	
	@Override
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
	public void setCover(Bitmap bmp) {
		if (null == bmp) {
			String cover = getString(R.string.font_musics);
			bmp = getDefaultBitmapCover(64, 62, 60, cover);
		}
		service.updatePictureNotification(bmp);
		((ImageView)findViewById(R.id.mini_player_cover)).setImageBitmap(bmp);
	}
	
	@Override
	protected void setPlayPauseMini(boolean playPayse) {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 46), Util.dpToPx(this, 46), Util.dpToPx(this, 45), playPayse ? getString(R.string.font_pause_mini) : getString(R.string.font_play_mini));
		((ImageView) findViewById(R.id.mini_player_play_pause)).setImageBitmap(bmp);
	}
	
	@Override
	protected void setImageDownloadButton() {
		Bitmap bmp = getDefaultBitmapCover(Util.dpToPx(this, 46), Util.dpToPx(this, 46), Util.dpToPx(this, 45), getString(R.string.font_download_button));
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
		editor.commit();
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
	
	@Override
	public void onQueryTextSubmitAct(String query) {
		String lastFragmentName = getPreviousFragmentName(1);
		if (lastFragmentName.equals(LibraryFragment.class.getSimpleName())) {
			LibraryFragment fragment = (LibraryFragment) getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		} else if(lastFragmentName.equals(PlaylistFragment.class.getSimpleName())){
			PlaylistFragment fragment = (PlaylistFragment) getFragmentManager().findFragmentByTag(PlaylistFragment.class.getSimpleName());
			if (fragment.isVisible()) {
				if (query.isEmpty()) {
					fragment.clearFilter();
				} else {
					fragment.setFilter(query);
				}
			}
		}
	}

	@Override
	public void onQueryTextChangeAct(String newText) {
		if ("".equals(newText)) {
			String fragmentName =  getPreviousFragmentName(1);
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
	}
	
	@Override
	protected void showPlayerFragment() {
		setDrawerEnabled(false);
		onNavigationDrawerItemSelected(PLAYER_FRAGMENT);
	}
	
	@Override
	public int getSettingsIcon() {
		return R.string.font_play_settings;
	}
	
	@Override
	protected int getFakeViewID() {
		return R.id.fake_view;
	}

	@Override
	protected Bitmap getSearchActinBarIcon() {
		return getDefaultBitmapCover(Util.dpToPx(this, 24), Util.dpToPx(this, 24), Util.dpToPx(this, 22), getResources().getString(R.string.font_search_online), R.color.main_color_for_search_fragment_text);
	}
	
	@Override
	protected Bitmap getCloseActinBarIcon() {
		return getDefaultBitmapCover(Util.dpToPx(this, 16), Util.dpToPx(this, 16), Util.dpToPx(this, 14), getResources().getString(R.string.font_cancel),  R.color.main_color_for_search_fragment_text);
	}
	
	@Override
	protected void download(RemoteSong song) {
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		DownloadListener downloadListener = new DownloadListener(this, song, id);
		downloadListener.setDownloadPath(getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
	}

	@Override
	protected int getMiniPlayerDuplicateID() {
		return R.id.mini_player_duplicate;
	}
	
	public boolean isPlayerFragment() {
		FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
		return backEntry.getName().equals(PlayerFragment.class.getSimpleName());
	}
	
	@Override
	protected DownloadClickListener createDownloadListener(RemoteSong song) {
		return new DownloadListener(this, song, 0);
	}
	
}