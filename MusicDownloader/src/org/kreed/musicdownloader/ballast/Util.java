package org.kreed.musicdownloader.ballast;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Util {
	
	public static String getThemeName(Context context)
	{
	    PackageInfo packageInfo;
	    try
	    {
	        packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
	        int themeResId = packageInfo.applicationInfo.theme;
	        return context.getResources().getResourceEntryName(themeResId);
	    }
	    catch (NameNotFoundException e)
	    {
	        return null;
	    }
	}
}

