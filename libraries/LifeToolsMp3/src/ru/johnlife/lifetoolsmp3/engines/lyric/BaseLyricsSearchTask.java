package ru.johnlife.lifetoolsmp3.engines.lyric;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.Util;
import android.os.AsyncTask;

public abstract class BaseLyricsSearchTask  extends AsyncTask<String, Void, String>{
	
	protected String artist;
	protected String title;
	protected OnEnginesListener searchLyrics;
	private Map<String, String> cookies = new HashMap<String, String>();

	public static interface OnEnginesListener {
		public void OnEnginesFinished (boolean found, String lyrics);
	}
	
	public BaseLyricsSearchTask(String artist, String title, OnEnginesListener searchLyrics) {
		this.artist = Util.removeSpecialCharacters(artist);
		this.title = Util.removeSpecialCharacters(title);
		this.searchLyrics = searchLyrics;
	}
	
	protected String[] agents = new String[] {
		"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36", 
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36", 
		"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36", 
		"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2226.0 Safari/537.36", 
		//2
		"Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0", 
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10; rv:33.0) Gecko/20100101 Firefox/33.0",
		"Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0",
		//3
		"Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16", 
		"Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14",
		"Opera/12.0(Windows NT 5.2;U;en)Presto/22.9.168 Version/12.00",
		//4
		"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/533.1 (KHTML, like Gecko) Maxthon/3.0.8.2 Safari/533.1",
		"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/532.4 (KHTML, like Gecko) Maxthon/3.0.6.27 Safari/532.4",
		//4
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A",
	};
	
	protected String getRandomUserAgent() {
		return agents[new Random().nextInt(agents.length)];
	}
	
	protected String readUrl(String url) throws Exception {
		Response defaut = Jsoup.connect(url)
				.userAgent(getRandomUserAgent())
				.timeout(10000)
				.followRedirects(true)
				.ignoreContentType(true)
				.ignoreHttpErrors(true)
				.method(Method.GET)
				.execute();
		return defaut.parse().toString();
	}
	
	protected Response readUrlResp (String url) throws Exception {
		Response defaut = Jsoup.connect(url)
				.userAgent(getRandomUserAgent())
				.timeout(10000)
				.followRedirects(true)
				.ignoreContentType(true)
				.cookies(cookies)
				.ignoreHttpErrors(true)
				.method(Method.GET)
				.execute();
		cookies = defaut.cookies();
		return defaut;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (null == result) {
			searchLyrics.OnEnginesFinished(false, "");
		} else {
			searchLyrics.OnEnginesFinished(true, result);
		}
		super.onPostExecute(result);
	}
}
