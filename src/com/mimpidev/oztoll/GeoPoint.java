/**
 * 
 */
package com.mimpidev.oztoll;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author bugman
 *
 */
public class GeoPoint {
	private LatLng latLng;
	
	public GeoPoint(int latitude, int longitude) {
		setLatLng(new LatLng((double)(latitude/1E6), (double)(longitude/1E6)));
	}
	
	public GeoPoint(double latitude, double longitude){
		setLatLng(new LatLng(latitude/1E6, longitude/1E6));
	}
	
	public GeoPoint(){
	}

	/**
	 * @return the latLng
	 */
	public LatLng getLatLng() {
		return latLng;
	}

	/**
	 * @param latLng the latLng to set
	 */
	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}

}
