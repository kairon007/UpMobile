package org.kreed.musicdownloader;

import android.graphics.Bitmap;

public interface LoadPercentageInterface {
	
	public void insertProgress (String progress);
	
	public void insertCover (Bitmap cover);
	
	public void setFileUri(String uri, long downloadId);
	
	public void currentDownloadingSongTitle (String currentDownloadingSongTitle);
	
	public void currentDownloadingID (Long currentDownloadingID);
}
