package ru.johnlife.lifetoolsmp3;

import android.os.Environment;

public interface BaseConstants {
//	String DEFAULT_PREFERENCES = "default_preferences";
//	String EDIT_ARTIST_NAME = "edit_artist_name";
//	String EDIT_ALBUM_TITLE = "edit_album_title";
//	String EDIT_SONG_TITLE = "edit_song_title";
//	String USE_ALBUM_COVER = "use_album_cover";
	String DOWNLOAD_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
	String DIRECTORY_PREFIX = "/MusicDownloader/";
//	String INTENT_ACTION_LOAD_URL = "intent_action_load_url";

	String EQUALIZER_SAVE = "library.equalizer.save";
	String EQUALIZER_PRESET = "library.equalizer.preset";
	String EQUALIZER_VERTICAL_SEEKBAR_ONE = "library.equalizer.vertical.seekbar.one";
	String EQUALIZER_VERTICAL_SEEKBAR_TWO = "library.equalizer.vertical.seekbar.two";
	String EQUALIZER_VERTICAL_SEEKBAR_THREE = "library.equalizer.vertical.seekbar.three";
	String EQUALIZER_VERTICAL_SEEKBAR_FOUR = "library.equalizer.vertical.seekbar.four";
	String EQUALIZER_VERTICAL_SEEKBAR_FIVE = "library.equalizer.vertical.seekbar.five";
	String EQUALIZER_SEEKBAR_ONE = "library.equalizer.seekbar.one";
	String EQUALIZER_SEEKBAR_TWO = "library.equalizer.vseekbar.two";
	String EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_ONE = "library.equalizer.vertical.seekbar.custom.one";
	String EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_TWO = "library.equalizer.vertical.seekbar.custom.two";
	String EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_THREE = "library.equalizer.vertical.seekbar.custom.three";
	String EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_FOUR = "library.equalizer.vertical.seekbar.custom.four";
	String EQUALIZER_VERTICAL_SEEKBAR_CUSTOM_FIVE = "library.equalizer.vertical.seekbar.custom.five";
	String EQUALIZER_SEEKBAR_CUSTOM_ONE = "library.equalizer.seekbar.custom.one";
	String EQUALIZER_SEEKBAR_CUSTOM_TWO = "library.equalizer.vseekbar.custom.two";
}