/**
 * 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class Tollway {
	private ArrayList<Street> exits;
	private ArrayList<TollPoint> tolls;
	private String name;
		
	public Tollway(){
		exits = new ArrayList<Street>();
		tolls = new ArrayList<TollPoint>();
	}
	
	public Tollway(String name){
		this();
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void addStreet(Street newStreet){
		if (newStreet!=null)
			exits.add(newStreet);
	}
	
	public void addToll(TollPoint tollPoint){
		tolls.add(tollPoint);
	}
	
	public ArrayList<TollPoint> getTollPoints(){
		return tolls;
	}
	
	public ArrayList<Street> getStreets(){
		return exits;
	}
	
	public Street getStreetByName(String streetName){
		for (int sc=0; sc < exits.size(); sc++){
			if (exits.get(sc).getName().equalsIgnoreCase(streetName))
				return exits.get(sc);
		}
		return null;
	}
}
