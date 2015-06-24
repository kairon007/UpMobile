package mp3.music.player.us.ui;

import mp3.music.player.us.Constants;
import mp3.music.player.us.Nulldroid_Advertisement;
import mp3.music.player.us.Nulldroid_Settings;
import mp3.music.player.us.ui.activities.HomeActivity;
import mp3.music.player.us.utils.MusicUtils;
import mp3.music.player.us.utils.PreferenceUtils;
import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {
	
	public SearchView(LayoutInflater inflater) {
		super(inflater);
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
	public void refreshLibrary() {
		HomeActivity.refreshLibrary();
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		MusicUtils.pause();
	}
	
	@Override
	public boolean isWhiteTheme(Context context) {
		String theme = Util.getThemeName(context);
		return null != theme && theme.equals(Constants.WHITE_THEME);
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		// TODO Auto-generated method stub
		return null;
	}
}
