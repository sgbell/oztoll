package com.mimpidev.oztoll;


import android.app.Activity;
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
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class OzTollActivity extends SherlockFragmentActivity {
	private OzTollApplication global;
	private SharedPreferences preferences;

	private boolean loadingShown=false, 
					startShown=false,
					finishShown=false,
					welcomeScreen=false;

	//private AlertDialog alert;
	//private AlertDialog.Builder builder;
	private Activity thisActivity=this; // This is needed for the AlertDialog code

	private MapFragment mMapFragment;
	private OzTollTextFragment mTextFragment;
	private ResultsFragment resultsFragment;
	private TutorialFragment tutorialFragment;
	
	private Dialog startDialog;
	private ProgressDialog progDialog;
	private Runnable closeDialog;

	private LinearLayout rateLayout;
	
	public OzTollActivity(){
		
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	// This is used to access the object that is going to be used to share the data across both activities
    	global = (OzTollApplication)getApplication();
    	// Creating and sharing of the sync object, used for pausing an activity till the first data has been loaded
    	// from the data file.

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

    	// Passing the handler to the global settings, so the fragments onResume can connect with Handler.
    	global.setMainActivityHandler(handler);
    	
    	if (!global.getTollData().isFinished()){
        	String cityFilename=preferences.getString("cityFile", "melbourne.xml");
    		
    		global.getTollData().setDataFile(cityFilename, getAssets());
            global.getTollData().setDataSync(global.getDatasync());
    		global.getTollData().setPreferences(preferences);
    		new Thread(global.getTollData()).start();
    	}
        
        // This is where the application's preferences are stored
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
		setContentView(R.layout.activity_main);

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
				resetView();
				
				if (mMapFragment!=null)
					mMapFragment.populateMarkers();

				setView();
				
				Message newMessage = handler.obtainMessage();
				newMessage.what=6;
				handler.sendMessage(newMessage);
				break;
			case R.id.tutorial:
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				if (getResources().getBoolean(R.bool.isTablet)){
					ft.hide(mMapFragment);
					ft.show(tutorialFragment);
				} else {
   					ft.hide(mMapFragment);
   					ft.hide(mTextFragment);
   					ft.hide(resultsFragment);
					ft.show(tutorialFragment);
				}
				ft.commit();
				break;
		}
		
		return true;
	}

	public void openPreferences(){
		Intent intent = new Intent (OzTollActivity.this, AppPreferences.class);
		startActivity(intent);
    }

	// Tried to combine the reset for both MapFragment & OzTollTextFragment. this needs to be handled by seperate handle codes.
	// do this next.
	private void resetView() {
		global.getTollData().reset();
		startShown=false;
		finishShown=false;
		if (mTextFragment!=null){
			mTextFragment.setStart("");
			mTextFragment.populateStreets();
		}
		
		if (mMapFragment!=null){
			mMapFragment.populateMarkers();
		}
	}
	
    /**
     * onResume is called after onCreate, or when an app is still in memory and resumed.
     */
    public void onResume(){
    	super.onResume();
    	
    	global = (OzTollApplication)getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
		// Creates a new dialog
		//builder = new AlertDialog.Builder(thisActivity);

		setupFragments();

        setView();
    }
    
    public void onPause(){
    	super.onPause();
    	//if ((alert.getWindow()!=null)&&(alert!=null))
    	//	alert.cancel();
    	handler.removeCallbacks(closeDialog);
    }

    public void setView(){
        // The following was gleaned from
        // http://stackoverflow.com/questions/5373930/how-to-check-network-connection-enable-or-disable-in-wifi-and-3gdata-plan-in-m

        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		if (getResources().getBoolean(R.bool.isTablet)){
			// If the screen is a tablet use this layout
    		ft.show(mTextFragment);
    		if (!preferences.getBoolean("welcomeScreenShown", false)){
    			welcomeScreen=true;
    			ft.show(tutorialFragment);
    			ft.hide(mMapFragment);
    		}else{
    			ft.show(mMapFragment);
    			ft.hide(tutorialFragment);
    		}
    		if ((global.getTollData().getStart()!=null)&&
        	    (global.getTollData().getFinish()!=null)){
           			
					rateLayout = global.getTollData().processToll(getBaseContext());
					TextView disclaimer = new TextView(getBaseContext());
					disclaimer.setText(Html.fromHtml(getString(R.string.toll_disclaimer)));
					disclaimer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
					rateLayout.addView(disclaimer);
					
					Message newMessage = handler.obtainMessage();
					newMessage.obj = rateLayout;
					newMessage.what=3;
					handler.sendMessage(newMessage);
			}
    		ft.show(resultsFragment);
    	} else {
    		// If the layout is not a tablet device.
    		if (!preferences.getBoolean("welcomeScreenShown", false)){
    			// On first run show the tutorial
    			welcomeScreen=true;
    			ft.show(tutorialFragment);
    			ft.hide(mMapFragment);
    	       	ft.hide(mTextFragment);
    	       	ft.hide(resultsFragment);
    		} else {
    			if ((global.getTollData().getStart()!=null)&&
        	     	(global.getTollData().getFinish()!=null)){
       	       		// If the path has been chosen
           			ft.hide(tutorialFragment);
           			
					rateLayout = global.getTollData().processToll(getBaseContext());
					TextView disclaimer = new TextView(getBaseContext());
					disclaimer.setText(Html.fromHtml(getString(R.string.toll_disclaimer)));
					disclaimer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
					rateLayout.addView(disclaimer);
					
					Message newMessage = handler.obtainMessage();
					newMessage.obj = rateLayout;
					newMessage.what=3;
					handler.sendMessage(newMessage);
        	    } else {
        	    	if ((preferences.getBoolean("applicationView", true))&&
            			((wifi.isConnected())||(mobile.isConnected()))){
            			// if view is mapView
            			ft.show(mMapFragment);
                	    ft.hide(mTextFragment);
                	    ft.hide(resultsFragment);
                		ft.hide(tutorialFragment);
            		} else {
            			// if the view text view
              	       	ft.show(mTextFragment);
               	       	ft.hide(mMapFragment);
               	       	ft.hide(resultsFragment);
               	       	ft.hide(tutorialFragment);
            		}
        	    }
    		}
    	}
		ft.commit();
    }
    
    private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
		if (mMapFragment == null){
			mMapFragment = new MapFragment(handler);
			if (!getResources().getBoolean(R.bool.isTablet)){
				ft.add(R.id.fragment_container, mMapFragment, MapFragment.TAG);
			} else {
				ft.add(R.id.map_fragment, mMapFragment, MapFragment.TAG);
			}
		}

		mTextFragment = (OzTollTextFragment) getSupportFragmentManager().findFragmentByTag(OzTollTextFragment.TAG);
		if (mTextFragment == null){
			mTextFragment = new OzTollTextFragment(handler);
			if (!getResources().getBoolean(R.bool.isTablet)){
				ft.add(R.id.fragment_container, mTextFragment, OzTollTextFragment.TAG);
			} else {
				ft.add(R.id.text_fragment, mTextFragment, OzTollTextFragment.TAG);
			}
		}
		
		resultsFragment = (ResultsFragment) getSupportFragmentManager().findFragmentByTag(ResultsFragment.TAG);
		if (resultsFragment == null){
			resultsFragment = new ResultsFragment();
			if (!getResources().getBoolean(R.bool.isTablet)){
				ft.add(R.id.fragment_container, resultsFragment, ResultsFragment.TAG);
			} else {
				ft.add(R.id.results_fragment, resultsFragment, ResultsFragment.TAG);
			}
		}

		tutorialFragment = (TutorialFragment) getSupportFragmentManager().findFragmentByTag(TutorialFragment.TAG);
		if (tutorialFragment == null){
			tutorialFragment = new TutorialFragment(handler);
			if (!getResources().getBoolean(R.bool.isTablet)){
				ft.add(R.id.fragment_container, tutorialFragment, TutorialFragment.TAG);
			} else {
				ft.add(R.id.map_fragment, tutorialFragment, TutorialFragment.TAG);
			}
		}
		
		ft.commit();
	}

	final Handler handler = new Handler(){
    	public void handleMessage(Message msg){
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Message newMessage;

    		switch (msg.what){
    			case 1:
    				    				
    				SharedPreferences.Editor edit = preferences.edit();
    				edit.putBoolean("welcomeScreenShown", true);
    				edit.commit();

    				setView();

    				welcomeScreen=false;
    				newMessage = handler.obtainMessage();
    				newMessage.what=6;
    				handler.dispatchMessage(newMessage);
    				
    				break;
    			case 3:
    				// Call the results fragment
    				resultsFragment.getScrollView().removeAllViews();
    				resultsFragment.setContent((LinearLayout)msg.obj);
    				if (!getResources().getBoolean(R.bool.isTablet)){
        				ft.hide(mMapFragment);
        				ft.hide(mTextFragment);
        				ft.show(resultsFragment);
        				
        				ft.commit();
    				}
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

       					if (!welcomeScreen){
        					if (global.getTollData().getStart()==null){
        						if (!startShown){
            						//showMessage("Please select your Entry point");
        							Toast toast = Toast.makeText(thisActivity.getApplicationContext(),"Please select your Entry point", Toast.LENGTH_SHORT);
        							toast.show();
            						startShown=true;
        						}
        					} else {
        						if (global.getTollData().getFinish()==null){
        							if (!finishShown){
        								//showMessage("Please select your Exit point");
        								Toast toast = Toast.makeText(thisActivity.getApplicationContext(),"Please select your Exit point", Toast.LENGTH_SHORT);
            							toast.show();
        								finishShown=true;
        							}
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
    				if (global.getTollData()!=null){
    					if (((Street)msg.obj).isValid()){
    						if (global.getTollData().getStart()==null){
        						global.getTollData().setStart((Street)msg.obj);
        						mTextFragment.setStart("Start Street: "+global.getTollData().getStart().getName());
        						
        						newMessage = handler.obtainMessage();
        						newMessage.what=6;
        						handler.sendMessage(newMessage);
        					} else if ((global.getTollData().getStart()!=null)
        							   &&(global.getTollData().getFinish()==null)){
        						global.getTollData().setFinish((Street)msg.obj);
        						
        						// Message to finish
        						rateLayout = global.getTollData().processToll(getBaseContext());

        						TextView disclaimer = new TextView(getBaseContext());
        						disclaimer.setText(Html.fromHtml(getString(R.string.toll_disclaimer)));
        						disclaimer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        						rateLayout.addView(disclaimer);
        						
        						newMessage = handler.obtainMessage();
        						newMessage.obj = rateLayout;
        						newMessage.what=3;
        						handler.sendMessage(newMessage);
        					} else if ((Street)msg.obj==global.getTollData().getFinish()){
        						global.getTollData().setFinish(null);
        						finishShown=false;
        						
        						newMessage = handler.obtainMessage();
        						newMessage.what=6;
        						handler.sendMessage(newMessage);
        					}
    					} else if ((Street)msg.obj==global.getTollData().getStart())
    						resetView();
    				}
    				
					if (mMapFragment!=null)
						mMapFragment.populateMarkers();
					if (mTextFragment!=null)
						mTextFragment.populateStreets();
    				break;
    			case 10:
    				resetView();
    				break;
    			case 12:
    				setView();
    				/*
    				ft.hide(resultsFragment);
    				if (preferences.getBoolean("applicationView", true))
    					ft.show(mMapFragment);
    				else
    					ft.show(mTextFragment);
    				
    				ft.commit();*/
    				
    				break;
    		}
    	}
    };
}