package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchZvukoff extends SearchWithPages{
	
	private static String HTTP_ZVUKOFF_RU = "http://zvukoff.ru";
	private static String URL = "http://zvukoff.ru/mp3/search?keywords=%s&page=%s";
	

	public SearchZvukoff(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... paramVarArgs) {
		try {
			String link = String.format(URL, URLEncoder.encode(getSongName(), "UTF-8"), page);
			Response res = Jsoup.connect(link)
					.method(Method.GET)
					.followRedirects(true)
					.ignoreHttpErrors(true)
					.userAgent(getRandomUserAgent())
					.timeout(20000)
					.execute();
			Document doc = res.parse();
			Elements songs = doc.body().select("div[class=song song-xl]");
			for (Element element : songs) {
				String artist = element.select("div[class=song-artist]").select("span").text().toString();
				String title = element.select("div[class=song-name]").select("span").text().toString();
				long duration = Long.parseLong(element.select("a").attr("duration").toString()) * 1000;
				String downloadUrl = HTTP_ZVUKOFF_RU + element.select("a[class=song-play btn4 play]").attr("href").toString();
				addSong(new RemoteSong(downloadUrl).setArtistName(artist).setSongTitle(title).setDuration(duration));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
