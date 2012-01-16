/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author bugman
 *
 */
public class OzTollData {
		
	private XmlReader xmldata;
	private Vector<Tollway> tollways;
	private String cityName;
		
	public OzTollData(){
		tollways = new Vector<Tollway>();
	}
	
	public OzTollData(String filename){
		this();
		setXmlReader(filename);
	}
	
	public XmlReader getXmlReader(){
		return xmldata;
	}
	
	public void setXmlReader(String filename){
		xmldata = new XmlReader(filename);
		tollways = new Vector<Tollway>();
		cityName = getCityName();
		for (int twc=0; twc < getTollwayCount(); twc++){
			Tollway newTollway = new Tollway(getTollwayName(twc));
			// Populate the streets list in the tollway class
			for (int tsc=0; tsc < countStreets(twc); tsc++){
				Street newStreet = new Street(getStreetName(twc, tsc), 
											  getStreetX(twc, tsc),
											  getStreetY(twc, tsc));
				if (newStreet!=null)
					newTollway.addStreet(newStreet);
			}
			// Populate pathway list, which is used to draw the roads on the screen
			for (int pwc=0; pwc < getTollPathwayCount(twc); pwc++){
				String streets[] = getTollPath(twc, pwc);
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
			for (int tec=0; tec < getTollCount(twc); tec++){
				newTollway.addToll(getTollPointRate(twc, tec, newTollway));
			}
			
			// Array storing all tollways for current city
			tollways.add(newTollway);
		}
	}
	
	
	public long getOriginX(){
		long minX=0;
		
		for (int twc=0; twc < tollways.size(); twc++){
			for (int ec=0; ec < tollways.get(twc).getStreets().size(); ec++){
				if (((twc==0)&&(ec==0))||(tollways.get(twc).getStreets().get(ec).getX()<minX))
					minX=tollways.get(twc).getStreets().get(ec).getX();
			}
		}
		return minX;
	}
	
	public long getOriginY(){
		long minY=0;
		
		for(int twc=0; twc < tollways.size(); twc++){
			for (int ec=0; ec < tollways.get(twc).getStreets().size(); ec++){
				if (((twc==0)&&(ec==0))||(tollways.get(twc).getStreets().get(ec).getY()<minY))
					minY=tollways.get(twc).getStreets().get(ec).getY();
			}
		}
		return minY;
	}
	
	/**
	 * This will read the city name from the xml file.
	 * @return city name stored in xml file.
	 */
	public String getCityName(){
		NodeList nodeList = xmldata.getElementsByTagName("city");
		for (int s = 0; s < nodeList.getLength(); s++) {
			Node currentNode = nodeList.item(s);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				return xmldata.getNodeData(currentNode,"name");
			}
		}		
		return null;				
	}
	
	/**
	 * Counts the number of tollways in the xml file
	 * @return
	 */
	public int getTollwayCount(){
		NodeList nodeList = xmldata.getElementsByTagName("tollway");
		return nodeList.getLength();
	}
	
