package org.kreed.vanilla.engines;

import android.util.Log;

import com.scilor.grooveshark.API.Base.GroovesharkClient;
import com.scilor.grooveshark.API.Functions.SearchArtist.SearchArtistResult;

public class SearchGrooveshark extends BaseSearchTask {
	
	private static final String sharkConfig =
		  "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
		+ "<GrooveFix version=\"20130530\">"
	  		+ "<htmlshark>"
		  		+ "<GrooveClient>htmlshark</GrooveClient>"
		  		+ "<GrooveClientRevision>20130520</GrooveClientRevision>"
		  		+ "<GrooveStaticRandomizer>:nuggetsOfBaller:</GrooveStaticRandomizer>"
	  		+ "</htmlshark>"
	  		+ "<jsqueue>"
		  		+ "<GrooveClient>jsqueue</GrooveClient>"
		  		+ "<GrooveClientRevision>20130520</GrooveClientRevision>"
		  		+ "<GrooveStaticRandomizer>:chickenFingers:</GrooveStaticRandomizer>"
	  		+ "</jsqueue>"
	  		+ "<mobileshark>"
		  		+ "<GrooveClient>mobileshark</GrooveClient>"
		  		+ "<GrooveClientRevision>20120112</GrooveClientRevision>"
		  		+ "<GrooveStaticRandomizer>:boomGoesTheDolphin:</GrooveStaticRandomizer>"
	  		+ "</mobileshark>"
	  		+ "<mobileshark>"
		  		+ "<GrooveClient>jsplayer</GrooveClient>"
		  		+ "<GrooveClientRevision>20120124.01</GrooveClientRevision>"
		  		+ "<GrooveStaticRandomizer>:needsMoarFoodForSharks:</GrooveStaticRandomizer>"
	  		+ "</mobileshark>"
  		+ "</GrooveFix>";
	
	private static GroovesharkClient client = null;

	public SearchGrooveshark(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		try {
			SearchArtistResult[] results = getClient().SearchArtist(getSongName()).result.result;
			if (results.length != 0 ) {
				for (SearchArtistResult result : results) {
					String downloadUrl = client.GetStreamKey(result.SongID).result.DirectURL();
					String songArtist = result.ArtistName;
					String songTitle = result.Name;
					addSong(new RemoteSong(downloadUrl).setArtistName(songArtist).setTitle(songTitle));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static GroovesharkClient getClient() {
		if (null == client) {
			try {
			client = new GroovesharkClient(true, sharkConfig);
			} catch (Exception e) {
				Log.e("GroovesharkClient", "Error creating client", e);
			}
		}
		return client;
	}

}