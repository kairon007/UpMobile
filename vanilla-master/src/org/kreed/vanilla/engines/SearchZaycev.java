package org.kreed.vanilla.engines;

import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchZaycev extends BaseSearchTask {
	
	public SearchZaycev(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8").replace(" ", "%20");
			String link = "http://go.mail.ru/search_site?q="+songName+"&p=1&rch=l&aux=Kd7dJd&sf=10";
				Document document = Jsoup.connect(link).get();
				for (Element result : document.select("li.result__li")) {
					Elements source = result.select("h3.result__title").select("a");
					String[] array = source.text().split("-");
					String artist = array[0].trim();
					String title = array[1].trim();
					String pageUrl = source.attr("href");
					addSong(new ZaycevSong(pageUrl).setTitle(title).setArtistName(artist));
				} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getDownloadUrl(String pageUrl) {
		try {
			Document document = Jsoup.connect(pageUrl).get();
			for (Element resultElement : document.select("script")) {
				String result = resultElement.toString();
				if (result.contains("url") && result.contains("http")) {
					String[] parts = result.substring(result.indexOf("url")).split("'");
					String downloadUrl = parts[1];
					return downloadUrl;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}