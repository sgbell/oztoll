package com.bg.oztoll;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public class OzTollActivity extends Activity {
	private OzTollData tollData;
	private OzTollView ozView;
	private Object dataSync;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	dataSync = new Object();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ozView = new OzTollView(this);
        setContentView(ozView);
        
        // Creates a new OzStorage object, and gets the ozTollData object it creates
        tollData = new OzStorage().getTollData();
        tollData.setDataSync(dataSync);
        // passes ozTollData into ozTollView
		ozView.setDataFile(tollData);
    }

    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.layout.menu, menu);
    	
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    		case R.id.settings:
    			break;
    		case R.id.reset:
    			ozView.reset();
    			break;
    	}
    	
    	return true;
    }
}