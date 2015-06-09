package ru.johnlife.lifetoolsmp3.engines.lyric;

import java.net.URLEncoder;

import org.jsoup.nodes.Document;

public class SongLyrics extends BaseLyricsSearchTask {
	
	private String url = "http://www.songlyrics.com/%s/%s-lyrics/";

	public SongLyrics(String artist, String title, OnEnginesListener searchLyrics) {
		super(artist, title, searchLyrics);
	}

	@Override
	protected String doInBackground(String... params) {
		StringBuilder lyrics = new StringBuilder();
		try {
			String sTitle = URLEncoder.encode(title, "UTF-8");
			String sArtist = URLEncoder.encode(artist, "UTF-8");
			sTitle = sTitle.contains("+") ? sTitle.replaceAll("\\+", "-") : sTitle;
			sArtist =sArtist.contains("+") ? sArtist.replaceAll("\\+", "-") : sArtist;
			url = String.format(url, sArtist.trim(), sTitle.trim());
			Document body = readUrlResp(url).parse();
			lyrics.append(body.select("p#songLyricsDiv").html());
			if (lyrics.toString().contains("Sorry, we have no")) return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return lyrics.toString().isEmpty() ? null: lyrics.toString();
	}

}
