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
			for (Element songTag : document.select("div.main2")) {
				try {
					Element victim = songTag.select("div.clear").first();
					victim.html(((Comment)songTag.select("a.play").first().nextSibling().nextSibling()).getData());
					String author = victim.select("span#autor").first().text();  
					String name = victim.select("span#title").first().text();  
					String downloadUrl = songTag.select("a.download").first().attr("href");
					addSong(new RemoteSong(downloadUrl).setTitle(name).setArtistName(author));
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Error parsing song: "+songTag, e);
				}
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
