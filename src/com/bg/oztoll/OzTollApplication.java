/** 
 * OzTollApplication is an extension of the Application class, so that I can then offload
 * the data loading to a service, which will allow the data to be loaded once, for either of the activities.
 */
package com.bg.oztoll;

import android.app.Application;

/**
 * @author bugman
 *
 */
public class OzTollApplication extends Application {
	private OzTollData tollData;
	private Object datasync, viewChange;
	private boolean isMapViewStarted=false, isTextViewStarted=false;
	
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
	}

	public Object getDatasync() {
		return datasync;
	}

	public void setDatasync(Object syncObject) {
		datasync = syncObject;
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
