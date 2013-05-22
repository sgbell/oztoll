/**
 * 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class OzTollTextFragment extends SherlockFragment {

	public static final String TAG = "ozTollTextFragment";

	private ExpandableListAdapter adapter;
	private ExpandableListView listView;
	private TextView startStreet;
	private View view;
	
	private OzTollApplication global;
	private Handler handler;
	private SharedPreferences preferences;
	
	
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

		global = (OzTollApplication)getSherlockActivity().getApplication();
		handler = global.getMainActivityHandler();
		preferences = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity().getBaseContext());
		
		Message newMessage = handler.obtainMessage();
		newMessage.what = 5;
		handler.dispatchMessage(newMessage);
		synchronized(global.getDatasync()){
			while (!global.getTollData().isFinished()){
				newMessage = handler.obtainMessage();
				newMessage.what = 5;
				handler.dispatchMessage(newMessage);
				try {
					global.getDatasync().wait();
				} catch (InterruptedException e) {
					
				}
			}
		}

    	// Create a handler message here to tell OzTollActivity to do the resetView();
		newMessage = handler.obtainMessage();
		//newMessage.what = 10;
		//handler.dispatchMessage(newMessage);

		if (global.getTollData().getStart()!=null)
			setStart("Start Street: "+global.getTollData().getStart().getName());
		else
			setStart("");
		populateStreets();

		//newMessage = handler.obtainMessage();
		newMessage.what = 6;
		handler.dispatchMessage(newMessage);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		super.onCreateView(inflater, vg, savedInstanceBundle);
		view = inflater.inflate(R.layout.textrate, null); 
		
		adapter = new ExpandableListAdapter(getSherlockActivity().getApplicationContext(), new ArrayList<String>(),
				new ArrayList<ArrayList<String>>());
    	setListView((ExpandableListView)view.findViewById(R.id.streetList));
    	startStreet = (TextView)view.findViewById(R.id.startStreet);

		return view;
	}
	
	public void onDestroyView(){
		super.onDestroyView();
		((ViewGroup)view.getParent()).removeView(view);
	}
	
	public void setListView(ExpandableListView exListView) {
		listView = exListView;
		listView.setAdapter(adapter);
		
		listView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (global.getTollData().isFinished()){
					String tollway=(String)adapter.getGroup(groupPosition);
					String street=(String)adapter.getChild(groupPosition, childPosition);

					Message newMessage = handler.obtainMessage();
					newMessage.what=9;
					newMessage.obj=global.getTollData().getStreet(tollway, street);
					handler.dispatchMessage(newMessage);

				}

				return true;
			}
		});
	}

	public void populateStreets(){
		if (global!=null){
			OzTollData tollData = global.getTollData();
			
			adapter.resetView();

			if (tollData.getStart()==null){
				/* Need to check if a city has been set in the preferences.
				 * 
				 * If no city has been selected, or the city selected is not found
				 * in the city list, list all tollways and entrances.
				 * 
				 * If a city has been selected in the preferences, only list the tollways for that
				 * city.
				 *
				 * Rewrite the code below in OzTollData. Make a method in OzTollData that:
				 *    - takes a string for input
				 *    - if the string is empty, creates and returns an array of Tollway+Street from all cities
				 *    - if the string is not empty, matches the string to the city name, then creates and returns an array of Tollway+Street from nominated city
				 *    - if the string is not empty, but no match is found for city, returns array of Tollways from all cities.
				 * 
				 */
				ArrayList<String[]> streetList = tollData.getValidStreetsAsStrings(preferences.getString("selectedCity", ""));
				for (int streetListCount=0; streetListCount< streetList.size(); streetListCount++){
					adapter.addStreet(streetList.get(streetListCount)[0], streetList.get(streetListCount)[1]);
				}
				collapseGroups();
			} else {
				ArrayList<Street> validExits = tollData.getTollPointExits(tollData.getStart());
				String tollway = tollData.getTollwayName(tollData.getStart());

				adapter.addStart(tollway, tollData.getStart().getName());
				for (int sc=0;sc<validExits.size();sc++){
					adapter.addStreet(tollway, validExits.get(sc).getName());
				}
			}

			adapter.notifyDataSetChanged();
		}
	}

	public void collapseGroups(){
		handler.post(new Runnable(){

			@Override
			public void run() {
				for (int groupCount=0; groupCount < adapter.getGroupCount(); groupCount++)
					listView.collapseGroup(groupCount);
			}
		});
	}

	public void setStart(String string) {
		if (startStreet!=null)
			startStreet.setText(string);
	}
}
