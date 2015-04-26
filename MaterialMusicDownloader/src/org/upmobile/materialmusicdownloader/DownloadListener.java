package org.upmobile.materialmusicdownloader;

import java.io.File;
import java.util.ArrayList;

import org.upmobile.materialmusicdownloader.activity.MainActivity;
import org.upmobile.materialmusicdownloader.app.MaterialMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.DownloadCache;
import ru.johnlife.lifetoolsmp3.PlaybackService;
import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoBar;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController.UndoListener;
import ru.johnlife.lifetoolsmp3.ui.widget.UndoBarController;
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
	private UndoBar undoBar;
	
	public interface OnCancelDownload {
		public void onCancel();
	}

	public DownloadListener(Context context, RemoteSong song, int id) {
		super(context, song, id);
		this.context = context;
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
						if (PlaybackService.SMODE_SONG_FROM_LIBRARY == service.sourceSong()) {
							service.reset();
							ArrayList<AbstractSong> list = new ArrayList<AbstractSong>();
							list.add(song);
							service.setArrayPlayback(list);
							service.play(song);	
							return;
						}
						if (!((MainActivity) context).isPlayerFragment()) {
							((BaseMiniPlayerActivity) context).startSong(song);
						} else {
							((BaseMiniPlayerActivity) context).startSong(song, false);
						}
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
		UndoBarController.clear((MainActivity) context);
		undoBar = new UndoBar(((Activity) context));
		undoBar.message(message);
		undoBar.duration(MESSAGE_DURATION);
		undoBar.listener(new UndoListener() {

			@Override
			public void onUndo(Parcelable token) {
				DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				manager.remove(currentDownloadId);
				StateKeeper.getInstance().removeSongInfo(downloadingSong.getUrl());
				DownloadCache.getInstanse().remove(downloadingSong);
				if (null != cancelDownload) {
					cancelDownload.onCancel();
				}
				((BaseMiniPlayerActivity) context).miniPlayerDownloadVisible(true);
			}
		});
		undoBar.show(false);
	}
	
	@Override
	protected String getDirectory() {
		return MaterialMusicDownloaderApp.getDirectory();
	}
	
	public long getDownloadId() {
		return currentDownloadId;
	}
	
}