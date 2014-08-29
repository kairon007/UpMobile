package org.kreed.musicdownloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
	
	public void deleteAll() {
		new DeleteAllTask().execute();
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
				String fileUriString = c.getString(c.getColumnIndex("fileuri"));
				data.setFileUri(fileUriString);
				try {
					MusicMetadataSet src_set = new MyID3()
							.read(new File(fileUriString));
					if (src_set != null) {
						MusicMetadata metadata = src_set.merged;
						Bitmap bitmap = getArtworkImage(2, metadata);
						data.setSongBitmap(bitmap);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				all.add(data);
			}
			return all;
		}
		
		@Override
		protected void onPostExecute(ArrayList<MusicData> data) {
			listener.success(data);
		}
	}
	
	public static Bitmap getArtworkImage(int maxWidth, MusicMetadata metadata) {
		if (maxWidth == 0) {
			return null;
		}
		Vector<ImageData> pictureList = metadata.getPictureList();
		if ((pictureList == null) || (pictureList.size() == 0)) {
			return null;
		}
		ImageData imageData = (ImageData) pictureList.get(0);
		if (imageData == null) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		int scale = 1;
		if ((maxWidth != -1) && (opts.outWidth > maxWidth)) {
			// Find the correct scale value. It should be the power of 2.
			int scaleWidth = opts.outWidth;
			while (scaleWidth > maxWidth) {
				scaleWidth /= 2;
				scale *= 2;
			}
		}
		opts = new BitmapFactory.Options();
		opts.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeByteArray(imageData.imageData,
				0, imageData.imageData.length, opts);
		return bitmap;
	}
	
	private class DeleteTask extends AsyncTask<Void, Void, Void> {

		private MusicData data;
		
		public DeleteTask(MusicData data) {
			this.data = data;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			String s = data.getFileUri();
			char[] mass = s.toCharArray();
			StringBuilder s1 = null;
			for (int i = 0; i < s.length(); i++) {
				if (mass[i] == '\'') {
					s1 = new StringBuilder();
					s1.append(s.substring(0, i));
					s1.append(mass[i]);
					s1.append(mass[i]);
					s1.append(s.substring(i + 1));
				}
			}
			if (null != s1) {
				s = s1.toString();
			}
			getWritableDatabase().delete(DB_NAME,  "fileuri = " + "('" + s + "')", null); 
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

	private class DeleteAllTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			getWritableDatabase().execSQL("DELETE FROM " + DB_NAME);
			return null;
		}
	}
}
