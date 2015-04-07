package org.upmobile.materialmusicdownloader;

import java.io.File;

import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarStyle;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;

public class DownloadListener extends DownloadClickListener {
	
	private final int MESSAGE_DURATION = 5000;

	private Context context;
	private String songArtist;
	private String songTitle;
	private OnCancelDownload cancelDownload;
	
	public interface OnCancelDownload {
		public void onCancel();
	}

	public DownloadListener(Context context, RemoteSong song, int id) {
		super(context, song, id);
		this.context = context;		
		this.song = song;
		songTitle = Util.removeSpecialCharacters(song.getTitle());
		songArtist = Util.removeSpecialCharacters(song.getArtist());
	}
	
	public void setCancelCallback(OnCancelDownload cancelListener) {
		cancelDownload = cancelListener;
	}

	@Override
	protected void prepare(File src, final RemoteSong song, String pathToFile) {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String message = context.getString(R.string.download_finished).concat(songArtist).concat(" - ").concat(songTitle);
				UndoBar undoBar = new UndoBar(((Activity) context));
				undoBar.message(message);
				undoBar.duration(MESSAGE_DURATION);
				undoBar.listener(new UndoListener() {

					@Override
					public void onUndo(Parcelable token) {
						PlaybackService service = PlaybackService.get(context);
						service.play(song);
					}
				});
				undoBar.style(new UndoBarStyle(R.drawable.ic_play, R.string.play));
				undoBar.show(false);
			}
		});
	}
	
	@Override
	public void showMessage(Context context, int message) {
		showMessage(context, context.getString(message));
	}
	
	@Override
	public void showMessage(final Context context, String message) {
		UndoBar undoBar = new UndoBar(((Activity) context));
		undoBar.message(message);
		undoBar.duration(MESSAGE_DURATION);
		undoBar.listener(new UndoListener() {

			@Override
			public void onUndo(Parcelable token) {
				DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				manager.remove(currentDownloadId);
				StateKeeper.getInstance().removeSongInfo(song.getUrl());
				cancelDownload.onCancel();
			}
		});
		undoBar.show(false);
	}
	
	@Override
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
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