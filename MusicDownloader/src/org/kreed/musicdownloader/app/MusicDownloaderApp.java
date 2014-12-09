package org.kreed.musicdownloader.app;

import org.kreed.musicdownloader.PlayerService;

import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class MusicDownloaderApp extends MusicApp {
	public static Typeface FONT_LIGHT;
	public static Typeface FONT_REGULAR;
	public static Typeface FONT_BOLD;
	private static PlayerService service;
		
	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName paramComponentName) {}

		@Override
		public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder) {
			try {
				service = ((PlayerService.PlayerBinder) paramIBinder).getService();
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		bindService(new Intent(this, PlayerService.class), serviceConnection, BIND_AUTO_CREATE);
	}

	public static PlayerService getService() {
		return service;
	}
}