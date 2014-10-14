package ru.johnlife.lifetoolsmp3;

import android.os.Environment;

public interface BaseConstants {
	public static final String DEFAULT_PREFERENCES = "default_preferences";
	public static final String EDIT_ARTIST_NAME = "edit_artist_name";
	public static final String EDIT_ALBUM_TITLE = "edit_album_title";
	public static final String EDIT_SONG_TITLE = "edit_song_title";
	public static final String USE_ALBUM_COVER = "use_album_cover";
	public static final String DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
	public static final String DIRECTORY_PREFIX = "/MusicDownloader/";
}
