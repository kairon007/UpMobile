package ru.johnlife.lifetoolsmp3.song;

import ru.johnlife.lifetoolsmp3.engines.SearchTing;
import android.content.Context;

public class TingSong extends RemoteSong {
	
	private int songId;
	
	public TingSong(Integer id, int songId) {
		super(id.hashCode());
		this.songId = songId;
	}
	
	@Override
	public boolean getDownloadUrl(Context context) {
		super.getDownloadUrl(context);
		if (downloadUrl == null) {
			downloadUrl = SearchTing.getDownloadUrl(songId);
			return true;
		} else return false;
	}
	
}