package com.simpleandroid.music;

import com.simpleandroid.music.MusicUtils.ServiceToken;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;

public class SearchView extends OnlineSearchView {

	private IMediaPlaybackService mService = null;
	private ServiceToken mToken;

	private ServiceConnection osc = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			mService = IMediaPlaybackService.Stub.asInterface(obj);
		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Settings();
	}

	@Override
	protected Advertisment getAdvertisment() {
		return null;
	}

	@Override
	public void refreshLibrary() {
		// do nothing, just for others projects
	}

	@Override
	protected void stopSystemPlayer() {
		if (mService == null) return;
		try {
			mService.pause();
		} catch (RemoteException e) {
			android.util.Log.d("log", "Appear problem: " + e);
		}
	}

	@Override
	protected void bindToService(Context context) {
		mToken = MusicUtils.bindToService((Activity) context, osc);
	}

}