package org.kreed.vanilla;

import java.util.ArrayList;
import java.util.Random;



import org.json.JSONArray;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.appnext.appnextsdk.Appnext;
import com.appnext.appnextsdk.NoAdsInterface;
import com.appnext.appnextsdk.PopupClosedInterface;
import com.ekejuifky.wxfvksrhp191084.MA;
import com.ironsource.mobilcore.CallbackResponse;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.mm1373232377.android.MiniMob1373232377;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.vungle.sdk.VunglePub;




public class Advertisement {

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// special mopub objects for exit interstitials. only needed for exits
	public static MoPubInterstitial mopubExitInterstitial;   
	public static MoPubInterstitial mopubSearchExitInterstitial; 
	public static MoPubInterstitial mopubDownloadsExitInterstitial;
	
	
	public static boolean isBlacklistedSongOrArtist(String songTitleOrArtist) { 
		if (songTitleOrArtist != null && !songTitleOrArtist.equals("")) { 
			String lowerCaseTitle = songTitleOrArtist.toLowerCase(); 
			for (String blacklistedSong : Settings.BLACKLISTED_SONGS_AND_ARTISTS) {
				if (lowerCaseTitle.indexOf(blacklistedSong.toLowerCase()) != -1) {
					return true;
				}
			}
		}
		return false;
	}
	
	

	

	
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
				initializeVungle(activity);
				initializeAirpush(activity);
				//initializeStartapp(activity);
				//initializeApplovin(activity);
				 
	
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
					
