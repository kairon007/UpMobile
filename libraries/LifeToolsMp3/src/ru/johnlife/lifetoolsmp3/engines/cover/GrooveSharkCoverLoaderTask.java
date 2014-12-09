package ru.johnlife.lifetoolsmp3.engines.cover;

import com.nostra13.universalimageloader.core.ImageLoader;


public class GrooveSharkCoverLoaderTask extends CoverLoaderTask {

	private String urlImage;
	
	public GrooveSharkCoverLoaderTask(String urlImage) {
		super();
		this.urlImage = urlImage;
	}
	
	@Override
	public void execute() {
		ImageLoader.getInstance().loadImage(urlImage, this);
	}
}