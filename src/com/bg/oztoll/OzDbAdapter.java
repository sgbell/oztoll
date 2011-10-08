package com.bg.oztoll;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class OzDbAdapter {
	private Context context;
	private OzDbHelper dbHelper;
	private SQLiteDatabase database;
		
	public OzDbAdapter(Context context){
		this.context = context;
	}
	
	public OzDbAdapter open() throws SQLException {
		dbHelper = new OzDbHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public Cursor fetchMapPoints(String city){
		Cursor cityList = database.query(true, "city", new String[] { "_id","name"}, "name='"+city+"'", null, null, null, null, null);
		
		//Cursor paths = database.query(true, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		return null;
	}
}
