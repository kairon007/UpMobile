package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;

public class SearchPleerV2 extends SearchWithPages {
	
	private final String TAG_ID = "id";
	private final String TAG_DOWNLOAD = "download";
	private final String TAG_ACTION = "action";
	private String URL_PATTERN = "http://pleer.com/search?q=%s&target=tracks&page={%s}";
	private final String PLEER_API_URL = "http://pleer.com/site_api/files/get_url";

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
			Document document = Jsoup.connect(url).get();
			Element listSongs = document.select("ol.scrolledPagination").first();
			for (Element element : listSongs.children()) {
				String strId = element.attr("link");
				if (strId.isEmpty()) continue;
				String title = element.attr("song");
				String artist = element.attr("singer");
				long duration = Long.valueOf(element.attr("duration")) * 1000;
				Connection.Response response = Jsoup.connect(PLEER_API_URL)
													.ignoreContentType(true)
													.data(TAG_ACTION, TAG_DOWNLOAD)
													.data(TAG_ID, strId)
													.method(Method.POST)
													.execute();
				JSONObject jsonObject = new JSONObject(response.body());
				String link = jsonObject.getString("track_link");
				addSong(new RemoteSong(link).setDuration(duration).setSongTitle(title).setArtistName(artist));
			}
		} catch (Exception e) {
			android.util.Log.d("logks", "in " + getClass().getName() + " appear problem: " + e);
		}
		return null;
	}
	
}