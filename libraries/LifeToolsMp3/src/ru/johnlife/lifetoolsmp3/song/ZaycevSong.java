package ru.johnlife.lifetoolsmp3.song;

import android.os.AsyncTask;

import ru.johnlife.lifetoolsmp3.engines.SearchZaycev;

public class ZaycevSong extends RemoteSong {

	private String id;
	private static final String ERROR_GETTING_URL = "Error getting url";

	public ZaycevSong(String id) {
		super(id);
		this.id = id;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(id);
		return false;
	}
	
	public void getDownloadUrl(final String songId) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					return SearchZaycev.getDownloadUrl(Integer.valueOf(songId));
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
						listener.error(ERROR_GETTING_URL);
					}
				}
				downloadUrlListeners.clear();
			};
		}.execute();
	}

}
