package org.kreed.musicdownloader.engines;


public class GrooveSong extends RemoteSong {
	
	private static final String urlImage = "http://images.gs-cdn.net/static/albums/%d_%d.jpg";
	private int songId;
	private int albumId;
	
	public GrooveSong(long id, int songId, int albumId) {
		super(id);
		this.songId = songId;
		this.albumId = albumId;
	}
	
	@Override
	public String getDownloadUrl() {
		if (downloadUrl == null) {
			downloadUrl = SearchGrooveshark.getDownloadUrl(songId);
		}
		return downloadUrl;
	}
	
	public String getUrlSmallImage() {
		return String.format(urlImage, 40, albumId);
	}
	
	public String getUrlLargeImage() {
		return String.format(urlImage, 500, albumId);
	}

}