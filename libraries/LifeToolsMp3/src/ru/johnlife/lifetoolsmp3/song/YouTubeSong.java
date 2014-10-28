package ru.johnlife.lifetoolsmp3.song;


public class YouTubeSong extends SongWithCover {

	private String largeCoverUrl;

	public YouTubeSong(String watchId, String largeCoverUrl) {
		super(watchId);
		this.largeCoverUrl = largeCoverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}
}