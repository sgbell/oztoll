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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
	private Activity thisActivity=this;
	
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
	
    public void openPreferences(){
			Intent intent = new Intent (OzTollTextActivity.this, AppPreferences.class);
			startActivity(intent);
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
    		switch (msg.what){
    			case 1:
        			TextView headingText = (TextView)findViewById(R.id.heading);
        			headingText.setText((String)msg.obj);
    				break;
    			case 2:
        			TextView startStreet = (TextView)findViewById(R.id.startStreet);
        			startStreet.setText((String)msg.obj);
    				break;
    			case 3:
    				rateDialog = new Dialog(thisActivity);
        			if (preferences.getString("vehicleType", "car").equalsIgnoreCase("all"))
        				rateDialog.setContentView(R.layout.allratedialog);
        			else
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
    				break;
    		}
    	}
    };
}
