package ru.johnlife.lifetoolsmp3.engines.lyric;

import java.io.EOFException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.util.Log;

public class LyricsFetcher {
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
	private static final String AZLYRICS_URL = "http://www.azlyrics.com/";
	private static final String TAG = "AZLyricsViewer:LyricsFetcher";
	private static final String ANONIMIZER = "http://cameleo.ru/r";
	private static final boolean DEBUG = false;

	public interface OnLyricsFetchedListener {
		abstract void onLyricsFetched(boolean foundLyrics, String lyrics);
	}
	
	private class FetchLyrics extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... arg0) {
			Response defaut = null;
			Response anonimizer = null;
			String url = arg0[0];
			try { 
				try {
					defaut = Jsoup.connect(AZLYRICS_URL).userAgent(USER_AGENT)
							.timeout(10000)
							.followRedirects(true)
							.ignoreContentType(true)
							.ignoreHttpErrors(true)
							.method(Method.GET)
							.execute();
				} catch (EOFException e) {
					anonimizer = Jsoup.connect(ANONIMIZER).userAgent(USER_AGENT)
							.timeout(10000)
							.data("url", AZLYRICS_URL)
							.followRedirects(true)
							.ignoreContentType(true)
							.ignoreHttpErrors(true)
							.method(Method.GET)
							.execute();
					url = url.replace(AZLYRICS_URL, anonimizer.url().toString());
				}
				Response res = Jsoup.connect(url)
						.userAgent(USER_AGENT)
						.followRedirects(true)
						.cookies(defaut == null ? anonimizer.cookies() : defaut.cookies())
						.header("Connection", "close")
						.timeout(10000)
						.ignoreHttpErrors(true)
						.method(Method.GET)
						.execute();
				Document document = res.parse();
				StringBuilder builder = new StringBuilder();
				builder.append(document.select("div.row"));
				String response = builder.toString();
				if (response.isEmpty() || !response.contains("start of lyrics")) return null;
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
	private AsyncTask<String, Integer, String> fetchLyrics;

	public LyricsFetcher(Context context) {
		
	}

	@SuppressLint("NewApi")
	public void fetchLyrics(String songName, String artistName) {
		Locale locale = Locale.getDefault();
		if (!checkParameter(songName, artistName)) {
			new FetchLyrics().execute("");
		}
		String urlString = AZLYRICS_URL + "lyrics/" + deleteSpecialCharacters(artistName.toLowerCase(locale)) + "/" + deleteSpecialCharacters(songName.toLowerCase(locale)) + ".html";
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			fetchLyrics = new FetchLyrics().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlString);
		} else {
			fetchLyrics = new FetchLyrics().execute(urlString);
		}
	}

	private boolean checkParameter(String songName, String artistName) {
		if (songName != null && artistName != null) {
			return true;
		}
		return false;
	}
	
	public void cancel() {
		if (null != fetchLyrics) {
			if (fetchLyrics.getStatus() == Status.PENDING || fetchLyrics.getStatus() == Status.RUNNING) {
				fetchLyrics.cancel(true);
			}
		}
	}

	private String deleteSpecialCharacters(String name) {
		name = name.replaceAll(" ", "").replaceAll("-", "").replaceAll("'", "").replaceAll("!", "").replaceAll("\"", "");
		return name;
	}

	public void setOnLyricsFetchedListener(OnLyricsFetchedListener listener) {
		mListener = listener;
	}
}