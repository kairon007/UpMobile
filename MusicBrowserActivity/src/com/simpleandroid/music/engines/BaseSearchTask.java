package com.simpleandroid.music.engines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class BaseSearchTask extends AsyncTask<Void, Void, Void> {
	public static final Class[] PARAMETER_TYPES = new Class[]{FinishedParsingSongs.class, String.class, Context.class};
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

	private List<OnlineSong> songsList = new ArrayList<OnlineSong>();
	private boolean downloadStopped = false;
	private boolean finished;
	private FinishedParsingSongs dInterface;
	private String songName;
	protected Context context;

	public BaseSearchTask(FinishedParsingSongs dInterface, String songName, Context context) {
		this.dInterface = dInterface;
		this.songName = songName;
		this.context = context;
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
	
	protected static String getRandomUserAgent() {
		return agents[new Random().nextInt(agents.length)];
	}
	
	@Override
	protected void onPostExecute(Void result) {
		finished = true;
		if (downloadStopped) return;
		dInterface.onFinishParsing(songsList);
		super.onPostExecute(result);
	}

	public void stopDownload() {
		downloadStopped = true;
	}

	protected void addSong(OnlineSong song) {
		songsList.add(song);
	}

	public String getSongName() {
		return songName;
	}

}