package com.example.musicequalizer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDB extends SQLiteOpenHelper {

	public static final String TABLE_PROGRESS = "progress";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PG1 = "pg1"; // progress band 1
	public static final String COLUMN_PG2 = "pg2"; // progress band 2
	public static final String COLUMN_PG3 = "pg3"; // progress band 3
	public static final String COLUMN_PG4 = "pg4"; // progress band 4
	public static final String COLUMN_PG5 = "pg5"; // progress band 5
	public static final String COLUMN_USER = "thisUser"; // preset
	public static final String COLUMN_SK1 = "bassArc"; // bass 
	public static final String COLUMN_SK2 = "virtArc"; // virtualizer

	private static final String DATABASE_NAME = "myprogress.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
			+ TABLE_PROGRESS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_PG1
			+ " integer not null, " + COLUMN_PG2 + " integer not null, "
			+ COLUMN_PG3 + " integer not null, " + COLUMN_PG4
			+ " integer not null, " + COLUMN_PG5 + " integer not null, "
			+ COLUMN_USER + " text not null, " + COLUMN_SK1
			+ " integer not null, " + COLUMN_SK2 + " integer not null);";

	public MyDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.e(MyDB.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROGRESS);
		onCreate(db);
	}

}
