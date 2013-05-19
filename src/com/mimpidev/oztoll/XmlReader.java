/** XmlReader is a class I had previously developed for podsalinan. It handles everything from
 * opening the file to reading the information from the xml file
 * 
 */
package com.mimpidev.oztoll;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.res.AssetManager;

/**
 * @author bugman
 *
 */
public class XmlReader {
	private Document doc;

	public XmlReader(File xmlFile){
		openFile(xmlFile);
	}
	
	// Opens an xml file and prepares it for reading.
	public XmlReader(String filename){
		File file = new File(filename);
		openFile(file);
	}
	
	public void openFile (File xmlFile){
		if (xmlFile.length()>1024){
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(xmlFile);
				doc.getDocumentElement().normalize();
			} catch (ParserConfigurationException e) {
					// exception
			} catch (SAXException e) {
					// exception
			} catch (IOException e) {
					// exception
			}			
		}
	}
	
	public XmlReader(String filename, AssetManager assetMan) {
		InputStream input;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			input = assetMan.open(filename);
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(input);
			doc.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			// exception
		} catch (IOException e) {
			// exception
		} catch (SAXException e) {
			// exception
		}
	}



	/** Protection from bad coding. If the xml file has not been opened this will return null
	 * @param node - String value you want to retrieve the node list of
	 * @return a node list.
	 */
	public NodeList getElementsByTagName(String node){
		if (doc!=null)
			return doc.getElementsByTagName(node);
		else
			return null;
	}
	
	/** Will have to check my code, as this is identical to getElementsByTagName, without the error
	 * checking :(
	 * @param nodeTree
	 * @return
	 */
	public NodeList getNodesList(String nodeTree){
		return doc.getElementsByTagName(nodeTree);
	}
	
	/**
	 * Attempting to create a method that will return a NodeList dependant on an array
	 * of nodes for a tree.
	 * @param nodeTree - Strings of the node path to take 
	 * @param index - values to select along the way
	 * @return
	 */
	public NodeList getNodesList(String[] nodeTree, int[] index, Node startNode){
		Node currentNode = startNode;
		for (int ntc=0; ntc < nodeTree.length; ntc++){
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
}
