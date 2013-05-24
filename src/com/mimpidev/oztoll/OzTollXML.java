/**
 * 
 */
package com.mimpidev.oztoll;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.res.AssetManager;
import android.util.Log;

/**
 * @author bugman
 *
 */
public class OzTollXML {
	private XmlReader xmldata;
	private NodeList cityNodes,
					 tollwayNodes,
					 currentStreetList,
					 currentTollPathways,
					 currentTollNodes;
	private Node currentTollwayNode,
				 currentCity;
	private int currentTollwayId=-1,
				currentCityId=-1;

	// Hard coding the toll types into the program.
	String tollType[] = {"car","car-we","lcv","lcv-day",
						 "lcv-night","hcv","hcv-day","hcv-night",
						 "cv-day","cv-night","mc"};
	
	public OzTollXML(){
	}
	
	public void setXMLReader(String filename, AssetManager assetMan){
		xmldata = new XmlReader(filename, assetMan);
	}

	public void setXMLReader(String filename) {
		xmldata = new XmlReader(filename);
	}
	
	public void setXMLReader(File xmlFile){
		xmldata = new XmlReader(xmlFile);
	}
	
	public NodeList getCityNodes(){
		if (cityNodes==null){
			cityNodes = xmldata.getElementsByTagName("city");
		}
		return cityNodes;
	}
	
	public int getCityCount(){
		getCityNodes();
		
		return cityNodes.getLength();
	}
	
	/**
	 * This will read the city name from the xml file.
	 * @return city name stored in xml file.
	 */
	public String getCityName(int city){
		getCityNodes();
		
		if (cityNodes.getLength()>0){
			if (currentCityId!=city){
				currentCity = cityNodes.item(city);
				currentCityId=city;
			}
			if (currentCity.getNodeType() == Node.ELEMENT_NODE) {
				return xmldata.getNodeData(currentCity,"name");
			}
		}
		return null;
	}
	
	public String getExpiry(int city){
		getCityNodes();
		
		if (cityNodes.getLength()>0){
			if (currentCityId!=city){
				currentCity = cityNodes.item(city);
				currentCityId=city;
			}
			if (currentCity.getNodeType() == Node.ELEMENT_NODE)
				return xmldata.getNodeData(currentCity, "expiry");
		}

		return null;
	}

	public String getTimeStamp() {
		NodeList nodes = xmldata.getElementsByTagName("oztoll");
		if (nodes!=null)
			for (int s=0; s<nodes.getLength(); s++){
				Node currentNode = nodes.item(s);
				if (currentNode.getNodeType() == Node.ELEMENT_NODE)
					return xmldata.getNodeData(currentNode, "timestamp");
			}
		return null;
	}
	
	public GeoPoint getOrigin(int city){
		cityNodes=getCityNodes();
		
		if ((cityNodes.getLength()>0)&&(city<cityNodes.getLength())){
			if (currentCityId!=city){
				currentCity = cityNodes.item(city);
				currentCityId=city;
			}
			NodeList originNodes = xmldata.getChildNodesByTagName(currentCity, "origin");
			if (originNodes.getLength()==1){
				Node subNode = originNodes.item(0);
				GeoPoint origin = new GeoPoint(Integer.parseInt(xmldata.getNodeData(subNode, "latitude")),
											   Integer.parseInt(xmldata.getNodeData(subNode, "longitude")));
				return origin;
			}
		}
		return null;
	}
	
	public NodeList getTollwayNodes(int city){
		if ((cityNodes.getLength()>0)&&(city<cityNodes.getLength())){
			if (currentCityId!=city){
				currentCity = cityNodes.item(city);
				currentCityId=city;
			}
			tollwayNodes = xmldata.getChildNodesByTagName(currentCity, "tollway");
			return tollwayNodes;
		}
		
		return null;
	}
	
	/**
	 * This is used to count the toll ways in a city
	 * @return
	 */
	public int getTollwayCount(int city){
		getTollwayNodes(city);

		return xmldata.getNodeListCount(tollwayNodes);
	}
	
	public Node getTollwayNode(int city, int tollway){
		getTollwayNodes(city);
		
		if (tollwayNodes!=null){
			if ((tollwayNodes.getLength()>0)&&(tollway<getTollwayCount(city))){
				if (currentTollwayId!=tollway){
					currentTollwayNode = tollwayNodes.item(tollway);
					currentTollwayId=tollway;
				}
				return currentTollwayNode;
			}
		}
		
		return null;
	}
	
	/**
	 * This grabs the name of the requested tollway
	 * @param tollway
	 * @return
	 */
	public String getTollwayName(int cityCount, int tollway){
		getTollwayNode(cityCount, tollway);
		
		if(currentTollwayNode!=null){
			if (currentTollwayNode.getNodeType() == Node.ELEMENT_NODE){
				Element currentTollway = (Element)currentTollwayNode;
				return currentTollway.getAttribute("name");
			}
		}
		return null;
	}
	
	public NodeList getStreetNodes(int cityCount, int tollway){
		getTollwayNode(cityCount, tollway);
		
		if (currentTollwayNode!=null){
			String[] nodes = {"exit","street"};
			int[] index = {0};
			currentStreetList = xmldata.getNodesList(nodes,index,currentTollwayNode);
		}
		return currentStreetList;
	}
	
	/**
	 * This will return the number of Exits for the selected tollway
	 * @param tollway
	 * @return
	 */
	public int getStreetCount(int cityCount, int tollway){
		getStreetNodes(cityCount, tollway);
		
		return xmldata.getNodeListCount(currentStreetList);
	}

	/**
	 * Combined 3 methods into 1, as the only thing different was the string that is now passed 
	 * by value param
	 * @param tollway
	 * @param exitcount
	 * @param value
	 * @return
	 */
	public String getStreetDetail(int city, int tollway, int exitcount, String value){
		getStreetNodes(city, tollway);
		
		if ((currentStreetList!=null)&&(exitcount<getStreetCount(city, tollway))){
			Node currentNode = currentStreetList.item(exitcount);
			return xmldata.getNodeData(currentNode,value);
		} else 
			return null;
	}
	
	public NodeList getTollNodes(int city, int tollway){
		getTollwayNode(city, tollway);
		String[] nodes = {"tollpoint"};
		int[] index = null;
		currentTollNodes = xmldata.getNodesList(nodes,index, currentTollwayNode);

		return currentTollNodes;
	}

	public int getTollCount(int city, int tollway){
		getTollNodes(city, tollway);

		return xmldata.getNodeListCount(currentTollNodes);
	}
	
	/**
	 * This function is used to create a TollPoint object, with the start, and various
	 * exits found in the XML file, with the cost for the paths.
	 * @param newTollway 
	 * @param vector 
	 * @return
	 */
	public TollPoint getTollPointRate(int city, int tollway, int tollpoint, Tollway tollwayData){
		TollPoint newTollPoint = new TollPoint();
		
		NodeList tollnodes = getTollNodes(city, tollway);
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
				TollRate tollRate = new TollRate();
				tollRate.rate=xmldata.getNodeData(exitNodes.item(tec), tollType[trc]);
				if (tollRate.rate!=null){
					tollRate.vehicleType=tollType[trc];
					newTollPointExit.addRate(tollRate);
				}
			}
			newTollPoint.addExit(newTollPointExit);
		}
		return newTollPoint;
	}
}
