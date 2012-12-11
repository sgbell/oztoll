/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;


/**
 * @author bugman
 *
 */
public class OzTollTextActivity extends SherlockActivity {
	private OzTollApplication global;
	private SharedPreferences preferences;
	private Dialog rateDialog;
	private OzTollTextView ozTextView;
	private Activity thisActivity=this;
	private boolean loadingShown=false, startShown=false, finishShown=false;
	private ProgressDialog progDialog;
	private AlertDialog alert;
	private AlertDialog.Builder builder;
	
	private OzTollTextFragment ozTollTextFragment;
	
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
	
	public boolean onCreateOptionsMenu (Menu menu){
		//http://avilyne.com/?p=180
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
				resetView();
				
				break;
		}
		
		return true;
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

		// Creates a new dialog
		builder = new AlertDialog.Builder(thisActivity);

		
        // The following was gleaned from
        // http://stackoverflow.com/questions/5373930/how-to-check-network-connection-enable-or-disable-in-wifi-and-3gdata-plan-in-m

        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		// If this activity is resumed, and the user has not changed the view to map view
		if ((!preferences.getBoolean("applicationView", true))||
			((!wifi.isConnected())&&(!mobile.isConnected()))){

			// This is where we'll get the fragments running and the screen setup
			ozTollTextFragment = new OzTollTextFragment();
			
			
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
    			case 5:
    				// This case is used to display the loading dialog
    				if (!loadingShown){
        				progDialog = new ProgressDialog(thisActivity);
        				progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        				progDialog.setMessage("Loading...");
        				loadingShown=true;
        				progDialog.show();
    				} else {
    					progDialog.setProgress(progDialog.getProgress()+1);
    				}
    				break;
    			case 6:
    				// This case is used to remove the loading dialog
    				if (loadingShown=true){
    					if (progDialog!=null)
    						if (progDialog.isShowing())
    							progDialog.dismiss();
    					
    					if (global.getTollData().getStart()==null){
    						if (!startShown){
        						showMessage("Please select your Entry point");
        						startShown=true;
    						}
    					} else {
    						if (global.getTollData().getFinish()==null){
    							if (!finishShown){
    								showMessage("Please select your Exit point");
    								finishShown=true;
    							}
    						}
    					}
    				}
    				break;
    			case 10:
    				resetView();
    				break;
    		}
    	}
    };
    
    public void showMessage(String message){
    	//Code Block for showing "Please select your starting point and Exit Point"
		// This is the message
		builder.setMessage(message);
		alert = builder.create();
		// Show it on the screen
		alert.show();

		// This handler is created to dismiss the dialog after 3 seconds
		Handler alertHandler = new Handler();
	    alertHandler.postDelayed(new Runnable(){
	    							public void run(){
	    								alert.cancel();
	    								alert.dismiss();
	    							}
		    					 }, 5000);
    }
	
	public void resetView(){
		global.getTollData().reset();
		TextView startStreet = (TextView)findViewById(R.id.startStreet);
		startStreet.setText("");
		// this needs to reference the method in OzTollTextFragment
		ozTollTextFragment.populateStreets();
	}
	
	

}
