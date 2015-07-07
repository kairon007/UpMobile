package ru.johnlife.lifetoolsmp3.engines.cover;

import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;


public class GrooveSharkCoverLoaderTask extends CoverLoaderTask {

	private String urlImage;
	
	public GrooveSharkCoverLoaderTask(String urlImage) {
		super();
		this.urlImage = urlImage;
	}
	
	@Override
	public void execute() {
		try {
			if (null == urlImage) {
				for (OnBitmapReadyListener listener : listeners) {
					if (null != listener) {
						listener.onBitmapReady(null);
					}
				}
			} else {
				ImageLoader.getInstance().loadImage(urlImage, this);
			}
		} catch (Throwable e) {
			Log.e(getClass().getSimpleName(), "Error while reading links contents", e);
		}
	}
}