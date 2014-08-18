package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchTing;

public class TingSong extends RemoteSong {
	
	private int songId;
	
	public TingSong(Integer id, int songId) {
		super(id.hashCode());
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