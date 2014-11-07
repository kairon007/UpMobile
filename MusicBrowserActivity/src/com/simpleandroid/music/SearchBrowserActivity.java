/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.simpleandroid.music;

import ru.johnlife.lifetoolsmp3.SongArrayHolder;
import ru.johnlife.lifetoolsmp3.Util;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class is used to display the {@link ViewPager} used to swipe between the
 * main {@link Fragment}s used to browse the user's music.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class SearchBrowserActivity extends Activity {
	private View viewSearchActivity;
	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Util.getThemeName(this).equals("AppTheme.White")) {
			setContentView(Settings.SHOW_BANNER_ON_TOP ? R.layout.media_picker_activity_expanding_top_white : R.layout.media_picker_activity_expanding_white);
		} else {
			setContentView(Settings.SHOW_BANNER_ON_TOP ? R.layout.media_picker_activity_expanding_top : R.layout.media_picker_activity_expanding);
		}
		findViewById(android.R.id.list).setVisibility(View.GONE);
		findViewById(R.id.search).setVisibility(View.VISIBLE);
		MusicUtils.updateButtonBar(this, R.id.searchtab);
		FrameLayout layout = (FrameLayout) findViewById(R.id.search);
		searchView = new SearchView(this);
		viewSearchActivity = searchView.getView();
		layout.addView(viewSearchActivity);
		

		// show cross promo box
		try {
			LinearLayout downloadsLayout = (LinearLayout) findViewById(R.id.content);
			if (downloadsLayout != null) {
				if (Settings.getIsBlacklisted(this)) {
					Advertisement.hideCrossPromoBox(this, downloadsLayout);
				} else {
					Advertisement.showCrossPromoBox(this, downloadsLayout);
				}
			}
		} catch (Exception e) {

		}

		// show or hide disclaimer
		TextView editTextDisclaimer = (TextView) findViewById(R.id.editTextDisclaimer);
		if (editTextDisclaimer != null) {
			if (Settings.getIsBlacklisted(this)) {
				editTextDisclaimer.setVisibility(View.VISIBLE);
			} else {
				editTextDisclaimer.setVisibility(View.GONE);
			}
		}


		// load banner ad
		try {
			if (Settings.ENABLE_ADS) {
				Advertisement.showBanner(this);
			}
		} catch (Exception e) {

		}
		


		// initialize ad networks
		try {
			if (!Settings.getIsBlacklisted(this)) {
				Advertisement.start(this, false);
			} else {
				Advertisement.showDisclaimer(this);
			}
		} catch (Exception e) {

		}


	}

	@Override
	protected void onDestroy() {
		/*
		if (Settings.ENABLE_ADS) {
			Advertisement.mopubDestroy(this);
		}
		*/
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		SongArrayHolder.getInstance().saveStateAdapter(searchView);
		super.onPause();
	}	
	
}