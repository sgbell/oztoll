/** Pathway is a class that defines connections between streets
 * 
 */
package com.mimpidev.oztoll;

/**
 * @author bugman
 *
 */
public class Pathway {
	private Street start=null;
	private Street end=null;
	private boolean route=false;
	
	public Pathway(Street newStart, Street newEnd){
		start = newStart;
		end = newEnd;
	}
	
	public Street getStart(){
		return start;
	}
	
	public Street getEnd(){
		return end;
	}
	
	public void setStart(Street newStart){
		start=newStart;
	}
	
	public void setEnd(Street newEnd){
		end = newEnd;
	}

	/**
	 * @return the route
	 */
	public boolean isRoute() {
		return route;
	}

	/**
	 * @param route the route to set
	 */
	public void setRoute(boolean route) {
		this.route = route;
	}
}
