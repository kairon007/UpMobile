package org.kreed.vanilla.engines;

import java.net.URLEncoder;
import java.util.ArrayList;

import android.content.Context;
import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SearchSoundCloudV2 extends BaseSearchTask {
	
	private static final String APP_CLIENT_ID = "f66e127e703b3896443f7f5f0fc49075";
	private static final String APP_CLIENT_SECRET = "112295d92bd30c61374d9fbf61dc9f6d";
	
	public SearchSoundCloudV2(FinishedParsingSongs dInterface, String songName, Context context) {
		super(dInterface, songName, context);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			SoundCloud soundcloud = new SoundCloud(APP_CLIENT_ID, APP_CLIENT_SECRET);
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			ArrayList<Track> result = soundcloud.findTrack(songName);
			if(result != null) {
			    for(Track track : result) {
			    	if (track.isStreamable()) {
				    	String downloadUrl = track.getStreamUrl();
				    	String largeCoverUrl = track.getArtworkUrl();
				    	String[] pair = getPair(track.getTitle());
				    	String songArtist = pair[0].trim();
				    	String songTitle = pair[1].trim();
				    	SoundCloudV2Song song = new SoundCloudV2Song(downloadUrl, largeCoverUrl);
						song.setArtistName(songArtist);
						song.setTitle(songTitle);
						addSong(song);
			    	}
			    }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String[] getPair(String title) {
    	if (title.contains("-")) {
    		return title.split("-");
    	} else if (title.contains("\"")) {
    		return title.split("\"");
    	} else if (title.contains("'")) {
    		return title.split("'");
    	} else if (title.contains("  ")) {
    		return title.split("  ");
    	} else {
    		String[] pair = {title, title};
    		return pair;
    	}
	}
	
}