package org.kreed.vanilla.zaycev;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.kreed.vanilla.engines.BaseSearchTask;
import org.kreed.vanilla.engines.FinishedParsingSongs;

public class SearchZaycev extends BaseSearchTask {
	protected String accessToken;

	public SearchZaycev(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			if (accessToken == null) {

			}
			String songName = URLEncoder.encode(getSongName(), "UTF-8").replace(" ", "%20");
			String link = "http://zaycev.net/search.html?query_search=" + songName + "&page=1&access_token=" + accessToken;

			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void getAccessToken() {
		new GetAsyncJSON().execute();
	}

}
