/**
 * 
 */
package com.bg.oztoll;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * @author bugman
 *
 */
public class OzTollTextActivity extends Activity {
	private OzTollApplication global;
	private SharedPreferences preferences;
	private Dialog rateDialog;
	private OzTollTextView ozTextView;
	
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
    			ozTextView.reset();
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
	    	setContentView(R.layout.textrate);
	    	ozTextView = new OzTollTextView(this.getApplicationContext(),global.getTollData(),handler);
	    	ozTextView.setListView((ExpandableListView)findViewById(R.id.streetList));
	    	rateDialog = new Dialog(this);
	    	Thread ozTextViewThread = new Thread(ozTextView);
	    	ozTextViewThread.start();
		} else {
			// If user has just returned to view after changing the preference, end the view to switch to mapView
			setResult(1);
			finish();
		}
    }
    
    final Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		if (msg.what==1){
    			TextView headingText = (TextView)findViewById(R.id.heading);
    			headingText.setText((String)msg.obj);
    		} else if (msg.what==2){
    			TextView startStreet = (TextView)findViewById(R.id.startStreet);
    			startStreet.setText((String)msg.obj);
    		} else if (msg.what==3){
    			rateDialog.setContentView(R.layout.ratedialog);
    			rateDialog.setTitle("Trip Toll Result");
    			ScrollView dialogScroll = (ScrollView)rateDialog.findViewById(R.id.scrollView);
				dialogScroll.removeAllViews();
				dialogScroll.addView((LinearLayout)msg.obj);
    			Button closeButton = (Button) rateDialog.findViewById(R.id.close);
    			closeButton.setText("Close");
    			closeButton.setOnClickListener(new OnClickListener(){

    				@Override
    				public void onClick(View v) {
    					rateDialog.dismiss();
    					((ScrollView)rateDialog.findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_UP);
    				}
    			});
    			
				rateDialog.show();
    		}
    	}
    };
}
