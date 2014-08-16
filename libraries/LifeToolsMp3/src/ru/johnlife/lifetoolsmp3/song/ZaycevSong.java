package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchZaycev;


public class ZaycevSong extends RemoteSong {
	
//	private static final String pageUrl = "http://zaycev.net/pages/%s/%s.shtml";
	private int songId;
	
	public ZaycevSong(Integer songId) {
		super(songId.hashCode());
		this.songId = songId;
	}
	
	@Override
	public String getDownloadUrl() {
		if (downloadUrl == null) {
			downloadUrl = SearchZaycev.getDownloadUrl(songId);
		}
		return downloadUrl;
	}
}