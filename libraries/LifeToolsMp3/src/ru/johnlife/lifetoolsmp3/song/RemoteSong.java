package ru.johnlife.lifetoolsmp3.song;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.LastFmCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask;

public class RemoteSong extends Song implements CoverLoaderTask.OnCoverTaskListener {

    private boolean hasCoverFromSearch = false;
    public static boolean tryMuzicBrainz = true;
    private int coverLoaderIndex = 0;
    private WeakReference<Bitmap> cover;
    public ArrayList<String[]> headers;
    private ArrayList<OnBitmapReadyListener> onBitmapReadyListeners = new ArrayList<>();
    protected List<DownloadUrlListener> downloadUrlListeners = new ArrayList<>();
    private static final Object lock = new Object();

    public RemoteSong(String downloadUrl) {
        super(downloadUrl.hashCode());
        this.downloadUrl = comment = downloadUrl;
    }

    public interface DownloadUrlListener {

        void success(String url);

        void error(String error);
    }

    public interface OnBitmapReadyListener {
        void onBitmapReady(Bitmap bmp);
    }

    protected RemoteSong(long id) {
        super(id);
        downloadUrl = comment = "-1";
    }

    public RemoteSong setSongTitle(String songTitle) {
        title = songTitle;
        return this;
    }

    public RemoteSong setArtistName(String songArtist) {
        artist = songArtist;
        return this;
    }

    public RemoteSong setDuration(Long songDuration) {
        duration = songDuration;
        return this;
    }

    public String getUrl() {
        if (null != downloadUrl && !downloadUrl.isEmpty()) {
            return downloadUrl;
        }
        Log.e(getClass().getSimpleName(), "Call getDownloadUrl() first!");
        return null;
    }

    @Override
    public boolean getDownloadUrl(DownloadUrlListener listener) {
        downloadUrlListeners.add(listener);
        if (null != downloadUrl && !downloadUrl.isEmpty() && null != downloadUrlListeners && downloadUrl.startsWith("http")) {
            listener.success(downloadUrl);
            return true;
        }
        return false;
    }

    public void addListener(DownloadUrlListener listener) {
        downloadUrlListeners.add(listener);
    }

    public void cancelTasks() {
        //do nothing, only for childs
    }

    public RemoteSong setHeader(ArrayList<String[]> headers) {
        this.headers = headers;
        return this;
    }

    public ArrayList<String[]> getHeaders() {
        return headers;
    }

    protected CoverLoaderTask getTask() {
        switch (coverLoaderIndex) {
            case 0:
                return new LastFmCoverLoaderTask("", artist, title, this);
            case 1:
                if (tryMuzicBrainz) {
                    return new MuzicBrainzCoverLoaderTask("", artist, title, this);
                } else {
                    return null;
                }
            default:
                clearCoverLoaderQueue();
                return null;
        }
    }

    public void getCover(OnBitmapReadyListener onBitmapReadyListener) {
        synchronized (lock) {
            onBitmapReadyListeners.add(onBitmapReadyListener);
            if (null != cover && null != cover.get()) {
                setBmp(cover.get());
                clearCoverLoaderQueue();
                return;
            }
            if (coverLoaderIndex != 0) return;
            startSearch();
        }
    }

    @Override
    public void onCoverTaskFinished(Bitmap bitmap) {
        if (bitmap != null) {
            setBmp(bitmap);
            clearCoverLoaderQueue();
        } else if (coverLoaderIndex < 2) {
            startSearch();
        } else {
            setBmp(null);
            clearCoverLoaderQueue();
        }
    }

    protected void useNextCoverLoader() {
        coverLoaderIndex++;
    }

    protected void clearCoverLoaderQueue() {
        coverLoaderIndex = 0;
    }

    private void startSearch() {
        CoverLoaderTask coverLoaderTask = getTask();
        if (null == coverLoaderTask) {
            setBmp(null);
            clearCoverLoaderQueue();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            coverLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            coverLoaderTask.execute();
        }
        useNextCoverLoader();
    }

    private void setBmp(Bitmap bmp) {
        synchronized (lock) {
            cover = new WeakReference<>(bmp);
            for (OnBitmapReadyListener listener : onBitmapReadyListeners) {
                if (null != listener) listener.onBitmapReady(bmp);
            }
            onBitmapReadyListeners.clear();
        }
    }

    public RemoteSong setDownloadUrl(String url) {
        downloadUrl = url;
        return this;
    }

    @Override
    public RemoteSong cloneSong() {
        try {
            return (RemoteSong) clone();
        } catch (CloneNotSupportedException e) {
            Log.e(getClass().getSimpleName(), "Can't clone RemoteSong: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 1;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeLong(id);
        parcel.writeString(path);
        parcel.writeString(title);
        parcel.writeString(album);
        parcel.writeLong(albumId);
        parcel.writeString(artist);
        parcel.writeLong(artistId);
        parcel.writeLong(duration);
        parcel.writeInt(trackNumber);
        parcel.writeString(downloadUrl);
        parcel.writeString(comment);
    }

    public RemoteSong(Parcel parcel) {
        super(parcel);
    }

    public boolean isHasCoverFromSearch() {
        return hasCoverFromSearch;
    }

    public void setHasCoverFromSearch(boolean hasCoverFromSearch) {
        this.hasCoverFromSearch = hasCoverFromSearch;
    }

    public static final Parcelable.Creator<RemoteSong> CREATOR = new Parcelable.Creator<RemoteSong>() {
        public RemoteSong createFromParcel(Parcel in) {
            return new RemoteSong(in);
        }

        public RemoteSong[] newArray(int size) {
            return new RemoteSong[size];
        }
    };
}