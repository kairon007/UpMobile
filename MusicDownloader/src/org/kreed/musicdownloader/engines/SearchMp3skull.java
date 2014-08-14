package org.kreed.musicdownloader.engines;


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import org.kreed.musicdownloader.song.RemoteSong;

import android.util.Log;



public class SearchMp3skull extends BaseSearchTask {
	public SearchMp3skull(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	private static String MP3_SKULL_URL = "http://mp3skull.com/mp3/";

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			songName = songName.replace("%20", "_");
			String link = MP3_SKULL_URL + songName + ".html";
			System.out.println(link);
			// Send data
			StringBuffer sb = readLink(link);

			String[] searchRI = sb.toString().split("<div id=\"song_html\" class=\"show");
			for (int i = 1; i < searchRI.length; i++) {
				try {
					String songData = searchRI[i];

					String songURL = songData.substring(songData.indexOf("<a href=\""));
					songURL = songURL.substring(0, songURL.indexOf("\" rel=\"nofollow\""));
					songURL = songURL.replace("<a href=\"", "");

					String songTitle = songData.substring(songData.indexOf("<div style=\"font-size:15px;\"><b>"));
					songTitle = songTitle.substring(0, songTitle.indexOf("</b></div>"));
					songTitle = songTitle.replace("<div style=\"font-size:15px;\"><b>", "");
					songTitle = songTitle.replace(" mp3", "");

					String[] songArtistTitle = songTitle.split(" - ", 2);
					if (songArtistTitle.length == 2) {

						String songArtist = songArtistTitle[0];
						songTitle = songArtistTitle[1];

						if (songURL != "" && songTitle != "") {
							RemoteSong song = new RemoteSong(songURL);
							song.setTitle(songTitle);
							song.setArtistName(songArtist);
							addSong(song);
						}
					}
				} catch (StringIndexOutOfBoundsException e) {
					Log.e(getClass().getSimpleName(), "", e);
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.e(getClass().getSimpleName(), "", e);
				}
			}
		} catch (MalformedURLException e) {
			Log.e(getClass().getSimpleName(), "", e);
		} catch (UnsupportedEncodingException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}
		return null;
	}

}
