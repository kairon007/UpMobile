package org.upmobile.materialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.Constants;
import org.upmobile.materialmusicdownloader.DownloadListener;
import org.upmobile.materialmusicdownloader.Nulldroid_Advertisement;
import org.upmobile.materialmusicdownloader.Nulldroid_Settings;
import org.upmobile.materialmusicdownloader.R;
import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.PlaybackService.OnStatePlayerListener;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

public class SearchView extends OnlineSearchView implements Constants, PlaybackService.OnErrorListener{

	private PlaybackService service;
    
	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	protected void click(final View view, int position) {
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
		((MainActivity) getContext()).showPlayerElement(true);
		try {
			((MainActivity) getContext()).startSong(((Song)getResultAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		((MainActivity)getContext()).setSelectedItem(Constants.SEARCH_FRAGMENT);
		super.click(view, position);
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

	@Override
	protected void showMessage(String msg) {
		((MainActivity)getContext()).showMessage(msg);
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
		return 0;
	}
	
	@Override
	public boolean isUseDefaultSpinner() {
		return true;
	}
	
	@Override
	public Object initRefreshProgress() {
		return LayoutInflater.from(getContext()).inflate(org.upmobile.materialmusicdownloader.R.layout.progress, null);
	}
	
	@Override
	public Bitmap getDeafultBitmapCover() {
		String cover =  getResources().getString(R.string.font_musics);
		return ((MainActivity) getContext()).getDefaultBitmapCover(64, 62, 60, cover);
	}
	
	@Override
	protected void animateListView(boolean isRestored) {
		final AnimationAdapter animAdapter = new AlphaInAnimationAdapter(getResultAdapter());
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
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
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
	protected void download(final View v, RemoteSong song, final int position) {
		downloadListener = new DownloadListener(getContext(), song, 0);
		downloadListener.setDownloadPath(getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
		if (!showDownloadLabel()) return;
		((TextView) v.findViewById(R.id.infoView)).setVisibility(View.VISIBLE);
		((TextView) v.findViewById(R.id.infoView)).setText(R.string.downloading);
		((TextView) v.findViewById(R.id.infoView)).setTextColor(Color.RED);
	}
	
	OnStatePlayerListener stateListener = new OnStatePlayerListener() {

		@Override
		public void start(AbstractSong song) {
		}

		@Override
		public void play(AbstractSong song) {
			setVisToLastClickedElements(true);
			StateKeeper.getInstance().setPlayingSong(song);
		}

		@Override
		public void pause(AbstractSong song) {
			setVisToLastClickedElements(false);
			StateKeeper.getInstance().setPlayingSong(null);
		}

		@Override
		public void stop(AbstractSong song) {
		}

		@Override
		public void stopPressed() {
			setVisToLastClickedElements(false);
			StateKeeper.getInstance().setPlayingSong(null);
		}

		@Override
		public void onTrackTimeChanged(int time, boolean isOverBuffer) {
		}

		@Override
		public void onBufferingUpdate(double percent) {
		}

		@Override
		public void update(AbstractSong song) {
		}

		@Override
		public void error() {
			setVisToLastClickedElements(false);
			StateKeeper.getInstance().setPlayingSong(null);
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
	protected boolean usePlayingIndicator() {
		return true;
	}
}
