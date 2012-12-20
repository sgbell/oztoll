/** 
 * OzTollApplication is an extension of the Application class, so that I can then offload
 * the data loading to a service, which will allow the data to be loaded once, for either of the activities.
 */
package com.bg.oztoll;

import android.app.Application;
import android.util.Log;

/**
 * @author bugman
 *
 */
public class OzTollApplication extends Application {
	private OzTollData tollData;
	private Object datasync, viewChange;
	private boolean isMapViewStarted=false, // This is used to tell the program if mapView has been started 
					isTextViewStarted=false; // This is used to tell the program if TextView has been started
	
	/**
	 * 
	 */
	public OzTollApplication() {
		super();
		tollData = new OzTollData();
	}
	
	public OzTollData getTollData(){
		return tollData;
	}
	
	public void setTollData(OzTollData newData){
		tollData = newData;
		Log.w ("ozToll", "OzTollApplication.setTollData()");
	}

	public Object getDatasync() {
		Log.w ("ozToll", "OzTollApplication.getDatasync()");
		return datasync;
	}

	public void setDatasync(Object syncObject) {
		datasync = syncObject;
		Log.w ("ozToll", "OzTollApplication.setDatasync() called");
	}

	public Object getViewChange() {
		return viewChange;
	}

	public void setViewChange(Object viewLock) {
		viewChange = viewLock;
	}

	public boolean isMapViewStarted() {
		return isMapViewStarted;
	}

	public void setMapViewStarted(boolean isMapViewStarted) {
		this.isMapViewStarted = isMapViewStarted;
	}

	public boolean isTextViewStarted() {
		return isTextViewStarted;
	}

	public void setTextViewStarted(boolean isTextViewStarted) {
		this.isTextViewStarted = isTextViewStarted;
	}
}
