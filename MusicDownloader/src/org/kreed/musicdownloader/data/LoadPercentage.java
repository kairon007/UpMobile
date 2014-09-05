package org.kreed.musicdownloader.data;

public class LoadPercentage {
	String progress;
	String currentDownloadingSongTitle;

	public String getCurrentDownloadingSongTitle() {
		return currentDownloadingSongTitle;
	}

	public void setCurrentDownloadingSongTitle(String currentDownloadingSongTitle) {
		this.currentDownloadingSongTitle = currentDownloadingSongTitle;
	}

	public void loadPercentage() {
	}

	public void loadPercentage(String progress) {
		this.progress = progress;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

}
