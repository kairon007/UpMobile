package org.kreed.vanilla.engines;


public class GrooveSong extends RemoteSong {
	
	private int songId;
	
	public GrooveSong(long id, int songId) {
		super(id);
		this.songId = songId;
	}
	
	@Override
	public String getDownloadUrl() {
		if (downloadUrl == null) {
			downloadUrl = SearchGrooveshark.getDownloadUrl(songId);
		}
		return downloadUrl;
	}

}