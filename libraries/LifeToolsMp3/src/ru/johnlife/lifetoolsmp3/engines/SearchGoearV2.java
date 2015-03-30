package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.GoearV2Song;

public class SearchGoearV2 extends SearchWithPages {

	private static final String BASE_URL = "http://www.goear.com";

	public SearchGoearV2(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			Response defaultResponse = Jsoup.connect(BASE_URL)
					.ignoreHttpErrors(true)
					.followRedirects(true)
					.ignoreContentType(true)
					.userAgent(getRandomUserAgent())
					.execute();
			String searchUrl = BASE_URL + "/search/" + URLEncoder.encode(getSongName(), "UTF-8") + "/" + (page - 1);
			Response searchResponse = Jsoup.connect(searchUrl)
					.ignoreHttpErrors(true)
					.followRedirects(true)
					.cookies(defaultResponse.cookies())
					.ignoreContentType(true)
					.userAgent(getRandomUserAgent())
					.execute();
			Document body = searchResponse.parse();
			Elements songs = body.select("li.board_item.sound_item.group");
			for (Element element : songs) {
				String title = element.select("li.title").select("a").text();
				String artist = element.select("li.band").select("a").text();
				String coverUrl = element.select("li.band_img").select("img").attr("src");
				long duration = 0;
				try {
					duration = Util.formatTime("0" + element.select("li.stats.length").text());
				} catch (Exception e) {}
				addSong(new GoearV2Song(getId(element.select("li.title").select("a").attr("href")),coverUrl).setArtistName(artist).setSongTitle(title).setDuration(duration));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getId(String attr) {
		attr = attr.replace("http://www.goear.com/listen/", "");
		attr = attr.substring(0, attr.indexOf("/"));
		return attr;
	}

}
