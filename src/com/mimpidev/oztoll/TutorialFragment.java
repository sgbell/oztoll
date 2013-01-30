/**
 * 
 */
package com.mimpidev.oztoll;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
	private ImageView imageWindow;
	private Bitmap image;
	private int screenCount = 0;
	private LinearLayout tutorialWindowLayout;

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
				if (screenCount<4){
					screenCount++;
					processView();
				} else {
					// send handler message to close tutorial and show normal screen
					Message newMessage = handler.obtainMessage();
					newMessage.what=1;
					handler.dispatchMessage(newMessage);
					screenCount=1;
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
				tutorialWindowLayout.removeAllViews();
				processView();
				close.setVisibility(Button.VISIBLE);
				next.setVisibility(Button.VISIBLE);
			}
		};
		
		handler.postDelayed(clearSplash, 4000);
		
		return view;
	}
	
	private void processView(){
		
		switch (screenCount){
			case 0:
				// Draw stuff on the screen to tell the user what stuff is and what it does
				
				previous.setVisibility(Button.INVISIBLE);
				break;
		}
	}
}
