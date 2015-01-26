package org.upmobile.musix.models;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Gustavo on 02/07/2014.
 */
public class Song {
	private long id;
	private String title;
	private String artistName;
	private String albumTitle;
	private Bitmap albumCoverArt;
	private long albumId;
	private String genre;
	private Uri uri;

	// constructor
	public Song(long songId, String songTitle, String artistName, String albumTitle, long albumId, Bitmap coverArt, String genre, Uri uri) {

		this.setId(songId);
		this.setTitle(songTitle);
		this.setArtistName(artistName);
		this.setAlbumTitle(albumTitle);
		this.setAlbumId(albumId);
		this.setAlbumCoverArt(coverArt);
		this.setGenre(genre);
		this.setUri(uri);

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getAlbumTitle() {
		return albumTitle;
	}

	public void setAlbumTitle(String albumTitle) {
		this.albumTitle = albumTitle;
	}

	public Bitmap getAlbumCoverArt() {
		return albumCoverArt;
	}

	public void setAlbumCoverArt(Bitmap albumCoverArt) {
		this.albumCoverArt = albumCoverArt;
	}

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}
}
