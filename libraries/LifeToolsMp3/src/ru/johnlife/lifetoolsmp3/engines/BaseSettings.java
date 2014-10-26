package ru.johnlife.lifetoolsmp3.engines;

import java.util.ArrayList;
import android.content.Context;

public interface BaseSettings {
	public String[][] getSearchEnginesArray(Context context);
	public String[][] getSearchEnginesArray2(Context context);
	public String[][] getSearchEnginesArray3(Context context);
	public String[][] getSearchEnginesArray4(Context context);
	public String[][] getSearchEnginesArray5(Context context);
	public String[][] getSearchEnginesArray6(Context context);
	public String[][] getSearchEnginesArray7(Context context);
	public String[][] getSearchEnginesArray8(Context context);
	public boolean getIsCoversEnabled(Context context);
	public ArrayList<String> getEnginesArray (Context context);
}