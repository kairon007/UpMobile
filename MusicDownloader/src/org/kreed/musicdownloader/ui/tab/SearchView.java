package org.kreed.musicdownloader.ui.tab;

import java.util.ArrayList;

import org.kreed.musicdownloader.Advertisement;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.Settings;
import org.kreed.musicdownloader.app.MusicDownloaderApp;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.listeners.DownloadListener;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.adapter.ViewPagerAdapter;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

public class SearchView  extends OnlineSearchView {
	
	private MainActivity activity;
	private ViewPagerAdapter parentAdapter;
	private String path;

	public SearchView(LayoutInflater inflater, ViewPagerAdapter parentAdapter, MainActivity activity) {
		super(inflater);
		this.parentAdapter = parentAdapter;
		this.activity = activity;
	}
	
	@Override
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
		int id = song.getArtist().hashCode() * song.getTitle().hashCode() * (int) System.currentTimeMillis();
		if (view.getId() == R.id.btnDownload) {
			StringBuilder stringBuilder = new StringBuilder(song.getArtist()).append(" - ").append(song.getTitle());
			final String sb = Util.removeSpecialCharacters(stringBuilder.toString());
			String directory = Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX;
			if ((Util.existFile(directory, sb) != 0) && !DownloadCache.getInstanse().contain(song.getArtist(), song.getTitle())) {
				MainActivity activity = (MainActivity) getContext();
				if (null != activity) {
					activity.getViewPager().setCurrentItem(2);
					ListView list = (ListView) parentAdapter.getListView();
					list.clearFocus();
					list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
					list.requestFocusFromTouch();
					int pos = getTrackIndexPosition(list, song.getArtist(), song.getTitle());
					if (pos != -1) {
						list.setSelection(pos);
					}
					return;
				}
			} else if ((Util.existFile(directory, sb) == 0) && !DownloadCache.getInstanse().contain(song.getArtist(), song.getTitle())) {
				downloadListener = new DownloadListener(getContext(), song, id);
				if (downloadListener.isBadInet()) return;
				song.setDownloaderListener(downloadListener.notifyStartDownload(id));
				song.getDownloadUrl(new DownloadUrlListener() {

					@Override
					public void success(String url) {
						song.getCover(downloadListener);
						downloadListener.onClick(view);
					}

					@Override
					public void error(String error) {

					}
				});
			}
		} else {
			if (MusicDownloaderApp.getService().containsPlayer()) {
				MusicDownloaderApp.getService().getPlayer().setNewName(data.getSongArtist(), data.getSongTitle(), true);
			}
			showProgressDialog(view, song, position);
			((MainActivity) activity).stopPlayer();
			song.getDownloadUrl(new DownloadUrlListener() {
				
				@Override
				public void success(String url) {
					path = url;
					SearchView.this.alertProgressDialog.dismiss();
					StateKeeper.getInstance().closeDialog(StateKeeper.PROGRESS_DIALOG);
					startPlay(song, data);
				}
				
				@Override
				public void error(String error) {}
			});
		}
	}
	
	private int getTrackIndexPosition(ListView list, String song, String title) {
		int count = list.getAdapter().getCount();
		for (int i=0; i<count; i++) {
			MusicData musicData = (MusicData) list.getItemAtPosition(i);
			if (musicData.getSongArtist().equals(song) && musicData.getSongTitle().equals(title)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected BaseSettings getSettings() {
		return new Settings();
	}
	
	@Override
	protected void hideView() {
		activity.onBackPressed();
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