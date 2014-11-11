package org.kreed.musicdownloader.listeners;

import java.io.File;
import java.util.Random;

import org.kreed.musicdownloader.DBHelper;
import org.kreed.musicdownloader.R;
import org.kreed.musicdownloader.data.MusicData;
import org.kreed.musicdownloader.ui.adapter.ViewPagerAdapter;
import org.kreed.musicdownloader.ui.tab.DownloadsTab;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadListener extends DownloadClickListener {

	private DownloadsTab downloadsTab;
	private ViewPagerAdapter adapter;
	private Context context;
	private Random rand = new Random(System.currentTimeMillis());

	public DownloadListener(Context context, RemoteSong song, ViewPagerAdapter adapter) {
		super(context, song, null);
		this.context = context;
		this.adapter = adapter;
		downloadsTab = DownloadsTab.getInstance();
	}

	@Override
	protected void prepare(File src, RemoteSong song, String pathToFile) {
		((Activity)context).runOnUiThread(new Runnable() {	
		@Override
		public void run() {
			String chuck = context.getString(R.string.download_finished);
			String message = chuck + " " + songArtist + " - " +songTitle;
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
		});
		MusicData data = new MusicData();
		data.setSongArtist(song.getArtist());
		data.setSongTitle(song.getTitle());
		data.setSongDuration(duration);
		data.setSongBitmap(cover);
		data.setFileUri(pathToFile);
		DBHelper.getInstance(context).insert(data);
		//adapter.changeArrayMusicData(data);
	}

	@Override
	protected String getDirectory() {
		return Environment.getExternalStorageDirectory() + BaseConstants.DIRECTORY_PREFIX;
	}
	
	@Override
	protected void setCanceledListener(long id, CanceledCallback callback) {
		downloadsTab.insertTag(callback, id);
	} 
	
	@Override
	public CoverReadyListener notifyStartDownload(long downloadId) {
		final MusicData downloadItem = new MusicData();
		downloadItem.setSongArtist(songArtist);
		downloadItem.setSongTitle(songTitle);
		downloadItem.setSongDuration(String.valueOf(duration));
		downloadItem.setDownloadId(downloadId);
		downloadItem.setDownloadProgress(0);
		downloadsTab.insertData(downloadItem);
		/**
		 * while song is loaded, fileUri is ID for DownloadClickListener
		 */
		downloadItem.setFileUri(url+"__"+rand.nextInt());
		Log.e("id", ""+songId);
		return new CoverReadyListener() {
			
			@Override
			public void onCoverReady(Bitmap cover) {
				downloadItem.setSongBitmap(cover);
			}
		};
	}
	
	@Override
	protected boolean continueDownload(long lastID, long newID) {
		return downloadsTab.updateData(lastID, newID);
	}
	
	@Override
	protected void notifyAboutFailed(long downloadId) {
		super.notifyAboutFailed(downloadId);
		downloadsTab.deleteItem(downloadId);
	}

	@Override
	protected void notifyDuringDownload(final long downloadId, final long currentProgress) {
		downloadsTab.insertProgress(currentProgress, downloadId);
	}

	@Override
	protected void setFileUri(long downloadId, String uri) {
		downloadsTab.setFileUri(uri, downloadId);
	}
	
	@Override
	protected boolean isFullAction() {
		return false;
	}
}