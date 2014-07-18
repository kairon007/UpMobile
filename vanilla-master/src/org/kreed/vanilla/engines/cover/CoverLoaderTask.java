package org.kreed.vanilla.engines.cover;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public abstract class CoverLoaderTask extends AsyncTask<Void, Void, Bitmap> {

	public interface OnBitmapReadyListener {
		public void onBitmapReady(Bitmap bmp);
	}
	
	protected String artist;
	protected String title;
	protected List<OnBitmapReadyListener> listeners = new ArrayList<OnBitmapReadyListener>();
	
	public CoverLoaderTask(String artist, String title) {
		this.artist = artist;
		this.title = title;
	}
	
	public void addListener(OnBitmapReadyListener listener) {
		listeners.add(listener);
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		for (OnBitmapReadyListener listener : listeners) {
			if (null != listener) {
				listener.onBitmapReady(result);
			}
		}
	}
	
}