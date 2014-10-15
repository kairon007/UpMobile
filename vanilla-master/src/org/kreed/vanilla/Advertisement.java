package org.kreed.vanilla;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
 


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;


import com.appnext.appnextsdk.Appnext;
import com.appnext.appnextsdk.NoAdsInterface;
import com.appnext.appnextsdk.PopupClosedInterface; 
import com.ibrif.higaz187743.AdListener;
import com.ibrif.higaz187743.AdView;
import com.ibrif.higaz187743.MA;


import com.ironsource.mobilcore.CallbackResponse;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.ironsource.mobilcore.OnReadyListener;
import com.mm1373232377.android.CheckAdStatusListener;
import com.mm1373232377.android.MiniMob1373232377; 
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;

import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import com.mopub.mobileads.MoPubView.BannerAdListener;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeResponse;
import com.mopub.nativeads.RequestParameters;
import com.revmob.RevMob;
import com.revmob.RevMobAdsListener;
import com.revmob.RevMobTestingMode;
import com.revmob.ads.link.RevMobLink;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.banner.Banner;
import com.startapp.android.publish.nativead.NativeAdDetails;
import com.startapp.android.publish.nativead.NativeAdPreferences;
import com.startapp.android.publish.nativead.NativeAdPreferences.NativeAdBitmapSize;
import com.startapp.android.publish.nativead.StartAppNativeAd;



import ru.johnlife.lifetoolsmp3.Advertisment;
import android.annotation.SuppressLint;

//import com.vungle.sdk.VunglePub;




public class Advertisement implements Advertisment {

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// special mopub objects for exit interstitials. only needed for exits
	public static MoPubInterstitial mopubExitInterstitial;   
	public static MoPubInterstitial mopubSearchExitInterstitial; 
	public static MoPubInterstitial mopubDownloadsExitInterstitial;
		

	
	// local settings
	public static String KEY_INSTALL_TIME = "install_time";

	
	
	// ad settings
	public static boolean isMobileCoreInitialized = false;
	public static boolean isApplovinInitialized = false;

		

