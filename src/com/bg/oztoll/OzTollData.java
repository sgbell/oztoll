/**
 * 
 */
package com.bg.oztoll;

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
	private OzTollXML ozTollXML;
		
	public OzTollData(){
		tollways = new Vector<Tollway>();
		ozTollXML = new OzTollXML();
	}
	
	public OzTollData(String filename){
		this();
		ozTollXML.setXMLReader(filename);
		getTollwayData();
	}
	
	public XmlReader getXmlReader(){
		return xmldata;
	}
	
	public void getTollwayData(){
		tollways = new Vector<Tollway>();
		setCityName(ozTollXML.getCityName());
		
		for (int twc=0; twc < ozTollXML.getNodeListCount("tollway"); twc++){
			Tollway newTollway = new Tollway(ozTollXML.getTollwayName(twc));
			// Populate the streets list in the tollway class
			for (int tsc=0; tsc < ozTollXML.countStreets(twc); tsc++){
				Street newStreet = new Street(ozTollXML.getStreetDetail(twc, tsc,"name"), 
											  Long.parseLong(ozTollXML.getStreetDetail(twc, tsc,"x")),
											  Long.parseLong(ozTollXML.getStreetDetail(twc, tsc,"y")));
				if (newStreet!=null)
					newTollway.addStreet(newStreet);
			}
			// Populate pathway list, which is used to draw the roads on the screen
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
		
		NodeList tollnodes = ozTollXML.getTollNodes(tollway);
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
				try {
					TollRate tollRate = new TollRate();
					tollRate.vehicleType=tollType[trc];
					tollRate.rate=xmldata.getNodeData(exitNodes.item(tec), tollType[trc]);
					newTollPointExit.addRate(tollRate);
				} catch (NullPointerException e){
					// do nothing if it doesn't exist
				}
			}
			newTollPoint.addExit(newTollPointExit);
		}
		return newTollPoint;
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
}
