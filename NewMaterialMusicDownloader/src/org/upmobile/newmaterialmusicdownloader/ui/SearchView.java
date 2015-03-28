package org.upmobile.newmaterialmusicdownloader.ui;

import java.util.ArrayList;

import org.upmobile.newmaterialmusicdownloader.DownloadListener;
import org.upmobile.newmaterialmusicdownloader.Nulldroid_Settings;
import org.upmobile.newmaterialmusicdownloader.R;
import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class SearchView extends OnlineSearchView implements PlaybackService.OnErrorListener {

	private PlaybackService service;
	public SearchView(LayoutInflater inflater) {
		super(inflater);
	}
	
	@Override
	protected void click(final View view, int position) {
		if (null == service) {
			service = PlaybackService.get(getContext());
			service.setOnErrorListener(this);
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
		((MainActivity) getContext()).showPlayerElement(true);
		try {
			((MainActivity) getContext()).startSong(((Song)getResultAdapter().getItem(position)).cloneSong());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		super.click(view, position);
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
	
	public void saveState() {
		StateKeeper.getInstance().saveStateAdapter(this);
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
	protected void download(RemoteSong song) {
		DownloadListener downloadListener = new DownloadListener(getContext(), song, 0);
		downloadListener.setDownloadPath(getDirectory());
		downloadListener.setUseAlbumCover(true);
		downloadListener.downloadSong(false);
	}
}