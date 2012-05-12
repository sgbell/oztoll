/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

/**
 * @author bugman
 *
 */
public class OzTollTextView implements Runnable{
	private OzTollData tollData;
	private Context appContext;
	private ExpandableListView listView;
	private ExpandableListAdapter adapter;
	private Street start, finish;
	
	public OzTollTextView(){
		
	}

	public OzTollTextView(OzTollData data) {
		tollData = data;
	}

	public OzTollTextView(Context context, OzTollData data) {
		this(data);
		appContext = context;
		adapter = new ExpandableListAdapter(appContext, new ArrayList<String>(),
				new ArrayList<ArrayList<String>>());
	}

	/*
	public void addStreets(ListView listView) {
		adapter = new ArrayAdapter<String>(appContext, R.layout.list_street, streets);
		listView.setAdapter(adapter);
		
		for (int twc=0; twc<tollData.getTollwayCount(); twc++){
			for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
				streets.add(tollData.getStreetName(twc, sc));
				adapter.notifyDataSetChanged();
			}
		}
	}
	*/
	
	public void populateStreets(){
		for (int twc=0; twc<tollData.getTollwayCount(); twc++)
			for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
				adapter.addStreet(tollData.getTollwayName(twc), tollData.getStreetName(twc, sc));
			}
		
		handler.sendEmptyMessage(1);
	}

	@Override
	public void run() {
		boolean stillRunning=true;
		while (stillRunning){
			if (!tollData.isFinished()){
				populateStreets();
			}
		}
	}

	public ExpandableListView getExListView() {
		return listView;
	}

	public void setExListView(ExpandableListView exListView) {
		listView = exListView;
		listView.setAdapter(adapter);
		
		listView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				String tollway=(String)adapter.getGroup(groupPosition);
				String street=(String)adapter.getChild(groupPosition, childPosition);
				if (start==null){
					start=tollData.getStreet(tollway, street);
					// The code from here down, should be shifted to the thread, so it doesn't lock up the interface. oops
					if (start!=null){
						adapter.resetView();
						ArrayList<Street> validExits = tollData.getTollPointExits(start);
						Connection connectingRoad = null;
						boolean connectionFound=false;
						for (int sc=0;sc<validExits.size();sc++){
							adapter.addStreet(tollway, validExits.get(sc).getName());
							int cc=0;
							if (!connectionFound){
								while ((cc<tollData.getConnectionCount())&&
										(!connectionFound)){
									if ((tollData.getConnection(cc).getStartTollway().equalsIgnoreCase(tollway))&&
										((tollData.getConnection(cc).getStart().getName().equalsIgnoreCase(street))||
										 (tollData.getConnection(cc).getEnd().getName().equalsIgnoreCase(street)))){
										connectionFound=true;
									}
								}
								if (connectionFound){
									connectingRoad=tollData.getConnection(cc);
								}
							}
						}
						if (connectingRoad!=null){
							String otherTollway=null;
							if (connectingRoad.getStartTollway().equalsIgnoreCase(tollway)){
								validExits = tollData.getTollPointExits(connectingRoad.getEnd());
								otherTollway = connectingRoad.getEndTollway();
							}else{
								validExits = tollData.getTollPointExits(connectingRoad.getStart());
								otherTollway = connectingRoad.getStartTollway();
							}
							for (int sc=0; sc<validExits.size();sc++){
									adapter.addStreet(otherTollway, validExits.get(sc).getName());
							}
						}
					}
				} else {
					
				}
				
				handler.sendEmptyMessage(1);
				return true;
			}
		});
	}
	
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg){
			adapter.notifyDataSetChanged();
			super.handleMessage(msg);
		}
	};
}
