package org.kreed.musicdownloader.engines.task;

import java.io.File;
import java.util.ArrayList;

import org.kreed.musicdownloader.Constans;
import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.interfaces.MusicDataInterface;
import org.kreed.musicdownloader.ui.activity.MainActivity;
import org.kreed.musicdownloader.ui.adapter.LibraryPagerAdapter;
import org.kreed.musicdownloader.ui.tab.DownloadsTab;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

public class DownloadListener extends DownloadClickListener {

	private DownloadsTab downloadsTab;
	private LibraryPagerAdapter adapter;
	private MainActivity activity;
	private Context context;

	public DownloadListener(Context context, RemoteSong song, Bitmap bitmap, LibraryPagerAdapter adapter, MainActivity activity) {
		super(context, song, bitmap);
		this.context = context;
		this.adapter = adapter;
		this.activity = activity;
		downloadsTab = DownloadsTab.getInstance();
	}

	@Override
	protected void prepare(File src, RemoteSong song) {
		MusicData data = new MusicData();
		data.setSongArtist(song.getArtist());
		data.setSongTitle(song.getTitle());
		data.setSongDuration(duration);
		data.setSongBitmap(cover);
		data.setFileUri(song.path);
		DBHelper.getInstance(context).insert(data);
		adapter.changeArrayMusicData(data);
	}

	@Override
	protected String getDirectory() {
		return Environment.getExternalStorageDirectory() + Constans.DIRECTORY_PREFIX;
	}

	@Override
	protected void notifyAboutDownload(long downloadId) {
		InsertDownloadItem insertDownloadItem = new InsertDownloadItem(songTitle, songArtist, duration, downloadsTab, downloadId, cover);
		insertDownloadItem.insertData();
	}

	@Override
	protected void notifyDuringDownload(final long currentDownloadId, final String currentDownloadTitle, final double currentProgress) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				downloadsTab.currentDownloadingID(currentDownloadId);
				downloadsTab.currentDownloadingSongTitle(currentDownloadTitle);
				downloadsTab.insertProgress(currentProgress);
				downloadsTab.insertCover(cover);
			}
		});
	}

	@Override
	protected boolean isFullAction() {
		return false;
	}

	private static class InsertDownloadItem {
		private MusicDataInterface musicDataInterface;
		private String songTitle;
		private String songArtist;
		private String duration;
		private long downloadId;
		private Bitmap cover;

		public InsertDownloadItem(String songTitle, String songArtist, String formatTime, MusicDataInterface musicDataInterface, long downloadId, Bitmap cover) {
			this.songArtist = songArtist;
			this.songTitle = songTitle;
			this.duration = formatTime;
			this.musicDataInterface = musicDataInterface;
			this.downloadId = downloadId;
			this.cover = cover;
		}

		public void insertData() {
			ArrayList<MusicData> mData = new ArrayList<MusicData>();
			MusicData mItem = new MusicData();
			mItem.setSongArtist(songArtist);
			mItem.setSongTitle(songTitle);
			mItem.setSongDuration(String.valueOf(duration));
			mItem.setSongBitmap(cover);
			mItem.setDownloadId(downloadId);
			mItem.setDownloadProgress(0.0);
			mData.add(mItem);
			musicDataInterface.insertData(mData);

		}
	}
}
