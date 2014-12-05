package org.upmobile.clearmusicdownloader;

import java.io.File;

import ru.johnlife.lifetoolsmp3.BaseConstants;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

public class DownloadListener extends DownloadClickListener {

	private Context context;
	private String songArtist;
	private String songTitle;
	private String duration;

	public DownloadListener(Context context, RemoteSong song, int id) {
		super(context, song, null, id);
		songTitle = Util.removeSpecialCharacters(song.getTitle());
		songArtist = Util.removeSpecialCharacters(song.getArtist());
		duration = Util.getFormatedStrDuration(song.getDuration());
		this.context = context;
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
	}

	@Override
	protected String getDirectory() {
		return Environment.getExternalStorageDirectory() + Constants.DIRECTORY_PREFIX;
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
}