package org.kreed.musicdownloader;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

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
		new InsertTask(data).execute();
	}
	
	public void getAll(TaskSuccessListener listener) {
		new GetAllTask(listener).execute();
	}
	
	public void delete(MusicData data) {
		new DeleteTask(data).execute();
	}
	
	private class GetAllTask extends AsyncTask<Void, Void, ArrayList<MusicData>> {

		private TaskSuccessListener listener;
		
		public GetAllTask(TaskSuccessListener listener) {
			this.listener = listener;
		}
		
		@Override
		protected ArrayList<MusicData> doInBackground(Void... params) {
			ArrayList<MusicData> all = new ArrayList<MusicData>();
			Cursor c = getReadableDatabase().query(DB_NAME, null, null, null, null, null, null);
			while (c.moveToNext()) {
				MusicData data = new MusicData();
				data.setSongArtist(c.getString(c.getColumnIndex("artist")));
				data.setSongTitle(c.getString(c.getColumnIndex("title")));
				data.setSongDuration(c.getString(c.getColumnIndex("duration")));
				data.setFileUri(c.getString(c.getColumnIndex("fileuri")));
				all.add(data);
			}
			return all;
		}
		
		@Override
		protected void onPostExecute(ArrayList<MusicData> data) {
			listener.success(data);
		}
	}
	
	private class DeleteTask extends AsyncTask<Void, Void, Void> {

		private MusicData data;
		
		public DeleteTask(MusicData data) {
			this.data = data;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			getWritableDatabase().delete(DB_NAME, "(artist = " + "'" + data.getSongArtist() + "'"
					+ ") AND (title = " + "'" + data.getSongTitle() + "'"
					+ ") AND (duration = " + "'" + data.getSongDuration() + "'"
					+ ") AND (fileuri = " + "'" + data.getFileUri() + "'" + ")", null); 
			return null;
		}
	}
	
	private class InsertTask extends AsyncTask<Void, Void, Void> {

		private MusicData data;
		
		public InsertTask(MusicData data) {
			this.data = data;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			ContentValues cv = new ContentValues();
			cv.put("artist", data.getSongArtist());
			cv.put("title", data.getSongTitle());
			cv.put("duration", data.getSongDuration());
			cv.put("fileuri", data.getFileUri());
			getWritableDatabase().insert(DB_NAME, null, cv);
			return null;
		}
		
	}
}
