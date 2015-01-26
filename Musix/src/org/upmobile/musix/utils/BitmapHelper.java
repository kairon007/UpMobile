package org.upmobile.musix.utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.view.WindowManager;

import java.io.InputStream;

/**
 * Created by Gustavo on 02/07/2014.
 */
public class BitmapHelper {

    Context mContext;
    WindowManager windowManager;

    public BitmapHelper(Context context) {
        mContext = context;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public Bitmap decodeSampledBitmapFromResourceMemOpt(InputStream inputStream, int reqWidth, int reqHeight) {

        Bitmap result = null;

        byte[] byteArray = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {

                    if (count + len > byteArray.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArray, 0, newbuf, 0, count);
                        byteArray = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArray, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(byteArray, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            result = BitmapFactory.decodeByteArray(byteArray, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public Bitmap scaleToDisplayAspectRatio(Bitmap bitmap) {
        if (bitmap != null) {
            boolean flag = true;

            Point displaySize = new Point();
            int deviceWidth, deviceHeight;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

                windowManager.getDefaultDisplay().getSize(displaySize);
                deviceWidth = displaySize.x;
                deviceHeight = displaySize.y;

            } else {
                deviceWidth = windowManager.getDefaultDisplay().getWidth();
                deviceHeight = windowManager.getDefaultDisplay().getHeight();

            }

            int bitmapHeight = bitmap.getHeight();
            int bitmapWidth = bitmap.getWidth();

            // Aspect ratio: siempre es Width x Height (Alto x Ancho)
            if (bitmapWidth > deviceWidth) {
                flag = false;

                // scale According to WIDTH
                int scaledWidth = deviceWidth;
                int scaledHeight = (scaledWidth * bitmapHeight) / bitmapWidth;

                try {
                    if (scaledHeight > deviceHeight)
                        scaledHeight = deviceHeight;

                    bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (flag) {
                if (bitmapHeight > deviceHeight) {
                    // scale According to HEIGHT
                    int scaledHeight = deviceHeight;
                    int scaledWidth = (scaledHeight * bitmapWidth) / bitmapHeight;

                    try {
                        if (scaledWidth > deviceWidth)
                            scaledWidth = deviceWidth;

                        bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return bitmap;
    }
}
