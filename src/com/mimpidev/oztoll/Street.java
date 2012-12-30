/** Street defines a street, usually populated from an xml file
 * 
 */
package com.mimpidev.oztoll;

/**
 * @author bugman
 *
 */

public class Street extends Coordinates{
	// Street name
	private String name;
	/* Location is the exit number in the city's tolls, according to listing in the xml file
	 */
	private int location;
	// Valid is used to determine if a user can click on the road.
	private boolean valid=false;
	
	
	public Street(String name, Coordinates coords){
		super(coords.getX(),coords.getY());
		this.name = name;
	}
	
	public Street(String name, float x, float y, int i){
		super(x,y);
		this.name = name;
		location = i;
	}
	
	public Street() {
		super();
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
	public int getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(int location) {
		this.location = location;
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