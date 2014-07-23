package mp3.music.player.us.engines;

public class RemoteSong extends OnlineSong {

	protected String downloadUrl;

	public RemoteSong(String downloadUrl) {
		super(downloadUrl.hashCode());
		this.downloadUrl = downloadUrl;
	}
	
	protected RemoteSong(long id) {
		super(id);
		downloadUrl = null;
	}

	public RemoteSong setTitle(String songTitle) {
		title = songTitle;
		return this;
	}

	public RemoteSong setArtistName(String songArtist) {
		artist = songArtist;
		return this;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

}
