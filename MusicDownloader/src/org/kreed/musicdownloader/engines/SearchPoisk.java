package org.kreed.musicdownloader.engines;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.kreed.musicdownloader.song.RemoteSong;

import android.util.Log;


public class SearchPoisk extends BaseSearchTask {
	
	private String Tag = SearchPoisk.class.getSimpleName();
	private static String POISK_URL = "http://www.mp3poisk.net/";
	private static final DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
	public SearchPoisk(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		String songName = "";
		try {
			songName = URLEncoder.encode(getSongName(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}
		String urlMp3poisk = POISK_URL + songName;
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
					link = "http://www.mp3poisk.net/" + downloadUrl; 
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
					realSong.setTitle(title);
					realSong.setDuration(isoDateFormat.parse(duration).getTime());
				} catch(Exception e) {
					Log.e(getClass().getSimpleName(), "", e);
				}
				addSong(realSong);  
				Log.i(Tag, "values = " + link);
			}
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}

	}

}
