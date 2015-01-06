package ru.johnlife.lifetoolsmp3.engines.lyric;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class LyricsFetcher {
	
	private static final String TAG = "AZLyricsViewer:LyricsFetcher";
	private static final boolean DEBUG = false;

	public interface OnLyricsFetchedListener {
		abstract void onLyricsFetched(boolean foundLyrics, String lyrics);
	}
	
	private class FetchLyrics extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {
			try {
				Document document = Jsoup.connect(arg0[0]).get();
				Element div = document.getElementsByAttributeValue("style", "margin-left:10px;margin-right:10px;").first();
				String response = div.toString();
				Pattern p = Pattern.compile("<!-- start of lyrics -->(.*)<!-- end of lyrics -->", Pattern.DOTALL);
				Matcher matcher = p.matcher(response);
				if (matcher.find()) {
					String htmlLyrics = matcher.group(1);
					return htmlLyrics;
				} else {
					if (DEBUG) Log.i(TAG, "doesn't match");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (mListener != null) {
				mListener.onLyricsFetched(result != null, result);
			}
			super.onPostExecute(result);
		}
	}

	private LyricsFetcher.OnLyricsFetchedListener mListener = null;

	public LyricsFetcher(Context context) {
	}

	public void fetchLyrics(String songName, String artistName) {
		Locale locale = Locale.getDefault();
		if (!checkParameter(songName, artistName)) {
			new FetchLyrics().execute("");
		}
		String urlString = "http://www.azlyrics.com/lyrics/" + deleteSpecialCharacters(artistName.toLowerCase(locale)) + "/" + deleteSpecialCharacters(songName.toLowerCase(locale)) + ".html";
		new FetchLyrics().execute(urlString);
	}

	private boolean checkParameter(String songName, String artistName) {
		if (songName != null && artistName != null) {
			return true;
		}
		return false;
	}

	private String deleteSpecialCharacters(String name) {
		name = name.replaceAll(" ", "").replaceAll("-", "").replaceAll("'", "").replaceAll("!", "").replaceAll("\"", "");
		return name;
	}

	public void setOnLyricsFetchedListener(OnLyricsFetchedListener listener) {
		mListener = listener;
	}
}