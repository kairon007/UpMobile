package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;

public class DownloadCache {
	
	private final int CACHE_CAPACITY = 3;

	private static DownloadCache instanse = null;
	private ArrayList<Item> cache = new ArrayList<Item>();
	
	public static DownloadCache getInstanse() {
		if (null == instanse) {
			instanse = new DownloadCache();
		}
		return instanse;
	}
	
	public boolean contain(String artist, String title) {
		for (Item item : cache) {
			if (item.getArtist().equals(artist) && item.getTitle().equals(title)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean put(String artist, String title, DownloadCacheCallback callback) {
		boolean isCached = cache.size() + 1 > CACHE_CAPACITY;
		Item item = new Item(artist, title, isCached);
		item.setCallback(callback);
		cache.add(item);
		return isCached;
	}
	
	private boolean remove(Item item) {
		int position = cache.indexOf(item);
		if (position < 0 || position >= cache.size()) {
			return false;
		}
		boolean cached = cache.get(position).isCached;
		cache.remove(position);
		if (!cached) {
			for (Item buf : cache) {
				if (buf.isCached()) {
					buf.callback();
					return cached;
				}
			}
		}
		return cached;
	}
	
	public boolean remove(String artist, String title) {
		Item deleteItem = null;
		for (Item item : cache) {
			if (item.getArtist().equals(artist) && item.getTitle().equals(title)) {
				deleteItem = new Item(artist, title, false);
			}
		}
		if (null != deleteItem) {
			return remove(deleteItem);
		} else {
			return false;
		}
	}
	
	public ArrayList<Item> getCachedItems() {
		ArrayList<Item> cachedItems = new ArrayList<Item>();
		for (Item item : cache) {
			if (item.isCached) {
				cachedItems.add(item);
			}
		}
		return cachedItems;
	}
	
	public class Item {
		
		private String artist;
		private String title;
		private boolean isCached;
		private DownloadCacheCallback callback;
		private DownloadCacheCallback customCallback;
		
		public Item(String artist, String title, boolean isCached) {
			this.artist = artist;
			this.title = title;
			this.isCached = isCached;
		}
		
		public String getArtist() {
			return artist;
		}
		
		public String getTitle() {
			return title;
		}
		
		public boolean isCached() {
			return isCached;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof Item 
					&& ((Item)o).getArtist().equals(artist)
					&& ((Item)o).getTitle().equals(title);
		}

		public void callback() {
			this.isCached = false;
			callback.callback(this);
			if (customCallback != null) {
				customCallback.callback(this);
			}
		}

		public void setCallback(DownloadCacheCallback callback) {
			this.callback = callback;
		}
		
		public void setCustovCallback(DownloadCacheCallback callback) {
			this.customCallback = callback;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}
	
	public interface DownloadCacheCallback {
		
		void callback(Item item);
	}
}
