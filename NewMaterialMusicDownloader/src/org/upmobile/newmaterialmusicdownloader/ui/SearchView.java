package org.upmobile.newmaterialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.adapter.SearchAdapter;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
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

public class SearchView extends OnlineSearchView implements PlaybackService.OnErrorListener {

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
	protected Nulldroid_Advertisment getAdvertisment() { return null; }

	@Override
	protected void stopSystemPlayer(Context context) { }
	
	//TODO remove if it possible
	@Override
	public boolean isWhiteTheme(Context context) { return false; }
	
	@Override
	protected boolean showFullElement() { return false; }
	
	@Override
	public boolean isUseDefaultSpinner() { return true; }
	
	@Override
	protected int getDropDownViewResource() {
		return R.layout.drop_down_view;
	}
	
	@Override
	protected void click(final View view, int position) {
		AbstractSong playingSong = (AbstractSong) getAdapter().getItem(position);
		if (!service.isCorrectlyStateFullCheck(Song.class, getAdapter().getCount(), new ArrayList<AbstractSong>(getAdapter().getAll()))) {
			updatePlaybackArray();
		}
		if (service.getArrayPlayback().indexOf(playingSong) == -1){
			updatePlaybackArray();
		}
		((MainActivity) getContext()).startSong((AbstractSong) getAdapter().getItem(position));
		super.click(view, position);
	}

	private void updatePlaybackArray() {
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
	
	@Override
	protected void showMessage(String msg) {
		((MainActivity) getContext()).showMessage(msg);
	}
	
	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}
	
	public void restoreState() {
		StateKeeper.getInstance().setSearchView(this);
	}

	@Override
	public void error(final String error) {
		((MainActivity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				((MainActivity) getContext()).showMessage(R.string.error_getting_url_songs);
				service.stopPressed();
			}
		});
	}
	
	private OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void start(AbstractSong song) {
			notifyAdapter();
		}

		@Override
		public void play(AbstractSong song) {}

		@Override
		public void pause(AbstractSong song) {}

		@Override
		public void stop(AbstractSong song) {
			notifyAdapter();
		}

		@Override
		public void stopPressed() {
			notifyAdapter();
		}

		@Override
		public void onTrackTimeChanged(int time, boolean isOverBuffer) {}

		@Override
		public void onBufferingUpdate(double percent) {}

		@Override
		public void update(final AbstractSong song) {
			notifyAdapter();
		}

		@Override
		public void error() {
			notifyAdapter();
		}
		
	};
	
	public void onResume() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (null == service) {
					service = PlaybackService.get(getContext());
				}
				service.setOnErrorListener(SearchView.this);
				service.addStatePlayerListener(stateListener);

			}
		}).start();
	}
	
	@Override
	protected void showShadow(final boolean visible) {
		((MainActivity) getContext()).showToolbarShadow(visible);
	}
	
	public void onPause() {
		if (null != service) {
			service.removeStatePlayerListener(stateListener);
		}
	}

	@Override
	public BaseSearchAdapter getAdapter() {
		if (null == adapter) {
			new NullPointerException("Adapter must not be null");
			return adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
		return adapter;
	}

	@Override
	protected ListView getListView(View view) {
		return (ListView) view.findViewById(R.id.list);
	}
}