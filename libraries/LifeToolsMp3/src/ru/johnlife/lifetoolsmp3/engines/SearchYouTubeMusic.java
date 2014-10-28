package ru.johnlife.lifetoolsmp3.engines;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.song.YouTubeSong;
import android.util.Log;

public class SearchYouTubeMusic extends SearchWithPages {
	
//	private String link = "https://content.googleapis.com/youtube/v3/search?part=snippet&maxResults=50&q=%s&type=video&key=AIzaSyDUmb30N4rIk-3rrScwuki3219dcOF2nBE";
	private String URL_PATTERN = "https://gdata.youtube.com/feeds/api/videos/-/Music?q=%s&v=2&alt=jsonc&start-index=%s";
//	private String[] resolution = {"default" ,"medium", "high", "standard", "maxres"};
	private String[] resolution = {"sqDefault" ,"hqDefault"};
	public SearchYouTubeMusic(FinishedParsingSongs dInterface, String songName) {
		super(dInterface, songName);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			

			String strLink = String.format(URL_PATTERN, URLEncoder.encode(getSongName(), "UTF-8"), ((page * 25) - 24));
		
			
			JSONObject parent = new JSONObject(readUrl(strLink));
			JSONObject data = parent.getJSONObject("data");
			JSONArray items = data.getJSONArray("items");
			for(int i = 0; i< items.length(); i++) {
				if (items.getJSONObject(i) != null) {
						JSONObject item = items.getJSONObject(i);
						int duartion = item.getInt("duration");
						if (duartion < 1140) {
							String title = item.getString("title").substring((item.getString("title").contains("-") ? item.getString("title").indexOf("-") + 1 : item.getString("title").indexOf(" ") + 1), item.getString("title").length() - 1);
							String author = item.getString("title").substring(0, (item.getString("title").contains("-") ? item.getString("title").indexOf("-") + 1 : item.getString("title").indexOf(" ") + 1)); 
							String watchId = item.getString("id");
							JSONObject thumbnailsObject = item.getJSONObject("thumbnail");
							int pictureArrayLength = thumbnailsObject.length();
							String imageUrl = thumbnailsObject.getString(resolution[pictureArrayLength - 1]);
							addSong(new YouTubeSong(getUrlTask(watchId), imageUrl).setArtistName(author.replace("-", "")).setTitle(title).setDuration((long)(duartion * 1000)));
					}
				}
			}
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Something went wrong :( " + e.getMessage());
		}
		return null;
	}

	private String readUrl(String urlString) throws Exception {
		URL url = new URL(urlString);
		InputStreamReader inp = new InputStreamReader(url.openStream());
		Scanner sc = new Scanner(inp);
		String jsonString = "";
		while (sc.hasNext()) {
			String part = sc.nextLine();
			jsonString = jsonString.concat(part);
		}
		inp.close();
		sc.close();
		return jsonString;
	}
	
	private String  getUrlTask(String watchId) {
		JSONObject h = null;
		String hHash = null;
		String url;
		try {
			url = "http://www.youtube-mp3.org" + sig_url("/a/itemInfo/?video_id="+ watchId +"&ac=www&t=grp&r=" + System.currentTimeMillis());
			h = new JSONObject(readUrl(url).replace("info = ", "").replace(";", ""));
			hHash = h.getString("h");
		} catch (Exception e) {
		} 
		long currentTimeMillis = System.currentTimeMillis();
		String a = "/get?ab=128&video_id="+ watchId + "&h=" + hHash +"&r="+ currentTimeMillis +"." + decode(watchId + currentTimeMillis);
		url = "http://www.youtube-mp3.org"+ sig_url(a);
		return url;
	}
	
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

	static boolean inValue(String str) {
		switch (str) {
		case "a":
			return true;
		case "b":
			return true;
		case "c":
			return true;
		case "d":
			return true;
		case "e":
			return true;
		case "f":
			return true;
		case "g":
			return true;
		case "h":
			return true;
		case "i":
			return true;
		case "j":
			return true;
		case "k":
			return true;
		case "l":
			return true;
		case "m":
			return true;
		case "n":
			return true;
		case "o":
			return true;
		case "p":
			return true;
		case "q":
			return true;
		case "r":
			return true;
		case "s":
			return true;
		case "t":
			return true;
		case "u":
			return true;
		case "v":
			return true;
		case "w":
			return true;
		case "x":
			return true;
		case "y":
			return true;
		case "z":
			return true;
		case "_":
			return true;
		case "&":
			return true;
		case "-":
			return true;
		case "/":
			return true;
		case "=":
			return true;
		}
		return false;
	}

	static int value(String str) {
		switch (str) {
		case "a":
			return 870;
		case "b":
			return 906;
		case "c":
			return 167;
		case "d":
			return 119;
		case "e":
			return 130;
		case "f":
			return 899;
		case "g":
			return 248;
		case "h":
			return 123;
		case "i":
			return 627;
		case "j":
			return 706;
		case "k":
			return 694;
		case "l":
			return 421;
		case "m":
			return 214;
		case "n":
			return 561;
		case "o":
			return 819;
		case "p":
			return 925;
		case "q":
			return 857;
		case "r":
			return 539;
		case "s":
			return 898;
		case "t":
			return 866;
		case "u":
			return 433;
		case "v":
			return 299;
		case "w":
			return 137;
		case "x":
			return 285;
		case "y":
			return 613;
		case "z":
			return 635;
		case "_":
			return 638;
		case "&":
			return 639;
		case "-":
			return 880;
		case "/":
			return 687;
		case "=":
			return 721;
		}
		return 0;
	}

	public static double start(String H) {
		String[] r3 = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		double[] L = { 1.23413, 1.51214, 1.9141741, 1.5123114, 1.51214, 1.2651 };
		double F = 1;
		double N = 3219;
		char[] M = { 'a', 'c', 'b', 'e', 'd', 'g', 'm', '-', 's', 'o', '.', 'p', '3', 'r', 'u', 't', 'v', 'y', 'n' };
		int[][] X = { { 17, 9, 14, 15, 14, 2, 3, 7, 6, 11, 12, 10, 9, 13, 5 }, { 11, 6, 4, 1, 9, 18, 16, 10, 0, 11, 11, 8, 11, 9, 15, 10, 1, 9, 6 } };

		F = L[1 % 2];
		String W = gh(), S = gs(X[0], M), T = gs(X[1], M);
		if (ew(W, S) || ew(W, T)) {
			F = L[1]; 					// in this case all will be ok.
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

	private static String gs(int[] I, char[] B) {
		String J = "";
		for (int R = 0; R < I.length; R++) {
			J += B[I[R]];
		}
		;
		return J;
	};

	private static boolean ew(String I, String B) {
		System.out.println(I.indexOf(B, I.length() - B.length()));
		return I.indexOf(B, I.length() - B.length()) != -1;
	};

	private static String gh() {
		return "youtube-mp3.org";
	};

	private static int fn(String[] I, String B) {
		for (int R = 0; R < I.length; R++) {
			if (I[R].equals(B)) {
				return R;
			}

		}
		return -1;
	};

	static String sig(String a) {
		String b = "X";
		b = String.valueOf(start(a));
		if ("X" != b) return b;
		return "-1";
	}

	static String sig_url(String a) {
		String b = sig(a);
		return a + "&s=" + (b.contains(".0") ? b.replace(".0", "") : b);
	}
}