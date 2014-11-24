package org.upmobile.clearmusicdownloader.activity;

import org.upmobile.clearmusicdownloader.R;
import org.upmobile.clearmusicdownloader.fragment.DownloadsFragment;
import org.upmobile.clearmusicdownloader.fragment.LibraryFragment;
import org.upmobile.clearmusicdownloader.fragment.SearchFragment;

import android.support.v4.app.Fragment;

import com.special.BaseClearActivity;
import com.special.menu.ResideMenuItem;

public class MainActivity extends BaseClearActivity {

	@Override
	protected Fragment[] getFragments() {
		Fragment[] fragments = new Fragment[3];
		fragments[0] = new SearchFragment();
		fragments[1] = new DownloadsFragment();
		fragments[2] = new LibraryFragment();
		return fragments;
	}

	@Override
	protected ResideMenuItem[] getMenuItems() {
		ResideMenuItem[] items = new ResideMenuItem[3];
		items[0] = new ResideMenuItem(this, R.drawable.ic_search, R.string.navigation_search);
		items[1] = new ResideMenuItem(this, R.drawable.ic_downloads, R.string.navigation_downloads);
		items[2] = new ResideMenuItem(this, R.drawable.ic_library, R.string.navigation_library);
		return items;
	}
}