package org.kreed.vanilla;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.Activity;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {
	
	private PlaybackService service;

	public SearchView(LayoutInflater inflater, Activity activity) {
		super(inflater, activity);
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
		// do nothing, just for others projects
	}

	@Override
	protected void stopSystemPlayer() {
		// TODO stop player
		service.pause();
	}
	
	@Override
	protected void bindToService(Activity activity) {
		service = PlaybackService.get(activity);
	}
}
