package com.bg.oztoll;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class OzTollActivity extends Activity {
	private OzTollData tollData;
	private OzTollView ozView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ozView = new OzTollView(this);
        setContentView(ozView);
        
		// Creates a new OzStorage object, and gets the ozTollData object it creates
        tollData = new OzStorage().getTollData();
        // passes ozTollData into ozTollView
		ozView.setDataFile(tollData);
    }

}