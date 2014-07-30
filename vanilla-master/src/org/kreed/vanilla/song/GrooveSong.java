package org.kreed.vanilla.song;

import org.kreed.vanilla.engines.SearchGrooveshark;

public class GrooveSong extends RemoteSong implements SongWithCover{

	private static final String coverUrl = "http://images.gs-cdn.net/static/albums/%d_%d.jpg";
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

	@Override
	public String getSmallCoverUrl() {
		return String.format(coverUrl, 40, albumId);
	}

	@Override
	public String getLargeCoverUrl() {
		return String.format(coverUrl, 500, albumId);
	}

	public Integer getSongId() {
		return songId;
	}

}