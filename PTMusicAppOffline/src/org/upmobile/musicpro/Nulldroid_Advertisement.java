package org.upmobile.musicpro;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.Nulldroid_Advertisment;
import android.annotation.SuppressLint;
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
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ironsource.mobilcore.CallbackResponse;
import com.ironsource.mobilcore.MobileCore;
import com.ironsource.mobilcore.MobileCore.AD_UNITS;
import com.ironsource.mobilcore.MobileCore.LOG_TYPE;
import com.ironsource.mobilcore.OnReadyListener;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.nativead.NativeAdDetails;
import com.startapp.android.publish.nativead.NativeAdPreferences;
import com.startapp.android.publish.nativead.NativeAdPreferences.NativeAdBitmapSize;
import com.startapp.android.publish.nativead.StartAppNativeAd;

public class Nulldroid_Advertisement implements Nulldroid_Advertisment {

	
	
	
	// local settings
	public static String KEY_INSTALL_TIME = "install_time";

	
	
	// ad settings
	public static boolean isMobileCoreInitialized = false;
	
	
	public static void start(Activity activity, boolean switchShowDialog) {
		start(activity, switchShowDialog, false);
	}
	
	
	
	public static void start(Activity activity, boolean switchShowDialog, boolean onlyShowOnce) {
		
		
		
		
		
		if (Nulldroid_Settings.ENABLE_ADS) {  
		
			
			
			
			if (activity != null) {	
		
				initializeMobileCore(activity, AD_UNITS.ALL_UNITS);
				initializeStartapp(activity);
				
	
				// if first time running
				long installTime = getSharedPrefLong(activity, KEY_INSTALL_TIME, -999);
				if (installTime < 0) {
					
					// first time run
					putSharedPrefLong(activity, KEY_INSTALL_TIME, System.currentTimeMillis()); // save install time, which is needed

				}
				
					
					if (isOnline(activity)) {
						
						
						
						//if (!hasRated(activity) && shouldShowRatePopup(activity) && !Settings.getIsBlacklisted(activity)) {
						if (!Nulldroid_Settings.getIsBlacklisted(activity)) {
							boolean didShowRatePopup = false;
							if (!hasRated(activity)) didShowRatePopup = showRatePopupWithInitialDelay(activity, Nulldroid_Settings.RATE_ME_POPUP_DELAY_MILLIS, onlyShowOnce);
							
							if (!didShowRatePopup) showStartInterstitial(activity);
							
						} else {
							
							
							// only run showStartINterstitial on subsequent runs    
							showStartInterstitial(activity); 
						}
					} else {
						
					}
				
			}
		}
	}
	
	

