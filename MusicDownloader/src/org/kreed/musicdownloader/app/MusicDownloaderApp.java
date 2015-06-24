package org.kreed.musicdownloader.app;

import org.kreed.musicdownloader.services.PlayerService;

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
	private static PlayerService playerService;
		
	private ServiceConnection playerServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName paramComponentName) {}

		@Override
		public void onServiceConnected(ComponentName paramComponentName, IBinder paramIBinder) {
			try {
				playerService = ((PlayerService.PlayerBinder) paramIBinder).getService();
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		bindService(new Intent(this, PlayerService.class), playerServiceConnection, BIND_AUTO_CREATE);
	}

	public static PlayerService getService() {
		return playerService;
	}
}