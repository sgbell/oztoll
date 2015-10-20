/**
 * 
 * Copyright (C) 2015  Sam Bell
 * @email - sam@mimpidev.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or  any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sam Bell - initial API and implementation
 * 
 */
/** 
 * OzTollApplication is an extension of the Application class, so that I can then offload
 * the data loading to a service, which will allow the data to be loaded once, for either of the activities.
 */
package com.mimpidev.oztoll;

import android.app.Application;
import android.os.Handler;

/**
 * @author bugman
 *
 */
public class OzTollApplication extends Application {
	private OzTollData tollData;
	private Object datasync, viewChange;
	private boolean isMapViewStarted=false, // This is used to tell the program if mapView has been started 
					isTextViewStarted=false, // This is used to tell the program if TextView has been started
					tollDataLoaded=false;
	private Handler mainActivityHandler;
	
	/**
	 * 
	 */
	public OzTollApplication() {
		super();
		tollData = new OzTollData();
		datasync = new Object();
	}
	
	public OzTollData getTollData(){
		return tollData;
	}
	
	public void setTollData(OzTollData newData){
		newData.setStart(tollData.getStartStreet());
		newData.setFinish(tollData.getFinishStreet());
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

	public Handler getMainActivityHandler() {
		return mainActivityHandler;
	}

	public void setMainActivityHandler(Handler mainActivityHandler) {
		this.mainActivityHandler = mainActivityHandler;
	}

	/**
	 * @return the tollDataLoaded
	 */
	public boolean isTollDataLoaded() {
		return tollDataLoaded;
	}

	/**
	 * @param tollDataLoaded the tollDataLoaded to set
	 */
	public void setTollDataLoaded(boolean tollDataLoaded) {
		this.tollDataLoaded = tollDataLoaded;
	}
}
