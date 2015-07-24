package ru.johnlife.lifetoolsmp3.engines.cover;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public abstract class CoverLoaderTask extends AsyncTask<String, Void, String> implements ImageLoadingListener {

    private String coverUrl;
    private final String artist;
    private final String title;
    private final OnCoverTaskListener coverTaskListener;

    public CoverLoaderTask(String coverUrl, String artist, String title, OnCoverTaskListener coverTaskListener) {
        this.coverUrl = coverUrl;
        this.artist = artist;
        this.title = title;
        this.coverTaskListener = coverTaskListener;
    }

    public interface OnCoverTaskListener {
        void onCoverTaskFinished(Bitmap bitmap);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try {
            if (null == s || s.equals("NOT_FOUND") || "".equals(s) || "null".equals(s)) {
                if (null != coverTaskListener) coverTaskListener.onCoverTaskFinished(null);
                Log.i(getClass().getSimpleName(), "Error, cover not found from engines");
                return;
            }
            ImageLoader.getInstance().loadImage(s, this);
        } catch (Throwable e) {
            Log.e(getClass().getSimpleName(), "Error while reading links contents", e);
        }
    }

    @Override
    public void onLoadingCancelled(String arg0, View arg1) {
    }

    @Override
    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
        coverTaskListener.onCoverTaskFinished(arg2);
    }

    @Override
    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
        coverTaskListener.onCoverTaskFinished(null);
    }

    @Override
    public void onLoadingStarted(String arg0, View arg1) {
    }
}