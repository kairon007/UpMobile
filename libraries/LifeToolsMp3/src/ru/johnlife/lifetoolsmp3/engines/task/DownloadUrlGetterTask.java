package ru.johnlife.lifetoolsmp3.engines.task;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.os.AsyncTask;

public abstract class DownloadUrlGetterTask extends AsyncTask<Song, Void, String> {
	@Override
	protected String doInBackground(Song... params) {
		Song song = params[0];
		return ((RemoteSong) song).getDownloadUrl();
	}

	@Override
	protected abstract void onPostExecute(String downloadUrl);
}