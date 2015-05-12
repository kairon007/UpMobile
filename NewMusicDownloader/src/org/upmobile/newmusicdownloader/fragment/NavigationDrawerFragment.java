package org.upmobile.newmusicdownloader.fragment;


import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.DrawerItem;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.adapter.NavigationAdapter;
import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

public class NavigationDrawerFragment extends Fragment implements Constants {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private NavigationAdapter mAdapter;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private int previousSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
        if (savedInstanceState != null) {
            previousSelectedPosition = mCurrentSelectedPosition;
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
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
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
		if (null == getActivity()) {
			return;
		}
		boolean isWhiteTheme = ((MainActivity) getActivity()).isWhiteTheme(getActivity());
		String library = getActivity().getString(R.string.tab_library);
		String download = getActivity().getString(R.string.tab_downloads);
		String search = getActivity().getString(R.string.tab_search);
		String nowPlaying = getActivity().getString(R.string.tab_now_plaing);
		String playlist = getActivity().getString(R.string.playlist);
		ArrayList<DrawerItem> items = new ArrayList<DrawerItem>();
		items.add(new DrawerItem(isWhiteTheme ? R.drawable.navigation_search_black : R.drawable.navigation_search, search, DrawerItem.Types.TYPE_MENU));
		items.add(new DrawerItem(isWhiteTheme ? R.drawable.navigation_downloads_black : R.drawable.navigation_downloads, download, DrawerItem.Types.TYPE_MENU));
		items.add(new DrawerItem(isWhiteTheme ? R.drawable.navigation_playlist_black : R.drawable.navigation_playlist, playlist, DrawerItem.Types.TYPE_MENU));
		items.add(new DrawerItem(isWhiteTheme ? R.drawable.navigaion_library_black : R.drawable.navigaion_library, library, DrawerItem.Types.TYPE_MENU));
		if (isNowPlaying) items.add(new DrawerItem(isWhiteTheme ? R.drawable.navigation_player_black : R.drawable.navigation_player, nowPlaying, DrawerItem.Types.TYPE_MENU));
		items.add(new DrawerItem(getActivity().getResources().getString(R.string.tab_download_location), DrawerItem.Types.TYPE_SECTION));
		items.add(new DrawerItem(isWhiteTheme ? R.drawable.navigation_settings_black :  R.drawable.navigation_settings, NewMusicDownloaderApp.getDirectory(), DrawerItem.Types.TYPE_SETTING));
		mAdapter = new NavigationAdapter(getActivity(), items);
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
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                ((MainActivity) getActivity()).isDarkActionBar(getActivity()) ? R.drawable.ic_drawer_compat : R.drawable.ic_drawer_compat_black,      /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
            	if (!isVisibleSettings()) {
            		mDrawerListView.setItemChecked(mCurrentSelectedPosition, true); //	
            	}
                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
            }

        };
        
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        previousSelectedPosition = mCurrentSelectedPosition;
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
        	if (!isVisibleSettings()) {
        		mDrawerListView.setItemChecked(position, true); //	
        	} else {
        		setSelectedItem(previousSelectedPosition);
        	}
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
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
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }
    
    public void setEnabled(boolean enabled) {
    	mDrawerToggle.setDrawerIndicatorEnabled(enabled);
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
    	previousSelectedPosition = mCurrentSelectedPosition;
    	mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
        	mDrawerListView.setItemChecked(position, true);
        }
    }
    
    private boolean isVisibleSettings() {
		return mCurrentSelectedPosition == SETTINGS_FRAGMENT || mCurrentSelectedPosition == 6;
	}
}
