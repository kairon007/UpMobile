package org.kreed.vanilla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import ru.johnlife.lifetoolsmp3.engines.BaseSettings;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;


public class Settings implements BaseSettings {
	
	
	
	// if you need to access SEARCH_ENGINES, call the public static method GET_SEARCH_ENGINES()  
	private static final String[][] SEARCH_ENGINES = new String[][] { 
		//{"SearchVmusice", "1"}, 
		{"SearchPleer", "2"},
		{"SearchPoisk", "1"}, 
		{"SearchHulkShare", "1"}, 
		{"SearchMp3skull", "1"},
		{"SearchMp3World", "1"}, 
		{"SearchSoundCloud", "1"},  
		{"SearchSoundCloudV2", "1"}, 
		{"SearchSogou", "1"},
		{"SearchGrooveshark", "1"}, 
		{"SearchTing", "1"}, 
		{"SearchZaycev", "7"} 
	};
	
	
	
	
	///////////////
 

	public static String REMOTE_SETTINGS_URL ="";   
	public static boolean ENABLE_ADS = true; 
	public static int REMOTE_SETTINGS_MIN_UPDATE_INTERVAL_MILLIS = 9999999;//30000; //28800000;    
	public static int RATE_ME_POPUP_DELAY_MILLIS = 9999999;//30000; 
	private static final boolean ENABLE_ALBUM_COVERS = true;	 
	public static final boolean ENABLE_SHOW_ALBUM_COVERS_IN_LIBRARY_TAB = false;

	// AD NETWORK ID'S
	public static String APPNEXT_ID = "85acb334-cdb4-4a5b-b434-4dc722c6a97f"; 
	public static String MOBILECORE_ID = "6AFPIUJW9K2IAJMIJ41605AI1UJUY";
	public static String MOPUB_ID_BANNER = "606c9343ef8849e79f9636fde1889428";     
	public static String MOPUB_ID_INTERSTITIAL = "22f809781d7d414abb7236c6044a922e";
	public static String STARTAPP_DEV_ID = "107671050";
	public static String STARTAPP_APP_ID = "207135275";	
	public static String VUNGLE_ID = "53d5c18be262df2c4200002a"; 


	// blacklisted song items (dmca)
	public static String[] BLACKLISTED_SONGS_AND_ARTISTS = {};
	// public static String[] BLACKLISTED_SONGS_AND_ARTISTS = {"trilla t", "mitchie rikzu", "razzykill", "ana carolina", "renato russo", "tom jobim", "jobim", "marisa monte", "monte", "marisa", "renato russo", "renato", "russo", "michel telo", "telo", "elis regina", "elis", "caetano veloso", "caetano", "veloso", "chico buarque", "buarque", "andre matos", "matos", "andre", "gilberto gil", "gilberto", "joao gilberto", "joao", "ivete sangalo", "ivete", "sangalo", "roberto carlos", "cazuza", "cassia eller", "cassia", "eller", "paula fernandes", "zizi possi", "zizi", "possi", "simone", "gusttavo lima", "gusttavo", "lima", "tim maia", "maia", "lisa ono", "maria bethania", "bethania", "adriana calcanhotto", "adriana", "calcanhotto", "astrud gilberto", "astrud", "bebel gilberto", "gilberto", "bebel", "max cavalera", "cavalera", "luan santana", "luan", "gal costa", "daniela mercury", "seu jorge", "ana carolina", "carolina", "ivan lins", "martinho da vila", "martinho", "raul seixas", "seixas", "claudia leitte", "leitte", "samuel rosa", "jorge ben jor", "zeze di camargo", "zeze", "camargo", "sergio mendes", "moraes moreira", "moraes", "moreira", "roberta sa", "dinho", "joanna", "oswaldo montenegro", "oswaldo", "montenegro", "jesse", "edu falaschi", "falaschi", "carlinhos brown", "carlinhos", "djavan", "vanessa da mata", "da mata", "nara leao", "nara", "leao", "geraldo vandre", "vandre", "mano brown", "tom ze", "jorge aragao", "aragao", "dick farney", "carney", "psirico", "valesca popzuda", "valesca", "popzuda", "lucas lucco", "lucco", "shakira", "claudia leitte", "leitte", "fernando", "sorocaba", "jason derulo", "derulo", "henrique", "juliano", "bastille", "gusttavo lima", "cristiano araujo", "gusttavo", "araujo", "fernandes", "maria cecilia", "cecilia", "rodolfo", "luan santana", "anitta", "zeze di camargo", "zeze", "camargo", "sorriso maroto", "sorriso", "maroto", "marrone", "ricardo", "joao", "guilherme", "naldo benny", "naldo", "benny", "ivete sangalo", "ivete", "turma do pagode", "turma", "pagode", "sorriso maroto", "sorriso", "maroto", "thiaguinho", "martin garrix", "garrix", "sorocaba", "gusttavo", "lima", "bruno", "sorriso maroto", "moroto", "sorriso", "thiaguinho", "michel telo", "anitta", "santana", "mateus", "cesar menotti", "menotti", "fabiano", "gabriel valim", "valim", "michel telo", "telo", "detonautas", "lucas lucco", "lucco"};
	

	
	
	
	
	
	
	

