package ru.johnlife.lifetoolsmp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Vector;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public final class Util {
	
	public final static String WHITE_THEME = "AppTheme.White";
	private final static DateFormat isoDateFormat = new SimpleDateFormat("mm:ss", Locale.US);
	private final static int SMALL_BITMAP_SIZE = 100;
	private final static String ZAYCEV_TAG = "(zaycev.net)";
	
	public static long formatTime(String duration) {
		long durationLong;
	    int curSor = duration.indexOf(":");
	    if (curSor == 2) {
	    	int min = Integer.valueOf(duration.substring(0, 2));
	    	int sec = Integer.valueOf(duration.substring(3, 5));
			durationLong = (min * 60 * 1000) +  (sec * 1000);
	    }
	    else {
	    	int min = Integer.valueOf(duration.substring(0, 3));
	    	int sec = Integer.valueOf(duration.substring(4, 6));
			durationLong = (min * 60 * 1000) +  (sec * 1000);
	    }
		return durationLong;
	}
	public static String getFormatedStrDuration(long l) {
		if (l > Integer.MAX_VALUE)
			return "> 20 days";
			int duration = (int) l;
			duration /= 1000;
			int min = duration / 60;
			int sec = duration % 60;
			int h = min / 60;
			min = min % 60;
			if (h>0)
				return String.format("%d:%02d:%02d", h, min, sec);
			else 
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
		Bitmap small = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), m, false);
		return small;
	}
	
	public static int existFile(String parentName, String fileName) {
		int result = 0;
		File file = new File(parentName);
		File[] fileArray = file.listFiles();
		for (int i = 0; i < fileArray.length; i++) {
			File f = fileArray[i];
			String fName = f.getName().split(".mp3")[0];
			if(fName.contains("-<")){
				fName = fName.split("-<")[0];
			}
			if (fileName.equals(fName)){
				result++;
				continue;
			}
		}
		return result;
	}
	
	public synchronized static Bitmap getArtworkImage(int maxWidth, MusicMetadata metadata) {
		if (maxWidth == 0) {
			return null;
		}
		Vector<ImageData> pictureList = metadata.getPictureList();
		if ((pictureList == null) || (pictureList.size() == 0)) {
			return null;
		}
		ImageData imageData = (ImageData) pictureList.get(0);
		if (imageData == null) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inPurgeable = true;
		int scale = 1;
		if ((maxWidth != -1) && (opts.outWidth > maxWidth)) {
			// Find the correct scale value. It should be the power of 2.
			int scaleWidth = opts.outWidth;
			while (scaleWidth > maxWidth) {
				scaleWidth /= 2;
				scale *= 2;
			}
		}
		opts = new BitmapFactory.Options();
		opts.inSampleSize = scale;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeByteArray(imageData.imageData, 0, imageData.imageData.length, opts);
		} catch (OutOfMemoryError e) {
			Log.d("log", "ru.johnlife.lifetoolsmp3.Util.getArtworkImage :" + e.getMessage());
			bitmap.recycle();
			bitmap = null;
		}
		return bitmap;
	}
	
	public synchronized static void copyFile(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (null != is && null != os) {
				is.close();
				os.close();
			}
		}
	}
	
	public static String getThemeName(Context context)
	{
	    PackageInfo packageInfo;
	    try {
	        packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
	        int themeResId = packageInfo.applicationInfo.theme;
	        return context.getResources().getResourceEntryName(themeResId);
	    } catch (Exception e) {
	    	Log.e(Util.class.getSimpleName(), e.getMessage());
	        return null;
	    }
	}
	
	public static String removeSpecialCharacters(String str) {
		while (str.endsWith(" ")) {
			str = str.substring(0, str.length() - 1);
		}
		return str.toString().replaceAll("\\\\", "-").replaceAll("/", "-").replaceAll(ZAYCEV_TAG, "");
	}
}
