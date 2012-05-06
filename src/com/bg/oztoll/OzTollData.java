/** This class stores all of the information extracted from the xml file, for use in our program.
 * Established early on that i couldn't read from the xml in real time as the program would not draw
 * the screen quick enough.
 */
package com.bg.oztoll;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.SharedPreferences;
import android.content.res.AssetManager;

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
		
		for (int twc=0; twc < ozTollXML.getTollwayCount(); twc++){
			Tollway newTollway = new Tollway(ozTollXML.getTollwayName(twc));
			// Populate the streets list in the tollway class
			for (int tsc=0; tsc < ozTollXML.getStreetCount(twc); tsc++){
				
				Street newStreet = new Street(ozTollXML.getStreetDetail(twc, tsc,"name"), 
											  Float.parseFloat(ozTollXML.getStreetDetail(twc, tsc,"x")),
											  Float.parseFloat(ozTollXML.getStreetDetail(twc, tsc,"y")),
											  Integer.parseInt(ozTollXML.getStreetDetail(twc, tsc, "location")));
				/* 
				 * I decided to use float globally for x,y details, as the screen location is stored as float
				 */
				if (newStreet!=null)
					newTollway.addStreet(newStreet);
			}
			/* 
			 * Populate pathway list, which is used to draw the roads on the screen. Pathways
			 * are the path, or road in the map.
			 */
			for (int pwc=0; pwc < ozTollXML.getTollPathwayCount(twc); pwc++){
				String streets[] = ozTollXML.getTollPath(twc, pwc);
				Street newStart, newEnd;
				newStart = newEnd = new Street();
				for (int sc=0; sc < newTollway.getStreets().size(); sc++){
					if (newTollway.getStreets().get(sc).getName().equalsIgnoreCase(streets[0]))
						newStart = newTollway.getStreets().get(sc);
					if (newTollway.getStreets().get(sc).getName().equalsIgnoreCase(streets[1]))
						newEnd = newTollway.getStreets().get(sc);
				}
				newTollway.addPath(newStart, newEnd);
			}
			
			// Array storing all tollways for current city
			tollways.add(newTollway);
			synchronized (dataSync){
				dataSync.notify();
			}
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

		synchronized (dataSync){
			dataSync.notify();
		}
		finishedRead=true;
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
				charges.add(getTollRate(start,startTollwayEnd,startTollway));
				if (currentConnection.getEndTollway().equals(endTollway.getName())){
					endTollwayStart=currentConnection.getEnd();
				} else {
					endTollwayStart=currentConnection.getStart();
				}
				charges.add(getTollRate(endTollwayStart,end,endTollway));
			}
		}
		
		return charges;
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
							//Log.w ("getPathway(Street, Street)","");
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
								 (currentPath.getEnd().getY()<end.getY()))){
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
									    (currentPath.getStart().getY()>end.getY()))){
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
										 (currentPath.getStart().getX()>end.getX()))){
								lastDecision=currentPath;
								paths.add(currentPath);
								startFound=true;
							} else if ((currentPath.getStart()==start)&&
									(start.getY()==currentPath.getEnd().getY())&&
									((currentPath.getEnd().getX()>start.getX())&&
									 (currentPath.getEnd().getX()<end.getX()))){
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
		if (tollways.size()>0)
			if (tollways.get(0).getStreets().size()>0)
				return tollways.get(twc).getStreets().get(twi);
			else
				return null;
		else
			return null;
	}
	
	public String getTollwayName(int tollway){
		return tollways.get(tollway).getName();
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
}
