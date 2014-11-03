package ru.johnlife.lifetoolsmp3.song;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;
import android.util.Log;

public class YouTubeSong extends SongWithCover {

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.0 Chrome/26.0.1410.43 Safari/535.12";
	private static final String YOUTUBE_MP3_URL = "http://www.youtube-mp3.org";
	private static final String PENDING = "pending";
	private String largeCoverUrl;
	private String watchId;
	private AsyncTask<Void, Void, String> getUrl;

	public YouTubeSong(String watchId, String largeCoverUrl) {
		super(watchId);
		this.watchId = watchId;
		this.largeCoverUrl = largeCoverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}

	private class Updater extends TimerTask {

		@Override
		public void run() {
			String result = getUrlTask(watchId);
			if (!PENDING.equals(result) && result.startsWith("http")) {
				downloadUrl = result;
				downloadUrlListener.success(result);
				this.cancel();
			}
		}
	}
	
	@Override
	public void cancelTasks() {
		if (null != getUrl) {
			getUrl.cancel(true);
		}
		downloadUrlListener = null;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(watchId);
		return false;
	}
	
	private void getDownloadUrl(final String watchId) {

		getUrl = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				try {
					Response connectResponse = Jsoup
							.connect(YOUTUBE_MP3_URL + "/")
							.method(Method.GET)
							.userAgent(USER_AGENT)
							.followRedirects(true)
							.timeout(10000)
							.execute();
					String pushItem = "/a/pushItem/?item=" + "https%3A//www.youtube.com/watch%3Fv%3D" + watchId + "&el=na&bf=" + "false" + "&r=" + System.currentTimeMillis();
					Response pushItemResponse = Jsoup
							.connect(YOUTUBE_MP3_URL + sig_url(pushItem))
							.userAgent(USER_AGENT)
							.cookies(connectResponse.cookies())
							.header("Cache-Control", "no-cache")
							.header("Accept-Location", "*")
							.header("Accept-Language", "ru-RU")
							.referrer("http://www.youtube-mp3.org/?c")
							.ignoreContentType(true).timeout(10000)
							.followRedirects(true)
							.method(Method.GET)
							.execute();
					new Timer().schedule(new Updater(), 3000, 3000);
					return watchId;
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage());
				}
				return watchId;
			}

			@Override
			protected void onPostExecute(String result) {
			}

			@Override
			protected void onCancelled(String result) {
				downloadUrlListener.error(result);
			}
		}.execute();
	}
		
	public static String getUrlTask(String watchId) {
		JSONObject h = null;
		String hHash = null;
		String status = null;
		try {
			Document doc = Jsoup.connect(YOUTUBE_MP3_URL + sig_url("/a/itemInfo/?video_id=" + watchId + "&ac=www&t=grp&r=" + System.currentTimeMillis())).ignoreContentType(true).get();
			h = new JSONObject(doc.body().text().replace("info = ", "").replace(";", ""));
			hHash = h.getString("h");
			status = h.getString("status");
			if (PENDING.equals(status) || "loading".equals(status) || "converting".equals(status)) {
				return PENDING;
			}
		} catch (Exception e) {
		}
		if (hHash == null) return watchId;
		long currentTimeMillis = System.currentTimeMillis();
		String getItem = "/get?ab=128&video_id=" + watchId + "&h=" + hHash + "&r=" + currentTimeMillis + "." + decode(watchId + currentTimeMillis);
		return YOUTUBE_MP3_URL + sig_url(getItem);
	}

	public static int decode(String a) {
		int secret = 65521;
		int b = 1, c = 0, d, e;
		for (e = 0; e < a.length(); e++) {
			d = a.charAt(e);
			b = (b + d) % secret;
			c = (c + b) % secret;
		}
		return c << 16 | b;
	}

	public static boolean inValue(String str) {
		switch (str) {
			case "a": return true;
			case "b": return true;
			case "c": return true;
			case "d": return true;
			case "e": return true;
			case "f": return true;
			case "g": return true;
			case "h": return true;
			case "i": return true;
			case "j": return true;
			case "k": return true;
			case "l": return true;
			case "m": return true;
			case "n": return true;
			case "o": return true;
			case "p": return true;
			case "q": return true;
			case "r": return true;
			case "s": return true;
			case "t": return true;
			case "u": return true;
			case "v": return true;
			case "w": return true;
			case "x": return true;
			case "y": return true;
			case "z": return true;
			case "_": return true;
			case "&": return true;
			case "-": return true;
			case "/": return true;
			case "=": return true;
		}
		return false;
	}

	public static int value(String str) {
		switch (str) {
			case "a": return 870;
			case "b": return 906;
			case "c": return 167;
			case "d": return 119;
			case "e": return 130;
			case "f": return 899;
			case "g": return 248;
			case "h": return 123;
			case "i": return 627;
			case "j": return 706;
			case "k": return 694;
			case "l": return 421;
			case "m": return 214;
			case "n": return 561;
			case "o": return 819;
			case "p": return 925;
			case "q": return 857;
			case "r": return 539;
			case "s": return 898;
			case "t": return 866;
			case "u": return 433;
			case "v": return 299;
			case "w": return 137;
			case "x": return 285;
			case "y": return 613;
			case "z": return 635;
			case "_": return 638;
			case "&": return 639;
			case "-": return 880;
			case "/": return 687;
			case "=": return 721;
		}
		return 0;
	}

	public static double start(String H) {
		String[] r3 = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		double[] L = { 1.23413, 1.51214, 1.9141741, 1.5123114, 1.51214, 1.2651 };
		double F = 1;
		double N = 3219;
		char[] M = { 'a', 'c', 'b', 'e', 'd', 'g', 'm', '-', 's', 'o', '.', 'p', '3', 'r', 'u', 't', 'v', 'y', 'n' };
		int[][] X = { { 17, 9, 14, 15, 14, 2, 3, 7, 6, 11, 12, 10, 9, 13, 5 }, 
					  { 11, 6, 4, 1, 9, 18, 16, 10, 0, 11, 11, 8, 11, 9, 15, 10, 1, 9, 6 } };

		F = L[1 % 2];
		String W = gh(), S = gs(X[0], M), T = gs(X[1], M);
		if (ew(W, S) || ew(W, T)) {
			F = L[1]; // in this case all will be ok.
		} else {
			F = L[(5 % 3)];
		}
		for (int Y = 0; Y < H.length(); Y++) {
			String Q = String.valueOf(H.toCharArray()[Y]).toLowerCase();
			if (fn(r3, Q) > -1) {
				N = N + (Integer.parseInt(Q) * 121 * F);
			} else {
				if (inValue(Q)) {
					N = N + (value(Q) * F);
				}
			}
			N = N * 0.1;
		}
		N = Math.round(N * 1000);
		return N;
	};

	public static String gs(int[] I, char[] B) {
		String J = "";
		for (int R = 0; R < I.length; R++) {
			J += B[I[R]];
		}
		;
		return J;
	};

	public static boolean ew(String I, String B) {
		return I.indexOf(B, I.length() - B.length()) != -1;
	};

	public static String gh() {
		return "youtube-mp3.org";
	};

	public static int fn(String[] I, String B) {
		for (int R = 0; R < I.length; R++) {
			if (I[R].equals(B)) {
				return R;
			}

		}
		return -1;
	};

	public static String sig(String a) {
		String b = "X";
		b = String.valueOf(start(a));
		if ("X" != b)
			return b;
		return "-1";
	}

	public static String sig_url(String a) {
		String b = sig(a);
		return a + "&s=" + (b.contains(".0") ? b.replace(".0", "") : b);
	}
}