	/**
	 * This grabs the name of the requested tollway
	 * @param tollway
	 * @return
	 */
	public String getTollwayName(int tollway){
		NodeList nodeList = xmldata.getElementsByTagName("city");
		Node currentNode = nodeList.item(0);
		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
			return xmldata.getNodeAttribute(currentNode,"tollway","name",tollway);
		}
		return null;
	}
	
	/**
	 * This will return the number of Exits for the selected tollway
	 * @param tollway
	 * @return
	 */
	public int countStreets(int tollway){
		NodeList streetList=getStreetNodes(tollway);
		if (streetList!=null)
			return streetList.getLength();
		else
			return -1;
	}
	
	public int getTollCount(int tollway){
		NodeList tollList = getTollNodes(tollway);
		if (tollList!=null)
			return tollList.getLength();
		else
			return -1;
	}
	
	public String[] getTollPath(int tollway, int pathNum){
		NodeList path = getTollPathway(tollway);
		if (path!=null){
			Node pathNode = path.item(pathNum);
			String[] streets = {xmldata.getNodeData(pathNode, "start"),
					   			xmldata.getNodeData(pathNode, "end")};
			return streets;
		}
		
		return null;
	}
	
	public int getTollPathwayCount(int tollway){
		NodeList path = getTollPathway(tollway);
		if (path!=null)
			return path.getLength();
		else
			return -1;
	}
	
	public NodeList getTollPathway(int tollway){
		String[] nodes = {"tollway","pathway","path"};
		int[] index = {tollway,0};
		return xmldata.getNodesList(nodes, index);
	}
	
	public NodeList getTollNodes(int tollway){
		String[] nodes = {"tollway","tollpoint"};
		int[] index = {tollway};
		return xmldata.getNodesList(nodes,index);
	}
	
	public NodeList getStreetNodes(int tollway){
		String[] nodes = {"tollway","exit","street"};
		int[] index = {tollway,0};
		return xmldata.getNodesList(nodes,index);
	}
	
	/**
	 * This will return the Street name from the xml file
	 * @param tollway
	 * @param exitcount
	 * @return
	 */
	public String getStreetName(int tollway, int exitcount){
		NodeList streetList = getStreetNodes(tollway);
		if (streetList!=null){
			Node currentNode = streetList.item(exitcount);
			return xmldata.getNodeData(currentNode,"name");
		} else
			return null;
	}

	public int getStreetX(int tollway, int exitcount){
		NodeList streetList = getStreetNodes(tollway);
		if (streetList!=null){
			Node currentNode = streetList.item(exitcount);
			return Integer.parseInt(xmldata.getNodeData(currentNode,"x"));
			
		} else
			return -1;
	}
	
	public int getStreetY(int tollway, int exitcount){
		NodeList streetList = getStreetNodes(tollway);
		if (streetList!=null){
			Node currentNode = streetList.item(exitcount);
			return Integer.parseInt(xmldata.getNodeData(currentNode,"y"));
			
		} else
			return -1;
	}
	
	public ArrayList<TollCharges> getFullRate(String entry, String exit){
		int entryTollRoadId=-1, exitTollRoadId=-1, roadId=0;
		boolean bothFound=false;
		ArrayList<TollCharges> tollCharges = new ArrayList<TollCharges>();
		
		do{
			for (int twc=0; twc < getTollwayCount(); twc++)
				if (roadId<countStreets(twc))
					if (getStreetName(twc,roadId)!=null){
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
			newTollCharges.tollway=getTollwayName(entryTollRoadId);
			newTollCharges.tolls=getTollRate(entry,exit,entryTollRoadId);
			tollCharges.add(newTollCharges);
		} else {
			/* Need to read the connections tag from the xml file
			 * and then call getTollRate twice, and add charges together
			 * for a total. */
			NodeList nodeList = xmldata.getElementsByTagName("connection");
			if (nodeList!=null){
				String toll1Exit,
					   toll2Entry;
				for (int conc=0; conc < nodeList.getLength(); conc++){
					Node currentNode = nodeList.item(conc);
					String tollwayName=xmldata.getNodeAttribute(currentNode, "start", "tollway", 0);
					if (tollwayName.equals(getTollwayName(entryTollRoadId))){
						toll1Exit=xmldata.getNodeAttribute(currentNode,"start", "exit", 0);
						TollCharges newTollCharges = new TollCharges();
						newTollCharges.tollway=getTollwayName(entryTollRoadId);
						newTollCharges.tolls=getTollRate(entry,toll1Exit,entryTollRoadId);
						tollCharges.add(newTollCharges);
					}
				}
				for (int conc=0; conc < nodeList.getLength(); conc++){
					Node currentNode = nodeList.item(conc);
					String tollwayName=xmldata.getNodeAttribute(currentNode, "end", "tollway", 0);
					if (tollwayName.equals(getTollwayName(exitTollRoadId))){
						toll2Entry=xmldata.getNodeAttribute(currentNode,"end", "exit", 0);
						TollCharges newTollCharges = new TollCharges();
						newTollCharges.tollway=getTollwayName(exitTollRoadId);
						newTollCharges.tolls=getTollRate(toll2Entry,exit,exitTollRoadId);
						tollCharges.add(newTollCharges);
					}
				}
			}
		}
		return tollCharges;
	}
	
	/**
	 * Searches nodeList for the corresponding Tag and value
	 * @param nodeList - node list to search
	 * @param tagName - xml tag
	 * @param targetValue - requested value
	 * @return node with target value in child node
	 */
	public Node getNodeByTagValue(NodeList nodeList, String tagName, String targetValue){
		
		for (int nodeCount=0; nodeCount < nodeList.getLength(); nodeCount++){
			Node currentNode = nodeList.item(nodeCount);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE){
				Element currentElement = (Element) currentNode;
				NodeList childNodeList = currentElement.getElementsByTagName(tagName);
				if (childNodeList!=null)
					for (int childNodeCount=0; childNodeCount < childNodeList.getLength(); childNodeCount++){
						Node currentChild = childNodeList.item(childNodeCount);
						if (currentChild.getNodeType() == Node.ELEMENT_NODE){
							NodeList tag = ((Element)currentChild).getChildNodes();
							if (((Node)tag.item(0)).getNodeValue().equals(targetValue))
								return currentNode;
						}
					}
			}
		}
		return null;
	}

	/**
	 * This function is used to create a TollPoint object, with the start, and various
	 * exits found in the XML file, with the cost for the paths.
	 * @param newTollway 
	 * @param vector 
	 * @return
	 */
	public TollPoint getTollPointRate(int tollway, int tollpoint, Tollway tollwayData){
		TollPoint newTollPoint = new TollPoint();
		// Hard coding the toll types into the program.
		String tollType[] = {"car","car-we","lcv","lcv-day",
							 "lcv-night","hcv","hcv-day","hcv-night",
							 "cv-day","cv-night","mc"};
		
		NodeList tollnodes = getTollNodes(tollway);
		Node currentToll = tollnodes.item(tollpoint);
		Element tollElement = (Element)currentToll;
		NodeList startNodes = tollElement.getElementsByTagName("start");
		for (int tsc=0; tsc < startNodes.getLength(); tsc++){
			Element startElmnt = (Element) startNodes.item(tsc);
			if (startElmnt!= null){
				NodeList startname = startElmnt.getChildNodes();
				String start = ((Node) startname.item(0)).getNodeValue();
				newTollPoint.addStart(tollwayData.getStreetByName(start));
			}
		}
		NodeList exitNodes = tollElement.getElementsByTagName("exit");
		for (int tec=0; tec < exitNodes.getLength(); tec++){
			TollPointExit newTollPointExit = new TollPointExit();
			Element exitElmnt = (Element) exitNodes.item(tec);
			NodeList exitStreet = exitElmnt.getElementsByTagName("street");
			for (int tesc=0; tesc < exitStreet.getLength(); tesc++){
				Element exitStElmnt = (Element) exitStreet.item(tesc);
				if (exitStElmnt!=null){
					NodeList exitname = exitStElmnt.getChildNodes();
					String exitSt = ((Node) exitname.item(0)).getNodeValue();
					newTollPointExit.addExit(tollwayData.getStreetByName(exitSt));
				}
			}
			
			for (int trc=0; trc < tollType.length; trc++){
				TollRate tollrate = new TollRate();
				tollrate.rate = xmldata.getNodeData(exitNodes.item(tec), tollType[trc]);
				tollrate.vehicleType = tollType[trc];
				newTollPointExit.addRate(tollrate);
			}
			newTollPoint.addExit(newTollPointExit);
		}
		return newTollPoint;
	}
	
	/**
	 * This function scans through the xml to find the tollrate for the entry and exit.
	 * It is only used for a single tollway
	 * @param entry The name of the entry Street
	 * @param exit The name of the exit Street
	 * @param tollway The tollway
	 * @return an array of object TollRate, which contains the vehicle type and the rate
	 */
	public ArrayList<TollRate> getTollRate(String entry, String exit, int tollway) {
		ArrayList<TollRate> tolls= new ArrayList<TollRate>();
		
		Node startNode = getNodeByTagValue(getTollNodes(tollway),"start",entry);
		if ((startNode!=null)&&(startNode.getNodeType() == Node.ELEMENT_NODE)){
			Node exitNode = getNodeByTagValue(((Element)startNode).getElementsByTagName("exit"),"street",exit);
			if (exitNode!=null){
				String[] nodes = {"tolltypes","vehicle"};
				int[] index = {0};
				NodeList vehicleList = xmldata.getNodesList(nodes, index);
				if (vehicleList!=null){
					for (int vlc=0; vlc < vehicleList.getLength(); vlc++){
						Node vehicle = vehicleList.item(vlc);
						if (vehicle.getNodeType() == Node.ELEMENT_NODE){
							NodeList vehicleName = ((Element)vehicle).getChildNodes();
							String rate=xmldata.getNodeData(exitNode, ((Node)vehicleName.item(0)).getNodeValue());
							if (rate!=null){
								TollRate newTollrate = new TollRate();
								newTollrate.vehicleType=((Node)vehicleName.item(0)).getNodeValue();
								newTollrate.rate=rate;
								tolls.add(newTollrate);
							}
						}
					}
				}				
			}
		}
		return tolls;
	}
}
