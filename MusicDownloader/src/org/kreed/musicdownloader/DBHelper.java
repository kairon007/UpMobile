package org.kreed.musicdownloader;

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
				+ "imageuri text"
				+ ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void delete() {
		
	}
	
	public int getNextId() {
		int id = -1;
		String query = "SELECT MAX(id) from " + DB_NAME;
		Cursor result = instance.getReadableDatabase().rawQuery(query, null);
		while (result.moveToNext()) {
			id = result.getInt(0);
		}
		result.close();
		return id;
	}
}
