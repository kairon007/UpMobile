package org.upmobile.newmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.newmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmusicdownloader.R;
import org.upmobile.newmusicdownloader.activity.MainActivity;
import org.upmobile.newmusicdownloader.adapter.SearchAdapter;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class SearchView extends OnlineSearchView implements PlaybackService.OnErrorListener {

	private PlaybackService service;
	private BaseSearchAdapter adapter;
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
	}
	
	@Override
	protected BaseSettings getSettings() { return new Nulldroid_Settings(); }

	@Override
	protected Nulldroid_Advertisement getAdvertisment() { return new Nulldroid_Advertisement(); }

	@Override
	protected void stopSystemPlayer(Context context) {}
	
	//TODO remove if it possible
	@Override
	public boolean isWhiteTheme(Context context) { return false; }

	@Override
	protected boolean showFullElement() { return false; }
	
	@Override
	public boolean isUseDefaultSpinner() { return true; }

	@Override
	public View getView() {
		View view = super.getView();
		listView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		return view;
	}
	
	@Override
	protected void click(final View view, int position) {
		if (null == service) {
			service = PlaybackService.get(getContext());
			service.setOnErrorListener(this);
		}
		if (!service.isCorrectlyState(Song.class, getAdapter().getCount())) {
			ArrayList<AbstractSong> list = new ArrayList<AbstractSong>();
			for (AbstractSong abstractSong : getAdapter().getAll()) {
				try {
					list.add(abstractSong.cloneSong());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			service.setArrayPlayback(list);
		} 
		try {
			((MainActivity) getContext()).startSong(((Song) getAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}

	@Override
	public void error(final String error) {
		((MainActivity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getContext(), R.string.error_getting_url_songs, Toast.LENGTH_SHORT).show();
				service.stopPressed();
			}
		});
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			new NullPointerException("Adapter must not be null");
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
		return adapter;
	}

	@Override
	protected ListView getListView(View v) {
		return (ListView) v.findViewById(R.id.list);
	}
}
