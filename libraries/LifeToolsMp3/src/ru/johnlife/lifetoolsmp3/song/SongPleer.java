package ru.johnlife.lifetoolsmp3.song;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;

import ru.johnlife.lifetoolsmp3.engines.SearchZaycev;
import android.os.AsyncTask;

public class SongPleer extends RemoteSong {

	private String songId;
	private static final String ERROR_GETTING_URL = "Error getting url";
	private final String GET_URL_LINK = "http://pleer.com/site_api/files/get_url";

	public SongPleer(String id) {
		super(id);
		songId = id;
	}

	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(songId);
		return false;
	}
	
	public void getDownloadUrl(final String songId) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					return new JSONObject(Jsoup.connect(GET_URL_LINK)
							.data("action", "download")
							.data("id", songId)
							.method(Method.POST)
							.ignoreContentType(true)
							.followRedirects(true)
							.ignoreHttpErrors(true)
							.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36")
							.execute()
							.parse()
							.body()
							.text())
							.getString("track_link");
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
