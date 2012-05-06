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
	private OzTollApplication global;
	private SharedPreferences preferences;
	private Intent mapView, textView;
	
	public OzTollTextActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
		global = (OzTollApplication)getApplication();
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
		if (global.getDatasync()==null){
			global.setDatasync(new Object());
		}
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
    
    protected void onStop(){
    	setResult(2);
    	
    	super.onStop();
    }
    
    protected void onDestroy(){
    	setResult(2);
    	super.onDestroy();
    }
    
    public void onResume(){
    	super.onResume();
    	
		global = (OzTollApplication)getApplication();
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
		if (global.getDatasync()==null){
			global.setDatasync(new Object());
		}

		// If this activity is resumed, and the user has not changed the view to map view
		if (!preferences.getBoolean("applicationView", true)){
	    	synchronized (global.getDatasync()){
	    		try {
	    			// So we don't get put to sleep indefinately, if the service has already finished loading the data
	    			if (!global.getTollData().isFinished())
	    				global.getDatasync().wait();
	    		} catch (InterruptedException e){
	    			// just wait for it
	    		}
	    	}
	    	OzTollTextView ozTextView = new OzTollTextView(global.getTollData());
	    	ozTextView.addStreets((LinearLayout) findViewById(R.id.streetList));
	    	setContentView(R.layout.textrate);
		} else {
			// If user has just returned to view after changing the preference, end the view to switch to mapView
			setResult(1);
			finish();
		}
    }
}
