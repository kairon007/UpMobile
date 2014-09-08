package ru.johnlife.lifetoolsmp3.song;

import java.util.ArrayList;


public class RemoteSong extends Song {
	protected String downloadUrl;
	public ArrayList<String []> headers;

	public RemoteSong(String downloadUrl) {
		super(downloadUrl.hashCode());
		this.downloadUrl = downloadUrl;
	}
	
	protected RemoteSong(long id) {
		super(id);
		downloadUrl = null;
	}

	public RemoteSong setTitle(String songTitle) {
		title = songTitle;
		return this;
	}

	public RemoteSong setArtistName(String songArtist) {
		artist = songArtist;
		return this;
	}
	
	public RemoteSong setDuration (Long songDuration) {
		duration = songDuration;
		return this;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getParentUrl() {
		return downloadUrl;
	}
	
	public RemoteSong setHeader(ArrayList<String []> headers) {
		this.headers = headers;
		return this;
	}
	public ArrayList<String []> getHeaders() {
		return headers;
	}	
}
