package com.lastbear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScoreBuilder extends SQLiteOpenHelper {

	private static ScoreBuilder scoreBuilder = null;

	public static ScoreBuilder getInstance() {
		return scoreBuilder;
	}

	public static ScoreBuilder getInstance(Context context) {
		if (scoreBuilder == null)
			scoreBuilder = new ScoreBuilder(context);
		return scoreBuilder;
	}

	private static final String DATABASE_NAME = "lastbear";
	private static final int DATABASE_VERSION = 6;

	private ScoreBuilder(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE scores(gid TEXT, mid TEXT, score INTEGER);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS scores;";
		db.execSQL(sql);
		onCreate(db);
	}

	private Cursor select(String groupId, String missionId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = String.format(
				"SELECT score FROM scores WHERE gid='%s' AND mid='%s';",
				groupId, missionId);
		Cursor cursor = db.rawQuery(sql, null);
		return cursor;
	}

	public Integer getScore(String groupId, String missionId) {
		Cursor cursor = select(groupId, missionId);
		if (cursor.getCount() != 1) {
			cursor.close();
			return 0;
		}
		cursor.moveToFirst();
		int score = cursor.getInt(0);
		cursor.close();
		return new Integer(score);
	}

	public void setScore(String groupId, String missionId, Integer score) {
		Cursor cursor = select(groupId, missionId);
		boolean existed = (cursor.getCount() >= 1);
		cursor.close();
		if (!existed) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("gid", groupId);
			values.put("mid", missionId);
			values.put("score", score);
			if (db.insert("scores", null, values) == -1) {
				System.out.println("SQLiteDatabase.insert failed.");
			}
		} else {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("score", score);
			String[] arguments = new String[] { groupId, missionId };
			if (db.update("scores", values, "gid=? AND mid=?", arguments) < 1) {
				System.out.println("SQLiteDatabase.update failed.");
			}
		}
	}

	public void clearScore(String groupId) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("score", new Integer(0));
		String[] arguments = new String[] { groupId };
		if (db.update("scores", values, "gid=?", arguments) < 1) {
			System.out.println("SQLiteDatabase.update failed.");
		}
	}
}
