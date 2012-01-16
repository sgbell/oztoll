/**
 * 
 */
package com.bg.oztoll;

/**
 * @author bugman
 *
 */
public class Pathway {
	private Street start=null;
	private Street end=null;
	
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
}
