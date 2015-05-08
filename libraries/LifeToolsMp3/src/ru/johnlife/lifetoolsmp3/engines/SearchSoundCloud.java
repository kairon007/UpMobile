package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.song.SoundCloudV1Song;
import android.util.Log;

public class SearchSoundCloud extends SearchWithPages {
	private int pag;
	private static String SOUNDCLOUD_BASE_URL = "http://api.soundcloud.com/tracks.json?client_id=";

	public SearchSoundCloud(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	public String getSoundcloudUrl(String clientId) {
		return SOUNDCLOUD_BASE_URL + clientId + "&q=";		
	}
	
	private int getPage() {
		this.pag = page;
		return (pag - 1) * 50;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String soundcloudClientId = getSoundcloudClientId();
			if (null == getSongName()) return null;
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			songName = songName.replace("%20", "_");
			String offset = "&offset=" + getPage();
			String link = getSoundcloudUrl(soundcloudClientId) + songName + offset;
			android.util.Log.d("logd", link);
			JSONArray parent = new JSONArray(Jsoup.connect(link)
					.ignoreContentType(true)
					.userAgent(getRandomUserAgent())
					.timeout(10000)
					.followRedirects(true)
					.method(Method.GET)
					.get().body().text());
			for (int i = 0; i < parent.length(); i++) {
				JSONObject song = parent.getJSONObject(i);
				String artist = getArtistName(song.getString("title"));
				String title = getTitle(song.getString("title"));
				String coverUrl = song.getString("artwork_url").replace("large", "t500x500");
				String streamUrl = song.getString("stream_url") + "?client_id=" + soundcloudClientId;
				long duration = song.getLong("duration");
				addSong(new SoundCloudV1Song(streamUrl, coverUrl).setSongTitle(title).setArtistName(artist).setDuration(duration));
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e + "");
		}
		return null;
	}

	private String getArtistName(String title) {
		int indexOfDownloadUrl = title.indexOf("-");
		return (indexOfDownloadUrl == -1) ? title : title.substring(0, indexOfDownloadUrl).trim();
	}

	private String getTitle(String melodie2) {
		int indexOfDownloadUrl = melodie2.indexOf("-");
		return melodie2.substring(indexOfDownloadUrl + 1 , melodie2.length()).trim();
	}
}
