package ru.johnlife.lifetoolsmp3.song;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import android.os.AsyncTask;

public class PleerV2Song extends RemoteSong {
	
	private final String TAG_ID = "id";
	private final String TAG_DOWNLOAD = "download";
	private final String TAG_ACTION = "action";
	private final String PLEER_API_URL = "http://pleer.com/site_api/files/get_url";
	private static final String ERROR_GETTING_URL = "Error getting url";
	private String id;

	public PleerV2Song(String id) {
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
					Connection.Response response = Jsoup.connect(PLEER_API_URL)
							.ignoreContentType(true)
							.data(TAG_ACTION, TAG_DOWNLOAD)
							.data(TAG_ID, songId)
							.method(Method.POST)
							.execute();
					return new JSONObject(response.body()).getString("track_link");
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
