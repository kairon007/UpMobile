/*
 * Copyright (C) 2011 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package mp3.music.player.us.widgets;

import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;

/**
 * Adapter for action bar navigation events. This class implements an adapter
 * that facilitates handling of action bar navigation events. An instance of
 * this class must be installed as a TabListener or OnNavigationListener on an
 * Action Bar, and it will relay the navigation events to a configured listener
 * (a {@link CompatActionBarNavListener}). This class should only be instanced
 * and used on Android platforms that support the Action Bar, that is, SDK level
 * 11 and above.
 */ 
public class CompatActionBarNavHandler implements TabListener, OnNavigationListener {

    /* The listener that we notify of navigation events */ 
    private final CompatActionBarNavListener mNavListener;

    /**
     * Constructs an instance with the given listener.
     * 
     * @param listener the listener to notify when a navigation event occurs.
     */
    public CompatActionBarNavHandler(final CompatActionBarNavListener listener) {
        mNavListener = listener;
    }

    /**
     * Called by framework when a tab is selected. This will cause a navigation
     * event to be delivered to the configured listener.
     */
    @Override 
    public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
        mNavListener.onCategorySelected(tab.getPosition());
    }

    /**
     * Called by framework when a item on the navigation menu is selected. This
     * will cause a navigation event to be delivered to the configured listener.
     */
    @Override
    public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
        mNavListener.onCategorySelected(itemPosition);
        return true;
    }

    /**
     * Called by framework when a tab is re-selected. That is, it was already
     * selected and is tapped on again. This is not used in our app.
     */
    @Override
    public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
        /* Nothing to do */
    }

    /**
     * Called by framework when a tab is unselected. Not used in our app.
     */
    @Override
    public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
        /* Nothing to do */
    }

}
