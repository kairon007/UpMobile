package ru.johnlife.lifetoolsmp3.engines.lyric;

import android.annotation.SuppressLint;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;

public class AzLyrics extends BaseLyricsSearchTask {

	private static final String AZLYRICS_URL = "http://www.azlyrics.com/";

	public AzLyrics(String artist, String title, OnEnginesListener searchLyrics) {
		super(artist, title, searchLyrics);
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected String doInBackground(String... params) {
		String url = AZLYRICS_URL + "lyrics/" + artist.toLowerCase() + "/" + title.toLowerCase() + ".html";
		String lyrics = "";
		try {
			Document body = readUrlResp(url).parse();
			StringBuilder builder = new StringBuilder();
			builder.append(body.select("div.row"));
			String response = builder.toString();
			if (response.isEmpty() || !response.contains("start of lyrics")) return null;
			String start = "<!-- Usage of azlyrics.com content by any third-party lyrics provider is prohibited by our licensing agreement. Sorry about that. -->";
			String end = "<!-- MxM banner -->";
			Pattern p = Pattern.compile(start + "(.*)" + end, Pattern.DOTALL);
			Matcher matcher = p.matcher(response);
			if (matcher.find()) {
				String htmlLyrics = matcher.group(1);
				return htmlLyrics;
			}
			return lyrics.isEmpty() ? null : lyrics;
		} catch (Exception e) {
			return null;
		}
	}

}
