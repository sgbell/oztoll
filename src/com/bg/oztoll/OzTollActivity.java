package com.bg.oztoll;


import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class OzTollActivity extends SherlockActivity {
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
     * onResume is called after onCreate, or when an app is still in memory and resumed.
     */
    public void onResume(){
    	super.onResume();
    	
    	Log.w("oztoll", "oztollactiivity.onResume() called");
    	
    	global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        // The following was gleaned from
        // http://stackoverflow.com/questions/5373930/how-to-check-network-connection-enable-or-disable-in-wifi-and-3gdata-plan-in-m

        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	
        if ((!wifi.isConnected())&&(!mobile.isConnected())){
         	if (!global.isTextViewStarted()){
          		// If applicationView=text and textView is not Started, Start it
               	textView = new Intent(OzTollActivity.this, OzTollTextActivity.class);
               	startActivityForResult(textView,0);
               	global.setTextViewStarted(true);
         	}
        } else {
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
        if (global.isTextViewStarted())
        	Log.w ("ozToll","OzTollActivity - TextView Started");
        if (global.isMapViewStarted())
        	Log.w ("ozToll","OzTollActivity - MapView Started");
        Log.w ("ozToll","I ove cheese");
    }
    
    /**
     * This is called when a child activity ends.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

    	Log.w("oztoll", "oztollactiivity.onActivityResult() called");
    	
    	global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // The following was gleaned from
        // http://stackoverflow.com/questions/5373930/how-to-check-network-connection-enable-or-disable-in-wifi-and-3gdata-plan-in-m
        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((preferences.getBoolean("applicationView", true))&&
            (!global.isMapViewStarted())){
            	// same as above.
        		mapView = new Intent(OzTollActivity.this, OzTollMapActivity.class);
        		startActivityForResult(mapView, 0);
        		global.setMapViewStarted(true);
        		// Just clearing the textViewStarted boolean
        	   	global.setTextViewStarted(false);
            } else if (((!preferences.getBoolean("applicationView", true))||
            		   ((!wifi.isConnected())&&
            		    (!mobile.isConnected())))&&
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