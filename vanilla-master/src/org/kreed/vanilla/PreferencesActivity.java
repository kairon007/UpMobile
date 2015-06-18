package org.kreed.vanilla;

import java.util.List;

import ru.johnlife.lifetoolsmp3.Util;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewFragment;

/**
 * The preferences activity in which one can change application preferences.
 */
public class PreferencesActivity extends PreferenceActivity {
	/**
	 * Initialize the activity, loading the preference specifications.
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preferences);
			if (!Nulldroid_Settings.ENABLE_LYRICS) {
				CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference(getString(R.string.lyric_preference));
				// PreferenceCategory mCategory = (PreferenceCategory) findPreference("category_lyrics");
				PreferenceScreen screen = getPreferenceScreen();
				screen.removePreference(mCheckBoxPref);
			}
		}
	}

	@TargetApi(11)
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
		if (!Nulldroid_Settings.ENABLE_LYRICS) {
			for (int i = 0; i < target.size(); i++) {
				if (target.get(i).fragment != null && target.get(i).fragment.contains(LyricFragment.class.getSimpleName())) {// equalsIgnoreCase(getPackageName()+"."+getClass().getSimpleName()+"$"+LyricFragment.class.getSimpleName())){
					target.remove(i);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public static class AudioActivity extends PreferenceActivity {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_audio);
		}
	}

	@TargetApi(11)
	public static class AudioFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_audio);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	public static class PlaybackActivity extends PreferenceActivity {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			addPreferencesFromResource(R.xml.preference_playback);
		}
	}

	@TargetApi(11)
	public static class PlaybackFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_playback);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	public static class LibraryActivity extends PreferenceActivity {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_library_until_11);
		}
	}

	@TargetApi(11)
	public static class LibraryFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_library);
			PreferenceGroup group = getPreferenceScreen();
			group.removePreference(group.findPreference("controls_in_selector"));
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	public static class NotificationsActivity extends PreferenceActivity {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_notifications);
		}
	}

	@TargetApi(11)
	public static class NotificationsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_notifications);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	public static class ShakeActivity extends PreferenceActivity {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_shake);
		}
	}

	@TargetApi(11)
	public static class ShakeFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_shake);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	public static class MiscActivity extends PreferenceActivity {
		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_misc);
		}
	}

	@TargetApi(11)
	public static class MiscFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_misc);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	@TargetApi(11)
	public static class LyricFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_lyric);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = (View) super.onCreateView(inflater, container, savedInstanceState);
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	public static class AboutActivity extends Activity {
		@Override
		public void onCreate(Bundle state) {
			super.onCreate(state);
			WebView view = new WebView(this);
			view.getSettings().setJavaScriptEnabled(true);
			view.loadUrl("file:///android_asset/about.html");
			setTheme(Util.getResIdFromAttribute(this, R.attr.back_actionbar));
			setContentView(view);
		}
	}

	@TargetApi(11)
	public static class AboutFragment extends WebViewFragment {
		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			WebView view = (WebView) super.onCreateView(inflater, container, savedInstanceState);
			view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			view.getSettings().setJavaScriptEnabled(true);
			view.loadUrl("file:///android_asset/about.html");
			view.setBackgroundColor(getResources().getColor(
					Util.getResIdFromAttribute(getActivity(), R.attr.preferences_bg)));
			return view;
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}
}