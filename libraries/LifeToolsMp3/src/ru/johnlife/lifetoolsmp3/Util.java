package ru.johnlife.lifetoolsmp3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public final class Util {
	
	private final static DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
	private final static int SMALL_BITMAP_SIZE = 100;
	
	public static String formatTimeIsoDate(long date) {
		return isoDateFormat.format(new Date(date));
	}
	
	public static String formatTimeSimple(int duration) {
		duration /= 1000;
		int min = duration / 60;
		int sec = duration % 60;
		return String.format("%d:%02d", min, sec);
	}
	
	public static Bitmap resizeToSmall(Bitmap original) {
		int originalSize = Math.max(original.getWidth(), original.getHeight());
		float scale = 1;
		while (originalSize > SMALL_BITMAP_SIZE) {
			originalSize /= 2;
			scale /= 2;
		}
		Matrix m = new Matrix();
		m.postScale(scale, scale);
		Log.d("logd", "original size = " + (original.getRowBytes() * original.getHeight()) / 1024 + "kB");
		Bitmap small = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), m, false);
		Log.d("logd", "small size = " + (small.getRowBytes() * small.getHeight()) / 1024 + "kB");
		return small;
	}
}
