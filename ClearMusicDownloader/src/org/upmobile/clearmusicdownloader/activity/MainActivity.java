package org.upmobile.clearmusicdownloader.activity;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.PlayerFragment;
import org.upmobile.clearmusicdownloader.fragment.SearchFragment;
import org.upmobile.clearmusicdownloader.service.PlayerService;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenuItem;

public class MainActivity extends BaseClearActivity {

	private Fragment[] fragments;
	private ResideMenuItem[] items;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		PlayerService.get(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Fragment[] getFragments() {
		if (PlayerService.hasInstance() && PlayerService.get(this).isPrepared()) {
			fragments = new Fragment[4];
			fragments[3] = new PlayerFragment();
		} else {
			fragments = new Fragment[3];
		}
		fragments[0] = new SearchFragment();
		fragments[1] = new DownloadsFragment();
		fragments[2] = new LibraryFragment();
		return fragments;
	}

	@Override
	protected ResideMenuItem[] getMenuItems() {
		if (PlayerService.hasInstance() && PlayerService.get(this).isPrepared()) {
			items = new ResideMenuItem[4];
			items[3] = new ResideMenuItem(this, R.drawable.ic_player, R.string.navigation_player);
		} else {
			items = new ResideMenuItem[3];
		}
		items[0] = new ResideMenuItem(this, R.drawable.ic_search, R.string.navigation_search);
		items[1] = new ResideMenuItem(this, R.drawable.ic_downloads, R.string.navigation_downloads);
		items[2] = new ResideMenuItem(this, R.drawable.ic_library, R.string.navigation_library);
		return items;
	}
	
	@Override
	public void showTopFrame() {
		super.showTopFrame();
	}
	
	@Override
	public void hideTopFrame() {
		super.hideTopFrame();
	}
}