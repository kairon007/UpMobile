package org.upmobile.materialmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;
import org.upmobile.materialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.materialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.materialmusicdownloader.fragment.SearchFragment;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog;
import org.upmobile.materialmusicdownloader.ui.dialog.FolderSelectorDialog.FolderSelectCallback;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.Fragment;
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

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.font.MusicTextView;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;

public class MainActivity extends UIMainActivity implements Constants, FolderSelectCallback {

	private final String ARRAY_SAVE = "extras_array_save";
	private final String folderPath = MaterialMusicDownloaderApp.getDirectory();
	private PlaybackService service;
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
		if (null != service) {
			if (null != savedInstanceState && savedInstanceState.containsKey(ARRAY_SAVE)) {
				ArrayList<AbstractSong> list = savedInstanceState.getParcelableArrayList(ARRAY_SAVE);
				service.setArrayPlayback(list);
			}
			if (service.isPlaying()) showPlayerElement(true);
		}
		fileObserver.startWatching();
		
		Nulldroid_Advertisement.startIfNotBlacklisted(this, false);
	}

	@Override
	protected ArrayList<BaseMaterialFragment> getFragments() {
		ArrayList<BaseMaterialFragment> fragments = new ArrayList<BaseMaterialFragment>();
		fragments.add(new SearchFragment());
		fragments.add(new DownloadsFragment());
		fragments.add(new LibraryFragment());
		fragments.add(new PlayerFragment());
		return fragments;
	}
	
	@Override
	protected void clickOnSearchView(String message) {
		changeFragment(new SearchFragment(message));
	}
	
	@Override
	protected void onStart() {
		startService(new Intent(this, PlaybackService.class));
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		if (null != service && service.isPlaying()) {
			showPlayerElement(true);
		} else if (PlaybackService.hasInstance()) {
			service = PlaybackService.get(this);
		}
		super.onResume();
	}
	
	@Override
	protected void setFilter(String filter) {
		LibraryFragment fragment = (LibraryFragment)getFragmentManager().findFragmentByTag(LibraryFragment.class.getSimpleName());
		if (fragment.isVisible()) {
			if (filter.isEmpty()) {
				fragment.clearFilter();
			} else {
				fragment.setFilter(filter);
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		isEnabledFilter = false;
		if (null != player && player.isVisible()) {
			getFragmentManager().popBackStack();
		} else if (currentFragmentID == 3){
			Class<? extends AbstractSong> current = PlaybackService.get(this).getPlayingSong().getClass();
			Fragment fragment;
			if (current == MusicData.class) {
				fragment = new LibraryFragment();
				currentFragmentID = 2;
			} else {
				setVisibleSearchView(false);
				fragment = new SearchFragment();
				currentFragmentID = 0;
			}
			getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment, fragment.getClass().getSimpleName()).commit();
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
		if (service == null) {
			service = PlaybackService.get(this);
		}
		if (service.hasArray()) {
			out.putParcelableArrayList(ARRAY_SAVE, service.getArrayPlayback());
		}
	}
	
	public void showPlayerElement(boolean flag) {
		addPlayerElement(flag);
	}
	
	public void showMessage(String message) {
		AppMsg.makeText(this, message, new Style(5000, R.color.main_color_500)).show();
	}
	
	public void showMessage(int message) {
		showMessage(getString(message));
	}
	
	public Bitmap getDeafultBitmapCover(int outWidth, int outHeight, int property) {
		MusicTextView textCover = new MusicTextView(this);
		textCover.setText(getString(R.string.font_musics));
		textCover.setTextColor(getResources().getColor(R.color.main_color_500));
		Rect bounds = new Rect();
		Paint textPaint = textCover.getPaint();
		textPaint.getTextBounds(getString(R.string.font_musics), 0, getString(R.string.font_musics).length(), bounds);
		int height = bounds.height();
		while (height < property) {
			textCover.setTextSize(TypedValue.COMPLEX_UNIT_SP, height < property ?  Util.pixelsToSp(this, textCover.getTextSize()) + 1f : Util.pixelsToSp(this, textCover.getTextSize()) - 1f);
			bounds = new Rect();
			textPaint = textCover.getPaint();
			textPaint.getTextBounds(getString(R.string.font_musics), 0, getString(R.string.font_musics).length(), bounds);
			height = bounds.height();
		}
		return Util.textViewToBitmap(textCover, outWidth, outHeight);
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
		return 0;
	}

	@Override
	public void onFolderSelection(File folder) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sp.edit();
		editor.putString(PREF_DIRECTORY, folder.getAbsolutePath());
		editor.putString(PREF_DIRECTORY_PREFIX, File.separator + folder.getAbsoluteFile().getName() + File.separator);
		editor.commit();
		showPlayerElement(PlaybackService.get(this).isPlaying());
	}
	
	@Override
	public int getSettingsIcon() {
		return R.string.font_play_settings;
	}
}