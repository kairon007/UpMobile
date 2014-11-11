package org.kreed.musicdownloader.ui.tab;

import java.io.File;
import java.util.ArrayList;

import org.kreed.musicdownloader.Advertisement;
import org.kreed.musicdownloader.Settings;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.listeners.DownloadListener;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.adapter.ViewPagerAdapter;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class SearchView  extends OnlineSearchView {
	
	private MainActivity activity;
	private ViewPagerAdapter parentAdapter;
	private String path;
	private File directory = new File(Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX);

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
		if (isOffline(getContext())) {
			Toast.makeText(getContext(), getContext().getString(R.string.search_message_no_internet), Toast.LENGTH_LONG).show();
			return;
		}
		final RemoteSong song = (RemoteSong) getResultAdapter().getItem(position);
		final MusicData data = new MusicData();
		data.setSongArtist(song.getArtist());
		data.setSongTitle(song.getTitle());
		data.setSongDuration(Util.getFormatedStrDuration(song.getDuration()));
		if (view.getId() == R.id.btnDownload) {
			if (!directory.exists())
				activity.setFileObserver();
			final DownloadListener listener = new DownloadListener(getContext(), song, parentAdapter);
			final int id = (int)(System.currentTimeMillis()%(Integer.MAX_VALUE-1));
			listener.notifyStartDownload(id, null);
			song.getDownloadUrl(new DownloadUrlListener() {
				
				@Override
				public void success(String url) {
					song.getCover(true, listener);
					DownloadsTab.getInstance().deleteItem(id);
					((Activity)getContext()).runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							listener.onClick(view);							
						}
					});
				}
				
				@Override
				public void error(String error) {}
			});
		} else {
			song.getDownloadUrl(new DownloadUrlListener() {
				
				@Override
				public void success(String url) {
					path = url;
					SearchView.this.progressDialog.dismiss();
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
		return Util.getThemeName(context).equals(Util.WHITE_THEME);
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