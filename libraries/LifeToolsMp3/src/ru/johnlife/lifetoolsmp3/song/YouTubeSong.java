package ru.johnlife.lifetoolsmp3.song;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;
import android.util.Log;


public class YouTubeSong extends SongWithCover {

	private String largeCoverUrl;
	private String watchId;

	public YouTubeSong(String watchId, String largeCoverUrl) {
		super("");
		this.watchId = watchId;
		this.largeCoverUrl = largeCoverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}

	@Override
	public void getDownloadUrl(DownloadUrlListener listener) {
		super.getDownloadUrl(listener);
		getDownloadUrl(watchId);
	}
	
	private void getDownloadUrl(final String watchId) {
		
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				char server;
				String key = null;
				try {
					Response getConnect = Jsoup.connect("http://www.video2mp3.at/settings.php?set=check&format=mp3&id=" + watchId +"&key=" + Math.floor(Math.random() * 3500000))
							.method(Method.GET)
							.userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.0 Chrome/26.0.1410.43 Safari/535.12")
							.ignoreContentType(true)
							.followRedirects(true)
							.execute();
					Document doc = getConnect.parse();
					if (doc.body().text().contains("OK|")) {
						server = doc.body().text().charAt(3);
						key = doc.body().text().substring(5, doc.body().text().indexOf("|", 20));
						return "http://s" + server +".video2mp3.at/dl.php?id=" + key;
					} else {
						this.cancel(true);
						return "";
					}
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage() + "this");
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(String result) {
				setDownloadUrl(result);
				downloadUrlListener.success(result);
			}
			
			@Override
			protected void onCancelled(String result) {
				downloadUrlListener.error(result);
			}
		}.execute();
	}
}