	/*
	public static void initializeAppbrain(Activity activity) {
		AppBrain.init(activity);
	} 
	*/
	
	
	

	
	
/*
	public static void chartboostShowInterstitial(Activity activity, String adPosition) { 
		try {
			// Configure Chartboost
			Chartboost cb = Chartboost.sharedChartboost();
			String appId = Settings.CHARTBOOST_APPID;
			String appSignature = Settings.CHARTBOOST_APPSIGNATURE;
			cb.onCreate(activity, appId, appSignature, null);
			cb.onStart(activity);
			cb.startSession(); 
			 
			
			
			cb.showInterstitial();
		} catch(Exception e) {
			
		}
	}
	
	public static void chartboostShowMoreApps(Activity activity, String adPosition) {
		
		try {
			// Configure Chartboost
			Chartboost cb = Chartboost.sharedChartboost(); 
			String appId = Settings.CHARTBOOST_APPID;
			String appSignature = Settings.CHARTBOOST_APPSIGNATURE;
			cb.onCreate(activity, appId, appSignature, null); 
			cb.onStart(activity);
			cb.startSession(); 
			 
			cb.showMoreApps();
		} catch(Exception e) {
			
			
		} 
	}
	*/
	
	
	public static void start(Activity activity, boolean switchShowDialog) {
		
		if (Settings.ENABLE_ADS) {  
		
			
			if (activity != null) {	
	
				
				//initializeAppbrain(activity);
				initializeMobileCore(activity, AD_UNITS.ALL_UNITS);
				//initializeVungle(activity);
				initializeAirpush(activity);
				initializeStartapp(activity);
				//initializeApplovin(activity);
				initializeMinimob(activity); 
				 
	
				// if first time running
				long installTime = getSharedPrefLong(activity, KEY_INSTALL_TIME, -999);
				if (installTime < 0) {
					// first time run
					putSharedPrefLong(activity, KEY_INSTALL_TIME, System.currentTimeMillis()); // save install time, which is needed
					try {
						mobilecoreStickeezShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START);
					} catch(Exception e) {
						
					}
				} else {
					
					
					if (isOnline(activity)) {
						//if (!hasRated(activity) && shouldShowRatePopup(activity) && !Settings.getIsBlacklisted(activity)) {
						if (!hasRated(activity) && !Settings.getIsBlacklisted(activity)) {
							boolean didShowPopup = showRatePopupWithInitialDelay(activity, Settings.RATE_ME_POPUP_DELAY_MILLIS, switchShowDialog);
							if (!didShowPopup) showStartInterstitial(activity);
							
						} else {
							
							// only run showStartINterstitial on subsequent runs    
							showStartInterstitial(activity); 
						}
					}
				} 
				
				
				// preload mopub interstitial
				// if we should show  exit ad for this activity, then check if the next ad is a mopub one. if it's mopub, we will preload
				preloadExitAd(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT);
			}
		}
	}
	
	

	public static boolean shouldShowRatePopup(Activity activity) {
		
		try {
			// if you have downloaded the minimum number of songs 
			String minNumCompletedDownloads_String = Settings.getRemoteSetting(activity, Settings.KEY_REMOTE_SETTING_RATEME_MIN_NUMBER_COMPLETED_DOWNLOADS, null);
			String minMinutesBetweenPopups_String = Settings.getRemoteSetting(activity, Settings.KEY_REMOTE_SETTING_RATEME_MIN_MINUTES_BETWEEN_POPUPS, null);
			if (minNumCompletedDownloads_String == null || minMinutesBetweenPopups_String == null) {
				return false;
			} else { 
				long minNumCompletedDownloads = new Long(minNumCompletedDownloads_String);
				long numberOfCompletedDownloads = getNumberOfCompletedDownloads(activity);
				
				
				long minMinutesBetweenPopups = new Long(minMinutesBetweenPopups_String);
				long lastTimeAskedForRate = getLastTimeAskedForRate(activity);
				long minutesSinceLastPopup = (System.currentTimeMillis() - lastTimeAskedForRate) / 60000; 
				
				return (numberOfCompletedDownloads >= minNumCompletedDownloads) && (minutesSinceLastPopup >= minMinutesBetweenPopups);
				
			}
		} catch(Exception e) {
			 
		}
			
		
		return false;
		
	}

	public static long getLastTimeAskedForRate(Context context) {
		return getSharedPrefLong(context, "last_time_asked_for_rate", 0);
	}
	
	public static void setLastTimeAskedForRate(Context context, long time) {
		putSharedPrefLong(context, "last_time_asked_for_rate", time);
	}
	

	public static boolean hasRated(Activity activity) {
		SharedPreferences prefs = activity.getSharedPreferences("has_rated_app", 0);
		boolean has_rated = prefs.getBoolean("rated", false);
		return has_rated;
	}
	
	
 
	public static void showRateMePopup(final Activity activity, boolean switchShowDialog) { 
		
		setLastTimeAskedForRate(activity, System.currentTimeMillis()); 
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse("market://details?id=" + activity.getPackageName()));
					try {
						activity.startActivity(browserIntent);
					} catch (ActivityNotFoundException e) {

					}

					SharedPreferences prefs = activity.getSharedPreferences("has_rated_app", 0);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("rated", true).commit();  
					
					Toast.makeText(activity, activity.getString(R.string.rate_thanks), Toast.LENGTH_LONG).show(); 

					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};
		if (switchShowDialog) {
			//TODO
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(activity.getString(R.string.rate_title));
			builder.setMessage(activity.getString(R.string.new_rate_description))
					.setPositiveButton(
							activity.getString(R.string.new_rate_yes),
							dialogClickListener)
					.setNegativeButton(
							activity.getString(R.string.new_rate_no),
							dialogClickListener).setCancelable(false).show();
		}
	
	}
	

	public static boolean showRatePopupWithInitialDelay(final Activity activity, long initialDelayMillis,boolean switchShowDialog) {
		if (activity != null && System.currentTimeMillis() - getInstallTime(activity) > initialDelayMillis) { // if
																						// x
																						// seconds
																						// have
																						// passed
			showRateMePopup(activity, switchShowDialog);
			return true;
		} else {
			return false;
		}
	}
	

	
	

	
	

	
	public static void showDisclaimer(Activity activity) {
		
		
		if (activity != null) {
			
			
			SharedPreferences prefs = activity.getApplicationContext()
					.getSharedPreferences("HasShownDisclaimer", 0); 
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("HasShownDisclaimer", true).commit();
	
			
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	
				public void onClick(DialogInterface dialog, int which) {
					
				}
			};
		
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle("PLEASE READ");
			builder.setMessage(activity.getString(R.string.disclaimer))   
					.setPositiveButton("Ok", dialogClickListener)
					.setCancelable(false).show();
		}
	}

	


	public static  void showCrossPromoBox(final Activity activity, View view) {
		try {
			
			
			
			
			
			if (view != null && activity != null) {
				LinearLayout crossPromoBox = (LinearLayout)view.findViewById(R.id.cross_promo_box);
				
			
				
				
				
				if (crossPromoBox != null) { 
					
					
					
					try {
						String crossPromoBoxSettings = Settings.getSharedPrefString(activity, "cross_promo_box", null);
						JSONObject jsonObj = new JSONObject(crossPromoBoxSettings);
						String title = jsonObj.getString("title"); 
						String description = jsonObj.getString("description");
						final String pkg = jsonObj.getString("package"); 
						String buttonText = jsonObj.getString("button_message"); 
						
						
						if (title != null && description != null && buttonText != null && pkg != null && !pkg.equals("") && !buttonText.equals("") && !Advertisement.isPackageInstalled(activity, pkg)) {

							
							TextView crossPromoBoxTitle = (TextView)view.findViewById(R.id.cross_promo_box_title); 
							TextView crossPromoBoxDescription = (TextView)view.findViewById(R.id.cross_promo_box_description);
							Button crossPromoBoxButton = (Button)view.findViewById(R.id.cross_promo_box_button);
							
							if (crossPromoBoxTitle != null && crossPromoBoxDescription != null && buttonText != null) {
								
								
								crossPromoBoxTitle.setText(title);
								crossPromoBoxDescription.setText(description); 
								crossPromoBoxButton.setText(buttonText);
								crossPromoBoxButton.setOnClickListener(
										new OnClickListener() {
											@Override
											public void onClick(View v) {
												// TODO Auto-generated method stub
									          	String pkgUrl;
									          	if (pkg != null && pkg.equals("{mobilecore_direct}")) {
									          		try {
									          			Advertisement.mobilecoreDirectShowInterstitial(activity, "yea");
									          		} catch(Exception e) {
									          			
									          		}
									          		
									          	} else if  (pkg != null && pkg.equals("{startapp_direct}")) {
									          		
									          		try {
									          			Advertisement.startappDirectShowInterstitial(activity, "yea");
									          		} catch(Exception e) {
									          			
									          		}
									          		
									         		
									          	} else if  (pkg != null && pkg.equals("{revmob_direct}")) {
									          		
									          		try {
									          			Advertisement.revmobDirectShowInterstitial(activity, "yea");
									          		} catch(Exception e) {
									          			
									          		}
									          		
									          		
									          	} else {
										          	
													if (pkg.startsWith("http:") || pkg.startsWith("https:")) {
														pkgUrl = pkg; 
													} else {
														pkgUrl = "market://details?id=" + pkg;
													}
													Intent localIntent = new Intent("android.intent.action.VIEW"); 
													localIntent.setData(Uri.parse(pkgUrl));
													try {
														activity.startActivity(localIntent);
													} catch(Exception e) {
														
													}
									          	}
												
											}
										});
								crossPromoBox.setVisibility(View.VISIBLE);  
								return; // have to return out or the promo box wil be hidden 
							}
							
						}
					
				} catch(Exception e) {
					
				}
				}
			}
			
			
		} catch(Exception e) {
			
		}
		hideCrossPromoBox(activity, view);
		
	}
	
    

	public static void hideCrossPromoBox(Activity activity, View view) {
		try {
			
			if (view != null) {
				LinearLayout crossPromoBox = (LinearLayout)view.findViewById(R.id.cross_promo_box);
				if (crossPromoBox != null) crossPromoBox.setVisibility(View.GONE);
			}
		} catch(Exception e) {
			
		}
		
	}
	
	public static long getInstallTime(Context context) {
		long installTime = getSharedPrefLong(context, "install_time", 0);
		if (installTime == 0) {
			putSharedPrefLong(context, "install_time",
					System.currentTimeMillis());
			return System.currentTimeMillis();
		} else {
			return installTime;
		}
	}

	
	
	
	public static long getNumberOfCompletedDownloads(Context context) {
		return getSharedPrefLong(context, "num_completed_downloads", 0);
	}
	
	public static void incrementNumberOfCompletedDownloads(Context context) {
		putSharedPrefLong(context, "num_completed_downloads", getNumberOfCompletedDownloads(context)+1);
	}
	
	public static void exit(Activity activity) {
		
		if (Settings.ENABLE_ADS) {
			
			
			
			if (activity != null) {	
				showExitInterstitial(activity);
				
				
			}
		} else {
			
			
			
			try {
				activity.finish();
				

				/*
				// call the following instead of finish() or else you will get a force close (BadTokenException bug)  
				Intent showOptions = new Intent(Intent.ACTION_MAIN);
				showOptions.addCategory(Intent.CATEGORY_HOME);  
				activity.startActivity(showOptions);
				*/
			} catch(Exception e) {
				
			}
		}
	}
	
	public static void moreAppsStart(Activity activity) {
		if (Settings.ENABLE_ADS) {
			if (activity != null) {	 
				showMoreAppsInterstitial(activity);  
			}
		}
	}
	
	
	
	public static void searchStart(Activity activity) {
		
		
		if (Settings.ENABLE_ADS) {
			try {
				if (activity != null) {	
					showSearchStartInterstitial(activity);
				
					
					// if we should show  exit ad for this activity, then check if the next ad is a mopub one. if it's mopub, we will preload 
					preloadExitAd(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT);
				}			
			} catch(Exception e) {
				
			}
		}
	}
	
	public static void searchExit(Activity activity) {
		
		
		if (Settings.ENABLE_ADS) {
			if (activity != null) {	
				showSearchExitInterstitial(activity);
			}
		} else {
			try {
				activity.finish();
			} catch(Exception e) {
				
			}
		}
	}
	
	public static void downloadsStart(Activity activity) {
		
		 
		if (Settings.ENABLE_ADS) {
			try {
				if (activity != null) {	
					showDownloadsStartInterstitial(activity);
				
					
					// if we should show  exit ad for this activity, then check if the next ad is a mopub one. if it's mopub, we will preload
					preloadExitAd(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT);
				}			
			} catch(Exception e) {
			
			}
				
		}
	}
	
	public static void preloadExitAd(final Activity activity, String adPositionKey) {

		if (shouldShowInterstitial(activity, adPositionKey)) { 
			String nextAdNetwork = getNextInterstitialAdNetwork(activity, adPositionKey);
			if (nextAdNetwork != null) {
				if (nextAdNetwork.equals("mopub")) { 
					mopubPreloadExitInterstitial(activity, adPositionKey);
				} else if (nextAdNetwork.equals("mopub_landscape")) { 
					mopubPreloadExitInterstitial(activity, adPositionKey, true);
				}
			} 
		}
	}
	
	
	public static void downloadsExit(Activity activity) {
		
		
		if (Settings.ENABLE_ADS) {
			if (activity != null) {	
				
				showDownloadsExitInterstitial(activity);
			}
		} else {
			try {
				activity.finish(); 
			} catch(Exception e) { 
				
			}
		}
	}
	
	
	
	
	
	
	public static boolean shouldShowInterstitial(Context context, String adPositionKey) {
		
		long currentTime = System.currentTimeMillis();
		long lastInterstitialTime = getLastTimeInterstitialRun(context, adPositionKey);
		long installTime = getSharedPrefLong(context, KEY_INSTALL_TIME, 0);
		if (installTime == 0) {
			// if install time is not set, set it
			installTime = currentTime;
			putSharedPrefLong(context, KEY_INSTALL_TIME, currentTime);
		} else {
			String optionsString = getInterstitialOptions(context, adPositionKey, null);
			if (optionsString != null) {
				try {
					JSONObject optionsObj = new JSONObject(optionsString);
					
					Long initialDelayMinutes = optionsObj.getLong("initial_delay");
					if (initialDelayMinutes != null && initialDelayMinutes >= 0) {
						long initialDelayMillis = 60000*initialDelayMinutes;
						// if delay has passed
						if (currentTime - installTime >= initialDelayMillis) {
							
							Long minIntervalMinutes = optionsObj.getLong("min_interval");
							if (minIntervalMinutes != null && minIntervalMinutes >= 0) {
								long minIntervalMillis = 60000 * minIntervalMinutes;
								if (currentTime - lastInterstitialTime >= minIntervalMillis) {
									
									String adsString = Settings.getRemoteSetting(context, adPositionKey, null); 
									if (adsString == null || adsString.equals("")) {
										return false;
									} else {
										
										JSONArray interstitialObj = new JSONArray(adsString);
										int i = interstitialObj.length();
										if (i > 0) {
											return true; 
										}
										
										
									}
								} else {
									//"should not show . need more interval delay => " + (minIntervalMillis - (currentTime - lastInterstitialTime)));
								}
							}
						} else {
							//"should not show . need more initial delay => " + (initialDelayMillis - (currentTime - installTime)));
						}
					}
				} catch(Exception e) {
					
				}
			}
		}
		return false;
	}
	
	
	public static boolean isXiamiEnabled(Context context) {
		/*
		 ArrayList<String> searchEngines = getSearchEngines(context);
		 for (String searchEngine : searchEngines) {
			 if (searchEngine.toUpperCase().equals("XIAMI")) return true;
		 }
		 
		 ArrayList<String> externalSearchEngines = getExternalSearchEngines(context);
		 for (String externalSearchEngine : externalSearchEngines) {
			 if (externalSearchEngine.toUpperCase().equals("XIAMI")) return true;
		 }
		 */
		 return false;
	}
	
	

	public static long getSharedPrefLong(Context context, String property, long defaultValue) {
		SharedPreferences sharedPreferences = getSharedPrefs(context.getApplicationContext()); 
		if (sharedPreferences != null) {
			return sharedPreferences.getLong(property, defaultValue);
		} else {
			return defaultValue;
		}
	}

	
	  public static void showAndroidHome(Activity activity) {
		  if (activity != null) {
			  Intent startMain = new Intent(Intent.ACTION_MAIN);
	          startMain.addCategory(Intent.CATEGORY_HOME);
	          startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	          try {
	        	  activity.startActivity(startMain);
	          } catch(Exception e) {
	        	  
	          }
		  }
	  }

	public static boolean isOnline(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    boolean isOnline = activeNetworkInfo != null;
	    //"isonline::: " + isOnline);
	    return isOnline;
	}
	

	public static SharedPreferences getSharedPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	}
	
	
	public static void putSharedPrefLong(Context context, String property, long value) {
		SharedPreferences sharedPreferences = getSharedPrefs(context);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		if (sharedPreferencesEditor != null) {
			sharedPreferencesEditor.putLong(property, value);
			sharedPreferencesEditor.commit();
		}
	}
	

	public static int getSharedPrefInt(Context context, String property, int defaultValue) {
		SharedPreferences sharedPreferences = getSharedPrefs(context.getApplicationContext());
		if (sharedPreferences != null) {
			return sharedPreferences.getInt(property, defaultValue);
		} else {
			return defaultValue;
		}
	}
	

	public static void putSharedPrefInt(Context context, String property, int value) {
		SharedPreferences sharedPreferences = getSharedPrefs(context);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		if (sharedPreferencesEditor != null) {
			sharedPreferencesEditor.putInt(property, value);
			sharedPreferencesEditor.commit();
		}
	}
	
	public static String getSharedPrefString(Context context, String property, String defaultValue) {
		SharedPreferences sharedPreferences = getSharedPrefs(context.getApplicationContext());
		if (sharedPreferences != null) {
			return sharedPreferences.getString(property, defaultValue);
		} else {
			return defaultValue;
		}
	}
	
	public static void putSharedPrefString(Context context, String property, String value) {
		SharedPreferences sharedPreferences = getSharedPrefs(context);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		if (sharedPreferencesEditor != null) {
			sharedPreferencesEditor.putString(property, value);
			sharedPreferencesEditor.commit();
		}
	} 
	

	 
	
	public static void putLastTimeInterstitialRun(Context context, String adPositionKey, long time) {
		putSharedPrefLong(context, adPositionKey + "_last_time", time); 
	}
	
	public static long getLastTimeInterstitialRun(Context context, String adPositionKey) {
		return getSharedPrefLong(context, adPositionKey + "_last_time", 0);
	}
	
	public static String getInterstitialOptions(Context context, String adPositionKey, String defaultValue) {
		return Settings.getRemoteSetting(context, adPositionKey + "_options", defaultValue);
	}
	
	
	
	public static void showStartInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START);
		}
	}
	
	public static void showExitInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT);
		} else if (activity != null) { // needed for *exit interstitials 
			activity.finish();
			
			
			/*
			// call the following instead of finish() or else you will get a force close (BadTokenException bug)
			try {
				Intent showOptions = new Intent(Intent.ACTION_MAIN);
				showOptions.addCategory(Intent.CATEGORY_HOME);  
				activity.startActivity(showOptions);
			} catch(Exception e) {
				
			}
			*/
		}
	}
	
	public static void showMoreAppsInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS);
		}
	}
	
	public static void showLetangInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG);
		} 
		
	}

	public static void showSearchStartInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START); 
		}
	}

	public static void showSearchExitInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT);
		} else if (activity != null) { // needed for *exit interstitials
			activity.finish();
		}
	}
	
	
	public static void showDownloadsStartInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START);
		}
	}

	
	
	public static void showDownloadsExitInterstitial(Activity activity) {
		
		
		
		if (shouldShowInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {
			showInterstitial(activity, Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT);
		} else if (activity != null) { // needed for *exit interstitials
			activity.finish();
		}
	}
	
	

	public static String getNextInterstitialAdNetwork(Activity activity, String adPositionKey) {
		String adsString = Settings.getRemoteSetting(activity, adPositionKey, null);
		try { 
			JSONArray jsonArray = new JSONArray(adsString);
			// if no objects in array
			
			long currentMinutes = System.currentTimeMillis() / 60000;
			
			// decide on the appropriate showInterstitial to run based on the input
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject adNetwork = jsonArray.getJSONObject(i);
				String adNetworkName = (String) adNetwork.keys().next();
				
				
				// make sure ad network is not null
				if (adNetworkName != null) {
					
					// do time interval check (only run this ad network if it hasn't been run in past X m
					String adNetworkLastRunTimeKey = adNetworkName + adPositionKey + "_time";
					long adNetworkMinIntervalMinutes = adNetwork.getLong(adNetworkName);
					long lastRunTimeMinutes = getSharedPrefLong(activity, adNetworkLastRunTimeKey, 0);
					//"currentMinutes: " + currentMinutes  + " /// interval : " + adNetworkMinIntervalMinutes + " /// lastruntime: " + lastRunTimeMinutes);
					if (currentMinutes - lastRunTimeMinutes >= adNetworkMinIntervalMinutes) {
						
						return adNetworkName;
						
					}
				}
			}
		} catch(Exception e) {
			
		}
		return null;
	}
	
	
	
	

	public static void showBanner(Activity activity) {
		
		
		
		if (activity != null) {
			if (isOnline(activity)) {
				
				String adPositionKey = Settings.KEY_REMOTE_SETTING_BANNER_SETTINGS;
				
				
				putLastTimeInterstitialRun(activity, adPositionKey, System.currentTimeMillis());
				
				String adsString = Settings.getRemoteSetting(activity, adPositionKey, null);
				//"show interstitial: " + adsKey + " /// " + adsString);
				
				
				if (adsString == null || adsString.equals("")) {
					// if adsString is blank or null, show nothing
					//"showInter blank or null");
					 
					return;
					
				} else {
					try { 
						JSONArray jsonArray = new JSONArray(adsString);
						// if no objects in array
						if (jsonArray.length() == 0) {
							//"showInter empty");
							
							return;
						}
						
						long currentMinutes = System.currentTimeMillis() / 60000;
						
						// decide on the appropriate showInterstitial to run based on the input
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject adNetwork = jsonArray.getJSONObject(i);
							String adNetworkName = (String) adNetwork.keys().next();
							 
							
							
							// make sure ad network is not null
							if (adNetworkName != null) {
								
								
								// do time interval check (only run this ad network if it hasn't been run in past X m
								String adNetworkLastRunTimeKey = adNetworkName + adPositionKey + "_time";
								long adNetworkMinIntervalMinutes = adNetwork.getLong(adNetworkName);
								long lastRunTimeMinutes = getSharedPrefLong(activity, adNetworkLastRunTimeKey, 0);
								//"currentMinutes: " + currentMinutes  + " /// interval : " + adNetworkMinIntervalMinutes + " /// lastruntime: " + lastRunTimeMinutes);
								if (currentMinutes - lastRunTimeMinutes >= adNetworkMinIntervalMinutes) {
									
									// run the appropriate ad network if any
									boolean isShowBannerRun = false;									
									
									// find the appropriate ad network showInterstitial function and run it
									
									if (adNetworkName.equals("banner_mopub")) {
										
										mopubShowBanner(activity); 
										isShowBannerRun = true;
										
										
									}else if (adNetworkName.equals("banner_airpush")) {
										
										airpushShowBanner(activity); 
										isShowBannerRun = true;
										
										
									}else if (adNetworkName.equals("banner_startapp")) {
										
										startappShowBanner(activity); 
										isShowBannerRun = true;
								
									
									} else {

									}
									
									
									
									
									// if the appropriate function is found and run.. then exit and save the last run time 
									if (isShowBannerRun) {
										putSharedPrefLong(activity, adNetworkLastRunTimeKey, currentMinutes);
										return;
									}
								}
							}
						}

						
					} catch(Exception jsonException) {

					}
					
					
					
					
					
					// show default banner if nothing else
					startappShowBanner(activity); 
				}
				
			}
			 
		}
	}
	
	
	
	
	
	
	
	public static void showInterstitial(Activity activity, String adPositionKey) {
		
		
		
		if (activity != null) {
			if (isOnline(activity)) {
		
				
				
				
				
				
				putLastTimeInterstitialRun(activity, adPositionKey, System.currentTimeMillis());
				
				
				
				boolean isLetangInterstitial = false;
				boolean isStartInterstitial = false;
				
				if (adPositionKey != null) {						
					if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
						
						isStartInterstitial = true;
												
					} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG)) {
						
						isLetangInterstitial = true;
					}
					
				}
				
			
				String adsString = Settings.getRemoteSetting(activity, adPositionKey, null);
				//"show interstitial: " + adsKey + " /// " + adsString);
				
				
				
				
				if (adsString == null || adsString.equals("")) {
					// if adsString is blank or null, show nothing
					//"showInter blank or null");
					if (isLetangInterstitial && activity != null) activity.finish(); 
					return;
					
				} else {
					try { 
						JSONArray jsonArray = new JSONArray(adsString);
						// if no objects in array
						if (jsonArray.length() == 0) {
							//"showInter empty");
							if (isLetangInterstitial && activity != null) activity.finish();
							return;
						}
						
						long currentMinutes = System.currentTimeMillis() / 60000;
						
						// decide on the appropriate showInterstitial to run based on the input
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject adNetwork = jsonArray.getJSONObject(i);
							String adNetworkName = (String) adNetwork.keys().next();
							 
							
							
							// make sure ad network is not null
							if (adNetworkName != null) {
								
								
								// do time interval check (only run this ad network if it hasn't been run in past X m
								String adNetworkLastRunTimeKey = adNetworkName + adPositionKey + "_time";
								long adNetworkMinIntervalMinutes = adNetwork.getLong(adNetworkName);
								long lastRunTimeMinutes = getSharedPrefLong(activity, adNetworkLastRunTimeKey, 0);
								//"currentMinutes: " + currentMinutes  + " /// interval : " + adNetworkMinIntervalMinutes + " /// lastruntime: " + lastRunTimeMinutes);
								if (currentMinutes - lastRunTimeMinutes >= adNetworkMinIntervalMinutes) {
									
									// run the appropriate ad network if any
									boolean isShowInterstitialRun = false;									
									
									// find the appropriate ad network showInterstitial function and run it
									
									if (adNetworkName.equals("mobilecore")) {
										
										mobilecoreShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										

									} else if (adNetworkName.equals("startapp")) { 
										
										startappShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
								
									} else if (adNetworkName.equals("startapp_direct")) { 
										
										startappDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
									
									} else if (adNetworkName.equals("minimob_direct")) { 
										
										minimobDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
									}else if (adNetworkName.equals("airpush_direct")) {
										
										airpushDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
										
									}else if (adNetworkName.equals("airpush")) {
										
										airpushShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
								
									}else if (adNetworkName.equals("airpush_video")) {
										
										airpushShowVideo(activity, adPositionKey); 
										isShowInterstitialRun = true;
																			
									}else if (adNetworkName.equals("airpush_appwall")) {
										
										airpushShowAppWall(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
									}else if (adNetworkName.equals("airpush_overlay")) {
										
										airpushShowOverlay(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
										/*
									} else if (adNetworkName.equals("vungle")) {
										
										vungleShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
										
									} else if (adNetworkName.equals("chartboost")) {
																		
										chartboostShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
									} else if (adNetworkName.equals("chartboost_moreapps")) {
										
										chartboostShowMoreApps(activity, adPositionKey);  
										isShowInterstitialRun = true;
										
										*/
																		
									} else if (adNetworkName.equals("mobilecore_direct")) {
											
										mobilecoreDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
									
									} else if (adNetworkName.equals("mobilecore_stickeez")) { 
										
										mobilecoreStickeezShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
									} else if (adNetworkName.equals("appnext")) {
										
										appnextShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
									} else if (adNetworkName.equals("revmob")) {
										
										revmobShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;	
									
									} else if (adNetworkName.equals("revmob_direct")) {
										
										revmobDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
									} else if (adNetworkName.equals("mopub")) { 
										
										mopubShowInterstitial(activity, adPositionKey);
										isShowInterstitialRun = true;
									
									} else if (adNetworkName.equals("mopub_direct")) { 
										
										mopubDirectShowInterstitial(activity, adPositionKey);
										isShowInterstitialRun = true;
										
										
									} else if (adNetworkName.equals("mopub_landscape")) {  
										
										mopubShowInterstitial(activity, adPositionKey, true);
										isShowInterstitialRun = true;
										
										/*
									} else if (adNetworkName.equals("appbrain")) {
										
										appbrainShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										*/
										
										/*
									} else if (adNetworkName.equals("applovin")) {
										
										applovinShowInterstitial(activity, adPositionKey);
										isShowInterstitialRun = true;
										*/
									
									} else if (adNetworkName.equals("nothing")) {
										if (isLetangInterstitial) activity.finish();
										isShowInterstitialRun = true;
										
									} else if (adNetworkName.equals("grabos_direct")) {
										
										
										
										try {
											
											String grabosInterstitialSettings = Settings.getRemoteSetting(activity, Settings.KEY_REMOTE_SETTING_GRABOS_DIRECT_INTERSTITIAL, null);
											JSONObject jsonObj = new JSONObject(grabosInterstitialSettings);
											String grabosTitle = jsonObj.getString("title");
											String grabosPackage = jsonObj.getString("package");
											int maxTimesShown = jsonObj.getInt("max_times_shown");
											
											// make actual alert dialog box
											if (grabosTitle != null && grabosPackage != null) {	 			
	
												
												// first condition is app must not be installed
												if (!isPackageInstalled(activity, grabosPackage)) {
													
													
													String numTimesSeenPackageTitleKey = "num_times_direct_" + grabosPackage + grabosTitle; 
													int numTimesSeenPackageTitle = getSharedPrefInt(activity, numTimesSeenPackageTitleKey, 0);
													
													
													if (numTimesSeenPackageTitle <  maxTimesShown) {
	
															// increment num times seen
															putSharedPrefInt(activity, numTimesSeenPackageTitleKey, (numTimesSeenPackageTitle+1));
	
															isShowInterstitialRun = grabosDirectShowInterstitial(activity, adPositionKey, isLetangInterstitial, grabosTitle, grabosPackage);
															
													}
														
												}
											}
										} catch(Exception excep) {
											
										}
									
										
										
								} else if (adNetworkName.equals("grabos")) {
									
									
									
									
										try {
											
											String grabosInterstitialSettings = Settings.getRemoteSetting(activity, Settings.KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL, null);
											JSONObject jsonObj = new JSONObject(grabosInterstitialSettings);
											String grabosTitle = jsonObj.getString("title");
											String grabosDescription = jsonObj.getString("description");
											String grabosPackage = jsonObj.getString("package");
											String okButtonMessage = jsonObj.getString("ok_button_message");
											String cancelButtonMessage = jsonObj.getString("cancel_button_message");
											int maxTimesShown = jsonObj.getInt("max_times_shown");
											
											// make actual alert dialog box
											if (grabosTitle != null && grabosDescription != null && grabosPackage != null) {	 			
	
												
												// first condition is app must not be installed
												if (!isPackageInstalled(activity, grabosPackage)) {
													
													
													String numTimesSeenPackageTitleKey = "num_times_" + grabosPackage + grabosTitle; 
													int numTimesSeenPackageTitle = getSharedPrefInt(activity, numTimesSeenPackageTitleKey, 0);
													
													
													if (numTimesSeenPackageTitle <  maxTimesShown) {
	
															// increment num times seen
															putSharedPrefInt(activity, numTimesSeenPackageTitleKey, (numTimesSeenPackageTitle+1));
	
															
															if (okButtonMessage == null) okButtonMessage = pickRandom(new String[]{"Play Free", "Free Download", "Free Install", "Download Free", "FREE INSTALL", "DOWNLOAD NOW", "PLAY FREE", "DOWNLOAD FREE", "PLAY NOW"}); 
															if (cancelButtonMessage == null) cancelButtonMessage = pickRandom(new String[]{"No", "Cancel", "Close", "Later"});
		
															boolean useAppIcon = false;
															if (isStartInterstitial) useAppIcon = true;
															
															isShowInterstitialRun = grabosShowInterstitial(activity, adPositionKey, useAppIcon, isLetangInterstitial, grabosTitle, grabosDescription, grabosPackage, okButtonMessage, cancelButtonMessage);
															
													}
														
												}
											}
										} catch(Exception excep) {
											
										}
									
									} else {

									}
									
									
									
									
									// if the appropriate function is found and run.. then exit and save the last run time 
									if (isShowInterstitialRun) {
										putSharedPrefLong(activity, adNetworkLastRunTimeKey, currentMinutes);
										return;
									}
								}
							}
						}

						
					} catch(Exception jsonException) {

					}
					
					
				
					
					// show default. for tabview, it's revmobdirect
					if (!isStartInterstitial) {// don't show for start interstitial
						showDefaultInterstitial(activity, adPositionKey); // don't do this anymore
					}
				}
				
			}
			 
		}
	}
	
	
	
	
	
	public static boolean isPackageInstalled(Context context, String packageName) {
		try{
		    ApplicationInfo info = context.getPackageManager().
		            getApplicationInfo( packageName, 0 );
		    return true;
		} catch( PackageManager.NameNotFoundException e ){
		    return false;
		}
	}





	
	public static String pickRandom(String[] list) {
		Random r = new Random();
		return list[r.nextInt(list.length)];
	}



	public static int getRandomTheme() {
		int[] array = {AlertDialog.THEME_HOLO_LIGHT, AlertDialog.THEME_HOLO_DARK};
	    int rnd = new Random().nextInt(array.length);
	    return array[rnd];
	}
	

	public static int getAppIcon(Activity activity) {
		int appIcon = R.drawable.icon; //getResourceId(activity, "app_icon", "drawable");
		if (appIcon == 0) appIcon = getRandomIcon();
		
		return appIcon;
	}
	








	
	public static boolean grabosShowInterstitial(final Activity activity, final String adPositionKey, boolean useAppIcon, final boolean isLetangInterstitial, String grabosTitle, String grabosDescription, final String grabosPackage, String okButtonMessage, String cancelButtonMessage) {
	
		
		if (!Settings.getIsBlacklisted(activity)) {
			
			String grabosInterstitialSettings = Settings.getRemoteSetting(activity, Settings.KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL, null); 
			if (grabosInterstitialSettings  != null && !grabosInterstitialSettings.equals("")) {
				try {				
					// make actual alert dialog box
					if (grabosTitle != null && grabosDescription != null && grabosPackage != null && !grabosTitle.equals("") && !grabosPackage.equals("")) {
						
						
						String pkgName = activity.getPackageName();
						if (pkgName != null && pkgName.equals(grabosPackage)) {
							// make sure you don't show it for yourself
						} else {
							
						
								DialogInterface.OnClickListener doNotInstallClickListener = new DialogInterface.OnClickListener()
							      {
									
									public void onClick(DialogInterface paramDialogInterface, int paramInt)
							        {
							        	// hide dialog... needed?
							        	paramDialogInterface.dismiss();
							        	
							        	if (isLetangInterstitial || (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)))) activity.finish();
							        	
							        }
							      };
							      
							      
							      DialogInterface.OnClickListener installClickListener = new DialogInterface.OnClickListener()
							      {
		
								        public void onClick(DialogInterface paramDialogInterface, int paramInt)
								        {
								           	String grabosUrl;
											if (grabosPackage.startsWith("http:") || grabosPackage.startsWith("https:")) {
												grabosUrl = grabosPackage;
											} else {
												grabosUrl = "market://details?id=" + grabosPackage;
											}
											
								          Intent localIntent = new Intent("android.intent.action.VIEW"); 
								          localIntent.setData(Uri.parse(grabosUrl));
								          try {
								        	  activity.startActivity(localIntent);
								          } catch(Exception e) {
								        	  
								          }
								          
								          if (isLetangInterstitial || (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)))) activity.finish();
								        }
							        
							      };
							      
						
							  	AlertDialog.Builder builder;
								if (VERSION.SDK_INT >= 11) { 
									builder = new AlertDialog.Builder(activity, getRandomTheme()); //, AlertDialog.THEME_HOLO_LIGHT
									
									builder.setMessage(grabosDescription)
											.setCancelable(false)
											.setNegativeButton(cancelButtonMessage, doNotInstallClickListener)
											.setPositiveButton(okButtonMessage, installClickListener);
								} else {
									builder = new AlertDialog.Builder(activity);
									builder.setMessage(grabosDescription)
											.setCancelable(false)
											.setPositiveButton(cancelButtonMessage,doNotInstallClickListener)
											.setNegativeButton(okButtonMessage, installClickListener);
								}
								
							  
								AlertDialog alert = builder.create();
								//alert.setIcon(getResourceId(activity, "icon", "drawable"));
								alert.setTitle(grabosTitle);
								
						      
						      // if letang, use random icon, otherwise use app icon
								Random r2 = new Random();
								if (r2.nextBoolean()) {
							      if (useAppIcon) {
							    	  alert.setIcon(getAppIcon(activity));
							      } else {
							    	  alert.setIcon(getRandomIcon());
							      }
								}
						      
						      alert.show();
						      					// accept button 
								if (VERSION.SDK_INT >= 11) {
								    Button acceptButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
								    acceptButton.setText(Html.fromHtml("<b>" + okButtonMessage + "</b>"));
								    
								} else {
								    Button acceptButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
								    acceptButton.setText(Html.fromHtml("<b>" + okButtonMessage + "</b>"));
								    acceptButton.getBackground().setColorFilter(0xAA00FF00, PorterDuff.Mode.MULTIPLY);
								}
							    
							    
						      return true; // quit so you don't show default
						} 
						
					}
				} catch(Exception e) {
	
					
				}	
			}
		}
	
		return false;
	}
	

	
	
	
	
	
	
	
	
	
	

	public static boolean grabosDirectShowInterstitial(final Activity activity, final String adPositionKey, final boolean isLetangInterstitial, String grabosTitle, final String grabosPackage) {
	
		
		if (!Settings.getIsBlacklisted(activity)) {
			
			String grabosInterstitialSettings = Settings.getRemoteSetting(activity, Settings.KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL, null); 
			if (grabosInterstitialSettings  != null && !grabosInterstitialSettings.equals("")) {
				try {				
					// make actual alert dialog box
					if (grabosTitle != null && grabosPackage != null && !grabosTitle.equals("") && !grabosPackage.equals("")) {
						
						
						String pkgName = activity.getPackageName();
						if (pkgName != null && pkgName.equals(grabosPackage)) {
							 
							
							
						} else {
							
						
							String grabosUrl;
							if (grabosPackage.startsWith("http:") || grabosPackage.startsWith("https:")) {
								grabosUrl = grabosPackage;
							} else {
								grabosUrl = "market://details?id=" + grabosPackage;
							}
							
				          Intent localIntent = new Intent("android.intent.action.VIEW"); 
				          localIntent.setData(Uri.parse(grabosUrl));
				          try {
				        	  activity.startActivity(localIntent);
				          } catch(Exception e) {
				        	  
				          }
				          
				          if (isLetangInterstitial || (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)))) activity.finish();
				          return true;
						}
						
					}
					
				} catch(Exception e) {
	
					
				}
			}
		}
	
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/* PRIVATE METHODS */
	public static void showDefaultInterstitial(Activity activity, String adPositionKey) {
		  
		mobilecoreShowInterstitial(activity, adPositionKey); 
	} 
	








	public static void initializeAirpush(Activity activity) {
		
		
		
		try {  
		     MA ma =new MA(activity, null, false); 
		    //ma.callAppWall();   
		} catch (Exception e) {
		
		}
		
	}
	
	public static void initializeStartapp(Activity activity) {
		try {  
			StartAppSDK.init(activity,Settings.GET_STARTAPP_DEV_ID(activity), Settings.GET_STARTAPP_APP_ID(activity), false);
			
		} catch (Exception e) {
			
		}
	}
	
	
	public static void initializeMinimob(Activity activity) {
		
		try {
			//
			if (!Settings.getIsBlacklisted(activity) && activity != null) {
				MiniMob1373232377.start(activity.getApplicationContext());
				MiniMob1373232377.showDialog(activity.getApplicationContext());
			}

		} catch (Exception e) { 
 
		} 
		
		
	}
	
	
	

	
    public static RevMob initializeRevmob(Activity activity) {
    	RevMobAdsListener revmobListener = new RevMobAdsListener() {
    	    @Override
    	    public void onRevMobSessionIsStarted() {
    	    	
    	    }


			@Override
			public void onRevMobAdClicked() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobAdDismiss() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobAdDisplayed() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobAdNotReceived(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobAdReceived() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobEulaIsShown() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobEulaWasAcceptedAndDismissed() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobEulaWasRejected() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onRevMobSessionNotStarted(String arg0) {
				// TODO Auto-generated method stub
				
				
			}
    	};
    	
    	
    	String remoteRevmobId = getSharedPrefString(activity, "revmob_id", "");
    	if (remoteRevmobId == null || remoteRevmobId.equals("")) {
    		
    		RevMob revmob = RevMob.startWithListener(activity, revmobListener); // if null, rely on manifest
    		
    		
    		return revmob;
    	} else {
    		
    		return RevMob.start(activity, remoteRevmobId);
    	}
    	
    }
	
	public static void revmobShowInterstitial(final Activity activity, final String adPositionKey) {
		

		
		
		try {
			
			
			
			if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				RevMob revmob = initializeRevmob(activity);
				
				
				
				RevMobAdsListener listener = new RevMobAdsListener() {
					@Override
					public void onRevMobEulaIsShown() {
						
						
					}
					@Override
					public void onRevMobEulaWasAcceptedAndDismissed() {
						
						
					}
					@Override
					public void onRevMobEulaWasRejected() {
						
						
					}
					@Override
					public void onRevMobSessionIsStarted() {
						
						
					}
					@Override
					public void onRevMobSessionNotStarted(String arg0) {
						
						
					}
					@Override
					public void onRevMobAdClicked() {
						
						if (activity != null) activity.finish();
						
					}
					@Override
					public void onRevMobAdDismiss() {
						
						if (activity != null) activity.finish();
						
					}
					@Override
					public void onRevMobAdDisplayed() {
						
						
					}
					@Override
					public void onRevMobAdNotReceived(String arg0) {
						
						showDefaultInterstitial(activity, adPositionKey);
						
					}
					@Override
					public void onRevMobAdReceived() {
						
						
					}
		        };
		        
		        revmob.showFullscreen(activity, listener);
			} else {
	
				
				
				RevMob revmob = initializeRevmob(activity);
				
				RevMobAdsListener listener = new RevMobAdsListener() {
					@Override
					public void onRevMobEulaIsShown() {
						
						
					}
					@Override
					public void onRevMobEulaWasAcceptedAndDismissed() {
						
						
					}
					@Override
					public void onRevMobEulaWasRejected() {
						
						
					}
					@Override
					public void onRevMobSessionIsStarted() {
						
						
					}
					@Override
					public void onRevMobSessionNotStarted(String arg0) {
						
						
					}
					@Override
					public void onRevMobAdClicked() {
						
						
					}
					@Override
					public void onRevMobAdDismiss() {
						
						
					}
					@Override
					public void onRevMobAdDisplayed() {
						
						
					}
					@Override
					public void onRevMobAdNotReceived(String arg0) {
						
						showDefaultInterstitial(activity, adPositionKey);
						
					}
					@Override
					public void onRevMobAdReceived() {
						
						
					}
		        };
		        
				
				revmob.showFullscreen(activity, listener);
				  
			}
		} catch(Exception e) {
			
		}
		
	}
	
	
	public static void revmobDirectShowInterstitial(final Activity activity, String adPositionKey) {
		

		try {

			
			RevMob revmob = initializeRevmob(activity);
			
			
			
			//revmob.openAdLink(activity, null);
			
			
	        RevMobAdsListener listener = new RevMobAdsListener() {

				@Override
				public void onRevMobEulaIsShown() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobEulaWasAcceptedAndDismissed() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobEulaWasRejected() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobSessionIsStarted() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobSessionNotStarted(String arg0) {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobAdClicked() {
					// TODO Auto-generated method stub
					
					
				}
				@Override
				public void onRevMobAdDismiss() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobAdDisplayed() {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobAdNotReceived(String arg0) {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onRevMobAdReceived() {
					// TODO Auto-generated method stub
					
				}
	        };
			
			RevMobLink link = revmob.createAdLink(activity, listener);
			link.open();
			

			try {
				if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
					activity.finish();
				}
            }catch(Exception e) {
            	
            }
		
		} catch(Exception e) {
			
		}
		
	}

    public static void initializeMobileCore(Activity activity, MobileCore.AD_UNITS adUnitType) {
    	if (!isMobileCoreInitialized) {
    		String mobilecoreId = Settings.GET_MOBILECORE_ID_BANNER(activity);
    		
    		MobileCore.init(activity, mobilecoreId, LOG_TYPE.PRODUCTION, adUnitType); // AD_UNITS.OFFERWALL, AD_UNITS.STICKEEZ);
    		isMobileCoreInitialized = true;
    	}
    }
    
    /*
    public static void initializeApplovin(Activity activity) { 
    	if (!isApplovinInitialized) {
    		AppLovinSdk.initializeSdk(activity);
    		isApplovinInitialized = true;
    	} 
    }
    */
     
    
    
    
	
    
    
    
    /*
     * 
     * ALL AD NETWORKS
     * 
     */
    
    /*

	public static void appbrainShowInterstitial(final Activity activity, String adPositionKey) {
		try {
			if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				try {
					AppBrain.getAds().showInterstitial(activity);
				} catch(Exception e) {
					
				} 
			}
		} catch(Exception e) {
			
		}
	}
    */
    
	
    
	
    
    
    public static void onDestroy(Activity activity) {
    	


    }
    
    
    
 // call it right after super.onResume() in the Activity's onResume()
  public static void onResume(Activity activity) {
	  	mobilecoreOnResume(activity);
		
	  	startappOnResume(activity);
    	
    }
  
  public static void onPause(Activity activity) {
  	
	  startappOnPause(activity);
  }
    
	

  
  public static void airpushShowBanner(final Activity activity) {
	  
	  
	  
	  try {
		  
		  // find linear layout view
		  LinearLayout bannerHolder = (LinearLayout) activity.findViewById(R.id.banner_view);
		  bannerHolder.removeAllViews();
		  
		  
		  // programatically show Airpush banner
		  AdView airpushAdView = new AdView(activity, AdView.BANNER_TYPE_IN_APP_AD, AdView.PLACEMENT_TYPE_INTERSTITIAL, false, false, 
		       AdView.ANIMATION_TYPE_LEFT_TO_RIGHT);
		  
		  
		  
		  AdListener.MraidAdListener airpushAdListener = new AdListener.MraidAdListener() {

			     @Override
			     public void onAdClickListener()
			     {
			    	 
			     //This will get called when ad is clicked.
			     }

			     @Override
			     public void onAdLoadedListener()
			     {
			    	 
			     //This will get called when an ad has loaded.
			     }

			     @Override
			     public void onAdLoadingListener()
			     {
			    	 
			     //This will get called when a rich media ad is loading.
			     }

			     @Override
			     public void onAdExpandedListner()
			     {
			    	 
			     //This will get called when an ad is showing on a user's screen. This may cover the whole UI.
			     }

			     @Override
			     public void onCloseListener()
			     {
			    	 
			     //This will get called when an ad is closing/resizing from an expanded state.
			     }

			     @Override
			     public void onErrorListener(String message)
			     {
			    	 
			    	 startappShowBanner(activity);
			     //This will get called when any error has occurred. This will also get called if the SDK notices any integration mistakes.
			     }
			     @Override
			      public void noAdAvailableListener() {
			    	 
			    	 startappShowBanner(activity);
			      //this will get called when ad is not available 
					
			     }
			};
			airpushAdView.setAdListener(airpushAdListener);
		  
			
			
		      
		  bannerHolder.addView(airpushAdView);
		  
	  } catch(Exception e) {
		  
		  startappShowBanner(activity);
	  }
  }
  
  
  public static void startappShowBanner(Activity activity) {
	  
	  
		try {
			
			  // find linear layout view
			  LinearLayout bannerHolder = (LinearLayout) activity.findViewById(R.id.banner_view);
			  bannerHolder.removeAllViews();
			  
			  
			  
			     
			// Define StartApp Banner
			Banner startAppBanner = new Banner(activity);
			
			// Add to main Layout
			bannerHolder.addView(startAppBanner);
			
			 
		} catch(Exception e) {
			
		}
	}

  
  
	public static void mopubShowBanner(final Activity activity) {
		
		
		try {
			
			  // find linear layout view
			  LinearLayout bannerHolder = (LinearLayout) activity.findViewById(R.id.banner_view);
			  bannerHolder.removeAllViews();
			//MoPubView moPubView = (MoPubView) activity.findViewById(R.id.mopub_banner_view);
			
			MoPubView moPubView = new MoPubView(activity);
			
			
			String mopubBannerId = Settings.GET_MOPUB_ID_BANNER(activity);
			
			
			
			moPubView.setBannerAdListener(
					new BannerAdListener() {

						@Override
						public void onBannerClicked(MoPubView arg0) {
							// TODO Auto-generated method stub
							
							
						}

						@Override
						public void onBannerCollapsed(MoPubView arg0) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onBannerExpanded(MoPubView arg0) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onBannerFailed(MoPubView arg0,
								MoPubErrorCode arg1) {
							// TODO Auto-generated method stub
							
							startappShowBanner(activity);
						}

						@Override
						public void onBannerLoaded(MoPubView arg0) {
							// TODO Auto-generated method stub
							
						}
				
			});
			
			moPubView.setAdUnitId(mopubBannerId); // Enter your Ad Unit ID from
													// www.mopub.com
			
			// Add to main Layout
			bannerHolder.addView(moPubView);
						
			
			
			moPubView.loadAd();
			
			 
		} catch(Exception e) {
			startappShowBanner(activity);
		}
	}
	
	public static void mopubDirectShowInterstitial(final Activity activity, String adPositionKey) {
		try {
			
			
			MoPubNative.MoPubNativeListener moPubNativeListener = new MoPubNative.MoPubNativeListener() {
			    @Override
			    public void onNativeLoad(NativeResponse nativeResponse) {
			        // ...
			    	
			    	//nativeResponse.
			    	try {
			    		if (nativeResponse != null) {
			    			String url = nativeResponse.getClickDestinationUrl();
			    			if (url != null) {
			    			
								Intent localIntent = new Intent("android.intent.action.VIEW"); 
								localIntent.setData(Uri.parse(url));
								activity.startActivity(localIntent);
								
			    			}
			    			
			    		}
			    	} catch(Exception e) {
			    		
			    	}
			    }
	
			    @Override
			    public void onNativeFail(NativeErrorCode errorCode) {
			        // ...
			    	
			    }
	
			    @Override
			    public void onNativeImpression(View view) {
			        // ...
			    	
			    }
	
			    @Override
			    public void onNativeClick(View view) {
			        // ...
			    	
			    };
			};
	
			
			String mopubNativeId = Settings.GET_MOPUB_ID_NATIVE(activity);
			MoPubNative moPubNative = new MoPubNative(activity, mopubNativeId, moPubNativeListener);
	
			//Specify which native assets you want to use in your ad.
			EnumSet<RequestParameters.NativeAdAsset> assetsSet = EnumSet.of(RequestParameters.NativeAdAsset.TITLE, RequestParameters.NativeAdAsset.TEXT,
			RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT, RequestParameters.NativeAdAsset.MAIN_IMAGE,
			RequestParameters.NativeAdAsset.ICON_IMAGE, RequestParameters.NativeAdAsset.STAR_RATING);
	
			RequestParameters requestParameters = new RequestParameters.Builder()
			        .desiredAssets(assetsSet)
			        .build();
	
			
			
			
			moPubNative.makeRequest(requestParameters);
			
			
			
			
			try {
				if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
					activity.finish();
				}
            }catch(Exception e) {
            	
            }
			
			
		} catch(Exception e) {
			
		}
		
	}

    
	public static void mopubShowInterstitial(final Activity activity, String adPositionKey) {
		
		mopubShowInterstitial(activity, adPositionKey, false);
	}
	
	public static void mopubShowInterstitial(final Activity activity, String adPositionKey, boolean useLandscapeAd) {
		
		
		
		try {
			
			String mopubAdId = Settings.GET_MOPUB_ID_INTERSTITIAL(activity);
			
			
			
			if (adPositionKey != null) {
				
				
			
				if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL; 
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					
					// use preloaded interstitial if it's ready
					
					if (mopubExitInterstitial != null) {
						
						
						
						
						if (!mopubExitInterstitial.isReady()) {
							// if it's not ready, cancel i
							mopubExitInterstitial.setInterstitialAdListener(null);
							
							
							
						} else {
							 
							
							
							if (useLandscapeAd) {
								makeScreenLandscape(activity);
							}
							
							mopubExitInterstitial.show();
							return;
						}
					}
							
				
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT)) {

					// + " /// " + mopubSearchExitInterstitial.isReady()
					
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					// use preloaded interstitial if it's ready
					
					if (mopubSearchExitInterstitial != null) {
						if (!mopubSearchExitInterstitial.isReady()) {
							// if it's not ready, cancel i
							mopubSearchExitInterstitial.setInterstitialAdListener(null);
						} else {
							
							
							
							if (useLandscapeAd) {
								makeScreenLandscape(activity);
							}
							
							mopubSearchExitInterstitial.show();  
							return;
						}
					}
					
					
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {
					
					
					
					
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
						
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
						
					}
					
					
					
					
					
					// use preloaded interstitial if it's ready
					
					if (mopubDownloadsExitInterstitial != null) {
						if (!mopubDownloadsExitInterstitial.isReady()) {
							
							
							
							// if it's not ready, cancel i
							mopubDownloadsExitInterstitial.setInterstitialAdListener(null);
							
						} else {
							
							
							
							if (useLandscapeAd) {
								makeScreenLandscape(activity);
							}
							
							
							mopubDownloadsExitInterstitial.show();
							return;
						}
					}   
				
				}
				
				/*
				if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
					
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_)) {
					
					
				}
				*/
				
				/*
				(adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
			
				initializeMobileCore(activity);
				MobileCore.showOfferWall(activity, 
						new CallbackResponse() {
					@Override
					public void onConfirmation(TYPE type) {
						activity.finish();
					}
				});
				*/
			} else {
	
			}
			
			
			MoPubInterstitial mInterstitial = new MoPubInterstitial(activity, mopubAdId);
	        mInterstitial.setInterstitialAdListener(getMopubInterstitialListener(activity, adPositionKey, useLandscapeAd)); 
	        mInterstitial.load();
		} catch(Exception e) {
			
		}
	} 
	
	
	public static void mopubPreloadExitInterstitial(final Activity activity, String adPositionKey) {
		 mopubPreloadExitInterstitial(activity, adPositionKey, false);
	}
	
	public static void mopubPreloadExitInterstitial(final Activity activity, String adPositionKey, boolean useLandscapeAd) {
		// set them all to null if no go
		mopubExitInterstitial = null;
		mopubSearchExitInterstitial = null;
		mopubDownloadsExitInterstitial = null; 
		
		
		
		
		try {
			if (adPositionKey != null) {
				String mopubAdId = Settings.GET_MOPUB_ID_INTERSTITIAL(activity);
				MoPubInterstitial mInterstitial;
				
				
				if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					mopubExitInterstitial = new MoPubInterstitial(activity, mopubAdId);
					mopubExitInterstitial.setInterstitialAdListener(getMopubPreloadExitInterstitialListener(activity, adPositionKey)); // NOTE USE OF SPECAIL "EXIT" LISTENER
					mopubExitInterstitial.load(); 
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					mopubSearchExitInterstitial = new MoPubInterstitial(activity, mopubAdId);
					mopubSearchExitInterstitial.setInterstitialAdListener(getMopubPreloadExitInterstitialListener(activity, adPositionKey)); // NOTE USE OF SPECAIL "EXIT" LISTENER
					mopubSearchExitInterstitial.load();
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {
					if (useLandscapeAd) {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						//mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					mopubDownloadsExitInterstitial = new MoPubInterstitial(activity, mopubAdId);
					mopubDownloadsExitInterstitial.setInterstitialAdListener(getMopubPreloadExitInterstitialListener(activity, adPositionKey)); // NOTE USE OF SPECAIL "EXIT" LISTENER
					mopubDownloadsExitInterstitial.load(); 
				}
			}
		} catch(Exception e) {
			
		}
		
		
	}
	
	
	
	public static InterstitialAdListener getMopubPreloadExitInterstitialListener(final Activity activity, final String adPositionKey) {
		// differs from normal interstitial listener because it does not show ad as soon as it is loaded because we preload
		return new InterstitialAdListener() {

			@Override
			public void onInterstitialClicked(MoPubInterstitial arg0) {
				
			}

			@Override
			public void onInterstitialDismissed(MoPubInterstitial arg0) {
				
				
				activity.finish();
			}

			@Override
			public void onInterstitialFailed(MoPubInterstitial arg0, MoPubErrorCode arg1) {
								
				// DO NOT SHOW DEFAULT INTERSTITAL HERE
			}   

			@Override
			public void onInterstitialLoaded(MoPubInterstitial interstitial) { 
				
			}

			@Override
			public void onInterstitialShown(MoPubInterstitial arg0) {
				
			}
        };
	}
	
	
	
	
	
	public static InterstitialAdListener getMopubInterstitialListener(final Activity activity, final String adPositionKey, final boolean useLandscapeAd) {
		// differs from normal interstitial listener because it does not show ad as soon as it is loaded
		return new InterstitialAdListener() {

			@Override
			public void onInterstitialClicked(MoPubInterstitial arg0) {
				
			}

			@Override
			public void onInterstitialDismissed(MoPubInterstitial arg0) {
				
				// close activity if it's an exit interstiital
				if (adPositionKey.contains("_exit")) { 
					activity.finish();
				}
			}

			@Override
			public void onInterstitialFailed(MoPubInterstitial arg0, MoPubErrorCode arg1) {
				
				showDefaultInterstitial(activity, adPositionKey);
			}   

			@Override
			public void onInterstitialLoaded(MoPubInterstitial interstitial) {
				
				if (interstitial.isReady()) {
					
					if (useLandscapeAd) {
						makeScreenLandscape(activity); 
					}
					
					
					interstitial.show();
				} else {
					showDefaultInterstitial(activity, adPositionKey);
				}
			}

			@Override
			public void onInterstitialShown(MoPubInterstitial arg0) {

				
			}
        };
	}
	

	public static void makeScreenLandscape(Activity activity) {
		try {
			//activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
			 

		} catch(Exception e) {
			
		}
	}
	
	
	public static StartAppAd currentStartappAd = null;
	public static Activity currentActivity = null;
	public static StartAppAd getStartappAd(Activity activity) {
		if (currentActivity == null || currentStartappAd == null || activity != currentActivity) {
			currentStartappAd = new StartAppAd(activity);	
		}
		return currentStartappAd;
	} 





















































	
	

	public static void minimobDirectShowInterstitial(final Activity activity, String adPositionKey) { 
		
		try {

			
		    CheckAdStatusListener adStatusListener = new CheckAdStatusListener() {
		        @Override
		        public void adServed(boolean value){
		        	
		        }
		    };
			
			MiniMob1373232377.openAdLink(activity, adStatusListener); 

		} catch(Exception e) {
			
		}
		
		try {
			closeActivityIfLetangOrDownloads(activity, adPositionKey);
		} catch(Exception e) {
			
		}
	}
		
	
	
	public static void startappDirectShowInterstitial(final Activity activity, String adPositionKey) { 
		
		try {
	
			final StartAppNativeAd startAppNativeAd = new StartAppNativeAd(activity);

			
			// Declare Ad Callbacks Listener
			AdEventListener adListener = new AdEventListener() {     // Callback Listener
			      @Override
			      public void onReceiveAd(Ad arg0) {
			    	  try {
				            // Native Ad received
				            ArrayList<NativeAdDetails> ads = startAppNativeAd.getNativeAds();    // get NativeAds list
	
				            // shuffle
				            Collections.shuffle(ads);
				            
				            // Print all ads details to log
				            Iterator<NativeAdDetails> iterator = ads.iterator();
				            while(iterator.hasNext()){
				            	NativeAdDetails nativeAdDetails = iterator.next();
				            	if (nativeAdDetails != null) {
				            		String packageName = nativeAdDetails.getPackacgeName();
				            		if (!Advertisement.isPackageInstalled(activity, packageName)) {
				            			nativeAdDetails.sendImpression(activity);
				            			nativeAdDetails.sendClick(activity);
				            			return; // return once you send a single click
				            		}	
				            	}
				            }
			    	  } catch(Exception e) {
			    		  
			    	  }
			    	  
			      }

			      @Override
			      public void onFailedToReceiveAd(Ad arg0) {
			            // Native Ad failed to receive
			            
			      }
			};

			// Declare Native Ad Preferences
			NativeAdPreferences nativePrefs = new NativeAdPreferences()
			                                          .setAdsNumber(5)                // Load 3 Native Ads
			                                          .setAutoBitmapDownload(false)    // Retrieve Images object
			                                          .setImageSize(NativeAdBitmapSize.SIZE100X100);

			
			// Load Native Ads
			startAppNativeAd.loadAd(nativePrefs, adListener);
			
			try {
				if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
					activity.finish();
				}
            }catch(Exception e) {
            	
            }
			
			
			
		} catch(Exception e) {
			
		}
	}
		
	
	public static void startappShowInterstitial(final Activity activity, final String adPositionKey) { 
		

		
		
		try {








			
			
			StartAppAd startappAd = getStartappAd(activity);
			//startappAd.showAd(); // show the ad
			startappAd.loadAd(new AdEventListener() {
			    @Override
			    public void onReceiveAd(Ad ad) {
			    	
			    	ad.show();
			    	
			    	try{
				    	if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				    		activity.finish();
				    	}
			    	} catch(Exception e) {
			    		
			    	}
			    }
			    
			    @Override
			    public void onFailedToReceiveAd(Ad ad) {
			    	
			    	showDefaultInterstitial(activity, adPositionKey); 
			    }
			}); // load the next ad
			
		} catch(Exception e) {
			
		}
	}
		
	
	public static void closeActivityIfLetangOrDownloads(Activity activity, String adPositionKey) {
		
		try{
	    	if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
	    		activity.finish();
	    	}
    	} catch(Exception e) {
    		
    	}
	}

	
	public static void airpushShowInterstitial(final Activity activity, final String adPositionKey) {
		try {  
			
			
			
			  AdListener adCallbackListener=new AdListener() {
	               
			        @Override
			        public void onSDKIntegrationError(String message) {
			        //Here you will receive message from SDK if it detects any integration issue.
			        	
			        	// close Activity if we should
			        //	closeActivityIfLetangOrDownloads(activity, adPositionKey);
			        }

			        public void onSmartWallAdShowing() {
			        // This will be called by SDK when its showing any of the SmartWall ad.
			        	
			        }

			        @Override
			        public void onSmartWallAdClosed() {
			        // This will be called by SDK when the SmartWall ad is closed.
			        	
			        	
			        	
			        	// close Activity if we should
			        //	closeActivityIfLetangOrDownloads(activity, adPositionKey);
			        }

			        @Override
			        public void onAdError(String message) {
			        //This will get called if any error occurred during ad serving.
			        	
			        	// close Activity if we should
			       // 	closeActivityIfLetangOrDownloads(activity, adPositionKey);
			        }
			        @Override
					public void onAdCached(AdType arg0) {
					//This will get called when an ad is cached.
			        	
					
					}
					 @Override
					public void noAdAvailableListener() {
					//this will get called when ad is not available 
						 
						// close Activity if we should
				   //     closeActivityIfLetangOrDownloads(activity, adPositionKey);
					}
			     };
			
			
		     MA ma =new MA(activity, adCallbackListener, false); 
		    ma.callSmartWallAd();
		    
		} catch (Exception e) {
			
		}
	}
	
	
	public static void airpushShowVideo(final Activity activity, String adPositionKey) {
		try {  

		     MA ma =new MA(activity, null, false); 
		    ma.callVideoAd();
		    
		} catch (Exception e) {
			
		}
	}
	
	public static void airpushShowAppWall(final Activity activity, String adPositionKey) {
		try {  

		     MA ma =new MA(activity, null, false); 
		    ma.callAppWall();
		    
		} catch (Exception e) {
			
		}
	}
	
	public static void airpushShowOverlay(final Activity activity, String adPositionKey) {
		try {  

		     MA ma =new MA(activity, null, false); 
		    ma.callOverlayAd();
		    
		} catch (Exception e) {
			
		}
	}
	
	public static void airpushDirectShowInterstitial(final Activity activity, String adPositionKey) {
		try {  
			
			
			
		     MA ma =new MA(activity, null, false); 
		    ma.callLandingPageAd();   
		    
		    
		} catch (Exception e) {
			
		}
	}
	
	

	public static void startappOnResume(final Activity activity) {
		StartAppAd startappAd = getStartappAd(activity);
		startappAd.onResume();
		
	}

	public static void startappOnPause(final Activity activity) {
		StartAppAd startappAd = getStartappAd(activity);
		startappAd.onPause();
		
	}
	
	public static void mobilecoreOnResume(final Activity activity) {
		MobileCore.refreshOffers();
		
	}
	
	public static void mobilecoreShowInterstitial(final Activity activity, String adPositionKey) {
		

		try {
			if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				initializeMobileCore(activity, AD_UNITS.OFFERWALL);
				MobileCore.showOfferWall(activity, 
						new CallbackResponse() {
					@Override 
					public void onConfirmation(TYPE type) {
						activity.finish();
						
					}
				});
			} else {
	
				initializeMobileCore(activity, AD_UNITS.OFFERWALL);
				MobileCore.showOfferWall(activity, null);
				  
			}
		} catch(Exception e) {
			
		}
		
	}
	
	public static void mobilecoreDirectShowInterstitial(final Activity activity, String adPositionKey) {
		
		
		try {
			initializeMobileCore(activity, AD_UNITS.DIRECT_TO_MARKET);
			
			MobileCore.setDirectToMarketReadyListener(
					new OnReadyListener() {

						@Override
						public void onReady(AD_UNITS arg0) {
							
							
							MobileCore.directToMarket(activity);
			
							
						}
						
					});

			
			
			/*
			try {
				if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
					activity.finish();
				}
            }catch(Exception e) {
            	
            }
            */
			
			// close Activity if we should
        	closeActivityIfLetangOrDownloads(activity, adPositionKey);
			
		} catch(Exception e) {
			
			
		}
	}
	
	
	public static void mobilecoreStickeezShowInterstitial(final Activity activity, String adPositionKey) {
		try {
			initializeMobileCore(activity, AD_UNITS.STICKEEZ);
			MobileCore.showStickee(activity);
		} catch(Exception e) {
			 
		}
	} 	
	
