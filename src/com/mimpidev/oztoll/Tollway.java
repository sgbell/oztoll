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

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author bugman
 *
 */
public class Tollway {
	private ArrayList<Street> exits;
	private ArrayList<TollPoint> tolls;
	private String name;
		
	public Tollway(){
		exits = new ArrayList<Street>();
		tolls = new ArrayList<TollPoint>();
	}
	
	public Tollway(String name){
		this();
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void addStreet(Street newStreet){
		if (newStreet!=null)
			exits.add(newStreet);
	}
	
	public void addToll(TollPoint tollPoint){
		tolls.add(tollPoint);
	}
	
	public ArrayList<TollPoint> getTollPoints(){
		return tolls;
	}
	
	public ArrayList<Street> getStreets(){
		return exits;
	}
	
	public Street getStreetByName(String streetName){
		for (int sc=0; sc < exits.size(); sc++){
			if (exits.get(sc).getName().equalsIgnoreCase(streetName))
				return exits.get(sc);
		}
		return null;
	}

	public Street getStreetByCoordinates(LatLng latLng) {
		for (Street currentStreet: exits){
			if ((currentStreet.getLatLng().latitude==latLng.latitude)&&
				(currentStreet.getLatLng().longitude==latLng.longitude))
				return currentStreet;
		}
		return null;
	}
}
