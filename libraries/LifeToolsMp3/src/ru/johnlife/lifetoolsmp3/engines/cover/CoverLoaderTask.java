package ru.johnlife.lifetoolsmp3.engines.cover;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class CoverLoaderTask extends AsyncTask<Void, Void, Bitmap> {

	public interface OnBitmapReadyListener {
		public void onBitmapReady(Bitmap bmp);
	}
		
	protected List<OnBitmapReadyListener> listeners = new ArrayList<OnBitmapReadyListener>();
	private String coverUrl;
	
	protected CoverLoaderTask() {}
	
	public CoverLoaderTask(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	
	public void addListener(OnBitmapReadyListener listener) {
		listeners.add(listener);
	}
	
	@Override
	protected Bitmap doInBackground(Void... params) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(coverUrl);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			BufferedInputStream bitmapStream = new BufferedInputStream(entity.getContent());
			return BitmapFactory.decodeStream(bitmapStream);			
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error while reading links contents", e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		for (OnBitmapReadyListener listener : listeners) {
			if (null != listener) {
				listener.onBitmapReady(result);
			}
		}
	}
	
}