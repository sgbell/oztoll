/**
 * 
 */
package com.mimpidev.oztoll;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author bugman
 *
 */
public class OzTollResultsActivity extends SherlockFragmentActivity {
	private OzTollApplication global;
	private SharedPreferences preferences;
	
	private ResultsFragment resultsFragment;
	private LinearLayout rateLayout;

	public OzTollResultsActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);

    	global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        setContentView(R.layout.activity_main);
	}
	
    /**
     * onResume is called after onCreate, or when an app is still in memory and resumed.
     */
    public void onResume(){
    	super.onResume();

		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    	
		resultsFragment = (ResultsFragment) getSupportFragmentManager().findFragmentByTag(ResultsFragment.TAG);
		if (resultsFragment == null){
			resultsFragment = new ResultsFragment(handler);
			ft.add(R.id.fragment_container, resultsFragment, ResultsFragment.TAG);
		}
		
		ft.show(resultsFragment);
		ft.commit();
    }
    
    public void onPause(){
    	super.onPause();

    	global.getTollData().reset();
		finish();
    }
    
	public boolean onCreateOptionsMenu (Menu menu){
		MenuInflater inflator = getSupportMenuInflater();
		inflator.inflate(R.layout.menu,menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.preferences:
				openPreferences();
				break;
			case R.id.clear:
				global.getTollData().reset();
				finish();
				break;
			case R.id.tutorial:
				
				break;
		}
		
		return true;
	}

	public void openPreferences(){
		Intent intent = new Intent (OzTollResultsActivity.this, AppPreferences.class);
		startActivity(intent);
    }
	
	final Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		switch (msg.what){
    			case 12:
    				global.getTollData().reset();
    				finish();
    				break;
    		}
    	}
	};
}
