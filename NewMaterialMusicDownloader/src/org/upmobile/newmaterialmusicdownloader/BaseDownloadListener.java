package org.upmobile.newmaterialmusicdownloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.os.Parcelable;
import android.util.Log;

import org.upmobile.newmaterialmusicdownloader.activity.MainActivity;
import org.upmobile.newmaterialmusicdownloader.application.NewMaterialApp;

import java.io.File;
import java.util.ArrayList;

import ru.johnlife.lifetoolsmp3.R;
import ru.johnlife.lifetoolsmp3.activity.BaseMiniPlayerActivity;
import ru.johnlife.lifetoolsmp3.services.PlaybackService;
import ru.johnlife.lifetoolsmp3.song.AbstractSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.utils.DownloadCache;
import ru.johnlife.lifetoolsmp3.utils.StateKeeper;
import ru.johnlife.lifetoolsmp3.utils.Util;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoBar;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarController.UndoListener;
import ru.johnlife.uilibrary.widget.notifications.undobar.UndoBarStyle;

public class BaseDownloadListener extends BaseDownloadSongTask {

	private static final int MESSAGE_DURATION = 5000;
	
	private String songArtist;
	private String songTitle;
	private OnCancelDownload cancelDownload;
	private UndoBar undoBar;
	
	public interface OnCancelDownload {
		public void onCancel();
	}

	public BaseDownloadListener(Context context, RemoteSong song, int id, boolean b) {
		super(context, song, id, b);
		songTitle = Util.removeSpecialCharacters(song.getTitle());
		songArtist = Util.removeSpecialCharacters(song.getArtist());
	}
	
	public void setCancelCallback(OnCancelDownload cancelListener) {
		cancelDownload = cancelListener;
	}

	@Override
	protected void prepare(File src, final RemoteSong song, String pathToFile) {
		((Activity) getContext()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				StringBuilder chuck = new StringBuilder(getContext().getString(R.string.download_finished));
				StringBuilder message = new StringBuilder(chuck).append(" ").append(songArtist).append(" - ").append(songTitle);
				UndoBar undoBar = new UndoBar(((Activity) getContext()));
				undoBar.message(message);
				undoBar.duration(MESSAGE_DURATION);
				undoBar.listener(new UndoListener() {

					@Override
					public void onUndo(Parcelable token) {
						Context context = getContext();
						PlaybackService service = PlaybackService.get(context);
						if (!service.hasArray() || PlaybackService.SMODE_SONG_FROM_LIBRARY == service.sourceSong()) {
							service.reset();
							ArrayList<AbstractSong> list = new ArrayList<AbstractSong>();
							list.add(song);
							service.setArrayPlayback(list);
						} else {
							service.addArrayPlayback(song);
						}
						boolean inPlayerFragment = ManagerFragmentId.playerFragment() == ((MainActivity) context).getCurrentFragmentId();
						((BaseMiniPlayerActivity) context).startSong(song, !inPlayerFragment);
					}
					
	 			});
				undoBar.style(new UndoBarStyle(R.drawable.ic_play, R.string.play));
				undoBar.show(false);
			}
		});
	}
	
	@Override
	public void showMessage(final Context context, final int message) {
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				showMessage(context, context.getString(message));
			}
		});
	}
	
	@Override
	public void showMessage(final Context context, final String message) {
		undoBar = new UndoBar(((MainActivity) context));
		undoBar.message(message);
		undoBar.duration(MESSAGE_DURATION);
		undoBar.listener(new UndoListener() {

			@Override
			public void onUndo(Parcelable token) {
				DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				manager.remove(currentDownloadId);
				String str = context.getResources().getString(R.string.download_started);
				if (message.startsWith(str)) {
					((MainActivity) context).setupDownloadBtn();
				}
				Log.d("logd", "onUndo: " + isEarlierDownloaded);
				if (!isEarlierDownloaded) StateKeeper.getInstance().removeSongInfo(downloadingSong.getComment());
				DownloadCache.getInstanse().remove(downloadingSong);
				if (null != cancelDownload) {
					cancelDownload.onCancel();
				}
			}
		});
		undoBar.show();
	}

	@Override
	protected String getDirectory() {
		return NewMaterialApp.getDirectory();
	}
	
	public long getDownloadId() {
		return currentDownloadId;
	}
	
}