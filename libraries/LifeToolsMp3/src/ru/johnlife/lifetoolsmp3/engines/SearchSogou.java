package ru.johnlife.lifetoolsmp3.engines;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import android.util.Log;


/**
 * This class is used to search / search and download a song from the http://mp3.sogou.com website.
 * SetSearchMod must be used before execute() command to initiate the search query, id of the song to download and path.
 * If the id parameter is left with the value of -1 the class will only perform a search.
 * 
 * To extract the results of the search an interface can be used. This feature is not implemented
 * 
 * @author Cristi
 *
 */
public class SearchSogou extends SearchWithPages {

	/* CONSTANTS */
	private static String basicUrl = "http://mp3.sogou.com/music.so?query=%s&xiamipage=1&dt=other&page=%s";
	private static int MIN_CHINESE = 13312;
	
	/* CONSTRUCTOR */
	/**
	 * This is the constructor used to search only.
	 * @param con - the Cotext of the activity
	 */
	public SearchSogou(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}


	@Override
	protected Void doInBackground(Void... params) {
		try {
			String link = String.format(basicUrl, URLEncoder.encode(getSongName(), "UTF-8"), page);
			Document doc = Jsoup.connect(link)
					.userAgent("Mozilla/5.0 (iPad; CPU OS 6_1 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10B141 Safari/8536.25")
					.get();
			
			Elements songs = doc.body().select("div[id=otherResult]").first().select("div[class=music_list]").select("tr");
			if (songs.isEmpty()) return null;
			//This eliminates the first row which is not a song element
			songs.remove(0);

			for (Element song : songs) {
				
				String title = song.select("td[class=on_hover]").select("div[class=tit]").select("dt").text();
				String artist = song.select("div[class=name]").select("a").attr("title");
				String album = song.select("td").get(3).text();
				String downloadUrl = song.select("td").get(5).select("a").attr("onclick"); //This is testing if there is a button
				String directLink = null; // This is the direct link from which to download
				
				boolean downloadLinkOk = false;
				boolean hasDownloadButton = (downloadUrl.length() > 1);
				if (hasDownloadButton) { // Meaninig that we have the button
					// Start extracting the direct link
					directLink = findDirectLink(song.select("td").get(4).select("a").attr("onClick"));
					downloadLinkOk = (directLink != null);
				}
				
				// testing 
				boolean isntChinese = (checkWord(title) && checkWord(artist) && checkWord(album));
				if (isntChinese && hasDownloadButton && downloadLinkOk) {
					addSong(new RemoteSong(directLink).setArtistName(artist).setSongTitle(title));
				}
			}
			
		
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), "IOError", e);
		}
		return null;
	}
	
	/**
	 * This function is testing if the string received as parameter has any chinese characters
	 * @param string
	 * @return true if is not chinese, false if is chinese
	 */
	private static boolean checkWord(String string) {
		for (int i = 0; i < string.length(); i++){
			if (Integer.valueOf(String.valueOf(string.codePointAt(i)), 16) > MIN_CHINESE){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets the link from the string obtained from the parsing page, from the attribute "onClick"
	 * @param string
	 * @return
	 */
	private static String findDirectLink(String string) {
		String[] parts = string.split("#");
		if (parts[5].contains("http")) {
			return parts[5];
		} else {
			for (String part : parts) {
				if (part.contains("http")) return part;
			}
		}
		return null;
	}
	
}
