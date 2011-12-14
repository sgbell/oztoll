package com.bg.oztoll;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

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
        
		tollData = new OzStorage().getTollData();
		ozView.setDataFile(tollData);
    }

}