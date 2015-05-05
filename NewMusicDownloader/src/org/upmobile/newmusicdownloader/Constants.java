package org.upmobile.newmusicdownloader;

import java.io.File;

import android.os.Environment;

public interface Constants extends ru.johnlife.lifetoolsmp3.Constants {
	
	public static final String PREF_DIRECTORY_PREFIX = "newmusicdownloader.pref.directory.prefix";
	public static final String PREF_DIRECTORY = "newmusicdownloader.pref.directory";
	
	public static final String DIRECTORY_PREFIX = File.separator + "NewMusicDownloader" + File.separator;
	public static final String DIRECTORY = Environment.getExternalStorageDirectory() + DIRECTORY_PREFIX;
	
}
