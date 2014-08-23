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

//import mp3.music.player.us.ui.fragments.phone.MusicBrowserPhoneFragment;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

/**
 * This class is used to display the {@link ViewPager} used to swipe between the
 * main {@link Fragment}s used to browse the user's music.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class SearchBrowserActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(Settings.SHOW_BANNER_ON_TOP?R.layout.media_picker_activity_expanding_top:R.layout.media_picker_activity_expanding);
        findViewById(android.R.id.list).setVisibility(View.GONE);
        findViewById(R.id.search).setVisibility(View.VISIBLE);
        MusicUtils.updateButtonBar(this, R.id.searchtab);
        FrameLayout layout = (FrameLayout)findViewById(R.id.search);
        View searchView = OnlineSearchView.getInstanceView(getLayoutInflater(), this);
        layout.addView(searchView);
        if (Settings.ENABLE_ADS) {
            Advertisement.mopubShowBanner(this);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == OnlineSearchView.STREAM_DIALOG_ID) {
			return OnlineSearchView.getInstance(getLayoutInflater(), this).createStreamDialog(args);
		}
		return super.onCreateDialog(id, args);
	}
}