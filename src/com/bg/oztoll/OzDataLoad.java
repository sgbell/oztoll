/**
 * 
 */
package com.bg.oztoll;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

/**
 * @author bugman
 *
 */
public class OzDataLoad extends Service {
	boolean isRunning;

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

	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		
		isRunning = true;
		
		OzTollApplication global = (OzTollApplication)getApplication();
		
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		String cityFilename=preferences.getString("cityFile", "melbourne.xml");
		
		AssetManager assetMan = getAssets();
        // Creates a new OzStorage object, and gets the ozTollData object it creates
        // Have changed the code so that melbourne.xml is stored in the assets folder
        global.setTollData(new OzTollData(cityFilename, assetMan));
        global.getTollData().setDataSync(global.getDatasync());
		global.getTollData().setPreferences(preferences);
        
        new Thread(global.getTollData()).start();
		
		return START_STICKY;
	}
	
	public void onDestroy(){
		super.onDestroy();
		
		isRunning = false;
	}
}
