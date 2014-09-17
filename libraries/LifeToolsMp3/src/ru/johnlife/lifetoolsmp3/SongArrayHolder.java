package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;
import java.util.Iterator;

import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;
import ru.johnlife.lifetoolsmp3.ui.Player;
import android.os.Bundle;

public class SongArrayHolder {
	private static SongArrayHolder instance = null;
	private ArrayList<Song> results = null;
	private String songName;
	private Iterator<Engine> taskIterator;
	private boolean isStreamDialogOpened = false;
	private boolean isID3DialogOpened = false;
	private String[] fields;
	private boolean isCoverEnabled = true;
	private Bundle streamDialogArgs;
	private Player playerInstance;
	private boolean isLyricsOpened;
	private String[] titleArtistLyrics;
	private String lyrics;
	private boolean isDirectoryChooserOpened = false;
	private String directoryChooserPath;

	public static SongArrayHolder getInstance() {
		if (instance == null) {
			instance = new SongArrayHolder();
		}
		return instance;
	}

	public ArrayList<Song> getResults() {
		if (results != null) {
		}
		return results;
	}

	public void setResults(ArrayList<Song> results) {
		this.results = results;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public Iterator<Engine> getTaskIterator() {
		return taskIterator;
	}

	public void setTaskIterator(Iterator<Engine> taskIterator) {
		this.taskIterator = taskIterator;
	}

	public void getResultsFromAdapter(OnlineSearchView searchView) {
		results = new ArrayList<Song>();
		if (searchView != null && searchView.getResultAdapter() != null) {
			for (int i = 0; i < searchView.getResultAdapter().getCount(); i++) {
				Song song = searchView.getResultAdapter().getItem(i);
				results.add(song);
			}
			SongArrayHolder.getInstance().setSongName(searchView.getSearchField().getText().toString());
			SongArrayHolder.getInstance().setTaskIterator(searchView.getTaskIterator());
		}
	}

	public void setResultsToAdapter(OnlineSearchView searchView) {
		if (SongArrayHolder.getInstance().getResults() != null) {
			for (Song song : SongArrayHolder.getInstance().getResults()) {
				searchView.getResultAdapter().add(song);
			}
			searchView.setTaskIterator(SongArrayHolder.getInstance().getTaskIterator());
			searchView.getSearchField().setText(SongArrayHolder.getInstance().getSongName().toString());
			searchView.setCurrentName(SongArrayHolder.getInstance().getSongName().toString());
			searchView.getResultAdapter().notifyDataSetChanged();
			searchView.setSearchStopped(false);
		}
	}

	public boolean isStreamDialogOpened() {
		return isStreamDialogOpened;
	}

	public void setStreamDialogOpened(boolean isStreamDialogOpened, Bundle streamDialogArgs, Player player) {
		this.isStreamDialogOpened = isStreamDialogOpened;
		this.streamDialogArgs = streamDialogArgs;
		this.playerInstance = player;
	}
	
	public void setID3DialogOpened(boolean isID3DialogOpened, String[] fields, boolean isCoverEnabled) {
		this.isID3DialogOpened = isID3DialogOpened;
		this.fields = fields;
		this.isCoverEnabled = isCoverEnabled;
	}

	public Bundle getStreamDialogArgs() {
		return streamDialogArgs;
	}
	
	public Player getPlayerInstance() {
		return playerInstance;
	}

	public boolean isID3DialogOpened() {
		return isID3DialogOpened;
	}
	
	public String[] getID3Fields() {
		return fields;
	}

	public boolean isLyricsOpened() {
		return isLyricsOpened;
	}

	public void setLyricsOpened(boolean isLyricsOpened, String[] titleArtistLyrics) {
		this.isLyricsOpened = isLyricsOpened;
		this.titleArtistLyrics = titleArtistLyrics;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyricsString(String lyrics) {
		this.lyrics = lyrics;
	}

	public String[] getTitleArtistLyrics() {
		return titleArtistLyrics;
	}

	public boolean isDirectoryChooserOpened() {
		return isDirectoryChooserOpened;
	}

	public void setDirectoryChooserOpened(boolean isDirectoryChooserOpened) {
		this.isDirectoryChooserOpened = isDirectoryChooserOpened;
	}

	public String getDirectoryChooserPath() {
		return directoryChooserPath;
	}

	public void setDirectoryChooserPath(String directoryChooserPath) {
		this.directoryChooserPath = directoryChooserPath;
	}

	public boolean isCoverEnabled() {
		return isCoverEnabled;
	}

	public void setCoverEnabled(boolean isCoverEnabled) {
		this.isCoverEnabled = isCoverEnabled;
	}
}
