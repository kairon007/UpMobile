package ru.johnlife.lifetoolsmp3.engines;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;


public class SearchPoisk extends SearchWithPages {
	
	private String Tag = SearchPoisk.class.getSimpleName();
	private static String POISK_URL = "http://www.mp3poisk.net/%s?page=%s";
	private static String MP3_POISK_BASE = "http://www.mp3poisk.net/";
	
	public SearchPoisk(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		
		
		String urlMp3poisk = null;
		try {

			urlMp3poisk = String.format(POISK_URL, URLEncoder.encode(getSongName(), "UTF-8"), page);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		downloadAndParseMp3poisk(urlMp3poisk);
		return null;
	}

	/**
	 * 
	 * @param url
	 */
	public void downloadAndParseMp3poisk(String url) {

		try {
			Document doc = Jsoup.connect(url.toString())
					.userAgent(getRandomUserAgent())       
					.get();  
			Elements container = doc.getElementsByAttributeValue("class", "template_song-list");
			Elements rawSongs = container.select("li");
			for (Element song : rawSongs) {
				String link = "";
				try {
					Elements index = song.getElementsByClass("player-actions");
					String downloadUrl = index.get(0).child(0).attr("href");
					link = MP3_POISK_BASE + downloadUrl; 
				} catch(Exception e) {
					Log.e(getClass().getSimpleName(), "", e);
					continue;
				}
				RemoteSong realSong = new RemoteSong(link);
				String artistName = ""; 
				String title  = "";
				String duration = "";
				try {
					Elements time = song.getElementsByClass("time");
					duration = time.text();
					Elements rawArtist = song.getElementsByClass("song-artist"); 
					artistName = rawArtist.get(0).text();
					if (rawArtist.size() > 1) {
						title = rawArtist.get(1).text();
					} else {
						try {
							Elements rawTitle = song.getElementsByClass("song-name"); 
							if (rawTitle.size() > 0) {		 
								title = rawTitle.get(0).text();
							} else {
								if (artistName.length() > 0) {
									String[] items = artistName.split(" - ");
									if (items.length == 1) items = artistName.split(" — ");
									if (items.length == 2) {
										title = items[1];
										artistName = items[0];
									}
								}
							}
						} catch(Exception e) {
							Log.e(getClass().getSimpleName(), "", e);
						}
					}
					
					realSong.setArtistName(artistName);
					realSong.setSongTitle(title);
					realSong.setDuration(formatTime(duration));
				} catch(Exception e) {
					Log.e(getClass().getSimpleName(), "", e);
				}
				addSong(realSong);
				Log.d("1","1" + title);
			}
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}

	}
	public long formatTime(String duration) {
		long durationLong;
		int min = Integer.valueOf(duration.substring(0, 2));
		int sec = Integer.valueOf(duration.substring(3, 5));
		durationLong = (min * 60 * 1000) +  (sec * 1000);
		return durationLong;
	}

}
