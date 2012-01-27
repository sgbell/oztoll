/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */

public class Street extends Coordinates{
	private String name;
	private int location;
	
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
}
