package org.kreed.musicdownloader;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;

public class PlayerService extends Service {

	private Player player;
	private final PlayerBinder binder = new PlayerBinder();
	private static final ArrayList<MainActivity> sActivities = new ArrayList<MainActivity>(3);

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public Player getPlayer() {
		return player;
	}
	
	public boolean conteinsPlayer() {
		return player != null;
	}

	public void setPlayer(Player player) {
		if (this.player != null) {
			this.player = null;
		}
		this.player = player;
	}
	
	public final ContentObserver mObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			MediaUtils.onMediaChange();
			onMediaChange();
		}
	};
	
	public void onCreate() {
		getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mObserver);
	};
	
	public void onMediaChange() {
		ArrayList<MainActivity> list = sActivities;
		for (int i = list.size(); --i != -1; )
			list.get(i).onMediaChange();
	}
	
	public static void addActivity(MainActivity activity) {
		sActivities.add(activity);
	}
	
	public static void removeActivity(MainActivity activity) {
		sActivities.remove(activity);
	}
	
	public class PlayerBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

}