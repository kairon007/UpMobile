package ru.johnlife.lifetoolsmp3.engines.cover;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class LastFmCoverLoaderTask extends CoverLoaderTask {

	private static final String URL_PATTERN = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&format=json&api_key=2d5da0a538b02cca9f43af16b5d0d6e0&artist=%s&track=%s";
	protected String artist;
	protected String title;

	public LastFmCoverLoaderTask(String artist, String title) {
		super();
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

	public String GetUrlImage() {
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
		if (jsonObject.toString().length() < 800) {
			return null;
		}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String imageUrl = list.get(3);
		
		// do not use Last.fm default image
		if (imageUrl == null || imageUrl.contains("default_album_")) return null;
		
		return imageUrl;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			String urlImage = GetUrlImage();
			if (urlImage == null) {
				return null;
			}
			HttpGet httpget = new HttpGet(urlImage);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			BufferedInputStream bitmapStream = new BufferedInputStream(entity.getContent());
			return BitmapFactory.decodeStream(bitmapStream);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error while reading links contents", e);
		}
		return null;
	}
}
