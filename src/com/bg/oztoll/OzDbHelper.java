package com.bg.oztoll;

import java.io.IOException;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class OzDbHelper extends SQLiteOpenHelper {
	private static final int DB_VERSION = 1;
	private static String DB_PATH=Environment.getExternalStorageDirectory().toString()+ "/oztoll/oztoll.db";
	
	public static final int EXTERNAL_READ_WRITE=0,
							EXTERNAL_READ_ONLY=1,
							EXTERNAL_ERROR=2;
	
	private static final String TABLE_TOLL_EXIT_CREATE = "create table point (_id integer primary key autoincrement, "
														 		+"x text not null," +
														 		"y text not null," +
														 		"street text not null);";
	private static final String TABLE_TOLL_CREATE = "create table toll (_id integer primary key autoincrement," +
															"point_a integer not null," +
															"point_b integer not null," +
															"city_id integer not null,"+
															"toll text not null);";
	private static final String TABLE_CITY = "create table city (_id integer primary key autoincrement," +
															"name text not null);";
	
	private SQLiteDatabase myDatabase;
	
	public OzDbHelper(Context context) {
		super(context, DB_PATH, null, DB_VERSION);
		
		try {
			createDatabase();
		} catch (IOException e){
			throw new Error("Unable to create Database");
		}
	}

	public void createDatabase() throws IOException {
		if (!checkDatabase()) this.getWritableDatabase();
	}

	public int externalStatus(){
		String state = Environment.getExternalStorageState();
		
		// Following If Statements check sdcard availability & wriability
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write to the media
			//available = writable = true;
			return EXTERNAL_READ_WRITE;
		}  else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			return EXTERNAL_READ_ONLY;
		} else {
			// The storage isn't available
			return EXTERNAL_ERROR;
		}		
	}
	
	public boolean checkDatabase() {
		SQLiteDatabase checkDB = null;
		
		if (externalStatus()<EXTERNAL_ERROR){
			try {
				checkDB = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
			} catch (SQLiteException e) {
				// database doesn't exist yet.
			}
		}
		if (checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_TOLL_EXIT_CREATE);
		db.execSQL(TABLE_TOLL_CREATE);
		db.execSQL(TABLE_CITY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS toll_exit;");
		db.execSQL("DROP TABLE IF EXISTS toll;");
		db.execSQL("DROP TABLE IF EXISTS city;");
		onCreate(db);
	}

	public void openDatabase(int mode) throws SQLException {
		boolean databaseOpen=false;
		while (!databaseOpen){
			try {
				myDatabase = SQLiteDatabase.openDatabase(DB_PATH, null, mode);
				databaseOpen=true;
			} catch (IllegalStateException e) {
				// Sometimes after application upgrade the database will not be closed, raising
				// an IllegalStateException. Try to avoid it by opening again.
				Log.d("ozToll", "Error opening database.");
			}			
		}
	}
	
	public void openDatabase() throws SQLException {
		openDatabase(SQLiteDatabase.OPEN_READWRITE);
	}
	
	public SQLiteDatabase getDb(){
		return myDatabase;
	}
	
	public synchronized void close(){
		if (myDatabase != null)
			myDatabase.close();
		super.close();
	}
}
