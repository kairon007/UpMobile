package org.kreed.vanilla.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kreed.vanilla.song.RemoteSong;

import android.util.Log;


public class SearchPleer extends SearchWithPages {
	
	private final static int COUNT_SONGS_ON_PAGE = 30;
	
	public SearchPleer(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		if (page == 1) maxPages = 100;
		if (page > maxPages) return null;
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			String link = "http://pleer.com/browser-extension/search?limit="+COUNT_SONGS_ON_PAGE+"&page="+page+"&q="+songName;
			String result = readLink(link).toString();
			if ((result != null) && !(result.equals(""))) {
				JSONObject jsonResult = new JSONObject(result);
				JSONArray listOfSongs = jsonResult.getJSONArray("tracks");
				if (listOfSongs.length() == 0) {
					maxPages = page-1;
				} else {
					for (int i = 0; i < listOfSongs.length(); i++) {
						JSONObject jsonSong = listOfSongs.getJSONObject(i);
						RemoteSong song = new RemoteSong(jsonSong.getString("file"));
						String songTitle = jsonSong.getString("track");
						song.setTitle(songTitle);
						song.setArtistName(jsonSong.getString("artist"));
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