package org.kreed.vanilla;

import org.kreed.vanilla.adapter.SearchAdapter;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;

public class SearchView extends OnlineSearchView {
	
	private PlaybackService service;
	private BaseSearchAdapter adapter;

	public SearchView(LayoutInflater inflater) {
		super(inflater);
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
	}

	//TODO remove it if it possible
	@Override
	public boolean isWhiteTheme(Context context) { return Util.isDifferentApp(context); }
	
	@Override
	protected BaseSettings getSettings() { return new Nulldroid_Settings(); }
	
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
		service = PlaybackService.get(context);
		service.pause();
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			new NullPointerException("Adapter must not be null");
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
		return adapter;
	}
}
