/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.simpleandroid.music;

import java.io.FileDescriptor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.simpleandroid.music.BuildConfig;
import com.simpleandroid.music.ImageCache;
import com.simpleandroid.music.ApolloUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.support.v4.util.LruCache;

/**
 * Used to turn off logging for jaudiotagger and free up memory when
 * {@code #onLowMemory()} is called on pre-ICS devices. On post-ICS memory is
 * released within {@link ImageCache}.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
@SuppressLint("NewApi")
public class MusicPlayerApp extends Application {

	private static final BitmapFactory.Options BITMAP_OPTIONS = new BitmapFactory.Options();
	public static Typeface FONT_LIGHT;
	public static Typeface FONT_REGULAR;
	public static Typeface FONT_BOLD;
	
	static {
		BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.RGB_565;
		BITMAP_OPTIONS.inDither = false;
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        // Enable strict mode logging
        // enableStrictMode();
        // Turn off logging for jaudiotagger.
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
		if (sCoverCache == null) {
			sCoverCache = new CoverCache(this);
		}
		FONT_BOLD = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.otf");
		FONT_REGULAR = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Regular.otf");
		FONT_LIGHT = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.otf");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLowMemory() {
        ImageCache.getInstance(this).evictAll();
        super.onLowMemory();
    }

    @TargetApi(11)
    private void enableStrictMode() {
        if (ApolloUtils.hasGingerbread() && BuildConfig.DEBUG) {
            final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog();
            final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog();

            if (ApolloUtils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }
    
	/**
	 * A cache of 6 MiB of covers.
	 */
	public static class CoverCache extends LruCache<Long, Bitmap> {
		private final Context mContext;

		public CoverCache(Context context)
		{
			super(6 * 1024 * 1024);
			mContext = context;
		}

		@Override
		public Bitmap create(Long key)
		{
			Uri uri =  Uri.parse("content://media/external/audio/media/" + key + "/albumart");
			ContentResolver res = mContext.getContentResolver();

			try {
				ParcelFileDescriptor parcelFileDescriptor = res.openFileDescriptor(uri, "r");
				if (parcelFileDescriptor != null) {
					FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
					return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, BITMAP_OPTIONS);
				}
			} catch (Exception e) {
				// no cover art found
			}

			return null;
		}

		@Override
		protected int sizeOf(Long key, Bitmap value)
		{
			return value.getRowBytes() * value.getHeight();
		}
	}
    
	/**
	 * The cache instance.
	 */
	private static CoverCache sCoverCache = null;
	
	public static CoverCache getCoverCache() {
		return sCoverCache;
	}
	
}