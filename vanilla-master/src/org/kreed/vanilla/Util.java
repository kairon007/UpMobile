package org.kreed.vanilla;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Util {
	public static String getThemeName(Activity activity)
	{
	    PackageInfo packageInfo;
	    try
	    {
	        packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
	        int themeResId = packageInfo.applicationInfo.theme;
	        return activity.getResources().getResourceEntryName(themeResId);
	    }
	    catch (NameNotFoundException e)
	    {
	        return null;
	    }
	}
}
