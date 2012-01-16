/**
 * 
 */
package com.bg.oztoll;

import java.util.Vector;

/**
 * @author bugman
 *
 */
public class TollPoint {
	private Vector<Street> start;
	private Vector<TollPointExit> exit;
	
	public TollPoint(){
		start = new Vector<Street>();
		exit = new Vector<TollPointExit>();
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
	
	public Vector<TollPointExit> getExit(){
		return exit;
	}
	
	public void addExit(TollPointExit tollExit){
		exit.add(tollExit);
	}
}
