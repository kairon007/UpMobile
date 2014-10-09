package org.kreed.vanilla;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {
	
	private PlaybackService service;

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
		// do nothing, just for others projects
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		service = PlaybackService.get(context);
		service.pause();
	}
	
	@Override
	protected boolean isWhiteThemeVanilla() {
		if ("AppTheme.White".equals(Util.getThemeName(getContext())) && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			return true;
		} else {
			return false;
		}
		
	}
}
