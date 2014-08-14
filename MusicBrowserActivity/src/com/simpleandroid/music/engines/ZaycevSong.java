package com.simpleandroid.music.engines;

public class ZaycevSong extends RemoteSong {
	
	private static final String pageUrl = "http://zaycev.net/pages/%s/%s.shtml";
	private String userId;
	private String songId;
	
	public ZaycevSong(String userId, String songId) {
		super((userId+songId).hashCode());
		this.userId = userId; 
		this.songId = songId; 
	}
	
	@Override
	public String getDownloadUrl() {
		if (downloadUrl == null) {
			downloadUrl = SearchZaycev.getDownloadUrl(String.format(pageUrl, userId, songId));
		}
		return downloadUrl;
	}
	
}
