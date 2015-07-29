package ru.johnlife.lifetoolsmp3.engines.lyric;

import ru.johnlife.lifetoolsmp3.engines.lyric.BaseLyricsSearchTask.OnEnginesListener;
import android.os.AsyncTask;
import android.os.Build;

public class SearchLyrics implements OnEnginesListener {
	
	private OnLyricsFetchedListener fetchedListener;
	private String artist;
	private String title;
	private int enginesIndex = 0;
	private BaseLyricsSearchTask searchTask;
	
	private String[] engines = { 
			"AzLyrics", 
			"MetroLyrics", 
			"SongLyrics" };
	
	private BaseLyricsSearchTask getTask() {
		switch (enginesIndex) {
		case 0:
			return new AzLyrics(artist, title, this);
		case 1:
			return new MetroLyrics(artist, title, this);
		case 2:
			return new SongLyrics(artist, title, this);
		default:
			return new AzLyrics(artist, title, this);
		}
	}
	
	public SearchLyrics(OnLyricsFetchedListener fetchedListener, String artist, String title) {
		this.fetchedListener = fetchedListener;
		this.artist = artist;
		this.title = title;
	}
	
	public void startSearch() {
		searchTask = getTask();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
        	searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			searchTask.execute();
		}
		enginesIndex++;
	}
	
	public void cancelSearch() {
		if (null == searchTask) return;
		searchTask.cancel(true);
	}

	@Override
	public void OnEnginesFinished(boolean found, String lyrics) {
		if (searchTask.isCancelled()) return;
		if (found) {
			fetchedListener.onLyricsFetched(true, lyrics);
		} else if (enginesIndex < engines.length) {
			startSearch();
		} else {
			fetchedListener.onLyricsFetched(false, "");
		}
	}
	
}
