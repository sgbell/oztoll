/**
 * 
 */
package com.bg.oztoll;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;


/**
 * @author bugman
 *
 */
public class OzTollMapActivity extends SherlockFragmentActivity {
	private Dialog rateDialog, startDialog;
	private ProgressDialog progDialog;
	private SharedPreferences preferences;
	private Activity thisActivity=this;
	private boolean loadingShown=false, startShown=false,
					finishShown=false;
	private OzTollApplication global;
	private AlertDialog alert;
	private AlertDialog.Builder builder;
	private LinearLayout rateLayout;
	public MapView mapView=null;
	private MapFragment mMapFragment;
	private OzTollTextFragment mTextFragment;
	private ResultsFragment resultsFragment;
	private Object msgBoxSync = new Object();

	
	public OzTollMapActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
	}
	
	public boolean onCreateOptionsMenu (Menu menu){
		//http://avilyne.com/?p=180
		/*
		MenuItem miPrefs = menu.add("Preferences");
		miPrefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		miPrefs.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				openPreferences();
				return true;
			}
		});
		*/
		
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
				if (mMapFragment!=null)
					mMapFragment.getOverlay().doPopulate();

				Message newMessage = handler.obtainMessage();
				newMessage.what=6;
				handler.sendMessage(newMessage);
				break;
		}
		
		return true;
	}

	// Tried to combine the reset for both MapFragment & OzTollTextFragment. this needs to be handled by seperate handle codes.
	// do this next.
	private void resetView() {
		Log.w ("ozToll","resetView() called");
		global.getTollData().reset();
		startShown=false;
		finishShown=false;
		if (mTextFragment!=null){
			mTextFragment.setStart("");
			mTextFragment.populateStreets();
		}
	}
	
	public void openPreferences(){
		Intent intent = new Intent (OzTollMapActivity.this, AppPreferences.class);
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
		
		/** 
		 *  Just had a brilliant idea. if it's a first run, make a layout in the main executable
		 *  that gives the welcome message. Which should be a short tutorial on how to use the app
		 *  
		 */
		if (preferences.getBoolean("firstRun", true)){
			Message msg = handler.obtainMessage();
			msg.what=4;
			
			handler.sendMessage(msg);
			SharedPreferences.Editor edit = preferences.edit();
			edit.putBoolean("firstRun", false);
			edit.commit();
		}
		
		// Creates a new dialog
		builder = new AlertDialog.Builder(thisActivity);

		setContentView(R.layout.activity_main);
		
		setupFragments();
		
        // The following was gleaned from
        // http://stackoverflow.com/questions/5373930/how-to-check-network-connection-enable-or-disable-in-wifi-and-3gdata-plan-in-m

        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if ((preferences.getBoolean("applicationView", true))&&
			((wifi.isConnected())||(mobile.isConnected()))){
			// if view is mapView
       /*
        	if (getResources().getBoolean(R.bool.isTablet)){
        		ft.show(mTextFragment);
        		ft.show(mMapFragment);
        	} else {*/
        		//ft.show(mMapFragment);
        		ft.hide(mMapFragment);
        		ft.hide(mTextFragment);
        		//ft.hide(resultsFragment);
        		ft.show(resultsFragment);
        	/*}*/
		} else {
			// if the view text view
        	/*if (getResources().getBoolean(R.bool.isTablet)){
        		ft.show(mTextFragment);
        		ft.show(mMapFragment);
        	} else {*/
        		//ft.show(mTextFragment);
				ft.hide(mTextFragment);
        		ft.hide(mMapFragment);
        		//ft.hide(resultsFragment);
        		ft.show(resultsFragment);

        	/*}*/
		}
        
		ft.commit();
    }
    
    private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
		if (mMapFragment ==null){
			mMapFragment = new MapFragment(mapView, handler);
			ft.add(R.id.fragment_container, mMapFragment, MapFragment.TAG);
		}
		ft.hide(mMapFragment);
		mTextFragment = (OzTollTextFragment) getSupportFragmentManager().findFragmentByTag(OzTollTextFragment.TAG);
		if (mTextFragment == null){
			mTextFragment = new OzTollTextFragment(handler);
			ft.add(R.id.fragment_container, mTextFragment, OzTollTextFragment.TAG);
		}
		ft.hide(mMapFragment);
		
		resultsFragment = (ResultsFragment) getSupportFragmentManager().findFragmentByTag(ResultsFragment.TAG);
		if (resultsFragment == null){
			resultsFragment = new ResultsFragment(handler);
			ft.add(R.id.fragment_container, resultsFragment, ResultsFragment.TAG);
		}
		ft.hide(resultsFragment);
		
		ft.commit();
	}

	final Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		switch (msg.what){
    			case 3:
    				/*
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
        					showMessage("Please clear selection");
        				}
        			});
        			
    				rateDialog.show();
    				*/
    				// Call the results fragment
    				break;
    			case 4:
    				startDialog = new Dialog(thisActivity);
    				startDialog.setContentView(R.layout.welcome);
    				startDialog.setTitle("Welcome");

    				TextView welcomeText = (TextView) startDialog.findViewById(R.id.welcomeText);
    				welcomeText.setText(Html.fromHtml(getString(R.string.welcome)));
    				Button closeButton2 = (Button) startDialog.findViewById(R.id.WelcomeClose);
        			closeButton2.setText("Close");
        			closeButton2.setOnClickListener(new OnClickListener(){

        				@Override
        				public void onClick(View v) {
        					startDialog.dismiss();
        				}
        			});
        			
    				startDialog.show();
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
    			case 7:
    				startShown=false;
    				break;
    			case 8:
    				finishShown=false;
    				break;
    			// case 9 is when a street exit has been selected on the map
    			case 9:
    				OverlayStreet item;
    				if (global.getTollData()!=null){
    					if (((Street)msg.obj).isValid()){
    						if (global.getTollData().getStart()==null){
        						global.getTollData().setStart((Street)msg.obj);
        						
        						Message newMessage = handler.obtainMessage();
        						newMessage.what=6;
        						handler.sendMessage(newMessage);
        					} else if ((global.getTollData().getStart()!=null)
        							   &&(global.getTollData().getFinish()==null)){
        						global.getTollData().setFinish((Street)msg.obj);
        						
        						// Message to finish
        						rateLayout = global.getTollData().processToll(getBaseContext());
        						Message newMessage = handler.obtainMessage();
        						newMessage.obj = rateLayout;
        						newMessage.what=3;
        						handler.sendMessage(newMessage);
        					} else if ((Street)msg.obj==global.getTollData().getFinish()){
        						global.getTollData().setFinish(null);
        						finishShown=false;
        						
        						Message newMessage = handler.obtainMessage();
        						newMessage.what=6;
        						handler.sendMessage(newMessage);
        					}
    					} else if ((Street)msg.obj==global.getTollData().getStart())
    						resetView();
    				}
					if (mMapFragment!=null)
						mMapFragment.getOverlay().doPopulate();
    				break;
    			case 10:
    				resetView();
    				break;
    			case 11:
    				hideMessage();
    				break;
    		}
    	}
    };
    
    // TollDataView Lines 536 & 568 to send handler messages.
    
    public void showMessage(String message){
    	//Code Block for showing "Please select your starting point and Exit Point"
		// This is the message
		builder.setMessage(message);
		synchronized (builder){
			if ((alert==null)||(!alert.isShowing())){
				alert = builder.create();
				// Show it on the screen
				alert.show();

				// This handler is created to dismiss the dialog after 3 seconds
				Handler alertHandler = new Handler();
			    alertHandler.postDelayed(new Runnable(){
			    							public void run(){
			    								hideMessage();
			    							}
				    					 }, 2000);
			    Log.w ("ozToll","msgbox() showing now");
			}
		}
    }
    
    public void hideMessage(){
    	alert.cancel();
    	alert.dismiss();
    	Log.w ("ozToll","msgbox() dismissed");
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
