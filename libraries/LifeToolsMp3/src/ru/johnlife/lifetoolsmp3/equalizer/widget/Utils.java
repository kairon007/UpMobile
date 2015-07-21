package ru.johnlife.lifetoolsmp3.equalizer.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;
import android.util.Log;

public class Utils {

	public static boolean isOn = true;
	public static final String EQ_PREFERENCES = "eq.vanilla.prefs";
	public static final String EQ_SETTINGS = "eq.vanilla.settings";
	
	public static void setEqPrefs(Context context, boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(EQ_PREFERENCES, Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putBoolean(EQ_SETTINGS, value);
		ed.apply();
	}
	
	public static boolean getEqPrefs(Context context) {
		return context.getSharedPreferences(EQ_PREFERENCES, Context.MODE_PRIVATE)
				.getBoolean(EQ_SETTINGS, false);
	}
	
	public static void changeAtBand(Equalizer equalizer, final short band, int progress) {
		short level;
		try {
			if (progress >= 0) {
				level = (short) (progress * 100); // + maxEQLevel);
				equalizer.setBandLevel(band, level);
			} else if (progress < 0) {
				level = (short) (progress * 100); // + minEQLevel);
				equalizer.setBandLevel(band, level);
			}
		} catch (Exception e) {
			Log.e(Utils.class.getSimpleName(), e.getMessage());
		}
	}
}
