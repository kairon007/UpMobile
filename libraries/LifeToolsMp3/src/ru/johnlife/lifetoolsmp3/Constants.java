package ru.johnlife.lifetoolsmp3;

public interface Constants {
	
	int NOTIFICATION_ID = 2101;

	int COUNT_FRAGMENT = 6;
	int SEARCH_FRAGMENT = 0;
	int DOWNLOADS_FRAGMENT = 1;
	int PLAYLIST_FRAGMENT = 2;
	int LIBRARY_FRAGMENT = 3;
	int PLAYER_FRAGMENT = 4;
	int SETTINGS_FRAGMENT = 5;
	
	int FULL_DRAVER_SIZE = 7;
	int LESS_DRAVER_SIZE = 6;

	String MAIN_ACTION = "playbackservice.action.main";
	String PLAY_ACTION = "playbackservice.action.play";
	String NEXT_ACTION = "playbackservice.action.next";
	String CLOSE_ACTION = "playbackservice.action.close";
	
	String PREF_LAST_PLAYLIST_ID = "pref.last.played.playlist.id";
	String EXTRA_DATA = "extra.data.to.service.helper";
	
	String EMPTY_STRING = "";
	String AUDIO_END = ".mp3";
	
	String PREF_SHOW_INFO_MESSAGE = "pref.show.info.message";
	
}
