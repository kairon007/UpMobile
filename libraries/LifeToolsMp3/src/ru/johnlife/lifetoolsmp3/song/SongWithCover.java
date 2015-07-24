package ru.johnlife.lifetoolsmp3.song;

import android.os.Parcel;

import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.SimpleCoverLoaderTask;

public abstract class SongWithCover extends RemoteSong {

    private boolean useCached = true;

    public SongWithCover(long id) {
        super(id);
    }

    public SongWithCover(Parcel parcel) {
        super(parcel);
    }

    public SongWithCover(String downloadUrl) {
        super(downloadUrl);
        setHasCoverFromSearch(true);
    }

    public abstract String getLargeCoverUrl();

    @Override
    protected CoverLoaderTask getTask() {
        return useCached ? new SimpleCoverLoaderTask(getLargeCoverUrl(), null, null, this) : super.getTask();
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
