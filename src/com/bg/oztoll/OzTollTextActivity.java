/**
 * 
 */
package com.bg.oztoll;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class OzTollTextActivity extends Activity {

	public OzTollTextActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
		OzTollApplication global = (OzTollApplication)getApplication();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
		synchronized (global.getDatasync()){
    		try {
    			global.getDatasync().wait();
    		} catch (InterruptedException e){
    			// just wait for it
    		}
    	}
    	/* Going about this all wrong. looks like I will have to create a seperate activity to use list activity (or some activity
    	 * named similar). I will need 3 activities. 1 to start, 1 for Map view of OzToll, and 1 for the list version.
    	 * 
    	 * Fix this tomorrow
    	 */
    	OzTollTextView ozTextView = new OzTollTextView(global.getTollData());
    	ozTextView.addStreets((LinearLayout) findViewById(R.id.streetList));
    	setContentView(R.layout.textrate);
    	global.setTextViewStarted(true);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.menu, menu);
    	
    	return true;
	}
	
    public void openPreferences(){
			Intent intent = new Intent (OzTollTextActivity.this, AppPreferences.class);
			startActivity(intent);
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.settings:
    			openPreferences();
    			break;
    		case R.id.reset:
    			// this is the reset setting
    			break;
    	}
    	return true;
    }
}
