package ru.johnlife.lifetoolsmp3.engines.cover;

import com.nostra13.universalimageloader.core.ImageLoader;


public class SoundCloudCoverLoaderTask extends CoverLoaderTask {

	private String urlImage;

	public SoundCloudCoverLoaderTask(String urlImage) {
		super();
		this.urlImage = urlImage;
	}

	@Override
	public void execute() {
		if (null == urlImage) {
			for (OnBitmapReadyListener listener : listeners) {
				if (null != listener) {
					listener.onBitmapReady(null);
				}
			}
		} else {
			ImageLoader.getInstance().loadImage(urlImage, this);
		}
	}
}