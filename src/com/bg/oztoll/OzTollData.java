/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author bugman
 *
 */
public class OzTollData {
	private XmlReader xmldata;
	
	public OzTollData(){	
	}
	
	public OzTollData(String filename){
		xmldata = new XmlReader(filename);
	}
	
	public XmlReader getXmlReader(){
		return xmldata;
	}
	
	public void setXmlReader(String filename){
		xmldata = new XmlReader(filename);
	}
	
	
	public int getOriginX(){
		int minX=0;
		
		for(int twc=0; twc < getTollwayCount(); twc++){
			for (int ec=0; ec < countStreets(twc); ec++){
				if ((twc==0)&&(ec==0))
					minX=getStreetX(twc,ec);
				if (getStreetX(twc,ec)<minX)
					minX=getStreetX(twc,ec);
			}
		}
		return minX;
	}
	
	public int getOriginY(){
		int minY=0;
		
		for(int twc=0; twc < getTollwayCount(); twc++){
			for (int ec=0; ec < countStreets(twc); ec++){
				if ((twc==0)&&(ec==0))
					minY=getStreetY(twc,ec);
				if (getStreetX(twc,ec)<minY)
					minY=getStreetY(twc,ec);
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
			System.out.println("One tollway");
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
				NodeList tollPointEntry = currentElement.getElementsByTagName(tagName);
				if (tollPointEntry!=null)
					for (int tollPointEntryCount=0; tollPointEntryCount < tollPointEntry.getLength(); tollPointEntryCount++){
						Node currentEntryPoint = tollPointEntry.item(tollPointEntryCount);
						if (currentEntryPoint.getNodeType() == Node.ELEMENT_NODE){
							NodeList streetName = ((Element)currentEntryPoint).getChildNodes();
							if (((Node)streetName.item(0)).getNodeValue().equals(targetValue))
								return currentNode;
						}
					}
			}
		}
		
		return null;
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
		if (startNode!=null){
			Node exitNode = getNodeByTagValue(((Element)startNode).getElementsByTagName("exit"),"street",exit);
			if (exitNode!=null){
				// Grab the information from currentExitPoint using getNodeData
				String[] nodes = {"tolltypes","vehicle"};
				int[] index = {0};
				NodeList vehicleList = xmldata.getNodesList(nodes, index);
				if (vehicleList!=null){
					for (int vlc=0; vlc < vehicleList.getLength(); vlc++){
						Node vehicle = vehicleList.item(vlc);
						if (vehicle.getNodeType() == Node.ELEMENT_NODE){
							NodeList vehicleName = ((Element)vehicle).getChildNodes();
							// Grab vehicle names using this ((Node)vehicleName.item(0)).getNodeValue()
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
