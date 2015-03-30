package ru.johnlife.lifetoolsmp3.song;

import org.jsoup.Jsoup;

import android.os.AsyncTask;

public class HulkShareSong extends RemoteSong {

	private final String ERROR_RETRIEVING_URL = "ERROR_RETRIEVING_URL";
	private static String hulkshareBaseUrl = "https://www.hulkshare.com/dl/";
	private static String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.0 Chrome/26.0.1410.43 Safari/535.12";
	private String songURL;

	public HulkShareSong(String songURL) {
		super(songURL);
		this.songURL = songURL;
	}

	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(songURL);
		return false;
	}

	public void getDownloadUrl(final String songId) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					return Jsoup.connect(hulkshareBaseUrl + songId).followRedirects(true).ignoreHttpErrors(true).ignoreContentType(true).userAgent(USER_AGENT).execute().url().toString();
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
	}

}
