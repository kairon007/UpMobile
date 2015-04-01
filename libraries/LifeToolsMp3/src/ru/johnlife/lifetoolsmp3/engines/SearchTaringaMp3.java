package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.text.TextUtils;


public class SearchTaringaMp3 extends SearchWithPages {
	
	private static String URL_PATTERN = "http://taringamp3.net/descargar-musica/%s/%s/";

	public SearchTaringaMp3(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			String link = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"), page);
			if (!checkConnection(link)) {
				return null;
			}
			Document doc = Jsoup.connect(link).get();
			Elements items = doc.select("li.cplayer-sound-item");
			for (Element el : items) {
				RemoteSong song;
				song = onParse(el);
				addSong(song);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private RemoteSong onParse(Element el) throws Exception {
		String url = el.select("li.cplayer-sound-item").attr("data-download-url");
		String artist = Util.removeSpecialCharacters(el.select("i.cplayer-data-sound-author").text());
		String title = Util.removeSpecialCharacters(el.select("b.cplayer-data-sound-title").text());
		String duration = el.select("em.cplayer-data-sound-time").text();
		RemoteSong song = new RemoteSong(url);
		song.setArtist(artist);
		song.setTitle(title);
		song.setDuration((long) parseTime(duration) * 1000);
		return song;
	}
	
	private int parseTime(String time) {
		if (TextUtils.isEmpty(time))
			return -1;
		String[] s = time.split(":");
		if (s.length < 2)
			return -1;
		return (Integer.parseInt(s[0]) * 60) + Integer.parseInt(s[1]);
	}

}
