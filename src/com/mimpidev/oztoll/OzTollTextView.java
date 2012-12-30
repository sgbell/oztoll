/**
 * 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class OzTollTextView implements Runnable{
	private OzTollData tollData;
	private Context appContext;
	private ExpandableListView listView;
	private ExpandableListAdapter adapter;
	private Street start=null, finish=null;
	private boolean shownExits;
	private Object threadSync;
	private Handler mainHandler;
	
	public OzTollTextView(Context context, OzTollData ozTollData, Handler handler2){
		this(context,ozTollData);
		mainHandler=handler2;
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

	public void showExits(){
		if (!shownExits){
			if (start!=null){
				Message msg = mainHandler.obtainMessage();
				msg.what=2;
				String startText = "Start Street: "+start.getName();
				msg.obj = startText;
				mainHandler.sendMessage(msg);
				
				adapter.resetView();
				ArrayList<Street> validExits = tollData.getTollPointExits(start);
				String tollway = tollData.getTollwayName(start);

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
				collapseGroups();
				
				handler.sendEmptyMessage(1);
				
		    	Message newMessage = mainHandler.obtainMessage();
				newMessage.what = 6;
				mainHandler.dispatchMessage(newMessage);

			}
			shownExits=true;
		}
	}
	
	public void collapseGroups(){
		mainHandler.post(new Runnable(){

			@Override
			public void run() {
				for (int groupCount=0; groupCount < adapter.getGroupCount(); groupCount++)
					listView.collapseGroup(groupCount);
			}
		});
	}
	
	public void showDialog(){
		Message msg = mainHandler.obtainMessage();
		msg.what=3;
		LinearLayout rateLayout = tollData.processToll(start, finish, appContext);
		msg.obj=rateLayout;	
		
		mainHandler.sendMessage(msg);
		finish=null;
	}
	
	public void populateStreets(){
		Log.w ("ozToll","OzTollTextView.populateStreets() called");
		for (int twc=0; twc<tollData.getTollwayCount(); twc++)
			for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
				if (tollData.getStreet(twc, sc).isValid()){
					adapter.addStreet(tollData.getTollwayName(twc), tollData.getStreetName(twc, sc));
					Log.w ("ozToll","OzTollTextView.populateStreets() - Added :"+tollData.getStreetName(twc, sc));
				}
			}
		
		collapseGroups();
		Log.w ("ozToll","OzTollTextView.populateStreets() finished");
	}


	public void reset(){
		start=null;
		finish=null;
		shownExits=false;
		
		adapter.resetView();
		handler.sendEmptyMessage(1);
		synchronized (threadSync){
			threadSync.notify();
		}
	}
	
	@Override
	public void run() {
		Log.w ("ozToll","OzTollTextView.run() called");
		// working here, need to grab heading textview and give the user instructions.
		//TextView heading = 
		boolean stillRunning=true;
		while (stillRunning){
			populateStreets();
			
			Message newMessage = mainHandler.obtainMessage();
			newMessage.what = 6;
			mainHandler.dispatchMessage(newMessage);

			if (adapter.getGroupCount()<1){
				populateStreets();
			}
				
				
			synchronized (threadSync){
				try {
					Log.w ("ozToll","OzTollTextView.threadSync sleep");
					threadSync.wait();
				} catch (InterruptedException e){
					// do nothing again
				}
			}
			Log.w ("ozToll","OzTollTextView.threadSync awake");
			if (start!=null){
				if (finish!=null){
					showDialog();
				} else {
					showExits();
				}
			}
		}
	}

	public ExpandableListView getExListView() {
		return listView;
	}

	public void setListView(ExpandableListView exListView) {
		listView = exListView;
		listView.setAdapter(adapter);
		
		listView.setOnChildClickListener(new OnChildClickListener(){

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (tollData.isFinished()){
					String tollway=(String)adapter.getGroup(groupPosition);
					String street=(String)adapter.getChild(groupPosition, childPosition);
					if (start==null){
						start=tollData.getStreet(tollway, street);
						if (start!=null){
							synchronized (threadSync){
								threadSync.notify();
							}
						}
					}else if (finish==null){
						finish=tollData.getStreet(tollway, street);
						synchronized (threadSync){
							threadSync.notify();
						}
					}
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
