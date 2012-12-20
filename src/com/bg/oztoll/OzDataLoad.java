/**
 * 
 */
package com.bg.oztoll;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author bugman
 *
 */
public class OzDataLoad extends Service {
	boolean isRunning;
	private OzTollApplication global;
	private SharedPreferences preferences;

	/**
	 * 
	 */
	public OzDataLoad() {
		
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		Log.w ("ozToll", "OzDataLoad.onCreate()");

		global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	}

	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		Log.w ("ozToll", "OzDataLoad.onStartCommand()");
		
		isRunning = true;
		
		
		String cityFilename=preferences.getString("cityFile", "melbourne.xml");
		
		AssetManager assetMan = getAssets();
        // Creates a new OzStorage object, and gets the ozTollData object it creates
        // Have changed the code so that melbourne.xml is stored in the assets folder
		Log.w ("ozToll", "OzDataLoad.onStartCommand() calling OzTollApplication.setTollData(create OzTollData(melbourne, assetMan))");
		global.setTollData(new OzTollData(cityFilename, assetMan));
		Log.w ("ozToll", "OzDataLoad.onStartCommand() calling OzTollApplication.getTollData().setDataSync(OzTollApplication.getDatasync())");
        global.getTollData().setDataSync(global.getDatasync());
		Log.w ("ozToll", "OzDataLoad.onStartCommand() calling OzTollApplication.getTollData().setPreferences(preferences)");
		global.getTollData().setPreferences(preferences);
        
		Log.w ("ozToll", "OzDataLoad.onStartCommand() Start ozTollData thread4");
        new Thread(global.getTollData()).start();
		
		return START_STICKY;
	}
	
	public void onDestroy(){
		super.onDestroy();
		
		isRunning = false;
	}
}
