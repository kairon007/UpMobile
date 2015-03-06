package org.upmobile.materialmusicdownloader;

import java.io.File;

import android.os.Environment;

public interface Constants extends com.csform.android.uiapptemplate.Constants{
	
	public static final String KEY_SELECTED_SONG = "key.selected.song";
	public static final String KEY_SELECTED_POSITION = "key.selected.position";
	
	public static final String PREF_DIRECTORY_PREFIX = "materialmusicdownloader.pref.directory.prefix";
	public static final String PREF_DIRECTORY = "materialmusicdownloader.pref.directory";
	
	public static final String DIRECTORY_PREFIX = File.separator + "MaterialMusicDownloader" + File.separator;
	public static final String DIRECTORY = Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;

}
