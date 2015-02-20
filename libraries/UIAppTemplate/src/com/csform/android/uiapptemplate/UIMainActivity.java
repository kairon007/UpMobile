package com.csform.android.uiapptemplate;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.csform.android.uiapptemplate.adapter.DrawerAdapter;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.model.DrawerItem;

public abstract class UIMainActivity extends ActionBarActivity {
	
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	private ListView mDrawerList;
	private List<BaseMaterialFragment> mFragments;
	private List<DrawerItem> mDrawerItems;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
    private boolean mUserLearnedDrawer;
    private Toolbar toolbar;
	
	private Handler mHandler;
	
	private SearchView searchView;

	protected abstract <T extends BaseMaterialFragment> ArrayList<T> getFragments();
	
	protected void clickOnSearchView(String message) {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_main);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
		toolbar = (Toolbar) findViewById(R.id.material_toolbar);
		setSupportActionBar(toolbar);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
				R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
				hideKeyboard();
				
			}

			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
				hideKeyboard();
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mTitle = mDrawerTitle = getTitle();
		mDrawerList = (ListView) findViewById(R.id.list_view);
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		setAdapter(false);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		mHandler = new Handler();
		
		if (savedInstanceState == null) {
			int position = 0;
			selectItem(position, mDrawerItems.get(position).getTag());
			if (!mUserLearnedDrawer) {
				mUserLearnedDrawer = true;
                sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				mDrawerLayout.openDrawer(mDrawerList); // open drawer on first load
			}
		}
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
				clickOnSearchView(query);
				searchView.setIconified(true);
				searchView.setIconified(true);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String arg0) {
				return false;
			}
		});
	    return super.onCreateOptionsMenu(menu);
	}
	
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			searchView.setIconified(false);
			searchView.setIconifiedByDefault(false);
			return super.onOptionsItemSelected(item);
		}
	    return false;
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
		mDrawerList.setAdapter(new DrawerAdapter(this, mDrawerItems, true));
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			hideKeyboard();
			selectItem(position, mDrawerItems.get(position).getTag());
		}
	}
	
	public void setToolbarIcon(boolean b) {
		//TODO
		if (b) {// from player
//			toolbar.setNavigationIcon(drawableId);
//			getSupportActionBar().setDisplayShowTitleEnabled(false);
//			getSupportActionBar().setDisplayShowHomeEnabled(true);
			mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_close);
//			mDrawerToggle.setDrawerIndicatorEnabled(false);
		} else {
//			toolbar = (Toolbar) findViewById(R.id.material_toolbar);
//			mDrawerToggle.setDrawerIndicatorEnabled(true);
			mDrawerToggle.setHomeAsUpIndicator(null);
//			getSupportActionBar().setDisplayShowHomeEnabled(false);
//			getSupportActionBar().setDisplayShowTitleEnabled(false);
		}
	}

	protected void selectItem(int position, int drawerTag) {
		
		Fragment fragment = getFragmentByDrawerTag(drawerTag);
		commitFragment(fragment);
		
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItems.get(position).getTitle());
		mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	private Fragment getFragmentByDrawerTag(int drawerTag) {
		for (BaseMaterialFragment fragment : mFragments) {
			if (fragment.getDrawerTag() == drawerTag) return (Fragment) fragment;
		}
		return null;
	}
	
	private class CommitFragmentRunnable implements Runnable {

		private Fragment fragment;
		
		public CommitFragmentRunnable(Fragment fragment) {
			this.fragment = fragment;
		}
		
		@Override
		public void run() {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment)
					.commit();
		}
	}
	
	public void commitFragment(Fragment fragment) {
		//Using Handler class to avoid lagging while
		//committing fragment in same time as closing
		//navigation drawer
		mHandler.post(new CommitFragmentRunnable(fragment));
	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getString(titleId));
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}