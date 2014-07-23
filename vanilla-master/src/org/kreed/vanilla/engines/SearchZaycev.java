package org.kreed.vanilla.engines;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SearchZaycev extends BaseSearchTask {

	private static final String access_token_key = "key.access.token.prefs";
	protected static String INIT_TOKEN_URL = "http://zaycev.net/external/hello";
	protected static String AUTHENTICATION_TOKEN_URL = "http://zaycev.net/external/auth?code=%s&hash=%s";
	protected static String TAG_TOKEN = "token";

	public SearchZaycev(FinishedParsingSongs dInterface, String songName, Context context) {
		super(dInterface, songName, context);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8").replace(" ", "%20");
			String baseUrl = "http://zaycev.net/external/search?query="+songName+"&page=1&access_token=";
			String link = baseUrl + getAccessToken();
			JSONObject response = new JSONObject(readLink(link).toString());
			if(response.has("error")) {
				link = baseUrl + generateAccessToken();
				response = new JSONObject(readLink(link).toString());
			}
			JSONArray songResults = response.getJSONArray("tracks");
			for (int i = 0; i < songResults.length(); i++) {
				JSONObject songObject = songResults.getJSONObject(i);
				String songTitle = songObject.getString("track");
				String songArtist = songObject.getString("artistName");
				String userId = songObject.getString("userId");
				String songId = songObject.getString("id");
				addSong(new ZaycevSong(userId, songId).setTitle(songTitle).setArtistName(songArtist));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getAccessToken() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String accessToken = prefs.getString(access_token_key, null);
		if (accessToken == null || accessToken.equals("")) {
			accessToken = generateAccessToken();
		}
		return accessToken;
	}
	
	private String generateAccessToken() {
		String authenticationToken = "";
		try {
			JSONObject response = new JSONObject(readLink(INIT_TOKEN_URL).toString());
			String initKey = response.getString(TAG_TOKEN);
			String salt = "llf7116f22c";
			String hashKey = md5(initKey + encryptB(salt));
			response = new JSONObject(readLink(String.format(AUTHENTICATION_TOKEN_URL, initKey, hashKey)).toString());
			authenticationToken = response.getString(TAG_TOKEN);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			prefs.edit().putString(access_token_key, authenticationToken).commit();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return authenticationToken;	
	}

	private static String encryptA(int paramInt1, int paramInt2) {
		String str = "000000" + Integer.toBinaryString(paramInt1);
		return str.substring(str.length() - paramInt2);
	}

	private static String encryptB(String paramString) {
		int i = 0;
		StringBuilder localStringBuilder1 = new StringBuilder();
		byte[] arrayOfByte = paramString.toLowerCase().getBytes();
		int j = arrayOfByte.length;
		for (int k = 0; k < j; k++)
			localStringBuilder1.append(encryptA("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(arrayOfByte[k]), 5));
		localStringBuilder1.setLength(localStringBuilder1.length() - localStringBuilder1.length() % 6);
		int m = localStringBuilder1.length();
		StringBuilder localStringBuilder2 = new StringBuilder();
		while (i < m) {
			localStringBuilder2.append("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(Integer.parseInt(localStringBuilder1.substring(i, i + 6), 2)));
			i += 6;
		}
		return localStringBuilder2.toString();
	}

	private final static String md5(final String s) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getDownloadUrl(String pageUrl) {
		try {
			Document doc = Jsoup.connect(pageUrl).timeout(20000).userAgent(getMobileUserAgent()).get();
			String downloadUrl = null;
			Elements scripts = doc.getElementsByTag("script");
			if (scripts.size() > 0) {
				for (Element script: scripts) {
					String text = script.html();
					if (text != null && text.contains(".mp3")) {
						String[] tokens = text.split("\\.mp3");
						for (String token : tokens) {
							int httpIndex = token.lastIndexOf("http:");
							if (httpIndex != -1) {
								downloadUrl = tokens[0].substring(httpIndex) + ".mp3?dlKind=dl";
								return downloadUrl;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getMobileUserAgent() {
		return "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19";
	}
	
}