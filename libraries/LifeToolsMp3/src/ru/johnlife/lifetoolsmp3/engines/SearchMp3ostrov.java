package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchMp3ostrov extends SearchWithPages{
	
	private final String URL = "http://mp3ostrov.com/";

	public SearchMp3ostrov(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			String link = URL + "?string=" + URLEncoder.encode(getSongName(), "UTF-8" + "&p=" + page);
			Response defaultResponse = Jsoup.connect(URL).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true).userAgent(getRandomUserAgent()).execute();
			Response searchResponse = Jsoup.connect(link)
					.cookies(defaultResponse.cookies())
					.followRedirects(true)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.userAgent(getRandomUserAgent())
					.execute();
			Document body = searchResponse.parse();
			Elements songs = body.select("tr.play_line");
			for (Element element : songs) {
				String artist = element.select("a.inside").text();
				String title = element.select("div.hidden_title > span").text();
				String downloadUrl = element.select("a.downloads").attr("abs:href");
				addSong(new RemoteSong(downloadUrl).setArtistName(artist.replace(title, "").replace("-", "").trim()).setSongTitle(title));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
