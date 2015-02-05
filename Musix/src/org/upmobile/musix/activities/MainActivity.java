package org.upmobile.musix.activities;


import org.upmobile.musix.R;
import org.upmobile.musix.fragments.NavigationDrawerFragment;
import org.upmobile.musix.fragments.SearchFragment;
import org.upmobile.musix.fragments.SongsListFragment;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import android.app.FragmentManager.BackStackEntry;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mNavigationDrawerFragment = (NavigationDrawerFragment)getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();
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

//            case 1:
//                mFragment = new ArtistsFragment();
//                break;
//
//            case 2:
//                mFragment = new FavFragment();
//                break;

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

    public void setSectionTitle(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.menu_search);
                break;

            case 1:
                mTitle = getString(R.string.menu_songs);
                break;
//
//            case 2:
//                mTitle = getString(R.string.menu_fav);
//                break;

            case 3:
                mTitle = getString(R.string.menu_exit);
                break;
        }
        getSupportActionBar().setTitle(mTitle);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}
