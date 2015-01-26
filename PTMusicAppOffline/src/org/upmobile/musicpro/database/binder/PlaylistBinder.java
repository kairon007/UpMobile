package org.upmobile.musicpro.database.binder;

import org.upmobile.musicpro.database.ParameterBinder;
import org.upmobile.musicpro.object.Playlist;

import android.database.sqlite.SQLiteStatement;

public class PlaylistBinder implements ParameterBinder {
	public void bind(SQLiteStatement statement, Object object) {
		Playlist playlist = (Playlist) object;
		statement.bindString(1, playlist.getId());
		statement.bindString(2, playlist.getName());
		statement.bindString(3, playlist.getJsonArraySong());
	}
}
