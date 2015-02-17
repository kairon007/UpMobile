package org.upmobile.materialmusicdownloader.activity;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.materialmusicdownloader.fragment.LibraryFragment;
import org.upmobile.materialmusicdownloader.fragment.PlayerFragment;
import org.upmobile.materialmusicdownloader.fragment.SearchFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.MusicData;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;

import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;

public class MainActivity extends UIMainActivity implements Constants {

	private final String ARRAY_SAVE = "extras_array_save";
	private final String folderPath = Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;
	private PlaybackService service;
	private int currentFragmentID;

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
	
	public void changeFragment(Fragment targetFragment) {
		getFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, targetFragment, targetFragment.getClass().getSimpleName())
		.addToBackStack(targetFragment.getClass().getSimpleName())
		.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		.commit();
	}
	
	@Override
	public void onBackPressed() {
		Fragment player = getFragmentManager().findFragmentByTag(PlayerFragment.class.getSimpleName());
		if (null != player && player.isVisible()) {
			getFragmentManager().popBackStack();
		} else if (currentFragmentID == 3){
			Class<? extends AbstractSong> current = PlaybackService.get(this).getPlayingSong().getClass();
			Fragment fragment;
			if (current == MusicData.class) {
				fragment = new LibraryFragment();
				currentFragmentID = 2;
			} else {
				fragment = new SearchFragment();
				currentFragmentID = 0;
			}
			getFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
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
		setAdapter(flag);
	}
	
	@Override	
	protected void selectItem(int position, int drawerTag) {
		currentFragmentID = position;
		super.selectItem(position, drawerTag);
		
	}
	
}