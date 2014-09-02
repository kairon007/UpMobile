package org.kreed.musicdownloader.app;

import java.io.FileDescriptor;

import org.kreed.musicdownloader.PlayerService;



import ru.johnlife.lifetoolsmp3.app.MusicApp;
import android.app.Application;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;

public class MusicDownloaderApp extends Application {
	private static final BitmapFactory.Options BITMAP_OPTIONS = new BitmapFactory.Options();
	public static Typeface FONT_LIGHT;
	public static Typeface FONT_REGULAR;
	public static Typeface FONT_BOLD;
	private static PlayerService service;
	private static SharedPreferences prefs;
	
	static {
		BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.RGB_565;
		BITMAP_OPTIONS.inDither = false;
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
	
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service  = ((PlayerService.PlayerBinder) binder).getService(); 
		}
		public void onServiceDisconnected(ComponentName name) {}
	};


	@Override
	public void onCreate() {
		super.onCreate();
		if (sCoverCache == null) {
			sCoverCache = new CoverCache(this);
		}
//		FONT_BOLD = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Bold.otf");
//		FONT_REGULAR = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Regular.otf");
//		FONT_LIGHT = Typeface.createFromAsset(getAssets(), "fonts/ProximaNova-Light.otf");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		MusicApp.setSharedPreferences(prefs);
		bindService(new Intent(this, PlayerService.class), serviceConnection, BIND_AUTO_CREATE);
	}
	
	public static SharedPreferences getSharedPreferences() {
		return prefs;
	}

	public static PlayerService getService() {
		return service;
	}
	
}