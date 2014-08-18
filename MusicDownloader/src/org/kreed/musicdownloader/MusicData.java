package org.kreed.musicdownloader;

import android.graphics.Bitmap;

public class MusicData {
	private String songArtist;
	private String songTitle;
	private String songDuration;
	private Bitmap songBitmap;
	private String downloadProgress;
	private String songGenre;
	private long downloadId;
	private String fileUri;
	private String filePathSD;

	public MusicData() {
	}

	public MusicData(String songArtist, String songTitle, String songDuration,
			Bitmap songBitmap) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.songBitmap = songBitmap;
	}

	public MusicData(String songArtist, String songTitle, String songDuration,String downloadProgress, Bitmap cover) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.downloadProgress = downloadProgress;
		this.songBitmap = cover;
	}
	
	public MusicData(String songArtist, String songTitle, String songDuration, Bitmap cover, String songGenre) {
		this.songArtist = songArtist;
		this.songTitle = songTitle;
		this.songDuration = songDuration;
		this.songBitmap = cover;
		this.songGenre = songGenre;
	}

	public String getSongArtist() {
		return songArtist;
	}

	public void setSongArtist(String songArtist) {
		this.songArtist = songArtist;
	}

	public String getSongTitle() {
		return songTitle;
	}

	public void setSongTitle(String songTitle) {
		this.songTitle = songTitle;
	}

	public String getSongDuration() {
		return songDuration;
	}

	public void setSongDuration(String songDuration) {
		this.songDuration = songDuration;
	}

	public Bitmap getSongBitmap() {
		return songBitmap;
	}

	public void setSongBitmap(Bitmap songBitmap) {
		this.songBitmap = songBitmap;
	}

	public String getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(String downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	public void setDownloadId(long downloadId) {
		this.downloadId = downloadId;
	}
	
	public long getDownloadId() {
		return downloadId;
	}

	public String getSongGenre() {
		return songGenre;
	}

	public void setSongGenre(String songGenre) {
		this.songGenre = songGenre;
	}

	public String getFileUri() {
		return fileUri;
	}

	public void setFileUri(String fileUri) {
		this.fileUri = fileUri;
	}

	public synchronized String getFilePathSD() {
		return filePathSD;
	}

	public synchronized void setFilePathSD(String filePathSD) {
		this.filePathSD = filePathSD;
	}
	
}
