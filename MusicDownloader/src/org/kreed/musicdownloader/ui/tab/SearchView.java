package org.kreed.musicdownloader.ui.tab;

import org.kreed.musicdownloader.engines.Settings;
import org.kreed.musicdownloader.engines.task.DownloadListener;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.adapter.LibraryPagerAdapter;

import android.view.LayoutInflater;
import android.view.View;

import ru.johnlife.lifetoolsmp3.Advertisment;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;

public class SearchView  extends OnlineSearchView {
	
	private MainActivity activity;
	private LibraryPagerAdapter parentAdapter;

	public SearchView(LayoutInflater inflater, LibraryPagerAdapter parentAdapter, MainActivity activity) {
		super(inflater);
		this.parentAdapter = parentAdapter;
		this.activity = activity;
	}
	
	protected boolean showFullElement() {
		return false;
	}

	protected void click(View view, int position) {
		if (view.getId() == R.id.btnDownload) {
			android.util.Log.d("log", "download");
			RemoteSong song = (RemoteSong) getResultAdapter().getItem(position);
			DownloadListener listener = new DownloadListener(getContext(),song , null, parentAdapter, activity);
			song.getCover(listener);
			listener.onClick(view);
			return;
		}
		android.util.Log.d("log", "play");
	}

	@Override
	protected BaseSettings getSettings() {
		return new Settings();
	}

	@Override
	protected Advertisment getAdvertisment() {
		return null;
	}
	
}