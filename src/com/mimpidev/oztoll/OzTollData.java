/** This class stores all of the information extracted from the xml file, for use in our program.
 * Established early on that i couldn't read from the xml in real time as the program would not draw
 * the screen quick enough.
 */
package com.mimpidev.oztoll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * @author bugman
 *
 */
public class OzTollData implements Runnable{
		
	private ArrayList<OzTollCity> cities;
	private String timestamp;
	private XMLPullParserHandler xmlReader;
	public  String connectionsTest;
	private boolean finishedRead=false;
	private Object syncObject, dataSync;
	private SharedPreferences sharedPreferences;
	private LatLng start, finish;
	private InputStream dataFile;
	private String oldCityName = "";         // This will be used to keep a check on when the cityName changes
	
	/** Initializes the XML Handler.
	 */
	public OzTollData(){
		xmlReader = new XMLPullParserHandler();
	}
	
	/** This constructor initializes the vectors, and loads the xml file and calls
	 *  getTollwayData to populate the data
	 * @param filename
	 * @param assetMan 
	 */
	public OzTollData(String filename, AssetManager assetMan){
		this();
		try {
			dataFile = assetMan.open(filename);
		} catch (IOException e) {
		}
	}
	
	public OzTollData(String filename){
		this();
		try {
			dataFile = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
		}
	}
	
	public OzTollData(File fileLink){
		this();
		try {
			dataFile = new FileInputStream(fileLink);
		} catch (FileNotFoundException e) {
		}
	}
	
	public OzTollData(File openFile, SharedPreferences preferences) {
		this(openFile);
		setPreferences(preferences);
	}

	public void setDataFile(String filename, AssetManager assetMan, SharedPreferences preferences){
		setPreferences(preferences);
		finishedRead=false;
		try {
			dataFile = assetMan.open(filename);
		} catch (IOException e) {
		}
	}
	
	public void setDataFile(String filename){
		finishedRead=false;
		try {
			dataFile = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
		}
	}
	
	public void setSyncObject(Object syncMe){
		syncObject = syncMe;
	}
	
	public Object getSyncObject(){
		return syncObject;
	}
	
	public void setDataSync(Object syncMe){
		dataSync = syncMe;
	}
	
	public Object getDataSync(){
		return dataSync;
	}
	
	public void run(){
		readFile();
		
		try {
			synchronized (dataSync){
				dataSync.notify();
			}
			cities = xmlReader.getCities();
		} catch (NullPointerException e){
			// Ignore Null pointer that occurs when the program is exiting
		}
	}
	
	/**
	 * readFile - A method to populate arrays with the information stored in the xml file opened
	 * using ozTollXML. The reason all of the information will be stored in memory, is because accessing
	 * the xml file has proved to be time consuming when the program needs to access the xml file to
	 * redraw the screen.
	 */
	public void readFile(){
		xmlReader.setDataSync(dataSync);
		cities = xmlReader.parse(dataFile);
		timestamp = xmlReader.getTimestamp();
		setValidStarts();
		
		finishedRead=true;
	}
	
	public ArrayList<OzTollCity> getCities(){
		return cities;
	}

	public OzTollCity getCityByName(String cityName){
		
		for (OzTollCity currentCity : cities)
			if (currentCity.getCityName().equalsIgnoreCase(cityName))
				return currentCity;
		return null;
	}
	
	public OzTollCity getSelectedCity(){
		String city;
		try {
			city = sharedPreferences.getString("selectedCity", "Melbourne");
		} catch (NullPointerException e){
			city = "Melbourne";
		}
		if (!city.contentEquals(oldCityName)){
			oldCityName=city;
			reset();
		}		
		
		return getCityByName(city);
	}
	
	public OzTollCity getCityById(int cityId){
		return cities.get(cityId);
	}
	
