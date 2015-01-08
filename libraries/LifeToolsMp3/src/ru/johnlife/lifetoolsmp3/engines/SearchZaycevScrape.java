package ru.johnlife.lifetoolsmp3.engines;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;

public class SearchZaycevScrape extends SearchWithPages {
	
	private static String URL_PATTERN = "http://zaycev.net/search.html?query_search=%s&attempt=%s&page=%s";
	private static String ZAYCEV_REF_URL = "http://zaycev.net/search.html?query_search=muse&attempt=1&page=1";
	private static String ZAYCEV_URL = "http://zaycev.net";
	public SearchZaycevScrape(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		Response response;

		try {
			String strLink = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"), page, page);
					
			response = Jsoup.connect(strLink)
					.method(Method.GET)
					.userAgent("Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3")
					.referrer(ZAYCEV_REF_URL)
					.execute();
			Document body = response.parse();
			Elements items = body.select("li.result-list__item");
			for (int i = 1; i < items.size(); i++) { // i from 1, because first element is bad
				String author = items.get(i).select("div.result-list__item-subtitle").text();
				String title = items.get(i).select("div.result-list__item-title").text();
				String songPage = items.get(i).select("a").attr("abs:href");
				addSong(new RemoteSong(getDownloadUrl(songPage)).setArtistName(author).setSongTitle(title));
			}
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :(" + e.getMessage());
		}
		return null;
	}
	
	public String getDownloadUrl(final String songPage) throws IOException {
		Response res;
		try {
			res = Jsoup
					.connect(songPage)
					.method(Method.GET)
					.userAgent("Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3")
					.timeout(20000)
					.execute();
			Document document = res.parse();
			Document doc = Jsoup.connect(ZAYCEV_URL + document.select("div.track__actions > span").get(0).attr("data-url"))
					.ignoreContentType(true)
					.cookies(res.cookies())
					.get();
			return new JSONObject(doc.body().text()).getString("url");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :(" + e.getMessage());
		}
		return null;
	}
}
