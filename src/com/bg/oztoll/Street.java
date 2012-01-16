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
	
	public Street(String name, Coordinates coords){
		super(coords.getX(),coords.getY());
		this.name = name;
	}
	
	public Street(String name, long x, long y){
		super(x,y);
		this.name = name;
		
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
}
