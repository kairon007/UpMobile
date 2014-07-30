package org.kreed.vanilla.song;

import org.kreed.vanilla.engines.SearchZaycev;


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