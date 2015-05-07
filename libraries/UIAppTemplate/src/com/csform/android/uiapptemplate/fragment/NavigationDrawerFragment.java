package com.csform.android.uiapptemplate.fragment;


import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.Util;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.csform.android.uiapptemplate.R;
import com.csform.android.uiapptemplate.UIMainActivity;
import com.csform.android.uiapptemplate.adapter.DrawerAdapter;
import com.csform.android.uiapptemplate.model.BaseMaterialFragment;
import com.csform.android.uiapptemplate.model.DrawerItem;

public class NavigationDrawerFragment extends Fragment implements Constants {
	
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private NavigationDrawerCallbacks mCallbacks;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private android.support.v4.app.ActionBarDrawerToggle drawerToggle;
    private List<BaseMaterialFragment> mFragments;
   
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private DrawerAdapter mAdapter;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private int previousSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
	private ArrayList<DrawerItem> mDrawerItems;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.list_view, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            	imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            	DrawerItem item = (DrawerItem) mAdapter.getItem(position);
                if (item.getType() != DrawerItem.Types.TYPE_SECTION) {
                	selectItem(position);
                }
            }
        });
        setAdapter(false);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

	public void setAdapter(boolean isNowPlaying) {
		mDrawerItems = new ArrayList<DrawerItem>();
		mFragments = ((UIMainActivity) getActivity()).getItems();
		int lenght = mFragments.size();
		for(int i=0; i<lenght; i++) {
			if (!isNowPlaying && (i == lenght - 1)) break;
			BaseMaterialFragment fragment = mFragments.get(i);
			mDrawerItems.add(new DrawerItem(fragment.getDrawerIcon(), fragment.getDrawerTitle(), fragment.getDrawerTag(), DrawerItem.Types.TYPE_MENU));
		}
		mDrawerItems.add(new DrawerItem(R.string.tab_download_location, DrawerItem.Types.TYPE_SECTION));
		mDrawerItems.add(new DrawerItem(((UIMainActivity) getActivity()).getSettingsIcon(), ((UIMainActivity) getActivity()).getDirectory(), DrawerItem.Types.TYPE_SETTING ));
		mAdapter = new DrawerAdapter(getActivity(), mDrawerItems, true);
		mDrawerListView.setAdapter(mAdapter);
	}
	
    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    
    public void closeDrawer() {
    	mDrawerLayout.closeDrawers();
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    @SuppressWarnings("deprecation")
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        ViewGroup.LayoutParams layoutParams = mFragmentContainerView.getLayoutParams();
        Display display = ((Activity) mFragmentContainerView.getContext()).getWindowManager().getDefaultDisplay(); 
        int width = display.getWidth();
        width = width - Util.dpToPx(mFragmentContainerView.getContext(), 56);
        layoutParams.width = width > 320 ? 320 : width;
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        android.support.v7.app.ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.main_color_500)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        
        if (useOldToggle()) {
	        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(
	                getActivity(),                    /* host Activity */
	                mDrawerLayout,                    /* DrawerLayout object */
	                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
	                R.string.navigation_drawer_close);  /* "close drawer" description for accessibility */
        } else {
        	drawerToggle = new android.support.v4.app.ActionBarDrawerToggle(
	                    getActivity(),                    /* host Activity */
	                    mDrawerLayout,                    /* DrawerLayout object */
	                    R.drawable.ic_drawer_compat,             /* nav drawer image to replace 'Up' caret */
	                    R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
	                    R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
	            );
        }
        
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mUserLearnedDrawer = true;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
            	if (useOldToggle()) {
            		mDrawerToggle.syncState();
            	} else {
            		drawerToggle.syncState();
            	}
            }
        });

        mDrawerLayout.setDrawerListener(useOldToggle() ? mDrawerToggle : drawerToggle);
    }
    
    public boolean isDrawerIndicatorEnabled() {
		if (useOldToggle()) {
			return mDrawerToggle.isDrawerIndicatorEnabled();
		}
		return drawerToggle.isDrawerIndicatorEnabled();
	}

    private void selectItem(int position) {
    	mCurrentSelectedPosition = position;
    	if (null != mDrawerListView) {
    		if (position != SETTINGS_FRAGMENT && position != 6) {
        		mDrawerListView.setItemChecked(position, true);
                previousSelectedPosition = mCurrentSelectedPosition;
        	} else {
        		setSelectedItem(previousSelectedPosition);
        	}
        }
        if (null != mDrawerLayout) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (null != mCallbacks) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		if (useOldToggle()) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		} else {
			drawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (useOldToggle() && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		} else if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private android.support.v7.app.ActionBar getActionBar() {
        return ((UIMainActivity) getActivity()).getSupportActionBar();
    }
    
    public void setEnabled(boolean enabled) {
    	if (!isAdded()) return;
		if (useOldToggle()) {
	    	mDrawerToggle.setDrawerIndicatorEnabled(enabled);
	    	mDrawerToggle.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_ab_up_compat_trim));
		} else {
			drawerToggle.setDrawerIndicatorEnabled(enabled);
	    	drawerToggle.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_ab_up_compat));
		}
		mDrawerLayout.setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    	setHasOptionsMenu(enabled);
    }
    
    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
    
    public void setSelectedItem(int position){
        if (mDrawerListView != null) {
        	mDrawerListView.setItemChecked(position, true);
        }
    }
    
    public void setTitle(CharSequence mTitle) {
    	int color = getResources().getColor(R.color.main_color_for_search_fragment_text);
        String str = Integer.toHexString(color);
        String strColor =  "#"+str.substring(2);
    	getActionBar().setTitle(Html.fromHtml("<font color = " + strColor + ">" + mTitle+ "</font>"));
    }
    
    private boolean useOldToggle() {
		return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
    }
}
