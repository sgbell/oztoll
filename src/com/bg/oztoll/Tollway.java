/**
 * 
 */
package com.bg.oztoll;

import java.util.Vector;

/**
 * @author bugman
 *
 */
public class Tollway {
	private Vector<Street> exits;
	private Vector<Pathway> paths;
	private Vector<TollPoint> tolls;
	private Vector<Pathway> connections;
	private String name;
		
	public Tollway(){
		exits = new Vector<Street>();
		paths = new Vector<Pathway>();
		tolls = new Vector<TollPoint>();
		connections = new Vector<Pathway>();
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
	
	public void addPath(Street start, Street end){
		Pathway newPath = new Pathway(start,end);
		paths.add(newPath);
	}
	
	public void addToll(TollPoint tollPoint){
		tolls.add(tollPoint);
	}
	
	public void addConnection(Street start, Street end){
		Pathway newConnection = new Pathway(start,end);
		connections.add(newConnection);
	}
	
	public Vector<Pathway> getPaths(){
		return paths;
	}
	
	public Vector<TollPoint> getTollPoints(){
		return tolls;
	}
	
	public Vector<Street> getStreets(){
		return exits;
	}
	
	public Street getStreetByName(String streetName){
		for (int sc=0; sc < exits.size(); sc++){
			if (exits.get(sc).getName().equalsIgnoreCase(streetName))
				return exits.get(sc);
		}
		return null;
	}
	
	public Vector<Pathway> getConnections(){
		return connections;
	}
}
