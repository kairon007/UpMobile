package ru.johnlife.lifetoolsmp3.song;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class YouTubeSong extends SongWithCover {

	private static final String CONVERTING = "converting";
	private static final String LOADING = "loading";
	private static final String TS_CREATE = "ts_create";
	private static final String STATUS = "status";
	private static final String H2 = "h2";
	private Timer timer = new Timer();
	private static String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.12 (KHTML, like Gecko) Maxthon/3.0 Chrome/26.0.1410.43 Safari/535.12";
	private static String YOUTUBE_MP3_URL = "http://www.youtube-mp3.org";
	private static String YOUTUBE_MP3_REF = "http://www.youtube-mp3.org/?c";
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
				for (DownloadUrlListener listener : downloadUrlListeners) {
					listener.success(downloadUrl);
				}
				downloadUrlListeners.clear();
				this.cancel();
			}
		}
	}
	
	@Override
	public void cancelTasks() {
		if (null != getUrl) {
			getUrl.cancel(true);
			timer.cancel();
		}
		if (null != timer) {
			timer.cancel();
		}
		downloadUrlListeners.clear();
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
							.referrer(YOUTUBE_MP3_REF)
							.ignoreContentType(true)
							.timeout(10000)
							.followRedirects(true)
							.method(Method.GET)
							.execute();
					timer.schedule(new Updater(), 5000, 3000);
					return watchId;
				} catch (Exception e) {
					Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage());
				}
				return watchId;
			}

			@Override
			protected void onPostExecute(String result) {
			}
		}.execute();
	}
		
	public static String getUrlTask(String watchId) {
		JSONObject h = null;
		String hHash = null;
		String status = null;
		long tsCreate = 0;
		String r = null;
		try {
			Document doc = Jsoup.connect(YOUTUBE_MP3_URL + sig_url("/a/itemInfo/?video_id=" + watchId + "&ac=www&t=grp&r=" + System.currentTimeMillis()))
					.ignoreContentType(true)
					.ignoreHttpErrors(true)
					.userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A")
					.get();
			String body = doc.body().text().toString();
			body = body.replace("info = ", "").replace(";", "");
			body = body.substring(0, (body.indexOf("\"title\": \"")) + 10) + body.substring((body.indexOf("\"title\": \"") + 10), body.indexOf("\", \"h\"")).replace("\"", "") + body.substring(body.indexOf("\", \"h\""), body.indexOf("}")) + "}";
			h = new JSONObject(body);
			hHash = h.getString(H2);
			status = h.getString(STATUS);
			tsCreate = h.getLong(TS_CREATE);
			r = h.getString("r");
			if (PENDING.equals(status) || LOADING.equals(status) || CONVERTING.equals(status)) {
				return PENDING;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (hHash == null) return watchId;
		String getItem = "/get?video_id=" + watchId + "&ts_create=" + tsCreate + "&r=" + r.replace("=", "%3D") +"&h2=" + hHash;
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
		if (str != null) {
			if (str.equals("a")) return true;
			else if (str.equals("b")) return true;
			else if (str.equals("c")) return true;
			else if (str.equals("d")) return true;
			else if (str.equals("e")) return true;
			else if (str.equals("f")) return true;
			else if (str.equals("g")) return true;
			else if (str.equals("h")) return true;
			else if (str.equals("i")) return true;
			else if (str.equals("j")) return true;
			else if (str.equals("k")) return true;
			else if (str.equals("l")) return true;
			else if (str.equals("m")) return true;
			else if (str.equals("n")) return true;
			else if (str.equals("o")) return true;
			else if (str.equals("p")) return true;
			else if (str.equals("q")) return true;
			else if (str.equals("r")) return true;
			else if (str.equals("s")) return true;
			else if (str.equals("t")) return true;
			else if (str.equals("u")) return true;
			else if (str.equals("v")) return true;
			else if (str.equals("w")) return true;
			else if (str.equals("x")) return true;
			else if (str.equals("y")) return true;
			else if (str.equals("z")) return true;
			else if (str.equals("_")) return true;
			else if (str.equals("&")) return true;
			else if (str.equals("-")) return true;
			else if (str.equals("/")) return true;
			else if (str.equals("=")) return true;
		}
		return false;
	}


	public static int value(String str) {
		if (str != null) {
			if (str.equals("a")) return 870;
			else if (str.equals("b")) return 906;
			else if (str.equals("c")) return 167;
			else if (str.equals("d")) return 119;
			else if (str.equals("e")) return 130;
			else if (str.equals("f")) return 899;
			else if (str.equals("g")) return 248;
			else if (str.equals("h")) return 123;
			else if (str.equals("i")) return 627;
			else if (str.equals("j")) return 706;
			else if (str.equals("k")) return 694;
			else if (str.equals("l")) return 421;
			else if (str.equals("m")) return 214;
			else if (str.equals("n")) return 561;
			else if (str.equals("o")) return 819;
			else if (str.equals("p")) return 925;
			else if (str.equals("q")) return 857;
			else if (str.equals("r")) return 539;
			else if (str.equals("s")) return 898;
			else if (str.equals("t")) return 866;
			else if (str.equals("u")) return 433;
			else if (str.equals("v")) return 299;
			else if (str.equals("w")) return 137;
			else if (str.equals("x")) return 285;
			else if (str.equals("y")) return 613;
			else if (str.equals("z")) return 635;
			else if (str.equals("_")) return 638;
			else if (str.equals("&")) return 639;
			else if (str.equals("-")) return 880;
			else if (str.equals("/")) return 687;
			else if (str.equals("=")) return 721;
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

	@Override
	public int describeContents() {
		return 2;
	}

	@Override
	public void writeToParcel(Parcel parcel, int arg1) {
		parcel.writeLong(id);
		parcel.writeString(path);
		parcel.writeString(title);
		parcel.writeString(album);
		parcel.writeLong(albumId);
		parcel.writeString(artist);
		parcel.writeLong(artistId);
		parcel.writeLong(duration);
		parcel.writeInt(trackNumber);
	
	}

	public YouTubeSong (Parcel parcel) {
		super(parcel);
	}	
	
	public static final Parcelable.Creator<YouTubeSong> CREATOR = new Parcelable.Creator<YouTubeSong>() {
		public YouTubeSong createFromParcel(Parcel in) {
			return new YouTubeSong(in);
		}

		public YouTubeSong[] newArray(int size) {
			return new YouTubeSong[size];
		}
	};
}