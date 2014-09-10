package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import android.util.Log;
import ru.johnlife.lifetoolsmp3.song.SoundCloudV2Song;
import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SearchSoundCloudV2 extends BaseSearchTask {
	
	private static String APP_CLIENT_ID = "b28035537c669e1d5f232dcbf2b32dc4";
	private static String APP_CLIENT_SECRET = "1d8677051ec9f25e767161507a12b7db"; 
	
	public SearchSoundCloudV2(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			SoundCloud soundcloud = new SoundCloud(APP_CLIENT_ID, APP_CLIENT_SECRET);
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			ArrayList<Track> result = soundcloud.findTrack(songName);
			if(result != null) {
			    for(Track track : result) {
			    		String downloadUrl = "http://api.soundcloud.com/tracks/" + track.getId() + "/stream?client_id=2Kf29hhC5mgWf62708A";
				    	String largeCoverUrl = track.getArtworkUrl();
				    	String[] pair = getPair(track.getTitle());
				    	long duration = (long) track.getDuration();
				    	String songArtist = pair[0].trim();
				    	String songTitle = pair[1].trim();
						addSong(new SoundCloudV2Song(downloadUrl, (largeCoverUrl == null || largeCoverUrl.equals("null")) ? "NOT_FOUND" : largeCoverUrl).setArtistName(songArtist).setDuration(duration).setTitle(songTitle));
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