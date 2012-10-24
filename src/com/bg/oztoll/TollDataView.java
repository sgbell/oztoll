/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class TollDataView implements Runnable{
	private Object syncObject, 
	               dataSync,
	               moveSync;
	private OzTollData tollData;
	private String cityName;
	private boolean stillRunning, 
					rateCalculated=false;
	private Context appContext;
	private LinearLayout rateLayout;
	private Handler mainHandler;

	public TollDataView(){
		syncObject = new Object();
		moveSync = new Object();
	}
	
	public TollDataView(OzTollData data){
		this();
		//the above line
		tollData=data;
		cityName = "";		
		
		dataSync = tollData.getDataSync();
	}
	
	public TollDataView(OzTollData data, int height, int width, Context context){
		this(data);
		// the above line is involved
		appContext=context;
	}
	
	public Object getMoveSync(){
		return moveSync;
	}
	
	public Object getSync(){
		return syncObject;
	}
	
	public void setDataSync(Object syncMe){
		dataSync = syncMe;
	}
	
	public Object getDataSync(){
		return dataSync;
	}
	
	
	@Override
	public void run() {
		stillRunning=true;
		while (stillRunning){
			synchronized (dataSync){
				try {
					// Put in this condition so that if tollData has finished reading the file, the app wont be put to sleep
					// waiting for the data to be read
					dataSync.wait();
				} catch (InterruptedException e) {
					// just wait for it
				}
			}
			if (!cityName.equalsIgnoreCase(tollData.getCityName()))
				cityName=tollData.getCityName();
			synchronized(syncObject){
				// Populate ItemizedOverlay 
				
			}
		}
	}
	
	public void resetStreets() {
		for (int twc=0; twc < tollData.getTollwayCount(); twc++){
			for (int tec=0; tec < tollData.getStreetCount(twc); tec++){
				tollData.getStreet(twc, tec).setValid(false);
			}
		}

		tollData.setValidStarts();
	}

	public Handler getMainHandler() {
		return mainHandler;
	}

	public void setMainHandler(Handler mainHandler) {
		this.mainHandler = mainHandler;
	}
}
