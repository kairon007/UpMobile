package ru.johnlife.lifetoolsmp3.engines.lyric;

import java.net.URLEncoder;

import org.jsoup.nodes.Document;

public class MetroLyrics extends BaseLyricsSearchTask {
	
	private String url = "http://www.metrolyrics.com/printlyric/%s-lyrics-%s.html";

	public MetroLyrics(String artist, String title, OnEnginesListener searchLyrics) {
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
			url = String.format(url, sTitle.trim(), sArtist.trim());
			Document body = readUrlResp(url).parse();
			lyrics.append(body.select("p.verse").html());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return lyrics.toString().isEmpty() ? null: lyrics.toString();
	}

}
