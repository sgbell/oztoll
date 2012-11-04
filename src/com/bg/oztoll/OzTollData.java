/** This class stores all of the information extracted from the xml file, for use in our program.
 * Established early on that i couldn't read from the xml in real time as the program would not draw
 * the screen quick enough.
 */
package com.bg.oztoll;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author bugman
 *
 */
public class OzTollData implements Runnable{
		
	private ArrayList<Tollway> tollways;
	private ArrayList<Connection> connections;
	private String cityName;
	private OzTollXML ozTollXML;
	public String connectionsTest;
	private boolean finishedRead=false;
	private Object syncObject, dataSync;
	private SharedPreferences sharedPreferences;
	private Street start, finish;
		
	/** Initializes the vectors.
	 */
	public OzTollData(){
		tollways = new ArrayList<Tollway>();
		connections = new ArrayList<Connection>();
		ozTollXML = new OzTollXML();
	}
	
	/** This constructor initializes the vectors, and loads the xml file and calls
	 *  getTollwayData to populate the data
	 * @param filename
	 * @param assetMan 
	 */
	public OzTollData(String filename, AssetManager assetMan){
		this();
		ozTollXML.setXMLReader(filename,assetMan);
	}
	
	public OzTollData(String filename){
		this();
		ozTollXML.setXMLReader(filename);
	}
	
	public void setSyncObject(Object syncMe){
		syncObject = syncMe;
	}
	
	public Object getSyncObject(){
		return syncObject;
	}
	
	public void setDataSync(Object syncMe){
		dataSync = syncMe;
	}
	
	public Object getDataSync(){
		return dataSync;
	}
	
	/**
	 * getTollwayData - A method to populate arrays with the information stored in the xml file opened
	 * using ozTollXML. The reason all of the information will be stored in memory, is because accessing
	 * the xml file has proved to be time consuming when the program needs to access the xml file to
	 * redraw the screen.
	 */
	public void run(){
		tollways = new ArrayList<Tollway>();
		setCityName(ozTollXML.getCityName());
		int streetCounter=1;
		
		for (int twc=0; twc < ozTollXML.getTollwayCount(); twc++){
			Tollway newTollway = new Tollway(ozTollXML.getTollwayName(twc));
			// Populate the streets list in the tollway class
			for (int tsc=0; tsc < ozTollXML.getStreetCount(twc); tsc++){
				
				Street newStreet = new Street(ozTollXML.getStreetDetail(twc, tsc,"name"), 
											  Float.parseFloat(ozTollXML.getStreetDetail(twc, tsc,"longitude")),
											  Float.parseFloat(ozTollXML.getStreetDetail(twc, tsc,"latitude")),
											  streetCounter++);
				/* 
				 * I decided to use float globally for x,y details, as the screen location is stored as float
				 */
				if (newStreet!=null)
					newTollway.addStreet(newStreet);
			}
			
			// Array storing all tollways for current city
			tollways.add(newTollway);
		}
		
		// Populate connections. Connections are the direct joins between tollways.
		NodeList connectionList = ozTollXML.getConnections();
		if (connectionList!=null){
			for (int tcc=0; tcc < connectionList.getLength(); tcc++){
				// Grab the (tcc)th connection from the list in the xml file.
				Node currentNode = connectionList.item(tcc);
				if (currentNode!=null){
					Element fstElmnt = (Element) currentNode;
					// Grab the <start> tag and extract the tollway and the exit street from the tag
					NodeList elementList = fstElmnt.getElementsByTagName("start");
					String startTollway = ((Element)elementList.item(0)).getAttribute("tollway");
					String startExit=((Element)elementList.item(0)).getAttribute("exit");
					
					// Grab the <end> tag and extract the tollway and the exit street from the tag
					elementList = fstElmnt.getElementsByTagName("end");
					String endTollway = ((Element)elementList.item(0)).getAttribute("tollway");
					String endExit=((Element)elementList.item(0)).getAttribute("exit");
					
					Street start = new Street();
					Street end = new Street();

					/* The following code searches through the array to find the start and end
					 * tollways, and grabs the street object from the tollway.
					 */
					for (int twc=0; twc < tollways.size(); twc++){
						if (tollways.get(twc).getName().equalsIgnoreCase(startTollway))
							start=tollways.get(twc).getStreetByName(startExit);
						if (tollways.get(twc).getName().equalsIgnoreCase(endTollway))
							end=tollways.get(twc).getStreetByName(endExit);
					}
					/* Creates a new Connection object with the streets objects just initialized, and the tollway
					 *  names and adds the object to the connections array.
					 */
					Connection newConnection = new Connection(start, startTollway, end, endTollway);
					connections.add(newConnection);
				}
			}
		}

		// Populate tolls list
		for (int twc=0; twc < ozTollXML.getTollwayCount(); twc++){
			for (int tec=0; tec < ozTollXML.getTollCount(twc); tec++){
				tollways.get(twc).addToll(ozTollXML.getTollPointRate(twc, tec, tollways.get(twc)));
			}
		}
		
		setValidStarts();

		finishedRead=true;
		try {
			synchronized (dataSync){
				dataSync.notify();
			}
		} catch (NullPointerException e){
			// Ignore Null pointer that occurs when the program is exiting
		}
	}
	
