package org.upmobile.clearmusicdownloader;

import java.io.File;

import android.os.Environment;

public interface Constants {
	
	public static final String KEY_SELECTED_SONG = "key.selected.song";
	
	public static final String PREF_DIRECTORY_PREFIX = "clearmusicdownloader.pref.directory.prefix";
	public static final String PREF_DIRECTORY = "clearmusicdownloader.pref.directory";
	
	public static final String DIRECTORY_PREFIX = File.separator + "ClearMusicDownloader" + File.separator;
	public static final String DIRECTORY = Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;

}
