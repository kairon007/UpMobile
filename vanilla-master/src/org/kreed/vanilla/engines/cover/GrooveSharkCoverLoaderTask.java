package org.kreed.vanilla.engines.cover;

import java.io.BufferedInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GrooveSharkCoverLoaderTask extends CoverLoaderTask {

	private String urlImage;
	
	public GrooveSharkCoverLoaderTask(String artist, String title, String urlImage) {
		super(artist, title);
		this.urlImage = urlImage;
	}
	
	@Override
	protected Bitmap doInBackground(Void... params) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(urlImage);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			BufferedInputStream bitmapStream = new BufferedInputStream(entity.getContent(), 5242880);
			return BitmapFactory.decodeStream(bitmapStream);			
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Error while reading links contents", e);
		}
		return null;
	}
	
}