/** ResultsActivity will be an activity the is just used to display the results from the
 * calculations of the Tolls.
 * 
 */
package com.bg.oztoll;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author bugman
 *
 */
public class ResultsActivity extends SherlockActivity {
	private OzTollApplication global;
	private SharedPreferences preferences;

	
	public ResultsActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		global = (OzTollApplication)getApplication();
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		//http://avilyne.com/?p=180
		MenuInflater inflator = getSupportMenuInflater();
		inflator.inflate(R.layout.menu,menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){

		}
		
		return true;
	}

    public void onResume(){
    	super.onResume();

    	
    }
}
