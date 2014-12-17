package ru.johnlife.lifetoolsmp3.engines;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ru.johnlife.lifetoolsmp3.song.RemoteSong;
import ru.johnlife.lifetoolsmp3.song.SoundCloudV1Song;
import android.util.Log;

public class SearchSoundCloud extends SearchWithPages {
	private int specialIndex = 0;
	private int pag;

	public SearchSoundCloud(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	public String getSoundcloudUrl(String clientId) {
		return "http://api.soundcloud.com/tracks.json?client_id=" + clientId + "&filter=downloadable&q=";		
	}
	
	private int getPage() {
		this.pag = page;
		return (pag - 1) * 50;
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		
		try {
			
			String soundcloudClientId = getSoundcloudClientId();
			
			specialIndex = 0;
			if (null == getSongName()) return null;
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			songName = songName.replace("%20", "_");
			String offset = "&offset=" + getPage();
			String link = getSoundcloudUrl(soundcloudClientId) + songName + offset;
			StringBuffer sb = readLinkApacheHttp(link);
			String songString;
			do {
				songString = searchNext(sb.toString());
				if (songString != null) {
					addSong(new SoundCloudV1Song(getDownloadUrl(songString) + "?client_id=" + soundcloudClientId, getImageUrl(songString).equals("ul") ? "NOT_FOUND" : getImageUrl(songString))
							.setArtistName(getArtistName(getTitle(songString)))
							.setSongTitle(getTitle(songString))
							.setDuration(Long.valueOf(getDuration(songString))));
				}
			} while (songString != null);
		} catch (UnsupportedEncodingException e) {
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
		return null;
	}

	private String getArtistName(String title) {
		int indexOfDownloadUrl = title.indexOf("-");
		return (indexOfDownloadUrl == -1) ? title : title.substring(0, indexOfDownloadUrl);
	}

	private String getTitle(String melodie2) {
		// "title":"Eminem, Slaughterhouse, Yelawolf - Shady 2.0 Cypher"

		int indexOfDownloadUrl = melodie2.indexOf("\"title\"");
		int indexOfDouaPuncte = melodie2.indexOf(":", indexOfDownloadUrl);
		int indexOfApostrofOne = melodie2.indexOf("\"", indexOfDouaPuncte) + 1;
		int indexOfApostrofDoi = melodie2.indexOf("\"", indexOfApostrofOne);

		// TODO Auto-generated method stub
		return melodie2.substring(indexOfApostrofOne, indexOfApostrofDoi).replace("\\", "").replace("/", "");
	}

	private String getDownloadUrl(String melodie2) {
		int indexOfDownloadUrl = melodie2.indexOf("download_url");
		int indexOfDouaPuncte = melodie2.indexOf(":", indexOfDownloadUrl);
		int indexOfApostrofOne = melodie2.indexOf("\"", indexOfDouaPuncte) + 1;
		int indexOfApostrofDoi = melodie2.indexOf("\"", indexOfApostrofOne);
		return melodie2.substring(indexOfApostrofOne, indexOfApostrofDoi);
	}

	private String searchNext(String tot) {

		int index = specialIndex;
		int indexOfDownloadUrl = tot.indexOf("download_url", index);
		if (indexOfDownloadUrl == -1)
			return null;
		specialIndex = indexOfDownloadUrl + 12;

		String partial = tot.substring(0, indexOfDownloadUrl);
		int indexLastTrack = partial.lastIndexOf("\"kind\":\"track\"");
		int indexLastTrack2 = tot.indexOf("\"kind\":\"track\"", indexOfDownloadUrl);
		if (indexLastTrack2 == -1) {
			return tot.substring(indexLastTrack);
		} else {
			return tot.substring(indexLastTrack, indexLastTrack2);
		}

	}

	private String getDuration(String item) {
		String startString = "\"duration\":";
		int start = item.indexOf(startString);
		int end = item.indexOf(",\"commentable\"");
		return item.substring(start + startString.length(), end);
	}

	private String getImageUrl(String item) {
		String startString = "\"artwork_url\":";
		int start = item.indexOf(startString);
		int end = item.indexOf(",\"waveform_url\"");
		return item.substring(start + startString.length() + 1, end - 1);
	}

}
