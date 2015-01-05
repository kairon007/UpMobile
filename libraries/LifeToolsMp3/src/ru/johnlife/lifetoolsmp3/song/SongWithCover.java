package ru.johnlife.lifetoolsmp3.song;

import android.os.Parcel;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;

public abstract class SongWithCover extends RemoteSong {
	
	private boolean useCached = true;
	
	public SongWithCover(long id) {
		super(id);
	}

	public SongWithCover (Parcel parcel) {
		super(parcel);
	}
	
	public SongWithCover(String downloadUrl) {
		super(downloadUrl);
		setHasCoverFromSearch(true);
	}
	
	public abstract String getLargeCoverUrl();
	
	@Override
	protected CoverLoaderTask getCoverLoader() {
		return useCached ? new CoverLoaderTask(getLargeCoverUrl()) : super.getCoverLoader();
	}
	
	@Override
	protected void clearCoverLoaderQueue() {
		useCached = true;
		super.clearCoverLoaderQueue();
	}
	
	@Override
	protected void useNextCoverLoader() {
		if (useCached) {
			useCached = !useCached;
		} else {
			super.useNextCoverLoader();
		}
	}
	
	@Override
	public void setHasCoverFromSearch(boolean hasCoverFromSearch) {
		super.setHasCoverFromSearch(hasCoverFromSearch);
	}
}
