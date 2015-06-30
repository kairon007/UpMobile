package mp3.music.player.us.ui;

import mp3.music.player.us.Constants;
import mp3.music.player.us.Nulldroid_Advertisement;
import mp3.music.player.us.Nulldroid_Settings;
import mp3.music.player.us.R;
import mp3.music.player.us.adapters.SearchAdapter;
import mp3.music.player.us.utils.MusicUtils;
import mp3.music.player.us.utils.PreferenceUtils;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class SearchView extends OnlineSearchView {
	
	private SearchAdapter adapter;

	public SearchView(LayoutInflater inflater) {
		super(inflater);
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisement getAdvertisment() {
		try {
			return Nulldroid_Advertisement.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected boolean onlyOnWifi() {
		ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean flag;
		if (PreferenceUtils.getInstace(getContext()).onlyOnWifi()) {
			flag = wifi.isConnected();
		} else {
			flag = true;
		}
		return flag;
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		MusicUtils.pause();
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			new NullPointerException("Adapter must not be null");
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
		return adapter;
	}
	
	@Override
	protected boolean showFullElement() {
		return true;
	}

	@Override
	protected ListView getListView(View v) {
		return (ListView) v.findViewById(R.id.list);
	}
}
