package ru.johnlife.lifetoolsmp3.engines.cover;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class CoverLoaderTask implements ImageLoadingListener {

	public interface OnBitmapReadyListener {
		public void onBitmapReady(Bitmap bmp);
	}
		
	protected List<OnBitmapReadyListener> listeners = new ArrayList<OnBitmapReadyListener>();
	private String coverUrl;
	
	protected CoverLoaderTask() {}
	
	public CoverLoaderTask(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	
	public void addListener(OnBitmapReadyListener listener) {
		listeners.add(listener);
	}
	
	public void execute() {
		try {
			if (coverUrl == null || coverUrl.equals("NOT_FOUND") || "".equals(coverUrl)) {
				Log.e(getClass().getSimpleName(), "Error, cover not found from engines");
				return;
			}
		    ImageLoader.getInstance().loadImage(coverUrl, this);			
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error while reading links contents", e);
		}
		return;
	}

	@Override
	public void onLoadingCancelled(String arg0, View arg1) {}

	@Override
	public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
		for (OnBitmapReadyListener listener : listeners) {
			if (null != listener) {
				listener.onBitmapReady(arg2);
			}
		}
	}

	@Override
	public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {}

	@Override
	public void onLoadingStarted(String arg0, View arg1) {}
}