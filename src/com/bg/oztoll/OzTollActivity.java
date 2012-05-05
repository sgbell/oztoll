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
import android.widget.LinearLayout;
import android.widget.Spinner;

public class OzTollActivity extends Activity {
	private OzTollData tollData;
	private OzTollView ozView;
	private Object dataSync;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	String cityFilename; 
    	
    	dataSync = new Object();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // This is where the application's preferences are stored
        SharedPreferences sP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
		// if "vehicleType" exists, the program has been run previously, and as such, it will
		// start normally.
		if (!sP.contains("vehicleType")){
			// First time run, it will oopent he preferences window to get the user to select
			// how they want to view the system, ie from drop down boxes or the map
			openPreferences();
			
			// Need to set melbourne.xml as the file to open here.
		}
		cityFilename = sP.getString("cityFile", "melbourne.xml");
		
		AssetManager assetMan = getAssets();
        // Creates a new OzStorage object, and gets the ozTollData object it creates
        //tollData = new OzStorage().getTollData();
        // Have changed the code so that melbourne.xml is stored in the assets folder
        tollData = new OzTollData(cityFilename, assetMan);
        tollData.setDataSync(dataSync);
		tollData.setPreferences(sP);
        
        new Thread(tollData).start();
        
        if (sP.getBoolean("applicationView", true)){
            ozView = new OzTollView(this, tollData);
            setContentView(ozView);
        } else {
        	synchronized (dataSync){
        		try {
        			dataSync.wait();
        		} catch (InterruptedException e){
        			// just wait for it
        		}
        	}
        	/* Going about this all wrong. looks like I will have to create a seperate activity to use list activity (or some activity
        	 * named similar). I will need 3 activities. 1 to start, 1 for Map view of OzToll, and 1 for the list version.
        	 * 
        	 * Fix this tomorrow
        	 */
        	OzTollTextView ozTextView = new OzTollTextView(tollData);
        	ozTextView.addStreets((LinearLayout) findViewById(R.id.streetList));
        	setContentView(R.layout.textrate);
        }
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