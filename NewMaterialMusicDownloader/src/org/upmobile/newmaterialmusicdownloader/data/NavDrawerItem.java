package org.upmobile.newmaterialmusicdownloader.data;

import java.util.Random;


public class NavDrawerItem {

	public enum Type {
		Primary, Secondary
	};
	
	private long id;
	private int icon;
	private String title;
	private Type type;
	private final Random rand = new Random();
	
	public NavDrawerItem() {}

	public NavDrawerItem(String title, Type type) {
		this(0, title, type);
	}

	public NavDrawerItem(int icon, String title, Type type) {
		this.id = System.currentTimeMillis() << 4 * getRandomLong();
		this.icon = icon;
		this.title = title;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	private long getRandomLong() {
		return Math.abs(rand.nextLong());
	}

}
