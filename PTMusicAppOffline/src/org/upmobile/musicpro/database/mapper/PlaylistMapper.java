package org.upmobile.musicpro.database.mapper;

import org.upmobile.musicpro.config.DatabaseConfig;
import org.upmobile.musicpro.database.CursorParseUtility;
import org.upmobile.musicpro.database.IRowMapper;
import org.upmobile.musicpro.object.Playlist;

import android.database.Cursor;

public class PlaylistMapper implements IRowMapper<Playlist> {
	@Override
	public Playlist mapRow(Cursor row, int rowNum) {
		Playlist song = new Playlist();
		song.setId(CursorParseUtility.getString(row, DatabaseConfig.KEY_ID));
		song.setName(CursorParseUtility.getString(row, DatabaseConfig.KEY_NAME));
		song.setListSongs(CursorParseUtility.getString(row, DatabaseConfig.KEY_LIST_SONG));
		return song;
	}
}