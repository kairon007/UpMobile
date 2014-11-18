package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchGrooveshark;
import android.content.Context;


public class GrooveSong extends SongWithCover {

	private static final String coverUrl = "http://images.gs-cdn.net/static/albums/%d_%d.jpg";
	private int songId;
	private int albumId;

	public GrooveSong(long id, int songId, int albumId) {
		super(id);
		this.songId = songId;
		this.albumId = albumId;
	}

	@Override
	public boolean getDownloadUrl(Context context) {
		super.getDownloadUrl(context);
		if (downloadUrl == null) {
			downloadUrl = SearchGrooveshark.getDownloadUrl(songId);
			return true;
		} else return false;
	}

	@Override
	public String getLargeCoverUrl() {
		return String.format(coverUrl, 500, albumId);
	}

	public Integer getSongId() {
		return songId;
	}

}