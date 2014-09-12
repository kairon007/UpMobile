package ru.johnlife.lifetoolsmp3.engines;

import android.content.Context;



public interface BaseSettings {
	String[][] getSearchEnginesArray(Context context);
	boolean getIsCoversEnabled(Context context);
}
