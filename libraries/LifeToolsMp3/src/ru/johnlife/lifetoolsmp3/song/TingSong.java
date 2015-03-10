package ru.johnlife.lifetoolsmp3.song;

import android.os.AsyncTask;
import ru.johnlife.lifetoolsmp3.engines.SearchTing;
import ru.johnlife.lifetoolsmp3.song.RemoteSong.DownloadUrlListener;

public class TingSong extends RemoteSong {
	
	private int songId;
	private final String ERROR_RETRIEVING_URL = "ERROR_RETRIEVING_URL";
	
	public TingSong(Integer id, int songId) {
		super(id.hashCode());
		this.songId = songId;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(songId);
		return false;
	}

	private void getDownloadUrl(final int songId2) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return SearchTing.getDownloadUrl(songId2);
			}
			
			protected void onPostExecute(String result) {
				if (null != result) {
					for (DownloadUrlListener listener : downloadUrlListeners) {
						listener.success(result);
					}
				} else {
					for (DownloadUrlListener listener : downloadUrlListeners) {
						listener.error(ERROR_RETRIEVING_URL);
					}
				}
				downloadUrlListeners.clear();
			};
			
		}.execute();
	}
	
}