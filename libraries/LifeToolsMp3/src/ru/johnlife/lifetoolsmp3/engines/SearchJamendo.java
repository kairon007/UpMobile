package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.song.JamendoSong;
import android.util.Log;

public class SearchJamendo extends SearchWithPages {
	/**
	 * If you need a search by artist, rather than on the track, it should be in front of the URL / tracks and put "/artists" after v3.0
	 */
	private final static String CLIENT_ID = "52e6c5e4";
	private String url = "https://api.jamendo.com/v3.0/tracks?client_id=" + CLIENT_ID + "&format=jsonpretty&limit=150&namesearch=%s";

	public SearchJamendo(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			JSONObject json = new JSONObject(Jsoup.connect(String.format(url, URLEncoder.encode(getSongName(), "UTF-8"))).ignoreContentType(true).followRedirects(true).get().body().text());
			JSONArray results = json.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject trackMassive = results.getJSONObject(i);
				String author = trackMassive.getString("name");
				JSONArray tracks = trackMassive.getJSONArray("tracks");
				for(int j = 0; j< tracks.length(); j++) {
					JSONObject track = tracks.getJSONObject(j);
					String title = track.getString("name");
					long duration = 1000 * track.getLong("duration");
					String downloadUrl = track.getString("audiodownload");
					String coverUrl = track.getString("album_image");
					addSong(new JamendoSong(downloadUrl, coverUrl).setArtistName(author).setDuration(duration).setTitle(title));
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
		return null;
	}

}
