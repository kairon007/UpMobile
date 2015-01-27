package org.upmobile.musicpro.widget;

import org.upmobile.musicpro.Nulldroid_Advertisement;
import org.upmobile.musicpro.Nulldroid_Settings;

import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.graphics.Color;
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
	protected void stopSystemPlayer(Context context) {

	}

	@Override
	public void refreshLibrary() {

	}
	
	@Override
	public boolean isWhiteTheme(Context context) {
		return true;
	}

}
