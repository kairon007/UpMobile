package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchQQ extends SearchWithPages {
	
	private static String SEARCH_URL = "http://soso.music.qq.com/fcgi-bin/music_json.fcg";

	public SearchQQ(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			Response res  = Jsoup.connect(SEARCH_URL)
					.userAgent(getRandomUserAgent())
					.data("catZhida", "1")
					.data("lossless", "0")
					.data("json",  "1")
					.data("t", "0")
					.data("utf8", "1")
					.data("g_tk", "5381")
					.data("w", URLEncoder.encode(getSongName(), "UTF-8"))
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.followRedirects(true)
					.execute();
			parseJson(res.parse().text().replace("searchJsonCallback(", "").replace("})", "}"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
 
	private void parseJson(String json) { 
		try {
			JSONObject parent = new JSONObject(json);
			JSONArray songs = parent.getJSONArray("list");
			for (int i = 0; i < songs.length(); i++) {
				JSONObject song = songs.getJSONObject(i);
				String artist = song.getString("fsong");
				String title = song.getString("fsinger");
				String specification = song.getString("f");
				long duration = getDuration(specification);
				addSong(new RemoteSong(getDownloadUrl(specification)).setArtistName(artist).setSongTitle(title).setDuration(duration));
			}
		} catch (Exception e) {
			removeBadCharacter(json, getInvalidPosition(e.getMessage()));
		}
	}
	
	private int getInvalidPosition(String message) {
		message = message.replace("Unterminated object at character ", "");
		message = message.substring(0, message.indexOf("of"));
		return Integer.valueOf(message.trim());
	}

	private void removeBadCharacter(String json, int position) {
		json = json.substring (0, position - 2) +  "\\" + json.substring(position - 2, json.length());
		parseJson(json);
	}

	private long getDuration(String specification) {
		// for example
		//"102202133|Psycho|5302|Muse|965184|Drones|2829384|328|4|1|2|13138755|5255626|320000|0|0|0|7498819|8035152|0|002UGqST1z26QE|0034gAQr2Z8jsi|001cQlKJ09TqjK|31|0 "
		String [] strings = specification.split("\\|");
		for (int i = 0; i < strings.length; i++) {
			android.util.Log.d("logd", "getDuration: " + strings[i] + " - " + i);
		}
		return Long.parseLong(strings[7]) * 1000;
	}
	
	private String getDownloadUrl(String specification) {
		String [] strings = specification.split("\\|");
		String songId = strings[0];
		String location = strings[8];
		android.util.Log.d("logd", "getDownloadUrl: " + "http://stream" + (location.length() > 1 ? "" : "1") + location + ".qqmusic.qq.com/3"+ songId +".mp3");
		return "http://stream1"+location+".qqmusic.qq.com/3"+ songId +".mp3";
	}
	
}
