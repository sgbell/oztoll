/**
 * 
 */
package com.mimpidev.oztoll;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author bugman
 *
 */
public class OzTollTutorialActivity extends SherlockFragmentActivity {

	private OzTollApplication global;
	private TutorialFragment tutorialFragment;

	public OzTollTutorialActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
    	super.onCreate(savedInstanceState);
    	
    	global = (OzTollApplication)getApplication();
        PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.activity_main);
	}
	
	public void onResume(){
		super.onResume();
		
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    	
		tutorialFragment = (TutorialFragment) getSupportFragmentManager().findFragmentByTag(TutorialFragment.TAG);
		if (tutorialFragment == null){
			tutorialFragment = new TutorialFragment(handler);
			ft.add(R.id.fragment_container, tutorialFragment, TutorialFragment.TAG);
		}
		
		tutorialFragment.setHandler(handler);
		
		ft.show(tutorialFragment);
		ft.commit();
	}
	
	public void onPause(){
		super.onPause();
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
				break;
			case R.id.tutorial:
				break;
		}
		
		return true;
	}

	public void openPreferences(){
		Intent intent = new Intent (OzTollTutorialActivity.this, AppPreferences.class);
		startActivity(intent);
    }
	
	final Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		switch (msg.what){
    			case 1:
    				finish();
    				break;
    		}
    	}
	};
}
