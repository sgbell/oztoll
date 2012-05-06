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

/**
 * @author bugman
 *
 */
public class OzTollMapActivity extends Activity {
	private OzTollView ozView;

	public OzTollMapActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
		OzTollApplication global = (OzTollApplication)getApplication();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.menu, menu);
    	
    	return true;
	}
	
    public void openPreferences(){
			Intent intent = new Intent (OzTollMapActivity.this, AppPreferences.class);
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
    	
		OzTollApplication global = (OzTollApplication)getApplication();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if (preferences.getBoolean("applicationView", true)){
			// if view is mapView
			ozView = new OzTollView(this, global.getTollData());
			setContentView(ozView);
		} else {
			// if user has just changed the preference to text view
			setResult(1);
			finish();
		}
    }
}
