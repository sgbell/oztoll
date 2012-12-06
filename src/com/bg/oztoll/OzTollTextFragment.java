/**
 * 
 */
package com.bg.oztoll;

import android.os.Bundle;
import android.os.Handler;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class OzTollTextFragment extends SherlockFragment {

	public static final String TAG = "ozTollTextFragment";
	
	private OzTollApplication global;
	private Handler handler;
	
	public OzTollTextFragment(){
		
	}
	
	public OzTollTextFragment(Handler mainHandler){
		handler = mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		
	}
	
	public void onResume(){
		super.onResume();
		
	}
}
