package org.upmobile.musicpro.widget;

import org.upmobile.musicpro.Nulldroid_Advertisement;
import org.upmobile.musicpro.Nulldroid_Settings;
import org.upmobile.musicpro.R;
import org.upmobile.musicpro.activity.MainActivity;
import org.upmobile.musicpro.service.MusicService;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

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
		MusicService service  = ((MainActivity) getContext()).getService(false);
		if (null != service && service.isPlay()){
			service.pauseMusic();
			((MainActivity) getContext()).setButtonPlay();
		}
	}
	
	@Override
	protected int getDropDownViewResource() {
		return R.layout.item_drop_down;
	}
	
	@Override
	public boolean isUseDefaultSpinner() {
		return true;
	}

	@Override
	public void refreshLibrary() {

	}
	
	@Override
	public void specialInit(View view) {
		if (StateKeeper.getInstance().checkState(StateKeeper.SEARCH_EXE_OPTION)) {
			progressSecond = ProgressDialog.show(view.getContext(), getResources().getString(R.string.app_name), getResources().getString(R.string.searching), true);
		}
	}
	
	@Override
	public int defaultCover() {
		return R.drawable.ic_music_node_search;
	}
	
	@Override
	protected int getAdapterBackground() {
		return R.drawable.bg_item_song;
	}
	
	@Override
	protected int getIdCustomView() {
		return R.layout.row_online_search_pt;
	}
	
	@Override
	public boolean isWhiteTheme(Context context) {
		return false;
	}

}