	/** This method finds the street with the lowest value for X, and returns the lowest value  
	 * 
	 * @return The lowest value for X
	 */
	public Coordinates[] getMapLimits(){
		Coordinates limit[] = new Coordinates[2];
		limit[0] = new Coordinates();
		limit[1] = new Coordinates();
		
		for (int twc=0; twc < tollways.size(); twc++){
			for (int ec=0; ec < tollways.get(twc).getStreets().size(); ec++){
				if (((twc==0)&&(ec==0))||(tollways.get(twc).getStreets().get(ec).getX()<limit[0].getX()))
					limit[0].setX(tollways.get(twc).getStreets().get(ec).getX());
				if (tollways.get(twc).getStreets().get(ec).getX()>limit[1].getX())
					limit[1].setX(tollways.get(twc).getStreets().get(ec).getX());
				if (((twc==0)&&(ec==0))||(tollways.get(twc).getStreets().get(ec).getY()<limit[0].getY()))
					limit[0].setY(tollways.get(twc).getStreets().get(ec).getY());
				if (tollways.get(twc).getStreets().get(ec).getY()>limit[1].getY())
					limit[1].setY(tollways.get(twc).getStreets().get(ec).getY());
			}
		}
		return limit;
	}
	
	public TollCharges getTollRate(Street start, Street end, Tollway tollway){
		TollCharges charges= new TollCharges();
		for (int tc=0; tc<tollway.getTollPoints().size(); tc++){
			TollPoint currentTollPoint = tollway.getTollPoints().get(tc); 
			if (currentTollPoint.isStart(start.getName())){
				for (int tpe=0; tpe < currentTollPoint.getExit().size(); tpe++){
					TollPointExit currentExit = currentTollPoint.getExit().get(tpe);
					if (currentExit.isExit(end.getName())){
						charges.tollway=tollway.getName();
						charges.tolls=currentExit.getRates();
					}
				}
			}
		}
		return charges;
	}
	
	/**
	 * This function calculates the Toll Rates and returns it to be displayed
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<TollCharges> getFullRate(Street start, Street end){
		Tollway startTollway=null, endTollway=null;
		ArrayList<TollCharges> charges = new ArrayList<TollCharges>();
		
		for (int twc=0; twc < tollways.size(); twc++){
			for (int sc=0; sc < tollways.get(twc).getStreets().size(); sc++){
				if (tollways.get(twc).getStreets().get(sc).equals(start)){
					startTollway = tollways.get(twc);
				}
				if (tollways.get(twc).getStreets().get(sc).equals(end)){
					endTollway = tollways.get(twc);
				}
			}
		}
		
		// Single Tollway
		if (startTollway.equals(endTollway)){
			charges.add(getTollRate(start, end, startTollway));
		} else {
			// Multiple Tollways
			// This code only allows for 2 tollways so far. I will recode this area
			// when the tollways demand travel through 3 tollways.
			Connection currentConnection=null;
			int cc=0;
			while ((currentConnection==null)&&(cc<connections.size())){
				if (((connections.get(cc).getStartTollway().equalsIgnoreCase(startTollway.getName()))&&
					(connections.get(cc).getEndTollway().equalsIgnoreCase(endTollway.getName())))||
					((connections.get(cc).getStartTollway().equalsIgnoreCase(endTollway.getName()))&&
					(connections.get(cc).getEndTollway().equalsIgnoreCase(startTollway.getName())))){
					currentConnection = connections.get(cc);
				}
				cc++;
			}
			if (currentConnection!=null){
				Street startTollwayEnd, endTollwayStart;
				
				if (currentConnection.getStartTollway().equals(startTollway.getName())){
					startTollwayEnd=currentConnection.getStart();
				} else {
					startTollwayEnd=currentConnection.getEnd();
				}
				if (start!=startTollwayEnd)
					charges.add(getTollRate(start,startTollwayEnd,startTollway));
				if (currentConnection.getEndTollway().equals(endTollway.getName())){
					endTollwayStart=currentConnection.getEnd();
				} else {
					endTollwayStart=currentConnection.getStart();
				}
				if (end!=endTollwayStart)
					charges.add(getTollRate(endTollwayStart,end,endTollway));
			}
		}
		
		return charges;
	}

	public void setValidStarts(){
		for (int twc=0; twc < tollways.size(); twc++)
			for (int tpc=0; tpc < tollways.get(twc).getTollPoints().size(); tpc++)
				tollways.get(twc).getTollPoints().get(tpc).setStartValid();
	}
	
	public void setStreetsToInvalid(){
		for (int twc=0; twc< getTollwayCount(); twc++){
			for (int sc=0; sc < getStreetCount(twc); sc++){
				getStreet(twc, sc).setValid(false);
				//Log.w("ozToll","findStreet().setStreetsToInvalid().Street"+getStreet(twc,sc).getName());
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
			ArrayList<Street> exitList= getTollPointExits(validStreet);
			ArrayList<Street> tollwayConnections= new ArrayList<Street>();
			if (exitList.size()>0)
				for (int elc=0; elc < exitList.size(); elc++){
					exitList.get(elc).setValid(true);
					for (int cc=0; cc < getConnectionCount(); cc++){
						if (exitList.get(elc)==getConnection(cc).getStart())
							tollwayConnections.add(getConnection(cc).getEnd());
						if (exitList.get(elc)==getConnection(cc).getEnd())
							tollwayConnections.add(getConnection(cc).getStart());
					}
				}
			if (tollwayConnections.size()>0)
				for (int tc=0; tc < tollwayConnections.size(); tc++){
					markRoads(tollwayConnections.get(tc));
				}
		}
	}
	
	public int getTollwayCount() {
		return tollways.size();
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public int getTollCount(int twc) {
		return tollways.get(twc).getTollPoints().size();
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
	
	public int getStreetCount(int twc) {
		return tollways.get(twc).getStreets().size();
	}
	
	public int getPathwayCount(int tollway){
		return tollways.get(tollway).getPaths().size();
	}
	
	public Pathway getPathway(int tollway, int pathwayItem){
		return tollways.get(tollway).getPaths().get(pathwayItem);
	}
	
	/** This function returns an array of pathways, including connections, with the
	 *  requested street in it
	 * @param street
	 * @return Array of Pathways
	 */
	public ArrayList<Pathway> getPathway(Street street){
		ArrayList<Pathway> paths = new ArrayList<Pathway>();
		
		/**
		 * Search the tollways for the street and add them to an array to be returned
		 */
		for (int twc=0; twc < getTollwayCount(); twc++)
			for (int pwc=0; pwc < getPathwayCount(twc); pwc++){
				if ((getPathway(twc, pwc).getStart().equals(street))||
					(getPathway(twc, pwc).getEnd().equals(street)))
					paths.add(getPathway(twc,pwc));
			}
		
		/**
		 * The following goes through the tollway connections and adds those paths to
		 * the pathway returned too.
		 */
		for (int cc=0; cc < getConnectionCount(); cc++){
			if ((getConnection(cc).getStart().equals(street))||
					(getConnection(cc).getEnd().equals(street)))
				paths.add((Pathway)getConnection(cc));
		}
		return paths;
	}
	
