/**
 * 
 */
package com.mimpidev.oztoll;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author bugman
 *
 */
public class XMLPullParserHandler {
	
	private ArrayList<OzTollCity> cityList;
	private OzTollCity city;
	private Street street;
	private Tollway tollway;
	private TollPoint tollPoint;
	private TollPointExit tollPointExit;
	private String name,
				   timestamp,
				   longitude, latitude, 
				   pathBreadcrumbs; /* pathBreadcrumbs is used to track where the program is in the
				   					 * xml file, for populating the arrays.
				    				 */
	
	public XMLPullParserHandler(){
		cityList = new ArrayList<OzTollCity>();
		pathBreadcrumbs="";
	}
	
	public ArrayList<OzTollCity> getCities(){
		return cityList;
	}
	
	public ArrayList<OzTollCity> parse(InputStream is){
		XmlPullParserFactory factory = null;
		XmlPullParser parser = null;
		
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			
			while (eventType != XmlPullParser.END_DOCUMENT){
				String tagname = parser.getName();
				switch (eventType) {
					case XmlPullParser.START_TAG:
						if (tagname.equalsIgnoreCase("oztoll")){
							// Start of the xml document, pathBreadcrumbs will be the start
							pathBreadcrumbs = tagname;
						} else {
							// Add the current tag tag to the bread crumbs
							pathBreadcrumbs = pathBreadcrumbs.concat("->").concat(tagname);
						}
						if (tagname.equalsIgnoreCase("timestamp")){
							setTimestamp(parser.nextText());
						}
						if (tagname.equalsIgnoreCase("city")){
							city = new OzTollCity();
						}
						if (tagname.equalsIgnoreCase("name")){
							// If parent tag is "city" then set the city Name.
							if (isParentTag(pathBreadcrumbs,"city"))
								city.setCityName(parser.nextText());
							// else If parent tag is "street" then set the street name.
							else if (isParentTag(pathBreadcrumbs,"street"))
								name = parser.nextText();
						}
						if (tagname.equalsIgnoreCase("expiry")){
							city.setExpiryDate(parser.nextText());
						}
						if (tagname.equalsIgnoreCase("longitude")){
							longitude = parser.nextText();
						}
						if (tagname.equalsIgnoreCase("latitude")){
							latitude = parser.nextText();
						}
						if (tagname.equalsIgnoreCase("tollway")){
							tollway = new Tollway();
							tollway.setName(parser.getAttributeValue(null, "name"));
						}
						if (tagname.equalsIgnoreCase("exit")){
							if (isParentTag(pathBreadcrumbs,"tollpoint"))
								tollPointExit = new TollPointExit();
						}
						if (tagname.equalsIgnoreCase("street")){
							String[] breadCrumbs = pathBreadcrumbs.split("->");
							if (breadCrumbs[breadCrumbs.length-2].equalsIgnoreCase("tollpoint"))
								tollPointExit.addExit(tollway.getStreetByName(parser.nextText()));
						}
						if (tagname.equalsIgnoreCase("tollpoint")){
							tollPoint = new TollPoint();
						}
						if (tagname.equalsIgnoreCase("start")){
							tollPoint.addStart(tollway.getStreetByName(parser.nextText()));
						}
						if ((tagname.equalsIgnoreCase("car"))&&
							(tagname.equalsIgnoreCase("lcv"))&&
							(tagname.equalsIgnoreCase("hcv"))&&
							(tagname.equalsIgnoreCase("cv-day"))&&
							(tagname.equalsIgnoreCase("cv-night"))&&
							(tagname.equalsIgnoreCase("hcv-day"))&&
							(tagname.equalsIgnoreCase("hcv-night"))&&
							(tagname.equalsIgnoreCase("lcv-day"))&&
							(tagname.equalsIgnoreCase("lcv-night"))&&
							(tagname.equalsIgnoreCase("car-we"))&&
							(tagname.equalsIgnoreCase("mc"))){
								TollRate newTollRate = new TollRate();
								newTollRate.vehicleType=tagname;
								newTollRate.rate=parser.nextText();
								tollPointExit.addRate(newTollRate);
						}
						break;
					case XmlPullParser.END_TAG:
						if (tagname.equalsIgnoreCase("city")){
							cityList.add(city);
						}
						if (tagname.equalsIgnoreCase("origin")){
							city.setOrigin(new GeoPoint(Integer.parseInt(latitude),Integer.parseInt(longitude)));
						}
						if (tagname.equalsIgnoreCase("street")){
							String[] breadCrumbs = pathBreadcrumbs.split("->");
							if (breadCrumbs[breadCrumbs.length-2].equalsIgnoreCase("tollpoint")){
								street = new Street(name, new GeoPoint(Double.parseDouble(latitude),Double.parseDouble(longitude)),tollway.getStreets().size()+1);
								tollway.addStreet(street);
							}
						}
						if (tagname.equalsIgnoreCase("exit")){
							if (isParentTag(pathBreadcrumbs,"tollpoint")){
								tollPoint.addExit(tollPointExit);
							}
						}
						if (tagname.equalsIgnoreCase("tollpoint")){
							tollway.addToll(tollPoint);
						}
						
						if (tagname.equalsIgnoreCase("tollway")){
							city.addTollways(tollway);
						}
						// work here. as this is end tag, remove the tag (that ended) from the pathBreadcrumbs
						pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						break;
				}
				eventType = parser.next();
			}
		} catch (XmlPullParserException e) {
		} catch (IOException e) {
		}
		
		return cityList;
	}
	
	public boolean isParentTag(String path, String parentTag){
		if (path.substring((path.length()-parentTag.length()),path.length()).equalsIgnoreCase(parentTag))
			return true;
		else
			return false;
	}

	/**
	 * @return the timestamp
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
}
