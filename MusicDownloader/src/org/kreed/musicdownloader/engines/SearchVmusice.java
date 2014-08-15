package org.kreed.musicdownloader.engines;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kreed.musicdownloader.song.RemoteSong;





import android.util.Log;

public class SearchVmusice extends BaseSearchTask {
	
	public SearchVmusice(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	private static String VMUSIC_URL = "http://vmusice.net/mp3/";
	private static final DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
	
	@Override
	protected Void doInBackground(Void... params) {
		String url = VMUSIC_URL + getSongName();
		try {
			// Connect to the web site
			Document document = Jsoup.connect(url).get();
			for (Element songTag : document.select("li.x-track")) {
				try {
					Elements authorElements = songTag.select("strong");
					String author = authorElements.text();
					Elements nameElements = songTag.getElementsByClass("title");
					String name = nameElements.toString();
					String startString = "</strong> – ";
					int start = name.indexOf(startString);
					int end = name.indexOf("</span>");
					name = name.substring(start + startString.length(), end);
					String downloadUrl = songTag.select("a.download").first().attr("href");
					String duration = songTag.select("em").text().replace("(", "").replace(")", "");
					if (duration.length() == 4) {
						duration = "0" + duration;
					}
					addSong(new RemoteSong(downloadUrl).setTitle(name).setArtistName(author).setDuration(isoDateFormat.parse(duration).getTime()));
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
