package org.upmobile.newmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Advertisement;
import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.Settings;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.fragment.PlayerFragment;
import org.upmobile.newmusicdownloader.service.PlayerService;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class SearchView extends OnlineSearchView {

	private PlayerService service;
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				service = PlayerService.get(getContext());
			}
		}).start();
	}

	@Override
	public View getView() {
		View v = super.getView();
		listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
		return v;
	}
	
	@Override
	protected void click(final View view, int position) {
		if (!service.isCorrectlyState(Song.class, getResultAdapter().getCount())) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>(getResultAdapter().getAll());
			service.setArrayPlayback(list);
		} 
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_SELECTED_SONG, getResultAdapter().getItem(position));
		bundle.putInt(Constants.KEY_SELECTED_POSITION, position);
		PlayerFragment playerFragment = new PlayerFragment();
		playerFragment.setArguments(bundle);
		((MainActivity) view.getContext()).changeFragment(playerFragment);
		((MainActivity) getContext()).overridePendingTransition(0, 0);
		super.click(view, position);
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

	}

	@Override
	public void refreshLibrary() {

	}

	@Override
	public boolean isWhiteTheme(Context context) {
		return false;
	}

	@Override
	protected boolean showFullElement() {
		return false;
	}

	@Override
	protected boolean showDownloadButton() {
		return false;
	}

	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}
	
	@Override
	public int defaultCover() {
		return R.drawable.def_cover_circle;
	}
}
