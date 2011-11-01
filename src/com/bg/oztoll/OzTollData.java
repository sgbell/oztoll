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
			newTollCharges.tolls=getTollRate(entry,exit);
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
						newTollCharges.tolls=getTollRate(entry,toll1Exit);
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
						newTollCharges.tolls=getTollRate(toll2Entry,exit);
						tollCharges.add(newTollCharges);
					}
				}
			}
		}
		return tollCharges;
	}
	
	
	/**
	 * This function scans through the xml to find the tollrate for the entry and exit.
	 * It is only used for a single tollway
	 * @param entry
	 * @param exit
	 * @return
	 */
	public ArrayList<TollRate> getTollRate(String entry, String exit) {
		ArrayList<TollRate> tolls= new ArrayList<TollRate>();
		
		for (int trc=0; trc < getTollwayCount(); trc++){		
			NodeList tollList = getTollNodes(trc);
			if (tollList!=null){
				int tlc=0;
				do {
					Node currentNode = tollList.item(tlc);
					if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
						Element fstElmnt = (Element) currentNode;
						NodeList startList = fstElmnt.getElementsByTagName("start");
						if (startList!=null)
							for (int slc=0; slc < startList.getLength(); slc++)
								// If Entrance road is found
								if (startList.item(slc).getNodeValue().equals(entry)){
									NodeList exitData = fstElmnt.getElementsByTagName("exit");
									if (exitData!=null)
										for (int elc=0; elc < exitData.getLength(); elc++){
											Node exitNode=exitData.item(elc);
											if (exitNode.getNodeType() == Node.ELEMENT_NODE) {
												Element exitElement = (Element) exitNode;
												NodeList exits = exitElement.getElementsByTagName("street");
												if (exits!=null)
													for (int ec=0; ec < exits.getLength(); ec++)
														// If exit road is found
														if (exits.item(ec).getNodeValue().equals(exit)){
															String[] nodes = {"tolltypes","vehicle"};
															int[] index = {0};
															NodeList vehiclelist = xmldata.getNodesList(nodes,index);
															if (vehiclelist!=null){
																for (int vtc=0; vtc < vehiclelist.getLength(); vtc++){
																	try {
																		String toll = exitElement.getElementsByTagName(vehiclelist.item(vtc).getNodeValue()).item(0).getNodeValue();
																		TollRate newToll = new TollRate();
																		newToll.vehicleType=vehiclelist.item(vtc).getNodeValue();
																		newToll.rate=toll;
																		tolls.add(newToll);
																	} catch (NullPointerException e){
																		// do nothing
																	}
																}
															}
															return tolls;
														}
											}
										}
								}
					}
					tlc++;
				} while (tlc<tollList.getLength());
			}
		}
		return null;
	}
}
