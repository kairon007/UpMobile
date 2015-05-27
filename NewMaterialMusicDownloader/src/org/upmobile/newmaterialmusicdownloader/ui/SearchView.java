package org.upmobile.newmaterialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.DownloadListener;
import org.upmobile.newmaterialmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class SearchView extends OnlineSearchView implements PlaybackService.OnErrorListener {

	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	protected void click(final View view, int position) {
		AbstractSong playingSong = (AbstractSong) getResultAdapter().getItem(position);
		if (!service.isCorrectlyStateFullCheck(Song.class, getResultAdapter().getCount(), new ArrayList<AbstractSong>(getResultAdapter().getAll()))) {
			updatePlaybackArray();
		}
		if (service.getArrayPlayback().indexOf(playingSong) == -1){
			updatePlaybackArray();
		}
		((MainActivity) getContext()).startSong((AbstractSong) getResultAdapter().getItem(position));
		super.click(view, position);
	}

	private void updatePlaybackArray() {
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
	
	@Override
	protected BaseSettings getSettings() {
		return new Nulldroid_Settings();
	}

	@Override
	protected Nulldroid_Advertisment getAdvertisment() {
		return null;
	}
	
	@Override
	protected int getIdCustomView() {
		return R.layout.row_online_search;
	}

	@Override
	protected void stopSystemPlayer(Context context) {

	}

	@Override
	public void refreshLibrary() {

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
	
	@Override
	public int defaultCover() {
		return R.drawable.ic_album_grey;
	}
	
	@Override
	public boolean isUseDefaultSpinner() {
		return true;
	}
	
	@Override
	public Object initRefreshProgress() {
		return LayoutInflater.from(getContext()).inflate(R.layout.progress, null);
	}
	
	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}
	
	@Override
	protected int getDropDownViewResource() {
		return R.layout.drop_down_view;
	}

	@Override
	public void error(final String error) {
		((MainActivity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				((MainActivity) getContext()).showMessage(error);
				service.stopPressed();
			}
		});
	}
	
	@Override
	protected boolean showDownloadLabel() {
		return true;
	}
	
	@Override
	protected int getCustomColor() {
		return getResources().getColor(Util.getResIdFromAttribute((MainActivity) getContext(), R.attr.colorPrimary));
	}
	
	@Override
	protected void download(final View v, RemoteSong song, final int position) {
		downloadListener = new DownloadListener(getContext(), song, 0);
		downloadListener.setDownloadPath(getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
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
	protected boolean usePlayingIndicator() {
		return true;
	}
}