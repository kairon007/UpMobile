package org.upmobile.newmaterialmusicdownloader;

import java.io.File;

import android.os.Environment;


public interface Constants extends ru.johnlife.lifetoolsmp3.Constants {

	public static final String KEY_SELECTED_SONG = "key.selected.song";
	public static final String KEY_SELECTED_POSITION = "key.selected.position";
	
	public static final String PREF_DIRECTORY_PREFIX = "newmaterialmusicdownloader.pref.directory.prefix";
	public static final String PREF_DIRECTORY = "newmaterialmusicdownloader.pref.directory";
	
	public static final String DIRECTORY_PREFIX = File.separator + "NewMaterialMusicDownloader" + File.separator;
	public static final String DIRECTORY = Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;
	
}
