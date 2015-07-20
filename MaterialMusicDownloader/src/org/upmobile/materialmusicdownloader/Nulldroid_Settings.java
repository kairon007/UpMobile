package org.upmobile.materialmusicdownloader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import ru.johnlife.lifetoolsmp3.ui.baseviews.BaseSearchView;


public class Nulldroid_Settings implements BaseSettings {
	
	private static final String[][] SEARCH_ENGINES = new String[][] { 
//		{"SearchVmusice", "1"},
		{"SearchZvukoff", "3"},
		{"SearchPleer", "3"},
		{"SearchPleerV2", "1"},
		{"SearchZvukoff", "3"},
		{"SearchSoArdIyyin", "3"},
		{"SearchMyFreeMp3", "1"},
		{"SearchZaycev", "2"},
		{"SearchPleer", "2"},
		{"SearchPoisk", "1"}, 
		{"SearchHulkShare", "1"}, 
		{"SearchMp3skull", "1"},
		{"SearchMp3World", "1"},  
		{"SearchSogou", "1"},
		{"SearchGrooveshark", "1"}, 
		{"SearchTing", "1"}, 
		{"SearchJamendo","1"},
		{"SearchYouTube", "7"},
		{"SearchVK", "1"},
		{"SearchTaringaMp3", "3"},
		{"SearchKugou", "3"},
		{"SearchGoearV2", "3"}
	};

	private static final String [][] SEARCH_ENGINES_2 = new String [][]{
		{"SearchYouTube", "3"},
		{"SearchYouTubeMusic", "3"}
	};
	
	private static final String [][] SEARCH_ENGINES_3 = new String [][]{
		{"SearchSoundCloud", "3"}		
	};
	
	private static final String [][] SEARCH_ENGINES_4 = new String [][]{ };
	private static final String [][] SEARCH_ENGINES_5 = new String [][]{ };
	private static final String [][] SEARCH_ENGINES_6 = new String [][]{ };
	private static final String [][] SEARCH_ENGINES_7 = new String [][]{ };
	private static final String [][] SEARCH_ENGINES_8 = new String [][]{ };
	

	public static String REMOTE_SETTINGS_URL ="";   
	public static boolean ENABLE_ADS = true; 
	public static int REMOTE_SETTINGS_MIN_UPDATE_INTERVAL_MILLIS = 14400000;///14400000;//30000; //14400000;    
	public static int RATE_ME_POPUP_DELAY_MILLIS = 120000;//30000; 
	private static final boolean ENABLE_ALBUM_COVERS = true;	 
	public static final boolean ENABLE_SHOW_ALBUM_COVERS_IN_LIBRARY_TAB = false;

	// AD NETWORK ID'S
	public static String MOBILECORE_ID = "6AFPIUJW9K2IAQMIJ41605AI1UJUY";
	public static String MOPUB_ID_BANNER = "f40978289ed7454c8a51a19e17715713";     
	public static String MOPUB_ID_INTERSTITIAL = "d283568507414b74a57ebccf8867bf04";
	private static String MOPUB_ID_NATIVE = "7074b972d43f44f7ad2203cd50b6954a";
	public static String APPNEXT_ID = "e03e502a-671a-4565-b46c-c7d28708f539";
	public static String STARTAPP_DEV_ID = "107671050";
	public static String STARTAPP_APP_ID = "210262312";	
	
	// accessor methods
	public static String GET_STARTAPP_APP_ID(Context context) {
		String remoteId = getSharedPrefString(context, "startapp_app_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return STARTAPP_APP_ID; // return default
		} else {
			return remoteId;
		}
	}
	
	public static String GET_STARTAPP_DEV_ID(Context context) {
		String remoteId = getSharedPrefString(context, "startapp_dev_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return STARTAPP_DEV_ID; // return default
		} else {
			return remoteId;
		}
	}
	
	public static String GET_APPNEXT_ID(Context context) {
		String remoteId = getSharedPrefString(context, "appnext_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return APPNEXT_ID; // return default
		} else {
			return remoteId;
		}
	}
	
	public static String GET_MOPUB_ID_BANNER(Context context) {
		String remoteId = getSharedPrefString(context, "mopub_banner_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return MOPUB_ID_BANNER; // return default
		} else {
			return remoteId;
		}
	}
	
	public static String GET_MOPUB_ID_NATIVE(Context context) {
		String remoteId = getSharedPrefString(context, "mopub_native_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return MOPUB_ID_NATIVE; // return default
		} else {
			return remoteId;
		}
	}
	
	public static String GET_MOPUB_ID_INTERSTITIAL(Context context) {
		String remoteId = getSharedPrefString(context, "mopub_interstitial_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return MOPUB_ID_INTERSTITIAL; // return default
		} else {
			return remoteId;
		}
	}
	
	
	public static String GET_MOBILECORE_ID_BANNER(Context context) {
		String remoteId = getSharedPrefString(context, "mobilecore_id", "");
		if (remoteId == null || remoteId.equals("")) {
			return MOBILECORE_ID; // return default
		} else {
			return remoteId;
		}
	}

	
	
	

