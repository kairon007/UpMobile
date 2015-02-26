package ru.johnlife.lifetoolsmp3.engines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import ru.johnlife.lifetoolsmp3.app.MusicApp;
import ru.johnlife.lifetoolsmp3.song.Song;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseSearchTask extends AsyncTask<Void, Void, Void> {
	public static final Class<?>[] PARAMETER_TYPES = new Class[]{FinishedParsingSongs.class, String.class};
	private static String[] agents = new String[] {
		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36", 
		"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0", 
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36", 
		"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36", 
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.76.4 (KHTML, like Gecko) Version/7.0.4 Safari/537.76.4", 
		"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:29.0) Gecko/20100101 Firefox/29.0",
		"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)",
	};
	private final static StringBuffer EMPTY_BUFFER = new StringBuffer();
	

	public static String[] blacklist = {"bisbal", "princesa", "calamaro", "goosen", "belugo", "chayanne"};
	
	
	private List<Song> songsList = new ArrayList<Song>();
	private boolean downloadStopped = false;
	private FinishedParsingSongs dInterface;
	private String songName;

	public BaseSearchTask(FinishedParsingSongs dInterface, String songName) {
		this.dInterface = dInterface;
		this.songName = songName;
	}

	

	public String getJamendoClientId() {
		String defaultJamendoClientId = "551aabd5";
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		String jamendoClientId = prefs.getString("jamendo_client_id", defaultJamendoClientId);
		if (jamendoClientId == null || jamendoClientId.equals("")) jamendoClientId = defaultJamendoClientId;
		return jamendoClientId;
	}
	
	public String getSoundcloudClientId() {
		String defaultSoundcloudClientId = "62602b93da08a424fe9518b1607de7dc";
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		String soundcloudClientId = prefs.getString("soundcloud_client_id", defaultSoundcloudClientId);
		if (soundcloudClientId == null || soundcloudClientId.equals("")) soundcloudClientId = defaultSoundcloudClientId;
		return soundcloudClientId;
	}
	
	public static String getSoundcloudClientSecret() {
		String defaultSoundcloudClientSecret = " "; //"49c8b0d16ba2ca7dd2b7c1d915789c5e";
		SharedPreferences prefs = MusicApp.getSharedPreferences();
		String soundcloudClientSecret = prefs.getString("soundcloud_client_secret", defaultSoundcloudClientSecret);
		if (soundcloudClientSecret == null || soundcloudClientSecret.equals("")) soundcloudClientSecret = defaultSoundcloudClientSecret;
		return soundcloudClientSecret;
	}

	
	protected StringBuffer readLink(String link) throws MalformedURLException {
		URL url = new URL(link);
		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", getRandomUserAgent());
			conn.setDoOutput(true);
			conn.setConnectTimeout(3000);
			return readStream(conn.getInputStream());
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}
		return EMPTY_BUFFER;
	}

	protected StringBuffer readLinkApacheHttp(String link) {
		try {
			HttpClient httpclient = new DefaultHttpClient(); // Create HTTP Client
			HttpGet httpget = new HttpGet(link); // Set the action you want to do
			HttpResponse response = httpclient.execute(httpget); // Executeit
			HttpEntity entity = response.getEntity(); 
			return readStream(entity.getContent());
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}
		return EMPTY_BUFFER;
	}
	
	private StringBuffer readStream(InputStream in) throws UnsupportedEncodingException, IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		String line;
		while (!downloadStopped && (line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		return sb;
	}
	
	//without downloadStopped
	protected static String handleLink(String link) {
		try {
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", getRandomUserAgent());
			conn.setDoOutput(true);
			conn.setConnectTimeout(3000);
			StringBuffer sb = new StringBuffer();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	protected static String getRandomUserAgent() {
		return agents[new Random().nextInt(agents.length)];
	}
	

	
	
	// returns true if String s contains any item in arrayList. case insensitive
	public boolean isAnyArrayListItemInsideString(String s, ArrayList<String> arrayList) {
		if (s != null && !s.equals("")) {
			s = s.toLowerCase();
			for (String blacklistedItem : arrayList) {
	        	blacklistedItem = blacklistedItem.toLowerCase();
	        	if (s.contains(blacklistedItem)) {
	        		return true;
	        	}
	        }
		}
		return false;
	}
	
	protected boolean checkConnection(String strLink) {
		try {
			URL url = new URL(strLink);
			HttpsURLConnection connection  = (HttpsURLConnection) url.openConnection();
			if (connection.getResponseCode() == HttpsURLConnection.HTTP_FORBIDDEN
					|| connection.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND){
				Log.d("log", "response is bad, error #" + connection.getResponseCode());
				return false;
			}
		} catch (Exception e) {
			Log.d("log", "SearchMyFreeMp3.checkConnection; exeption - " + e.getMessage());
		} 
		return true;
	}
	
	
	
	@Override
	protected void onPostExecute(Void result) {
		
		
		// filter out songs
		ArrayList<Song> filteredSongList = new ArrayList<Song>();
		ArrayList<String> baseBlacklist = new ArrayList<String>(Arrays.asList(blacklist)); 
        ArrayList<String> dmcaResultBlacklist = getDMCABlacklistedItems("dmca_blacklist");
        
        		
        try {
	        for (Song s : songsList) {
	        	String songTitle = s.getTitle();
	        	String artistName = s.getArtist();
	        	
	        	boolean isEitherSongTitleOrArtistNameContainingBlacklistItem = isAnyArrayListItemInsideString(songTitle, baseBlacklist) || isAnyArrayListItemInsideString(songTitle, dmcaResultBlacklist) || isAnyArrayListItemInsideString(artistName, baseBlacklist) || isAnyArrayListItemInsideString(artistName, dmcaResultBlacklist);
	        	
	  	        if (!isEitherSongTitleOrArtistNameContainingBlacklistItem) filteredSongList.add(s);

	        }
        } catch(Exception e) {
        	
        }
		
        
        songsList = filteredSongList;
        
		if (downloadStopped) return;
		dInterface.onFinishParsing(songsList);
		super.onPostExecute(result);
	}

	public void stopDownload() {
		downloadStopped = true;
	}

	protected void addSong(Song song) {
		songsList.add(song);
	}

	public String getSongName() {
		return songName;
	}
	
	
	
	public ArrayList<String> getDMCABlacklistedItems(String remoteSetting) {
		ArrayList<String> searchEngines = new ArrayList<String>();
		try {
			SharedPreferences prefs = MusicApp.getSharedPreferences();
			String remoteSettingSearchEngines = prefs.getString(remoteSetting, null);
			JSONArray jsonArray = new JSONArray(remoteSettingSearchEngines);
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					String searchEngine = jsonArray.getString(i);
					searchEngines.add(searchEngine); 
				}catch(Exception e) { 
					
				}
			}
		}catch(Exception e) {
			
		}
		
		return searchEngines;
	}
	


}