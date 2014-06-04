package com.lastbear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Preferences extends SQLiteOpenHelper {

	private static Preferences preferences = null;

	public static Preferences getInstance() {
		return preferences;
	}

	public static Preferences getInstance(Context context) {
		if (preferences == null)
			preferences = new Preferences(context);
		return preferences;
	}

	private static final String DATABASE_NAME = "lastbearperferences";
	private static final int DATABASE_VERSION = 3;
	public static final int library_background_sound = 1;

	private Preferences(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE preferences(pid INTEGER, vi INTEGER);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS preferences;";
		db.execSQL(sql);
		onCreate(db);
	}

	private Cursor select(int pid) {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = String.format(
				"SELECT vi FROM preferences WHERE pid='%d';", pid);
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	public Integer getPreference(int pid) {
		Cursor cursor = select(pid);
		if (cursor.getCount() != 1) {
			cursor.close();
			return 0;
		}
		cursor.moveToFirst();
		int value = cursor.getInt(0);
		cursor.close();
		return new Integer(value);
	}

	public void setPreference(int pid, int value) {
		Cursor cursor = select(pid);
		boolean existed = (cursor.getCount() >= 1);
		cursor.close();
		if (!existed) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("pid", pid);
			values.put("vi", value);
			if (db.insert("preferences", null, values) == -1) {
				System.out.println("SQLiteDatabase.insert failed.");
			}
		} else {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("vi", value);
			String[] arguments = new String[] { "" + pid };
			if (db.update("preferences", values, "pid=?", arguments) < 1) {
				System.out.println("SQLiteDatabase.update failed.");
			}
		}
	}
}
