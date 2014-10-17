package org.kreed.musicdownloader.engines.task;

import java.io.File;

import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.adapter.ViewPagerAdapter;
import org.kreed.musicdownloader.ui.tab.DownloadsTab;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

public class DownloadListener extends DownloadClickListener {

	private DownloadsTab downloadsTab;
	private ViewPagerAdapter adapter;
	private Context context;

	public DownloadListener(Context context, RemoteSong song, ViewPagerAdapter adapter) {
		super(context, song, null);
		this.context = context;
		this.adapter = adapter;
		downloadsTab = DownloadsTab.getInstance();
	}

	@Override
	protected void prepare(File src, RemoteSong song, String pathToFile) {
		MusicData data = new MusicData();
		data.setSongArtist(song.getArtist());
		data.setSongTitle(song.getTitle());
		data.setSongDuration(duration);
		data.setSongBitmap(cover);
		data.setFileUri(pathToFile);
		DBHelper.getInstance(context).insert(data);
		adapter.changeArrayMusicData(data);
	}

	@Override
	protected String getDirectory() {
		return Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX;
	}
	
	@Override
	protected CoverReadyListener notifyStartDownload(long downloadId) {
		final MusicData downloadItem = new MusicData();
		downloadItem.setSongArtist(songArtist);
		downloadItem.setSongTitle(songTitle);
		downloadItem.setSongDuration(String.valueOf(duration));
		downloadItem.setDownloadId(downloadId);
		downloadItem.setDownloadProgress(0.0);
		downloadsTab.insertData(downloadItem);
		return new CoverReadyListener() {
			
			@Override
			public void onCoverReady(Bitmap cover) {
				downloadItem.setSongBitmap(cover);
			}
		};
	}
	
	@Override
	protected void notifyAboutFailed(long downloadId, String title) {
		downloadsTab.deleteItem(downloadId, title);
	}

	@Override
	protected void notifyDuringDownload(final long downloadId, final double currentProgress) {
		downloadsTab.insertProgress(currentProgress, downloadId);
	}

	@Override
	protected boolean isFullAction() {
		return false;
	}
}