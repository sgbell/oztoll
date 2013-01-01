/**
 * 
 */
package com.mimpidev.oztoll;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class ResultsFragment extends SherlockFragment {

	public static final String TAG = "resultsFragment";
	private Handler handler;
	private Button okButton;
	private ScrollView content;
	private View view;
	
	private OzTollApplication global;
	
	public ResultsFragment(){
	}
	
	public ResultsFragment(Handler mainHandler){
		handler = mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		global = (OzTollApplication)getSherlockActivity().getApplication();
	}
	
	public void onResume(){
		super.onResume();

		global = (OzTollApplication)getSherlockActivity().getApplication();
		handler = global.getMainActivityHandler();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		view = inflater.inflate(R.layout.ratedialog, null, false);
		content = (ScrollView)view.findViewById(R.id.scrollView);
		okButton = (Button)view.findViewById(R.id.close);
		okButton.setText("Clear");
		okButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// call handler to reset view
				Message newMessage = handler.obtainMessage();
				newMessage.what = 10;
				handler.dispatchMessage(newMessage);
				
				getScrollView().removeAllViews();
				
				if (!(getResources().getBoolean(R.bool.isTablet))){
					//call handler to change view
					newMessage = handler.obtainMessage();
					newMessage.what = 12;
					handler.dispatchMessage(newMessage);
				}
			}
		});
		
		return view;
	}
	
	public void onDestroyView(){
		super.onDestroyView();
		((ViewGroup)view.getParent()).removeView(view);
	}
	
	public ScrollView getScrollView(){
		return content;
	}
	
	public void setContent(LinearLayout newView){
		content.addView(newView);
	}
}
