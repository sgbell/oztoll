/** TollPoint lists the starting streets, and then the tolls from that point
 * It has one array of the starting streets and one array for the exits, 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class TollPoint {
	private ArrayList<Street> start;
	private ArrayList<TollPointExit> exit;
	
	public TollPoint(){
		start = new ArrayList<Street>();
		exit = new ArrayList<TollPointExit>();
	}
	
	public void addStart(Street newStart){
		start.add(newStart);
	}
	
	public boolean isStart(String street){
		for (int svc=0; svc < start.size(); svc++)
			if (start.get(svc).getName().equalsIgnoreCase(street))
				return true;
		return false;
	}
	
	public void setStartValid(){
		for (int svc=0; svc < start.size(); svc++)
			start.get(svc).setValid(true);
	}
	
	public ArrayList<TollPointExit> getExit(){
		return exit;
	}
	
	public void addExit(TollPointExit tollExit){
		exit.add(tollExit);
	}
}
