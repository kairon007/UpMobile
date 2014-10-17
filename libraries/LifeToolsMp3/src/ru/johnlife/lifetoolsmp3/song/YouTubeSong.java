package ru.johnlife.lifetoolsmp3.song;

import java.io.IOException;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.os.AsyncTask;
import android.util.Log;

public class YouTubeSong extends SongWithCover{
	
	private String largeCoverUrl;
	private String watchUrl;
	
	public YouTubeSong(String watchUrl, String largeCoverUrl) {
		super(watchUrl);
		this.watchUrl = watchUrl;
		this.largeCoverUrl = largeCoverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}
	
	@Override
	public String getDownloadUrl() {
			try {
				downloadUrl = getDownloadUrl(watchUrl);
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Something went wrong :(" + e.getMessage());
			}
		return downloadUrl;
	}
	
	//TODO: this parsing not work. Return an empty string.
	
	public static String getDownloadUrl(final String watchUrl) throws IOException {
		final String downloadUrl = null;
		AsyncTask<Void, Void, String> getDownloadUrlTask = new AsyncTask<Void, Void, String>() {
			
			@Override
			protected String doInBackground(Void... params) {
				String url = "http://www.youtube-mp3.org/";
				Response res;
				try {
					res = Jsoup
							.connect(url)
							.method(Method.GET)
							.data("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.0 Chrome/26.0.1410.43 Safari/535.12")
							.data("item", watchUrl)
							.data("el", "na")
							.data("bf", "false")
							.data("r", "1413532263553")
							.timeout(10000)
							.execute();
					Document document = res.parse();
					Element dlForm = document.getElementById("dl_link");
					android.util.Log.d("logd", "doInBackground " + document.body().text());
					return dlForm.attr("href");
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "Something went wrong :(" + e.getMessage());
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
			}

		};
		getDownloadUrlTask.execute();
		return downloadUrl;
	}
}
