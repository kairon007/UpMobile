package org.upmobile.musix.view;

import org.upmobile.musix.Nulldroid_Advertisement;
import org.upmobile.musix.Nulldroid_Settings;
import org.upmobile.musix.R;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {

	public SearchView(LayoutInflater inflater) {
		super(inflater);
		// TODO Auto-generated constructor stub
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

	@Override
	protected boolean showFullElement() {
		return true;
	}
	
	@Override
	public int defaultCover() {
		return R.drawable.ic_launcher;
	}
	
	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}
	
	@Override
	public boolean isUseDefaultSpinner() {
		return true;
	}
}
