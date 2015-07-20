package com.simpleandroid.music;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseSearchView;

public class SearchView extends BaseSearchView {

	private IMediaPlaybackService mService = null;
	private Activity activity;
	private BaseSearchAdapter adapter;

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
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
	}
	
	public SearchView(Activity activity) {
		super((LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.activity = activity;
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
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
	protected void stopSystemPlayer(Context context) {
		if (activity != null) MusicUtils.bindToService(activity, osc);
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			new NullPointerException("Adapter must not be null");
			return adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
		return adapter;
	}

	@Override
	protected ListView getListView(View v) {
		return (ListView) v.findViewById(R.id.list);
	}
}