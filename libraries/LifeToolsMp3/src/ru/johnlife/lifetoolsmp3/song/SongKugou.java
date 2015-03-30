package ru.johnlife.lifetoolsmp3.song;

import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import android.os.AsyncTask;

public class SongKugou extends RemoteSong{

	private String hash;
	private static String TRACKER_URL = "http://trackercdn.kugou.com/i/";
	private static final String ERROR_GETTING_URL = "Error getting url";

	public SongKugou(String hash) {
		super(hash);
		this.hash = hash;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(hash);
		return false;
	}
	
	public String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}
		return null;
	}
	
	public void getDownloadUrl(final String songId) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					Response res  = Jsoup.connect(TRACKER_URL)
							.data("acceptMp3", "1")
							.data("cmd", "3")
							.data("pid",  "6")
							.data("hash", hash)
							.data("key",  MD5(hash + "kgcloud"))
							.ignoreContentType(true)
							.ignoreHttpErrors(true)
							.followRedirects(true)
							.execute();
					return new JSONObject(res.body()).getString("url");
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
