package org.kreed.vanilla.engines;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kreed.vanilla.song.RemoteSong;

public class SearchMp3World extends BaseSearchTask {
	
	private static final Pattern SONG_TITLE_PATTERN = Pattern.compile("([^-]*)-(.*)mp3");
	
	public SearchMp3World(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	private static String MP3_WORLD_URL = "http://emp3world.com/search/";

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String songName = getSongName().replace(" ", "_");
			String urlMp3World = MP3_WORLD_URL + songName + "_mp3_download.html";
			Document doc = Jsoup.connect(urlMp3World).timeout(10000).get();
			Elements searchResults = doc.getElementsByAttributeValue("id", "results_box");
			if (searchResults.size() > 0) {
				Elements rawSongs = searchResults.get(0).getElementsByAttributeValue("class", "song_item");
				for (Element songElement : rawSongs) {
					Elements rawLink = songElement.getElementsByAttributeValue("type", "hidden");
					RemoteSong song = new RemoteSong(rawLink.get(0).attr("value").split(",")[0]);
					Elements rawTitle = songElement.getElementsByAttributeValue("id", "song_title");
					String songTitle = rawTitle.get(0).text();
					Matcher matcher = SONG_TITLE_PATTERN.matcher(songTitle);
					if (matcher.find()) {
						song.setTitle(matcher.group(2).trim());
						song.setArtistName(matcher.group(1).trim());
						addSong(song);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
