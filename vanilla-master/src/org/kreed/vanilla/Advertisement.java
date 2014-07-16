package org.kreed.vanilla;

import org.kreed.vanilla.R.string;

import android.app.Activity;

import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.startapp.android.publish.StartAppAd;

public class Advertisement {
	
	private static StartAppAd startAppAd;
	private static MoPubView moPubView;
	private static final String adUnitId = "3a0d11f3de694617aa81f8e3901eb33c";
	
	public static void startAppInit(Activity activity){
		if (startAppAd == null) {
			startAppAd = new StartAppAd(activity);
			StartAppAd.init(activity, "105125878", "207452668");
		}
	}
	
	public static void startAppOnResume(Activity activity){
		if (startAppAd != null)
			startAppAd.onPause();
	}
	
	public static void startAppOnPause(Activity activity){
		if (startAppAd != null)
			startAppAd.onPause();
	}
	
	public static void startAppOnBackPressed(Activity activity){
		if (startAppAd != null)
			startAppAd.onBackPressed();
	}
	
	public static void mobileCoreInit(Activity activity) {
		MobileCore.init(activity,"6AFPIUJW9K2IAEMIJ41605AI1UJUY", LOG_TYPE.DEBUG, AD_UNITS.ALL_UNITS);
		MobileCore.showOfferWall(activity, null);
	}
	
	public static void moPubInit(Activity activity) {
		if (moPubView == null) {
			moPubView = (MoPubView) activity.findViewById(R.id.banner_view);
			moPubView.setAdUnitId(adUnitId); // Enter your Ad Unit ID from www.mopub.com
			moPubView.loadAd();
		}
	}
	
	public static void moPubOnBackPressed(Activity activity) {
		if (moPubView != null) {
			MoPubInterstitial mInterstitial = 
					new MoPubInterstitial(activity, adUnitId);
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
}
