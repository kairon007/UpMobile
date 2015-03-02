package org.upmobile.newmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Constants;
import org.upmobile.newmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.newmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

public class SearchView extends OnlineSearchView {

	private PlaybackService service;
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public View getView() {
		View v = super.getView();
		listView.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);
		return v;
	}
	
	@Override
	protected void click(final View view, int position) {
		if (null == service) {
			service = PlaybackService.get(getContext());
		}
		if (!service.isCorrectlyState(Song.class, getResultAdapter().getCount())) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>();
			for (AbstractSong abstractSong : getResultAdapter().getAll()) {
				try {
					list.add(abstractSong.cloneSong());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			service.setArrayPlayback(list);
		} 
		Bundle bundle = new Bundle();
		try {
			bundle.putParcelable(Constants.KEY_SELECTED_SONG, ((Song) getResultAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
//		PlayerFragment playerFragment = new PlayerFragment();
//		playerFragment.setArguments(bundle);
//		((MainActivity) getContext()).showPlayerElement(true);
//		((MainActivity) view.getContext()).changeFragment(playerFragment);
//		((MainActivity) getContext()).overridePendingTransition(0, 0);
		((MainActivity) getContext()).showMiniPlayer(true);
		try {
			((MainActivity) getContext()).startSong(((Song) getResultAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisement getAdvertisment() {
		return new Nulldroid_Advertisement();
	}

	@Override
	protected void stopSystemPlayer(Context context) {

	}

	@Override
	public void refreshLibrary() {

	}

	public Object initRefreshProgress() {
		return new ProgressBar(getContext());
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
		return R.drawable.no_cover_art_light_big_dark;
	}
	
	@Override
	protected String getDirectory() {
		return Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
	}
}
