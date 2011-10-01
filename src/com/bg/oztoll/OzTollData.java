/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class OzTollData {
	private ArrayList<NodePoint> tolls;
	
	public OzTollData(){
		
	}
	
	public NodePoint getToll(int point){
		return (NodePoint)tolls.get(point);
	}
	
	public int tollSize(){
		return tolls.size();
	}
	
	public void addTollPoint(NodePoint newNode){
		tolls.add(newNode);
	}
	
	public void loadTolls(){
		
	}
}
