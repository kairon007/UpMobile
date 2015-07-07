package ru.johnlife.lifetoolsmp3;

public interface Constants {
	
	public static final int NOTIFICATION_ID = 2101;
	
	
	public static final int COUNT_FRAGMENT = 6;
	public static final int SEARCH_FRAGMENT = 0;
	public static final int DOWNLOADS_FRAGMENT = 1;
	public static final int PLAYLIST_FRAGMENT = 2;
	public static final int LIBRARY_FRAGMENT = 3;
	public static final int PLAYER_FRAGMENT = 4;
	public static final int SETTINGS_FRAGMENT = 5;
	
	public static final int FULL_DRAVER_SIZE = 7;
	public static final int LESS_DRAVER_SIZE = 6;

	public static String MAIN_ACTION = "playbackservice.action.main";
	public static String PLAY_ACTION = "playbackservice.action.play";
	public static String NEXT_ACTION = "playbackservice.action.next";
	public static String CLOSE_ACTION = "playbackservice.action.close";
	
	public static final String PREF_LAST_PLAYLIST_ID = "pref.last.played.playlist.id";
	public static final String EXTRA_DATA = "extra.data.to.service.helper";
	
	public static final String EMPTY_STRING = "";
	public static final String AUDIO_END = ".mp3";
	
	public static final String PREF_SHOW_INFO_MESSAGE = "pref.show.info.message";
	
}
