package ru.johnlife.lifetoolsmp3.song;

import org.jsoup.Jsoup;

import android.os.AsyncTask;

public class GoearV2Song extends SongWithCover {

	private String id;
	private String coverUrl;

	public GoearV2Song(String id, String coverUrl) {
		super(id);
		this.id = id;
		this.coverUrl = coverUrl;
	}

	@Override
	public String getLargeCoverUrl() {
		return coverUrl;
	}
	
	@Override
	public boolean getDownloadUrl(DownloadUrlListener listener) {
		if (super.getDownloadUrl(listener)) return true;
		getDownloadUrl(id);
		return false;
	}
	
	public void getDownloadUrl(final String songId) {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					return Jsoup.connect("http://www.goear.com/action/sound/get/" + songId)
							.followRedirects(true)
							.ignoreHttpErrors(true)
							.ignoreContentType(true)
							.execute()
							.url().toString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(String result) {
				if (null != result) {
					for (DownloadUrlListener listener : downloadUrlListeners) {
						listener.success(result);
					}
				} 
				downloadUrlListeners.clear();
			};
		}.execute();
	}

}
