package ru.johnlife.lifetoolsmp3.engines.cover;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.nostra13.universalimageloader.core.ImageLoader;

public class LastFmCoverLoaderTask extends CoverLoaderTask {

	private static final String URL_PATTERN = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json&api_key=2d5da0a538b02cca9f43af16b5d0d6e0&artist=%s&track=%s";
	protected String artist;
	protected String title;

	public LastFmCoverLoaderTask(String artist, String title) {
		this.artist = artist;
		this.title = title;
	}

	private String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1)
				buffer.append(chars, 0, read);

			return buffer.toString();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public String getUrlImage() {
		String link = null;
		try {
			link = String.format(URL_PATTERN, URLEncoder.encode(artist, "UTF-8"), URLEncoder.encode(title, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		JSONObject jsonObject = null;

		try {
			jsonObject = new JSONObject(readUrl(link));
		} catch (Exception e) {

			e.printStackTrace();
		}
		if (null == jsonObject || jsonObject.toString().length() < 800) return null;
		JSONObject track = null;
		JSONObject album = null;
		JSONArray image = null;
		try {
			track = (JSONObject) jsonObject.get("track");
			if (track == null) {
				return null;
			}
			album = (JSONObject) track.get("album");
			if (album == null) {
				return null;
			}
			image = (JSONArray) album.get("image");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < image.length(); i++) {
			try {
				list.add(image.getJSONObject(i).getString("#text"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (list.get(2).contains("default_album") || list.get(3).contains("default_album") ) {
			return null;
		}
		return list.get(3);
	}

	@Override
	public void execute() {
		new GetUrlTask().execute();
	}
	
	private class GetUrlTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... params) {
			return getUrlImage();
		}
		
		@Override
		protected void onPostExecute(String result) {
			ImageLoader.getInstance().loadImage(result, LastFmCoverLoaderTask.this);
		}
	}
}
