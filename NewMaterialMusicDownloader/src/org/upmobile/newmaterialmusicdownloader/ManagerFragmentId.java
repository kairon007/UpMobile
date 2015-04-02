package org.upmobile.newmaterialmusicdownloader;


public class ManagerFragmentId{

	private static boolean mode = false;
	
	private static int searchFragment = 1;
	private static int downloadFragment = 2;
	private static int libraryFragment = 3;
	private static int playerFragment = -2;
	private static int playlistFragment = 4;
	private static int settingFragment = 6;
	
	public static void switchMode (boolean m) {
		mode = m;
		if (mode) {
			playerFragment = 4;
			playlistFragment = 5;
			settingFragment = 7;
		} else {
			playerFragment = -2;
			playlistFragment = 4;
			settingFragment = 6;
		}
	}

	public static int searchFragment() {
		return searchFragment;
	}

	public static int downloadFragment() {
		return downloadFragment;
	}

	public static int libraryFragment() {
		return libraryFragment;
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
