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
import android.widget.LinearLayout;
import android.widget.TextView;

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
				
				msg = mainHandler.obtainMessage();
				msg.what=1;
				String heading = "Please select Exit Street";
				msg.obj = heading;
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
				handler.sendEmptyMessage(1);
			}
			shownExits=true;
		}
	}
	
	
	public void showDialog(){
		
	}
	
	public void populateStreets(){
		for (int twc=0; twc<tollData.getTollwayCount(); twc++)
			for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
				if (((!tollData.getStreet(twc, sc).isValid()) && (start==null))||
					((start!=null)&&(tollData.getStreet(twc, sc).isValid()))){
					adapter.addStreet(tollData.getTollwayName(twc), tollData.getStreetName(twc, sc));
				}
			}
		
		handler.sendEmptyMessage(1);
	}

	@Override
	public void run() {
		// working here, need to grab heading textview and give the user instructions.
		//TextView heading = 
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
				Message msg = mainHandler.obtainMessage();
				String heading = "Please Select Starting Street";
				msg.what = 1;
				msg.obj= heading;
				mainHandler.sendMessage(msg);
			} else {
				if (adapter.getGroupCount()<1){
					populateStreets();
					Message msg = mainHandler.obtainMessage();
					String heading = "Please Select Starting Street";
					msg.what = 1;
					msg.obj= heading;
					mainHandler.sendMessage(msg);
				}
				synchronized (threadSync){
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

	public void setListView(ExpandableListView exListView) {
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
