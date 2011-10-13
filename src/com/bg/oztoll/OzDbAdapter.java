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
	
	/**
	 * fetchMapPoints will retrieve the coordinates for the exits of the city tollways
	 * selected.
	 * @param city - The City we are requesting toll points for
	 * @return an array(Cursor) of toll points for the requested city
	 */
	public Cursor fetchTollData(String city){
		String sql = "SELECT t._id as _id, a.x, a.y, a.street, b.x, b.y, b.street, t.toll" +
					 "FROM point a, point b, toll t, city c" +
					 "WHERE a._id=t.point_a" +
					 "AND   b._id=t.point_b" +
					 "AND   t.city_id=c._id" +
					 "AND   c.name='"+city+"'";
				
		Cursor results = database.rawQuery(sql, null);
		return results;
	}
}
