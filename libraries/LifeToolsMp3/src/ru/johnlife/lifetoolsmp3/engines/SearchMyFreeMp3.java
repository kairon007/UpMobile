package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;

public class SearchMyFreeMp3 extends SearchWithPages {

	private String URL_PATTERN = "http://www.myfreemp3.cc/mp3/%s?page=%s";
	private String partServer = "http://89.248.172.6/dvv.php?q=";

	public SearchMyFreeMp3(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... params) {
		
		
		try {
			String strLink = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"), page);
			
			if(!checkConnection(strLink)) {
				return null;
			}
			Document document = Jsoup.connect(strLink).get();
			Element playlist = document.select("ul.playlist").first();
			if (null == playlist || null == playlist.children()) {
				Log.d("log", "request is invalid");
				return null;	
			}
			for (Element element : playlist.children()) {
				Element info = element.getElementsByTag("a").first();
				String[] src = info.text().split(" - ");
				String artist = src[0];
				String title;
				if (src[1].contains("mp3")) {
					title = src[1].replaceAll("mp3", "");
				} else if (src[1].contains(".mp3")) {
					title = src[1].replaceAll(".mp3", "");
				} else {
					title = src[1];
				}
				String srcDuration = info.attr("data-duration");
				long duration = Long.valueOf(srcDuration) * 1000;
				String idSong = info.attr("data-aid");
				String link = partServer + idSong + "/";
				addSong(new RemoteSong(link).setTitle(title).setArtistName(artist).setDuration(duration));
			}
		} catch (Exception e) {
			Log.d("log", "parsing error, couse:" + e.getMessage());
		}
		return null;
	}
}