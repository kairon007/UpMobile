package mp3.music.player.us.ui;

import mp3.music.player.us.Advertisement;
import mp3.music.player.us.Settings;
import mp3.music.player.us.ui.activities.HomeActivity;
import mp3.music.player.us.utils.MusicUtils;
import mp3.music.player.us.utils.PreferenceUtils;
import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	protected BaseSettings getSettings() {
		return new Settings();
	}

	@Override
	protected Advertisment getAdvertisment() {
		try {
			return Advertisement.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected boolean onlyOnWifi() {
		return PreferenceUtils.getInstace(getContext()).onlyOnWifi();
	}

	@Override
	public void refreshLibrary() {
		HomeActivity.refreshLibrary();
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		MusicUtils.pause();
	}
}
