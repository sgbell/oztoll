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
					
					for (int cc=0; cc<tollData.getConnectionCount(); cc++){
						if (((tollData.getConnection(cc).getStartTollway().equalsIgnoreCase(tollway))&&
							 (tollData.getConnection(cc).getStart().equals(start)))||
							 ((tollData.getConnection(cc).getEndTollway().equalsIgnoreCase(tollway))&&
							  (tollData.getConnection(cc).getEnd().equals(start)))){
							ArrayList<Street> childValidExits;
							String otherTollway;
							if (tollData.getConnection(cc).getStart().equals(start)){
								childValidExits = tollData.getTollPointExits(tollData.getConnection(cc).getEnd());
								otherTollway = tollData.getConnection(cc).getEndTollway();
							} else {
								childValidExits = tollData.getTollPointExits(tollData.getConnection(cc).getStart());
								otherTollway = tollData.getConnection(cc).getStartTollway();
							}
							for (int csc=0; csc<childValidExits.size();csc++){
								adapter.addStreet(otherTollway, childValidExits.get(sc).getName());
							}
						}
					}
				}
				handler.sendEmptyMessage(1);
			}
			shownExits=true;
		}
	}
	
	
	public void showDialog(){
		
	}
	
	public void populateStreets(){
		Log.w ("ozToll","populateStreets() called");
		for (int twc=0; twc<tollData.getTollwayCount(); twc++)
			for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
				if (((!tollData.getStreet(twc, sc).isValid()) && (start==null))||
					((start!=null)&&(tollData.getStreet(twc, sc).isValid()))){
					adapter.addStreet(tollData.getTollwayName(twc), tollData.getStreetName(twc, sc));
					Log.w ("ozToll",tollData.getStreetName(twc,sc)+" added");
				}
			}
		
		handler.sendEmptyMessage(1);
	}

	@Override
	public void run() {
		boolean stillRunning=true;
		while (stillRunning){
			if (!tollData.isFinished()){
				synchronized (tollData.getDataSync()){
					try {
						tollData.getDataSync().wait();
					} catch (InterruptedException e){
						// nothing
					}
				}
				populateStreets();
			} else {
				if (adapter.getGroupCount()<1)
					populateStreets();
				synchronized (threadSync){
					Log.w ("ozToll","threadSync sleep");
					try {
						threadSync.wait();
					} catch (InterruptedException e){
						// do nothing again
					}
				}
				if (start!=null){
					if (finish!=null){
						showDialog();
					} else {
						showExits();
					}
				}
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
					Log.w ("OzTollTextView","start:"+start.getName());
					synchronized (threadSync){
						threadSync.notify();
					}
				}else if (finish==null){
					finish=tollData.getStreet(tollway, street);
					synchronized (threadSync){
						threadSync.notify();
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
