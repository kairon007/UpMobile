package org.upmobile.musicpro.database.mapper;

import org.upmobile.musicpro.config.DatabaseConfig;
import org.upmobile.musicpro.database.CursorParseUtility;
import org.upmobile.musicpro.database.IRowMapper;
import org.upmobile.musicpro.object.Song;

import android.database.Cursor;

public class SongMapper implements IRowMapper<Song> {
	@Override
	public Song mapRow(Cursor row, int rowNum) {
		Song song = new Song();
		song.setId(CursorParseUtility.getString(row, DatabaseConfig.KEY_ID));
		song.setName(CursorParseUtility.getString(row, DatabaseConfig.KEY_NAME));
		song.setUrl(CursorParseUtility.getString(row, DatabaseConfig.KEY_URL));
		song.setImage(CursorParseUtility.getString(row, DatabaseConfig.KEY_IMAGE));
		song.setArtist(CursorParseUtility.getString(row, DatabaseConfig.KEY_ARTIST));
		song.setPosition(CursorParseUtility.getInt(row, DatabaseConfig.KEY_POSITION));
		return song;
	}
}