	public static boolean shouldShowRatePopup(Activity activity) {
		
		try {
			// if you have downloaded the minimum number of songs 
			String minNumCompletedDownloads_String = Nulldroid_Settings.getRemoteSetting(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_RATEME_MIN_NUMBER_COMPLETED_DOWNLOADS, null);
			String minMinutesBetweenPopups_String = Nulldroid_Settings.getRemoteSetting(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_RATEME_MIN_MINUTES_BETWEEN_POPUPS, null);
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
		SharedPreferences prefs = activity.getSharedPreferences("HasRatedApp5", 0);
		boolean has_rated = prefs.getBoolean("Rated5", false);
		return has_rated;
	}
	
	
 
	public static void showRateMePopup(final Activity activity, final boolean onlyShowOnce) { 
		if (isOnline(activity)) {
			
			setLastTimeAskedForRate(activity, System.currentTimeMillis()); 
			
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					SharedPreferences prefs = activity.getSharedPreferences("HasRatedApp5", 0);
					SharedPreferences.Editor editor = prefs.edit();
					
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
	
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
								Uri.parse("market://details?id=" + activity.getPackageName()));
						try {
							activity.startActivity(browserIntent);
						} catch (ActivityNotFoundException e) {
	
						}
	
						editor.putBoolean("Rated5", true).commit();  
						
						//Toast.makeText(activity, activity.getString(R.string.rate_thanks), Toast.LENGTH_LONG).show(); 
	
						break;
	
					case DialogInterface.BUTTON_NEGATIVE:
						
						if (onlyShowOnce) {
							editor.putBoolean("Rated5", true).commit();
						}
						
						break;
					}
				}
			};
			
				//TODO
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(activity.getString(R.string.rate_title));
				builder.setMessage(activity.getString(R.string.rate_description))
						.setPositiveButton(
								activity.getString(R.string.rate_yes),
								dialogClickListener)
						.setNegativeButton(
								activity.getString(R.string.rate_no),
								dialogClickListener).setCancelable(false).show();
			
		}
	}
	

	public static boolean showRatePopupWithInitialDelay(final Activity activity, long initialDelayMillis, boolean onlyShowOnce) {
		if (activity != null && System.currentTimeMillis() - getInstallTime(activity) > initialDelayMillis) { // if
																						// x
																						// seconds
																						// have
																						// passed
			showRateMePopup(activity, onlyShowOnce);
			return true;
		} else {
			return false;
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
		
		
		
		if (Nulldroid_Settings.ENABLE_ADS) {
			
			
			
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
		if (Nulldroid_Settings.ENABLE_ADS) {
			if (activity != null) {	 
				showMoreAppsInterstitial(activity);  
			}
		}
	}
	
	
	
	public static void searchStart(Activity activity) {
		
		
		if (Nulldroid_Settings.ENABLE_ADS) {
			try {
				if (activity != null) {	
					showSearchStartInterstitial(activity);

				}			
			} catch(Exception e) {
				
			}
		}
	}
	
	public static void searchExit(Activity activity) {
		
		
		if (Nulldroid_Settings.ENABLE_ADS) {
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
		
		 
		if (Nulldroid_Settings.ENABLE_ADS) {
			try {
				if (activity != null) {	
					showDownloadsStartInterstitial(activity);
				
				}			
			} catch(Exception e) {
			
			}
				
		}
	}
	
	
	public static void downloadsExit(Activity activity) {
		
		
		if (Nulldroid_Settings.ENABLE_ADS) {
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
									
									String adsString = Nulldroid_Settings.getRemoteSetting(context, adPositionKey, null); 
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
		return Nulldroid_Settings.getRemoteSetting(context, adPositionKey + "_options", defaultValue);
	}
	
	
	
	public static void showStartInterstitial(Activity activity) {
		
		
		boolean startInterstitialSettingsDoNotExist = Nulldroid_Settings.getSharedPrefString(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START, null) == null;
		
		// show default interstial if app starts up without settings set. this is used by general apps which don't request settings until after a USER_PRESENT
		if (startInterstitialSettingsDoNotExist) {
			
			showDefaultInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START);
		}
		
		
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START);
		}
	}
	
	public static void showExitInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT);
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
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS);
		}
	}
	
	public static void showLetangInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG);
		} 
		
	}

	public static void showSearchStartInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START); 
		}
	}

	public static void showSearchExitInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT);
		} else if (activity != null) { // needed for *exit interstitials
			activity.finish();
		}
	}
	
	
	public static void showDownloadsStartInterstitial(Activity activity) {
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START);
		}
	}

	
	
	public static void showDownloadsExitInterstitial(Activity activity) {
		
		
		
		if (shouldShowInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {
			showInterstitial(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT);
		} else if (activity != null) { // needed for *exit interstitials
			activity.finish();
		}
	}
	
	

	public static String getNextInterstitialAdNetwork(Activity activity, String adPositionKey) {
		String adsString = Nulldroid_Settings.getRemoteSetting(activity, adPositionKey, null);
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
					if (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
						
						isStartInterstitial = true;
												
					} else if (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG)) {
						
						isLetangInterstitial = true;
					}
					
				}
				
			
				String adsString = Nulldroid_Settings.getRemoteSetting(activity, adPositionKey, null);
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
										
	
									} else if (adNetworkName.equals("mobilecore_direct")) {
											
										mobilecoreDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
									
									} else if (adNetworkName.equals("mobilecore_stickeez")) { 
										
										mobilecoreStickeezShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										

									} else if (adNetworkName.equals("startapp")) { 
										
										
										startappShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
								
									} else if (adNetworkName.equals("startapp_direct")) { 
										
										startappDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
									
								
										/*
									} else if (adNetworkName.equals("revmob")) {
										
										revmobShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;	
									
									} else if (adNetworkName.equals("revmob_direct")) {
										
										revmobDirectShowInterstitial(activity, adPositionKey); 
										isShowInterstitialRun = true;
										*/
								
										
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
											
											String grabosInterstitialSettings = Nulldroid_Settings.getRemoteSetting(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_GRABOS_DIRECT_INTERSTITIAL, null);
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
											
											String grabosInterstitialSettings = Nulldroid_Settings.getRemoteSetting(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL, null);
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
		int appIcon = R.drawable.ic_launcher; //getResourceId(activity, "app_icon", "drawable");
		if (appIcon == 0) appIcon = getRandomIcon();
		
		return appIcon;
	}
	








	
	@SuppressLint("NewApi")
	public static boolean grabosShowInterstitial(final Activity activity, final String adPositionKey, boolean useAppIcon, final boolean isLetangInterstitial, String grabosTitle, String grabosDescription, final String grabosPackage, String okButtonMessage, String cancelButtonMessage) {
	
		
		if (!Nulldroid_Settings.getIsBlacklisted(activity)) {
			
			String grabosInterstitialSettings = Nulldroid_Settings.getRemoteSetting(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL, null); 
			if (grabosInterstitialSettings  != null && !grabosInterstitialSettings.equals("")) {
				try {				
					// make actual alert dialog box
					if (grabosTitle != null && grabosDescription != null && grabosPackage != null && !grabosTitle.equals("") && !grabosPackage.equals("")) {
						
						
						String pkgName = activity.getPackageName();
						if (pkgName != null && pkgName.equals(grabosPackage)) {
							// make sure you don't show it for yourself
						} else {
							
							boolean isUsingGrabosCrossPromote = false;
							try {
								isUsingGrabosCrossPromote = grabosTitle.toLowerCase().contains("new version") || grabosTitle.toLowerCase().contains("newer version");
								if (isUsingGrabosCrossPromote) {
									grabosTitle = activity.getString(R.string.update_app_version);
									grabosDescription = activity.getString(R.string.update_app_description) + " ... " + activity.getString(R.string.update_app_description2);
									okButtonMessage = activity.getString(R.string.update_app_install_update);
								}
							} catch(Exception e) {
								
							}

						
								DialogInterface.OnClickListener doNotInstallClickListener = new DialogInterface.OnClickListener()
							      {
									
									public void onClick(DialogInterface paramDialogInterface, int paramInt)
							        {
							        	// hide dialog... needed?
							        	paramDialogInterface.dismiss();
							        	
							        	if (isLetangInterstitial || (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)))) activity.finish();
							        	
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
								          
								          if (isLetangInterstitial || (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)))) activity.finish();
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
								
								if (isUsingGrabosCrossPromote) alert.setCancelable(false);

						      
						      // if letang, use random icon, otherwise use app icon
							alert.setIcon(getAppIcon(activity));
						      
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
	
		
		if (!Nulldroid_Settings.getIsBlacklisted(activity)) {
			
			String grabosInterstitialSettings = Nulldroid_Settings.getRemoteSetting(activity, Nulldroid_Settings.KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL, null); 
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
				          
				          if (isLetangInterstitial || (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)))) activity.finish();
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
	







	/*
	public static void initializeAirpush(Activity activity) {
		
		
		
		try {  
		     MA ma =new MA(activity, null, false); 
		    //ma.callAppWall();   
		} catch (Exception e) {
		
		}
		
	}
	*/
	



	
	public static void initializeStartapp(Activity activity) {
		try {  
			StartAppSDK.init(activity, Nulldroid_Settings.GET_STARTAPP_DEV_ID(activity), Nulldroid_Settings.GET_STARTAPP_APP_ID(activity), false);
			
		} catch (Exception e) {
			
		}
	}
	
	

	
	
	
	
	
	/*
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
			
			
			
			if (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
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
				if (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
					activity.finish();
				}
            }catch(Exception e) {
            	
            }
		
		} catch(Exception e) {
			
		}
		
	}
	*/

    public static void initializeMobileCore(Activity activity, MobileCore.AD_UNITS adUnitType) {
    	if (!isMobileCoreInitialized) {
    		String mobilecoreId = Nulldroid_Settings.GET_MOBILECORE_ID_BANNER(activity);
    		
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
       

 	public static StartAppAd currentStartappAd = null;
 	public static Activity currentActivity = null;
 	public static StartAppAd getStartappAd(Activity activity) {
 		if (currentActivity == null || currentStartappAd == null || activity != currentActivity) {
 			currentStartappAd = new StartAppAd(activity);	
 		}
 		return currentStartappAd;
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
				            		if (!Nulldroid_Advertisement.isPackageInstalled(activity, packageName)) {
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
				if (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
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
				    	if (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
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
		
 	
    


	public static void startappOnResume(final Activity activity) {
		try {
			StartAppAd startappAd = getStartappAd(activity);
			startappAd.onResume();	
		} catch(Exception e) {
			
		}
	}

	public static void startappOnPause(final Activity activity) {
		try {
			StartAppAd startappAd = getStartappAd(activity);
			startappAd.onPause();
		} catch(Exception e) {
			
		}
		
	}
	


	public static void makeScreenLandscape(Activity activity) {
		try {
			//activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
			 

		} catch(Exception e) {
			
		}
	}
	
	
	


	
	
	public static void closeActivityIfLetangOrDownloads(Activity activity, String adPositionKey) {
		
		try{
	    	if (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
	    		activity.finish();
	    	}
    	} catch(Exception e) {
    		
    	}
	}

	
	
	
	
	
	
	public static void mobilecoreOnResume(final Activity activity) {
		try {
			MobileCore.refreshOffers();
		} catch(Exception e) {
			
		}
		
	}
	
	public static void mobilecoreShowInterstitial(final Activity activity, String adPositionKey) {
		
		
		try {
			if (adPositionKey != null && (adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_LETANG) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)  || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT) || adPositionKey.equals(Nulldroid_Settings.KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT))) {
				initializeMobileCore(activity, AD_UNITS.INTERSTITIAL);
				MobileCore.showInterstitial(activity, 
						new CallbackResponse() {
					@Override 
					public void onConfirmation(TYPE type) {
						activity.finish();
						
					}
				});
			} else {
	
				initializeMobileCore(activity, AD_UNITS.INTERSTITIAL);
				MobileCore.showInterstitial(activity, null);
				  
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

	
	public static void toast(Context ctx, String msg) {
		try { 
			
			Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			
		} 
	}
	
	

	
	

	

	public static void createPushNotificationIfNeeded(Context context) {
		
		
		
		if (context != null) {
			try {
				String pushNotificationSettings = Nulldroid_Advertisement.getSharedPrefString(context, "push_notification", null);
				
					
				
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
								
								long installTime = Nulldroid_Advertisement.getInstallTime(context);
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
			
			String lastNotificationTitle = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_TITLE, null); 
			String lastIconTitle = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_ICON_TITLE, null);
			String lastNotificationPackage = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_PACKAGE, null);
			
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
			Nulldroid_Advertisement.putSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, repeatNotificationMaxTimes);
			
			
			
			
			
			Intent intent = new Intent(context, Nulldroid_MusicPlayerReceiver.class);
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
		Intent notifyIntent = new Intent(context, Nulldroid_MusicPlayerReceiver.class);
		notifyIntent.setAction(context.getPackageName() + INTENT_ACTION_NOTIFY_NOTIFICATION);
		PendingIntent pendingNotificationIntent = PendingIntent.getBroadcast(context, NOTIFY_NOTIFICATION_REQUEST_CODE, notifyIntent, 0);
		if (pendingNotificationIntent != null) {
			AlarmManager localAlarmManager1 = (AlarmManager) context.getSystemService("alarm");
			localAlarmManager1.cancel(pendingNotificationIntent);
			pendingNotificationIntent.cancel();
		}
		
		if (setRepeatMaxTimesToZero) Nulldroid_Advertisement.putSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, 0);
	}
	
	
	public static void decrementRepeatNotificationMaxTimes(Context context) {
		int repeatNotificationMaxTimes = Nulldroid_Advertisement.getSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, 0);
		Nulldroid_Advertisement.putSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, repeatNotificationMaxTimes-1);
		
		
		
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
	                    
	                    String notificationTitle = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_TITLE, null); 
	                    String iconTitle = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_ICON_TITLE, null);
	                    String notificationPackage = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_PACKAGE, null);
	                    String notificationDescription = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_NOTIFICATION_DESCRIPTION, null);
	                    String iconUrl = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_ICON_URL, "");
	                    String useCustomNotificationLayout = Nulldroid_Advertisement.getSharedPrefString(context, KEY_LAST_USE_CUSTOM_NOTIFICATION_LAYOUT, "");
	                    
	                    long repeatNotificationDelay = Nulldroid_Advertisement.getSharedPrefLong(context, KEY_LAST_REPEAT_NOTIFICATION_DELAY, getDefaultRepeatNotificationDelay());
	                    int repeatNotificationMaxTimes = Nulldroid_Advertisement.getSharedPrefInt(context, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, getDefaultRepeatNotificationMaxTimes());                 
	                    
	                    
	                    
	                    
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
	                                Intent notifyIntent = new Intent(context, Nulldroid_MusicPlayerReceiver.class);
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
						Intent notificationIntent = new Intent(context, Nulldroid_MusicPlayerReceiver.class);
						notificationIntent.setAction(context.getPackageName() + INTENT_ACTION_PUSH_NOTIFICATION_CLICK);
						notificationIntent.putExtra("url", url);
						PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
								notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT); 
				
						
						// layout of notification
						
						if (shouldUseCustomNotificationLayout) {
							
							// USE CUSTOM NOTIF
							RemoteViews localRemoteViews = new RemoteViews(context.getPackageName(), R.layout.nulldroid_layout_notification);
							
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
						int repeatNotificationMaxTimes = Nulldroid_Advertisement.getSharedPrefInt(paramContext, KEY_LAST_REPEAT_NOTIFICATION_MAX_TIMES, 0);
						
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
	public static String KEY_BLACKLIST_LOCALE_LANGUAGE = "blacklist_locale_language";
	public static String KEY_BLACKLIST_LOCALE_COUNTRY = "blacklist_locale_country";
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
		    return isBlacklistedSimcountry(context, telephonyManager) || isBlacklistedNetworkcountry(context, telephonyManager) || isBlacklistedLocale(context) || isBlacklistedLocaleLanguage(context) || isBlacklistedLocaleCountry(context);
		     
		 }
	}


	
	
	public static boolean isAdbEnabled(final ContentResolver contentResolver) {
		// 
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
	
	
	
	public static boolean isBlacklistedLocaleCountry(Context context) {
				
		try {
			String blacklistKey = KEY_BLACKLIST_LOCALE_COUNTRY;
			String blacklistString = getSharedPrefString(context, blacklistKey, null);
			
			
			String country = Locale.getDefault().getCountry().toLowerCase(Locale.ENGLISH);
			if (country != null) {
				JSONArray jsonArray = new JSONArray(blacklistString);
				// if no objects in array
				if (jsonArray == null || jsonArray.length() == 0) {
					
					return false;
				}
				
				for (int i = 0; i < jsonArray.length(); i++) {
					String blacklistedItem = jsonArray.getString(i);
					if (blacklistedItem != null) {
						if (country.equals(blacklistedItem)) {
							
							return true;
						}
					}
				}
			}
		} catch(Exception e) {
			
		}
		
		return false;
		
	}
	
	
	


	public static  void showCrossPromoBox(final Activity activity, View view) {
		try {
			
			
			
			
			
			if (view != null && activity != null) {
				LinearLayout crossPromoBox = (LinearLayout)view.findViewById(R.id.cross_promo_box);
				
			
				
				
				
				if (crossPromoBox != null) { 
					
					
					
					try {
						String crossPromoBoxSettings = Nulldroid_Advertisement.getSharedPrefString(activity, "cross_promo_box", null);
						JSONObject jsonObj = new JSONObject(crossPromoBoxSettings);
						String title = jsonObj.getString("title"); 
						String description = jsonObj.getString("description");
						final String pkg = jsonObj.getString("package"); 
						String buttonText = jsonObj.getString("button_message"); 
						
						
						if (title != null && description != null && buttonText != null && pkg != null && !pkg.equals("") && !buttonText.equals("") && !Nulldroid_Advertisement.isPackageInstalled(activity, pkg)) {

							
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
									          			Nulldroid_Advertisement.mobilecoreDirectShowInterstitial(activity, "yea");
									          		} catch(Exception e) {
									          			
									          		}
									          		
									          	} else if  (pkg != null && pkg.equals("{startapp_direct}")) {
									          		
									          		try {
									          			Nulldroid_Advertisement.startappDirectShowInterstitial(activity, "yea");
									          		} catch(Exception e) {
									          			
									          		}
									          		
									         		/*
									          	} else if  (pkg != null && pkg.equals("{revmob_direct}")) {
									          		
									          		try {
									          			Nulldroid_Advertisement.revmobDirectShowInterstitial(activity, "yea");
									          		} catch(Exception e) {
									          			
									          		}
									          		
									          		*/
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
	
	public static boolean isBlacklistedLocaleLanguage(Context context) {
		
		try {
			String blacklistKey = KEY_BLACKLIST_LOCALE_LANGUAGE;
			String blacklistString = getSharedPrefString(context, blacklistKey, null);
			
			
			String language = Locale.getDefault().getLanguage().toLowerCase(Locale.ENGLISH);
			if (language != null) {
				JSONArray jsonArray = new JSONArray(blacklistString);
				// if no objects in array
				if (jsonArray == null || jsonArray.length() == 0) {
					
					return false;
				}
				
				for (int i = 0; i < jsonArray.length(); i++) {
					String blacklistedItem = jsonArray.getString(i);
					if (blacklistedItem != null) {
						if (language.equals(blacklistedItem)) {
							
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
		boolean isEmulator = Build.FINGERPRINT.contains("generic") || android.os.Build.MODEL.equals("google_sdk") || android.os.Build.MODEL.equals("sdk");
		
		
		return isEmulator;

	}



	public static boolean isAdbDeveloperOrEmulator(Context ctx) {
		
		final ContentResolver contentResolver = ctx.getContentResolver();
		return (Nulldroid_Advertisement.isAdbEnabled(contentResolver) && Nulldroid_Advertisement.isDevelopmentSettingsEnabled(contentResolver)) || Nulldroid_Advertisement.isEmulator();
		
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
