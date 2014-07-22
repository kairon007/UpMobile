package org.kreed.vanilla.engines;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.util.Log;

public class SearchVmusice extends BaseSearchTask {
	
	public SearchVmusice(FinishedParsingSongs dInterface, String songName, Context context) {
		super(dInterface, songName, context);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		String url = "http://vmusice.net/aj.php?p=0&mp3=" + getSongName();
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
