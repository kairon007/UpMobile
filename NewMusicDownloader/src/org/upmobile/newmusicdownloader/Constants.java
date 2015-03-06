package org.upmobile.newmusicdownloader;

import java.io.File;

import android.os.Environment;

public interface Constants {
	
	public static final int SEARCH_FRAGMENT = 0;
	public static final int DOWNLOADS_FRAGMENT = 1;
	public static final int PLAYLIST_FRAGMENT = 2;
	public static final int LIBRARY_FRAGMENT = 3;
	public static final int PLAYER_FRAGMENT = 4;
	public static final int SETTINGS_FRAGMENT = 5;
	
	public static final String KEY_SELECTED_SONG = "key.selected.song";
	public static final String KEY_SELECTED_POSITION = "key.selected.position";
	
	public static final String PREF_DIRECTORY_PREFIX = "newmusicdownloader.pref.directory.prefix";
	public static final String PREF_DIRECTORY = "newmusicdownloader.pref.directory";
	
	public static final String DIRECTORY_PREFIX = File.separator + "NewMusicDownloader" + File.separator;
	public static final String DIRECTORY = Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;
	
}
