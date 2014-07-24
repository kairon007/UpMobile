package org.kreed.vanilla;

import android.app.Activity;

import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.raivz.hbkds194978.AdListener;
import com.raivz.hbkds194978.AdListener.MraidAdListener;
import com.raivz.hbkds194978.AdView;
import com.raivz.hbkds194978.MA;
import com.raivz.hbkds194978.AdListener.AdType;
import com.startapp.android.publish.StartAppAd;

public class Advertisement {

	private static StartAppAd startAppAd;
	private static MoPubView moPubView;
	private static MA ma;

	public static void startAppInit(Activity activity) {
		if (startAppAd == null) {
			startAppAd = new StartAppAd(activity);
			StartAppAd.init(activity, Settings.STARTAPP_DEV_ID, Settings.STARTAPP_APP_ID);
		}
	}

	public static void startAppOnResume(Activity activity) {
		if (startAppAd != null)
			startAppAd.onPause();
	}

	public static void startAppOnPause(Activity activity) {
		if (startAppAd != null)
			startAppAd.onPause();
	}

	public static void startAppOnBackPressed(Activity activity) {
		if (startAppAd != null)
			startAppAd.onBackPressed();
	}

	public static void mobileCoreInit(Activity activity) {
		MobileCore.init(activity, Settings.MOBILECORE_ID,
				LOG_TYPE.DEBUG, AD_UNITS.ALL_UNITS);
		MobileCore.showOfferWall(activity, null);
	}

	public static void moPubInit(Activity activity) {
		if (moPubView == null) {
			moPubView = (MoPubView) activity.findViewById(R.id.banner_view);
			moPubView.setAdUnitId(Settings.MOPUB_ID); // Enter your Ad Unit ID from
												// www.mopub.com
			moPubView.loadAd();
		}
	}

	public static void moPubOnBackPressed(Activity activity) {
		if (moPubView != null) {
			MoPubInterstitial mInterstitial = new MoPubInterstitial(activity,
					Settings.MOPUB_ID);
			mInterstitial.load();
		} else {
			moPubInit(activity);
		}
	}

	public static void moPubOnDestroy(Activity activity) {
		if (moPubView != null) {
			moPubView.destroy();
		} else {
			moPubInit(activity);
		}
	}

	public static void airPushShow(Activity activity) {
		if (ma == null) {			
			ma = new MA(activity, null, false);
		}
//		ma.callSmartWallAd();
//		ma.callAppWall(); // this will start the AppWall ad but it will not show you AppWall instantly.
//		ma.callLandingPageAd();
		ma.callOverlayAd();
//		ma.callVideoAd();
//		ma.displayRichMediaInterstitialAd();
	}
	
	public static void airPushOnBackPressed(Activity activity) {
		try{
			ma.showCachedAd(activity, AdType.overlay); 
	    }catch (Exception e) {}
	}	
}
