package org.kreed.musicdownloader.engines;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.kreed.musicdownloader.app.MusicDownloaderApp;
import org.kreed.musicdownloader.song.ZaycevSong;

import android.content.SharedPreferences;

public class SearchZaycev extends SearchWithPages {

	private static String access_token_key = "key.access.token.prefs";
	private static String INIT_TOKEN_URL = "http://zaycev.net/external/hello";
	private static String AUTHENTICATION_TOKEN_URL = "http://zaycev.net/external/auth?code=%s&hash=%s";
	private static String TAG_TOKEN = "token";
	private static String ZAYCEV_SEARCH_URL = "http://zaycev.net/external/search?query=";
	private static String ACCESS = "access_token";
	private static String SA = "llf7116f22c";
	private static String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static String TRACK_URL = "http://zaycev.net/external/track/";
	private static String DOWNLOAD = "download";
	private static final DateFormat isoDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
	public SearchZaycev(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if (page == 1) maxPages = 1;
		if (page > maxPages) return null;
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8").replace(" ", "%20");
			String baseUrl = ZAYCEV_SEARCH_URL+songName+"&page="+page+"&" + ACCESS + "=";
			String link = baseUrl + getAccessToken();
			JSONObject response = new JSONObject(readLink(link).toString());
			if(response.has("error")) {
				link = baseUrl + generateAccessToken();
				response = new JSONObject(readLink(link).toString());
			}
			if (response.has("pagesCount")) {
				maxPages = response.getInt("pagesCount");
			} 
			JSONArray songResults = response.getJSONArray("tracks");
			for (int i = 0; i < songResults.length(); i++) {
				JSONObject songObject = songResults.getJSONObject(i);
				String songTitle = songObject.getString("track");
				String songArtist = songObject.getString("artistName");
				String songDuration = songObject.getString("duration");
				int songId = songObject.getInt("id");
				addSong(new ZaycevSong(songId).setTitle(songTitle).setArtistName(songArtist).setDuration(isoDateFormat.parse(songDuration).getTime()));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getAccessToken() {
		SharedPreferences prefs = MusicDownloaderApp.getSharedPreferences();
		String accessToken = prefs.getString(access_token_key, null);
		if (accessToken == null || accessToken.equals("")) {
			accessToken = generateAccessToken();
		}
		return accessToken;
	}
	
	private static String generateAccessToken() {
		String authenticationToken = "";
		try {
			JSONObject response = new JSONObject(handleLink(INIT_TOKEN_URL));
			String initKey = response.getString(TAG_TOKEN);
			String hashKey = md5(initKey + encryptB(SA));
			response = new JSONObject(handleLink(String.format(AUTHENTICATION_TOKEN_URL, initKey, hashKey)));
			authenticationToken = response.getString(TAG_TOKEN);
			SharedPreferences prefs = MusicDownloaderApp.getSharedPreferences();
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
			localStringBuilder1.append(encryptA(CHARS.indexOf(arrayOfByte[k]), 5));
		localStringBuilder1.setLength(localStringBuilder1.length() - localStringBuilder1.length() % 6);
		int m = localStringBuilder1.length();
		StringBuilder localStringBuilder2 = new StringBuilder();
		while (i < m) {
			localStringBuilder2.append(CHARS.charAt(Integer.parseInt(localStringBuilder1.substring(i, i + 6), 2)));
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

	public static String getDownloadUrl(int songId) {
		try {
			//	String link = "http://zaycev.net/external/download?id=" 
			//			+ songId + "&access_token=" + getAccessToken();
			String baseUrl = TRACK_URL + songId + "/" + DOWNLOAD + "?id=" 
					+ songId + "&" + ACCESS + "=";
			String link = baseUrl + getAccessToken();
			JSONObject response = new JSONObject(handleLink(link));
			if(response.has("error")) {
				link = baseUrl + generateAccessToken();
				response = new JSONObject(handleLink(link));
			}
			String downloadUrl = response.getString("url");
			return downloadUrl;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}