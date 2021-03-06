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
 *
 * Street defines a street, usually populated from an xml file
 * 
 */
package com.mimpidev.oztoll;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author bugman
 *
 */

public class Street extends GeoPoint{
	// Street name
	private String name;
	/* Location is a letter we have assigned to the exit in the city's tolls, 
	 * according to listing in the xml file.
	 */
	private String location;
	// Valid is used to determine if a user can click on the road.
	private boolean valid=false;
	
	
	public Street(String name, GeoPoint coords){
		super();
		this.setLatLng(coords.getLatLng());
		this.name = name;
	}
	
	public Street(String name, GeoPoint coords, int i){
		super();
		this.setLatLng(coords.getLatLng());
		this.name = name;
		// location = i > 0 && i < 27 ? (char)(i + 64) : null;
		// Problem is the above line, need to cater for more than 26 exits
		location = getEncodingFromNumber(i); 
	}
	
	public String getEncodingFromNumber(int number){
		String charOutput="";
		if (number<27)
			charOutput = getCharForNumber(number);
		else {
			if (number%26!=0){
				charOutput+=getCharForNumber(number/26);
				charOutput+=getCharForNumber(number%26);
			} else {
				charOutput=getCharForNumber((number/26)-1)+"Z";
			}
		}
		
		return charOutput;
	}
	
	public String getCharForNumber(int i){
		return i > 0 && i < 27 ? String.valueOf((char)(i + 64)) : null;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(int location) {
		this.location = getEncodingFromNumber(location);
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @param valid the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 *  Because we have a tollway with more than 26 exits we have to change the way we get the number
	 * @return
	 */
	public int getLocationNumber() {
		int number=1;
		if (location.length()>1){
			for (int charCount=0; charCount < location.length()-1; charCount++)
				number=number*26*(int)(location.toUpperCase().charAt(charCount)-64);
			number+=(int)(location.toUpperCase().charAt(location.length()-1)-64);
		} else if (location.length()==1)
			number=(int)(location.toUpperCase().charAt(0)-64);
		number--;
			
		return number;
	}
	
	public boolean equals(LatLng location){
		if (compareLatLng(location))
			return true;
		else
			return false;
	}
}
