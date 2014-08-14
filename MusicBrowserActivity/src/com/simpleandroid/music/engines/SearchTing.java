package com.simpleandroid.music.engines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class SearchTing extends BaseSearchTask {

	public SearchTing(FinishedParsingSongs dInterface, String songName, Context context) {
		super(dInterface, songName, context);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8").replace("%20", "+");
			String link = "http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.search.common&format=json&page_size=10&page_no=1&query="+songName;
			JSONObject response = new JSONObject(readLink(link).toString());
			JSONArray songResults = response.getJSONArray("song_list");
			for (int i = 0; i < songResults.length(); i++) {
				JSONObject songObject = songResults.getJSONObject(i);
				String songTitle = songObject.getString("title").replaceAll("<em>", "").replaceAll("</em>", "");
				String songArtist = songObject.getString("author").replaceAll("<em>", "").replaceAll("</em>", "");
				int songId = Integer.parseInt(songObject.getString("song_id"));
				addSong(new TingSong(songObject.hashCode(), songId).setTitle(songTitle).setArtistName(songArtist));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDownloadUrl(int songId) {
		String downloadUrl = null;
		try {
			String link = "http://ting.baidu.com/data/music/links?songIds="+songId;
			JSONObject response = new JSONObject(handleLink(link));
			downloadUrl = response.getJSONObject("data").getJSONArray("songList").getJSONObject(0).getString("songLink");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return downloadUrl;
	}
	
	private static String handleLink(String link) {
		try {
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", getRandomUserAgent());
			conn.setDoOutput(true);
			conn.setConnectTimeout(3000);
			StringBuffer sb = new StringBuffer();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
}