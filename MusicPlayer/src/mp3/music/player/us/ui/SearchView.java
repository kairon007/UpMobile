package mp3.music.player.us.ui;

import mp3.music.player.us.Settings;
import mp3.music.player.us.ui.activities.HomeActivity;
import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.util.Log;
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
		//TODO: do not return null!!!
		return null;
	}

	@Override
	public void refreshLibrary() {
		HomeActivity.refreshLibrary();
	}
}