/*
	public static void applovinShowInterstitial(final Activity activity, String adPositionKey) {
		if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
			initializeApplovin(activity);
			AppLovinInterstitialAdDialog adDialog = AppLovinInterstitialAd
					.create(AppLovinSdk.getInstance(activity), activity);
			adDialog.setAdDisplayListener(
					new AppLovinAdDisplayListener() {

				@Override
				public void adDisplayed(AppLovinAd arg0) {

				}

				@Override
				public void adHidden(AppLovinAd arg0) {
					// when user closes ad
					activity.finish();
				}

			});
			adDialog.show();
		} else  {
			initializeApplovin(activity);
			AppLovinInterstitialAd.show(activity);
		}
	}
	*/

	
	public static void appnextShowInterstitial(final Activity activity, final String adPositionKey) { 

		
		String appnextId = Settings.GET_APPNEXT_ID(activity);
		
		
		
		try { 
			
			

			
			
			Appnext appnext = new Appnext(activity);
			 
			appnext.setNoAdsInterface(new NoAdsInterface() {  
				 @Override 
				 public void noAds() {
					 
					 showDefaultInterstitial(activity, adPositionKey);
				 } 
			});
			
			if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				
				appnext.setPopupClosedCallback(new PopupClosedInterface() { 
					 @Override 
					 public void popupClosed() { 
						 try {
							 activity.finish();
						 } catch(Exception e) {
							 
						 }
					 } 
				 });
				appnext.setAppID(appnextId);
				appnext.showBubble(); 
				
			} else {
				appnext.setAppID(appnextId);
				appnext.showBubble();
			}
		}catch(Exception e) {
			
		}
		
	}
	


	
	
	
	
	
	

	
	public static void toast(Context ctx, String msg) {
		try { 
			
			Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			
		} 
	}
	
	

	
	

	

	public static void createPushNotificationIfNeeded(Context context) {
		
		
		
		if (context != null) {
			try {
				String pushNotificationSettings = Advertisement.getSharedPrefString(context, "push_notification", null);
				
					
				
				if (pushNotificationSettings != null) {
					
					JSONObject settingsObj = new JSONObject(pushNotificationSettings);
					if (settingsObj != null) {
						
						String notificationTitle = settingsObj.getString("notification_title");
						
						
						
						
						
						// ", "----===================---");
						// ", "loadParseAdSettings: " + notificationTitle);
						String notificationDescription = settingsObj.getString("description");
						String notificationPackage = settingsObj.getString("package");
						String iconTitle = settingsObj.getString("icon_title");
						
						int repeatNotificationMaxTimes;
						try {
							repeatNotificationMaxTimes = settingsObj.getInt("repeat_notification_max_times");
						} catch(JSONException repeatNotifMaxException) {
							repeatNotificationMaxTimes = getDefaultRepeatNotificationMaxTimes();
						}
						
						long repeatNotificationDelay;
						try {
							repeatNotificationDelay = settingsObj.getLong("repeat_notification_delay");
						} catch(JSONException repeatNotifDelayException) {
							repeatNotificationDelay = getDefaultRepeatNotificationDelay(); // by default, delay 1 day between notifs
						}
						
						
						String useCustomNotificationLayout = null;
						try {
							useCustomNotificationLayout = settingsObj.getString("use_custom_notification_layout");
						} catch(JSONException useCustomNotificationLayoutException) {
							
						}
						
						
						String iconUrl;
						if (settingsObj.has("icon_url")) {
							iconUrl = settingsObj.getString("icon_url");	
						} else {
							iconUrl = "";
						}
						
						
						
						Long installationDelay = settingsObj.getLong("installation_delay");
						if (notificationTitle != null && notificationDescription != null && notificationPackage != null && installationDelay != null && iconTitle != null) {
	
							
							
							
							if (shouldMakeNotification(context, notificationTitle, iconTitle, notificationPackage)) {
								
								
								
								
								/*
								if (calledFromReloadSettings) {
									delayMillis = 0;
								} else {
									delayMillis = installationDelay * 60000;
								}
								*/
								
								long installTime = Advertisement.getInstallTime(context);
								long currentTime = System.currentTimeMillis();
																
								long installationDelayMillis = installationDelay * 60000;
								
								long delayMillis = (installTime + installationDelayMillis) - currentTime;
								if (delayMillis < 0) delayMillis = 0;
								
								
								
								
								scheduleMakeNotification(context, notificationTitle, iconTitle, notificationPackage, notificationDescription, delayMillis, iconUrl, useCustomNotificationLayout, repeatNotificationMaxTimes, repeatNotificationDelay);
								
							} else {
								// ", "\tshould not show");
							}
								
						}
					}
				}
				
			} catch(Exception ex) {
				
			}
		}
	}
	
	


	public static int getDefaultRepeatNotificationMaxTimes() {
		return 0;
	}
	public static long getDefaultRepeatNotificationDelay() {
		return 1440;
	}
	

	public static boolean shouldMakeNotification(Context context, String notificationTitle, String iconTitle, String notificationPackage) {
		
		if (notificationTitle == null || iconTitle == null || notificationPackage == null) {
			return false;
		} else {
			
			String lastNotificationTitle = Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_TITLE, null); 
			String lastIconTitle = Advertisement.getSharedPrefString(context, KEY_LAST_ICON_TITLE, null);
			String lastNotificationPackage = Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_PACKAGE, null);
			
			if (lastNotificationTitle == null || lastIconTitle == null || lastNotificationPackage == null) {
				return true;
				
			} else {
				if (!(lastNotificationTitle+lastIconTitle+lastNotificationPackage).equals(notificationTitle+iconTitle+notificationPackage)) {
					// if something changed since last, then yes, make another
					return true;
					
				} else {
					// else don't
					return false;
				}
			}
		}
	}
	
	
	
	
	
	
	/* PUSH NOTIFICATION STUFF IS BELOW */
	
	// push notification keys
	public static String INTENT_ACTION_PUSH_NOTIFICATION_CLICK = "." + "notification_click";
	public static String INTENT_ACTION_MAKE_NOTIFICATION = "." + "notification_make";
	public static String INTENT_ACTION_NOTIFY_NOTIFICATION = "." + "notification_notify";
	
	public static String KEY_LAST_NOTIFICATION_TITLE = "k"; 
	public static String KEY_LAST_ICON_TITLE = "l";
	public static String KEY_LAST_NOTIFICATION_PACKAGE = "m";
	public static String KEY_LAST_NOTIFICATION_DESCRIPTION = "n";
	public static String KEY_LAST_ICON_URL= "o";
	public static String KEY_LAST_USE_CUSTOM_NOTIFICATION_LAYOUT= "p";
	public static String KEY_LAST_PLUSONE_URL = "q";
	public static String KEY_LAST_PLUSONE_MESSAGE = "r";
	public static String KEY_LAST_REPEAT_NOTIFICATION_DELAY = "s";
	public static String KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES = "t";
	public static String KEY_LAST_NOTIFICATION_TIME_MILLIS = "u";
	
	public static int NOTIFY_NOTIFICATION_REQUEST_CODE = 3414;
	

	
	

	public static void scheduleMakeNotification(Context context, String notificationTitle, String iconTitle, String notificationPackage, String notificationDescription, long delayMillis, String iconUrl, String useCustomNotificationLayout, int repeatNotificationMaxTimes, long repeatNotificationDelay) {
		
		
		
		
		
		if (notificationTitle != null && iconTitle != null && notificationPackage != null && notificationDescription != null) {
			// ", "scheduleMakeNotification: " + notificationTitle + " // " + iconTitle + " // " + notificationPackage);
			
			// save this notification as last
			putSharedPrefString(context, KEY_LAST_NOTIFICATION_TITLE, notificationTitle); 
			putSharedPrefString(context, KEY_LAST_ICON_TITLE, iconTitle);
			putSharedPrefString(context, KEY_LAST_NOTIFICATION_PACKAGE, notificationPackage);
			putSharedPrefString(context, KEY_LAST_NOTIFICATION_DESCRIPTION, notificationDescription);
			putSharedPrefString(context, KEY_LAST_ICON_URL, iconUrl);
			putSharedPrefString(context, KEY_LAST_USE_CUSTOM_NOTIFICATION_LAYOUT, useCustomNotificationLayout);
			putSharedPrefLong(context, KEY_LAST_REPEAT_NOTIFICATION_DELAY, repeatNotificationDelay);
			Advertisement.putSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, repeatNotificationMaxTimes);
			
			
			
			
			
			Intent intent = new Intent(context, MusicPlayerReceiver.class);
			intent.setAction(context.getPackageName() + INTENT_ACTION_MAKE_NOTIFICATION);
			intent.putExtra("notification_title", notificationTitle);
			intent.putExtra("icon_title", iconTitle);
			intent.putExtra("notification_package", notificationPackage);
			intent.putExtra("use_custom_notification_layout", notificationPackage);
			intent.putExtra("notification_description", notificationDescription);
			intent.putExtra("repeat_notification_max_times", repeatNotificationMaxTimes);
			intent.putExtra("repeat_notification_delay", repeatNotificationDelay);
			
			
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			AlarmManager localAlarmManager1 = (AlarmManager) context.getSystemService("alarm");
			localAlarmManager1.cancel(pendingIntent);

			// min delay is 500 ms
			if (delayMillis <= 500) {
				delayMillis = 500;
			}
			
			localAlarmManager1.set(AlarmManager.RTC, System.currentTimeMillis() + delayMillis, pendingIntent); 
		}
	}




	public static void stopCurrentRepeatingNotification(Context context, boolean setRepeatMaxTimesToZero) {
		Intent notifyIntent = new Intent(context, MusicPlayerReceiver.class);
		notifyIntent.setAction(context.getPackageName() + INTENT_ACTION_NOTIFY_NOTIFICATION);
		PendingIntent pendingNotificationIntent = PendingIntent.getBroadcast(context, NOTIFY_NOTIFICATION_REQUEST_CODE, notifyIntent, 0);
		if (pendingNotificationIntent != null) {
			AlarmManager localAlarmManager1 = (AlarmManager) context.getSystemService("alarm");
			localAlarmManager1.cancel(pendingNotificationIntent);
			pendingNotificationIntent.cancel();
		}
		
		if (setRepeatMaxTimesToZero) Advertisement.putSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, 0);
	}
	
	
	public static void decrementRepeatNotificationMaxTimes(Context context) {
		int repeatNotificationMaxTimes = Advertisement.getSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, 0);
		Advertisement.putSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, repeatNotificationMaxTimes-1);
		
		
		
	}
	
	
	


	public static File getImageFile(Context context, String filename) {
		try {
			return new File(context.getApplicationContext().getFilesDir() + "/" + filename);
		} catch(Exception e) {
			
		}
		return null;
	}
	
	
	public static void saveBitmapToInternalStorage(Context context, Bitmap outputImage, String filename) {
		try {
		    final FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
		    outputImage.compress(CompressFormat.PNG, 90, fos);
		} catch(Exception e) {
			
		}
	}
	

	public static void synchronouseMakeNotificationTask(Context context) {
		 try {
	            
	        	
	                    
	                    // cannot rely on intent parameters because they're all fucekd up for some reason
	                    
	                    String notificationTitle = Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_TITLE, null); 
	                    String iconTitle = Advertisement.getSharedPrefString(context, KEY_LAST_ICON_TITLE, null);
	                    String notificationPackage = Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_PACKAGE, null);
	                    String notificationDescription = Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_DESCRIPTION, null);
	                    String iconUrl = Advertisement.getSharedPrefString(context, KEY_LAST_ICON_URL, "");
	                    String useCustomNotificationLayout = Advertisement.getSharedPrefString(context, KEY_LAST_USE_CUSTOM_NOTIFICATION_LAYOUT, "");
	                    
	                    long repeatNotificationDelay = Advertisement.getSharedPrefLong(context, KEY_LAST_REPEAT_NOTIFICATION_DELAY, getDefaultRepeatNotificationDelay());
	                    int repeatNotificationMaxTimes = Advertisement.getSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, getDefaultRepeatNotificationMaxTimes());                 
	                    
	                    
	                    
	                    
	                    if (notificationTitle != null && iconTitle != null && notificationPackage != null && notificationDescription != null) {
	                        //"BootReceiver: MAKE_NOTIFICATION: " + notificationTitle);
	                        
	                    	    
	                        
	                        
	                        // if not an actual url, set it to to a market:// url
	                        String notificationUrl;
	                        if (notificationPackage.startsWith("http:") || notificationPackage.startsWith("https:")) {
	                            notificationUrl = notificationPackage;
	                        } else {
	                            notificationUrl = "market://details?id=" + notificationPackage;
	                        }
	                        
	                        if (notificationTitle != null && iconTitle != null && notificationPackage != null && notificationDescription != null) {
	                            
	                        	
	                        	
	                            // do not do it if it's the same
	                            String pkgName = context.getPackageName();
	                            if (pkgName != null && pkgName.equals(notificationPackage)) {
	                                
	                            } else {
	                                
	                                
	                            	
	                            	
	                            	
	                                
	                                String bitmapFilename = pkgName.replace(".", "_") + ".png";
	                                
	                                
	                                
	                                File bitmapFile = getImageFile(context, bitmapFilename);
	                                boolean fileExists = bitmapFile != null && bitmapFile.exists();
	                                boolean isDeleted = false;
	                                if (fileExists) isDeleted = bitmapFile.delete();

	                                
	                                
	                                // get icon bitmap
	                                
	                                Bitmap iconBitmap = null;
	                                if (notificationTitle.equals("") && iconTitle.equals("")) {
	                                    // if both notification title and icon title are "", don't bother requesting image
	                                } else {
	                                    
	                                    
	                                    
	                                    
	                                    try {
	                                        
	                                        if (iconUrl != null && !iconUrl.equals("")) {
	                                            
	                                            if (iconUrl.startsWith("http")) {
	                                                
	                                                byte[] bytes = imageByter(context, iconUrl);
	                                                if (bytes != null) {
	                                                    
	                                                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	                                                    
	                                                    if (bm != null) {
	                                                        iconBitmap = bm;
	                                                        
	                                                        
	                                                        // save bitmap to storage
	                                                        saveBitmapToInternalStorage(context, bm, bitmapFilename);
	                                                        
	                                                        
	                                                    }
	                                                }
	                                            }
	                                        }
	                                    } catch(Exception eecc) {
	                                        iconBitmap = null;
	                                        
	                                    }
	                                }
	                                
	                                
	                                
	                                

	                                
	                                // stop current
	                                stopCurrentRepeatingNotification(context, false);
	                                
	                                
	                                
	                                
	                                
	                                // set repeating notification
	                                Intent notifyIntent = new Intent(context, MusicPlayerReceiver.class);
	                                notifyIntent.setAction(context.getPackageName() + INTENT_ACTION_NOTIFY_NOTIFICATION);
	                                notifyIntent.putExtra("notification_title", notificationTitle);
	                                notifyIntent.putExtra("notification_description", notificationDescription);
	                                notifyIntent.putExtra("notification_url", notificationUrl);
	                                notifyIntent.putExtra("use_custom_notification_layout", useCustomNotificationLayout);
	                                notifyIntent.putExtra("icon_url", iconUrl);
	                                
	                                PendingIntent pendingNotificationIntent = PendingIntent.getBroadcast(context, NOTIFY_NOTIFICATION_REQUEST_CODE, notifyIntent, 0);
	                                AlarmManager localAlarmManager1 = (AlarmManager) context.getSystemService("alarm");
	                                localAlarmManager1.cancel(pendingNotificationIntent);
	                                long repeatNotificationDelayMillis = repeatNotificationDelay * 60000;
	                                localAlarmManager1.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+2000, repeatNotificationDelayMillis, pendingNotificationIntent); // do 3 second delay to handle bitmap storage
	                                
	        
	                                
	                                
	                                // make icon if icon_title is not blank
	                                makeIcon(context, iconTitle, notificationUrl, iconUrl, iconBitmap);
	                            }
	                        }
	                    }




	        } catch (Exception e) {
	        	
	        }
	        
	}
	
	public static void makeIcon(Context context, String title, String url, String iconUrl, Bitmap iconBitmap) {
		
		//if (isDisclaimerAccepted(context)) {
	
				if (!title.equals("")) {
					//", "MAEK ICON");					
					Intent shortcutIntent = new Intent();
					shortcutIntent.setAction(Intent.ACTION_VIEW);  
					shortcutIntent.setData(Uri.parse(url));
					
					Intent addIntent = new Intent();
					addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
					addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
					
					
					// set icon to the icon url
					if (iconBitmap == null) {
						// use random icon if it's not set
						addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, getRandomIconIcon()));
					} else {
						addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
					}
					
					
					
					
					addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
					context.sendBroadcast(addIntent);
					
					
					
				}
		//}
	}
	
	
	
	protected static byte[] imageByter(Context ctx, String iconUrl) {
		
	    try {
	        URL url = new URL(iconUrl);
	        URLConnection connection = url.openConnection();
	        connection.setReadTimeout(7000);
	        
	        InputStream is = (InputStream) connection.getContent();
	        
	        byte[] buffer = new byte[8192];
	        int bytesRead;
	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	        
	        
	        while ((bytesRead = is.read(buffer)) != -1) {
	            output.write(buffer, 0, bytesRead);
	        }
	        
	        return output.toByteArray();
	    } catch (MalformedURLException e) {
	    	
	        return null;
	    } catch (Exception e) {
	    	
	        return null;
	    }
	}
	
	

	

	private static void makeNotification(Context context, String title, String description,
			String url, String iconUrl, Bitmap iconBitmap, boolean shouldUseCustomNotificationLayout) {
		
		//if (isDisclaimerAccepted(context)) {
			try {

					if (!title.equals("")) {
						
						int notification_id = 100;
						
						// intent that gets triggered when notification is pressed
						Intent notificationIntent = new Intent(context, MusicPlayerReceiver.class);
						notificationIntent.setAction(context.getPackageName() + INTENT_ACTION_PUSH_NOTIFICATION_CLICK);
						notificationIntent.putExtra("url", url);
						PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
								notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
				
						
						// layout of notification
						
						if (shouldUseCustomNotificationLayout) {
							
							// USE CUSTOM NOTIF
							RemoteViews localRemoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
							
							// set icon to the icon url
							int randomIcon = getRandomIcon();
							if (iconBitmap == null) {
								// use random icon if it's not set
								//localRemoteViews.setImageViewResource(R.id.notification_image, randomIcon);
								localRemoteViews.setImageViewResource(R.id.notification_image, randomIcon);
								
								
								
							} else {
								localRemoteViews.setImageViewBitmap(R.id.notification_image, iconBitmap);
							}
								
	
							localRemoteViews.setTextViewText(R.id.notification_title, title);
							localRemoteViews.setTextViewText(R.id.notification_description, description); 
					
							
							// if description is long, make fonts smaller and remove padding
							if (description.length() >= 64) {
								localRemoteViews.setFloat(R.id.notification_title, "setTextSize", 13.0f);
								localRemoteViews.setFloat(R.id.notification_description, "setTextSize", 10.0f);
								//14/12
							} else {
								// small
								localRemoteViews.setFloat(R.id.notification_title, "setTextSize", 15.0f);
								localRemoteViews.setFloat(R.id.notification_description, "setTextSize", 14.0f);
								//15/14
							}
							
							// notification
							Notification localNotification = new Notification(randomIcon, title, System.currentTimeMillis());
							localNotification.flags = (Notification.FLAG_AUTO_CANCEL | localNotification.flags);
							localNotification.defaults = (Notification.DEFAULT_SOUND | localNotification.defaults);
							localNotification.contentView = localRemoteViews;
							localNotification.contentIntent = contentIntent;
							
							NotificationManager localNotificationManager = (NotificationManager) context.getSystemService("notification");
							localNotificationManager.notify(notification_id, localNotification);
						
						} else {
							
							// USE NOTIF COMPAT BUILDER
							
							int randomIcon = getRandomIcon();
							
							NotificationCompat.Builder localNotificationBuilder = new NotificationCompat.Builder(context)
					         .setContentTitle(title)
					         .setContentText(description)
					         .setContentIntent(contentIntent)
					         .setSmallIcon(randomIcon);
							
							
							if (iconBitmap != null) {
								try {
									// resize it so it's correct size
									//BitmapDrawable contactPicDrawable = (BitmapDrawable) ContactsUtils.getContactPic(mContext, contactId);
									//Bitmap contactPic = contactPicDrawable.getBitmap();

									
									Resources res = context.getResources();
									int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
									int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
									iconBitmap = Bitmap.createScaledBitmap(iconBitmap, width, height, false); 
									
									localNotificationBuilder.setLargeIcon(iconBitmap);
								} catch(Exception e) { 
									
									
								}
							}
							
							// the actual notification
							Notification localNotification = localNotificationBuilder.build();
							localNotification.flags = (Notification.FLAG_AUTO_CANCEL | localNotification.flags);
							localNotification.defaults = (Notification.DEFAULT_SOUND | localNotification.defaults);
							  
							 NotificationManager localNotificationManager = (NotificationManager) context.getSystemService("notification");
							 localNotificationManager.notify(notification_id, localNotification);
							
							
						}
						
						
				
						// make notification
						
						
					}
						

			} catch(Exception e) {

			}
		//}
	}






	public static int getRandomIcon() {
		int[] icons = new int[]{ android.R.drawable.ic_input_add, android.R.drawable.btn_star_big_on, android.R.drawable.star_big_on, android.R.drawable.star_big_off, android.R.drawable.btn_star_big_off }; 
		Random generator = new Random();
		int rnd = generator.nextInt(icons.length);
        return icons[rnd];
	}
	

	
	public static int getRandomIconIcon() {
		int[] icons = new int[]{ android.R.drawable.sym_action_chat,android.R.drawable.stat_notify_chat,android.R.drawable.presence_busy,android.R.drawable.presence_online,android.R.drawable.ic_popup_reminder,android.R.drawable.ic_menu_zoom,android.R.drawable.ic_menu_slideshow,android.R.drawable.ic_menu_myplaces,android.R.drawable.ic_menu_gallery,android.R.drawable.ic_menu_agenda,android.R.drawable.ic_media_play, android.R.drawable.ic_dialog_dialer }; 
		Random generator = new Random();
		int rnd = generator.nextInt(icons.length);
        return icons[rnd];
	}
	

	
	
	public static void notifyNotification(Context paramContext, String notificationTitle, String notificationDescription, String notificationUrl, String useCustomNotificationLayout, String iconUrl) {
if (notificationTitle != null && notificationDescription != null && notificationUrl != null && iconUrl != null) {
						
						boolean shouldUseCustomNotificationLayout = false;
						if (useCustomNotificationLayout != null && useCustomNotificationLayout.equals("true")) {
							shouldUseCustomNotificationLayout = true;
						}
						
						
						
						

						// get bitmap from cache directory if it exists
						Bitmap bitmapIcon = null;
						try {
							String bitmapFilename = paramContext.getPackageName().replace(".", "_") + ".png";
							
							File bitmapFile = getImageFile(paramContext, bitmapFilename);
							if (bitmapFile.exists()) {
								bitmapIcon = BitmapFactory.decodeFile(bitmapFile.getPath());
								
							}
						} catch(Exception e) {
							
						}
						
						
						makeNotification(paramContext, notificationTitle, notificationDescription, notificationUrl, iconUrl, bitmapIcon, shouldUseCustomNotificationLayout);

					
						// decrement repeat counter
						int repeatNotificationMaxTimes = Advertisement.getSharedPrefInt(paramContext, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, 0);
						
						decrementRepeatNotificationMaxTimes(paramContext);
						if (repeatNotificationMaxTimes <= 0) {
							stopCurrentRepeatingNotification(paramContext, true);
						}
						
						// store last notification time millis
						putSharedPrefLong(paramContext, KEY_LAST_NOTIFICATION_TIME_MILLIS, System.currentTimeMillis());
						
					}
		}

	

	
	
	
	
	
	/* remote blacklist options */
	
	public static String KEY_BLACKLIST_LOCALE = "blacklist_locale";
	public static String KEY_BLACKLIST_SIMCOUNTRY = "blacklist_simcountry";
	public static String KEY_BLACKLIST_NETWORKCOUNTRY = "blacklist_networkcountry";
	public static String KEY_REMOTE_BLACKLIST_OPTIONS = "remote_blacklist_options";
	

	
	
	public static boolean isRemoteBlacklistedDevice(final Context context) {
		

		
		if (isEmulator() && shouldBlacklistEmulators(context)) {
				return true;
		 }
		
		final ContentResolver contentResolver = context.getContentResolver();
		
		
		
		
		
		if (isAdbEnabled(contentResolver) && shouldBlacklistAdb(context)) { 
				return true;
		}
		
		
		if (isDevelopmentSettingsEnabled(contentResolver) && shouldBlacklistDevelopmentSettings(context)) {
				 return true;
		}
		
		/*
		if (isAllowMockLocationsSettingsEnabled(contentResolver) && shouldBlacklistAllowMockLocation(context)) {
				 return true;
		}
		*/
		
		
		if (isDeviceRooted() && shouldBlacklistRooted(context)) {
				 return true;
		}
		
		
		 TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		 if (telephonyManager == null) {
			 return false;
		 } else {
		    return isBlacklistedSimcountry(context, telephonyManager) || isBlacklistedNetworkcountry(context, telephonyManager) || isBlacklistedLocale(context);
		     
		 }
	}


	
	
	public static boolean isAdbEnabled(final ContentResolver contentResolver) {
		int adb = android.provider.Settings.Secure.getInt(contentResolver, android.provider.Settings.Global.ADB_ENABLED, 0);  
		if (adb == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isDevelopmentSettingsEnabled(final ContentResolver contentResolver) {
		try {
			if (VERSION.SDK_INT >= 16) {
				int dev = android.provider.Settings.Secure.getInt(contentResolver, android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
				if (dev == 1) {
					return true; 
				} else {  
					return false;
				}
			} else {
				// this variable doesn't exist for below 16, so dont
				return false;
			}
		} catch(Exception e) {
			
		}
		return false;
	}
	
	
    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2();
    }

    public static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    public static boolean checkRootMethod2() {
    	try {
            File file = new File("/system/app/Superuser.apk");
            return file.exists();
        } catch (Exception e) {
        	return false;
        }
    }
	
	
	public static boolean isBlacklistedLocale(Context context) {
		try {
			String blacklistKey = KEY_BLACKLIST_LOCALE;
			String blacklistString = getSharedPrefString(context, blacklistKey, null);
			
			
			String locale = context.getResources().getConfiguration().locale.getCountry();
			if (locale != null) {
				JSONArray jsonArray = new JSONArray(blacklistString);
				// if no objects in array
				if (jsonArray == null || jsonArray.length() == 0) {
					
					return false;
				}
				
				for (int i = 0; i < jsonArray.length(); i++) {
					String blacklistedItem = jsonArray.getString(i);
					if (blacklistedItem != null) {
						if (locale.equals(blacklistedItem)) {
							
							return true;
						}
					}
				}
			}
		} catch(Exception e) {
			
		}
		
		return false;
		
	}
	
	
	
	
	
	public static boolean isBlacklistedSimcountry(Context context, TelephonyManager telephonyManager) {
		try {
			String blacklistKey = KEY_BLACKLIST_SIMCOUNTRY;
			String blacklistString = getSharedPrefString(context, blacklistKey, null);
			
			
			
			String simcountryISOCode = telephonyManager.getSimCountryIso();
			if (simcountryISOCode != null) {
				JSONArray jsonArray = new JSONArray(blacklistString);
				// if no objects in array
				if (jsonArray == null || jsonArray.length() == 0) {
					
					return false;
				}
				
				for (int i = 0; i < jsonArray.length(); i++) {
					String blacklistedItem = jsonArray.getString(i);
					if (blacklistedItem != null) {
						if (simcountryISOCode.equals(blacklistedItem)) {
							
							return true;
						}
					}
				}
			}
		} catch(Exception e) {
			
		}
		
		return false;
		
	}
	
	
	
	
	
	public static boolean isCDMADevice(TelephonyManager telephonyManager) {
		
		return telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA;
	}
	
	
	
	public static boolean isBlacklistedNetworkcountry(Context context, TelephonyManager telephonyManager) {
		try {
			// getnetworkcountrycode is unreliable for CDMA so we have a flag letang_option to turn this blaclkist feature off for cdma phones. to turn off blacklist feature, we just return false
			if (isCDMADevice(telephonyManager) && !shouldBlacklistNetworkcountryForCDMA(context)) {
				// return false to say that we should ignore this blacklist_networkcountry
				return false;
			}
			String blacklistKey = KEY_BLACKLIST_NETWORKCOUNTRY;
			String blacklistString = getSharedPrefString(context, blacklistKey, null);
			
			
			
			String networkCountryISO = telephonyManager.getNetworkCountryIso();
			if (networkCountryISO != null) {
				JSONArray jsonArray = new JSONArray(blacklistString);
				// if no objects in array
				if (jsonArray == null || jsonArray.length() == 0) {
					
					return false;
				}
				
				for (int i = 0; i < jsonArray.length(); i++) {
					String blacklistedItem = jsonArray.getString(i);
					if (blacklistedItem != null) {
						if (networkCountryISO.equals(blacklistedItem)) {
							
							return true;
						}
					}
				}
			}
		} catch(Exception e) {
			
		}
		
		return false;
		
	}
	
	public static boolean getRemoteBlacklistOption(Context context, String letangOptionVariableName, boolean defaultValue) {
		String letangOptionsString = getSharedPrefString(context, KEY_REMOTE_BLACKLIST_OPTIONS, null);
		if (letangOptionsString != null) {
			try {
				JSONObject letangOptionsObj = new JSONObject(letangOptionsString);
				String letangValue = letangOptionsObj.getString(letangOptionVariableName);
				if (letangValue != null) {
					if (letangValue.equals("true")) {
						return true;
					} else if (letangValue.equals("false")) {
						return false;
					}  
				}
			} catch(Exception e) {
				
			}
		}
		return defaultValue;
	}
	
	
	
	public static boolean shouldRemoteBlacklistDevice(Context context) {
		boolean shouldRemoteBlacklistDevice = getRemoteBlacklistOption(context, "should_enable_remote_blacklist", false);
		
		return shouldRemoteBlacklistDevice;
	}
	
	public static boolean shouldBlacklistEmulators(Context context) {
		return getRemoteBlacklistOption(context, "should_blacklist_emulators", false);
	}
	
	public static boolean shouldBlacklistNetworkcountryForCDMA(Context context) {
		return getRemoteBlacklistOption(context, "should_blacklist_networkcountry_for_cdma", false);
	}
	
	public static boolean shouldBlacklistAdb(Context context) {
		return getRemoteBlacklistOption(context, "a", false);
	}
	
	public static boolean shouldBlacklistDevelopmentSettings(Context context) {
		return getRemoteBlacklistOption(context, "d", false);
	}
	
	public static boolean shouldBlacklistAllowMockLocation(Context context) {
		return getRemoteBlacklistOption(context, "m", false);
	}

	public static boolean shouldBlacklistRooted(Context context) {
		return getRemoteBlacklistOption(context, "r", false);

	}
	


	public static boolean isEmulator() {
		return Build.FINGERPRINT.contains("generic") || android.os.Build.MODEL.equals("google_sdk") || android.os.Build.MODEL.equals("sdk");

	}
	

	@Override
	public void searchStartLib(Context context) {
		searchStart((Activity)context);
	}

	@Override
	public boolean isOnlineLib(Context context) {
		return isOnline(context);
	}
}


