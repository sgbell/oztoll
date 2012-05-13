/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
	private boolean shownExits;
	private Object threadSync;
	
	public OzTollTextView(){
		
	}

	public OzTollTextView(OzTollData data) {
		tollData = data;
		shownExits=false;
		threadSync= new Object();
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
	
	public void showExits(){
		if (!shownExits){
			Log.w ("OzTollTextView","showExits() called");
			if (start!=null){
				Log.w ("OzTollTextView","Start="+start.getName());
				adapter.resetView();
				ArrayList<Street> validExits = tollData.getTollPointExits(start);
				Connection connectingRoad = null;
				boolean connectionFound=false;
				String tollway = tollData.getTollwayName(start);
				Log.w ("OzTollTextView","showExits(): validExits.size()="+validExits.size());

				for (int sc=0;sc<validExits.size();sc++){
					adapter.addStreet(tollway, validExits.get(sc).getName());
					Log.w ("OzTollTextView","showExits(): addStreets="+validExits.get(sc).getName());
					int cc=0;
					if (!connectionFound){
						while ((cc<tollData.getConnectionCount())&&
								(!connectionFound)){
							if ((tollData.getConnection(cc).getStartTollway().equalsIgnoreCase(tollway))&&
								((tollData.getConnection(cc).getStart().equals(start))||
								 (tollData.getConnection(cc).getEnd().equals(start)))){
								connectionFound=true;
							} else {
								cc++;
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
			shownExits=true;
		}
	}
	
	
	public void showDialog(){
		
	}
	
	public void populateStreets(){
		if (!tollData.isFinished()){
			Log.w ("OzTollTextView","populateStreets(): before dataSync.wait()");
			synchronized(tollData.getDataSync()){
				try {
					if (!tollData.isFinished()){
						tollData.getDataSync().wait();
					}
				} catch (InterruptedException e){
					// just wait for it
				}
			}
			Log.w ("OzTollTextView","populateStreets(): after dataSync.wait()");
			for (int twc=0; twc<tollData.getTollwayCount(); twc++)
				for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
					adapter.addStreet(tollData.getTollwayName(twc), tollData.getStreetName(twc, sc));
				}
		} else {
			if (start!=null){
				if (finish==null)
					showExits();
				else
					showDialog();
			}
		}
		
		handler.sendEmptyMessage(1);
	}

	@Override
	public void run() {
		boolean stillRunning=true;
		while (stillRunning){
			if (tollData.isFinished()){
				synchronized (threadSync){
					try {
						threadSync.wait();
					} catch (InterruptedException e){
						// do nothing
					}
				}
			}
			populateStreets();
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
				Log.w ("OzTollTextView","onChildClick() called");
				if (tollData.isFinished()){
					synchronized (tollData.getDataSync()){
						tollData.getDataSync().notify();
					}
					synchronized (threadSync){
						threadSync.notify();
					}
				}
				String tollway=(String)adapter.getGroup(groupPosition);
				String street=(String)adapter.getChild(groupPosition, childPosition);
				if (start==null){
					start=tollData.getStreet(tollway, street);
					Log.w ("OzTollTextView","start:"+start.getName());
				}else if (finish==null){
					finish=tollData.getStreet(tollway, street);
				}

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
