package org.kreed.musicdownloader;

import android.graphics.Bitmap;

public interface LoadPercentageInterface {
	
	public void insertProgress (String progress);
	
	public void insertCover (Bitmap cover);

}
