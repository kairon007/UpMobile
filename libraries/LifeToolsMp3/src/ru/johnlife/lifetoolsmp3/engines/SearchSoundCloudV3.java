package ru.johnlife.lifetoolsmp3.engines;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import android.util.Log;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchSoundCloudV3 extends BaseSearchTask {
	private final static String URL_PATTERN = "http://m.soundcloud.com/_api/tracks/?q=%s&client_id=2Kf29hhC5mgWf62708A&format=json";

	public SearchSoundCloudV3(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);

	}

	@Override
	protected Void doInBackground(Void... arg0) {

		String link = null;
		try {
			link = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		JSONArray arr = null;
		try {
			String str = readUrl(link);
			arr = new JSONArray(str);
			for (int i = 0; i < arr.length(); i++) {
				if (arr.getJSONObject(i) != null) {
					JSONObject nameObject = arr.getJSONObject(i);
					String name = nameObject.getString("title").substring((nameObject.getString("title").contains("-") ? nameObject.getString("title").indexOf("-") + 1 : nameObject.getString("title").indexOf(" ") + 1), nameObject.getString("title").length() - 1);
					String author = nameObject.getString("title").substring(0, (nameObject.getString("title").contains("-") ? nameObject.getString("title").indexOf("-") + 1 : nameObject.getString("title").indexOf(" ") + 1));
					String id = nameObject.getString("id");
					long duration = nameObject.getLong("duration");
					String downloadUrl = "http://api.soundcloud.com/tracks/" + id + "/stream?client_id=2Kf29hhC5mgWf62708A";
					addSong(new RemoteSong(downloadUrl).setTitle(name).setArtistName(author).setDuration(duration));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String readUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
		InputStreamReader inp = new InputStreamReader(url.openStream());
		Scanner sc = new Scanner(inp);
		int count = 0;
		String jsonString = "";
		String[] partOfJson = new String[3500];
		while (sc.hasNext()) {
			jsonString = sc.next();
			partOfJson[count] = jsonString;
			count++;
		}
		jsonString = "";
		for (int j = 0; j < partOfJson.length; j++) {
			jsonString = jsonString + partOfJson[j];
		}
		inp.close();
		jsonString = jsonString.substring(0, (jsonString.indexOf("}]") + 2));
		return jsonString;
	}

}
