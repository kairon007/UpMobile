package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import ru.johnlife.lifetoolsmp3.song.YouTubeSong;
import android.util.Log;

public class SearchYouTubeMusic extends SearchWithPages {
	
//	private String link = "https://content.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=%s&type=video&key=AIzaSyDUmb30N4rIk-3rrScwuki3219dcOF2nBE";
	private String URL_PATTERN = "https://gdata.youtube.com/feeds/api/videos/-/Music?q=%s&v=2&alt=jsonc&start-index=%s";
//	private String[] resolution = {"default" ,"medium", "high", "standard", "maxres"};
	private String[] resolution = {"sqDefault" ,"hqDefault"};
	
	public SearchYouTubeMusic(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String strLink = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"), ((page * 25) - 24));
			JSONObject parent = new JSONObject((Jsoup.connect(strLink).ignoreContentType(true).followRedirects(true).get()).body().text());
			JSONObject data = parent.getJSONObject("data");
			JSONArray items = data.getJSONArray("items");
			for(int i = 0; i< items.length(); i++) {
				if (items.getJSONObject(i) != null) {
						JSONObject item = items.getJSONObject(i);
						int duartion = item.getInt("duration");
						if (duartion < 1140) {
							String title = item.getString("title").substring((item.getString("title").contains("-") ? item.getString("title").indexOf("-") + 1 : item.getString("title").indexOf(" ") + 1), item.getString("title").length() - 1);
							String author = item.getString("title").substring(0, (item.getString("title").contains("-") ? item.getString("title").indexOf("-") + 1 : item.getString("title").indexOf(" ") + 1)); 
							String watchId = item.getString("id");
							JSONObject thumbnailsObject = item.getJSONObject("thumbnail");
							int pictureArrayLength = thumbnailsObject.length();
							String imageUrl = thumbnailsObject.getString(resolution[pictureArrayLength - 1]);
							addSong(new YouTubeSong(YouTubeSong.getUrlTask(watchId), imageUrl).setArtistName(author.replace("-", "")).setSongTitle(title).setDuration((long)(duartion * 1000)));
					}
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage());
		}
		return null;
	}
}