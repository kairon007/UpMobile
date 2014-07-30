package org.kreed.vanilla.song;


public class SoundCloudV2Song extends RemoteSong implements SongWithCover {

	private String largeCoverUrl;
	//private String smallCoverUrl;
	
	public SoundCloudV2Song(String downloadUrl, String largeCoverUrl) {
		super(downloadUrl);
		this.largeCoverUrl = largeCoverUrl;
		//smallCoverUrl = largeCoverUrl != null ? largeCoverUrl.replace("large", "small") : null;
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