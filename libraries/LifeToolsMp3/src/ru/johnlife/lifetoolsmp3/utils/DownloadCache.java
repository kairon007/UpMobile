package ru.johnlife.lifetoolsmp3.utils;

import java.util.ArrayList;
import java.util.Random;

import ru.johnlife.lifetoolsmp3.song.AbstractSong;

public class DownloadCache {
	
	private final int CACHE_CAPACITY = 3;

	private static DownloadCache instanse = null;
	private ArrayList<Item> cache = new ArrayList<Item>();
	private int randomId = 0;
	private Random random = new Random();
	
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
	
	public synchronized boolean put(String artist, String title, String comment, DownloadCacheCallback callback) {
		boolean isCached = cache.size() + 1 > CACHE_CAPACITY;
	    randomId = random.nextInt(9999);
		Item item = new Item(randomId, artist, title, isCached, comment);
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
	
	public boolean remove(AbstractSong song) {
		if (null == song) return false;
		return remove(song.getArtist(), song.getTitle());	
	}
	
	public boolean remove(String artist, String title) {
		Item deleteItem = null;
		for (Item item : cache) {
			if (item.getArtist().equals(artist) && item.getTitle().equals(title)) {
				randomId = random.nextInt(9999);
				deleteItem = new Item(randomId, artist, title, false, AbstractSong.EMPTY_COMMENT);
			}
		}
		if (null != deleteItem) {
			return remove(deleteItem);
		}
		return false;
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
	
	public Item getCachedItem(String title, String artist) {
		for (Item item : cache) {
			if (item.getArtist().equals(artist) && item.getTitle().equals(title) && item.isCached) {
				return item;
			}
		}
		return null;
	}
	
	public String getCommentFromItem(String title, String artist) {
		for (Item item : cache) {
			if (item.getArtist().equals(artist) && item.getTitle().equals(title)) {
				return item.comment;
			}
		}
		return AbstractSong.EMPTY_COMMENT;
	}
	
	public class Item {
		
		private long id;
		private String artist;
		private String title;
		private String comment;
		private boolean isCached;
		private DownloadCacheCallback callback;
		private DownloadCacheCallback customCallback;
		
		public Item(long id, String artist, String title, boolean isCached, String comment) {
			this.artist = artist;
			this.title = title;
			this.comment = comment;
			this.isCached = isCached;
			this.id = id;
		}
		
		public void setId(long id) {
			this.id = id;
		}
		
		public long getId() {
			return id;
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
		
		public void setCustomCallback(DownloadCacheCallback callback) {
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
