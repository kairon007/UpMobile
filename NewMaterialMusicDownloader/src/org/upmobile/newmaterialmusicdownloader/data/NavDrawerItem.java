package org.upmobile.newmaterialmusicdownloader.data;

public class NavDrawerItem {

	public enum Type {
		Primary, Secondary
	};

	private int icon;
	private String title;
	private Type type;

	public NavDrawerItem() {
	}

	public NavDrawerItem(String title, Type type) {
		this(0, title, type);
	}

	public NavDrawerItem(int icon, String title, Type type) {
		this.icon = icon;
		this.title = title;
		this.type = type;
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

}
