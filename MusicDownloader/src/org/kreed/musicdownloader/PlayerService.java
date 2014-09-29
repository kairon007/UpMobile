package org.kreed.musicdownloader;

import java.util.ArrayList;

import org.kreed.musicdownloader.ui.Player;
import org.kreed.musicdownloader.ui.activity.MainActivity;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;

public class PlayerService extends Service {
	
	private static final ArrayList<MainActivity> sActivities = new ArrayList<MainActivity>(3);
	private final PlayerBinder binder = new PlayerBinder();
	private Player player;

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
	
	public boolean containsPlayer() {
		return player != null;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public class PlayerBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

}