package com.csform.android.uiapptemplate;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.csform.android.uiapptemplate.fragment.NavigationDrawerFragment;
import com.csform.android.uiapptemplate.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.model.DrawerItem;

public abstract class UIMainActivity extends Activity implements NavigationDrawerCallbacks {
	
	private List<BaseMaterialFragment> mFragments;
	private List<DrawerItem> mDrawerItems;

	private CharSequence mTitle;
	
	private String query = null;
	
	private SearchView searchView;

	private NavigationDrawerFragment navigationDrawerFragment;

	protected abstract <T extends BaseMaterialFragment> ArrayList<T> getFragments();
	
	protected void clickOnSearchView(String message) {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragments = getFragments();
		setContentView(R.layout.activity_material_main);
		navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 getMenuInflater().inflate(R.menu.main, menu);
		 MenuItem searchItem = menu.findItem(R.id.action_search);
		 searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
		 searchView.setQueryHint(getResources().getString(R.string.hint_main_search));
		 searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				UIMainActivity.this.query = query;
				searchView.setIconified(true);
				searchView.setIconified(true);
				hideKeyboard();
				changeFragment(mFragments.get(0), null);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		View view  = findViewById(R.id.drawer_layout);
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	    int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			searchView.setIconified(false);// to Expand the SearchView when clicked
			return true; 
		}    
		return false;
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null != searchView) imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	}
	
	
	public void setAdapter(boolean isNowPlaying) {
		mDrawerItems = new ArrayList<DrawerItem>();
		mFragments = getFragments();
		int lenght = mFragments.size();
		for(int i=0; i<lenght; i++) {
			if (!isNowPlaying && (i == lenght-1)) break;
			BaseMaterialFragment fragment = mFragments.get(i);
			mDrawerItems.add(new DrawerItem(fragment.getDrawerIcon(), fragment.getDrawerTitle(), fragment.getDrawerTag()));
		}
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
	
	public ArrayList<BaseMaterialFragment> getItems(){
		return getFragments();
	}
	
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		hideKeyboard();
		switch (position) {
		case 0:
	        changeFragment(mFragments.get(0), null);
			break;
		case 1:
	        changeFragment(mFragments.get(1), null);
			break;
		case 2:
	        changeFragment(mFragments.get(2), null);
			break;
		case 3:
			android.app.FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1);
			String lastFragmentName = backEntry.getName();
	    	BaseMaterialFragment fragment = (BaseMaterialFragment) mFragments.get(3);
		    if (!lastFragmentName.equals(fragment.getClass().getSimpleName())) {
		    	changeFragment(fragment, null);
		    }
			break;
		default:
			break;
		}
	}
	
	public void changeFragment(BaseMaterialFragment baseMaterialFragment, Bundle args) {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, (Fragment) baseMaterialFragment, baseMaterialFragment.getClass().getSimpleName())
		.addToBackStack(baseMaterialFragment.getClass().getSimpleName())
		.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		.commit();
	}
	
	public void addPlayerElement(boolean flag) {
		if (null == navigationDrawerFragment) return;
		navigationDrawerFragment.setAdapter(flag);
	}
	
	public void setDrawerEnabled(boolean isEnabled) {
		navigationDrawerFragment.setEnabled(isEnabled);
	}

	public String getQuery() {
		return query;
	}
	
	public void setSelectedItem(int position) {
		if (null != navigationDrawerFragment) {
			navigationDrawerFragment.setSelectedItem(position);
		}
	}

}