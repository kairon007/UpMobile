package org.kreed.vanilla.engines;

import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchZaycev extends BaseSearchTask {

	private int maxZaycevPages = 5;

	public SearchZaycev(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8").replace(" ", "%20");
			String link = "http://zaycev.net/search.html?query_search="+songName+"&page="+maxZaycevPages;
			Document doc = Jsoup.connect(link).timeout(20000).userAgent(getMobileUserAgent()).get();
			Elements searchResults = doc.getElementsByClass("result-list__item");
			for (Element searchResult: searchResults) {
				String title = searchResult.getElementsByClass("result-list__item-title").html();
				title = title.replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
				String artist = searchResult.getElementsByClass("result-list__item-subtitle").html();
				artist = artist.replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
				String pageUrl = searchResult.getElementsByTag("a").get(0).attr("href");
				pageUrl = "http://zaycev.net" + pageUrl;
				addSong(new ZaycevSong(pageUrl).setTitle(title).setArtistName(artist));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDownloadUrl(String pageUrl) {
		try {
			Document doc = Jsoup.connect(pageUrl).timeout(20000).userAgent(getMobileUserAgent()).get();
			String downloadUrl = null;
			Elements scripts = doc.getElementsByTag("script");
			if (scripts.size() > 0) {
				for (Element script: scripts) {
					String text = script.html();
					if (text != null && text.contains(".mp3")) {
						String[] tokens = text.split("\\.mp3");
						for (String token : tokens) {
							int httpIndex = token.lastIndexOf("http:");
							if (httpIndex != -1) {
								downloadUrl = tokens[0].substring(httpIndex) + ".mp3?dlKind=dl";
								return downloadUrl;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getMobileUserAgent() {
		return "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
	}
	
}