package org.upmobile.musicpro.config;

import java.util.ArrayList;
import java.util.List;

import org.upmobile.musicpro.R;
import org.upmobile.musicpro.object.CategoryMusic;
import org.upmobile.musicpro.object.Song;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.androidquery.AQuery;

public class GlobalValue {
	public static Bitmap bmImgNotFound;
	public static List<CategoryMusic> listCategoryMusics;
	public static List<Song> listSongPlay;
	public static int currentSongPlay;
	public static int currentMenu;
	public static String DD;
	public static String dd;
	public static Bitmap bmNoImageAvailable;
	public static Bitmap ic_music_node_custom;

	public static void constructor(Activity activity) {
		bmImgNotFound = new AQuery(activity)
				.getCachedImage(R.drawable.img_not_found);
		listCategoryMusics = new ArrayList<CategoryMusic>();
		listSongPlay = new ArrayList<Song>();
		DD = activity.getString(R.string.DD);
		dd = activity.getString(R.string.dd);
		bmNoImageAvailable = BitmapFactory.decodeResource(
				activity.getResources(), R.drawable.img_not_found);
		ic_music_node_custom = BitmapFactory.decodeResource(
				activity.getResources(), R.drawable.ic_music_node_custom);
	}

	public static Song getCurrentSong() {
		if (listSongPlay != null && listSongPlay.size() > 0) {
			return listSongPlay.get(currentSongPlay);
		}
		return null;
	}
}
