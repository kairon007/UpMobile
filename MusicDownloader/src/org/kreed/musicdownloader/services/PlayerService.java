package org.kreed.musicdownloader.services;

import org.kreed.musicdownloader.ui.Player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class PlayerService extends Service {
	
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