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

package mp3.music.player.us.ui.activities;

import java.util.Random;

import mp3.music.player.us.Constants;
import mp3.music.player.us.Nulldroid_Advertisement;
import mp3.music.player.us.R;
import mp3.music.player.us.Nulldroid_Settings;
import mp3.music.player.us.ui.fragments.phone.MusicBrowserPhoneFragment;
import mp3.music.player.us.utils.PreferenceUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is used to display the {@link ViewPager} used to swipe between the
 * main {@link Fragment}s used to browse the user's music.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class HomeActivity extends BaseActivity {
	private boolean doesTheMopub = false;
	private final String IS_SHOW = "is.show";
	private static Fragment fr;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the music browser fragment
		if (savedInstanceState == null) { 
			Bundle b = new Bundle();
			b.putString(Constants.KEY_EXTRA_SEARCH, getIntent().getStringExtra(Constants.EXTRA_SEARCH));
			fr = Fragment.instantiate(this, MusicBrowserPhoneFragment.class.getName(), b);
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.activity_base_content, fr).commit();
		}
		if (savedInstanceState != null) {
			doesTheMopub = savedInstanceState.getBoolean(IS_SHOW);
		}
		// load banner ad
		/*
		try {
			if (Settings.ENABLE_ADS) {
				Advertisement.mopubShowBanner(this); 
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.toString());
		}
		*/
		
		// show cross promo box
		try {
			LinearLayout downloadsLayout = (LinearLayout) findViewById(R.id.content);
			if (downloadsLayout != null) {
				if (Nulldroid_Settings.getIsBlacklisted(this)) {
					Nulldroid_Advertisement.hideCrossPromoBox(this, downloadsLayout);
				} else {
					Nulldroid_Advertisement.showCrossPromoBox(this, downloadsLayout);
				}
			}
		} catch (Exception e) {

		}

		// show or hide disclaimer
		TextView editTextDisclaimer = (TextView) findViewById(R.id.editTextDisclaimer);
		if (editTextDisclaimer != null) {
			if (Nulldroid_Settings.getIsBlacklisted(this)) {
				editTextDisclaimer.setVisibility(View.VISIBLE);
			} else {
				editTextDisclaimer.setVisibility(View.GONE);
			}
		}


		// load banner ad
		/*
		try {
			if (Nulldroid_Settings.ENABLE_ADS) {
				Advertisement.showBanner(this);
			}
		} catch (Exception e) {

		}
		*/



		// show in the first activity


		// initialize ad networks
		try {
			if (!Nulldroid_Settings.getIsBlacklisted(this)) {
				Nulldroid_Advertisement.start(this, false);
			} else {
				//Advertisement.showDisclaimer(this);
			}
		} catch (Exception e) {

		}

		start(!PreferenceUtils.getInstace(this).isNeedRestore());
	}

	public static void refreshLibrary() {
		if (fr != null) {
			((MusicBrowserPhoneFragment)fr).refreshAdapters();
		}
	}

	public static boolean isOnline(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		boolean isOnline = activeNetworkInfo != null;
		//"isonline::: " + isOnline);
		return isOnline;
	}




	public static void toast(Activity activity, String text) {
		Toast toast = Toast.makeText(activity, text, Toast.LENGTH_LONG);
		toast.show();
	}


	public static int getRandomTheme() {
		int[] array = {AlertDialog.THEME_HOLO_LIGHT, AlertDialog.THEME_HOLO_DARK};
		int rnd = new Random().nextInt(array.length);
		return array[rnd];
	}



	public static SharedPreferences getSharedPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}


	public boolean hasRated() {
		SharedPreferences prefs = getApplicationContext().getSharedPreferences("HasRated", 0);
		boolean has_rated = prefs.getBoolean("rated", false);
		return has_rated;

	}

	public void start(boolean showRateMePopup) {
		// if first time running
		long installTime = getSharedPrefLong(this, "install_time", -999);
		if (installTime < 0) {
			// first time run
			putSharedPrefLong(this, "install_time", System.currentTimeMillis()); // save install time, which is needed
		}

		if (showRateMePopup && !hasRated() && !doesTheMopub) {
			showRatePopup(200000); 
		}
	}

	public static long getInstallTime(Context context) {
		long installTime = getSharedPrefLong(context, "install_time", 0); 
		if (installTime == 0) {
			putSharedPrefLong(context, "install_time", System.currentTimeMillis());
			return System.currentTimeMillis();
		} else {
			return installTime;
		}
	}

	public void showRatePopup(long initialDelayMillis) { 
		if (System.currentTimeMillis() - getInstallTime(this) > initialDelayMillis) { // if x seconds have passed
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) { 
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						doesTheMopub = true;
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
								Uri.parse("https://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
						startActivity(browserIntent);

						SharedPreferences prefs = getApplicationContext().getSharedPreferences("HasRated", 0);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putBoolean("rated", true).commit();  

						Toast.makeText(getActivity(), getString(R.string.rate_thanks), Toast.LENGTH_LONG).show(); 
						
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						doesTheMopub = true;
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getActivity().getString(R.string.rate_title));
			builder.setMessage(getActivity().getString(R.string.rate_description)).setPositiveButton(getString(R.string.rate_yes), dialogClickListener).setNegativeButton(getString(R.string.rate_no), dialogClickListener).setCancelable(false).show();
		} 
	}



	public Activity getActivity() {
		return this;
	}


	public static void putSharedPrefString(Context context, String property, String value) {
		SharedPreferences sharedPreferences = getSharedPrefs(context);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		if (sharedPreferencesEditor != null) {
			sharedPreferencesEditor.putString(property, value);
			sharedPreferencesEditor.commit();
		}
	}

	public static void putSharedPrefLong(Context context, String property, long value) {
		SharedPreferences sharedPreferences = getSharedPrefs(context);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		if (sharedPreferencesEditor != null) {
			sharedPreferencesEditor.putLong(property, value);
			sharedPreferencesEditor.commit();
		}
	}


	public static long getSharedPrefLong(Context context, String property, long defaultValue) {
		SharedPreferences sharedPreferences = getSharedPrefs(context.getApplicationContext());
		if (sharedPreferences != null) {
			return sharedPreferences.getLong(property, defaultValue);
		} else {
			return defaultValue;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public int setContentView() {
		return mp3.music.player.us.Nulldroid_Settings.SHOW_BANNER_ON_TOP ? R.layout.activity_base_top : R.layout.activity_base;
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
//		if (id == SearchView.STREAM_DIALOG_ID) {
//			return SearchView.getInstance(getLayoutInflater(), this).createStreamDialog(args, this);
//		}
		return super.onCreateDialog(id, args);
	}
	
	@Override
	public void onBackPressed() {
		Intent showOptions = new Intent(Intent.ACTION_MAIN);
		showOptions.addCategory(Intent.CATEGORY_HOME);
		startActivity(showOptions);
	}
	
	  protected void onSaveInstanceState(Bundle outState) {
		  	outState.putBoolean(IS_SHOW, doesTheMopub);
		    super.onSaveInstanceState(outState);
		  }
}