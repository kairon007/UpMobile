package org.kreed.vanilla.engines;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.util.Log;

public class SearchSoundCloud extends BaseSearchTask { 
	private int specialIndex = 0;
	
	public SearchSoundCloud(FinishedParsingSongs dInterface, String songName, Context context) {
		super(dInterface, songName, context);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			specialIndex=0;
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			songName = songName.replace("%20", "_");
			String link="http://api.soundcloud.com/tracks.json?client_id=2fd7fa3d5ed2be9ac17c538f644fc4c6&filter=downloadable&q="+songName;
			StringBuffer sb = readLinkApacheHttp(link); 
			int i=1;
			String songString;
			do {
				songString = searchNext(sb.toString());
				if (songString!=null) {
					Log.e("Melodie",songString.toString());
					RemoteSong song = new RemoteSong(getDownloadUrl(songString)+"?client_id=2fd7fa3d5ed2be9ac17c538f644fc4c6");
					song.setArtistName("artistname"+i);
					String titlu = getTitle(songString);
					song.setTitle(titlu);
					song.setArtistName(getArtistName(getTitle(songString)));
					
					if (titlu != null && (titlu.toLowerCase().contains("remix") || titlu.toLowerCase().contains("mash up") || titlu.toLowerCase().contains("cover") || titlu.toLowerCase().contains(" mix") || titlu.toLowerCase().contains(" mashup"))) {
						addSong(song); 
					}
				}
			} while (songString != null);
		} catch (UnsupportedEncodingException e) {
			Log.e(getClass().getSimpleName(), "", e);
		}

		return null;
	}

	private String getArtistName(String title)
	{
		int indexOfDownloadUrl = title.indexOf("-");
		return (indexOfDownloadUrl == -1) ? title : title.substring(0, indexOfDownloadUrl);
	}

	private String getTitle(String melodie2)
	{
		//"title":"Eminem, Slaughterhouse, Yelawolf - Shady 2.0 Cypher"
			
		int indexOfDownloadUrl = melodie2.indexOf("\"title\"");
		int indexOfDouaPuncte = melodie2.indexOf(":",indexOfDownloadUrl);
		int indexOfApostrofOne = melodie2.indexOf("\"",indexOfDouaPuncte)+1;
		int indexOfApostrofDoi = melodie2.indexOf("\"",indexOfApostrofOne);
			
			
		// TODO Auto-generated method stub
		return melodie2.substring(indexOfApostrofOne, indexOfApostrofDoi).replace("\\", "").replace("/", "");
	}

	private String getDownloadUrl(String melodie2)
	{
		int indexOfDownloadUrl = melodie2.indexOf("download_url");
		int indexOfDouaPuncte = melodie2.indexOf(":",indexOfDownloadUrl);
		int indexOfApostrofOne = melodie2.indexOf("\"",indexOfDouaPuncte)+1;
		int indexOfApostrofDoi = melodie2.indexOf("\"",indexOfApostrofOne);
		return melodie2.substring(indexOfApostrofOne, indexOfApostrofDoi);
	}

	private String searchNext(String tot)
	{
		
		int index = specialIndex;
		int indexOfDownloadUrl = tot.indexOf("download_url",index);
		if(indexOfDownloadUrl==-1)
			return null;
		specialIndex = indexOfDownloadUrl+12;
		
		
		String partial = tot.substring(0,indexOfDownloadUrl);
		int indexLastTrack = partial.lastIndexOf("\"kind\":\"track\"");
		int indexLastTrack2 = tot.indexOf("\"kind\":\"track\"", indexOfDownloadUrl);
		if(indexLastTrack2==-1)
		{
			return tot.substring(indexLastTrack);
		}
		else
		{
			return tot.substring(indexLastTrack,indexLastTrack2);
		}
		
	}

}
