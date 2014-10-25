package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchGrooveshark;


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
	public void getDownloadUrl(DownloadUrlListener listener) {
		super.getDownloadUrl(listener);
		if (downloadUrl == null) {
			downloadUrl = SearchGrooveshark.getDownloadUrl(songId);
		}
	}

	@Override
	public String getLargeCoverUrl() {
		return String.format(coverUrl, 500, albumId);
	}

	public Integer getSongId() {
		return songId;
	}

}