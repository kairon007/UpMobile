package org.upmobile.musicpro.widget;

import org.upmobile.musicpro.Nulldroid_Advertisement;
import org.upmobile.musicpro.Nulldroid_Settings;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;
import org.upmobile.musicpro.adapter.SearchAdapter;
import org.upmobile.musicpro.service.MusicService;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class SearchView extends OnlineSearchView {
	
	private BaseSearchAdapter adapter;

	public SearchView(LayoutInflater inflater) {
		super(inflater);
		System.out.println("!!! SearchView");
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search_pt);
		}
		StateKeeper.getInstance().activateOptions(StateKeeper.IS_PT_TEXT);
	}

	//TODO remove if it possible
	@Override
	protected boolean isAppPT() { return true; }
	
	@Override
	protected BaseSettings getSettings() { return new Nulldroid_Settings(); }
	
	@Override
	public boolean isUseDefaultSpinner() { return true; }
	
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
		MusicService service  = ((MainActivity) getContext()).getService(false);
		PlaybackService playbackService = PlaybackService.get(context);
		if ((null != service && service.isPlay()) || (null != playbackService && playbackService.isPlaying())){
			service.pauseMusic();
			playbackService.stop();
			((MainActivity) getContext()).setButtonPlay();
		}
	}
	
	@Override
	public void specialInit(View view) {
		if (StateKeeper.getInstance().checkState(StateKeeper.SEARCH_EXE_OPTION)) {
			progressSecond = ProgressDialog.show(view.getContext(), getResources().getString(R.string.app_name), getResources().getString(R.string.searching), true);
		}
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			System.out.println("!!! getAdapter adapter == null");
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search_pt);
		}
		return adapter;
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.list);
	}
}
