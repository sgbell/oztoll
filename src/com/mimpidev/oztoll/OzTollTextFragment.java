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

		if (global.getTollData().getStart()!=null){
			global.getTollData().setStreetsToInvalid();
			global.getTollData().markRoads(global.getTollData().getStart());
		}

		populateStreets();

		newMessage.what = 6;
		handler.dispatchMessage(newMessage);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		super.onCreateView(inflater, vg, savedInstanceBundle);
		view = inflater.inflate(R.layout.textrate, null); 
		
		adapter = new ExpandableListAdapter(getSherlockActivity().getApplicationContext(), new ArrayList<String>(),
				new ArrayList<ArrayList<String>>());
    	setListView((ExpandableListView)view.findViewById(R.id.streetList));

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
				 */
				
				String city = preferences.getString("selectedCity", "");
				
				if ((city.isEmpty())||(city==null)){
					for (int cityCount=0; cityCount<tollData.getCities().size(); cityCount++)
						for (int twc=0; twc<tollData.getCityById(cityCount).getTollwayCount(); twc++)
							for (int sc=0; sc<tollData.getCityById(cityCount).getStreetCount(twc); sc++)
								if (tollData.getCityById(cityCount).getStreet(twc, sc).isValid()){
									adapter.addStreet(tollData.getCityById(cityCount).getTollwayName(twc),
													 tollData.getCityById(cityCount).getStreetName(twc, sc));
								}
				} else {
					boolean cityFound=false;
					int cityCount=0;
					while ((!cityFound)&&(cityCount<tollData.getCities().size())){
						if (tollData.getCityById(cityCount).getCityName().equalsIgnoreCase(city)){
							cityFound=true;
							
							for (int twc=0; twc<tollData.getCityById(cityCount).getTollwayCount(); twc++)
								for (int sc=0; sc<tollData.getCityById(cityCount).getStreetCount(twc); sc++)
									if (tollData.getCityById(cityCount).getStreet(twc, sc).isValid()){
										adapter.addStart(tollData.getCityById(cityCount).getTollwayName(twc),
												 tollData.getCityById(cityCount).getStreetName(twc, sc));
									}
						}
						cityCount++;
					}
				}
				
				collapseGroups();
			} else {
				ArrayList<Street> validExits = tollData.getTollPointExits(tollData.getStart());
				String tollway = tollData.getTollwayName(tollData.getStart());

				adapter.addStart(tollway, tollData.getStart().getName());
				
				for (int sc=0;sc<validExits.size();sc++)
					adapter.addStreet(tollway, validExits.get(sc).getName());
								
				expandGroups();
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
	
	public void expandGroups(){
		handler.post(new Runnable(){

			@Override
			public void run() {
				for (int groupCount=0; groupCount < adapter.getGroupCount(); groupCount++)
					listView.expandGroup(groupCount);
			}
		});
	}
}
