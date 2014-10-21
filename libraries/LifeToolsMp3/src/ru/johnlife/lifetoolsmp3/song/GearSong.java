package ru.johnlife.lifetoolsmp3.song;

public class GearSong extends SongWithCover {

	private String coverUrl;

	public GearSong(String downloadUrl, String largeCoverUrl) {
		super(downloadUrl);
		this.coverUrl = largeCoverUrl.replace(" ", "").replace("https", "http");
	}

	@Override
	public String getLargeCoverUrl() {
		return coverUrl;
	}

}
