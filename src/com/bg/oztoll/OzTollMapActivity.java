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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Html;

/**
 * @author bugman
 *
 */
public class OzTollMapActivity extends Activity {
	private OzTollView ozView;
	private Dialog rateDialog, startDialog;
	private SharedPreferences preferences;
	private Activity thisActivity=this;

	public OzTollMapActivity(){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		
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
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if (preferences.getBoolean("firstRun", true)){
			Message msg = handler.obtainMessage();
			msg.what=4;
			
			handler.sendMessage(msg);
			SharedPreferences.Editor edit = preferences.edit();
			edit.putBoolean("firstRun", false);
			edit.commit();
		}
		
		if (preferences.getBoolean("applicationView", true)){
			// if view is mapView
			ozView = new OzTollView(this, global.getTollData(), handler);
			setContentView(ozView);
		} else {
			// if user has just changed the preference to text view
			setResult(1);
			finish();
		}
    }
    
    final Handler handler = new Handler(){
    	public void handleMessage(Message msg){
    		switch (msg.what){
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
    		}
    	}
    };
}
