package ru.johnlife.lifetoolsmp3.engines.task;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.os.AsyncTask;

public abstract class DownloadUrlGetterTask extends AsyncTask<Song, Void, Void> {

	private DownloadUrlListener downloadUrlListener;

	public DownloadUrlGetterTask(DownloadUrlListener downloadUrlListener) {
		this.downloadUrlListener = downloadUrlListener;
	}

	@Override
	protected Void doInBackground(Song... params) {
		Song song = params[0];
		((RemoteSong) song).getDownloadUrl(downloadUrlListener);
		return null;
	}

	@Override
	protected abstract void onPreExecute();
}