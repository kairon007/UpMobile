package ru.johnlife.lifetoolsmp3.song;

import java.util.ArrayList;

import android.database.Cursor;

public class PlaylistData {
	
	private long id;
	
	private String name;
	
	private ArrayList<MusicData> songs;
	
	public void populate(Cursor cursor) {
		id = cursor.getLong(0);
		name = cursor.getString(1);
	}
	
	public PlaylistData() {
	}
	
	public PlaylistData(int id, String name, ArrayList<MusicData> songs) {
		this.setId(id);
		this.setName(name);
		this.setSongs(songs);
	}

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<MusicData> getSongs() {
		return songs;
	}

	public void setSongs(ArrayList<MusicData> songs) {
		this.songs = songs;
	}
	
}
