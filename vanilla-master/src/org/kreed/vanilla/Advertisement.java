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
	private static final String adUnitId = "3a0d11f3de694617aa81f8e3901eb33c";
	private static MA ma;

	public static void startAppInit(Activity activity) {
		if (startAppAd == null) {
			startAppAd = new StartAppAd(activity);
			StartAppAd.init(activity, "105125878", "207452668");
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
		MobileCore.init(activity, "6AFPIUJW9K2IAEMIJ41605AI1UJUY",
				LOG_TYPE.DEBUG, AD_UNITS.ALL_UNITS);
		MobileCore.showOfferWall(activity, null);
	}

	public static void moPubInit(Activity activity) {
		if (moPubView == null) {
			moPubView = (MoPubView) activity.findViewById(R.id.banner_view);
			moPubView.setAdUnitId(adUnitId); // Enter your Ad Unit ID from
												// www.mopub.com
			moPubView.loadAd();
		}
	}

	public static void moPubOnBackPressed(Activity activity) {
		if (moPubView != null) {
			MoPubInterstitial mInterstitial = new MoPubInterstitial(activity,
					adUnitId);
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
//		ma.callLandingPageAd();	// work
		ma.callOverlayAd();		// work
//		ma.callVideoAd();		// work
//		ma.displayRichMediaInterstitialAd();
		
		/*
		 * AdView Class is same as View Class. You can use the Adview object
		 * as View object.
		 */
//		AdView adView = new AdView(activity, AdView.BANNER_TYPE_IN_APP_AD,
//				AdView.PLACEMENT_TYPE_INTERSTITIAL, false, false,
//				AdView.ANIMATION_TYPE_LEFT_TO_RIGHT);
//			adView.setAdListener(new MraidAdListener() {
	}
	
	public static void airPushOnBackPressed(Activity activity) {
		try{
			ma.showCachedAd(activity, AdType.overlay); 
	    }catch (Exception e) {
//		    	activity.finish();
	    }
	}	
}
