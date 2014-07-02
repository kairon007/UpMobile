package org.kreed.vanilla.engines;

import android.util.Log;

import com.scilor.grooveshark.API.Base.GroovesharkAudioStream;
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
			if (results.length == 0 ) {
				return null;
			} else {
				for (SearchArtistResult result : results) {
					addSong(new GrooveSong(result));
					GroovesharkAudioStream stream = client.GetMusicStream(result.SongID);
					byte[] buffer = new byte[4096];
					int readBytes = stream.Stream().read(buffer);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public static GroovesharkClient getClient() {
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


//	TextView tvShow;
//	private static String version = "Pre-Alpha v19";
//	private static boolean help = false;
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//		
//		tvShow = (TextView) findViewById(R.id.tvShow);
//		Log.e("OnCreate?", "Yes");
//		try {
//			tryConnect();
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			listSearch("Eminem");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try {
//			downloadSongFromSearch("Eminem", 1, Environment.getExternalStorageDirectory().toString());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//	}
//
//	private static void tryConnect() throws Exception {
//		
//		
//	 	
//	}
//
//	private static void downloadSongFromSearch(String search, int id,String pathWhereToDownloadSong) throws Exception {
//		SearchArtist.SearchArtistResult[] results = client.SearchArtist(search).result.result;
//		if (results.length > id) {
//			downloadSong(results[id],pathWhereToDownloadSong);
//		} else {
//			if (results.length == 0 ) {
//				Log.e("!!!","No results for " + "\"" + search + "\"");
//			} else {
//				Log.e("!!!","Your id" + "(" + (id + 1) + ")" + " is greater then the count of results" + "(" + results.length + ")");
//			}
//		}
//	}
//	
//	
//	
//	private static void downloadSong(SearchArtistResult song, String pathWhereToDownloadSong) throws Exception {
//		Log.e("!!!","Download: " + song.ArtistName + " - " + song.Name);
//		GroovesharkAudioStream stream = client.GetMusicStream(song.SongID);
//		String filename = fixFilename(song.ArtistName + " - " + song.Name + " - " + song.AlbumName) + ".mp3";
//	
//		FileOutputStream writer = new FileOutputStream(pathWhereToDownloadSong+"/"+filename);
//		int readBytes = 0;
//		int pos=0;
//		int percentage = 0;
//		int prevPercentage = 0;
//		String lastOutput = null;
//		do {
//			byte[] buffer = new byte[4096];
//			readBytes = stream.Stream().read(buffer);
//			pos += readBytes;
//			if (readBytes > 0) writer.write(buffer, 0, readBytes);
//			percentage = 100 * pos / (stream.Length() - 1);
//			if (percentage > prevPercentage + 4) {
//				lastOutput = percentage + "%" + " \"" + filename + "\"";
//				Log.e("!!!",lastOutput+"<");
//				prevPercentage = percentage;
//			}
//		} while (readBytes > 0);
//		stream.MarkSongAsDownloaded();
//		writer.flush();
//		writer.close();
//		stream.Stream().close();
//	}
//	
//	private static String fixFilename(String filename) {
//		String tmpString = "";
//		for (int i=0; i<filename.length(); i++) {
//			try {
//				if (filename.charAt(i) != '/' && filename.charAt(i) != '\\') {
//					tmpString +=  filename.charAt(i);
//				}
//			} catch(Exception ex) { }
//		}
//		return tmpString.replace("\"", "");
//	}
//
//}
