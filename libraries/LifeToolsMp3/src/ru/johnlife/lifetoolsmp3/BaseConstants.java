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
	public static final String INTENT_ACTION_LOAD_URL = "intent_action_load_url";

	public static final String EQUALIZER_SAVE = "library.equalizer.save";
	public static final String EQUALIZER_PRESET = "library.equalizer.preset";
	public static final String EQUALIZER_VERTICAL_SEEKBAR_ONE = "library.equalizer.vertical.seekbar.one";
	public static final String EQUALIZER_VERTICAL_SEEKBAR_TWO = "library.equalizer.vertical.seekbar.two";
	public static final String EQUALIZER_VERTICAL_SEEKBAR_THREE = "library.equalizer.vertical.seekbar.three";
	public static final String EQUALIZER_VERTICAL_SEEKBAR_FOUR = "library.equalizer.vertical.seekbar.four";
	public static final String EQUALIZER_VERTICAL_SEEKBAR_FIVE = "library.equalizer.vertical.seekbar.five";
	public static final String EQUALIZER_SEEKBAR_ONE = "library.equalizer.seekbar.one";
	public static final String EQUALIZER_SEEKBAR_TWO = "library.equalizer.vseekbar.two";
}