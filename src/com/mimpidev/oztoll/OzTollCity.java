/**
 * 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class OzTollCity {

	private ArrayList<Tollway> tollways;
	private String cityName;
	private GeoPoint origin=null;
	
	public OzTollCity(){
		tollways = new ArrayList<Tollway>();
	}
	
	/**
	 * @return the cityName
	 */
	public String getCityName() {
		return cityName;
	}
	
	/**
	 * @param cityName the cityName to set
	 */
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	
	public void addTollways(Tollway newTollway){
		tollways.add(newTollway);
	}
	
	public Tollway getTollway(int tollwayCount){
		return tollways.get(tollwayCount);
	}
	
	/**
	 * @return the tollways
	 */
	public ArrayList<Tollway> getTollways() {
		return tollways;
	}
	
	/**
	 * @param tollways the tollways to set
	 */
	public void setTollways(ArrayList<Tollway> tollways) {
		this.tollways = tollways;
	}

	public GeoPoint getOrigin(){
		return origin;
	}

	public void setOrigin(GeoPoint coords){
		origin = coords;
	}
	
	public int getTollwayCount(){
		return tollways.size();
	}

	public int getStreetCount(int twc) {
		return tollways.get(twc).getStreets().size();
	}
	
	public void setStreetsToInvalid(){
		for (int twc=0; twc< getTollwayCount(); twc++)
			for (int sc=0; sc < getStreetCount(twc); sc++)
				getStreet(twc, sc).setValid(false);
	}
	
	public Street getStreet(int twc, int twi) {
		if (twc<tollways.size())
			if (twi<tollways.get(twc).getStreets().size())
				return tollways.get(twc).getStreets().get(twi);
			else
				return null;
		else
			return null;
	}

	public Street getStreet(String tollway, String streetName){
		boolean tollwayFound=false, streetFound=false;
		int twc=0, sc=0;
		while ((twc<tollways.size())&&
				(!tollwayFound)){
			if (tollways.get(twc).getName().equalsIgnoreCase(tollway))
				tollwayFound=true;
			else
				twc++;
		}
		if (tollwayFound){
			while ((sc<tollways.get(twc).getStreets().size())&&
					(!streetFound)){
				if (tollways.get(twc).getStreets().get(sc).getName().equalsIgnoreCase(streetName))
					streetFound=true;
				else
					sc++;
			}
		}
		if ((tollwayFound)&&(streetFound)){
			return tollways.get(twc).getStreets().get(sc);
		} else
				return null;	
	}
	
	public int getTollCount(int twc) {
		return tollways.get(twc).getTollPoints().size();
	}

	public float getStreetX(int twc, int twi) {
		return tollways.get(twc).getStreets().get(twi).getX();
	}

	public float getStreetY(int twc, int twi) {
		return tollways.get(twc).getStreets().get(twi).getY();
	}

	public String getStreetName(int twc, int twi) {
		return tollways.get(twc).getStreets().get(twi).getName();
	}
	
	public int getLocation(int tollway, int exit){
		return tollways.get(tollway).getStreets().get(exit).getLocation();
	}

	public String getTollwayName(int tollway){
		return tollways.get(tollway).getName();
	}
	
	public String getTollwayName(Street currentStreet){
		boolean tollwayFound=false;
		int twc=0;
		while ((twc<tollways.size())&&
				(!tollwayFound)){
			if (tollways.get(twc).getStreets().contains(currentStreet))
				tollwayFound=true;
			else
				twc++;
		}
		if (tollwayFound)
			return tollways.get(twc).getName();
		else
			return null;
	}
	
	public ArrayList<Street> getTollPointExits(Street start){
		ArrayList<Street> exits = new ArrayList<Street>();
		for (int twc=0; twc < tollways.size(); twc++){
			for (int tpc=0; tpc < tollways.get(twc).getTollPoints().size(); tpc++){
				TollPoint tollPoints = tollways.get(twc).getTollPoints().get(tpc);
				if (tollPoints.isStart(start.getName())){
					for (int tpe=0; tpe<tollPoints.getExit().size(); tpe++){
						TollPointExit tpExits = tollPoints.getExit().get(tpe); 
						for (int ec=0; 
							 ec<tpExits.getExits().size();
							 ec++){
							exits.add(tpExits.getExits().get(ec));
						}
					}
				}
			}
		}
		return exits;
	}

	public boolean foundStreet(Street street){
		boolean foundIt=false;
		int tollwayCount=0,
			streetCount;
		
		while (!foundIt){
			streetCount=0;
			
			
			tollwayCount++;
		}
		
		return foundIt;
	}
	
	/** 
	 * This function will go through the entire map and mark the valid exits, once a
	 * starting point has been selected.
	 * @param validStreet
	 */
	public void markRoads(Street validStreet){
		if (validStreet!=null){
			ArrayList<Street> exitList= getTollPointExits(validStreet);
			if (exitList.size()>0)
				for (int elc=0; elc < exitList.size(); elc++)
					exitList.get(elc).setValid(true);
		}
	}
}
