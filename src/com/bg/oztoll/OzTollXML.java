/**
 * 
 */
package com.bg.oztoll;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author bugman
 *
 */
public class OzTollXML {
	private XmlReader xmldata;
	private NodeList currentStreetList;
	private int currentTollwayForStreetList=-1;

	// Hard coding the toll types into the program.
	String tollType[] = {"car","car-we","lcv","lcv-day",
						 "lcv-night","hcv","hcv-day","hcv-night",
						 "cv-day","cv-night","mc"};
	
	public OzTollXML(){
	}
	
	public void setXMLReader(String filename){
		xmldata = new XmlReader(filename);	
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
		NodeList tollList = getTollNodes(tollway);
		return getNodeListCount(tollList);
	}
	
	public int getTollPathwayCount(int tollway){
		NodeList path = getTollPathway(tollway);
		return getNodeListCount(path);
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
	
	public NodeList getStreetNodes(int tollway){
		if (currentTollwayForStreetList!=tollway){
			currentTollwayForStreetList=tollway;
			String[] nodes = {"tollway","exit","street"};
			int[] index = {tollway,0};
			return xmldata.getNodesList(nodes,index);
		} else {
			return currentStreetList;
		}
	}
	
	/**
	 * This will return the number of Exits for the selected tollway
	 * @param tollway
	 * @return
	 */
	public int countStreets(int tollway){
		currentStreetList=getStreetNodes(tollway);
		return getNodeListCount(currentStreetList);
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
		currentStreetList = getStreetNodes(tollway);
		if ((currentStreetList!=null)&&(exitcount<countStreets(tollway))){
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
		String[] nodes = {"tollway","pathway","path"};
		int[] index = {tollway,0};
		return xmldata.getNodesList(nodes, index);
	}
	
	public NodeList getTollNodes(int tollway){
		String[] nodes = {"tollway","tollpoint"};
		int[] index = {tollway};
		return xmldata.getNodesList(nodes,index);
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
				try {
					TollRate tollRate = new TollRate();
					tollRate.vehicleType=tollType[trc];
					tollRate.rate=xmldata.getNodeData(exitNodes.item(tec), tollType[trc]);
					newTollPointExit.addRate(tollRate);
				} catch (NullPointerException e){
					// do nothing if the tollType does not exist
				}
			}
			newTollPoint.addExit(newTollPointExit);
		}
		return newTollPoint;
	}

}
