/**
 * 
 */
package com.mimpidev.oztoll;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class TutorialFragment extends SherlockFragment {
	private OzTollApplication global;
	private Handler handler;
	private SharedPreferences preferences;
	
	private Button close, previous, next;
	private int screenCount = 0;
	private LinearLayout tutorialWindowLayout;
	private View tutorial_part_1,
	             tutorial_part_2;

	public static final String TAG = "TutorialFragment";
	
	public TutorialFragment(){
	}
	
	public TutorialFragment(Handler mainHandler){
		handler=mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);

		global = (OzTollApplication)getSherlockActivity().getApplication();
		preferences = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity().getBaseContext());
	}
	
	public void onResume(){
		super.onResume();

		global = (OzTollApplication)getSherlockActivity().getApplication();
		handler = global.getMainActivityHandler();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		View view = inflater.inflate(R.layout.tutorial_view, null, false);
		/* Build new interface here inserting r.layout.splash into r.id.tutorial
		*  On user touch, it will then remove the r.layout.splash & begin a tutorial
		*  identifying the important parts of the program including "Clear", "Settings" buttons,
		*  which streets are selected, and which are not. How to clear the screen.
		*/
		tutorialWindowLayout = (LinearLayout)view.findViewById(R.id.tutorial);
		View splashScreen = inflater.inflate(R.layout.splash, null, false);
		tutorialWindowLayout.addView(splashScreen);
		tutorial_part_1 = inflater.inflate(R.layout.tutorial_part_1, null, false);
		tutorial_part_2 = inflater.inflate(R.layout.tutorial_part_2, null, false);
		
		// grab the buttons which we had in the code before
		close = (Button)view.findViewById(R.id.button_close);
		previous = (Button)view.findViewById(R.id.button_prev);
		next = (Button)view.findViewById(R.id.button_next);
		
		close.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// send handler message to close tutorial and show normal screen
				Message newMessage = handler.obtainMessage();
				newMessage.what=1;
				handler.dispatchMessage(newMessage);
				screenCount=0;
			}
			
		});
		next.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if (screenCount<1){
					screenCount++;
					processView();
				} else {
					// send handler message to close tutorial and show normal screen
					Message newMessage = handler.obtainMessage();
					newMessage.what=1;
					handler.dispatchMessage(newMessage);
					screenCount=0;
					processView();
				}
			}
			
		});
		
		previous.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if (screenCount>1){
					screenCount--;
					processView();
				}
			}
			
		});

		
		// This handles the splash screen
		Runnable clearSplash = new Runnable(){
			@Override
			public void run() {
				processView();
			}
		};
		
		handler.postDelayed(clearSplash, 4000);
		
		return view;
	}
	
	private void processView(){
		// need to set tutorial view in preferences
		
		switch (screenCount){
			case 0:
				// Draw stuff on the screen to tell the user what stuff is and what it does
				tutorialWindowLayout.removeAllViews();
				tutorialWindowLayout.addView(tutorial_part_1);

				close.setVisibility(Button.VISIBLE);
				next.setVisibility(Button.VISIBLE);
				previous.setVisibility(Button.INVISIBLE);
				
				break;
			case 1:
				tutorialWindowLayout.removeAllViews();
				tutorialWindowLayout.addView(tutorial_part_1);
				tutorialWindowLayout.addView(tutorial_part_2);
				
				previous.setVisibility(Button.VISIBLE);
				break;
		}
	}
}
