package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.GearSong;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;


public class SearchGear extends SearchWithPages {
	
	private String URL_PATTERN = "http://www.goear.com/apps/android/search_songs_json.php?p=%s&q=%s&l=20";

	public SearchGear(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			
			String strLink = String.format(URL_PATTERN, (page-1), URLEncoder.encode(getSongName(), "UTF-8"));
				
			JSONArray items = new JSONArray(Jsoup.connect(strLink).userAgent("Dalvik/1.6.0 (Linux; U; Android 4.4.2; Google Nexus 5 - 4.4.2 - API 19 - 1080x1920 Build/KOT49H)").ignoreContentType(true).get().body().text());
			
			
			
			for (int i = 0; i < items.length(); i++) {
				if (items.getJSONObject(i) != null) {
					String author = items.getJSONObject(i).getString("artist");
					String title = items.getJSONObject(i).getString("title");
					String downloadUrl = items.getJSONObject(i).getString("mp3path");
					String coverUrl = items.getJSONObject(i).getString("imgpath");
					String duration = items.getJSONObject(i).getString("songtime");
					//addSong(new GearSong(downloadUrl, coverUrl).setArtistName(author).setTitle(title).setDuration(Util.formatTime("0" + duration)));
					addSong(new RemoteSong(downloadUrl).setArtistName(author).setTitle(title).setDuration(Util.formatTime("0" + duration)));
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :(" + e.getMessage());
		}
		return null;
	}

}