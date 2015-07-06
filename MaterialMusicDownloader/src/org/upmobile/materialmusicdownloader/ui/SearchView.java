package org.upmobile.materialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.adapter.SearchAdapter;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.adapter.BaseSearchAdapter;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.views.BaseSearchView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

public class SearchView extends BaseSearchView implements Constants, PlaybackService.OnErrorListener{
	
	private BaseSearchAdapter adapter;
	
	public SearchView(LayoutInflater inflater) {
		super(inflater);
		if (null == adapter) {
			adapter = new SearchAdapter(getContext(), R.layout.row_online_search);
		}
	}

	@Override
	protected boolean showFullElement() { return false; }
	
	@Override
	protected void click(final View view, int position) {
		AbstractSong playingSong = (AbstractSong) getAdapter().getItem(position);
		if (!service.isCorrectlyStateFullCheck(Song.class, getAdapter().getCount(), new ArrayList<AbstractSong>(getAdapter().getAll()))) {
			updatePlaybackArray();
		}
		if (service.getArrayPlayback().indexOf(playingSong) == -1){
			updatePlaybackArray();
		}
		((MainActivity) getContext()).startSong(playingSong);
		((MainActivity) getContext()).setSelectedItem(Constants.SEARCH_FRAGMENT);
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
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisement getAdvertisment() {
		return new Nulldroid_Advertisement();
	}

	@Override
	protected void stopSystemPlayer(Context context) {}

	@Override
	protected void showMessage(String msg) {
		((MainActivity)getContext()).showMessage(msg);
	}
	
	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
	}

	@Override
	protected void animateListView(boolean isRestored) {
		final AnimationAdapter animAdapter = new AlphaInAnimationAdapter(getAdapter());
		animAdapter.setAbsListView(listView);
		if (isRestored) {
			animAdapter.getViewAnimator().disableAnimations();
			listView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				
				@Override
				public void onLayoutChange(View paramView, int paramInt1, int paramInt2,
						int paramInt3, int paramInt4, int paramInt5, int paramInt6,
						int paramInt7, int paramInt8) {
					animAdapter.getViewAnimator().enableAnimations();
					listView.removeOnLayoutChangeListener(this);
				}
			});
		} else {
			listView.setAdapter(animAdapter);
		}
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
	
	@Override
	protected boolean showDownloadLabel() { return true; }
	
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
		public void update(AbstractSong song) {
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
