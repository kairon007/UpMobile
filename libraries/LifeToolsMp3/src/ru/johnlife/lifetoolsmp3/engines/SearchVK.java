package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchVK extends SearchWithPages {
	
	private final String URL = "https://api.vk.com/method/audio.search.json?";
	
	private final String TAG_ARTIST = "artist";
	private final String TAG_TITLE = "title";
	private final String TAG_DURATION = "duration";
	private final String TAG_URL = "url";
	
	private final int COUNT_SONGS_ON_PAGE = 30;
	
	public SearchVK(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		if (page == 1) {
			maxPages = 100;
		}
		if (page > maxPages) return null;
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			String link = URL + "&access_token=" + getVkClientToken() + "&q=" + songName + "&count=" + COUNT_SONGS_ON_PAGE;
			String result = readLink(link).toString();
			if ((result != null) && !(result.equals(""))) {
				JSONObject jsonResult = new JSONObject(result);
				JSONArray mp3list = (JSONArray) jsonResult.getJSONArray("response");
				int len = mp3list.length();
				if (len == 0) {
					maxPages = page - 1;
				} else {
					for (int i = 1; i < len; i++) {
						JSONObject obj = mp3list.getJSONObject(i);
						String artist = obj.getString(TAG_ARTIST);
						String title = obj.getString(TAG_TITLE);
						int duration = obj.getInt(TAG_DURATION);
						String url = obj.getString(TAG_URL).replace("\\/", "/");
						RemoteSong song = new RemoteSong(url);
						song.setArtist(artist);
						song.setTitle(title);
						song.setDuration((long) duration * 1000);
						addSong(song);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
