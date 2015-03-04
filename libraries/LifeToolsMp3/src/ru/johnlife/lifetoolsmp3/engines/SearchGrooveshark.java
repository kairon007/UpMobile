package ru.johnlife.lifetoolsmp3.engines;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.song.GrooveSong;

import com.scilor.grooveshark.API.Base.GroovesharkClient;
import com.scilor.grooveshark.API.Functions.SearchArtist.SearchArtistResult;

public class SearchGrooveshark extends BaseSearchTask {

	private static String USER_UUID = UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH);
	private static final String COOKIE_KEY = "Cookie";
	private static final String GET_COMMUNICATION_TOKEN = "http://grooveshark.com/preload.php?getCommunicationToken";
	private static final String GET_STREAM_KEY = "http://grooveshark.com/more.php?getStreamKeyFromSongIDEx";
	private final String GET_COUNTRY = "http://grooveshark.com/more.php?getCountry";
	private final String INITIATE_SESSION = "http://grooveshark.com/more.php?initiateSession";
	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:9.0.1) Gecko/20100101 Firefox/9.0.1";
	private final String getResults = "http://grooveshark.com/more.php?getResultsFromSearch";
	private Map<String, String> cookies;
	private static String CONFIGURATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?><GrooveFix version=\"20130530\"><htmlshark><GrooveClient>htmlshark</GrooveClient><GrooveClientRevision>20130520</GrooveClientRevision><GrooveStaticRandomizer>:nuggetsOfBaller:</GrooveStaticRandomizer></htmlshark><jsqueue><GrooveClient>jsqueue</GrooveClient><GrooveClientRevision>20130520</GrooveClientRevision><GrooveStaticRandomizer>:chickenFingers:</GrooveStaticRandomizer></jsqueue><mobileshark><GrooveClient>mobileshark</GrooveClient><GrooveClientRevision>20120112</GrooveClientRevision><GrooveStaticRandomizer>:boomGoesTheDolphin:</GrooveStaticRandomizer></mobileshark><mobileshark><GrooveClient>jsplayer</GrooveClient><GrooveClientRevision>20120124.01</GrooveClientRevision><GrooveStaticRandomizer>:needsMoarFoodForSharks:</GrooveStaticRandomizer></mobileshark></GrooveFix>";
	private String sessionID;
	private String communicationToken;
	private static GroovesharkClient client;


	public SearchGrooveshark(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			client = new GroovesharkClient(true, CONFIGURATION);
			SearchArtistResult[] results = client.SearchArtist(getSongName()).result.result;
			communicationToken = client.CommunicationToken();
			sessionID = client.SessionID();
			ArrayList<String[]> headers = new ArrayList<String[]>();
			headers.add(new String[] { "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8" });
			headers.add(new String[] { "Content-Type", "text/html; charset=UTF-8" });
			headers.add(new String[] { "Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3" });
			headers.add(new String[] { "Accept-Charset", "utf-8;q=0.7,*;q=0.7" });
			if (results.length != 0) {
				for (SearchArtistResult result : results) {
					int songId = result.SongID;
					String songArtist = result.ArtistName;
					String songTitle = result.Name;
					String duration = result.EstimateDuration;
					addSong(new GrooveSong(songId, songId)
										.setArtistName(songArtist)
										.setSongTitle(songTitle)
										.setDuration((long) (Double.valueOf(duration) * 1000))
										.setHeader(headers));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Map<String, String> getHeaders(boolean useSessionId) {
		Map<String, String> headers = new HashMap<String, String>();
		if (useSessionId) {
			headers.put(COOKIE_KEY, "PHPSESSID=" + sessionID);
		}
		headers.put("User-agent", USER_AGENT);
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headers.put("Content-Type", "text/html; charset=UTF-8");
		headers.put("Accept-Language", "de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		headers.put("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
		return headers;
	}
	
	/**
	 * Below code to send requests to the server manually
	**/
	
	private JSONObject initSessionJSON() {
		JSONObject parent = null;
		JSONObject header = null;
		JSONObject country = null;
		try {
			parent = new JSONObject();
			header = new JSONObject();
			country = new JSONObject();
			country.put("CC1", "0");
			country.put("CC2", "0");
			country.put("CC3", "0");
			country.put("CC4", "0");
			country.put("ID", "1");
			country.put("IPR", "1");
			header.put("client", "htmlshark");
			header.put("clientRevision", "20130520");
			header.put("country", country);
			header.put("uuid", USER_UUID);
			header.put("privacy", 0);
			parent.put("parameters", new JSONObject());
			parent.put("header", header);
			parent.put("method", "initiateSession");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return parent;
	}

	private JSONObject getResultsJSON(String arg0) {
		JSONObject parent = null;
		JSONObject header = null;
		JSONObject country = null;
		JSONObject parameters = null;
		try {
			parameters = new JSONObject();
			parent = new JSONObject();
			header = new JSONObject();
			country = new JSONObject();
			country.put("CC1", "0");
			country.put("CC2", "0");
			country.put("CC3", "0");
			country.put("CC4", "2147483648");
			country.put("ID", "223");
			country.put("IPR", "1");
			parameters.put("guts", "0");
			parameters.put("ppOverride", "");
			parameters.put("query", URLEncoder.encode(getSongName(), "UTF-8"));
			parameters.put("type", "Songs");
			header.put("session", sessionID);
			header.put("token", communicationToken);
			header.put("client", "htmlshark");
			header.put("clientRevision", "20130520");
			header.put("country", country);
			header.put("uuid", USER_UUID);
			header.put("privacy", 0);
			parent.put("parameters", parameters);
			parent.put("header", header);
			parent.put("method", "getResultsFromSearch");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parent;
	}

	private JSONObject getComunicationToken() {
		JSONObject parent = null;
		JSONObject header = null;
		JSONObject parameters = null;
		try {
			parameters = new JSONObject();
			parent = new JSONObject();
			header = new JSONObject();
			parameters.put("secretKey", md5Custom(cookies.get("PHPSESSID")));
			header.put("client", "htmlshark");
			header.put("clientRevision", "20130520");
			header.put("session", cookies.get("PHPSESSID"));
			header.put("uuid", USER_UUID);
			parent.put("parameters", parameters);
			parent.put("header", header);
			parent.put("method", "getCommunicationToken");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parent;
	}
	
	private JSONObject getStreamKey(String songId) {
		JSONObject parent = null;
		JSONObject header = null;
		JSONObject country = null;
		JSONObject parameters = null;
		try {
			parameters = new JSONObject();
			parent = new JSONObject();
			header = new JSONObject();
			country = new JSONObject();
			country.put("CC1", "0");
			country.put("CC2", "0");
			country.put("CC3", "0");
			country.put("CC4", "2147483648");
			country.put("ID", "223");
			country.put("IPR", "1");
			parameters.put("guts", "0");
			parameters.put("prefetch", "false");
			parameters.put("mobile", "false");
			parameters.put("country", country);
			header.put("client", "jsqueue");
			header.put("token", communicationToken);
			header.put("session", sessionID);
			header.put("clientRevision", "20130520");
			header.put("privacy", "0");
			header.put("uuid", USER_UUID);
			header.put("country", country);
			parent.put("parameters", parameters);
			parent.put("header", header);
			parent.put("method", "getStreamKeyFromSongIDEx");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parent;
	}

	public String md5Custom(String st) {
		MessageDigest messageDigest = null;
		byte[] digest = new byte[0];

		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(st.getBytes());
			digest = messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		BigInteger bigInt = new BigInteger(1, digest);
		String md5Hex = bigInt.toString(16);

		while (md5Hex.length() < 32) {
			md5Hex = "0" + md5Hex;
		}
		return md5Hex;
	}
	
	public static GroovesharkClient getClient() {
		if (null == client) {
			try {
				client = new GroovesharkClient(true,CONFIGURATION);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}
}