package org.upmobile.newmusicdownloader;

public class DrawerItem {
	private int icon;
	private String title;

	public DrawerItem(int icActionSearch, String title) {
		this.icon = icActionSearch;
		this.title = title;
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

}
