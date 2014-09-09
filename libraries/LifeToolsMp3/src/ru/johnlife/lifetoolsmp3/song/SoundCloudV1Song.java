package ru.johnlife.lifetoolsmp3.song;


public class SoundCloudV1Song extends RemoteSong implements SongWithCover {

	private String largeCoverUrl;

	public SoundCloudV1Song(String downloadUrl, String largeCoverUrl) {
		super(downloadUrl);
		this.largeCoverUrl = largeCoverUrl.replace(" ", "").replace("https", "http");
	}

	@Override
	public String getSmallCoverUrl() {
		return largeCoverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}
}
