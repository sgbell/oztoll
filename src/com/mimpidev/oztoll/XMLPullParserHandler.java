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
	private Object dataSync;
	
	public XMLPullParserHandler(){
		cityList = new ArrayList<OzTollCity>();
		pathBreadcrumbs="";
	}
	
	public ArrayList<OzTollCity> getCities(){
		return cityList;
	}
	
	public ArrayList<OzTollCity> parse(InputStream is){
		cityList = new ArrayList<OzTollCity>();
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
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						} else if (tagname.equalsIgnoreCase("city")){
							city = new OzTollCity();
						} else if (tagname.equalsIgnoreCase("name")){
							// If parent tag is "city" then set the city Name.
							if (isParentTag(pathBreadcrumbs,"city"))
								city.setCityName(parser.nextText());
							// else If parent tag is "street" then set the street name.
							else if (isParentTag(pathBreadcrumbs,"street"))
								name = parser.nextText();
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						} else if (tagname.equalsIgnoreCase("expiry")){
							city.setExpiryDate(parser.nextText());
							// added the following line, because if it's an all in 1 line, it misses the end tag.
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						} else if (tagname.equalsIgnoreCase("longitude")){
							longitude = parser.nextText();
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						} else if (tagname.equalsIgnoreCase("latitude")){
							latitude = parser.nextText();
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						} else if (tagname.equalsIgnoreCase("tollway")){
							tollway = new Tollway();
							tollway.setName(parser.getAttributeValue(null, "name"));
						} else if (tagname.equalsIgnoreCase("exit")){
							if (isParentTag(pathBreadcrumbs,"tollpoint"))
								tollPointExit = new TollPointExit();
						} else if (tagname.equalsIgnoreCase("street")){
							String[] breadCrumbs = pathBreadcrumbs.split("->");
							if (breadCrumbs[breadCrumbs.length-3].equalsIgnoreCase("tollpoint")){
								tollPointExit.addExit(tollway.getStreetByName(parser.nextText()));
								pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
							}
						} else if (tagname.equalsIgnoreCase("tollpoint")){
							tollPoint = new TollPoint();
						} else if (tagname.equalsIgnoreCase("start")){
							tollPoint.addStart(tollway.getStreetByName(parser.nextText()));
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						} else if ((tagname.equalsIgnoreCase("car"))||
							(tagname.equalsIgnoreCase("lcv"))||
							(tagname.equalsIgnoreCase("hcv"))||
							(tagname.equalsIgnoreCase("cv-day"))||
							(tagname.equalsIgnoreCase("cv-night"))||
							(tagname.equalsIgnoreCase("hcv-day"))||
							(tagname.equalsIgnoreCase("hcv-night"))||
							(tagname.equalsIgnoreCase("lcv-day"))||
							(tagname.equalsIgnoreCase("lcv-night"))||
							(tagname.equalsIgnoreCase("car-we"))||
							(tagname.equalsIgnoreCase("mc"))){
								TollRate newTollRate = new TollRate();
								newTollRate.vehicleType=tagname;
								newTollRate.rate=parser.nextText();
								tollPointExit.addRate(newTollRate);
								pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						}
						break;
					case XmlPullParser.END_TAG:
						if (tagname.equalsIgnoreCase("city")){
							cityList.add(city);
							try {
								synchronized (dataSync){
									dataSync.notify();
								}
							} catch (NullPointerException e){
								// Ignore Null pointer that occurs when the program is exiting
							}
						}
						if (tagname.equalsIgnoreCase("origin")){
							city.setOrigin(new GeoPoint(Integer.parseInt(latitude),Integer.parseInt(longitude)));
						}
						if (tagname.equalsIgnoreCase("street")){
							String[] breadCrumbs = pathBreadcrumbs.split("->");
							if (!breadCrumbs[breadCrumbs.length-3].equalsIgnoreCase("tollpoint")){
								GeoPoint location =new GeoPoint(Double.parseDouble(latitude),Double.parseDouble(longitude));
								street = new Street(name, location, tollway.getStreets().size()+1);
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
						if (!pathBreadcrumbs.equalsIgnoreCase("oztoll"))
							pathBreadcrumbs = pathBreadcrumbs.substring(0, (pathBreadcrumbs.length()-("->"+tagname).length()));
						break;
				}
				eventType = parser.next();
			}
		} catch (IOException e) {
		} catch (XmlPullParserException e) {
		}
		
		return cityList;
	}
	
	public boolean isParentTag(String path, String parentTag){
		String[] splitPath = path.split("->");
		if (splitPath[splitPath.length-2].equalsIgnoreCase(parentTag))
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

	public void setDataSync(Object dataSync) {
		this.dataSync=dataSync;
	}
}