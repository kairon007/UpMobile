package ru.johnlife.lifetoolsmp3.engines;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import java.net.URLEncoder;

import ru.johnlife.lifetoolsmp3.song.KugouSong;


public class SearchKugou extends SearchWithPages {
	
	private static String SEARCH_URL = "http://mobilecdn.kugou.com/new/app/i/search.php";
    
    public SearchKugou(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
    
	@Override
	protected Void doInBackground(Void... params) {
		try {
			Response res  = Jsoup.connect(SEARCH_URL)
					.userAgent(getRandomUserAgent())
					.data("pagesize", "20")
					.data("page", String.valueOf((page - 1)))
					.data("cmd",  "300")
					.data("rand", "1")
					.data("keyword", URLEncoder.encode(getSongName(), "UTF-8"))
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.followRedirects(true)
					.execute();
			JSONObject parent = new JSONObject(res.parse().text());
			JSONArray songs = parent.getJSONArray("data");
			for (int i = 0; i < songs.length(); i++) {
				JSONObject song = songs.getJSONObject(i);
				String artist = getArtistName( song.getString("filename"));
				String title = getTitle(song.getString("filename"));
				long duration = song.getLong("timelength") * 1000;
				String hash = song.getString("hash");
				addSong(new KugouSong(hash).setArtistName(artist).setSongTitle(title).setDuration(duration));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getArtistName(String str) {
		str = str.substring(0, str.indexOf("-")).trim();
		return str;
	}
	
	private String getTitle(String str) {
		str = str.substring(str.indexOf("-") + 1, str.length()).trim();
		return str;
	}

}