package ru.johnlife.lifetoolsmp3.song;

public class SoundCloudV2Song extends SongWithCover {

	private String largeCoverUrl;
	//private String smallCoverUrl;
	
	public SoundCloudV2Song(String downloadUrl, String largeCoverUrl) {
		super(downloadUrl);
		this.largeCoverUrl = largeCoverUrl;
		//smallCoverUrl = largeCoverUrl != null ? largeCoverUrl.replace("large", "small") : null;
	}
	
	@Override
	public String getLargeCoverUrl() {
		return largeCoverUrl;
	}
	
}