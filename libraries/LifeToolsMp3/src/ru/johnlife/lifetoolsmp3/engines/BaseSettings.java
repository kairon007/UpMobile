package ru.johnlife.lifetoolsmp3.engines;

import android.content.Context;



public interface BaseSettings {
	String[][] getSearchEngines(Context context);
	boolean getIsAlbumCoversEnabled(Context context);
}
