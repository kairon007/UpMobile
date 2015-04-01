package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchMp3s extends BaseSearchTask{
	
	private final String URL = "http://www.mp3s.vet/";

	public SearchMp3s(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			String link = URL + "mp3/" + URLEncoder.encode(getSongName(), "UTF-8");
			Response defaultResponse = Jsoup.connect(URL).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true).userAgent(getRandomUserAgent()).execute();
			Response searchResponse = Jsoup.connect(link)
					.cookies(defaultResponse.cookies())
					.followRedirects(true)
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.userAgent(getRandomUserAgent())
					.execute();
			Document body = searchResponse.parse();
			String[] songUrls = searchResponse.parse().select("script").html()
					.substring(searchResponse.parse().select("script").html().indexOf("var px = ") + 11 , searchResponse.parse().select("script").html().length() - 5)
					.replaceAll("Ω", "/")
					.replace("];", "")
					.trim()
					.split(",");
			Elements songs = body.select("div.track.clearfix");
			for (int i = 0; i < songs.size(); i++) {
				String title = songs.get(i).select("span.songnamebar").text().replace("—", "").trim();
				String artist = songs.get(i).select("span.songnamebar > b").text();
				addSong(new RemoteSong(songUrls[i].replaceAll("\"", "")).setArtistName(artist.trim()).setSongTitle(title.replace(artist, "").trim()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
