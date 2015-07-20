package org.upmobile.newmusicdownloader;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import java.io.File;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.tasks.BaseDownloadSongTask;
import ru.johnlife.lifetoolsmp3.utils.Util;

public class BaseDownloadListener extends BaseDownloadSongTask {

	private String songArtist;
	private String songTitle;

	public BaseDownloadListener(Context context, RemoteSong song, int id) {
		super(context, song, id);
		songTitle = Util.removeSpecialCharacters(song.getTitle());
		songArtist = Util.removeSpecialCharacters(song.getArtist());
	}

	@Override
	protected void prepare(File src, RemoteSong song, String pathToFile) {
		((Activity) getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Context context = getContext();
				String chuck = context.getString(R.string.download_finished);
				String message = chuck + " " + songArtist + " - " + songTitle;
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
			
		});
	}

	@Override
	protected String getDirectory() {
		return NewMusicDownloaderApp.getDirectory();
	}

}