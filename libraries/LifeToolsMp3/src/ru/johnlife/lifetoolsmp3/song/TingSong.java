package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchTing;

public class TingSong extends RemoteSong {
	
	private int songId;
	
	public TingSong(Integer id, int songId) {
		super(id.hashCode());
		this.songId = songId;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		super.getDownloadUrl(listener);
		if (downloadUrl == null) {
			downloadUrl = SearchTing.getDownloadUrl(songId);
			return true;
		} else return false;
	}
	
}