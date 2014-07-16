package org.kreed.vanilla;

import android.app.Activity;

import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.startapp.android.publish.StartAppAd;

public class Advertisement {
	
	public static void startAppInit(Activity activity){
		new StartAppAd(activity).onResume();
		StartAppAd.init(activity, "105125878", "207452668");
	}
	
	public static void mobileCoreInit(Activity activity) {
		MobileCore.init(activity,"6AFPIUJW9K2IAEMIJ41605AI1UJUY", LOG_TYPE.DEBUG, AD_UNITS.ALL_UNITS);
		MobileCore.showOfferWall(activity, null);
	}
}
