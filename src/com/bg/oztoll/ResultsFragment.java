/**
 * 
 */
package com.bg.oztoll;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class ResultsFragment extends SherlockFragment {

	public static final String TAG = "resultsFragment";
	
	private OzTollApplication global;
	
	public ResultsFragment(){
		
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		global = (OzTollApplication)getSherlockActivity().getApplication();
	}
	
	public void onResume(){
		super.onResume();
		
		
	}
}
