package com.bg.oztoll;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;

public class OzTollActivity extends Activity {
	private	boolean isRunning;
	private Intent mapView, textView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	isRunning=true;
    	
    	// This is used to access the object that is going to be used to share the data across both activities
    	OzTollApplication global = (OzTollApplication)getApplication();
    	// Creating and sharing of the sync object, used for pausing an activity till the first data has been loaded
    	// from the data file.
    	global.setDatasync(new Object());
    	global.setViewChange(new Object());
    	
        super.onCreate(savedInstanceState);
        
        // Starting the data reading first, so that it will be loaded as quick as possible, in the background
        Intent start = new Intent(OzTollActivity.this, OzDataLoad.class);
		startService(start);
        
        // This is where the application's preferences are stored
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
		// if "vehicleType" exists, the program has been run previously, and as such, it will
		// start normally.
		if (!preferences.contains("vehicleType")){
			// First time run, it will oopent he preferences window to get the user to select
			// how they want to view the system, ie from drop down boxes or the map
			openPreferences();
		}
		
        while (isRunning){
    		if (preferences.getBoolean("applicationView", true)){
    			if (!global.isMapViewStarted()){
    				if (global.isTextViewStarted()){
    					
    					global.setTextViewStarted(false);
    				}
    				mapView = new Intent(OzTollActivity.this, OzTollMapActivity.class);
    				startActivity(mapView);
    			}
            } else {
            	textView = new Intent(OzTollActivity.this, OzTollTextActivity.class);
            	startActivity(textView);
            }
    		
    		synchronized (global.getViewChange()){
    			try {
    				global.getViewChange().wait();
    			} catch (InterruptedException e){
    				// just wait for it
    			}
    		}
        }
    }

    public void openPreferences(){
			Intent intent = new Intent (OzTollActivity.this, AppPreferences.class);
			startActivity(intent);
    }
}