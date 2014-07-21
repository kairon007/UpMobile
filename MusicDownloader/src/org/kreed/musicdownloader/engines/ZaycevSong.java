package org.kreed.musicdownloader.engines;

public class ZaycevSong extends RemoteSong {
	
	private String pageUrl;
	
	public ZaycevSong(String pageUrl) {
		super(pageUrl.hashCode());
		this.pageUrl = pageUrl; 
	}
	
	@Override
	public String getDownloadUrl() {
		if (downloadUrl == null) {
			downloadUrl = SearchZaycev.getDownloadUrl(pageUrl);
		}
		return downloadUrl;
	}
	
}
