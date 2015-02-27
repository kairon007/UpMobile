package org.upmobile.newmusicdownloader;

public class DrawerItem {

	public enum Types {TYPE_MENU, TYPE_SECTION, TYPE_SETTING};
	
	private int icon = 0;
	private String title;
	private Types type;

	public DrawerItem(int icActionSearch, String title, Types type) {
		this.icon = icActionSearch;
		this.title = title;
		this.type = type;
	}
	
	public DrawerItem(String title, Types type) {
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
	
	public void setType(Types type) {
		this.type = type;
	}
	
	public Types getType() {
		return type;
	}

}
