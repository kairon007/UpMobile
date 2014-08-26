package org.kreed.vanilla.equalizer;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ProgressDataSource {

	private SQLiteDatabase database;
	private MyDB dbHelper;
	private String[] allColumns = { MyDB.COLUMN_ID, MyDB.COLUMN_PG1,
			MyDB.COLUMN_PG2, MyDB.COLUMN_PG3, MyDB.COLUMN_PG4, MyDB.COLUMN_PG5,
			MyDB.COLUMN_USER, MyDB.COLUMN_SK1, MyDB.COLUMN_SK2 };

	public ProgressDataSource(Context context) {
		dbHelper = new MyDB(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public ProgressClass createProgress(int pgs1, int pgs2, int pgs3, int pgs4,
			int pgs5, String user, int sk1, int sk2) {
		ContentValues values = new ContentValues();
		values.put(MyDB.COLUMN_PG1, pgs1);
		values.put(MyDB.COLUMN_PG2, pgs2);
		values.put(MyDB.COLUMN_PG3, pgs3);
		values.put(MyDB.COLUMN_PG4, pgs4);
		values.put(MyDB.COLUMN_PG5, pgs5);
		values.put(MyDB.COLUMN_USER, user);
		values.put(MyDB.COLUMN_SK1, sk1);
		Log.e("sk1ToDb", "flag " + Integer.toString(sk1));
		values.put(MyDB.COLUMN_SK2, sk2);

		Log.e("values", values.toString());
		long insertId = database.insert(MyDB.TABLE_PROGRESS, null, values);
		
		Cursor cursor = database.query(MyDB.TABLE_PROGRESS, allColumns,
				MyDB.COLUMN_ID + " = " + insertId, null, null, null, null);
		cursor.moveToFirst();
		ProgressClass progressClass = cursorToProgress(cursor);
		cursor.close();
		return progressClass;
	}

	public void deleteComment(ProgressClass progressClass) {
		long id = progressClass.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(MyDB.TABLE_PROGRESS, MyDB.COLUMN_ID + " = " + id, null);
	}

	public List<ProgressClass> getAllPgs() {
		List<ProgressClass> progressClasses = new ArrayList<ProgressClass>();
		Log.e("values", "falg");
		Cursor cursor = database.query(MyDB.TABLE_PROGRESS, allColumns, null,
				null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) { 
			ProgressClass progressClass = cursorToProgress(cursor);
			progressClasses.add(progressClass);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return progressClasses;
	}

	private ProgressClass cursorToProgress(Cursor cursor) {
		ProgressClass progressClass = new ProgressClass();
		progressClass.setId(cursor.getLong(0));
		progressClass.setProgress(1, cursor.getInt(1));
		progressClass.setProgress(2, cursor.getInt(2));
		progressClass.setProgress(3, cursor.getInt(3));
		progressClass.setProgress(4, cursor.getInt(4));
		progressClass.setProgress(5, cursor.getInt(5));
		progressClass.setUser(cursor.getString(6));
		Log.e("cursor6", cursor.getString(6).toString());
		progressClass.setSeekArk(1, cursor.getInt(7));
		Log.e("cursor7", Integer.toString(cursor.getInt(7)).toString());
		progressClass.setSeekArk(2, cursor.getInt(8));
		return progressClass;
	}

}
