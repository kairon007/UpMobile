package mp3.music.player.us.ui;

import mp3.music.player.us.Advertisement;
import mp3.music.player.us.Settings;
import mp3.music.player.us.ui.activities.HomeActivity;
import mp3.music.player.us.utils.MusicUtils;
import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.Activity;
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
	public void refreshLibrary() {
		HomeActivity.refreshLibrary();
	}

	@Override
	protected void stopSystemPlayer() {
		MusicUtils.pause();
	}

	@Override
	protected void bindToService(Context context) {
	}
}
