package com.simpleandroid.music;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {

	private IMediaPlaybackService mService = null;
	private Activity activity;

	private ServiceConnection osc = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			mService = IMediaPlaybackService.Stub.asInterface(obj);
			try {
				mService.pause();
			} catch (RemoteException e) {
				Log.e(getClass().getSimpleName(), e.getMessage());
			}
		}

		public void onServiceDisconnected(ComponentName classname) {}
	};

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	public SearchView(Activity activity) {
		super((LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.activity = activity;
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisment getAdvertisment() {
		return null;
	}

	@Override
	public void refreshLibrary() {
		// do nothing, just for others projects
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		if (activity != null) MusicUtils.bindToService(activity, osc);
	}

	@Override
	public boolean isWhiteTheme(Context context) {
		return Util.getThemeName(context).equals("AppTheme.White");
	}
}