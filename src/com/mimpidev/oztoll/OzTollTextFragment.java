/**
 * 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
					/*
					if (global.getTollData().getStart()==null){
						global.getTollData().setStart(global.getTollData().getStreet(tollway, street));
						populateStreets();
	        			TextView startStreet = (TextView)getView().findViewById(R.id.startStreet);
	        			startStreet.setText("Start Street: "+global.getTollData().getStart().getName());
						
						Message newMessage = handler.obtainMessage();
						newMessage.what=6;
						handler.dispatchMessage(newMessage);
					}else if (global.getTollData().getFinish()==null){
						global.getTollData().setFinish(global.getTollData().getStreet(tollway, street));
						LinearLayout rateLayout = global.getTollData().processToll(global.getBaseContext());
						Message newMessage = handler.obtainMessage();
						newMessage.obj = rateLayout;
						newMessage.what=3;
						handler.sendMessage(newMessage);
					}*/
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
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
						if (tollData.getStreet(twc, sc).isValid()){
							adapter.addStreet(tollData.getTollwayName(twc), tollData.getStreetName(twc, sc));
						}
					}
				}
			} else {
				ArrayList<Street> validExits = tollData.getTollPointExits(tollData.getStart());
				String tollway = tollData.getTollwayName(tollData.getStart());

				for (int sc=0;sc<validExits.size();sc++){
					adapter.addStreet(tollway, validExits.get(sc).getName());
					
					for (int cc=0; cc<tollData.getConnectionCount(); cc++){
						if ((tollData.getConnection(cc).getStart().equals(validExits.get(sc)))||
							(tollData.getConnection(cc).getEnd().equals(validExits.get(sc)))){
							ArrayList<Street> childValidExits;
							String otherTollway;
							if (tollData.getConnection(cc).getStart().equals(validExits.get(sc))){
								childValidExits = tollData.getTollPointExits(tollData.getConnection(cc).getEnd());
								otherTollway = tollData.getConnection(cc).getEndTollway();
							} else {
								childValidExits = tollData.getTollPointExits(tollData.getConnection(cc).getStart());
								otherTollway = tollData.getConnection(cc).getStartTollway();
							}
							for (int csc=0; csc<childValidExits.size();csc++){
								adapter.addStreet(otherTollway, childValidExits.get(csc).getName());
							}
						}
					}
				}
			}
			collapseGroups();

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
