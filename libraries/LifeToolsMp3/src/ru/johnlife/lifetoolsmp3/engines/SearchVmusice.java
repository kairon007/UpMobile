package ru.johnlife.lifetoolsmp3.engines;

import java.io.IOException;

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
	
	@Override
	protected Void doInBackground(Void... params) {
		String url = VMUSIC_URL + getSongName();
		try {
			// Connect to the web site
			Document document = Jsoup.connect(url).get();
			for (Element songItem : document.select("li.x-track")) {
				try {
					String author = songItem.select("strong").text();  
					String name = songItem.select("span").last().text().replace(author, "").replace(" – ", "");  
					String duration = songItem.select("em").text();
					String downloadUrl = songItem.select("a.download").first().attr("href");
					Log.d("tag download", downloadUrl);
					addSong(new RemoteSong(downloadUrl).setTitle(name).setArtistName(author).setDuration(formatTime(duration)));
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
