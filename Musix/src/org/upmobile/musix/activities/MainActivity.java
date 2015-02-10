package org.upmobile.musix.activities;


import org.upmobile.musix.R;
import org.upmobile.musix.fragments.NavigationDrawerFragment;
import org.upmobile.musix.fragments.SearchFragment;
import org.upmobile.musix.fragments.SongsListFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private String mTitle;
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment mFragment = null;
        String lastFragmentName = "";
        setActionBarTitle(setSectionTitle(position));
        int backStackEntry = fragmentManager.getBackStackEntryCount() - 1;
        if (backStackEntry != -1) {
        	android.support.v4.app.FragmentManager.BackStackEntry backEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
			lastFragmentName = backEntry.getName();
        }
        switch (position) {

            case 0:
            	if (lastFragmentName.equals(SearchFragment.class.getSimpleName())) return;
                mFragment = new SearchFragment();
                break;
            case 1:
            	if (lastFragmentName.equals(SongsListFragment.class.getSimpleName())) return;
                mFragment = new SongsListFragment();
                break;

            case 2:
                closeApplication();
                break;
        }

        if (mFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mFragment)
                    .addToBackStack(mFragment.getClass().getSimpleName())
                    .commit();
            setSectionTitle(position);
        }
    }
    
    @Override
    protected void onStart() {
    	startService(new Intent(this, PlaybackService.class));
    	super.onStart();
    }

    private void closeApplication() {
    	if (PlaybackService.hasInstance()) {
    		PlaybackService.get(this).stop();
    	}
        this.finish();
    }

    public String setSectionTitle(int number) {
        switch (number) {
            case 0:
                setmTitle(getString(R.string.menu_search));
                break;

            case 1:
                setmTitle(getString(R.string.menu_songs));
                break;
        }
       return getmTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
    
	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			finish();
			return;
		}
		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, R.string.doubleBackToExit, Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, 2000);
	}
    
    public void setActionBarTitle(String title) {
    	getSupportActionBar().setTitle(title);
    }

	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}
}
