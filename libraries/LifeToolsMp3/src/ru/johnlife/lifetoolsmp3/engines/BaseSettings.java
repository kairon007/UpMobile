package ru.johnlife.lifetoolsmp3.engines;

import java.util.ArrayList;
import android.content.Context;

public interface BaseSettings {
	public String[][] getSearchEnginesArray(Context context);
	public String[][] getSearchEnginesSC(Context context);
	public String[][] getSearchEnginesYT(Context context);
	public boolean getIsCoversEnabled(Context context);
	public ArrayList<String> getEnginesArray ();
}