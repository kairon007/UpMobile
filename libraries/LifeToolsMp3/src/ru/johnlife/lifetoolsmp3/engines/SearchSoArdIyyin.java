package ru.johnlife.lifetoolsmp3.engines;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;

public class SearchSoArdIyyin extends SearchWithPages {
	
	private static String URL_PATTERN = "http://so.ard.iyyin.com/v2/songs/search?size=50&page=%s&q=%s";

	public SearchSoArdIyyin(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String strLink = String.format(URL_PATTERN, page, URLEncoder.encode(getSongName(), "UTF-8"));
			
			JSONObject parent = new JSONObject(readUrl(strLink));
			JSONArray items = parent.getJSONArray("data");
			for (int i = 0; i < items.length(); i++) {
				if (items.getJSONObject(i) != null) {
					JSONObject item = items.getJSONObject(i);
					String title = item.getString("song_name");
					String author = item.getString("singer_name");
					JSONArray urlList = item.getJSONArray("audition_list");
					JSONObject urlObject = urlList.getJSONObject(urlList.length() -1);
					String duration = urlObject.getString("duration");
					String downloadUrl = urlObject.getString("url");
					addSong(new RemoteSong(downloadUrl).setArtistName(author).setSongTitle(title).setDuration(Util.formatTime(duration)));
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :(" + e);
		}
		return null;
	}
	
	private String readUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
		InputStreamReader inp = new InputStreamReader(url.openStream());
		Scanner sc = new Scanner(inp);
		String jsonString = "";
		while (sc.hasNext()) {
			String part = sc.nextLine();
			jsonString = jsonString.concat(part);
		}
		inp.close();
		sc.close();
		return jsonString;
	}

}
