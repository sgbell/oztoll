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

	public boolean compareLatLng(LatLng otherLatLng){
		if ((otherLatLng!=null)&&(otherLatLng.latitude==latLng.latitude)&&
			(otherLatLng.longitude==latLng.longitude))
			return true;
		else
			return false;
	}
}
