package org.upmobile.newmusicdownloader;

import java.io.File;

import org.upmobile.newmusicdownloader.app.NewMusicDownloaderApp;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class DownloadListener extends DownloadClickListener {

	private String songArtist;
	private String songTitle;

	public DownloadListener(Context context, RemoteSong song, int id) {
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