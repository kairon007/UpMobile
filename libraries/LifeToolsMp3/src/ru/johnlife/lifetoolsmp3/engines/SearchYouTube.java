package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.StateKeeper;
import ru.johnlife.lifetoolsmp3.song.YouTubeSong;
import android.util.Log;

public class SearchYouTube extends SearchWithPages {

	private final String API_KEY = "AIzaSyDUmb30N4rIk-3rrScwuki3219dcOF2nBE";
	private String URL_PATTERN_SHORT = "https://www.googleapis.com/youtube/v3/search?maxResults=50&part=snippet&safeSearch=moderate&q=%s&type=video&videoDuration=short&key=" + API_KEY;
	private String URL_PATTERN_MEDIUM = "https://www.googleapis.com/youtube/v3/search?maxResults=50&part=snippet&safeSearch=moderate&q=%s&type=video&videoDuration=medium&key="	+ API_KEY;
	private String[] resolution = { "default", "medium", "high", "standard", "maxres" };

	public SearchYouTube(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		int j = 0;
		String link = null;
		try {
			while (j < 2) {
				switch (j) {
				case 0:
					link = URL_PATTERN_SHORT;
					break;
				case 1:
					link = URL_PATTERN_MEDIUM;
					break;
				default:
					link = URL_PATTERN_SHORT;
					break;
				}
				String strLink = String.format(link, URLEncoder.encode(getSongName(), "UTF-8"));
				if (null != StateKeeper.getInstance().getYouTubeNextPageToken() && page != 1) {
					strLink = strLink + "&pageToken=" + StateKeeper.getInstance().getYouTubeNextPageToken()[j];
				}
				JSONObject parent = new JSONObject((Jsoup.connect(strLink)
						.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
						.ignoreContentType(true).followRedirects(true).get()).body().text());
				StateKeeper.getInstance().setYouTubeNextPageToken(j, parent.getString("nextPageToken"));
				JSONArray items = parent.getJSONArray("items");
				for (int i = 1; i < items.length(); i++) {
					if (items.getJSONObject(i) != null) {
						JSONObject item = items.getJSONObject(i);
						JSONObject snippet = item.getJSONObject("snippet");
						String title = snippet.getString("title");
						String author = snippet.getString("title");
						String watchId = item.getJSONObject("id").getString("videoId");
						title = title.substring((title.contains("-") ? title.indexOf("-") + 1 : title.indexOf(" ") + 1), title.length());
						author = author.substring(0, (author.contains("-") ? author.indexOf("-") + 1 : author.indexOf(" ") + 1));
						JSONObject thumbnailsObject = snippet.getJSONObject("thumbnails");
						JSONObject picture = thumbnailsObject.getJSONObject(resolution[thumbnailsObject.length() - 1]);
						String imageUrl = picture.getString("url");
						addSong(new YouTubeSong(watchId, imageUrl).setArtistName(author.replace("-", "").trim()).setSongTitle(title.trim()));
					}
				}
				j += 1;
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :( " + e);
		}
		return null;
	}
}
