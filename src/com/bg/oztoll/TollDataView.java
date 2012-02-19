/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class TollDataView implements Runnable{
	private ArrayList<Street> streets;
	private ArrayList<Pathway> pathways;
	private Object syncObject;
	private OzTollData tollData;
	private String cityName;
	private boolean stillRunning;
	private Coordinates screenOrigin, move, origin[];
	private int screenHeight, screenWidth;
	private int xMin, xMax;

	public TollDataView(){
		move = new Coordinates();
		screenOrigin = new Coordinates();
		syncObject = new Object();
	}
	
	public TollDataView(OzTollData data){
		this();
		tollData=data;
		cityName = "";
		
		while (!tollData.finishedReading()){
			// just waiting for tollData to finish
		}
			origin=tollData.getMapLimits();
	}
	
	public Object getSync(){
		return syncObject;
	}
	
	public TollDataView(OzTollData data, int height, int width){
		this(data);
		setHeight(height);
		setWidth(width);
	}
	
	public ArrayList<Street> getStreets(){
		return streets;
	}
	
	public ArrayList<Pathway> getPaths(){
		return pathways;
	}
	
	@Override
	public void run() {
		stillRunning=true;
		while (stillRunning){
			
			if (!cityName.equalsIgnoreCase(tollData.getCityName()))
				cityName=tollData.getCityName();
			
			
			if (getWidth()>0)
				synchronized(syncObject){
					streets = new ArrayList<Street>();
					pathways = new ArrayList<Pathway>();
					for (int twc=0; twc<tollData.getTollwayCount(); twc++){
						for (int tsc=0; tsc<tollData.getStreetCount(twc); tsc++){
							if ((tollData.getStreetX(twc, tsc)>minX()) &&
							    (tollData.getStreetX(twc, tsc)<maxX()) &&
							    (tollData.getStreetY(twc, tsc)>minY()) &&
							    (tollData.getStreetY(twc, tsc)<maxY())){
								streets.add(tollData.getStreet(twc, tsc));
							}
						}
						for (int pwc=0; pwc<tollData.getPathwayCount(twc);pwc++){
							Pathway currentPathway = tollData.getPathway(twc, pwc);
							boolean found=false;
							int sc=0;
							while ((!found)&&(sc<streets.size())){
								if ((streets.get(sc)==currentPathway.getStart())||
									(streets.get(sc)==currentPathway.getEnd()))
									found=true;
								else
									sc++;
							}
							if (found){
								pathways.add(currentPathway);
							}							
						}
					}
					for (int cc=0; cc< tollData.getConnectionCount(); cc++){
						Connection currentConnection = tollData.getConnection(cc);
						boolean found = false;
						int sc=0;
						while ((!found)&&(sc<streets.size())){
							if ((streets.get(sc)==currentConnection.getStart())||
								(streets.get(sc)==currentConnection.getEnd()))
								found=true;
							else
								sc++;
						}
						if (found){
							pathways.add(currentConnection);
						}							
					}
				}
		}
	}

	public Coordinates getScreenOrigin() {
		return screenOrigin;
	}

	public Coordinates getMove() {
		return move;
	}

	public void resetMove() {
		screenOrigin.updateX(move.getX());
		screenOrigin.updateY(move.getY());
		move.setX(0);
		move.setY(0);
	}

	// Determine minimum value for X in street coords for display on screen
	// return integer.
	public float minX(){
		return ((10-move.getX()-screenOrigin.getX()-(getWidth()/2))/70)-1;
		
		/**  0 = ((mapPointX*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX();
		 *   0-move.getX()-screenOrigin.getX() = ((mapPointX*70)+(getWidth()/2)-10)
		 *   10 - move.getX()-screenOrigin.getX() = (mapPointX*70)+(getWidth()/2)
		 *   mapPointX = (10 - move.getX()-screenOrigin.getX()-(getWidth()/2))/70 
		 */
	}
	
	// Determine Maximum value for X in street coords for display on screen
	// return integer.
	public float maxX(){
		return (20-move.getX()-screenOrigin.getX()+(getWidth()/2))/70;
	}
	
	public float minY(){
		return (-(15+move.getY()+screenOrigin.getY()))/50;
		/**
		 *   0 = (mapPointY*50)+15+move.getY()+screenOrigin.getY();
		 *   0-(15+move.getY()+screenOrigin.getY()) = mapPointY*50
		 *   mapPointY = (-(15+move.getY()+screenOrigin.getY()))/50
		 */
	}
	
	public float maxY(){
		/** getWidth() = (mapPointY*50)+15+move.getY()+screenOrigin.getY();
		 *  getWidth()-(15+move.getY()+screenOrigin.getY()) = mapPointY*50;
		 *  mapPointY = (getWidth()-(15+move.getY()+screenOrigin.getY()))/50;
		 * 
		 */
		return (getHeight()-(15+move.getY()+screenOrigin.getY()))/50;
	}
	
	public float drawX(float mapPointX){
		return ((mapPointX*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX();
	}
	
	public float drawY(float mapPointY){
		return (mapPointY*50)+15+move.getY()+screenOrigin.getY();
	}

	/**
	 * @return the screenHeight
	 */
	public int getHeight() {
		return screenHeight;
	}

	/**
	 * @param screenHeight the screenHeight to set
	 */
	public void setHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	/**
	 * @return the screenWidth
	 */
	public int getWidth() {
		return screenWidth;
	}

	/**
	 * @param screenWidth the screenWidth to set
	 */
	public void setWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public void checkMove(){
		/* This makes sure the user does not move the screen too far to the west of the
		 * map, loosing the map moving the screen too far to the west. */
		if (drawX(origin[0].getX())>getWidth()-1)
			move.setX(getWidth()-(drawX(origin[0].getX())-move.getX()));
		// Moving the map too far to the east
		if (drawX(origin[1].getX())<1)
			move.setX(0-(drawX(origin[1].getX())-move.getX()));
		// Moving the map too far north
		if (drawY(origin[0].getY())>getHeight()-1)
			move.setY(getHeight()-(drawY(origin[0].getY())-move.getY()));
		/* Moving the map too far south. The reason for using 10 instead of 0 like the 
		 * east check, is so we still have the most southern point on the screen. */
		if (drawY(origin[1].getY())<10)
			move.setY(10-(drawY(origin[1].getY())-move.getY()));
	}
}
