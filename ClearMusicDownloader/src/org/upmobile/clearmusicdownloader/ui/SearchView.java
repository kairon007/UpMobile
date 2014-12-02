package org.upmobile.clearmusicdownloader.ui;

import org.upmobile.clearmusicdownloader.Advertisement;
import org.upmobile.clearmusicdownloader.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;

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
		return new Advertisement();
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshLibrary() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isWhiteTheme(Context context) {
		return false;
	}
}
