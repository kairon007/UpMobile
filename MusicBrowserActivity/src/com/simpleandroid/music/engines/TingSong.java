package com.simpleandroid.music.engines;

public class TingSong extends RemoteSong {
	
	private int songId;
	
	public TingSong(long id, int songId) {
		super(id);
		this.songId = songId;
	}
	
	@Override
	public String getDownloadUrl() {
		if (downloadUrl == null) {
			downloadUrl = SearchTing.getDownloadUrl(songId);
		}
		return downloadUrl;
	}
	
}