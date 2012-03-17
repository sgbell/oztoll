/** This class stores all of the information extracted from the xml file, for use in our program.
 * Established early on that i couldn't read from the xml in real time as the program would not draw
 * the screen quick enough.
 */
package com.bg.oztoll;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

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
	 */
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
		
		for (int twc=0; twc < ozTollXML.getNodeListCount("tollway"); twc++){
			Tollway newTollway = new Tollway(ozTollXML.getTollwayName(twc));
			// Populate the streets list in the tollway class
			for (int tsc=0; tsc < ozTollXML.countStreets(twc); tsc++){
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
			// Populate tolls list
			for (int tec=0; tec < ozTollXML.getTollCount(twc); tec++){
				newTollway.addToll(ozTollXML.getTollPointRate(twc, tec, newTollway));
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
	
	/*
	public ArrayList<TollCharges> getFullRate(String entry, String exit){
		int entryTollRoadId=-1, exitTollRoadId=-1, roadId=0;
		boolean bothFound=false;
		ArrayList<TollCharges> tollCharges = new ArrayList<TollCharges>();
		
		do{
			for (int twc=0; twc < ozTollXML.getNodeListCount("tollway"); twc++)
				if (roadId<ozTollXML.countStreets(twc))
					if (ozTollXML.getStreetName(twc,roadId)!=null){
						if (getStreetName(twc,roadId).equals(entry))
							entryTollRoadId=twc;
						if (getStreetName(twc,roadId).equals(exit))
							exitTollRoadId=twc;
					}
			roadId++;
			if ((entryTollRoadId>-1)&&(exitTollRoadId>-1))
				bothFound=true;
		} while (!bothFound);
		if (entryTollRoadId==exitTollRoadId){
			TollCharges newTollCharges= new TollCharges();
			newTollCharges.tollway=ozTollXML.getTollwayName(entryTollRoadId);
			newTollCharges.tolls=getTollRate(entry,exit,entryTollRoadId);
			tollCharges.add(newTollCharges);
		} else {
			 * Need to read the connections tag from the xml file
			 * and then call getTollRate twice, and add charges together
			 * for a total. *
			NodeList nodeList = xmldata.getElementsByTagName("connection");
			if (nodeList!=null){
				String toll1Exit,
					   toll2Entry;
				for (int conc=0; conc < nodeList.getLength(); conc++){
					Node currentNode = nodeList.item(conc);
					String tollwayName=xmldata.getNodeAttribute(currentNode, "start", "tollway", 0);
					if (tollwayName.equals(ozTollXML.getTollwayName(entryTollRoadId))){
						toll1Exit=xmldata.getNodeAttribute(currentNode,"start", "exit", 0);
						TollCharges newTollCharges = new TollCharges();
						newTollCharges.tollway=ozTollXML.getTollwayName(entryTollRoadId);
						newTollCharges.tolls=getTollRate(entry,toll1Exit,entryTollRoadId);
						tollCharges.add(newTollCharges);
					}
				}
				for (int conc=0; conc < nodeList.getLength(); conc++){
					Node currentNode = nodeList.item(conc);
					String tollwayName=xmldata.getNodeAttribute(currentNode, "end", "tollway", 0);
					if (tollwayName.equals(ozTollXML.getTollwayName(exitTollRoadId))){
						toll2Entry=xmldata.getNodeAttribute(currentNode,"end", "exit", 0);
						TollCharges newTollCharges = new TollCharges();
						newTollCharges.tollway=ozTollXML.getTollwayName(exitTollRoadId);
						newTollCharges.tolls=getTollRate(toll2Entry,exit,exitTollRoadId);
						tollCharges.add(newTollCharges);
					}
				}
			}
		}
		return tollCharges;
	}
	*/

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
	
	/** This function returns an array of pathways with the requested street in it
	 * @param street
	 * @return
	 */
	public ArrayList<Pathway> getPathway(Street street){
		ArrayList<Pathway> paths = new ArrayList<Pathway>();
		
		for (int twc=0; twc < getTollwayCount(); twc++)
			for (int pwc=0; pwc < getPathwayCount(twc); pwc++){
				if ((getPathway(twc, pwc).getStart().equals(street))||
					(getPathway(twc, pwc).getEnd().equals(street)))
					paths.add(getPathway(twc,pwc));
			}
		return paths;
	}
	
	/** This method takes the start and end of the pathway to mark. It will create an array
	 * or the paths to mark, including connections.
	 * @param start - starting point
	 * @param end - end point
	 * @return Array of path from Start to end
	 */
	public ArrayList<Pathway> getPathway(Street start, Street end){
		ArrayList<Pathway> paths = new ArrayList<Pathway>();
		boolean endFound = false;
		
		Log.w("tollData", "tollData.getPathway(Start, end)");
		Street street=start;
		while (!endFound){
			ArrayList<Pathway> currentPaths = getPathway(street);
			
			/**
			 *	  0,0	   1,0
			 *     x		x										x 6,0
			 *     
			 *     
			 *     			x 1,1									x 6,1
			 *     
			 *     
			 *     
			 *     			x 1,2									x 6,2
			 *     
			 *     						   3,3
			 *     			x 1,3			x						x 6,3
			 *     
			 *     
			 *     			x		x		x		x		x		x 6,4
			 *     		   1,4	   2,4	   3,4	   4,4	   5,4
			 *     
			 *     													x 6,5
			 *     
			 *     
			 *     													x 6,6
			 */
			
			switch(currentPaths.size()){
				case 1:
					Log.w("tollData", "tollData.1 Pathway found");
					Log.w("tollData", "tollData.currentPath Start: "+currentPaths.get(0).getStart().getName());
					Log.w("tollData", "tollData.currentPath End: "+currentPaths.get(0).getEnd().getName());
					paths.add(currentPaths.get(0));
					break;
				case 2:
					Log.w("tollData", "tollData.2 Paths found");
					if (paths.size()>0){
						for (int cpc=0; cpc < currentPaths.size(); cpc++)
							if (!currentPaths.get(cpc).equals(paths.get(paths.size()-1))){
								Log.w("tollData", "tollData.currentPath Start: "+currentPaths.get(cpc).getStart().getName());
								Log.w("tollData", "tollData.currentPath End: "+currentPaths.get(cpc).getEnd().getName());
								paths.add(currentPaths.get(cpc));
							}
					} else {
						
						/**
						 *  if (currentPaths(0)start=start){
						 *  	if (currentPaths(0)start.x==currentPaths(0)end.x){
						 *  		if (end.y>start.Y){
						 * 				if (currentPaths(0)end.y>currentPaths(0)start.y)
						 * 					paths.add(currentPaths(0));
						 * 			} else {
						 * 				if (currentPaths(0)end.y<currentPaths(0)start.y)
						 * 					paths.add(currentPaths(0));
						 * 			}
						 * 		} else if (currentPaths(0)start.y==currentPaths(0)end.y){
						 *			if (end.x>start.x){
						 *				if (currentPaths(0)end.x>currentPaths(0)start.x)
						 *					paths.add(currentPaths(0));
						 *			} else {
						 *				if (currentPaths(0)end.x<currentPaths(0)start.x)
						 *					paths.add(currentPaths(0));
						 *			}
						 * 		}
						 * 	}  
						 */
					}
					
					break;
				case 3:
					Log.w("tollData", "tollData.3 Paths found");
					break;
				case 0:
				default:
					break;
			}
			if (paths.size()>1);
				street=paths.get(paths.size()-1).getEnd();
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
		return tollways.get(twc).getStreets().get(twi);
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
}
