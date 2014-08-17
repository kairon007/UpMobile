package org.kreed.musicdownloader;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "downloads";
	private static DBHelper instance = null;
	private Context context;
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public static DBHelper getInstance(Context context) {
		if (instance == null) {
			Context appContext = context.getApplicationContext();
			instance = new DBHelper(appContext);
			instance.context = appContext;
		}
		return instance;
	}
	
	public static DBHelper getInstance() {
		if (instance == null) {
			throw new IllegalStateException(
					"Instance is not created yet. Call getInstance(Context).");
		}
		return instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + DB_NAME + " ("
				+ "id integer primary key autoincrement," 
				+ "artist text,"
				+ "title text,"
				+ "duration text,"
				+ "fileuri text"
				+ ");");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public void insert(MusicData data) {
		ContentValues cv = new ContentValues();
		cv.put("artist", data.getSongArtist());
		cv.put("title", data.getSongTitle());
		cv.put("duration", data.getSongDuration());
		cv.put("fileuri", data.getFileUri());
		getWritableDatabase().insert(DB_NAME, null, cv);
	}
	
	public void getAll(TaskSuccessListener listener) {
		ArrayList<MusicData> all = new ArrayList<MusicData>();
		Cursor c = getReadableDatabase().query(DB_NAME, null, null, null, null, null, null);
		while (c.moveToFirst()) {
			MusicData data = new MusicData();
			data.setSongArtist(c.getString(c.getColumnIndex("artist")));
			data.setSongTitle(c.getString(c.getColumnIndex("title")));
			data.setSongDuration(c.getString(c.getColumnIndex("duration")));
			data.setFileUri(c.getString(c.getColumnIndex("fileuri")));
			all.add(data);
		}
		listener.success(all);
	}
	
	public void delete(MusicData data) {
		getWritableDatabase().delete(DB_NAME, "artist = " + data.getSongArtist()
				+ "AND title = " + data.getSongTitle()
				+ "AND duration = " + data.getSongDuration()
				+ "AND fileuri = " + data.getFileUri(), null);
	}
}
