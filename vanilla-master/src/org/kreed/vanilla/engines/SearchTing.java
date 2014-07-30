package org.kreed.vanilla.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kreed.vanilla.song.TingSong;

public class SearchTing extends BaseSearchTask {

	public SearchTing(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
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
	
}