package ru.johnlife.lifetoolsmp3.song;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.LastFmCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import android.graphics.Bitmap;


public class RemoteSong extends Song {
	private final class WrapperCoverListener implements OnBitmapReadyListener {
		List<OnBitmapReadyListener> listeners = new ArrayList<OnBitmapReadyListener>();
		@Override
		public void onBitmapReady(Bitmap bmp) {
			if (null != bmp) {
				synchronized (listeners) {
					for (OnBitmapReadyListener listener : listeners) {
						listener.onBitmapReady(bmp);
						cover = new WeakReference<Bitmap>(bmp);
					}
					listeners.clear();
					clearCoverLoaderQueue();
				}
			} else {
				useNextCoverLoader();
				getCover((OnBitmapReadyListener)null);
			}
		}

		public void addListener(OnBitmapReadyListener listener) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}

	public static boolean tryMuzicBrainz = true;
	
	private final WrapperCoverListener coverListener = new WrapperCoverListener();
	private int coverLoaderIndex = 0;
	
	protected String downloadUrl;
	public ArrayList<String []> headers;
	private WeakReference<Bitmap> cover;

	private Bitmap songCover;
	
	public RemoteSong(String downloadUrl) {
		super(downloadUrl.hashCode());
		this.downloadUrl = downloadUrl;
	}
	
	protected RemoteSong(long id) {
		super(id);
		downloadUrl = null;
	}

	public RemoteSong setTitle(String songTitle) {
		title = songTitle;
		return this;
	}

	public RemoteSong setArtistName(String songArtist) {
		artist = songArtist;
		return this;
	}
	
	public RemoteSong setDuration (Long songDuration) {
		duration = songDuration;
		return this;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getParentUrl() {
		return downloadUrl;
	}
	
	public RemoteSong setSongCover (Bitmap songCover) {
		songBmp = songCover;
		return this;
	}
	
	public RemoteSong setHeader(ArrayList<String []> headers) {
		this.headers = headers;
		return this;
	}
	public ArrayList<String []> getHeaders() {
		return headers;
	}	
	
	protected CoverLoaderTask getCoverLoader() {
		return 0 == coverLoaderIndex ? new LastFmCoverLoaderTask(artist, title) : 
			(1 == coverLoaderIndex && tryMuzicBrainz) ? new MuzicBrainzCoverLoaderTask(artist, title) : null;
	}


	protected void useNextCoverLoader() {
		coverLoaderIndex++;
	}

	protected void clearCoverLoaderQueue() {
		coverLoaderIndex = 0;
	}
	
	public boolean getCover(OnBitmapReadyListener listener) {
		if (null != cover && null != cover.get()) {
			listener.onBitmapReady(cover.get());
			return true;
		}
		if (null != listener) {
			coverListener.addListener(listener);
		}
		CoverLoaderTask loader = getCoverLoader();
		if (null != loader) {
			loader.addListener(coverListener);
			loader.execute();
			return true;
		}
		return false;
	}
}
