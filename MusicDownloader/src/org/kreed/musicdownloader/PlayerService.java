package org.kreed.musicdownloader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service {

	private Player player;
	private final PlayerBinder binder = new PlayerBinder();

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
	
	public class PlayerBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

}