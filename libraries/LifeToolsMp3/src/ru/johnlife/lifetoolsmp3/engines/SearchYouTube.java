package ru.johnlife.lifetoolsmp3.engines;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.song.YouTubeSong;
import android.util.Log;

public class SearchYouTube extends BaseSearchTask {
	
	private String watchLink = "https://www.youtube.com/watch?v=%s";
	private String link = "https://content.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=%s&type=video&key=AIzaSyDUmb30N4rIk-3rrScwuki3219dcOF2nBE";
	private String[] resolution = {"default" ,"medium", "high"};

	public SearchYouTube(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			JSONObject parent = new JSONObject(readUrl(String.format(link, URLEncoder.encode(getSongName(), "UTF-8"))));
			JSONArray items = parent.getJSONArray("items");
			for(int i = 0; i< items.length(); i++) {
				if (items.getJSONObject(i) != null) {
					JSONObject item = items.getJSONObject(i);
					JSONObject snippetObject = item.getJSONObject("snippet");
					String title = snippetObject.getString("title").substring((snippetObject.getString("title").contains("-") ? snippetObject.getString("title").indexOf("-") + 1 : snippetObject.getString("title").indexOf(" ") + 1), snippetObject.getString("title").length() - 1);
					String author = snippetObject.getString("title").substring(0, (snippetObject.getString("title").contains("-") ? snippetObject.getString("title").indexOf("-") + 1 : snippetObject.getString("title").indexOf(" ") + 1)); 
					String watchUrl = String.format(watchLink, URLEncoder.encode(item.getJSONObject("id").getString("videoId"), "UTF-8"));
					JSONObject thumbnailsObject = snippetObject.getJSONObject("thumbnails");
					int pictureArrayLength = thumbnailsObject.length();
					String imageUrl = thumbnailsObject.getJSONObject(resolution[pictureArrayLength - 1]).getString("url");
					addSong(new YouTubeSong(watchUrl, imageUrl).setArtistName(author).setTitle(title));
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage());
		}
		return null;
	}

	private String readUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
		InputStreamReader inp = new InputStreamReader(url.openStream());
		Scanner sc = new Scanner(inp);
		String jsonString = "";
		while (sc.hasNext()) {
			String part = sc.nextLine();
			jsonString = jsonString.concat(part);
		}
		inp.close();
		sc.close();
		return jsonString;
	}

	
}
