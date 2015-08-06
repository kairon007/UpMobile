package org.upmobile.newmaterialmusicdownloader;


public class ManagerFragmentId{

	private static boolean mode = false;
	
	private static int searchFragment = 1;
	private static int downloadFragment = 2;
	private static int songFragment = 3;
	private static int artistFragment = 4;
	private static int playerFragment = -2;
	private static int playlistFragment = 5;
	private static int settingFragment = 7;
	
	public static void switchMode (boolean m) {
		mode = m;
		if (mode) {
			playerFragment = 5;
			playlistFragment = 6;
			settingFragment = 8;
		} else {
			playerFragment = -2;
			playlistFragment = 5;
			settingFragment = 7;
		}
	}

	public static int searchFragment() {
		return searchFragment;
	}

	public static int downloadFragment() {
		return downloadFragment;
	}

	public static int songFragment() {
		return songFragment;
	}

	public static int artistFragment() {
		return artistFragment;
	}

	public static int playerFragment() {
		return playerFragment;
	}

	public static int playlistFragment() {
		return playlistFragment;
	}

	public static int settingFragment() {
		return settingFragment;
	}
	
}
