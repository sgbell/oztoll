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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
	private TextView disclaimer,
					 expireDate;
	private	LinearLayout rateLayout;

	
	private OzTollApplication global;
	
	public ResultsFragment(){
	}
	
	public ResultsFragment(Handler mainHandler){
		handler = mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		global = (OzTollApplication)getSherlockActivity().getApplication();
		
		disclaimer = new TextView(getActivity().getBaseContext());
		expireDate = new TextView(getActivity().getBaseContext());
	}
	
	public void onResume(){
		super.onResume();

		global = (OzTollApplication)getSherlockActivity().getApplication();
		if (getResources().getBoolean(R.bool.isTablet))
			handler = global.getMainActivityHandler();
		
		if (!getResources().getBoolean(R.bool.isTablet)){
			rateLayout = global.getTollData().processToll(getActivity().getBaseContext());

			if (disclaimer.getParent()!=null)
				((ViewGroup)disclaimer.getParent()).removeView(disclaimer);
			if (expireDate.getParent()!=null)
				((ViewGroup)expireDate.getParent()).removeView(expireDate);
			disclaimer.setText(Html.fromHtml(getString(R.string.toll_disclaimer)));
			disclaimer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
			rateLayout.addView(disclaimer);
			expireDate.setText(Html.fromHtml("<h2>Tolls Valid until "+global.getTollData().getSelectedExpiryDate()+"</h2>"));
			expireDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
			rateLayout.addView(expireDate);

			getScrollView().removeAllViews();
			setContent(rateLayout);
		}
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
				
				if (!(getResources().getBoolean(R.bool.isTablet))){
					//call handler to change view
					newMessage = handler.obtainMessage();
					newMessage.what = 12;
					handler.dispatchMessage(newMessage);
				} else {
					getScrollView().removeAllViews();
				}
			}
		});

		return view;
	}
	
	public void onDestroyView(){
		super.onDestroyView();
		((ViewGroup)view.getParent()).removeView(view);
		if (rateLayout!=null)
			if (rateLayout.getParent()!=null)
				((ViewGroup)rateLayout.getParent()).removeView(rateLayout);
	}
	
	public ScrollView getScrollView(){
		return content;
	}
	
	public void setContent(LinearLayout newView){
		content.addView(newView);
	}

	public void setHandler(Handler handler2) {
		handler = handler2;
	}
}
