/**
 * 
 * Copyright (C) 2015  Sam Bell
 * @email - sam@mimpidev.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or  any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sam Bell - initial API and implementation
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
import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class TutorialFragment extends SherlockFragment {
	private OzTollApplication global;
	private Handler handler;
	
	private Button finish;
	private LinearLayout tutorialWindowLayout;
	private View tutorial_part_1;

	public static final String TAG = "TutorialFragment";
	
	public TutorialFragment(){
	}
	
	public TutorialFragment(Handler mainHandler){
		handler=mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);

		global = (OzTollApplication)getSherlockActivity().getApplication();
	}
	
	public void onResume(){
		super.onResume();

		global = (OzTollApplication)getSherlockActivity().getApplication();
		if (handler==null)
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
		tutorial_part_1 = inflater.inflate(R.layout.tutorial_part_1, null, false);
		
		// grab the buttons which we had in the code before
		finish = (Button)view.findViewById(R.id.button_finish);
		
		finish.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// send handler message to close tutorial and show normal screen
				Message newMessage = handler.obtainMessage();
				newMessage.what=1;
				handler.dispatchMessage(newMessage);
			}
			
		});
		processView();
		return view;
	}
	
	private void processView(){
		// Draw stuff on the screen to tell the user what stuff is and what it does
		tutorialWindowLayout.removeAllViews();
		tutorialWindowLayout.addView(tutorial_part_1);

		finish.setVisibility(Button.VISIBLE);
	}

	public void setHandler(Handler handler) {
		this.handler=handler;
	}
}
