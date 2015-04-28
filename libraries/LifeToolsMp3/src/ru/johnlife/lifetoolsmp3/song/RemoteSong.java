package ru.johnlife.lifetoolsmp3.song;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetoolsmp3.Util;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.CoverLoaderTask.OnBitmapReadyListener;
import ru.johnlife.lifetoolsmp3.engines.cover.LastFmCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.engines.cover.MuzicBrainzCoverLoaderTask;
import ru.johnlife.lifetoolsmp3.ui.DownloadClickListener.CoverReadyListener;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RemoteSong extends Song {
	private boolean hasCoverFromSearch = false;
	private final class WrapperCoverListener implements OnBitmapReadyListener {
		List<OnBitmapReadyListener> listeners = new ArrayList<OnBitmapReadyListener>();

		@Override
		public void onBitmapReady(Bitmap bmp) {
			if (null != bmp) {
				setCover(bmp);
			} else {
				if (coverLoaderIndex > 1) {
					setCover(null);
				} else
					useNextCoverLoader();
				getCover((OnBitmapReadyListener) null);
			}
		}

		private void setCover(Bitmap bmp) {
			synchronized (listeners) {
				for (OnBitmapReadyListener listener : listeners) {
					if (downloaderListener != null) downloaderListener.onCoverReady(bmp);
					listener.onBitmapReady(bmp);
					cover = new WeakReference<Bitmap>(bmp);
					smallCover = new WeakReference<Bitmap>(bmp == null ? null : Util.resizeToSmall(bmp));
				}
				listeners.clear();
				clearCoverLoaderQueue();
			}
		}

		public void addListener(OnBitmapReadyListener listener) {
			synchronized (listeners) {
				listeners.add(listener);
			}
		}
	}
	
	public interface DownloadUrlListener {
		
		void success(String url);
		void error(String error);
	}

	public static boolean tryMuzicBrainz = true;
	
	private final WrapperCoverListener coverListener = new WrapperCoverListener();
	private int coverLoaderIndex = 0;
	
	public ArrayList<String []> headers;
	private WeakReference<Bitmap> cover;
	private WeakReference<Bitmap> smallCover;
	private CoverReadyListener downloaderListener = null;
	protected List<DownloadUrlListener> downloadUrlListeners = new ArrayList<DownloadUrlListener>();
	
	public RemoteSong(String downloadUrl) {
		super(downloadUrl.hashCode());
		this.downloadUrl = downloadUrl;
	}
	
	protected RemoteSong(long id) {
		super(id);
		downloadUrl = "-1";
	}

	public RemoteSong setSongTitle(String songTitle) {
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
		if (null != listener && null != cover && null != cover.get()) {
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
	
	public boolean getSmallCover(boolean callFromPlayer,final OnBitmapReadyListener listener) {
		if (null != smallCover && null != smallCover.get()) {
			listener.onBitmapReady(smallCover.get());
			return true;
		}
		if (null != cover && null != cover.get()) {
			listener.onBitmapReady(Util.resizeToSmall(cover.get()));
			return true;
		}
		return getCover(new OnBitmapReadyListener() {
			
			@Override
			public void onBitmapReady(Bitmap bmp) {
				listener.onBitmapReady(bmp == null ? null : Util.resizeToSmall(bmp));
			}
		});
	}
	
	public void setDownloaderListener(CoverReadyListener downloaderListener) {
		this.downloaderListener = downloaderListener;
		getSmallCover(false, new OnBitmapReadyListener() {
			
			@Override
			public void onBitmapReady(Bitmap bmp) {
				if (null != RemoteSong.this.downloaderListener) {
					RemoteSong.this.downloaderListener.onCoverReady(bmp);
				} 
			}
		});
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
	
	}

	public RemoteSong (Parcel parcel) {
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