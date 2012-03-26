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
	private Object syncObject, dataSync;
	private OzTollData tollData;
	private String cityName;
	private boolean stillRunning, pathMarked=false;
	private Coordinates screenOrigin, move, origin[];
	private int screenHeight, screenWidth;
	private int xMin, xMax;
	private Street startStreet, endStreet;
	private String rateDialogText;
	private boolean rateCalculated=false;

	public TollDataView(){
		move = new Coordinates();
		screenOrigin = new Coordinates();
		syncObject = new Object();
	}
	
	public TollDataView(OzTollData data){
		this();
		tollData=data;
		cityName = "";		
		
		dataSync = tollData.getDataSync();
	}
	
	public Object getSync(){
		return syncObject;
	}
	
	public void setDataSync(Object syncMe){
		dataSync = syncMe;
	}
	
	public Object getDataSync(){
		return dataSync;
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
		synchronized (dataSync){
			try {
				dataSync.wait();
			} catch (InterruptedException e) {
				// just wait for it
			}
		}
		boolean lastFileRead = false;

		stillRunning=true;
		while (stillRunning){
			// Need to make the program read getMapLimits 
			if (!tollData.isFinished()){
				origin=tollData.getMapLimits();
			} else if (!lastFileRead){
				lastFileRead = true;
				origin=tollData.getMapLimits();
			}
			
			if (!cityName.equalsIgnoreCase(tollData.getCityName()))
				cityName=tollData.getCityName();
			if (getWidth()>0){
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
					try {
						syncObject.wait();
					} catch (InterruptedException e) {
						// just wait for screen to be moved
					}
				}
				markRoads(startStreet);
				if (!pathMarked)
					processPath();
				if (pathMarked){
					ArrayList<TollCharges> tolls = tollData.getFullRate(startStreet, endStreet);
					ArrayList<TollRate> totalCharges = new ArrayList<TollRate>();						
					String[] tollNames = {"Car", "Car - Weekend Travel", "Light Commercial Vehicle",
							  "Heavy Commercial Vehicle", "Heavy Commercial Vehicle - Daytime",
							  "Heavy Commercial Vehicle - Nighttime", 
							  "Heavy/Light Commercial Vehicle - Daytime",
							  "Heavy/Light Commercial Vehicle - Nighttime",
							  "Motorcycle"};
					for (int trc=0; trc < 9; trc++){
						TollRate newTollrate = new TollRate();
						newTollrate.vehicleType=tollNames[trc];
						newTollrate.rate="0.0";
						totalCharges.add(newTollrate);
					}

					rateDialogText = "";
					for (int tc=0; tc < tolls.size(); tc++){
						TollCharges currentToll = tolls.get(tc);
						
						rateDialogText += "<h2>Tollway - "+currentToll.tollway+"</h2><br>";
						for (int trc=0; trc < currentToll.tolls.size(); trc++){
							if (currentToll.tolls.get(trc).rate!=null){
								String vehicleType="";
								if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car")){
									vehicleType="Car";
									totalCharges.get(0).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(0).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car-we")){
									vehicleType="Car - Weekend Travel";
									totalCharges.get(1).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(1).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv")){
									vehicleType="Light Commercial Vehicle";
									totalCharges.get(2).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(2).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv")){
									vehicleType="Heavy Commercial Vehicle";
									totalCharges.get(3).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(3).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv-day")){
									vehicleType="Heavy Commercial Vehicle - Daytime";
									totalCharges.get(4).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(4).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv-night")){
									vehicleType="Heavy Commercial Vehicle - Nighttime";
									totalCharges.get(5).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(5).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("cv-day")){
									vehicleType="Heavy/Light Commercial Vehicle - Daytime";
									totalCharges.get(6).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(6).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("cv-night")){
									vehicleType="Heavy/Light Commercial Vehicle - Nighttime";
									totalCharges.get(7).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(7).rate));
								}else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("mc")){
									vehicleType="Motor Cycle";
									totalCharges.get(8).rate=Float.toString(Float.parseFloat(currentToll.tolls.get(trc).rate)+
											Float.parseFloat(totalCharges.get(8).rate));
								}
								rateDialogText += "<b>"+vehicleType+"</b><br>"+currentToll.tolls.get(trc).rate+"<br>";
							}
						}
					}
					if (tolls.size()>1){
						//Need to go through the array and combine entries like lcv day & night
						// and hcv day & night
						rateDialogText += "<h2>Total Toll Payments</h2>";
						for (int trc=0; trc < 9; trc++){
							rateDialogText += "<b>"+totalCharges.get(trc).vehicleType+"</b><br>"+
											  totalCharges.get(trc).rate+"<br>";
						}
					}
					rateCalculated=true;					
				}
			}
		}
	}

	/**
	 * This is used to mark the path on the map between the selected points.
	 */
	public void processPath(){
		if ((startStreet!=null)&&(endStreet!=null)){
			ArrayList<Pathway> paths=tollData.getPathway(startStreet, endStreet);
			
			for (int pc=0; pc < paths.size(); pc++){
				paths.get(pc).setRoute(true);
			}
			pathMarked=true;			
		}
	}

	/**This goes through the arrays and marks the roads.
	 * This Function may have been replaced by processPath().
	 * 
	 * @param tollway
	 * @param start
	 * @param end
	 */
	public void markPaths(int tollway, Street start, Street end){
		boolean markStart=false, markEnd=false;

		for (int pwc=0; pwc < tollData.getPathwayCount(tollway); pwc++){
			if ((tollData.getPathway(tollway, pwc).getStart()==start)||
				(tollData.getPathway(tollway, pwc).getStart()==end)){
				if ((!markStart)&&(!markEnd))
					markStart=true;
			}
			if ((markStart)&&(!markEnd)){
				// Need to figure out how to make sure only the roads between start and end
				// are marked.
				tollData.getPathway(tollway,pwc).setRoute(true);
				if ((tollData.getPathway(tollway, pwc).getEnd()==start)||
					(tollData.getPathway(tollway, pwc).getEnd()==end)){
					markEnd=true;
				}
			}
		}
	}
	
	
	/** 
	 * This function will go through the entire map and mark the valid exits, once a
	 * starting point has been selected.
	 * @param validStreet
	 */
	public void markRoads(Street validStreet){
		if (validStreet!=null){
			ArrayList<Street> exitList= tollData.getTollPointExits(validStreet);
			ArrayList<Street> tollwayConnections= new ArrayList<Street>();
			if (exitList.size()>0)
				for (int elc=0; elc < exitList.size(); elc++){
					exitList.get(elc).setValid(true);
					for (int cc=0; cc < tollData.getConnectionCount(); cc++){
						if (exitList.get(elc)==tollData.getConnection(cc).getStart())
							tollwayConnections.add(tollData.getConnection(cc).getEnd());
						if (exitList.get(elc)==tollData.getConnection(cc).getEnd())
							tollwayConnections.add(tollData.getConnection(cc).getStart());
					}
				}
			if (tollwayConnections.size()>0)
				for (int tc=0; tc < tollwayConnections.size(); tc++){
					markRoads(tollwayConnections.get(tc));
				}
		}
	}
	
	public Coordinates getScreenOrigin() {
		return screenOrigin;
	}

	public Coordinates getMove() {
		synchronized (syncObject){
			syncObject.notify();
		}
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
		return (getHeight()-(5+move.getY()+screenOrigin.getY()))/50;
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

	public void findStreet(Coordinates touchStart) {
		for (int twc=0; twc < tollData.getTollwayCount(); twc++)
			for (int sc=0; sc < tollData.getStreetCount(twc); sc++){
				Street currentStreet = tollData.getStreet(twc, sc);
				Coordinates streetCoords = new Coordinates(
						drawX(currentStreet.getX()),
						drawY(currentStreet.getY()));
				
				if ((streetCoords.getX()>touchStart.getX()-15)&&
					(streetCoords.getX()<touchStart.getX()+15)&&
					(streetCoords.getY()>touchStart.getY()-15)&&
					(streetCoords.getY()<touchStart.getY()+15))
					if (getStart()==null)
						setStart(currentStreet);
					else if ((currentStreet.isValid())&&(getEnd()==null))
						setEnd(currentStreet);
			}
	}

	public Street getEnd() {
		return endStreet;
	}
	
	public void setEnd(Street end) {
		endStreet = end;
	}

	/**
	 * @return the start
	 */
	public Street getStart() {
		return startStreet;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Street start) {
		this.startStreet = start;
	}

	/**
	 * @return the rateDialogText
	 */
	public String getRateDialogText() {
		return rateDialogText;
	}

	/**
	 * @param rateDialogText the rateDialogText to set
	 */
	public void setRateDialogText(String rateDialogText) {
		this.rateDialogText = rateDialogText;
	}

	/**
	 * @return the rateCalculated
	 */
	public boolean isRateCalculated() {
		return rateCalculated;
	}

	/**
	 * @param rateCalculated the rateCalculated to set
	 */
	public void setRateCalculated(boolean rateCalculated) {
		this.rateCalculated = rateCalculated;
	}	
}
