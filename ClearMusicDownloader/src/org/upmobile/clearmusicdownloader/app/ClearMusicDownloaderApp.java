package org.upmobile.clearmusicdownloader.app;

import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ClearMusicDownloaderApp extends Application {

	private SharedPreferences prefs;
	
	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		MusicApp.setSharedPreferences(prefs);
	}
	
}
