package org.upmobile.materialmusicdownloader.fragment;


import java.util.ArrayList;
import java.util.List;

import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.DrawerAdapter;
import org.upmobile.materialmusicdownloader.models.BaseMaterialFragment;
import org.upmobile.materialmusicdownloader.models.DrawerItem;

import ru.johnlife.lifetoolsmp3.Constants;
import ru.johnlife.lifetoolsmp3.Util;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.view.Display;
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
    private OnNavigationDrawerState drawerState;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private List<BaseMaterialFragment> mFragments;
   
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private DrawerAdapter mAdapter;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private int previousSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
	private List<DrawerItem> mDrawerItems;

    public NavigationDrawerFragment() { }
    
    public interface OnNavigationDrawerState {
    	public void onDrawerOpen();
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
                } else {
                	mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
                }
            }
        });
        setAdapter(false);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }
    
	private ArrayList<BaseMaterialFragment> getFragments() {
		ArrayList<BaseMaterialFragment> fragments = new ArrayList<BaseMaterialFragment>();
		fragments.add(new SearchFragment());
		fragments.add(new DownloadsFragment());
		fragments.add(new PlaylistFragment());
		fragments.add(new LibraryFragment());
		fragments.add(new PlayerFragment());
		return fragments;
	}

	public void setAdapter(boolean isNowPlaying) {
		mDrawerItems = new ArrayList<DrawerItem>();
		mFragments = getFragments();
		int lenght = mFragments.size();
		for(int i=0; i<lenght; i++) {
			if (!isNowPlaying && (i == lenght - 1)) break;
			BaseMaterialFragment fragment = mFragments.get(i);
			mDrawerItems.add(new DrawerItem(fragment.getDrawerIcon(), fragment.getDrawerTitle(), fragment.getDrawerTag(), DrawerItem.Types.TYPE_MENU));
		}
		mDrawerItems.add(new DrawerItem(R.string.tab_download_location, DrawerItem.Types.TYPE_SECTION));
		mDrawerItems.add(new DrawerItem(((MainActivity) getActivity()).getSettingsIcon(), ((MainActivity) getActivity()).getDirectory(), DrawerItem.Types.TYPE_SETTING ));
		mAdapter = new DrawerAdapter(getActivity(), mDrawerItems, true);
		mDrawerListView.setAdapter(mAdapter);
		setSelectedItem(mCurrentSelectedPosition);
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
        int maxWidth = Util.dpToPx(mFragmentContainerView.getContext(), 320);
        layoutParams.width = width > maxWidth ? maxWidth : width;
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        android.support.v7.app.ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.main_color_500)));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
	        mDrawerToggle = new android.support.v7.app.ActionBarDrawerToggle(
	                getActivity(),                    /* host Activity */
	                mDrawerLayout,                    /* DrawerLayout object */
	                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
	                R.string.navigation_drawer_close) {

				@Override
				public void onDrawerOpened(View drawerView) {
					if (null != drawerState) {
						drawerState.onDrawerOpen();
					}
					super.onDrawerOpened(drawerView);
				}
			}; /* "close drawer" description for accessibility */
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
            	mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    
    public boolean isDrawerIndicatorEnabled() {
		return mDrawerToggle.isDrawerIndicatorEnabled();
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
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if ( mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }
    
    public void setEnabled(boolean enabled) {
    	if (!isAdded()) return;
	    mDrawerToggle.setDrawerIndicatorEnabled(enabled);
	    mDrawerToggle.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_ab_up_compat_trim));
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
		if (mDrawerItems.get(position).getType().equals(DrawerItem.Types.TYPE_SETTING)) {
			position = mCurrentSelectedPosition = previousSelectedPosition;
		}
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
    
	public OnNavigationDrawerState getDrawerState() {
		return drawerState;
	}

	public void setDrawerState(OnNavigationDrawerState drawerState) {
		this.drawerState = drawerState;
	}
    
}