package org.kreed.vanilla.engines;

import com.scilor.grooveshark.API.Functions.SearchArtist.SearchArtistResult;

public class GrooveSong extends RemoteSong {

	private static final String GROOVE_SCHEME = "groove://";

	public GrooveSong(SearchArtistResult result) {
		super(GROOVE_SCHEME+result.SongID);
		this.artist = result.ArtistName;
		this.title = result.Name;
	}

	@Override
	public String getDownloadUrl() {
		return super.getDownloadUrl();
	}

}