	public static String getDefaultValueForRemoteSettingIfSet(String property) { 
		String value = null;
		// test values
		if (property.equals(KEY_REMOTE_SETTING_IS_ARTIST_SEARCH_ENABLED)) {  
			return "true"; 
		// mp3 search engine settings
			
		} else if (property.equals(KEY_REMOTE_SETTING_SEARCH_ENGINES)) {    
			value = "[\"zaycev\", \"pleer\"]"; //value = "[\"xiami\"]";         
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
			value = "[{grabos:720},{grabos_direct:720},{mobilecore:0}]";
			//value = "[{grabos:2}, {grabos_direct:2}, {mobilecore_stickeez:2}, {mobilecore:0}]";
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_START_OPTIONS)) {  
			value = "{\"initial_delay\": 1, \"min_interval\": 10}";    
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START)) {        
			value = "[{appnext:120},{vungle:1440},{mobilecore:0}]"; 
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_SEARCH_START_OPTIONS)) {  
			value = "{\"initial_delay\": 3, \"min_interval\": 5}";   
			
			
		} else if (property.equals(KEY_REMOTE_SETTING_INTERSTITIAL_DOWNLOADS_START)) {         
			value = "[{mobilecore:0}]";
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
			value = "[{mobilecore:0}]";
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
		return !Settings.getIsBlacklisted(context) && ENABLE_ALBUM_COVERS;
	}	

	// this method tries to return remote search_engines if it exists, otherwise, it returns the default search egnines (SEARCH_ENGINES)
	public static String[][] GET_SEARCH_ENGINES(Context context) {
		String[][] ret = SEARCH_ENGINES;
		
		ArrayList<String[]> searchEngineTuples = new ArrayList<String[]>();
		if (Settings.getIsBlacklisted(context)) {
			
			String[] searchEngineTuple = {"SearchSoundCloud", "1"};
			searchEngineTuples.add(searchEngineTuple); 
			
		} else if (getIsRemoteSettingsOn()) {
			
			
			// for remote settings
			
		
			// get search engines from remote settings if it exists
			ArrayList<String> searchEnginesNames = getSearchEngines(context);
			 
			
			for (String searchEngineName : searchEnginesNames) {
				try {
					String[] searchEngineTuple = {"0", "0"};
					if (searchEngineName != null) {
						if (searchEngineName.equals("grooveshark")) {
							searchEngineTuple[0] = "SearchGrooveshark";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("zaycev")) {
							searchEngineTuple[0] = "SearchZaycev";
							searchEngineTuple[1] = "7";
						} else if (searchEngineName.equals("hulkshare")) { 
							searchEngineTuple[0] = "SearchHulkShare";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("mp3skull")) {
							searchEngineTuple[0] = "SearchMp3skull";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("mp3world")) {
							searchEngineTuple[0] = "SearchMp3World";
							searchEngineTuple[1] = "1"; 
						} else if (searchEngineName.equals("pleer")) {
							searchEngineTuple[0] = "SearchPleer";
							searchEngineTuple[1] = "2";
						} else if (searchEngineName.equals("poisk")) {
							searchEngineTuple[0] = "SearchPoisk";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("sogou")) {
							searchEngineTuple[0] = "SearchSogou";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("soundcloud")) {
							searchEngineTuple[0] = "SearchSoundCloud";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("soundcloudv2")) {
							searchEngineTuple[0] = "SearchSoundCloudV2";
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("baidu")) {
							searchEngineTuple[0] = "SearchTing"; 
							searchEngineTuple[1] = "1";
						} else if (searchEngineName.equals("vmusice")) { 
							searchEngineTuple[0] = "SearchVmusice";
							searchEngineTuple[1] = "1";
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


	
	

	
	// paradise / maniac
	public static ArrayList<String> getSearchEngines(Context context) {
		return getEngines(context, Settings.KEY_REMOTE_SETTING_SEARCH_ENGINES);
	}
	public static ArrayList<String> getExternalSearchEngines(Context context) {
		return getEngines(context, Settings.KEY_REMOTE_SETTING_EXTERNAL_SEARCH_ENGINES);
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
			String remoteSettingSearchEngines = Settings.getRemoteSetting(context, remoteSetting, null);
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
	
	

	public static void updateSettingsAsyncTask(Context context, String serverUrl, int minUpdateIntervalMillis, int connectionTimeoutMillis, int socketTimeoutMillis) { // NOT ASYNCTASK. so you should call it in an asynctask
		try {
			if (serverUrl != null && !serverUrl.equals("")) {
				UpdateSettingsAsyncTask task = new UpdateSettingsAsyncTask(context, serverUrl, minUpdateIntervalMillis, connectionTimeoutMillis, socketTimeoutMillis); 
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
		
		
		public UpdateSettingsAsyncTask(Context ctx, String serverUrl, int minUpdateIntervalMillis, int connectionTimeoutMillis, int socketTimeoutMillis) {
			mContext = ctx;
			mServerUrl = serverUrl;
			mMinUpdateIntervalMillis = minUpdateIntervalMillis;
			mConnectionTimeoutMillis = connectionTimeoutMillis;
			mSocketTimeoutMillis = socketTimeoutMillis;
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
									    
									    // store everything into getSharedPRefString
								        while( keys.hasNext() ){
								        	try {
									            String key = (String)keys.next(); 
									             String value = jObject.getString(key);
									             
									             putSharedPrefString(context, key, value); 
								        	} catch(Exception e) {
								        		
								        	}
								        }
										
										
				
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
		
		String isBlacklistedIPString = getSharedPrefString(ctx, IS_BLACKLISTED_IP_KEY, null);
		if (isBlacklistedIPString == null || isBlacklistedIPString.equals("false")) return false;
		else return true;
		 
	}
 
	public static boolean getIsBlacklisted(Context ctx) {      
		
		
		return getIsBlacklistedIP(ctx) || getIsBlacklistedLocation(ctx);     
		   
	}
	
	

	public static boolean getIsBlacklistedLocation(Context ctx) {
		
		String isBlacklistedLocationString = getSharedPrefString(ctx, Settings.IS_BLACKLISTED_LOCATION_KEY, null);
		if (isBlacklistedLocationString == null || isBlacklistedLocationString.equals("false")) return false;
		else return true;
		
	}
	
	
	// KEY_REMOTE_SETTING_SETTINGs keys
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_START = "interstitial_start"; // also options: interstitial_start_options for all
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_START_OPTIONS = "interstitial_start_options";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_EXIT= "interstitial_exit";
	public static String KEY_REMOTE_SETTING_INTERSTITIAL_EXIT_OPTIONS = "interstitial_exit_options";
	
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
	public static String KEY_REMOTE_SETTING_EXTERNAL_SEARCH_ENGINES = "external_search_engines";
	
	public static String KEY_REMOTE_SETTING_IS_ARTIST_SEARCH_ENABLED = "is_artist_search_enabled";
	public static String KEY_REMOTE_SETTING_MUSIC_PLAYER_CROSS_PROMO = "music_player_cross_promo";
	
	
	public static String IS_BLACKLISTED_LOCATION_KEY = "is_blacklisted_location_key";
	public static String IS_BLACKLISTED_IP_KEY = "is_blacklisted_ip_key"; 
	
	public static String LAST_BLACKLISTED_LOCATION_TIME_KEY = "LAST_BLACKLISTED_LOCATION_TIME_KEY"; 
	public static String LAST_BLACKLISTED_IP_TIME_KEY = "LAST_BLACKLISTED_IP_TIME_KEY";
	
	public static boolean ENABLE_LYRICS = true;
	public static boolean SHOW_BANNER_ON_TOP = false;
	public static boolean ENABLE_EQUALIZER = true;
	public static boolean ENABLE_GENRES_TAB_BY_DEFAULT = true;
	public static boolean ENABLE_FILES_TAB_BY_DEFAULT = true;
	public static final boolean ENABLE_MUSICBRAINZ_ALBUM_COVERS = true;
	
	
	@Override
	public String[][] getSearchEnginesArray(Context context) {
		return GET_SEARCH_ENGINES(context);
	}




	@Override
	public boolean getIsCoversEnabled(Context context) {
		return !Settings.getIsBlacklisted(context) && ENABLE_ALBUM_COVERS && getSharedPrefs(context).getBoolean(PrefKeys.DISABLE_COVER_ART, true);
	}
}
