package ru.johnlife.lifetoolsmp3.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public class MusicApp extends Application {
	
	public static Typeface FONT_LIGHT;
	public static Typeface FONT_REGULAR;
	public static Typeface FONT_BOLD;
	
	private static final DisplayImageOptions defaultImageOptions = new DisplayImageOptions.Builder()
		.showImageOnLoading(android.R.drawable.ic_menu_gallery)
		.showImageForEmptyUri(android.R.drawable.ic_menu_gallery)
		.cacheInMemory(true)
		.cacheOnDisc(true)
	    .imageScaleType(ImageScaleType.EXACTLY) 
	    .bitmapConfig(Bitmap.Config.RGB_565)
		.build(); 

	protected static SharedPreferences prefs;

	@SuppressLint("NewApi") 
	@Override
	public void onCreate() {
		super.onCreate();
		ImageLoaderConfiguration.Builder b = new ImageLoaderConfiguration.Builder(getApplicationContext());
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			b.taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
			 .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	    b.threadPoolSize(7)
	        .defaultDisplayImageOptions(defaultImageOptions)
	        .memoryCache(new LruMemoryCache(20 * 1024 * 1024))
	        .discCacheFileCount(500);
	    ImageLoader.getInstance().init(b.build());
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	public static SharedPreferences getSharedPreferences() {
		return prefs;
	}
}