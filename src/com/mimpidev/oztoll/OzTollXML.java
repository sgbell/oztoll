/**
 * 
 */
package com.mimpidev.oztoll;

import java.io.File;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.res.AssetManager;

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
	private Node currentTollwayNode;
	private int currentTollwayId=-1;

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
	
	public int getCityCount(){
		if (cityNodes==null){
			cityNodes = xmldata.getElementsByTagName("city");
		}
		
		return cityNodes.getLength();
	}
	
	/**
	 * This will read the city name from the xml file.
	 * @return city name stored in xml file.
	 */
	public String getCityName(int city){
		
		Node currentNode = cityNodes.item(city);
		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
			return xmldata.getNodeData(currentNode,"name");
		}
		
		return null;
	}
	
	public String getExpiry(){
		if (cityNodes==null)
			cityNodes = xmldata.getElementsByTagName("city");
		if (cityNodes.getLength()>0){
			Node currentNode = cityNodes.item(0);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE)
				return xmldata.getNodeData(currentNode, "expiry");
		}

		return null;
	}

	public String getTimeStamp() {
		NodeList nodes = xmldata.getElementsByTagName("oztoll");
		for (int s=0; s< nodes.getLength(); s++){
			Node currentNode = nodes.item(s);
			if (currentNode.getNodeType() == Node.ELEMENT_NODE){
				return xmldata.getNodeData(currentNode, "timestamp");
			}
		}
		return null;
	}
	
	public GeoPoint getOrigin(){
		NodeList originNodes = xmldata.getElementsByTagName("origin");
		Node currentNode = originNodes.item(0);
		GeoPoint origin = new GeoPoint(Integer.parseInt(xmldata.getNodeData(currentNode, "latitude")),
									   Integer.parseInt(xmldata.getNodeData(currentNode, "longitude")));
		return origin;
	}
	
	/**
	 * This is used to count the tollways in the xml file.
	 * @return
	 */
	public int getTollwayCount(){
		if (tollwayNodes==null)
			getTollwayNodes();

		return tollwayNodes.getLength();
	}
	
	public NodeList getTollwayNodes(){
		if (tollwayNodes==null)
			tollwayNodes=xmldata.getElementsByTagName("tollway");
		
		return tollwayNodes;
	}
	
	public Node getTollwayNode(int tollway){
		if ((currentTollwayId!=tollway)&&(tollway<getTollwayCount())){
			currentTollwayId=tollway;
			currentTollwayNode = tollwayNodes.item(tollway);
		}
		
		return currentTollwayNode;
	}
	
	/**
	 * This will return the number of Exits for the selected tollway
	 * @param tollway
	 * @return
	 */
	public int getStreetCount(int tollway){
		currentStreetList=getStreetNodes(tollway);
		return getNodeListCount(currentStreetList);
	}

	/**
	 * Counts the number of tollways in the xml file
	 * @return
	 */
	public int getNodeListCount(String node){
		NodeList nodeList = xmldata.getElementsByTagName(node);
		return nodeList.getLength();
	}
	
	public int getNodeListCount(NodeList nodes){
		if (nodes!=null)
			return nodes.getLength();
		else
			return -1;
	}
	
	public int getTollCount(int tollway){
		getTollNodes(tollway);
		return getNodeListCount(currentTollNodes);
	}
	
	public int getTollPathwayCount(int tollway){
		getTollPathway(tollway);
		return getNodeListCount(currentTollPathways);
	}
	
	/**
	 * This grabs the name of the requested tollway
	 * @param tollway
	 * @return
	 */
	public String getTollwayName(int tollway){
		Node currentNode = cityNodes.item(0);
		if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
			return xmldata.getNodeAttribute(currentNode,"tollway","name",tollway);
		}
		return null;
	}
	
	public NodeList getStreetNodes(int tollway){
		if (currentTollwayId!=tollway){
			getTollwayNode(tollway);
			String[] nodes = {"exit","street"};
			int[] index = {0};
			currentStreetList = xmldata.getNodesList(nodes,index,currentTollwayNode);			
		}
		
		return currentStreetList;
	}
	
	/**
	 * Combined 3 methods into 1, as the only thing different was the string that is now passed 
	 * by value param
	 * @param tollway
	 * @param exitcount
	 * @param value
	 * @return
	 */
	public String getStreetDetail(int tollway, int exitcount, String value){
		getStreetNodes(tollway);
		if ((currentStreetList!=null)&&(exitcount<getStreetCount(tollway))){
			Node currentNode = currentStreetList.item(exitcount);
			return xmldata.getNodeData(currentNode,value);
		} else 
			return null;
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
	
	public NodeList getTollPathway(int tollway){
		if ((currentTollwayId!=tollway)||(currentTollwayId<getTollwayCount())){
			getTollwayNode(currentTollwayId);
			String[] nodes = {"pathway","path"};
			int[] index = {0};
			currentTollPathways=xmldata.getNodesList(nodes, index, currentTollwayNode);			
		}
		
		return currentTollPathways;
	}
	
	public NodeList getTollNodes(int tollway){
		getTollwayNode(tollway);
		String[] nodes = {"tollpoint"};
		int[] index = null;
		currentTollNodes = xmldata.getNodesList(nodes,index, currentTollwayNode);

		return currentTollNodes;
	}

	public NodeList getConnections(){
		return xmldata.getNodesList("connection");
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
