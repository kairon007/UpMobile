package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchGrooveshark;
import android.os.AsyncTask;


public class GrooveSong extends RemoteSong {

	private final String ERROR_RETRIEVING_URL = "ERROR_RETRIEVING_URL";
	private int songId;

	public GrooveSong(long id, int songId) {
		super(id);
		this.songId = songId;
	}

	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(songId);
		return false;
	}

	public Integer getSongId() {
		return songId;
	}
	
	public Void getDownloadUrl(final int songId) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					return SearchGrooveshark.getClient().GetStreamKey(songId).result.DirectURL();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
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
		return null;
	}

}