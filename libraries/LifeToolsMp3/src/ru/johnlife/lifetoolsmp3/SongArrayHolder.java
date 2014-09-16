package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;
import java.util.Iterator;

import ru.johnlife.lifetoolsmp3.engines.Engine;
import ru.johnlife.lifetoolsmp3.song.Song;
import ru.johnlife.lifetoolsmp3.ui.OnlineSearchView;

public class SongArrayHolder {
	private static SongArrayHolder instance = null;
	private ArrayList<Song> results = null;
	private String songName;
	private Iterator<Engine> taskIterator;

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
		for (int i = 0; i < searchView.getResultAdapter().getCount(); i++) {
			Song song = searchView.getResultAdapter().getItem(i);
			results.add(song);
		}
		SongArrayHolder.getInstance().setSongName(searchView.getSearchField().getText().toString());
		SongArrayHolder.getInstance().setTaskIterator(searchView.getTaskIterator());
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

}
