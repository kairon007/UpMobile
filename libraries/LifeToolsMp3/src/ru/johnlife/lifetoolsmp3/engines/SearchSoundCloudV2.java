package ru.johnlife.lifetoolsmp3.engines;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import android.util.Log;
import ru.johnlife.lifetoolsmp3.song.SoundCloudV2Song;
import de.voidplus.soundcloud.SoundCloud;
import de.voidplus.soundcloud.Track;

public class SearchSoundCloudV2 extends BaseSearchTask {
	 
	private static String SOUNDCLOUD_BASE_V2 = "http://api.soundcloud.com/tracks/";
	
	public SearchSoundCloudV2(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		
		
		try {
			
			String soundcloudClientId = getSoundcloudClientId();
			String soundcloudClientSecret = getSoundcloudClientSecret();
			
			SoundCloud soundcloud = new SoundCloud(soundcloudClientId, soundcloudClientSecret);
			String songName = URLEncoder.encode(getSongName(), "UTF-8");
			ArrayList<Track> result = soundcloud.findTrack(songName);
			if(result != null) {
			    for(Track track : result) {
			    		String downloadUrl = SOUNDCLOUD_BASE_V2 + track.getId() + "/stream?client_id=" + soundcloudClientId;
				    	String largeCoverUrl = track.getArtworkUrl();
				    	String[] pair = getPair(track.getTitle());
				    	long duration = (long) track.getDuration();
				    	String songArtist = pair[0].trim();
				    	String songTitle = pair[1].trim();
						addSong(new SoundCloudV2Song(downloadUrl, (largeCoverUrl == null || largeCoverUrl.equals("null")) ? "NOT_FOUND" : largeCoverUrl).setArtistName(songArtist).setDuration(duration).setSongTitle(songTitle));
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