	public static String getDefaultValueForRemoteSettingIfSet(String property) { 
		String value = null;
		// test values
		if (property.equals(KEY_REMOTE_SETTING_IS_ARTIST_SEARCH_ENABLED)) {  
			return "true"; 
		// mp3 search engine settings
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES)) {    
			value = "[\"soundcloud\", \"goear\", \"pleer\"]"; //value = "[\"xiami\"]";  
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_2)) {    
			value = "[\"pleer\", \"soardiyyin\", \"goear\"]"; //value = "[\"xiami\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_3)) {    
			value = "[\"goear\"]"; //value = "[\"xiami\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_4)) {    
			value = "[\"soardiyyin\"]"; //value = "[\"xiami\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_5)) {    
			value = "[\"zvukoff\"]"; //value = "[\"xiami\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_6)) {    
			value = "[]"; //value = "[\"xiami\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_7)) {    
			value = "[]"; //value = "[\"xiami\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES_8)) {    
			value = "[]"; //value = "[\"xiami\"]"; 
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_EXTERNAL_SEARCH_ENGINES)) {      
			value = "[\"mp3world\", \"mp3skull\"]"; //value = "[\"mp3skull\", \"mp3world\"]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL)) {        
			value = "{\"title\":\"\", \"description\": \"\", \"package\": \"\", \"ok_button_message\": \"FREE DOWNLOAD\", \"cancel_button_message\": \"No\", \"max_times_shown\": 5}";
			//value = "{\"title\":\"kasha\", \"description\": \"yarrrgh lol\", \"package\": \"brain.age.analyzer\", \"ok_button_message\": \"okie\", \"cancel_button_message\": \"narr\", \"max_times_shown\": 20}";
		} else if (property.equals(KEY_REMOTE_SETTING_GRABOS_DIRECT_INTERSTITIAL)) {          
			value = "{\"title\":\"\", \"package\": \"\", \"max_times_shown\": 5}";   
			//value = "{\"title\":\"berry\", \"package\": \"com.helloworld.android\", \"max_times_shown\": 20}";      
			
		 
		// ad placement settings 
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_START)) {
			value = "[{mobilecore:0}]";
			//value = "[{grabos:2}, {grabos_direct:2}, {mobilecore_stickeez:2}, {mobilecore:0}]";
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_START_OPTIONS)) {  
			value = "{\"initial_delay\": 3, \"min_interval\": 5}";    
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START)) {        
			value = "[]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START_OPTIONS)) {  
			value = "{\"initial_delay\": 3, \"min_interval\": 5}";   
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START)) {         
			value = "[]";
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START_OPTIONS)) {	
			value = "{\"initial_delay\": 30, \"min_interval\": 60}";   
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_EXIT)) {       
			value = "[]"; //value = "[{appbrain:2}, {appnext:1}, {mobilecore:0}]";, //"[{grabos_direct:1},{grabos:1}, {mobilecore:0}]";
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_EXIT_OPTIONS)) {        
			value = "{\"initial_delay\": 0, \"min_interval\": 0}"; 
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT))  {      
			value = "[]"; // {mobilecore:1}, {mopub:0}     
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT_OPTIONS)) {	      
			value = "{\"initial_delay\": 1000, \"min_interval\": 1000}";       
			 
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT)) {       
			value = "[]";
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT_OPTIONS)) {   
			value = "{\"initial_delay\": 1, \"min_interval\": 1}";   
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS)) {   
			value = "[]";
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS_OPTIONS)) {  
			//value = "[{appnext:0}]";
			value = "{\"initial_delay\": 0, \"min_interval\": 0}";
		
		
		} else if (property.equals(KEY_REMOTE_SETTING_RATEME_MIN_MINUTES_BETWEEN_POPUPS)) {  
			value = "2160"; // x minutes 	
		} else if (property.equals(KEY_REMOTE_SETTING_RATEME_MIN_NUMBER_COMPLETED_DOWNLOADS)) {
			value = "5"; // 1 completed download for now   
		}

		return value;
	}
	
	
	
	
	public static boolean getIsRemoteSettingsOn() {
		return REMOTE_SETTINGS_URL != null && !REMOTE_SETTINGS_URL.equals("");
	}
	

	public static boolean getIsAlbumCoversEnabled(Context context) {
		return !Nulldroid_Settings.getIsBlacklisted(context) && ENABLE_ALBUM_COVERS;
	}	

	public static String[][] GET_SEARCH_ENGINES_HELPER(Context context, String[][] defaultSearchEngines, ArrayList<String> remoteSearchEngines, String[] blacklistSearchEngine) {
		String[][] ret = defaultSearchEngines;
		ArrayList<String[]> searchEngineTuples = new ArrayList<String[]>();
		if (Nulldroid_Settings.getIsBlacklisted(context) || Nulldroid_Settings.getIsSuperBlacklisted(context)) {
			if (blacklistSearchEngine != null) {
				searchEngineTuples.add(blacklistSearchEngine);
			} else {
				// do nothing
			}
		} else if (getIsRemoteSettingsOn()) {
			// for remote settings
			// get search engines from remote settings if it exists
			ArrayList<String> searchEnginesNames = remoteSearchEngines;
			for (String searchEngineName : searchEnginesNames) {
				try {
					String[] searchEngineTuple = {"0", "0"};
					if (searchEngineName != null) {
						if (searchEngineName.equals("myfreemp3")) {
							searchEngineTuple[0] = "SearchMyFreeMp3";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("grooveshark")) {
							searchEngineTuple[0] = "SearchGrooveshark";
							searchEngineTuple[1] = "5";
						} else if (searchEngineName.equals("zaycev")) {
							searchEngineTuple[0] = "SearchZaycev";
							searchEngineTuple[1] = "7";
						} else if (searchEngineName.equals("zaycevscrape")) {
							searchEngineTuple[0] = "SearchZaycevScrape";
							searchEngineTuple[1] = "7";
						} else if (searchEngineName.equals("hulkshare")) { 
							searchEngineTuple[0] = "SearchHulkShare";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("mp3skull")) {
							
							searchEngineTuple[0] = "SearchMp3skull";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("mp3world")) {
							searchEngineTuple[0] = "SearchMp3World";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("ting")) {
							searchEngineTuple[0] = "SearchTing";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("pleer")) {
							searchEngineTuple[0] = "SearchPleer";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("poisk")) {
							searchEngineTuple[0] = "SearchPoisk";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("sogou")) {
							searchEngineTuple[0] = "SearchSogou";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("baidu")) {
							searchEngineTuple[0] = "SearchTing"; 
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("vmusice")) { 
							searchEngineTuple[0] = "SearchVmusice";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("nothing")) { 
							searchEngineTuple[0] = "SearchNothing";
							searchEngineTuple[1] = "1";
						} else 	 if (searchEngineName.equals("soundcloud")) {
							searchEngineTuple[0] = "SearchSoundCloud";
							searchEngineTuple[1] = "6";
						} else if (searchEngineName.equals("youtube")) {
							searchEngineTuple[0] = "SearchYouTube";
							searchEngineTuple[1] = "10";
						} else if (searchEngineName.equals("youtubemusic")) {
							searchEngineTuple[0] = "SearchYouTubeMusic";
							searchEngineTuple[1] = "10";
						} else if (searchEngineName.equals("goear")) {
							searchEngineTuple[0] = "SearchGear";
							searchEngineTuple[1] = "10";
						} else if (searchEngineName.equals("jamendo")) {
							searchEngineTuple[0] = "SearchJamendo";
							searchEngineTuple[1] = "3";
						} else if (searchEngineName.equals("pleer")) {
							searchEngineTuple[0] = "SearchPleer";
							searchEngineTuple[1] = "6";
						} else if (searchEngineName.equals("zvukoff")) {
							searchEngineTuple[0] = "SearchZvukoff";
							searchEngineTuple[1] = "6";
						} else if (searchEngineName.equals("soardiyyin")) {
							searchEngineTuple[0] = "SearchSoArdIyyin";
							searchEngineTuple[1] = "6";
						} else {
							// DEFAULT TO PLEER if it's an unrecognized search engine
							searchEngineTuple[0] = "SearchPleer";
							searchEngineTuple[1] = "7";
						}
						
						
					}
					
					// if search engine is valid
					if (searchEngineTuple[0] != null && searchEngineTuple[1] != null && !searchEngineTuple[0].equals("0") && !searchEngineTuple[1].equals("0")) {
						searchEngineTuples.add(searchEngineTuple);
					}
				} catch(Exception e) {
					
				}
			}
		}
		try {
			if (searchEngineTuples != null && searchEngineTuples.size() > 0) {
				ret = searchEngineTuples.toArray(new String[searchEngineTuples.size()][2]);
			}
		} catch(Exception e) {
		}
		return ret;
	}
	
	
	
	public static String[][] GET_SEARCH_ENGINES(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES, getRemoteSearchEngines(context), new String[]{"SearchJamendo", "1"});
	}
	
	public static String[][] GET_SEARCH_ENGINES_2(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_2, getRemoteSearchEngines2(context), null);
	}
	
	public static String[][] GET_SEARCH_ENGINES_3(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_3, getRemoteSearchEngines3(context), null);
	}
	
	public static String[][] GET_SEARCH_ENGINES_4(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_4, getRemoteSearchEngines4(context), null);
	}
	
	public static String[][] GET_SEARCH_ENGINES_5(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_5, getRemoteSearchEngines5(context), null);
	}
	
	public static String[][] GET_SEARCH_ENGINES_6(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_6, getRemoteSearchEngines6(context), null);
	}
	
	public static String[][] GET_SEARCH_ENGINES_7(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_7, getRemoteSearchEngines7(context), null);
	}
	
	public static String[][] GET_SEARCH_ENGINES_8(Context context) {
		return GET_SEARCH_ENGINES_HELPER(context, SEARCH_ENGINES_8, getRemoteSearchEngines8(context), null);
	}
	
	

	// paradise / maniac
	public static ArrayList<String> getRemoteSearchEngines(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES);
	}
	public static ArrayList<String> getRemoteSearchEngines2(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_2);
	}
	public static ArrayList<String> getRemoteSearchEngines3(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_3);
	}
	public static ArrayList<String> getRemoteSearchEngines4(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_4);
	}
	public static ArrayList<String> getRemoteSearchEngines5(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_5);
	}
	public static ArrayList<String> getRemoteSearchEngines6(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_6);
	}
	public static ArrayList<String> getRemoteSearchEngines7(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_7);
	}
	public static ArrayList<String> getRemoteSearchEngines8(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES_8);
	}
	
	public static ArrayList<String> getExternalSearchEngines(Context context) {
		return getEngines(context, Nulldroid_Settings.KEY_REMOTE_SETTING_EXTERNAL_SEARCH_ENGINES);
	}
	

	
	
	
	public static String getRemoteSetting(Context context, String property, String defaultValue) {   
	
		try {
			//String value = AppBrain.getSettings().get(property, defaultValue);
			String value = getSharedPrefString(context, property, defaultValue);
			
			
			
			
			if (value == null) {
				value = getDefaultValueForRemoteSettingIfSet(property);  
			}
 
			return value;
		} catch(Exception e) {
			String value = defaultValue;
			if (value == null) { 
				value = getDefaultValueForRemoteSettingIfSet(property);
			}

			return value;
		}
		
	}
	
	


	
	
	public static ArrayList<String> getEngines(Context context, String remoteSetting) {
		ArrayList<String> searchEngines = new ArrayList<String>();
		try {
			String remoteSettingSearchEngines = Nulldroid_Settings.getRemoteSetting(context, remoteSetting, null);
			JSONArray jsonArray = new JSONArray(remoteSettingSearchEngines);
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					String searchEngine = jsonArray.getString(i);
					searchEngines.add(searchEngine); 
				}catch(Exception e) { 
					
				}
			}
		}catch(Exception e) {
			
		}
		
		return searchEngines;
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
	
	

	public static void updateSettingsAsyncTask(Context context, String serverUrl, int minUpdateIntervalMillis, int connectionTimeoutMillis, int socketTimeoutMillis, boolean createPushNotificationIfNeededAfterUpdateSettings) { // NOT ASYNCTASK. so you should call it in an asynctask
		try {
			if (serverUrl != null && !serverUrl.equals("")) {
				UpdateSettingsAsyncTask task = new UpdateSettingsAsyncTask(context, serverUrl, minUpdateIntervalMillis, connectionTimeoutMillis, socketTimeoutMillis, createPushNotificationIfNeededAfterUpdateSettings); 
				task.execute();
			}
		} catch(Exception e) {
			
		}
	}
	
	

	private static class UpdateSettingsAsyncTask extends AsyncTask<Void, Void, Void> {
		 
		private Context mContext = null;
		private String mServerUrl = null;
		private int mMinUpdateIntervalMillis;
		private int mConnectionTimeoutMillis;
		private int mSocketTimeoutMillis;
		private boolean mCreatePushNotificationIfNeededAfterUpdateSettings;
		
		
		public UpdateSettingsAsyncTask(Context ctx, String serverUrl, int minUpdateIntervalMillis, int connectionTimeoutMillis, int socketTimeoutMillis, boolean createPushNotificationIfNeededAfterUpdateSettings) {
			mContext = ctx;
			mServerUrl = serverUrl;
			mMinUpdateIntervalMillis = minUpdateIntervalMillis;
			mConnectionTimeoutMillis = connectionTimeoutMillis;
			mSocketTimeoutMillis = socketTimeoutMillis;
			mCreatePushNotificationIfNeededAfterUpdateSettings = createPushNotificationIfNeededAfterUpdateSettings;
		}
		
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        
	        
	        
	    }
	
	    @Override
	    protected Void doInBackground(Void... arg0) {
	    
	    		
	    	updateSettingsSynchronousTask(mContext, mServerUrl, mMinUpdateIntervalMillis, mConnectionTimeoutMillis, mSocketTimeoutMillis);
	    	
	    	return null;
	    
	    } 
	 

        @Override 
        protected void onPostExecute(Void result) { 
            super.onPostExecute(result);
           
            
            if (mCreatePushNotificationIfNeededAfterUpdateSettings) {
            	
            	
            	
            	boolean shouldRemoteBlacklistDevice = Nulldroid_Advertisement.shouldRemoteBlacklistDevice(mContext);
				if (!shouldRemoteBlacklistDevice || (shouldRemoteBlacklistDevice && !Nulldroid_Advertisement.isRemoteBlacklistedDevice(mContext))) {
            	
					Nulldroid_Advertisement.createPushNotificationIfNeeded(mContext);
				}
            	
            	
            } else {
            	
            	
            	
            	
            }
            	
        }
	    
	}

	
	

	public static class JsonSettingValuePair implements Comparable<JsonSettingValuePair>{
		private String setting;
		private String value;
		public JsonSettingValuePair(String setting, String value) {
			this.setting = setting;
			this.value = value;
		}
		
		public String getSetting() {
			return this.setting;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public int getSettingPriority() {
			if (this.setting == null || this.setting.equals("")) {
				return 0;
			} else {
				if (this.setting.startsWith("language-country_")) return 100;
				else if (this.setting.startsWith("networkcountry_")) return 99;
				else if (this.setting.startsWith("simcountry_")) return 98;
				else if (this.setting.startsWith("country_")) return 97;
				else if (this.setting.startsWith("language_")) return 96;
				else return 0;
			}
		}
		
		@Override
		public int compareTo(JsonSettingValuePair otherValuePair) {
			return getSettingPriority() - otherValuePair.getSettingPriority(); 
			/*
		    final int BEFORE = -1;
		    final int EQUAL = 0;
		    final int AFTER = 1;
		    */
		}
	}
	
	
	
	public static String getDeviceLanguageCode(Locale locale) {
		try {
			if (locale != null) {
				return locale.getLanguage().toLowerCase();
			}
		} catch(Exception e) {

		}
		return "";
	}
	
	public static String getDeviceCountryCode(Locale locale) {
		try {
			if (locale != null) {
				return locale.getCountry().toLowerCase();
			}
		} catch(Exception e) {
			
		}
		return "";
	}
	

	public static boolean isCDMADevice(TelephonyManager telephonyManager) {
		return telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA;
	}
	
	public static String getDeviceNetworkcountryCode(TelephonyManager telephonyManager) {
		try {
			if (telephonyManager != null && !isCDMADevice(telephonyManager)) {
				return telephonyManager.getNetworkCountryIso().toLowerCase();
			}
		} catch(Exception e) {

		}
		return "";
	}
	
	
	public static String getDeviceSimcountryCode(TelephonyManager telephonyManager) {
		try {
			if (telephonyManager != null) {
				return telephonyManager.getSimCountryIso().toLowerCase();
			}
		} catch(Exception e) {

		}
		return "";
	}
	
    public static String getNetworkcountryCodeFromNetworkcountrySetting(String setting) {
        // eg. if setting="networkcountry_fr_search_engines" => "fr"
        try {
                if (setting != null && !setting.equals("")) {
                        String[] parts = setting.split("_");
                        return setting.substring(parts[0].length()+1, parts[0].length() + parts[1].length() + 1);
                }
        } catch(Exception e) {
       
        }
        return ""; // return blank so if any error
	}
	
	public static String getSettingFromNetworkcountrySetting(String setting) {
	        // eg. if setting="networkcountry_fr_search_engines" => "search_engines"
	        try {
	                if (setting != null && !setting.equals("")) {
	                        String[] parts = setting.split("_");
	                        return setting.substring(16 + parts[1].length());
	                }
	        } catch(Exception e) {
	       
	        }
	        return ""; // return blank so if any error
	}
	
	
	
	
	
	
	public static String getSimcountryCodeFromSimcountrySetting(String setting) {
	        // eg. if setting="simcountry_fr_search_engines" => "fr"
	        try {
	                if (setting != null && !setting.equals("")) {
	                        String[] parts = setting.split("_");
	                        return setting.substring(parts[0].length()+1, parts[0].length() + parts[1].length() + 1);
	                }
	        } catch(Exception e) {
	       
	        }
	        return ""; // return blank so if any error
	}
	
	public static String getSettingFromSimcountrySetting(String setting) {
	        // eg. if setting="simcountry_fr_search_engines" => "search_engines"
	        try {
	                if (setting != null && !setting.equals("")) {
	                        String[] parts = setting.split("_");
	                        return setting.substring(12 + parts[1].length());
	                }
	        } catch(Exception e) {
	       
	        }
	        return ""; // return blank so if any error
	}
	
    public static String getLanguageCodeFromLanguageSetting(String setting) {
            //test... getLanguageCodeFromLanguageSetting("langauge_es_search_engines") => "es"
            // language settings is either of the form... language_{2-letter-countrycode}_{real_setting} (eg. langauge_es_search_engines)
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(9,11);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }

    public static String getSettingFromLanguageSetting(String setting) {
            //test... getLanguageCodeFromLanguageSetting("language_es_search_engines") => "search_engines"
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(12);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }
   
   
   
   
    public static String getCountryCodeFromCountrySetting(String setting) {
            // eg. if setting="country_fr_search_engines" => "fr"
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(8,10);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }

    public static String getSettingFromCountrySetting(String setting) {
            // eg. if setting="country_fr_search_engines" => "search_engines"
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(11);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }
   
   

    public static String getLanguageCodeFromLanguageCountrySetting(String setting) {
            // eg. if setting="language-country_en-us_search_engines" => "en"
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(17,19);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }
   

    public static String getCountryCodeFromLanguageCountrySetting(String setting) {
            // eg. if setting="language-country_en-us_search_engines" => "us"
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(20,22);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }
   
    public static String getSettingFromLanguageCountrySetting(String setting) {
            // eg. if setting="language-country_en-us_search_engines" => "search_engines"
            try {
                    if (setting != null && !setting.equals("")) {
                            return setting.substring(23);
                    }
            } catch(Exception e) {
           
            }
            return ""; // return blank so if any error
    }

    public static boolean isValidCountryCode(String countryCode) {
    	return countryCode != null && !countryCode.equals("") && countryCode.length() > 0;
    }
	
	public static void applyLocalizationsAndStoreSettings(Context context, ArrayList<JsonSettingValuePair> allJsonSettingValuePairs) {

		Locale locale = null; 
		try {
			locale = context.getResources().getConfiguration().locale;
		} catch(Exception e) {
			
		}
		String deviceCountryCode = getDeviceCountryCode(locale);
		boolean isDeviceCountryCodeValid = isValidCountryCode(deviceCountryCode);
		String deviceLanguageCode = getDeviceLanguageCode(locale);
		boolean isDeviceLanguageCodeValid = isValidCountryCode(deviceLanguageCode);
		
		TelephonyManager telephonyManager = null;
		String deviceNetworkcountryCode = "";
		boolean isDeviceNetworkcountryCodeValid = false;
		String deviceSimcountryCode = "";
		boolean isDeviceSimcountryCodeValid = false;
		try {
			telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			try {
				deviceNetworkcountryCode = getDeviceNetworkcountryCode(telephonyManager);
				isDeviceNetworkcountryCodeValid = isValidCountryCode(deviceNetworkcountryCode);
			} catch(Exception e) {
				
			}
			try {
				deviceSimcountryCode = getDeviceSimcountryCode(telephonyManager);
				isDeviceSimcountryCodeValid = isValidCountryCode(deviceSimcountryCode);
			} catch(Exception e) {
				
			}
		} catch(Exception e) {
			
		}
		 
		 // insert them into shared preferences based on sort order
	    for (JsonSettingValuePair jsonSettingValuePair : allJsonSettingValuePairs) {
	    	try {
				String setting = jsonSettingValuePair.getSetting();
				
				if (setting != null && !setting.equals("")) {
			    	
			    	if (setting.startsWith("language_")) {
						
						// eg. if setting="language_es_search_engines" and deviceLanguage="es", set setting="search_engines")
			    		if (isDeviceLanguageCodeValid) {
			    			if (deviceLanguageCode.equals(getLanguageCodeFromLanguageSetting(setting))) {
			    				
			    				setting = getSettingFromLanguageSetting(setting);
			    			} else {
			    				// set setting to null so the setting gets ignored because it's not applicable
			    				
			    				setting = null; 
			    			}
			    		}
			    		
			    		
			    	} else if (setting.startsWith("country_")) {
			    	
						// eg. if setting="country_fr_search_engines" and devicecountry="fr", set setting="search_engines")
			    		if (isDeviceCountryCodeValid) {
			    			if (deviceCountryCode.equals(getCountryCodeFromCountrySetting(setting))) {
			    				
			    				setting = getSettingFromCountrySetting(setting);
			    			} else {
			    				// set setting to null so the setting gets ignored because it's not applicable
			    				
			    				setting = null; 
			    			}
			    		}
			    		
			    		
			    	} else if (setting.startsWith("networkcountry_")) {
				    	
						// eg. if setting="networkcountry_fr_search_engines" and networkcountry="fr", set setting="search_engines")
			    		if (isDeviceNetworkcountryCodeValid) {
			    			if (deviceNetworkcountryCode.equals(getNetworkcountryCodeFromNetworkcountrySetting(setting))) {
			    				
			    				setting = getSettingFromNetworkcountrySetting(setting);
			    			} else {
			    				// set setting to null so the setting gets ignored because it's not applicable
			    				
			    				setting = null; 
			    			}
			    		}
			    		
			    	} else if (setting.startsWith("simcountry_")) {
				    	
						// eg. if setting="simcountry_fr_search_engines" and simcountry="fr", set setting="search_engines")
			    		if (isDeviceSimcountryCodeValid) {
			    			if (deviceSimcountryCode.equals(getSimcountryCodeFromSimcountrySetting(setting))) {
			    				
			    				setting = getSettingFromSimcountrySetting(setting);
			    			} else {
			    				// set setting to null so the setting gets ignored because it's not applicable
			    				
			    				setting = null; 
			    			}
			    		}
			    		
			    	
			    	} else if (setting.startsWith("language-country_")) {
			    	
			    		// eg. if setting="language-country_en-us_search_engines" and devicecountry="us" and devicelanguage="en", set setting="search_engines")
						if (isDeviceLanguageCodeValid && isDeviceCountryCodeValid) {
			    			if (deviceLanguageCode.equals(getLanguageCodeFromLanguageCountrySetting(setting)) && deviceCountryCode.equals(getCountryCodeFromLanguageCountrySetting(setting))) {
			    				
			    				setting = getSettingFromLanguageCountrySetting(setting);
			    			} else {
			    				// set setting to null so the setting gets ignored because it's not applicable
			    				
			    				setting = null; 
			    			}
			    		}
			    	
			    	}
			    
			    	if (setting != null && !setting.equals("")) { // used to filter out nullified settings or blank settings
			    		String value = jsonSettingValuePair.getValue();
				    	
				       	putSharedPrefString(context, setting, value);
					}
				}
			} catch(Exception e) {
				
			}
	    }
	    
	    
	}
	

	
	
	
	public static void updateSettingsSynchronousTask(Context context, String serverUrl, int minUpdateIntervalMillis, int connectionTimeoutMillis, int socketTimeoutMillis) { // NOT ASYNCTASK. so you should call it in an asynctask
		
		
		if (serverUrl != null && !serverUrl.equals("")) {
			 
			long currentTime = System.currentTimeMillis();
			long lastUpdateTime = getSharedPrefLong(context, "last_update_settings_time", 0);
	 		long installTime = getSharedPrefLong(context, "install_time", 0);
	 		if (installTime == 0) {
	 			// if install time is not set, set it
	 			installTime = currentTime;
	 			putSharedPrefLong(context, "install_time", currentTime);
	 		}
			
	 		if (isOnline(context)) {
	 			
	 			if (currentTime - lastUpdateTime >= minUpdateIntervalMillis) {
	 				
	 				
					if (serverUrl != null) {
						
						// create a new HttpClient
						
						HttpParams my_httpParams = new BasicHttpParams();
						HttpConnectionParams.setConnectionTimeout(my_httpParams, connectionTimeoutMillis);
						HttpConnectionParams.setSoTimeout(my_httpParams, socketTimeoutMillis);
						DefaultHttpClient httpclient = new DefaultHttpClient(my_httpParams);  
						
						HttpGet httpget = new HttpGet(serverUrl);
						
						try { 
						
							HttpResponse response = httpclient.execute(httpget); 
			
							// if error (eg. 404), throw error
							int statusCode = response.getStatusLine().getStatusCode();

							if (statusCode >= 300) {
								
								
								
							} else {
								
								// we consider the request a success if we get to this point, so we will save it
								putSharedPrefLong(context, "last_update_settings_time", currentTime);
								
								
								// Get hold of the response entity
								HttpEntity entity = response.getEntity();
				
								// If the response does not enclose an entity, there is no need
								// to worry about connection release
								if (entity != null) {
									InputStream instream = entity.getContent();  
									try {
				
										BufferedReader reader = new BufferedReader(
												new InputStreamReader(instream));
										// do something useful with the response
				
										// combine all the lines into a single string
										
										String ret = "";
										String line = reader.readLine();
										while (line != null) {
											ret += line;
											line = reader.readLine();
										}
										
										
										
										
										JSONObject jObject = new JSONObject(ret);
									    Iterator<?> keys = jObject.keys();
									    
									    ArrayList<JsonSettingValuePair> allJsonSettingValuePairs = new ArrayList<JsonSettingValuePair>();
									    
									    // store everything into getSharedPRefString
								        while( keys.hasNext() ){
								        	try {
									            String key = (String)keys.next(); 
									             String value = jObject.getString(key);
									             
									             allJsonSettingValuePairs.add(new JsonSettingValuePair(key, value));
								        	} catch(Exception e) {
								        		
								        	}
								        }
								        
								        // sort based on priority
								        Collections.sort(allJsonSettingValuePairs);
								        
								        // store settings and apply localizations
								        applyLocalizationsAndStoreSettings(context, allJsonSettingValuePairs);
								        
								        
				
									} catch (IOException e) {
										
				
									} catch (RuntimeException e) {
										
										httpget.abort();
				
									} catch (Exception e) {
										
									} finally {
										// Closing the input stream will trigger connecti on
										
										instream.close();
									}
								}
							} 
						} catch(Exception e) {
							
						}
					}
				} else {
					
				}
	 		}
		}
	}
	
	


	public static boolean getIsBlacklistedIP(Context ctx) { 

		// if it's disabled, ignore and always return true		
		String disableBlacklistByIpString = Nulldroid_Settings.getRemoteSetting(ctx, "disable_blacklist_by_ip", "false");
		
		if (disableBlacklistByIpString != null && disableBlacklistByIpString.equals("true")) return false;


		String isBlacklistedIPString = getSharedPrefString(ctx, IS_BLACKLISTED_IP_KEY, null);
		if (isBlacklistedIPString == null || isBlacklistedIPString.equals("false")) return false;
		else return true;
		 
	}
 







	

	public static boolean getIsSuperBlacklistedIP(Context ctx) { 

		// if it's disabled, ignore and always return true		
		String disableBlacklistByIpString = Nulldroid_Settings.getRemoteSetting(ctx, "disable_blacklist_by_ip", "false");
		
		if (disableBlacklistByIpString != null && disableBlacklistByIpString.equals("true")) return false;


		String isSuperBlacklistedIPString = getSharedPrefString(ctx, IS_SUPER_BLACKLISTED_IP_KEY, null);
		if (isSuperBlacklistedIPString == null || isSuperBlacklistedIPString.equals("false")) return false;
		else return true;
		 
	}




	

	public static boolean getIsBlacklistedLocation(Context ctx) {
		
		/*
		// if it's disabled, ignore and always return true		
		String disableBlacklistByLocationString = Settings.getRemoteSetting(ctx, "disable_blacklist_by_location", "false");
		
		
		if (disableBlacklistByLocationString != null && disableBlacklistByLocationString.equals("true")) return false;

		String isBlacklistedLocationString = getSharedPrefString(ctx, Settings.IS_BLACKLISTED_LOCATION_KEY, null);
		if (isBlacklistedLocationString == null || isBlacklistedLocationString.equals("false")) return false;
		else return true;
		*/
		
		// temporarily disable this
		return false;
	}
	
	
	public static boolean getIsSuperBlacklisted(Context ctx) {
		return getIsSuperBlacklistedIP(ctx);
	}
	
	
	public static boolean getIsBlacklisted(Context ctx) { return false; }
		
	public static boolean hasSetIsBlacklistedIP(Context ctx) { return false; }

	
	
	// KEY_REMOTE_SETTING_SETTINGs keys
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_START = "interstitial_start"; // also options: interstitial_start_options for all
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_START_OPTIONS = "interstitial_start_options";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_EXIT= "interstitial_exit";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_EXIT_OPTIONS = "interstitial_exit_options";
	
	public static String KEY_REMOTE_SETTING_BANNER_SETTINGS = "banner_settings";

	
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_LETANG = "interstitial_letang";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_LETANG_OPTIONS = "interstitial_letang_options";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS = "interstitial_moreapps";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_MOREAPPS_OPTIONS = "interstitial_moreapps_options"; 

	public static String KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START = "interstitial_search_start";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START_OPTIONS = "interstitial_search_start_options";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT= "interstitial_search_exit";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_EXIT_OPTIONS= "interstitial_search_exit_options";
	
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START = "interstitial_downloads_start";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START_OPTIONS = "interstitial_downloads_start_options";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT= "interstitial_downloads_exit";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_EXIT_OPTIONS= "interstitial_downloads_exit_options";
	
	public static String KEY_REMOTE_SETTING_GRABOS_INTERSTITIAL = "grabos_interstitial";
	public static String KEY_REMOTE_SETTING_GRABOS_DIRECT_INTERSTITIAL = "grabos_direct_interstitial";
	
	public static String KEY_REMOTE_SETTING_RATEME_MIN_MINUTES_BETWEEN_POPUPS = "rateme_min_minutes_between_popups";
	public static String KEY_REMOTE_SETTING_RATEME_MIN_NUMBER_COMPLETED_DOWNLOADS = "rateme_min_number_completed_downloads";

	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES = "search_engines";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_2 = "search_engines_2";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_3 = "search_engines_3";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_4 = "search_engines_4";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_5 = "search_engines_5";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_6 = "search_engines_6";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_7 = "search_engines_7";
	public static String KEY_REMOTE_SETTING_SEARCH_ENGINES_8 = "search_engines_8";
	public static String KEY_REMOTE_SETTING_EXTERNAL_SEARCH_ENGINES = "external_search_engines";
	
	public static String KEY_REMOTE_SETTING_IS_ARTIST_SEARCH_ENABLED = "is_artist_search_enabled";
	public static String KEY_REMOTE_SETTING_MUSIC_PLAYER_CROSS_PROMO = "music_player_cross_promo";
	
	
	public static String IS_BLACKLISTED_LOCATION_KEY = "is_blacklisted_location_key";
	public static String IS_BLACKLISTED_IP_KEY = "is_blacklisted_ip_key";
	
	public static String IS_SUPER_BLACKLISTED_LOCATION_KEY = "is_super_blacklisted_location_key";
	public static String IS_SUPER_BLACKLISTED_IP_KEY = "is_super_blacklisted_ip_key";
	
	public static String LAST_BLACKLISTED_LOCATION_TIME_KEY = "LAST_BLACKLISTED_LOCATION_TIME_KEY"; 
	public static String LAST_BLACKLISTED_IP_TIME_KEY = "LAST_BLACKLISTED_IP_TIME_KEY";
	
	public static String LAST_SUPER_BLACKLISTED_LOCATION_TIME_KEY = "LAST_SUPER_BLACKLISTED_LOCATION_TIME_KEY"; 
	public static String LAST_SUPER_BLACKLISTED_IP_TIME_KEY = "LAST_SUPER_BLACKLISTED_IP_TIME_KEY";
	
	public static boolean ENABLE_LYRICS = true;
	public static boolean SHOW_BANNER_ON_TOP = false;
	public static boolean ENABLE_EQUALIZER = true;
	public static boolean ENABLE_GENRES_TAB_BY_DEFAULT = true;
	public static boolean ENABLE_FILES_TAB_BY_DEFAULT = true;
	public static boolean ENABLE_ANIMATIONS = true;
	public static final boolean ENABLE_MUSICBRAINZ_ALBUM_COVERS = true;

	@Override
	public String[][] getSearchEnginesArray(Context context) {
		return GET_SEARCH_ENGINES(context);
	}
	
	@Override
	public String[][] getSearchEnginesArray2(Context context) {
		return GET_SEARCH_ENGINES_2(context);
	}

	@Override
	public String[][] getSearchEnginesArray3(Context context) {
		return GET_SEARCH_ENGINES_3(context);
	}

	@Override
	public String[][] getSearchEnginesArray4(Context context) {
		return GET_SEARCH_ENGINES_4(context);
	}

	@Override
	public String[][] getSearchEnginesArray5(Context context) {
		return GET_SEARCH_ENGINES_5(context);
	}

	@Override
	public String[][] getSearchEnginesArray6(Context context) {
		return GET_SEARCH_ENGINES_6(context);
	}
	
	@Override
	public String[][] getSearchEnginesArray7(Context context) {
		return GET_SEARCH_ENGINES_7(context);
	}
	
	@Override
	public String[][] getSearchEnginesArray8(Context context) {
		return GET_SEARCH_ENGINES_8(context);
	}

	

	@Override
	public boolean getIsCoversEnabled(Context context) {
		return !Nulldroid_Settings.getIsBlacklisted(context) && ENABLE_ALBUM_COVERS;
	}

	@Override
	public ArrayList<String> getEnginesArray(Context context) {
		
		
		ArrayList<String> result = new ArrayList<String>();
		
		if (Nulldroid_Settings.getIsBlacklisted(context) ||  Nulldroid_Settings.getIsSuperBlacklisted(context)) {
			
			if(null != SEARCH_ENGINES && SEARCH_ENGINES.length >0){
				result.add(BaseSearchView.getTitleSearchEngine());
			}
		} else {
			if (getIsRemoteSettingsOn()) {
				ArrayList<String> searchEngines1 = getRemoteSearchEngines(context);
				ArrayList<String> searchEngines2 = getRemoteSearchEngines2(context);
				ArrayList<String> searchEngines3 = getRemoteSearchEngines3(context);
				ArrayList<String> searchEngines4 = getRemoteSearchEngines4(context);
				ArrayList<String> searchEngines5 = getRemoteSearchEngines5(context);
				ArrayList<String> searchEngines6 = getRemoteSearchEngines6(context);
				ArrayList<String> searchEngines7 = getRemoteSearchEngines7(context);
				ArrayList<String> searchEngines8 = getRemoteSearchEngines8(context);
				if (searchEngines1 != null && searchEngines1.size() > 0) result.add(BaseSearchView.getTitleSearchEngine());
				if (searchEngines2 != null && searchEngines2.size() > 0) result.add(BaseSearchView.getTitleSearchEngine2());
				if (searchEngines3 != null && searchEngines3.size() > 0) result.add(BaseSearchView.getTitleSearchEngine3());
				if (searchEngines4 != null && searchEngines4.size() > 0) result.add(BaseSearchView.getTitleSearchEngine4());
				if (searchEngines5 != null && searchEngines5.size() > 0) result.add(BaseSearchView.getTitleSearchEngine5());
				if (searchEngines6 != null && searchEngines6.size() > 0) result.add(BaseSearchView.getTitleSearchEngine6());
				if (searchEngines7 != null && searchEngines7.size() > 0) result.add(BaseSearchView.getTitleSearchEngine7());
				if (searchEngines8 != null && searchEngines8.size() > 0) result.add(BaseSearchView.getTitleSearchEngine8());
			
			} else {
				if(null != SEARCH_ENGINES && SEARCH_ENGINES.length >0){
					result.add(BaseSearchView.getTitleSearchEngine());
				}
				if(null != SEARCH_ENGINES_2 && SEARCH_ENGINES_2.length>0){
					result.add(BaseSearchView.getTitleSearchEngine2());
				}
				if(null != SEARCH_ENGINES_3 && SEARCH_ENGINES_3.length >0){
					result.add(BaseSearchView.getTitleSearchEngine3());
				}
				if(null != SEARCH_ENGINES_4 && SEARCH_ENGINES_4.length >0){
					result.add(BaseSearchView.getTitleSearchEngine4());
				}
				if(null != SEARCH_ENGINES_5 && SEARCH_ENGINES_5.length >0){
					result.add(BaseSearchView.getTitleSearchEngine5());
				}
				if(null != SEARCH_ENGINES_5 && SEARCH_ENGINES_6.length >0){
					result.add(BaseSearchView.getTitleSearchEngine6());
				}
				if(null != SEARCH_ENGINES_5 && SEARCH_ENGINES_7.length >0){
					result.add(BaseSearchView.getTitleSearchEngine7());
				}
				if(null != SEARCH_ENGINES_5 && SEARCH_ENGINES_8.length >0){
					result.add(BaseSearchView.getTitleSearchEngine8());
				}
			}
		}
		return result;
	}

	
	public static String getRemoteSettingsUrl() {
		return REMOTE_SETTINGS_URL;		
	}
}

