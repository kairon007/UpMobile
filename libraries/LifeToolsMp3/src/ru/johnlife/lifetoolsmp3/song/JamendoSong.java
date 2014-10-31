package ru.johnlife.lifetoolsmp3.song;

public class JamendoSong extends SongWithCover {
	
	private String coverUrl;

	
	public JamendoSong(String downloadUrl, String coverUrl) {
		super(downloadUrl);
		this.coverUrl = coverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return coverUrl;
	}

}
