package org.upmobile.newmaterialmusicdownloader;

import java.io.File;

import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.devspark.appmsg.AppMsg;
import com.devspark.appmsg.AppMsg.Style;

public class DownloadListener extends DownloadClickListener {

	private Context context;
	private String songArtist;
	private String songTitle;

	public DownloadListener(Context context, RemoteSong song, int id) {
		super(context, song, id);
		songTitle = Util.removeSpecialCharacters(song.getTitle());
		songArtist = Util.removeSpecialCharacters(song.getArtist());
		this.context = context;
	}

	@Override
	protected void prepare(File src, RemoteSong song, String pathToFile) {
		((Activity)context).runOnUiThread(new Runnable() {	
		@Override
		public void run() {
			String chuck = context.getString(R.string.download_finished);
			String message = chuck + " " + songArtist + " - " +songTitle;
			((MainActivity) context).showMessage(message);
			}
		});
	}
	
	@Override
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
	
	@Override
	public void showMessage(Context context, String message) {
		((MainActivity) context).showMessage(message);
	}
	
	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}
	
	@Override
	protected void setCanceledListener(long id, CanceledCallback callback) {
	} 
	
	@Override
	public CoverReadyListener notifyStartDownload(long downloadId) {
		return new CoverReadyListener() {
			
			@Override
			public void onCoverReady(Bitmap cover) {
			}
		};
	}
	
	@Override
	protected boolean continueDownload(long lastID, long newID) {
		return false;
	}
	
	@Override
	protected void notifyAboutFailed(long downloadId) {
		super.notifyAboutFailed(downloadId);
	}

	@Override
	protected void notifyDuringDownload(final long downloadId, final long currentProgress) {
	}

	@Override
	protected void setFileUri(long downloadId, String uri) {
	}
	
	@Override
	protected boolean isFullAction() {
		return false;
	}
	
	public long getDownloadId() {
		return currentDownloadId;
	}
	
}