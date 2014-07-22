package org.kreed.vanilla.zaycev;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class GetAsyncJSON extends AsyncTask<Void, Void, String> {
	protected static String INIT_TOKEN_URL = "http://zaycev.net/external/hello";
	protected static String AUTHENTICATION_TOKEN_URL = "http://zaycev.net/external/auth?code=%s&hash=%s";
	protected static String TAG_TOKEN = "token";

	@Override
	protected String doInBackground(Void... params) {
		Log.d("log", "GetAsyncJSON:: doInBackground");

		return getAccessToken();
	}

	/**
	 * 
	 * @return <strong>String:</strong> AccessToken
	 */
	private static String getAccessToken() {
		ServiceHandler sh = new ServiceHandler();
		String jsonStr = sh.makeServiceCall(INIT_TOKEN_URL, ServiceHandler.GET);
		String initKey = "";
		String resultKey = "";
		if (jsonStr != null) {
			try {
				JSONObject jsonObj = new JSONObject(jsonStr);
				initKey = jsonObj.getString(TAG_TOKEN);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Log.e("log", "Couldn't get any data from the url");
		}
		Log.d("log", "initKey: " + initKey);
		String shaKey = "llf7116f22c";
		String hashKey = md5(initKey + encryptB(shaKey));
		Log.d("log", "hashKey: " + hashKey);
		jsonStr = sh.makeServiceCall(String.format(AUTHENTICATION_TOKEN_URL, initKey, hashKey), ServiceHandler.GET);
		if (jsonStr != null) {
			try {
				JSONObject jsonObj = new JSONObject(jsonStr);
				resultKey = jsonObj.getString(TAG_TOKEN);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Log.e("log", "Couldn't get any data from the url");
		}
		Log.d("log", "resultKey: " + resultKey);
		return resultKey;
	}

	private static String encryptA(int paramInt1, int paramInt2) {
		String str = "000000" + Integer.toBinaryString(paramInt1);
		return str.substring(str.length() - paramInt2);
	}

	private static String encryptB(String paramString) {
		int i = 0;
		StringBuilder localStringBuilder1 = new StringBuilder();
		byte[] arrayOfByte = paramString.toLowerCase().getBytes();
		int j = arrayOfByte.length;
		for (int k = 0; k < j; k++)
			localStringBuilder1.append(encryptA("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(arrayOfByte[k]), 5));
		localStringBuilder1.setLength(localStringBuilder1.length() - localStringBuilder1.length() % 6);
		int m = localStringBuilder1.length();
		StringBuilder localStringBuilder2 = new StringBuilder();
		while (i < m) {
			localStringBuilder2.append("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(Integer.parseInt(localStringBuilder1.substring(i, i + 6), 2)));
			i += 6;
		}
		return localStringBuilder2.toString();
	}

	private final static String md5(final String s) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}
