package org.kreed.vanilla.engines;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class SearchPleer extends BaseSearchTask {
	private String Tag = SearchPleer.class.getSimpleName();
	private int pageNumber;
	
	public static final int PLEER_SEARCH_RESULTS = 20;

	public SearchPleer(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
		this.pageNumber = 1;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			String urlPleer = "http://pleer.com/browser-extension/search?limit="
					+ PLEER_SEARCH_RESULTS + "&page="
					+ pageNumber + "&q=" + songName;
			downloadAndParsePleer(urlPleer);
		} catch (UnsupportedEncodingException e) {
			Log.e(getClass().getSimpleName(), "", e);
		} catch (MalformedURLException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}
		return null;
	}

	/**
	 * 
	 * @param url
	 * @throws MalformedURLException 
	 */
	public void downloadAndParsePleer(String url) throws MalformedURLException {
		String result = readLink(url).toString();
		// Parse json object
		if (result != null)
			if (!"".equals(result))
				try {
					JSONObject jsonResult = new JSONObject(result);
					JSONArray listOfSongs = jsonResult.getJSONArray("tracks");
					for (int i = 0; i < listOfSongs.length(); i++) {
						try {
							JSONObject jsonSong = listOfSongs.getJSONObject(i);
							RemoteSong song = new RemoteSong(jsonSong.getString("file"));
							String songTitle = jsonSong.getString("track");
							song.setTitle(songTitle);
							song.setArtistName(jsonSong.getString("artist"));
							addSong(song);
							Log.i(Tag, song.getTitle());
						} catch(Exception e) {
							
						}
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

	}
}
