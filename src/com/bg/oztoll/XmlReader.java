/**
 * 
 */
package com.bg.oztoll;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author bugman
 *
 */
public class XmlReader {
	private Document doc;

	public XmlReader(String filename){
		File file = new File(filename);
		if (file.length()>1024){
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(file);
				doc.getDocumentElement().normalize();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public NodeList getElementsByTagName(String node){
		return doc.getElementsByTagName(node);
	}
	
	/**
	 * Attempting to create a method that will return a NodeList dependant on an array
	 * of nodes for a tree.
	 * @param nodeTree - Strings of the node path to take 
	 * @param index - values to select along the way
	 * @return
	 */
	public NodeList getNodesList(String[] nodeTree, int[] index){
		NodeList nodeList = doc.getElementsByTagName(nodeTree[0]);
		Node currentNode = nodeList.item(index[0]);
		for (int ntc=1; ntc < nodeTree.length; ntc++){
			if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
				Element fstElmnt = (Element) currentNode;
				NodeList workingNodeList = fstElmnt.getElementsByTagName(nodeTree[ntc]);
				if (ntc==(nodeTree.length-1)){
					return workingNodeList;
				} else {
					currentNode = workingNodeList.item(index[ntc]);
				}
			}
		}
		return null;
	}
	
	/**
	 * This is used to pull the string data out of the xml file, if the tag does not exist
	 * null value is returned for the string.  
	 * @param currentNode passes in the current Node in the XML document.
	 * @param tagName passes in the tag we are looking for in the current Node.
	 * @return A string value. If the tag does not exist in the current Node, null will be
	 *   returned.
	 */
	public String getNodeData(Node currentNode, String tagName){
		String nodeValue;
		Element fstElmnt = (Element) currentNode;
		NodeList titleElmntLst = fstElmnt.getElementsByTagName(tagName);
		Element titleElmnt = (Element) titleElmntLst.item(0);
		if (titleElmnt != null) {
			NodeList menuName = titleElmnt.getChildNodes();
			try {
				nodeValue = ((Node) menuName.item(0)).getNodeValue();
			} catch (NullPointerException e){
				nodeValue=null;
			}
		} else
			nodeValue = null;
		return nodeValue;
	}
	
	/** getNodeAttributes is used to grab the data from the xml file. While supplying the
	 * tag we want information from, and which attribute, it will then return a string
	 * containing the value of the attribute.
	 * @param currentNode 
	 * @param tagName
	 * @param attribute
	 * @param index
	 * @return The value of the attribute
	 */
	public String getNodeAttribute(Node currentNode, String tagName, String attribute, int index) {
		Element fstElement = (Element) currentNode;
		NodeList elementList = fstElement.getElementsByTagName(tagName);
		Element elementItem = (Element) elementList.item(index);
		if (elementItem != null){
			return elementItem.getAttribute(attribute);
		} else {
			return null;
		}
	}
}
