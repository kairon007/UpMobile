package ru.johnlife.lifetoolsmp3.engines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;

public class SearchVmusice extends BaseSearchTask {
	
	public SearchVmusice(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	private static String VMUSIC_URL = "http://vmusice.net/mp3/";
	private static String VMUSIC_URL_REF = "http://vmusice.net/";
	
	@Override
	protected Void doInBackground(Void... params) {
		String url = VMUSIC_URL + getSongName();
		try {
			// Connect to the web site
			Response res = Jsoup.connect(url)
					.method(Method.GET)
					.timeout(10000)
					.execute();
			Document document = res.parse();
			Map<String, String> cookies = res.cookies();
			for (Element songItem : document.select("li.x-track")) {
				try {
					String author = songItem.select("strong").text();  
					String name = songItem.select("span").last().text().replace(author, "").replace(" - ", "");
					String duration = songItem.select("em").text();
					String downloadUrl = songItem.select("a.download").first().attr("href");
					String playUrl = songItem.select("a").attr("data-url");
					ArrayList<String[]> headers = new ArrayList<String[]>();
			    	headers.add(new String[] {"Referer",VMUSIC_URL_REF});
			    	headers.add(new String[] {"Range", "bytes=0-"});
					addSong(new RemoteSong(downloadUrl).setSongTitle(name).setArtistName(author).setDuration(formatTime(duration)).setHeader(headers));
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Error parsing song", e);
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("error", "vmusic");
		}
		return null;
	}
	
	public long formatTime(String duration) {
		duration = duration.replace("(", "0").replace(")", "");
		long durationLong;
		int min = Integer.valueOf(duration.substring(0, 2));
		int sec = Integer.valueOf(duration.substring(3, 5));
		durationLong = (min * 60 * 1000) +  (sec * 1000);
		return durationLong;
	}

}
