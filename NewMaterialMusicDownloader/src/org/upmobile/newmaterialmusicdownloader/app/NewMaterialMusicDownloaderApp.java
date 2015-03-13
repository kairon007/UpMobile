package org.upmobile.newmaterialmusicdownloader.app;

import org.upmobile.newmaterialmusicdownloader.Constants;

import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.content.SharedPreferences.Editor;

public class NewMaterialMusicDownloaderApp extends MusicApp implements Constants {
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (getSharedPreferences().getString(PREF_DIRECTORY, "").isEmpty() && getSharedPreferences().getString(PREF_DIRECTORY_PREFIX, "").isEmpty()) {
			Editor editor = getSharedPreferences().edit();
			editor.putString(PREF_DIRECTORY, DIRECTORY);
			editor.putString(PREF_DIRECTORY_PREFIX, DIRECTORY_PREFIX);
			editor.commit();
		}
	}
	
	public static String getDirectory() {
        return getSharedPreferences().getString(PREF_DIRECTORY, "");
	}
	
	public static String getDirectoryPrefix() {
        return getSharedPreferences().getString(PREF_DIRECTORY_PREFIX, "");
	}
}