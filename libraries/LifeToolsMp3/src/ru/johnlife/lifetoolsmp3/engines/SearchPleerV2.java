package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.johnlife.lifetoolsmp3.song.PleerV2Song;

public class SearchPleerV2 extends SearchWithPages {
	
	private String URL_PATTERN = "http://pleer.com/search?q=%s&target=tracks&page={%s}";

	public SearchPleerV2(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		String url;
		try {
			url = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"), page);
			if (!checkConnection(url)) {
				return null;
			}
			Document document = Jsoup.connect(url)
					.followRedirects(true)
					.userAgent(getRandomUserAgent())
					.ignoreContentType(true)
					.get();
			Element listSongs = document.select("ol.scrolledPagination").first();
			for (Element element : listSongs.children()) {
				String strId = element.attr("link");
				if (strId.isEmpty()) continue;
				String title = element.attr("song");
				String artist = element.attr("singer");
				long duration = Long.valueOf(element.attr("duration")) * 1000;
				addSong(new PleerV2Song(strId).setDuration(duration).setSongTitle(title).setArtistName(artist));
			}
		} catch (Exception e) {
			android.util.Log.d(getClass().getSimpleName(), e.getMessage());
		}
		return null;
	}
	
}