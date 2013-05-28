/** Street defines a street, usually populated from an xml file
 * 
 */
package com.mimpidev.oztoll;

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
	private char location;
	// Valid is used to determine if a user can click on the road.
	private boolean valid=false;
	
	
	public Street(String name, GeoPoint coords){
		super(coords.getLatLng().latitude,coords.getLatLng().longitude);
		this.name = name;
	}
	
	public Street(String name, GeoPoint coords, int i){
		this(name,coords.getLatLng().longitude,coords.getLatLng().longitude, i);
	}
	
	public Street(String name, double longitude, double latitude, int i){
		super(latitude,longitude);
		this.name = name;
		location = i > 0 && i < 27 ? (char)(i + 64) : null;
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
	public char getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(int location) {
		this.location = location > 0 && location < 27 ? (char)(location + 64) : null;
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
}
