/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class TollDataView implements Runnable{
	private ArrayList<Street> streets;
	private ArrayList<Pathway> pathways;
	private Object syncObject, 
	               dataSync,
	               moveSync;
	private OzTollData tollData;
	private String cityName;
	private boolean stillRunning, 
					rateCalculated=false,
					noRoadsMoverStarted=false,
					endMoveMoverStarted=false;
	private Coordinates screenOrigin, move, origin[];
	private int screenHeight=0, 
				screenWidth=0;
	private Context appContext;
	private LinearLayout rateLayout;
	private float screenXMultiplier,
				  screenYMultiplier;
	private Handler mainHandler;

	public TollDataView(){
		move = new Coordinates();
		screenOrigin = new Coordinates();
		syncObject = new Object();
		moveSync = new Object();
	}
	
	public TollDataView(OzTollData data){
		this();
		tollData=data;
		cityName = "";		
		
		dataSync = tollData.getDataSync();
	}
	
	public Object getMoveSync(){
		return moveSync;
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
	
	public TollDataView(OzTollData data, int height, int width, Context context){
		this(data);
		setHeight(height);
		setWidth(width);
		appContext=context;
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
				// Put in this condition so that if tollData has finished reading the file, the app wont be put to sleep
				// waiting for the data to be read
				if (!tollData.isFinished())
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
					if ((pathways.size()==0)&&(streets.size()==0)){
						// If nothing on the screen we need to move the map to get it there
						if (!noRoadsMoverStarted){
							(new Thread(){
								public void run(){
									noRoadsMoverStarted=true;
									final int MOVEUP=0, // This is used to tell the for loop after the while loop which
											  MOVEDOWN=1, // way to continue moving the map
											  MOVELEFT=2,
											  NOMOVEMENT=3;
									
									int movement=NOMOVEMENT;
											  
									while (streets.size()<1){
										// Hard coding this for the melbourne tollways. I will need to rewrite this
										// for other freeways
										if ((minX()>-3)&&(maxX()<12)){
											if (maxY()<7){
												screenOrigin.updateY(-1);
												movement=MOVEUP;
											} else if (minY()>5){
												screenOrigin.updateY(1);
												movement=MOVEDOWN;
											}
											checkMove();
										} else if (minX()<=-3){
											screenOrigin.updateX(-1);
											movement=MOVELEFT;
										}
										synchronized(syncObject){
											syncObject.notify();
										}
										synchronized(moveSync){
											try {
												moveSync.wait();
											} catch (InterruptedException e) {
												//just moving the stuff around
											}
										}
									}
									// Adding a little Extra movement of the map so that it doesn't show just the tip of an exit
									int moveMax=0;
									if (movement==MOVELEFT){
										moveMax=20;
									} else {
										moveMax=screenHeight/4;
									}
									for (int movingCount=0; movingCount<moveMax; movingCount++){
										switch (movement){
											case MOVEUP:
												screenOrigin.updateY(-1);
												break;
											case MOVEDOWN:
												screenOrigin.updateY(1);
												break;
											case MOVELEFT:
												screenOrigin.updateX(-1);
												break;
										}
										synchronized(syncObject){
											syncObject.notify();
										}
										synchronized(moveSync){
											try {
												moveSync.wait();
											} catch (InterruptedException e) {
												//just moving the stuff around
											}
										}
									}
									noRoadsMoverStarted=false;
								}
							}).start();
						}
					}
					try {
						syncObject.wait();
					} catch (InterruptedException e) {
						// just wait for screen to be moved
					}
				}
			}
		}
	}
	
	/**
	 * This is used to mark the path on the map between the selected points.
	 */
	public void processPath(Street start, Street end){
		if ((start!=null)&&(end!=null)){
			ArrayList<Pathway> paths=tollData.getPathway(start, end);
			
			for (int pc=0; pc < paths.size(); pc++){
				paths.get(pc).setRoute(true);
			}
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

	public void resetScreenOrigin(){
		screenOrigin = new Coordinates();
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

	public void stopMove(){
		endMoveMoverStarted=false;
	}
	
	public void resetMove(ArrayList<Coordinates> eventHistory) {
		final ArrayList<Coordinates> moveHistory = eventHistory;
		/* 
		 * The following inline Thread is used to make the map scroll for a little bit after the user has moved the
		 * map.
		 */
		(new Thread(){
			public void run(){
				if (!endMoveMoverStarted){
				endMoveMoverStarted=true;

					if (moveHistory.size()>1)
						for (int endMoveCount=0; endMoveCount<20; endMoveCount++){
							if (endMoveMoverStarted){
								move.updateX(moveHistory.get(1).getX()-moveHistory.get(0).getX());
								move.updateY(moveHistory.get(1).getY()-moveHistory.get(0).getY());
								checkMove();
								synchronized (syncObject){
									syncObject.notify();
								}
								// Created moveSync so the DrawingThread will wake this thread up. so the movement of the map
								// happens.
								synchronized (moveSync){
									try {
										moveSync.wait();
									} catch (InterruptedException e) {
										//just pausing for half a second							
									}
								}
							}
						}

					screenOrigin.updateX(move.getX());
					screenOrigin.updateY(move.getY());
					move.setX(0);
					move.setY(0);
					endMoveMoverStarted=false;
				} else {
					endMoveMoverStarted=false;
				}
			}
		}).start();
	
	}

	// Determine minimum value for X in street coords for display on screen
	// return integer.
	public float minX(){
		return (((10*screenXMultiplier)-move.getX()-screenOrigin.getX()-(getWidth()/2))/(70*screenXMultiplier))-(1*screenXMultiplier);
		
		/**  0 = ((mapPointX*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX();
		 *   0-move.getX()-screenOrigin.getX() = ((mapPointX*70)+(getWidth()/2)-10)
		 *   10 - move.getX()-screenOrigin.getX() = (mapPointX*70)+(getWidth()/2)
		 *   mapPointX = (10 - move.getX()-screenOrigin.getX()-(getWidth()/2))/70 
		 */
	}
	
	// Determine Maximum value for X in street coords for display on screen
	// return integer.
	public float maxX(){
		return ((20*screenXMultiplier)-move.getX()-screenOrigin.getX()+(getWidth()/2))/(70*screenXMultiplier);
	}
	
	public float minY(){
		return (-((15*screenYMultiplier)+move.getY()+screenOrigin.getY()))/(50*screenYMultiplier);
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
		return ((getHeight())-((5*screenYMultiplier)+move.getY()+screenOrigin.getY()))/(50*screenYMultiplier);
	}
	
	public float drawX(float mapPointX){
		return ((mapPointX*(70*screenXMultiplier))+(getWidth()/2)-(10*screenXMultiplier))+move.getX()+screenOrigin.getX();
	}
	
	public float drawY(float mapPointY){
		return (mapPointY*50*screenYMultiplier)+(15*screenYMultiplier)+move.getY()+screenOrigin.getY();
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
		if (origin!=null){
			if ((origin[0]!=null)&&(getWidth()!=0)&&
				(origin[1]!=null)&&(getHeight()!=0)){
				/* This makes sure the user does not move the screen too far to the west of the
				 * map, losing the map moving the screen too far to the west. */
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
	}

	public void findStreet(Coordinates touchStart, Paint textFont) {
		if (tollData.isFinished()){
			for (int twc=0; twc < tollData.getTollwayCount(); twc++)
				for (int sc=0; sc < tollData.getStreetCount(twc); sc++){
					Street currentStreet = tollData.getStreet(twc, sc);
					Coordinates streetCoords = new Coordinates(
							drawX(currentStreet.getX()),
							drawY(currentStreet.getY()));
					
					Rect bounds = new Rect();
					textFont.getTextBounds(currentStreet.getName(), 0, currentStreet.getName().length(), bounds);
					float minX=0, maxX=0, minY=0, maxY=0;
					/* The following switch statement will set the min/max x/y values needed for the current street
					 * to check if the street is selected by the user.
					 */
					switch (currentStreet.getLocation()){
						case 1:
							minX = streetCoords.getX()-(20*screenXMultiplier);
							// maxX adds the length of the text away
							maxX = streetCoords.getX()+(20*screenXMultiplier)+(bounds.right-bounds.left);
							minY = streetCoords.getY()-(20*screenYMultiplier);
							maxY = streetCoords.getY()+(20*screenYMultiplier);
							break;
						case 2:
							// minX and maxX are around the text width which is printed in the middle of the street point
							minX = streetCoords.getX()-((bounds.right-bounds.left)/2);
							maxX = streetCoords.getX()+((bounds.right-bounds.left)/2);
							minY = streetCoords.getY()-(20*screenYMultiplier);
							// maxY is the height of the text above the street point
							maxY = streetCoords.getY()+((bounds.bottom-bounds.top)+(25*screenYMultiplier));
							break;
						case 3:
							minX = streetCoords.getX()-((bounds.right-bounds.left)/2);
							maxX = streetCoords.getX()+((bounds.right-bounds.left)/2);
							minY = streetCoords.getY()-((20*screenYMultiplier)+(bounds.bottom-bounds.top));
							maxY = streetCoords.getY()+(20*screenYMultiplier);
							break;
						case 0:
						default:
							minX = streetCoords.getX()-((bounds.right-bounds.left)+(20*screenXMultiplier));
							maxX = streetCoords.getX()+(20*screenXMultiplier);
							minY = streetCoords.getY()-(20*screenYMultiplier);
							maxY = streetCoords.getY()+(20*screenYMultiplier);
							break;
					}

					// The if statement to check if the street is selected
					if ((touchStart.getX()>minX)&&
						(touchStart.getX()<maxX)&&
						(touchStart.getY()>minY)&&
						(touchStart.getY()<maxY)){
						if ((getStart()==null)&&(currentStreet.isValid())){
							// If no selection has been made yet
							setStart(currentStreet);
							tollData.setStreetsToInvalid();
							markRoads(tollData.getStart());
							for (int cc=0; cc < tollData.getConnectionCount(); cc++){
								if (tollData.getConnection(cc).getStart()==tollData.getStart())
									markRoads(tollData.getConnection(cc).getEnd());
								else if (tollData.getConnection(cc).getEnd()==tollData.getStart())
									markRoads(tollData.getConnection(cc).getStart());
							}
							try {
								syncObject.notify();
							} catch (IllegalMonitorStateException e){
								// just so it wont crash
							}
						} else if (currentStreet==getStart()){
							if (getEnd()==null){
								// If the user deselects the start road
								setStart(null);
								// calling resetPaths resets the paths and the valid streets
								resetPaths();
								// Send handler to reset shownStart to false
								Message msg = mainHandler.obtainMessage();
								msg.what=7;
								mainHandler.sendMessage(msg);
								try {
									syncObject.notify();
								} catch (IllegalMonitorStateException e){
									// just so it wont crash
								}
							}
						} else if ((currentStreet.isValid())&&(getEnd()==null)){
							// If the user selects the end road
							setEnd(currentStreet);
							processPath(tollData.getStart(),tollData.getFinish());
							rateLayout = tollData.processToll(tollData.getStart(), tollData.getFinish(), appContext);
							rateCalculated=true;
							try {
								syncObject.notify();
							} catch (IllegalMonitorStateException e){
								// just so it wont crash
							}
						} else if (currentStreet==getEnd()){
							// If the user de-selects the end road
							setEnd(null);
							resetPaths();
							tollData.setStreetsToInvalid();
							markRoads(tollData.getStart());
							for (int cc=0; cc < tollData.getConnectionCount(); cc++){
								if (tollData.getConnection(cc).getStart()==tollData.getStart()){
									markRoads(tollData.getConnection(cc).getEnd());
								} else if (tollData.getConnection(cc).getEnd()==tollData.getStart()){
									markRoads(tollData.getConnection(cc).getStart());
								}
							}
							setRateCalculated(false);
							Message msg = mainHandler.obtainMessage();
							msg.what=8;
							mainHandler.sendMessage(msg);
							try {
								syncObject.notify();
							} catch (IllegalMonitorStateException e){
								// just so it wont crash
							}
						}
					}
				}
		}
	}

	public Street getEnd() {
		return tollData.getFinish();
	}
	
	public void setEnd(Street end) {
		tollData.setFinish(end);
	}

	/**
	 * @return the start
	 */
	public Street getStart() {
		return tollData.getStart();
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Street start) {
		tollData.setStart(start);
	}

	/**
	 * @return the rateDialogText
	 */
	public LinearLayout getRateDialog() {
		return rateLayout;
	}

	/**
	 * @param rateDialogText the rateDialogText to set
	 */
	public void setRateDialog(LinearLayout rateDialog) {
		rateLayout = rateDialog;
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

	public void resetPaths() {
		for (int twc=0; twc < tollData.getTollwayCount(); twc++){
			for (int pwc=0; pwc < tollData.getPathwayCount(twc); pwc++){
				tollData.getPathway(twc, pwc).setRoute(false);
			}
			for (int tec=0; tec < tollData.getStreetCount(twc); tec++){
				tollData.getStreet(twc, tec).setValid(false);
			}
		}
		for (int cc=0; cc < tollData.getConnectionCount(); cc++)
			tollData.getConnection(cc).setRoute(false);

		tollData.setValidStarts();
	}

	public void setXMultiplier(float screenMultiplier) {
		screenXMultiplier=screenMultiplier;
	}
	
	public void setYMultiplier(float screenMultiplier) {
		screenYMultiplier=screenMultiplier;
	}

	public Handler getMainHandler() {
		return mainHandler;
	}

	public void setMainHandler(Handler mainHandler) {
		this.mainHandler = mainHandler;
	}
}
