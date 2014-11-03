package org.kreed.musicdownloader.interfaces;


public interface LoadPercentageInterface {
	
	public void insertProgress (long progress, long downloadId );
		
	public void setFileUri(String uri, long downloadId);
	
	public void currentDownloadingSongTitle (String currentDownloadingSongTitle);
}
