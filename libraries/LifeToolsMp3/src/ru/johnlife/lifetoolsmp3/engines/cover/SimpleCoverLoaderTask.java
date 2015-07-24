package ru.johnlife.lifetoolsmp3.engines.cover;


public class SimpleCoverLoaderTask extends CoverLoaderTask{

    private String coverUrl;

    public SimpleCoverLoaderTask(String coverUrl, String artist, String title, OnCoverTaskListener coverTaskListener) {
        super(coverUrl, artist, title, coverTaskListener);
        this.coverUrl = coverUrl;
    }

    @Override
    protected String doInBackground(String... params) {
        return coverUrl;
    }
}
