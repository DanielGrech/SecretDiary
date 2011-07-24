package com.DGSD.SecretDiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database { 
	private static final String TAG = Database.class.getSimpleName();

	private final DbHelper mDbHelper; 

	private static final int VERSION = 1;

	private static final String DATABASE = "diary.db";

	public static final String TABLE_NAME = "diary_data";

	public static final String C_ID = "_id";

	public static final String C_KEY = "key";

	public static final String C_VALUE = "value";

	public static final String C_DATE = "date";
	
	public static final String C_IMG_URI = "image_uris";
	
	public static final String C_FILES = "files";
	
	public static final String C_LAT = "latitude";
	
	public static final String C_LONG = "longitude";
	
	public static final String ORDER_BY_DATE = C_DATE + " DESC";

	private SQLiteDatabase mDatabase;

	public Database(Context context) { 
		mDbHelper = new DbHelper(context);

		mDatabase = mDbHelper.getWritableDatabase();

		Log.i(TAG, "Initialized data");
	}

	public DbHelper getDbHelper() {
		return mDbHelper;
	}

	public void close() {  
		mDbHelper.close();
	}

	public boolean insert(String table, ContentValues values) {  
		try {
			mDatabase.insertOrThrow(table, null, values);  
			return true;
		} catch(SQLException e) { 
			return false;
		}
	}

	public int delete(String table, int id) {
		return mDatabase.delete(table, C_ID + "=" + id, null);
	}

	public Cursor get(String table, long id) {
		return mDatabase.query(table, null, C_ID + "=" + id, null,
				null, null, ORDER_BY_DATE);
	}

	public Cursor getAll(String table) {
		return mDatabase.query(table, null, null, null,
				null, null, ORDER_BY_DATE);
	}
	
	public int update(String table, int id, ContentValues values) {
		return mDatabase.update(table, values, C_ID + "=" + id, null);
	}

	protected void finalize() throws Throwable {
	    try {
	        if(mDatabase != null) {
	        	mDatabase.close();
	        	mDbHelper.close();
	        }
	    } finally {
	        super.finalize();
	    }
	}
	
	// DbHelper implementations
	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + 
					C_ID + " INTEGER PRIMARY KEY, " + 
					C_DATE + " text, " + 
					C_LAT + " text, " +
					C_LONG + " text, " +
					C_IMG_URI + " text, " +
					C_FILES + " text, " +
					C_KEY + " text, " + 
					C_VALUE + " text)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table " + TABLE_NAME);
			this.onCreate(db);
		}
	}
}