	/** 
	 * This method takes the start and end of the pathway to mark. It will create an array
	 * or the paths to mark, including connections.
	 * @param start - starting point
	 * @param end - end point
	 * @return Array of path from Start to end
	 */
	public ArrayList<Pathway> getPathway(Street start, Street end){
		ArrayList<Pathway> paths = new ArrayList<Pathway>();
		//ArrayList<Pathway> decisions = new ArrayList<Pathway>();
		boolean endFound = false;
		
		Street street=start;
		Pathway lastDecision=null;
		while (!endFound){
			ArrayList<Pathway> currentPaths = getPathway(street);
			
			switch(currentPaths.size()){
				case 1:
					if (paths.size()>1){
						if (currentPaths.get(0)!=paths.get(paths.size()-1)){
							// If this path does not equal the last road in the array found
							// add it
							paths.add(currentPaths.get(0));
						} else {
							/**
							 * If the single road we have is the same as the last road, ie
							 * we hit a dead end, and not the end of our toll trip, we need to
							 * delete the roads from here back to the last intersection to find our way
							 * to the end of the toll trip.
							 */
							int pc=paths.size()-1;
							boolean lastDecisionFound=false;
							while ((pc>=0)&&(!lastDecisionFound)){
								if (paths.get(pc).equals(lastDecision)){
									lastDecisionFound=true;
								}
								paths.remove(pc);
								pc--;
							}
							// Need to change street now. compare lastDecision start
							if ((paths.get(paths.size()-1).getStart()==lastDecision.getStart())||
								(paths.get(paths.size()-1).getStart()==lastDecision.getEnd())){
								// Setting it to opposite, so that when it's checked at the end of
								// the while loop it will correct it.
								street=paths.get(paths.size()-1).getEnd();
							} else {
								street=paths.get(paths.size()-1).getStart();
							}
						}
					} else {
						// First street only gave us 1 path, so we add it
						paths.add(currentPaths.get(0));
					}
					break;
				case 2:
					if (paths.size()>0){
						/**
						 * If we already have streets in our array that we are collecting
						 * we need to find which of the 2 paths grabbed with the street
						 * we have searched for, is not in our paths array and add it
						 */
						int cpc=0;
						boolean streetAdded=false;
						while ((cpc < currentPaths.size())&&(!streetAdded)){
							if (!currentPaths.get(cpc).equals(paths.get(paths.size()-1))){
								paths.add(currentPaths.get(cpc));
								streetAdded=true;
							}
							cpc++;
						}
					} else {
						/**
						 * If we start with 2 options with which to go, we need to work out
						 * the correct trip. Generally we guess, and follow the path to its end,
						 * and if it's wrong we will find out in 'case 1:' and come back here, to
						 * decide again.
						 */
						int cpc=0;
						boolean startFound=false;
						while ((cpc<currentPaths.size())&&(!startFound)){
							Pathway currentPath = currentPaths.get(cpc);
							/**
							 *  Start = 1,1
							 *  End= 4,4
							 *  
							 *   Just take it 1 step at a time. Evaluate each and every possible 
							 *   solution, then worry about optimizing the code.									
							 */
							if ((currentPath.getStart()==start)&&
								(start.getX()==currentPath.getEnd().getX())&&
								((currentPath.getEnd().getY()>start.getY())&&
								 (currentPath.getEnd().getY()<=end.getY()))){
								paths.add(currentPath);
								lastDecision=currentPath;
								startFound=true;
							} else if ((currentPath.getEnd()==start)&&
									   (start.getX()==currentPath.getStart().getX())&&
									   ((currentPath.getStart().getY()>start.getY())&&
									    (currentPath.getStart().getY()<end.getY()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getStart()==start)&&
									   (start.getX()==currentPath.getEnd().getX())&&
									   ((currentPath.getEnd().getY()<start.getY())&&
									    (currentPath.getEnd().getY()>end.getY()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getEnd()==start)&&
									   (start.getX()==currentPath.getStart().getX())&&
									   ((currentPath.getStart().getY()<start.getY())&&
									    (currentPath.getStart().getY()>=end.getY()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getStart()==start)&&
									(start.getY()==currentPath.getEnd().getY())&&
									((currentPath.getEnd().getX()<start.getX())&&
									 (currentPath.getEnd().getX()>end.getX()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getEnd()==start)&&
										(start.getY()==currentPath.getStart().getY())&&
										((currentPath.getStart().getX()<start.getX())&&
										 (currentPath.getStart().getX()>=end.getX()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getStart()==start)&&
									(start.getY()==currentPath.getEnd().getY())&&
									((currentPath.getEnd().getX()>start.getX())&&
									 (currentPath.getEnd().getX()<=end.getX()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getEnd()==start)&&
										(start.getY()==currentPath.getStart().getY())&&
										((currentPath.getStart().getX()>start.getX())&&
										 (currentPath.getStart().getX()<end.getX()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if (((currentPath.getStart()==start)&&(currentPath.getEnd()==end))||
									   ((currentPath.getStart()==end)&&(currentPath.getEnd()==start))){
								paths.add(currentPath);
								startFound=true;
							}
							
							cpc++;
						}
					}
					
					break;
				case 3:
					/**
					 *  This section of code is called when we have 3 paths with the street name
					 *  to sort through. The program will make a guess at which road to take, if it's the wrong one,
					 *  it will hit 'case 1:' return to here and try a different path.
					 */
					if (paths.size()>0){
						int cpc=0;
						boolean pathFound=false;
						while ((cpc<currentPaths.size())&&(!pathFound)){
							Pathway currentPath = currentPaths.get(cpc);
							if ((!currentPath.equals(paths.get(paths.size()-1)))&&
								(!currentPath.equals(lastDecision))){
								if ((currentPath.getStart()==street)&&
									(street.getY()==currentPath.getEnd().getY())&&
									((currentPath.getEnd().getX()>street.getX())&&
									 (currentPath.getEnd().getX()<end.getX()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getEnd()==street)&&
										   (street.getY()==currentPath.getStart().getY())&&
										   ((currentPath.getStart().getX()>street.getX())&&
										    (currentPath.getStart().getX()<end.getX()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getEnd()==street)&&
										   (street.getY()==currentPath.getStart().getY())&&
										   ((currentPath.getStart().getX()<street.getX())&&
										    (currentPath.getStart().getX()>end.getX()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getStart()==street)&&
										   (street.getY()==currentPath.getEnd().getY())&&
										   ((currentPath.getEnd().getX()<street.getX())&&
										    (currentPath.getEnd().getX()>end.getX()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getEnd()==street)&&
										   (street.getX()==currentPath.getStart().getX())&&
										   ((currentPath.getStart().getY()<street.getY())&&
										    (currentPath.getStart().getY()>=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getStart()==street)&&
										   (street.getX()==currentPath.getEnd().getX())&&
										   ((currentPath.getEnd().getY()<street.getY())&&
										    (currentPath.getEnd().getY()>=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getEnd()==street)&&
										   (street.getX()==currentPath.getStart().getX())&&
										   ((currentPath.getStart().getY()>street.getY())&&
										    (currentPath.getStart().getY()<=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								} else if ((currentPath.getStart()==street)&&
										   (street.getX()==currentPath.getEnd().getX())&&
										   ((currentPath.getEnd().getY()>street.getY())&&
										    (currentPath.getEnd().getY()<=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
								}
							}
							cpc++;
						}
					} else {
						int cpc=0;
						boolean pathFound=false;
						while ((!pathFound)&&(cpc<currentPaths.size())){
							Pathway currentPath = currentPaths.get(cpc);
							
							if ((currentPath.getStart()==street)&&
								(street.getY()==currentPath.getEnd().getY())&&
								((currentPath.getEnd().getX()>street.getX())&&
								 (currentPath.getEnd().getX()<end.getX()))){
								paths.add(currentPath);
								lastDecision=currentPath;
								pathFound=true;
							} else if ((currentPath.getStart()==street)&&
									   (street.getX()==currentPath.getEnd().getX())&&
									   ((currentPath.getEnd().getY()<street.getY())&&
									    (currentPath.getEnd().getY()>=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
							} else if ((currentPath.getEnd()==street)&&
									   (street.getX()==currentPath.getStart().getX())&&
									   ((currentPath.getStart().getY()<street.getY())&&
									    (currentPath.getStart().getY()>=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
							} else if ((currentPath.getStart()==street)&&
									   (street.getX()==currentPath.getEnd().getX())&&
									   ((currentPath.getEnd().getY()>street.getY())&&
									    (currentPath.getEnd().getY()<=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
							} else if ((currentPath.getEnd()==street)&&
									   (street.getX()==currentPath.getStart().getX())&&
									   ((currentPath.getStart().getY()>street.getY())&&
									    (currentPath.getStart().getY()<=end.getY()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
							} else if ((currentPath.getEnd()==street)&&
									   (street.getY()==currentPath.getStart().getY())&&
									   ((currentPath.getStart().getX()<street.getX())&&
									    (currentPath.getStart().getX()>=end.getX()))){
									paths.add(currentPath);
									lastDecision=currentPath;
									pathFound=true;
							}
							cpc++;
						}
					}
					break;
				case 0:
				default:
					break;
			}
			/**
			 * The follow if statement works out which of the two roads in the current
			 *  pathway is the one we just searched for, and sets the next road to be searched for
			 *  as it will be the other end of the pathway.
			 */
			if (paths.size()>0)
				if (paths.get(paths.size()-1).getStart()==street)
					street=paths.get(paths.size()-1).getEnd();
				else
					street=paths.get(paths.size()-1).getStart();
			/**
			 * Checks to see if we have hit the other end of the user's trip so
			 * we can finish marking roads.
			 */
			if ((paths.get(paths.size()-1).getEnd().equals(end))||
				(paths.get(paths.size()-1).getStart().equals(end)))
				endFound=true;
		}
		return paths;
	}
	
	public int getConnectionCount(){
		return connections.size();
	}
	
	public Connection getConnection(int connectionID){
		return connections.get(connectionID);
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
		
		return tollways.get(twc).getName();
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
	
	public boolean isFinished(){
		return finishedRead;
	}

	public void setPreferences(SharedPreferences sP) {
		sharedPreferences = sP;
	}
	
	public SharedPreferences getPreferences(){
		return sharedPreferences;
	}
	
	/** This is used to test if the Toll rate is found in the processToll method
	 * @param tollType
	 * @param selectedVehicle
	 * @return
	 */
	public boolean isTollRateFound(String tollType, String selectedVehicle){
		if ((tollType.contains(selectedVehicle))||
			((selectedVehicle.equalsIgnoreCase("lcv"))&&
			 (tollType.contains("cv")&&
			 (!tollType.contains("hcv"))))||
			((selectedVehicle.equalsIgnoreCase("hcv"))&&
			 (tollType.contains("cv")&&
			 (!tollType.contains("lcv"))))||
			((selectedVehicle.equalsIgnoreCase("motorcycle"))&&
			 (tollType.equalsIgnoreCase("mc")))){
			return true;
		} else {
			return false;
		}
	}
	
	/** Originally processToll was written with the start & finish being passed in, but as the program has
	 * evolved, the start & finish have been stored in this object.
	 * @param appContext
	 * @return
	 */
	public LinearLayout processToll(Context appContext){
		if ((getStart()!=null)&&(getFinish()!=null))
			return processToll(getStart(),getFinish(), appContext);
		else
			return null;
	}
	
	public LinearLayout processToll(Street start, Street finish, Context appContext){
		String title="";
		
		String selectedVehicle = getPreferences().getString("vehicleType", "car");
		ArrayList<TollCharges> tolls = getFullRate(start, finish);
		
		ArrayList<TollRate> totalCharges = new ArrayList<TollRate>();
		TextView tollTitle;
		LinearLayout.LayoutParams fillParentParams, wrapContentParams;

		/* Need to create a linearLayout, and put all the stuff in it for
		 * ozView to then read to show the user.
		 */
		LinearLayout rateLayout = new LinearLayout(appContext);
		rateLayout.setOrientation(LinearLayout.VERTICAL);
		tollTitle = new TextView(appContext);

		// LinearLayout.LayoutParams to shorten the height of the textview
		fillParentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		fillParentParams.setMargins(0, 0, 0, -30);
		wrapContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		wrapContentParams.setMargins(0, 0, 0, -30);
		
		if (selectedVehicle.equalsIgnoreCase("car")){
			title="<h2>Car</h2>";
		} else if (selectedVehicle.equalsIgnoreCase("lcv")){
			title="<h2>Light Commercial Vehicle</h2>";
		} else if (selectedVehicle.equalsIgnoreCase("hcv")){
			title="<h2>Heavy Commercial Vehicle</h2>";
		} else if (selectedVehicle.equalsIgnoreCase("motorcycle")){
			title="<h2>Motorcycle</h2>";
		} else if (selectedVehicle.equalsIgnoreCase("all")){
			title="<h2>All</h2>";
		}
		
		tollTitle.setText(Html.fromHtml(title));
		tollTitle.setPadding(10, 0, 0, 0);
		tollTitle.setLayoutParams(fillParentParams);
		rateLayout.addView(tollTitle);
		
		/* The following for loop with traverse the toll result for the current trip.
		 */
		for (int tc=0; tc < tolls.size(); tc++){
			TollCharges currentToll = tolls.get(tc);
			
			/* The following toll loop will search the current toll Array for any tolls relating to the selected vehicle
			 * toll.
			 */
			int tollTypeCount=0;
			int trc=0;
			TextView tollwayName = new TextView(appContext);
			
			/* The following if statement block will select the correct tolls from the toll list
			 * to be shown to the user. 
			 */
			if (!selectedVehicle.equalsIgnoreCase("all")){
				for (trc=0; trc < currentToll.tolls.size(); trc++){
					if (isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle))
						tollTypeCount++;
				}
			} else {
				tollTypeCount = currentToll.tolls.size();
			}
			
			switch (tollTypeCount){
				case 0:
					// Don't want to display anything if it doesn't exist at least once.
					break;
				case 1:
					// If it exists only once in the toll Array, it will put the charge on the one line with the
					// tollway Name.
					LinearLayout tollwayLayout = new LinearLayout(appContext);
					tollwayLayout.setOrientation(LinearLayout.HORIZONTAL);
					tollwayName.setText(Html.fromHtml("<h3>"+currentToll.tollway+"</h3>"));
					tollwayName.setPadding(10, 0, 20, 0);

					tollwayLayout.addView(tollwayName);
					tollwayLayout.setLayoutParams(wrapContentParams);
					
					/* The following while loop will traverse through the currentToll list to find the only
					 * entry in the toll list relating to the selected vehicle, and will add it to the rateLayout.
					 * As there is only one, 
					 */
					trc=0;
					boolean found=false;
					
					while ((trc<currentToll.tolls.size())&&
							(!found)){
						if (isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle)){
							TextView tollwayCharge = new TextView(appContext);
							tollwayCharge.setText(currentToll.tolls.get(trc).rate);
							tollwayLayout.addView(tollwayCharge);
							rateLayout.addView(tollwayLayout);
							found=true;
						} else
							trc++;
					}
					/* If the path selected has more than 1 tollway we need to create the totalCharges array
					 */
					if (tolls.size()>1){
						
						// if there are no entries in totalCharges yet, add the first.
						if (totalCharges.size()<1){
							TollRate currentRate = new TollRate();
							if (isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle)){

								// Set the single tolls to a single toll rate name, to be distinguished down below when
								// the totals are merged
								if (selectedVehicle.equalsIgnoreCase("car"))
									currentRate.vehicleType="Car";
								else if (selectedVehicle.equalsIgnoreCase("lcv"))
									currentRate.vehicleType="light commercial vehicle";
								else if (selectedVehicle.equalsIgnoreCase("hcv"))
									currentRate.vehicleType="Heavy Commercial Vehicle";
								else if (selectedVehicle.equalsIgnoreCase("motorcycle"))
									currentRate.vehicleType="Motorcycle";
								
								currentRate.rate=currentToll.tolls.get(trc).rate;
								totalCharges.add(currentRate);
							}
						} else
							// if there is 1 or more entries in totalCharges
							if (totalCharges.size()==1){
								totalCharges.get(0).rate = Float.toString(
										Float.parseFloat(totalCharges.get(0).rate)+Float.parseFloat(currentToll.tolls.get(trc).rate));
							} else
								// If the is only 1 car toll, but more than one in totalCharges.
								for (int ttc=0; ttc < totalCharges.size(); ttc++){
									totalCharges.get(ttc).rate = Float.toString(
											Float.parseFloat(totalCharges.get(ttc).rate)+Float.parseFloat(currentToll.tolls.get(trc).rate));
								}
					}
					break;
				default:
					// Need to Sort out the headings when grabbing all results
					// More than one type of toll for this vehicle
					tollwayName.setText(Html.fromHtml("<h3>"+currentToll.tollway+"</h3>"));
					tollwayName.setPadding(10, 0, 0, 0);
					tollwayName.setLayoutParams(fillParentParams);
					rateLayout.addView(tollwayName);
					
					String variation ="";
					
					trc=0;
					int ttfound=0;
					while ((trc<currentToll.tolls.size())&&
						   (ttfound<tollTypeCount)){
						// Currently transforming code in the comments below to work for all vehicles
						// and not just cars
						if ((isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle))||
							(selectedVehicle.equalsIgnoreCase("all"))){
							boolean otherFound=false;
							if (!selectedVehicle.equalsIgnoreCase("all")){
								if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car"))
									variation = "Week days";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car-we"))
									variation = "Weekends";
								else if (currentToll.tolls.get(trc).vehicleType.contains("cv-day"))
									variation = "Day time";
								else if (currentToll.tolls.get(trc).vehicleType.contains("cv-night"))
									variation = "Night time";
							} else {
								if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car")){
									for (int check=0; check<currentToll.tolls.size(); check++)
										if (currentToll.tolls.get(check).vehicleType.equalsIgnoreCase("car-we"))
											otherFound=true;
									if (otherFound)
										variation="Week days";
									else
										variation="Car";
								} else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car-we"))
									variation="Weekends";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv"))
									variation="Light Commercial Vehicle";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv"))
									variation="Heavy Commercial Vehicle";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv-day"))
									variation="Heavy Commercial Vehicle Daytime";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv-night"))
									variation="Heavy Commercial Vehicle Nighttime";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("cv-day"))
									variation="Commercial Vehicle Daytime";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("cv-night"))
									variation="Commercial Vehicle Nighttime";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv-day"))
									variation="Light Commercial Vehicle Daytime";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv-night"))
									variation="Light Commercial Vehicle Nighttime";
								else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("mc"))
									variation="Motorcycle";
							}
							
							/* The following adds the content to the dialog Window */
							LinearLayout tollRateLayout = new LinearLayout(appContext);
							tollRateLayout.setOrientation(LinearLayout.HORIZONTAL);
							TextView rateTitle = new TextView(appContext);
							rateTitle.setText(variation);
							rateTitle.setPadding(10, 0, 10, 0);
							tollRateLayout.addView(rateTitle);
							TextView rateValue = new TextView(appContext);
							rateValue.setText(currentToll.tolls.get(trc).rate);
							tollRateLayout.addView(rateValue);
							rateLayout.addView(tollRateLayout);
							
							/* If there is more than 1 tollway in the path, we add it to the total charges */
							if (tolls.size()>1){
								if (totalCharges.size()<1){
									// Adds the first tollway to the totalCharges Array
									TollRate currentRate = new TollRate();
									currentRate.vehicleType = variation;
									currentRate.rate = currentToll.tolls.get(trc).rate;
									totalCharges.add(currentRate);
								} else {
									// If there is 1 or more tolls in the totalCharges array
									boolean totalChargeFound=false;
									for (int ttc=0; ttc < totalCharges.size(); ttc++){
										if (totalCharges.get(ttc).vehicleType.equalsIgnoreCase(variation)){
											totalCharges.get(ttc).rate = Float.toString(
													Float.parseFloat(totalCharges.get(ttc).rate)+
													Float.parseFloat(currentToll.tolls.get(trc).rate));
											totalChargeFound=true;
										}
									}
									if (!totalChargeFound){
										TollRate currentRate = new TollRate();
										currentRate.vehicleType = variation;
										currentRate.rate = currentToll.tolls.get(trc).rate;
										totalCharges.add(currentRate);
									}
								}
							}
							ttfound++;
						}
						trc++;
					}
					break;
			}
		}
	
		/* The following code will compress the total entry */
		if (tolls.size()>1){
						
			int tcc=0;
			boolean second=false;
			while ((tcc<totalCharges.size())&&(!second)){
				
				// need to search to see if car, lcv, hcv, and cv exists to combine them
				ArrayList<String> matchingValues = new ArrayList<String>();
				if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("car")){
					matchingValues.add("Week days");
					matchingValues.add("Weekends");
					convertTollTotal(tcc, matchingValues, totalCharges);
				} else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("light commercial vehicle")){
					matchingValues.add("Light Commercial Vehicle Daytime");
					matchingValues.add("Light Commercial Vehicle Nighttime");
					if(!convertTollTotal(tcc, matchingValues, totalCharges)){
						boolean commFound=false;
						for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
							if ((totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Daytime"))||
								(totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Nighttime")))
								commFound=true;
						}
						if (commFound){
							TollRate newTollRate;
							for (int mvc=0; mvc<matchingValues.size(); mvc++){
								newTollRate = new TollRate();
								newTollRate.vehicleType=matchingValues.get(mvc);
								newTollRate.rate=totalCharges.get(tcc).rate;
								totalCharges.add(newTollRate);
							}
							
							totalCharges.remove(tcc);
							matchingValues = new ArrayList<String>();
							tcc=-1;
						}
					}
				} else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Heavy Commercial Vehicle")){
					matchingValues.add("Heavy Commercial Vehicle Daytime");
					matchingValues.add("Heavy Commercial Vehicle Nighttime");
					if(!convertTollTotal(tcc, matchingValues, totalCharges)){
						boolean commFound=false;
						for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
							if ((totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Daytime"))||
								(totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Nighttime")))
								commFound=true;
						}
						if (commFound){
							TollRate newTollRate;
							for (int mvc=0; mvc<matchingValues.size(); mvc++){
								newTollRate = new TollRate();
								newTollRate.vehicleType=matchingValues.get(mvc);
								newTollRate.rate=totalCharges.get(tcc).rate;
								totalCharges.add(newTollRate);
							}
							
							totalCharges.remove(tcc);
							matchingValues = new ArrayList<String>();
							tcc=-1;
						}
					}
				} else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Commercial Vehicle Daytime")){
					boolean lcvConvert=false;
					boolean hcvConvert=false;
					for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
						if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("light commercial vehicle"))
							lcvConvert=true;
						if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("heavy commercial vehicle"))
							hcvConvert=true;
					}
					if ((!lcvConvert)&&(!hcvConvert)){
						matchingValues.add("Light Commercial Vehicle Daytime");
						matchingValues.add("Heavy Commercial Vehicle Daytime");
						if(convertTollTotal(tcc, matchingValues, totalCharges)){
							tcc=-1;
						}
					}
				} else 	if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Commercial Vehicle Nighttime")){
					boolean lcvConvert=false;
					boolean hcvConvert=false;
					for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
						if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("light commercial vehicle"))
							lcvConvert=true;
						if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("heavy commercial vehicle"))
							hcvConvert=true;
					}
					if ((!lcvConvert)&&(!hcvConvert)){
						matchingValues.add("Light Commercial Vehicle Nighttime");
						matchingValues.add("Heavy Commercial Vehicle Nighttime");
						if(convertTollTotal(tcc, matchingValues, totalCharges)){
							tcc=-1;
						}
					}
				}
				tcc++;
				if (tcc>=totalCharges.size()){
					tcc=0;
					second=true;
				}
			}
			
			if (totalCharges.size()>1){
				TextView tollTotalTitle = new TextView(appContext);
				tollTotalTitle.setText(Html.fromHtml("<h3>Total Tolls</h3>"));
				tollTotalTitle.setPadding(10, 0, 0, 0);
				tollTotalTitle.setLayoutParams(fillParentParams);
				rateLayout.addView(tollTotalTitle);

				for (tcc=0; tcc < totalCharges.size(); tcc++){
					LinearLayout totalLine = new LinearLayout(appContext);
					totalLine.setOrientation(LinearLayout.HORIZONTAL);
					TextView totalType = new TextView(appContext);
					if (selectedVehicle.equalsIgnoreCase("all"))
						if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Week days"))
							totalCharges.get(tcc).vehicleType="Car - Week days";
						else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Weekends"))
							totalCharges.get(tcc).vehicleType="Car - Weekends";
					totalType.setText(totalCharges.get(tcc).vehicleType);
					totalType.setPadding(10, 0, 10, 0);
					totalLine.addView(totalType);
					TextView totalValue = new TextView(appContext);
					totalValue.setText(totalCharges.get(tcc).rate);
					totalLine.addView(totalValue);
					rateLayout.addView(totalLine);
				}
			} else if (totalCharges.size()==1){
				LinearLayout totalLine = new LinearLayout(appContext);
				totalLine.setOrientation(LinearLayout.HORIZONTAL);
				TextView tollTotalTitle = new TextView(appContext);
				tollTotalTitle.setText(Html.fromHtml("<h3>Total Tolls </h3>"));
				tollTotalTitle.setPadding(10, 0, 0, 0);
				totalLine.addView(tollTotalTitle);
				
				TextView totalValue = new TextView(appContext);
				totalValue.setText(totalCharges.get(0).rate);
				totalLine.addView(totalValue);
				rateLayout.addView(totalLine);
			}
		}
		
		return rateLayout;
	}
	
	/** This is used to merge toll totals.
	 * 
	 * @param from
	 * @param to
	 * @param totalCharges
	 * @return
	 */
	public boolean convertTollTotal(int from, ArrayList<String> to, ArrayList<TollRate> totalCharges){
		int itemCount=0;
		
		for (int tcc=0; tcc<totalCharges.size(); tcc++){
			for (int toCount=0; toCount< to.size(); toCount++){
				if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase(to.get(toCount))){
					totalCharges.get(tcc).rate = Float.toString((float)Math.round(
							(Float.parseFloat(totalCharges.get(tcc).rate)+
							 Float.parseFloat(totalCharges.get(from).rate))*100)/100);
					itemCount++;
				}
			}
		}
		if (itemCount>0){
			totalCharges.remove(from);
			return true;
		}
		return false;
	}

	public Street getStart() {
		return start;
	}

	public void setStart(Street start) {
		this.start = start;
		setStreetsToInvalid();
		markRoads(start);
	}

	public Street getFinish() {
		return finish;
	}

	public void setFinish(Street finish) {
		this.finish = finish;
	}
}
