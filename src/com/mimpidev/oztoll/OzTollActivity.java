package com.mimpidev.oztoll;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
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
					welcomeScreen=false,
					mapFragmentVisible=false,
					textFragmentVisible=false;

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

        // This is where the application's preferences are stored
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

    	// Passing the handler to the global settings, so the fragments onResume can connect with Handler.
    	global.setMainActivityHandler(handler);

    	// Need to check if the Data file has been loaded in the Global class, so that on rotation, the
    	// data isn't reloaded unnecessarily.
    	if (!global.isTollDataLoaded()){
        	// location - false = internal app file.
        	//			- true  = external storage file.
        	boolean dataFileLocation = preferences.getBoolean("location", false);
        	if (!dataFileLocation){
        		// load data file from assets folder
        		global.getTollData().setDataFile("oztoll.xml", getAssets(),preferences);
        	} else {
        		// load data file from external folder
        		OzStorage extStorage = new OzStorage();
        		extStorage.setTollData("oztoll.xml");
        		if (extStorage.getTollData()!=null){
        			global.getTollData().setPreferences(preferences);
        			global.setTollData(extStorage.getTollData());
        		} else
            		global.getTollData().setDataFile("oztoll.xml", getAssets(),preferences);
        	}
        	global.getTollData().setDataSync(global.getDatasync());
        	global.getTollData().setPreferences(preferences);
        	// The following is the initial read of the data file, and population of the tollway arrays
        	new Thread(global.getTollData()).start();
    		global.setTollDataLoaded(true);
    	}
    	
		setContentView(R.layout.activity_main);
		
		SimpleEula.show(this);
    }

    /**
     * onCrateOptionsMenu - used to create the options menu. It inflates the menu xml file.
     */
	public boolean onCreateOptionsMenu (Menu menu){
		MenuInflater inflator = getSupportMenuInflater();
		inflator.inflate(R.layout.menu,menu);
		
		return true;
	}
	
	/**
	 * onOptionsItemSelected - When a menu item is selected in the app.
	 */
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
			case R.id.preferences:
				// On preferences being selected, open preferences
				openPreferences();
				break;
			case R.id.clear:
				// Clear the current View
				resetView();
				
				if (mMapFragment!=null)
					mMapFragment.populateMarkers();

				setView();
				
				// Handler message 6 is used to display a message, generally: "Select Start, or Finish"
				Message newMessage = handler.obtainMessage();
				newMessage.what=6;
				handler.sendMessage(newMessage);
				break;
			case R.id.tutorial:
				// Open the tutorial View
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				if (getResources().getBoolean(R.bool.isTablet)){
					/* If the device is a tablet we need to identify what view the device currently has
					 * in the main view window, as if the internet is disconnected it may be the textview
					 * not the map view 
					 */
			        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
					if ((wifi.isConnected())||(mobile.isConnected()))
						ft.hide(mMapFragment);
					else
						ft.hide(mTextFragment);
					ft.show(tutorialFragment);
				} else {
					// If the device is not a tablet. call the openTutorial method 
					openTutorial();
				}
				ft.commit();
				break;
		}
		
		return true;
	}

	/**
	 * openPreferences - open the preferences Activity
	 */
	public void openPreferences(){
		Intent intent = new Intent (OzTollActivity.this, AppPreferences.class);
		startActivity(intent);
    }

	/**
	 * openTutorial - This is only used if the device is not a tablet. It opens the tutorial
	 * Activity
	 */
	public void openTutorial(){
		Intent intent = new Intent (OzTollActivity.this, OzTollTutorialActivity.class);
		startActivity(intent);
    }

	/**
	 *  resetView() This is used to Reset the view on the screen when clear is pressed.
	 */
	private void resetView() {
		global.getTollData().reset();
		startShown=false;
		finishShown=false;
		if (mTextFragment!=null){
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
    	global.getTollData().setPreferences(preferences);
        
		setupFragments();

        setView();
    }
    
    /**
     * checkForUpdatedData - This is used for contacting our webserver and checking
     * 		if there is an updated data file on the server. 
     */
    private void checkForUpdatedData() {
    	
    	Thread newThread = new Thread(){
    		
    		public void run(){
    	    	Long storedTimestamp = preferences.getLong("lastUpdate", 0);
    	    	Long currentTimestamp = System.currentTimeMillis()/1000;
    	    	boolean checkWebsite=false;
    	    	
    	    	String lastModified = preferences.getString("lastModified", "");
    	    	String internetModification;
    	    	URL url;
    	    	URLConnection connection;
    	    	
    	    	// If the current timestamp is 1 day after the last check.
    	    	if ((storedTimestamp==0)||
    	    		(currentTimestamp >= (storedTimestamp+86400))){
    	    		checkWebsite=true;
    	    	}
    	    	
   	        	if (checkWebsite){
   	        		try {
   	    				url = new URL("http://www.mimpidev.com/oztoll/oztoll.xml");
   	    				connection = url.openConnection();
   	    				internetModification = connection.getHeaderField("Last-Modified");
   	    				
   	    				Editor prefEditor = preferences.edit();
   	    				
   	    				// Is the last modified date stored in preferences different to the one on the website
   	    				if (!lastModified.equalsIgnoreCase(internetModification)){
   	    					// Download file here
   	    					OzStorage storage = new OzStorage();
   	    					File saveFile = storage.saveFiletoExternal("temp.xml");
   	    					InputStream inputStream = connection.getInputStream();
    	    					
   	    					RandomAccessFile outstream = new RandomAccessFile(saveFile,"rw");
   	    					int byteRead=0;
   	    					byte buffer[]=new byte[1024];
   	    					while ((byteRead = inputStream.read(buffer)) > 0)
   	    						outstream.write(buffer, 0, byteRead);
    	    				outstream.close();
    	    				inputStream.close();
    	    					
    	    				OzTollData temp = storage.openExternalFile("temp.xml",preferences);
    	    				if (temp!=null){
        	    				if (Long.parseLong(temp.getTimestamp())>
    	    					    Long.parseLong(global.getTollData().getTimestamp())){
    	    					storage.keepFile(saveFile);
    	    					global.setTollData(temp);
    	    					prefEditor.putBoolean("location", true);
        	    				} else 
        	    					storage.removeFile(saveFile);
    	    				}
   	    				}
   	    				prefEditor.putLong("lastUpdate", System.currentTimeMillis()/1000);
   	    	    		prefEditor.commit();
   	    			} catch (MalformedURLException e) {
   	    			} catch (IOException e) {
   	    			}
   	        	}
   	    	}
    	};
    	
    	newThread.start();
	}

	public void onPause(){
    	super.onPause();

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
    		textFragmentVisible=true;
    		mapFragmentVisible=true;
    		// This if statement is copied below. move the following code somewhere so it's
    		// not written twice in my code.
    		if ((global.getTollData().getStart()!=null)&&
        	    (global.getTollData().getFinish()!=null)){
           			
					displayResults();
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
    		} else {
    			if ((global.getTollData().getStart()!=null)&&
        	     	(global.getTollData().getFinish()!=null)){
       	       		// If the path has been chosen
           			if (tutorialFragment!=null)
           				ft.hide(tutorialFragment);
           			
           			displayResults();
        	    } else {
        	    	if ((preferences.getBoolean("applicationView", true))&&
            			((wifi.isConnected())||(mobile.isConnected()))){
            			// if view is mapView
            			ft.show(mMapFragment);
                	    ft.hide(mTextFragment);
               			if (tutorialFragment!=null)
               				ft.hide(tutorialFragment);
               	       	textFragmentVisible=false;
                		mapFragmentVisible=true;
            		} else {
            			// if the view text view
              	       	ft.show(mTextFragment);
               	       	ft.hide(mMapFragment);
               			if (tutorialFragment!=null)
               				ft.hide(tutorialFragment);
                		mapFragmentVisible=false;
               	       	textFragmentVisible=true;
            		}
        	    }
    		}
    	}
		ft.commit();
    }

    /**
     * 
     */
    private void displayResults(){
    	
		if (getResources().getBoolean(R.bool.isTablet)){
			// Message to finish
			rateLayout = global.getTollData().processToll(getBaseContext());

			TextView disclaimer = new TextView(getBaseContext());
			disclaimer.setText(Html.fromHtml(getString(R.string.toll_disclaimer)));
			disclaimer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
			rateLayout.addView(disclaimer);
			TextView expireDate = new TextView(getBaseContext());
			expireDate.setText(Html.fromHtml("<h2>Tolls Valid until "+global.getTollData().getSelectedExpiryDate()+"</h2>"));
			expireDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
			rateLayout.addView(expireDate);
			
			Message newMessage = handler.obtainMessage();
			newMessage.obj = rateLayout;
			newMessage.what=3;
			handler.sendMessage(newMessage);
		} else {
			Intent intent = new Intent (OzTollActivity.this, OzTollResultsActivity.class);
			startActivity(intent);
		}
    }
    
    private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ConnectivityManager connection = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connection.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connection.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		
		mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag(MapFragment.TAG);
		if (mMapFragment == null){
			mMapFragment = new MapFragment(handler);
			if (!getResources().getBoolean(R.bool.isTablet)){
				ft.add(R.id.fragment_container, mMapFragment, MapFragment.TAG);
			} else {
    	    	if ((wifi.isConnected())||(mobile.isConnected()))
    	    		ft.add(R.id.map_fragment, mMapFragment, MapFragment.TAG);
    	    	else
    	    		ft.add(R.id.text_fragment, mMapFragment, MapFragment.TAG);
			}
		}

		mTextFragment = (OzTollTextFragment) getSupportFragmentManager().findFragmentByTag(OzTollTextFragment.TAG);
		if (mTextFragment == null){
			mTextFragment = new OzTollTextFragment(handler);
			if (!getResources().getBoolean(R.bool.isTablet)){
				ft.add(R.id.fragment_container, mTextFragment, OzTollTextFragment.TAG);
			} else {
    	    	if ((wifi.isConnected())||(mobile.isConnected()))
    	    		ft.add(R.id.text_fragment, mTextFragment, OzTollTextFragment.TAG);
    	    	else
    	    		ft.add(R.id.map_fragment, mTextFragment, OzTollTextFragment.TAG);
			}
		}
		
		/* If the device is a tablet it will add the results to the layout in the tablet.
		 * If the device is a phone, it will be opened in it's own activity, allowing the user to
		 * hit the back button to return from the results.
		 */ 
		if (getResources().getBoolean(R.bool.isTablet)){
			resultsFragment = (ResultsFragment) getSupportFragmentManager().findFragmentByTag(ResultsFragment.TAG);
			if (resultsFragment == null){
				resultsFragment = new ResultsFragment();
				ft.add(R.id.results_fragment, resultsFragment, ResultsFragment.TAG);
			}
		}

		/* This grabs the instance of the TutorialFragment, if 1 exists, so when the program resumes, it wont be
		 * overlayed on which ever view is active for the user. 
		 */
		tutorialFragment = (TutorialFragment) getSupportFragmentManager().findFragmentByTag(TutorialFragment.TAG);
		if ((getResources().getBoolean(R.bool.isTablet))||
			(!preferences.getBoolean("welcomeScreenShown", false))){
			/* If this is the first time running, or the app is running on a tablet the following code
			 * is called
			 */
			if (tutorialFragment == null){
				tutorialFragment = new TutorialFragment(handler);
				if (!getResources().getBoolean(R.bool.isTablet)){
					// If it's not a tablet
					ft.add(R.id.fragment_container, tutorialFragment, TutorialFragment.TAG);
				} else {
					// If the device is a tablet
					ft.add(R.id.map_fragment, tutorialFragment, TutorialFragment.TAG);
				}
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
    			case 2:
        	        ConnectivityManager connManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        	        android.net.NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        	        android.net.NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        	        
        	    	if ((wifi.isConnected())||(mobile.isConnected())){
        				checkForUpdatedData();
        				resetView();
        	    	}
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
        							Toast toast = Toast.makeText(thisActivity.getApplicationContext(),"Please select your Entry point", Toast.LENGTH_SHORT);
        							toast.show();
            						startShown=true;
        						}
        						// Check for an update after the interface is shown
        						newMessage = handler.obtainMessage();
        						newMessage.what=2;
        						handler.sendMessage(newMessage);
        					} else {
        						if (global.getTollData().getFinish()==null){
        							if (!finishShown){
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
        						
        						newMessage = handler.obtainMessage();
        						newMessage.what=6;
        						handler.sendMessage(newMessage);
        					} else if ((global.getTollData().getStart()!=null)&&
        							   (global.getTollData().getFinish()==null)){
        						global.getTollData().setFinish((Street)msg.obj);
        						// Because the code block was found in 3 places in this, I moved it to it's own method.
        						displayResults();
        					} else if (((Street)msg.obj).compareLatLng(global.getTollData().getFinish())){
        						global.getTollData().setFinish(null);
        						finishShown=false;
        						
        						newMessage = handler.obtainMessage();
        						newMessage.what=6;
        						handler.sendMessage(newMessage);
        					}
    					} else if (((Street)msg.obj).compareLatLng(global.getTollData().getStart()))
    						resetView();
    				}
    				
    				if ((mMapFragment!=null)&&
    					(mapFragmentVisible))
						mMapFragment.populateMarkers();
					if ((mTextFragment!=null)&&
						(textFragmentVisible))
						mTextFragment.populateStreets();
    				break;
    			case 10:
    				resetView();
    				break;
    			case 12:
    				setView();
    				break;
    		}
    	}
    };
}