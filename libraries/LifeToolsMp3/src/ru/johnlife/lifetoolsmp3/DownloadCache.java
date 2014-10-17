package ru.johnlife.lifetoolsmp3;

import java.util.ArrayList;

public class DownloadCache {
	
	private final int CACHE_CAPACITY = 3;

	private static DownloadCache instanse = null;
	private ArrayList<Item> cache = new ArrayList<Item>();
	
	private DownloadCache() {
		
	}
	
	public static DownloadCache getInstanse() {
		if (null == instanse) {
			instanse = new DownloadCache();
		}
		return instanse;
	}
	
	public boolean put(String artist, String title, boolean useCover, DownloadCacheCallback callback) {
		boolean isCached = cache.size() + 1 > CACHE_CAPACITY;
		Item item = new Item(artist, title, useCover, isCached);
		item.setCallback(callback);
		cache.add(item);
		return isCached;
	}
	
	public void remove(String artist, String title, boolean useCover) {
		cache.remove(new Item(artist, title, useCover, false));
		for (Item item : cache) {
			if (item.isCached()) {
				item.callback();
				return;
			}
		}
	}
	
	public void remove(String artist, String title) {
		Item deleteItem = null;
		for (Item item : cache) {
			if (item.getArtist().equals(artist) && item.getTitle().equals(title)) {
				deleteItem = new Item(artist, title, item.isUseCover(), false);
			}
		}
		if (null != deleteItem) {
			remove(deleteItem.getArtist(), deleteItem.getTitle(), deleteItem.isUseCover());
		}
	}
	
	public class Item {
		
		private String artist;
		private String title;
		private boolean useCover;
		private boolean isCached;
		private DownloadCacheCallback callback;
		
		public Item(String artist, String title, boolean useCover, boolean isCached) {
			this.artist = artist;
			this.title = title;
			this.useCover = useCover;
			this.isCached = isCached;
		}
		
		public String getArtist() {
			return artist;
		}
		
		public String getTitle() {
			return title;
		}
		
		public boolean isUseCover() {
			return useCover;
		}
		
		public boolean isCached() {
			return isCached;
		}
		
		@Override
		public boolean equals(Object o) {
			return o instanceof Item 
					&& ((Item)o).getArtist().equals(artist)
					&& ((Item)o).getTitle().equals(title)
					&& ((Item)o).isUseCover() == isUseCover();
		}

		public void callback() {
			this.isCached = false;
			callback.callback(this);
		}

		public void setCallback(DownloadCacheCallback callback) {
			this.callback = callback;
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
