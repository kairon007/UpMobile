package ru.johnlife.lifetoolsmp3.engines;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import ru.johnlife.lifetoolsmp3.song.PleerSong;

public class SearchPleer extends SearchWithPages {
	
	private final int COUNT_SONGS_ON_PAGE = 30;
	private final String PLEER_URL = "http://pleer.com/browser-extension/search?limit=";
	
	public SearchPleer(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		
	
		if (page == 1) maxPages = 100;
		if (page > maxPages) return null;
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			String link = PLEER_URL+COUNT_SONGS_ON_PAGE+"&page="+page+"&q="+songName;
			String result = readLink(link).toString();
			if ((result != null) && !(result.equals(""))) {
				JSONObject jsonResult = new JSONObject(result);
				JSONArray listOfSongs = jsonResult.getJSONArray("tracks");
				if (listOfSongs.length() == 0) {
					maxPages = page-1;
				} else {
					for (int i = 0; i < listOfSongs.length(); i++) {
						JSONObject jsonSong = listOfSongs.getJSONObject(i);
						/*
						 * Temporarily fix, until the player fix your API
						 */
						String id = jsonSong.getString("id");
						/*
						 * end
						 */
						PleerSong song = new PleerSong(id);
						String songTitle = jsonSong.getString("track");
						song.setSongTitle(songTitle);
						song.setArtistName(jsonSong.getString("artist"));
						song.setDuration((jsonSong.getLong("length") * 1000));
						addSong(song);
					}
				}
			} 
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "", e);
		} 
		return null;
	}
	
}