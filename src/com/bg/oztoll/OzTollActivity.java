package com.bg.oztoll;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public class OzTollActivity extends Activity {
	private OzTollData tollData;
	private OzTollView ozView;
	private Object dataSync;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	dataSync = new Object();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ozView = new OzTollView(this);
        setContentView(ozView);
        
        AssetManager assetMan = getAssets();
        // Creates a new OzStorage object, and gets the ozTollData object it creates
        //tollData = new OzStorage().getTollData();
        // Have changed the code so that melbourne.xml is stored in the assets folder
        tollData = new OzTollData("melbourne.xml", assetMan);
        tollData.setDataSync(dataSync);
        // passes ozTollData into ozTollView
		ozView.setDataFile(tollData);
		SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		tollData.setPreferences(sP);
		
		// if "vehicleType" exists, the program has been run previously, and as such, it will
		// start normally.
		if (!sP.contains("vehicleType")){
			// First time run, it will oopent he preferences window to get the user to select
			// how they want to view the system, ie from drop down boxes or the map
			openPreferences();
		}
		// Need to pass these into TollDataView so it's thread can figure out which 
		// tolls to put in the Toll Dialog.
		/**
			String strVehicleType = SP.getString("vehicleType", "car");
			String strViewType = SP.getString("viewType", "1");
			ozView.setVehicleType(strVehicleType);
			*/
    }

    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.menu, menu);
    	
    	return true;
    }
    
    public void openPreferences(){
			Intent intent = new Intent (OzTollActivity.this, AppPreferences.class);
			startActivity(intent);
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.settings:
    			openPreferences();
    			break;
    		case R.id.reset:
    			ozView.reset();
    			break;
    	}
    	return true;
    }
}