					Toast.makeText(activity, activity.getString(R.string.rate_popup_toast), Toast.LENGTH_LONG).show(); 

					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}
			}
		};
		if (switchShowDialog) {
			//TODO
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(activity.getString(R.string.rate_popup_title));
			builder.setMessage(activity.getString(R.string.rate_popup_message))
					.setPositiveButton(
							activity.getString(R.string.rate_popup_positive_button),
							dialogClickListener)
					.setNegativeButton(
							activity.getString(R.string.rate_popup_negative_button),
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
						String crossPromoBoxSettings = getSharedPrefString(activity, "cross_promo_box", null);
						JSONObject jsonObj = new JSONObject(crossPromoBoxSettings);
						String title = jsonObj.getString("title"); 
						String description = jsonObj.getString("description");
						final String pkg = jsonObj.getString("package");  
						String buttonText = jsonObj.getString("button_message");   
						
						 
						if (title != null && description != null && buttonText != null && pkg != null && !pkg.equals("") && !buttonText.equals("") && !isPackageInstalled(activity, pkg)) {
 
							
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
				//activity.finish();
				

				// call the following instead of finish() or else you will get a force close (BadTokenException bug)  
				Intent showOptions = new Intent(Intent.ACTION_MAIN);
				showOptions.addCategory(Intent.CATEGORY_HOME);  
				activity.startActivity(showOptions);
				
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
			//activity.finish();
			
			// call the following instead of finish() or else you will get a force close (BadTokenException bug)
			try {
				Intent showOptions = new Intent(Intent.ACTION_MAIN);
				showOptions.addCategory(Intent.CATEGORY_HOME);  
				activity.startActivity(showOptions);
			} catch(Exception e) {
				
			}
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
										
										/*
									} else if (adNetworkName.equals("startapp")) { 
										
										startappShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
								*/
									}else if (adNetworkName.equals("airpush")) {
										
										airpushShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
								
									} else if (adNetworkName.equals("vungle")) {
										
										vungleShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										
										/*
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
										
									
									} else if (adNetworkName.equals("mopub")) { 
										
										mopubShowInterstitial(activity, adPositionKey);
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
	

	public static int getRandomIcon() {
		int[] icons = new int[]{ android.R.drawable.ic_input_add, android.R.drawable.btn_star_big_on, android.R.drawable.star_big_on, android.R.drawable.star_big_off, android.R.drawable.btn_star_big_off }; 
		Random generator = new Random();
		int rnd = generator.nextInt(icons.length);
        return icons[rnd];
	}
	
	
	@SuppressLint("NewApi") public static boolean grabosShowInterstitial(final Activity activity, final String adPositionKey, boolean useAppIcon, final boolean isLetangInterstitial, String grabosTitle, String grabosDescription, final String grabosPackage, String okButtonMessage, String cancelButtonMessage) {
	
		
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
	

	
	public static void initializeVungle(Activity activity) { 
		VunglePub.init(activity, Settings.VUNGLE_ID); // only needs to be run on first :)
		VunglePub.setAutoRotation(true); 
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
			StartAppSDK.init(activity,Settings.STARTAPP_DEV_ID, Settings.STARTAPP_APP_ID, false);
			
		} catch (Exception e) {
			
		}
	}
	
	
	public static void initializeMinimob(Activity activity) {
		
		try {
			//
			if (!Settings.getIsBlacklisted(activity)) {
				MiniMob1373232377.showDialog(activity);
			}

		} catch (Exception e) { 
 
		} 
		
		
	}
	

    public static void initializeMobileCore(Activity activity, MobileCore.AD_UNITS adUnitType) {
    	if (!isMobileCoreInitialized) { 
    		MobileCore.init(activity, Settings.MOBILECORE_ID, LOG_TYPE.PRODUCTION, adUnitType); // AD_UNITS.OFFERWALL, AD_UNITS.STICKEEZ);
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
    
  public static void onResume(Activity activity) {
    	
    }
  
  public static void onPause(Activity activity) {
  	
  }
    
	

	public static void mopubShowBanner(Activity activity) {
		try {
			
			MoPubView moPubView = (MoPubView) activity.findViewById(R.id.banner_view);
			
			
			
			moPubView.setAdUnitId(Settings.MOPUB_ID_BANNER); // Enter your Ad Unit ID from
													// www.mopub.com
			moPubView.loadAd();
			
			 
		} catch(Exception e) {
			
		}
	}

    
	public static void mopubShowInterstitial(final Activity activity, String adPositionKey) {
		
		mopubShowInterstitial(activity, adPositionKey, false);
	}
	
	public static void mopubShowInterstitial(final Activity activity, String adPositionKey, boolean useLandscapeAd) {
		
		
		
		try {
			
			String mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
			
			
			
			if (adPositionKey != null) {
				
				
			
				if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL; 
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
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
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT)) {

					// + " /// " + mopubSearchExitInterstitial.isReady()
					
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
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
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {
					
					
					
					
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
						
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
						
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
				String mopubAdId = null;
				MoPubInterstitial mInterstitial;
				
				
				if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					mopubExitInterstitial = new MoPubInterstitial(activity, mopubAdId);
					mopubExitInterstitial.setInterstitialAdListener(getMopubPreloadExitInterstitialListener(activity, adPositionKey)); // NOTE USE OF SPECAIL "EXIT" LISTENER
					mopubExitInterstitial.load(); 
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT)) {
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					}
					
					mopubSearchExitInterstitial = new MoPubInterstitial(activity, mopubAdId);
					mopubSearchExitInterstitial.setInterstitialAdListener(getMopubPreloadExitInterstitialListener(activity, adPositionKey)); // NOTE USE OF SPECAIL "EXIT" LISTENER
					mopubSearchExitInterstitial.load();
				} else if (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {
					if (useLandscapeAd) {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
					} else {
						mopubAdId = Settings.MOPUB_ID_INTERSTITIAL;
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
	
	
	
	public static void vungleShowInterstitial(final Activity activity, String adPositionKey) {
		
		 
		
		VunglePub.setEventListener(new VunglePub.EventListener() {
		    /**
		     * Called when an ad starts.
		     */
		    @Override
		    public void onVungleAdStart() {
		    	
		        
		    }
		 
		    /**
		     * Called when the user exits ad unit completely (usually the post-roll).
		     */
		    @Override
		    public void onVungleAdEnd() {
		    	
		    }
		 
		    /**
		     * Called when the user exits the ad unit completely - but only if the user 
		     * watched at least some portion of the ad.
		     * 
		     * @param watchedSeconds the number of seconds of video that were watched.
		     * @param totalAdSeconds the total length of the ad in seconds.
		     */
		    @Override
		    public void onVungleView(double watchedSeconds, double totalAdSeconds) {
		    	
		        final double watchedPercent = watchedSeconds / totalAdSeconds;
		        if (watchedPercent >= 0.8) { 
		        	
		        }
		    }
		});
		
		boolean wasVungleAdPlayed = false; 
		
		try {

			boolean isAv = VunglePub.isVideoAvailable(true);
			
			 
			wasVungleAdPlayed = VunglePub.displayAdvert();    
			 
			

			
			
			
		} catch(Exception e) {
			
		}
		
		if (!wasVungleAdPlayed) showDefaultInterstitial(activity, adPositionKey); 
		
	}
	
	
	
	
	public static void startappShowInterstitial(final Activity activity, String adPositionKey) { 
		
		try {
			if (adPositionKey != null && (adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				// back
				
				StartAppAd startAppAd = new StartAppAd(activity);
				startAppAd.onBackPressed(); // load the next ad  
				
			} else {
				
				
				StartAppAd startAppAd = new StartAppAd(activity);
				startAppAd.showAd(); // show the ad
				startAppAd.loadAd(); // load the next ad
			}
		} catch(Exception e) {
			
		}
	}
		
	
	public static void airpushShowInterstitial(final Activity activity, String adPositionKey) {
		

		
		
		try {

			try {  
				
			     MA ma =new MA(activity, null, false); 
			    ma.callAppWall();    
			} catch (Exception e) {
				
			}
			
		} catch(Exception e) {
			
		}
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
			MobileCore.directToMarket(activity);
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
				appnext.setAppID(Settings.APPNEXT_ID);
				appnext.showBubble(); 
				
			} else {
				appnext.setAppID(Settings.APPNEXT_ID);
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
	
	

	
	
	
}


