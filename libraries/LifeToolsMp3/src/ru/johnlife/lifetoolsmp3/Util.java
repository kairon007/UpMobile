package ru.johnlife.lifetoolsmp3;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

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
	
	public static Bitmap getArtworkImage(int maxWidth, MusicMetadata metadata) {
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
		Bitmap bitmap = BitmapFactory.decodeByteArray(imageData.imageData, 0, imageData.imageData.length, opts);
		return bitmap;
	}
}