	public ArrayList<Street> getValidStreetsArray(String city) {
		ArrayList<Street> streetList = new ArrayList<Street>();

		if (start!=null){
			setStreetsToInvalid();
			markRoads(start);
		}
		
		OzTollCity currentCity = getCityByName(city);
				for (int twc=0; twc < currentCity.getTollwayCount(); twc++)
					for (int tsc=0; tsc < currentCity.getStreetCount(twc); tsc++){
						Street newStreet = currentCity.getStreet(twc, tsc);
						if ((newStreet.isValid())||
							(newStreet.equals(start))||
							(newStreet.equals(finish))){
							streetList.add(newStreet);
						}
					}
		
		return streetList;
	}

	public ArrayList<String[]> getValidStreetsAsStrings(String city){
		ArrayList<String[]> streetList = new ArrayList<String[]>();
		
		OzTollCity selectedCity = getSelectedCity();
		for (int twc=0; twc<selectedCity.getTollwayCount(); twc++)
			for (int sc=0; sc<selectedCity.getStreetCount(twc); sc++)
				if (selectedCity.getStreet(twc, sc).isValid()){
					String[] newString = new String[2];
					newString[0]=selectedCity.getTollwayName(twc);
					newString[1]=selectedCity.getStreetName(twc, sc);
					streetList.add(newString);
				}
		
		return streetList;
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

	
	public TollCharges getTollRate(LatLng start, LatLng end, Tollway tollway){
		TollCharges charges= new TollCharges();
		for (int tc=0; tc<tollway.getTollPoints().size(); tc++){
			TollPoint currentTollPoint = tollway.getTollPoints().get(tc); 
			if (currentTollPoint.isStart(findStreetByLatLng(start).getName())){
				Log.w("Toll","Start Found: "+findStreetByLatLng(start).getName());
				for (int tpe=0; tpe < currentTollPoint.getExit().size(); tpe++){
					TollPointExit currentExit = currentTollPoint.getExit().get(tpe);
					if (currentExit.isExit(findStreetByLatLng(end).getName())){
						Log.w("Toll", "End found: "+findStreetByLatLng(end).getName());
						charges.tollway=tollway.getName();
						charges.tolls=currentExit.getRates();
					}
				}
			}
		}
		return charges;
	}
	
	/**
	 * This function calculates the Toll Rates and returns it to be displayed
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<TollCharges> getFullRate(LatLng start, LatLng end){
		Tollway startTollway=null, endTollway=null;
		ArrayList<TollCharges> charges = new ArrayList<TollCharges>();
		
		OzTollCity currentCity = getSelectedCity();
		ArrayList<Tollway> tollways = currentCity.getTollways();
		for (int twc=0; twc < tollways.size(); twc++){
			for (int sc=0; sc < tollways.get(twc).getStreets().size(); sc++){
				if (tollways.get(twc).getStreets().get(sc).equals(start)){
					startTollway = tollways.get(twc);
				}
				if (tollways.get(twc).getStreets().get(sc).equals(end)){
					endTollway = tollways.get(twc);
				}
			}
		}
		Log.w("Toll","Tollway: "+startTollway.getName());
		Log.w("Toll","Start: "+findStreetByLatLng(start).getName());
		Log.w("Toll","Finish Tollway: "+endTollway.getName());
		Log.w("Toll","Finish: "+findStreetByLatLng(end).getName());
		
		// Single Tollway
		if ((startTollway!=null)&&(endTollway!=null)&&(startTollway.equals(endTollway))){
			Log.w("Toll","Entering getTollRate");
			charges.add(getTollRate(start, end, startTollway));
		}
		
		return charges;
	}

	public String getSelectedExpiryDate(){
		OzTollCity city = getSelectedCity();

        if (city!=null)
        	return city.getExpiryDate();
        else
        	return null;
	}
	
	public void reset(){
		setFinish(null);
		setStart(null);
	}
	
	public void setValidStarts(){
		OzTollCity currentCity = getSelectedCity();
		for (Tollway currentTollway: currentCity.getTollways())
			for (TollPoint currentTollpoint: currentTollway.getTollPoints())
				currentTollpoint.setStartValid();
	}
	
	public boolean isFinished(){
		return finishedRead;
	}

	public void setPreferences(SharedPreferences sP) {
		sharedPreferences = sP;
	}
	
	public SharedPreferences getPreferences(){
		return sharedPreferences;
	}
	
	/** This is used to test if the Toll rate is found in the processToll method
	 * @param tollType
	 * @param selectedVehicle
	 * @return
	 */
	public boolean isTollRateFound(String tollType, String selectedVehicle){
		if ((tollType.contains(selectedVehicle))||
			((selectedVehicle.equalsIgnoreCase("lcv"))&&
			 (tollType.contains("cv")&&
			 (!tollType.contains("hcv"))))||
			((selectedVehicle.equalsIgnoreCase("hcv"))&&
			 (tollType.contains("cv")&&
			 (!tollType.contains("lcv"))))||
			((selectedVehicle.equalsIgnoreCase("motorcycle"))&&
			 (tollType.equalsIgnoreCase("mc")))){
			return true;
		} else {
			return false;
		}
	}
	
	/** Originally processToll was written with the start & finish being passed in, but as the program has
	 * evolved, the start & finish have been stored in this object.
	 * @param appContext
	 * @return
	 */
	public LinearLayout processToll(Context appContext){
		if ((findStreetByLatLng(getStart())!=null)&&(findStreetByLatLng(getFinish())!=null)){
			OzTollCity city = getSelectedCity();
			if (city!=null){
				return processToll(getStart(),
						           getFinish(),appContext);
			}
		}
		return null;
	}
	
	public LinearLayout processToll(LatLng start, LatLng finish, Context appContext){
		String title="";
		
		String selectedVehicle;
		try {
			selectedVehicle = getPreferences().getString("vehicleType", "car");
		} catch (NullPointerException e){
			selectedVehicle = "car";
		}
		ArrayList<TollCharges> tolls = getFullRate(start, finish);
		Log.w("Toll rate","Tolls Found"+tolls.size());
		
		ArrayList<TollRate> totalCharges = new ArrayList<TollRate>();
		TextView tollTitle;
		LinearLayout.LayoutParams fillParentParams, wrapContentParams;

		if ((start!=null)&&(finish!=null)){
			/* Need to create a linearLayout, and put all the stuff in it for
			 * ozView to then read to show the user.
			 */
			LinearLayout rateLayout = new LinearLayout(appContext);
			rateLayout.setOrientation(LinearLayout.VERTICAL);
			tollTitle = new TextView(appContext);

			// LinearLayout.LayoutParams to shorten the height of the textview
			fillParentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			fillParentParams.setMargins(0, 0, 0, 0);
			wrapContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			wrapContentParams.setMargins(0, 0, 0, 0);
			
			if (selectedVehicle.equalsIgnoreCase("car")){
				title="Car";
			} else if (selectedVehicle.equalsIgnoreCase("lcv")){
				title="Light Commercial Vehicle";
			} else if (selectedVehicle.equalsIgnoreCase("hcv")){
				title="Heavy Commercial Vehicle";
			} else if (selectedVehicle.equalsIgnoreCase("motorcycle")){
				title="Motorcycle";
			} else if (selectedVehicle.equalsIgnoreCase("all")){
				title="All";
			}
			
			tollTitle.setText(title);
			tollTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
			tollTitle.setPadding(10, 0, 0, 0);
			tollTitle.setLayoutParams(fillParentParams);
			rateLayout.addView(tollTitle);
			TextView tollTrip = new TextView(appContext);
			tollTrip.setText(findStreetByLatLng(start).getName()+" - "+findStreetByLatLng(finish).getName());
			Log.w("Toll rate",findStreetByLatLng(start).getName()+" - "+findStreetByLatLng(finish).getName());
			tollTrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			tollTrip.setPadding(10, 0, 0, 0);
			//tollTrip.setLayoutParams(fillParentParams);
			rateLayout.addView(tollTrip);
			
			/* The following for loop with traverse the toll result for the current trip.
			 */
			for (int tc=0; tc < tolls.size(); tc++){
				TollCharges currentToll = tolls.get(tc);
				
				/* The following toll loop will search the current toll Array for any tolls relating to the selected vehicle
				 * toll.
				 */
				int tollTypeCount=0;
				int trc=0;
				TextView tollwayName = new TextView(appContext);
				
				/* The following if statement block will select the correct tolls from the toll list
				 * to be shown to the user. 
				 */
				if (!selectedVehicle.equalsIgnoreCase("all")){
					for (trc=0; trc < currentToll.tolls.size(); trc++){
						if (isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle))
							tollTypeCount++;
					}
				} else {
					tollTypeCount = currentToll.tolls.size();
				}
				
				switch (tollTypeCount){
					case 0:
						// If the user has selected Motorcycle on CityLink it will return nothing,
						// better to say it's free than not display anything
						if (selectedVehicle.equalsIgnoreCase("motorcycle")){
							LinearLayout tollwayLayout = new LinearLayout(appContext);
							tollwayLayout.setOrientation(LinearLayout.HORIZONTAL);

							tollwayName.setText(getSelectedCity().getCityName());
							tollwayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
							tollwayName.setPadding(10, 0, 20, 0);

							tollwayLayout.addView(tollwayName);
							tollwayLayout.setLayoutParams(wrapContentParams);
							
							TextView tollwayCharge = new TextView(appContext);
							tollwayCharge.setText("-   No Charge");
							tollwayCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
							tollwayCharge.setLayoutParams(wrapContentParams);
							tollwayLayout.addView(tollwayCharge);
							rateLayout.addView(tollwayLayout);
						}
						break;
					case 1:
						// If it exists only once in the toll Array, it will put the charge on the one line with the
						// tollway Name.
						LinearLayout tollwayLayout = new LinearLayout(appContext);
						tollwayLayout.setOrientation(LinearLayout.HORIZONTAL);
						tollwayName.setText(currentToll.tollway);
						tollwayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
						tollwayName.setPadding(10, 0, 20, 0);

						tollwayLayout.addView(tollwayName);
						tollwayLayout.setLayoutParams(wrapContentParams);
						
						/* The following while loop will traverse through the currentToll list to find the only
						 * entry in the toll list relating to the selected vehicle, and will add it to the rateLayout.
						 * As there is only one, 
						 */
						trc=0;
						boolean found=false;
						
						while ((trc<currentToll.tolls.size())&&
								(!found)){
							if (isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle)){
								TextView tollwayCharge = new TextView(appContext);
								tollwayCharge.setText("$"+currentToll.tolls.get(trc).rate);
								Log.w("Toll rate","$"+currentToll.tolls.get(trc).rate);
								tollwayCharge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
								tollwayCharge.setLayoutParams(wrapContentParams);
								tollwayLayout.addView(tollwayCharge);
								rateLayout.addView(tollwayLayout);
								found=true;
							} else
								trc++;
						}
						/* If the path selected has more than 1 tollway we need to create the totalCharges array
						 */
						if (tolls.size()>1){
							
							// if there are no entries in totalCharges yet, add the first.
							if (totalCharges.size()<1){
								TollRate currentRate = new TollRate();
								if (isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle)){

									// Set the single tolls to a single toll rate name, to be distinguished down below when
									// the totals are merged
									if (selectedVehicle.equalsIgnoreCase("car"))
										currentRate.vehicleType="Car";
									else if (selectedVehicle.equalsIgnoreCase("lcv"))
										currentRate.vehicleType="light commercial vehicle";
									else if (selectedVehicle.equalsIgnoreCase("hcv"))
										currentRate.vehicleType="Heavy Commercial Vehicle";
									else if (selectedVehicle.equalsIgnoreCase("motorcycle"))
										currentRate.vehicleType="Motorcycle";
									
									currentRate.rate=currentToll.tolls.get(trc).rate;
									totalCharges.add(currentRate);
								}
							} else
								// if there is 1 or more entries in totalCharges
								if (totalCharges.size()==1){
									totalCharges.get(0).rate = Float.toString(
											Float.parseFloat(totalCharges.get(0).rate)+Float.parseFloat(currentToll.tolls.get(trc).rate));
								} else
									// If the is only 1 car toll, but more than one in totalCharges.
									for (int ttc=0; ttc < totalCharges.size(); ttc++){
										totalCharges.get(ttc).rate = Float.toString(
												Float.parseFloat(totalCharges.get(ttc).rate)+Float.parseFloat(currentToll.tolls.get(trc).rate));
									}
						}
						break;
					default:
						// Need to Sort out the headings when grabbing all results
						// More than one type of toll for this vehicle
						tollwayName.setText(currentToll.tollway);
						tollwayName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
						tollwayName.setPadding(10, 0, 0, 0);
						tollwayName.setLayoutParams(fillParentParams);
						rateLayout.addView(tollwayName);
						
						String variation ="";
						
						trc=0;
						int ttfound=0;
						while ((trc<currentToll.tolls.size())&&
							   (ttfound<tollTypeCount)){
							// Currently transforming code in the comments below to work for all vehicles
							// and not just cars
							if ((isTollRateFound(currentToll.tolls.get(trc).vehicleType, selectedVehicle))||
								(selectedVehicle.equalsIgnoreCase("all"))){
								boolean otherFound=false;
								if (!selectedVehicle.equalsIgnoreCase("all")){
									if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car"))
										variation = "Weekdays";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car-we"))
										variation = "Weekends";
									else if (currentToll.tolls.get(trc).vehicleType.contains("cv-day"))
										variation = "Day time";
									else if (currentToll.tolls.get(trc).vehicleType.contains("cv-night"))
										variation = "Night time";
								} else {
									if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car")){
										for (int check=0; check<currentToll.tolls.size(); check++)
											if (currentToll.tolls.get(check).vehicleType.equalsIgnoreCase("car-we"))
												otherFound=true;
										if (otherFound)
											variation="Weekdays";
										else
											variation="Car";
									} else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("car-we"))
										variation="Weekends";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv"))
										variation="Light Commercial Vehicle";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv"))
										variation="Heavy Commercial Vehicle";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv-day"))
										variation="Heavy Commercial Vehicle Daytime";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("hcv-night"))
										variation="Heavy Commercial Vehicle Nighttime";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("cv-day"))
										variation="Commercial Vehicle Daytime";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("cv-night"))
										variation="Commercial Vehicle Nighttime";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv-day"))
										variation="Light Commercial Vehicle Daytime";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("lcv-night"))
										variation="Light Commercial Vehicle Nighttime";
									else if (currentToll.tolls.get(trc).vehicleType.equalsIgnoreCase("mc"))
										variation="Motorcycle";
								}
								
								/* The following adds the content to the dialog Window */
								LinearLayout tollRateLayout = new LinearLayout(appContext);
								tollRateLayout.setOrientation(LinearLayout.HORIZONTAL);
								TextView rateTitle = new TextView(appContext);
								rateTitle.setText(variation);
								rateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
								rateTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
																						LinearLayout.LayoutParams.WRAP_CONTENT,
																						0.7f));
								rateTitle.setPadding(10, 0, 10, 0);
								tollRateLayout.addView(rateTitle);
								TextView rateValue = new TextView(appContext);
								rateValue.setText("$"+currentToll.tolls.get(trc).rate);
								Log.w("Toll rate","$"+currentToll.tolls.get(trc).rate);
								rateValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
								rateTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
										LinearLayout.LayoutParams.WRAP_CONTENT,
										0.3f));
								tollRateLayout.addView(rateValue);
								rateLayout.addView(tollRateLayout);
								
								/* If there is more than 1 tollway in the path, we add it to the total charges */
								if (tolls.size()>1){
									if (totalCharges.size()<1){
										// Adds the first tollway to the totalCharges Array
										TollRate currentRate = new TollRate();
										currentRate.vehicleType = variation;
										currentRate.rate = currentToll.tolls.get(trc).rate;
										totalCharges.add(currentRate);
									} else {
										// If there is 1 or more tolls in the totalCharges array
										boolean totalChargeFound=false;
										for (int ttc=0; ttc < totalCharges.size(); ttc++){
											if (totalCharges.get(ttc).vehicleType.equalsIgnoreCase(variation)){
												totalCharges.get(ttc).rate = Float.toString(
														Float.parseFloat(totalCharges.get(ttc).rate)+
														Float.parseFloat(currentToll.tolls.get(trc).rate));
												totalChargeFound=true;
											}
										}
										if (!totalChargeFound){
											TollRate currentRate = new TollRate();
											currentRate.vehicleType = variation;
											currentRate.rate = currentToll.tolls.get(trc).rate;
											totalCharges.add(currentRate);
										}
									}
								}
								ttfound++;
							}
							trc++;
						}
						break;
				}
			}
			
			/* The following code will compress the total entry */
			if (tolls.size()>1){
							
				int tcc=0;
				boolean second=false;
				while ((tcc<totalCharges.size())&&(!second)){
					
					// need to search to see if car, lcv, hcv, and cv exists to combine them
					ArrayList<String> matchingValues = new ArrayList<String>();
					if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("car")){
						matchingValues.add("Weekdays");
						matchingValues.add("Weekends");
						convertTollTotal(tcc, matchingValues, totalCharges);
					} else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("light commercial vehicle")){
						matchingValues.add("Light Commercial Vehicle Daytime");
						matchingValues.add("Light Commercial Vehicle Nighttime");
						if(!convertTollTotal(tcc, matchingValues, totalCharges)){
							boolean commFound=false;
							for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
								if ((totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Daytime"))||
									(totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Nighttime")))
									commFound=true;
							}
							if (commFound){
								TollRate newTollRate;
								for (int mvc=0; mvc<matchingValues.size(); mvc++){
									newTollRate = new TollRate();
									newTollRate.vehicleType=matchingValues.get(mvc);
									newTollRate.rate=totalCharges.get(tcc).rate;
									totalCharges.add(newTollRate);
								}
								
								totalCharges.remove(tcc);
								matchingValues = new ArrayList<String>();
								tcc=-1;
							}
						}
					} else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Heavy Commercial Vehicle")){
						matchingValues.add("Heavy Commercial Vehicle Daytime");
						matchingValues.add("Heavy Commercial Vehicle Nighttime");
						if(!convertTollTotal(tcc, matchingValues, totalCharges)){
							boolean commFound=false;
							for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
								if ((totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Daytime"))||
									(totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("Commercial Vehicle Nighttime")))
									commFound=true;
							}
							if (commFound){
								TollRate newTollRate;
								for (int mvc=0; mvc<matchingValues.size(); mvc++){
									newTollRate = new TollRate();
									newTollRate.vehicleType=matchingValues.get(mvc);
									newTollRate.rate=totalCharges.get(tcc).rate;
									totalCharges.add(newTollRate);
								}
								
								totalCharges.remove(tcc);
								matchingValues = new ArrayList<String>();
								tcc=-1;
							}
						}
					} else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Commercial Vehicle Daytime")){
						boolean lcvConvert=false;
						boolean hcvConvert=false;
						for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
							if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("light commercial vehicle"))
								lcvConvert=true;
							if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("heavy commercial vehicle"))
								hcvConvert=true;
						}
						if ((!lcvConvert)&&(!hcvConvert)){
							matchingValues.add("Light Commercial Vehicle Daytime");
							matchingValues.add("Heavy Commercial Vehicle Daytime");
							if(convertTollTotal(tcc, matchingValues, totalCharges)){
								tcc=-1;
							}
						}
					} else 	if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Commercial Vehicle Nighttime")){
						boolean lcvConvert=false;
						boolean hcvConvert=false;
						for (int tcc2=0; tcc2<totalCharges.size(); tcc2++){
							if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("light commercial vehicle"))
								lcvConvert=true;
							if (totalCharges.get(tcc2).vehicleType.equalsIgnoreCase("heavy commercial vehicle"))
								hcvConvert=true;
						}
						if ((!lcvConvert)&&(!hcvConvert)){
							matchingValues.add("Light Commercial Vehicle Nighttime");
							matchingValues.add("Heavy Commercial Vehicle Nighttime");
							if(convertTollTotal(tcc, matchingValues, totalCharges)){
								tcc=-1;
							}
						}
					}
					tcc++;
					if (tcc>=totalCharges.size()){
						tcc=0;
						second=true;
					}
				}
				
				if (totalCharges.size()>1){
					TextView tollTotalTitle = new TextView(appContext);
					tollTotalTitle.setText("Total Tolls");
					tollTotalTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
					tollTotalTitle.setPadding(10, 0, 0, 0);
					tollTotalTitle.setLayoutParams(fillParentParams);
					rateLayout.addView(tollTotalTitle);

					for (tcc=0; tcc < totalCharges.size(); tcc++){
						LinearLayout totalLine = new LinearLayout(appContext);
						totalLine.setOrientation(LinearLayout.HORIZONTAL);
						TextView totalType = new TextView(appContext);
						if (selectedVehicle.equalsIgnoreCase("all"))
							if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Weekdays"))
								totalCharges.get(tcc).vehicleType="Car - Weekdays";
							else if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase("Weekends"))
								totalCharges.get(tcc).vehicleType="Car - Weekends";
						totalType.setText(totalCharges.get(tcc).vehicleType);
						totalType.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
						totalType.setPadding(10, 0, 10, 0);
						totalLine.addView(totalType);
						TextView totalValue = new TextView(appContext);
						totalValue.setText("$"+totalCharges.get(tcc).rate);
						totalValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
						totalValue.setLayoutParams(wrapContentParams);
						totalLine.addView(totalValue);
						rateLayout.addView(totalLine);
					}
				} else if (totalCharges.size()==1){
					LinearLayout totalLine = new LinearLayout(appContext);
					totalLine.setOrientation(LinearLayout.HORIZONTAL);
					TextView tollTotalTitle = new TextView(appContext);
					tollTotalTitle.setText("Total Tolls");
					tollTotalTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					tollTotalTitle.setPadding(10, 0, 0, 0);
					totalLine.addView(tollTotalTitle);
					
					TextView totalValue = new TextView(appContext);
					totalValue.setText("$"+String.format("%.2g%n", totalCharges.get(0).rate));
					totalValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
					totalValue.setLayoutParams(wrapContentParams);
					totalLine.addView(totalValue);
					rateLayout.addView(totalLine);
				}
			}
			
			return rateLayout;
			
		}
		return null;
	}
	
	/** This is used to merge toll totals.
	 * 
	 * @param from
	 * @param to
	 * @param totalCharges
	 * @return
	 */
	public boolean convertTollTotal(int from, ArrayList<String> to, ArrayList<TollRate> totalCharges){
		int itemCount=0;
		
		for (int tcc=0; tcc<totalCharges.size(); tcc++){
			for (int toCount=0; toCount< to.size(); toCount++){
				if (totalCharges.get(tcc).vehicleType.equalsIgnoreCase(to.get(toCount))){
					totalCharges.get(tcc).rate = Float.toString((float)Math.round(
							(Float.parseFloat(totalCharges.get(tcc).rate)+
							 Float.parseFloat(totalCharges.get(from).rate))*100)/100);
					totalCharges.get(tcc).rate = String.format("%.2f",Float.parseFloat(totalCharges.get(tcc).rate)); 
					itemCount++;
				}
			}
		}
		if (itemCount>0){
			totalCharges.remove(from);
			return true;
		}
		return false;
	}

	public Street getStreet(String tollway, String street){
		boolean cityFound=false;
		int cityCount=0;
		
		while ((!cityFound)&&(cityCount<cities.size())){
			if (cities.get(cityCount).getStreet(tollway, street)!=null)
				return cities.get(cityCount).getStreet(tollway, street);
			
			cityCount++;
		}
		
		return null;
	}

	public LatLng getStart() {
		return start;
	}
	
	public Street getStartStreet() {
		if (start!=null)
			return findStreetByLatLng(start);
		else
			return null;
	}
	
	public void setStart(Street newStart) {
		// need to seach for the street object to get the tollway name
		// tollway =
		if (newStart!=null){
			start = new LatLng(newStart.getLatLng().latitude,newStart.getLatLng().longitude);
			setStreetsToInvalid();
			markRoads(start);
		} else {
			start=null;
			setValidStarts();
		}
	}

	public LatLng getFinish() {
		return finish;
	}


	public Street getFinishStreet() {
		if (finish!=null)
			return findStreetByLatLng(finish);
		else
			return null;
	}
	
	public void setFinish(Street newFinish) {
		if (newFinish!=null)
			finish = new LatLng(newFinish.getLatLng().latitude, newFinish.getLatLng().longitude);
		else
			finish = null;
	}
	
	public void setStreetsToInvalid(){
		for (int cityCount=0; cityCount< cities.size(); cityCount++)
			cities.get(cityCount).setStreetsToInvalid();
	}
	
	public void markRoads(LatLng startingPoint){
		getSelectedCity().markRoads(startingPoint);
	}

	public String getTollway() {
		return getTollwayName(getStartStreet());
	}

	public ArrayList<Street> getTollPointExits(Street start) {
		ArrayList<Street> exitList = new ArrayList<Street>();
		
			exitList = getSelectedCity().getTollPointExits(start);
			
			if (exitList!=null)
				return exitList;
		
		return null;
	}

	public String getTollwayName(Street start) {
		String tollwayName="";
		for (int cityCount=0; cityCount<cities.size(); cityCount++){
			tollwayName=cities.get(cityCount).getTollwayName(start);
			if (tollwayName!=null)
				return tollwayName;
		}
		return null;
	}

	/** This method is used to find a street found at a specified Longitude & Latitude
	 * 
	 * @param latLng
	 * @return
	 */
	public Street findStreetByLatLng(LatLng latLng) {
		Street street=null;
		boolean streetFound=false;
		int cityCount=0,
			tollwayCount=0,
			streetCount=0;
		
		// While loop to go through all the cities
		while ((!streetFound)&&(cityCount<cities.size())){
			tollwayCount=0;
			// While loop to go through all tollways on each city
			while ((!streetFound)&&(tollwayCount<cities.get(cityCount).getTollwayCount())){
				streetCount=0;
				// While loop to go through all streets on each tollway
				while ((!streetFound)&&(streetCount<cities.get(cityCount).getTollway(tollwayCount).getStreets().size())){
					Street currentStreet = cities.get(cityCount).getTollway(tollwayCount).getStreets().get(streetCount);
					LatLng currentLatLng = currentStreet.getLatLng();
					// If location passed in is the current street, mark that it's found
					if ((currentLatLng.longitude==latLng.longitude)&&
						(currentLatLng.latitude==latLng.latitude)){
						street=currentStreet;
						streetFound=true;
					}
					streetCount++;
				}
				tollwayCount++;
			}
			cityCount++;
		}
		return street;
	}

	/** Get the Id of the city specified by name
	 * @param selectedCity
	 * @return cityId
	 */
	public int getCityId(String selectedCity) {
		boolean cityFound=false;
		int cityCount=0;
		
		while ((!cityFound)&&(cityCount<cities.size())){
			if (cities.get(cityCount).getCityName().equalsIgnoreCase(selectedCity))
				cityFound=true;
			else
				cityCount++;
		}
		if (cityFound)
			return cityCount;
		else
			return 0;
	}

	/**
	 * @return the oldCityName
	 */
	public String getOldCityName() {
		return oldCityName;
	}

	/**
	 * @param oldCityName the oldCityName to set
	 */
	public void setOldCityName(String oldCityName) {
		this.oldCityName = oldCityName;
	}
}
