package com.bg.oztoll;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class OzTollActivity extends Activity {
	private Intent mapView, textView;
	private OzTollApplication global;
	private SharedPreferences preferences;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// This is used to access the object that is going to be used to share the data across both activities
    	global = (OzTollApplication)getApplication();
    	// Creating and sharing of the sync object, used for pausing an activity till the first data has been loaded
    	// from the data file.
    	global.setDatasync(new Object());
    	
        super.onCreate(savedInstanceState);
        
        // Starting the data reading first, so that it will be loaded as quick as possible, in the background
        Intent start = new Intent(OzTollActivity.this, OzDataLoad.class);
		startService(start);
        
        // This is where the application's preferences are stored
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    /**
     * openPreferences is used to start the preferenced Dialog. on application startup
     */
    public void openPreferences(){
			Intent intent = new Intent (OzTollActivity.this, AppPreferences.class);
			startActivity(intent);
    }
    
    
    /**
     * onResume is called after onCreate, or when an app is still in memory and resumed.
     */
    public void onResume(){
    	super.onResume();
    	
    	global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
        if (preferences.getBoolean("applicationView", true)){
   			if (!global.isMapViewStarted()){
   				// If applicationView=map and mapView is not Started, start it
   				mapView = new Intent(OzTollActivity.this, OzTollMapActivity.class);
   				startActivityForResult(mapView, 0);
   				global.setMapViewStarted(true);
   			}
        } else {
         	if (!global.isTextViewStarted()){
          		// If applicationView=text and textView is not Started, Start it
               	textView = new Intent(OzTollActivity.this, OzTollTextActivity.class);
               	startActivityForResult(textView,0);
               	global.setTextViewStarted(true);
         	}
        }
    }
    
    /**
     * This is called when a child activity ends.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if ((preferences.getBoolean("applicationView", true))&&
               	(!global.isMapViewStarted())){
            	// same as above.
        		mapView = new Intent(OzTollActivity.this, OzTollMapActivity.class);
        		startActivityForResult(mapView, 0);
        		global.setMapViewStarted(true);
        		// Just clearing the textViewStarted boolean
        	   	global.setTextViewStarted(false);
            } else if ((!preferences.getBoolean("applicationView", true))&&
                   	   (!global.isTextViewStarted())){
                textView = new Intent(OzTollActivity.this, OzTollTextActivity.class);
                startActivityForResult(textView,0);
                global.setTextViewStarted(true);
        		// Just clearing the mapViewStarted boolean
        		global.setMapViewStarted(false);
            } else {
               	// Clearing both boolean values and exiting the app
               	global.setTextViewStarted(false);
               	global.setMapViewStarted(false);
               	finish();
            }        	
    }
}