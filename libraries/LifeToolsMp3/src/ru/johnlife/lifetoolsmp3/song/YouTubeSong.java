package ru.johnlife.lifetoolsmp3.song;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class YouTubeSong extends SongWithCover{
	
	private String largeCoverUrl;
	private String watchId;
	private boolean isLoaded  = false;
	private String parsingUrl;
	
	public YouTubeSong(String watchId, String largeCoverUrl) {
		super(watchId);
		this.watchId = watchId;
		this.largeCoverUrl = largeCoverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}
	
	@Override
	public String getDownloadUrl(Context context) {
			try {
				downloadUrl = getDownloadUrl(watchId, context);
			} catch (IOException e) {
				Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage());
			}
		return downloadUrl;
	}
	
	//TODO: this parsing not work. Return an empty string.
	
	public String getDownloadUrl(final String watchId, Context context) throws IOException {
		String downloadUrl = null;
		String h = null;
				try {
					String scriptStr =  javaScript(context).replace("replacethis", "http://www.youtube-mp3.org/a/itemInfo/?video_id=" + watchId + "&ac=www&t=grp&r=" + String.valueOf(System.currentTimeMillis()));
					android.util.Log.d("logd", "getDownloadUrl" + scriptStr);
					Document script = Jsoup.connect("http://www.compileonline.com/webpage.php")
							.referrer("http://www.compileonline.com/try_javascript_online.php")
							.userAgent("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.0 Chrome/26.0.1410.43 Safari/535.12")
//							.data("lang", "javascript")
//							.data("html",scriptStr)
//							.data("css", "")
//							.data("javascript", "")
//							.data("inputs","")
//							.method(Method.GET)
							.get();
					android.util.Log.d("logd", script.getElementById("view").select("body").text());
					Document hDoc = Jsoup.connect("http://www.youtube-mp3.org/a/itemInfo/?video_id=" + watchId + "&ac=www&t=grp&r=" + String.valueOf(System.currentTimeMillis()) + "&s=" + Math.random() * 100000)
							.header("Accept-Location", "*")
							.header("Cache-Control", "no-cache")
							.userAgent("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7")
							.ignoreContentType(true)
							.timeout(10000)
							.get();
					android.util.Log.d("logd", "http://www.youtube-mp3.org/a/itemInfo/?video_id=" + watchId + "&ac=www&t=grp&r=" + String.valueOf(System.currentTimeMillis()) + "&s=172524");
					h = new JSONObject(hDoc.body().text().replace("info = ", "").replace(";", "")).getString("h");
					android.util.Log.d("logd", "doInBackground " + h);
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Something went wrong :(" + e.getMessage());
				}
				downloadUrl = "http://www.youtube-mp3.org/get?ab=128&video_id=" + watchId +"&h=" + h +"&r=" + System.currentTimeMillis() + "." + decode(watchId + System.currentTimeMillis()) + "&s=";
				android.util.Log.d("logd", downloadUrl);
				return downloadUrl;
	}
	
/*	class LoadListener {
		@JavascriptInterface
		public void processHTML(String html) {
			Document doc = Jsoup.parse(html);
			parsingUrl = doc.getElementById("dl_link").select("a").get(2).attr("href");
			isLoaded = true;
			android.util.Log.d("logd", doc.getElementById("dl_link").select("a").get(2).attr("href"));
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled") public String getDownloadUrl(final String watchId, Context context) throws IOException {
		WebView view = new WebView(context);
		view.getSettings().setJavaScriptEnabled(true);
		view.addJavascriptInterface(new LoadListener(), "HTMLOUT");
		view.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
			}
		});
		view.loadUrl("http://www.youtube-mp3.org/?e=session_expired&r=true#v=" + watchId);
		while (!isLoaded) {
			Log.d(getClass().getSimpleName(), "Loading...");
		}
		view.
		return "http://www.youtube-mp3.org/" + parsingUrl;
	}*/
	
	public int decode(String a) {
		int secret = 65521;
		int b = 1, c = 0, d, e;
		for (e = 0; e < a.length(); e++) {
			d = a.charAt(e);
			b = (b + d) % secret;
			c = (c + b) % secret;
		}
		return c << 16 | b;
	}
	
	private String javaScript (Context context) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/JavaScript.txt"));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
}
