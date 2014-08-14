package org.kreed.musicdownloader.engines;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kreed.musicdownloader.song.RemoteSong;


public class SearchHulkShare extends BaseSearchTask {
	private static final Pattern SONG_TITLE_PATTERN = Pattern.compile("<b>([^<]*).*<a href[^>]*>([^<]*)");
	private static String hulkshareBaseUrl = "https://www.hulkshare.com/dl/";
	private static String hulkshareSuffix = "/hulkshare.mp3?d=1";
	private static String hulkshareSearchUrl = "http://www.hulkshare.com/search.php?q="; 
	
	public SearchHulkShare(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			songName = songName.replace("%20", "+");
			String link = hulkshareSearchUrl + songName + "&p=1&per_page=20";
			StringBuffer sb = readLink(link);
			String[] searchRI = sb.toString().split("<div class=\"searchResultsItem\">");
			for (int i = 1; i < searchRI.length; i++) {
				try {
					String content = searchRI[i];
					String songData = content.substring(content.indexOf("<div class=\"resTP\">"));
					songData = songData.substring(0, songData.indexOf("</div>"));
					String songURL = songData.substring(songData.indexOf("<a href=\"/"));
					songURL = songURL.substring(0, songURL.indexOf("\"><b>"));
					songURL = songURL.replace("<a href=\"/", "");
					//songURL = "http://www.hulkshare.com/ap-" + songURL+ "/&ref=.mp3"; 
					songURL = hulkshareBaseUrl + songURL + hulkshareSuffix;
					 
					Matcher m = SONG_TITLE_PATTERN.matcher(songData);
					if (m.find()) {
						String songTitle = m.group(1);
						String songArtist = m.group(2);
						if (songURL != "" && songTitle != "") {
							RemoteSong song = new RemoteSong(songURL);
							song.setTitle(songTitle);
							song.setArtistName(songArtist);
							addSong(song);
						}
					}
				} catch (StringIndexOutOfBoundsException e) {
					e.printStackTrace();
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return null;
	}
}
