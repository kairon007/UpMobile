package org.kreed.musicdownloader.ui.tab;

import java.util.ArrayList;

import org.kreed.musicdownloader.Advertisement;
import org.kreed.musicdownloader.Settings;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.listeners.DownloadListener;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.adapter.ViewPagerAdapter;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class SearchView  extends OnlineSearchView {
	
	private MainActivity activity;
	private ViewPagerAdapter parentAdapter;
	private String path;

	public SearchView(LayoutInflater inflater, ViewPagerAdapter parentAdapter, MainActivity activity) {
		super(inflater);
		this.parentAdapter = parentAdapter;
		this.activity = activity;
	}
	
	protected boolean showFullElement() {
		return false;
	}

	@Override
	protected void click(final View view, int position) {
		final RemoteSong song = (RemoteSong) getResultAdapter().getItem(position);
		final MusicData data = new MusicData();
		data.setSongArtist(song.getArtist());
		data.setSongTitle(song.getTitle());
		data.setSongDuration(Util.getFormatedStrDuration(song.getDuration()));
		if (view.getId() == R.id.btnDownload) {
			downloadListener = new DownloadListener(getContext(), song, parentAdapter);
			song.getDownloadUrl(new DownloadUrlListener() {
				
				@Override
				public void success(String url) {
					song.getCover(true, downloadListener);
					downloadListener.onClick(view);	
				}
				
				@Override
				public void error(String error) {}
			});
		} else {
			song.getDownloadUrl(new DownloadUrlListener() {
				
				@Override
				public void success(String url) {
					path = url;
					SearchView.this.alertProgressDialog.dismiss();
					startPlay(song, data);
				}
				
				@Override
				public void error(String error) {}
			});
		}
	}

	@Override
	protected BaseSettings getSettings() {
		return new Settings();
	}

	@Override
	protected Advertisment getAdvertisment() {
		try {
			return Advertisement.class.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void refreshLibrary() {
		// do nothing, just for others projects
	}

	@Override
	protected void stopSystemPlayer(Context context) {
		((MainActivity) activity).pausePlayer();
	}

	@Override
	public boolean isWhiteTheme(Context context) {
		return Util.isDifferentApp(context);
	}
	
	private void startPlay(final RemoteSong song, final MusicData data) {
		activity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				data.setFileUri(path);
				ArrayList<String[]> headers = song.getHeaders();
				((MainActivity) activity).play(headers, data);
			}
		});
	}
}