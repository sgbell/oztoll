/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class TollDataView implements Runnable{
	private Object syncObject, 
	               dataSync,
	               moveSync;
	private OzTollData tollData;
	private String cityName;
	private boolean stillRunning, 
					rateCalculated=false;
	private Context appContext;
	private LinearLayout rateLayout;
	private Handler mainHandler;

	private Bitmap tollwayMap; // New version of the map drawn to a bitmap
	private Canvas mapCanvas; // Canvas is used to draw on the bitmap
	private Paint map, 			// unselected map color & the pathway
				  mapSelected, 	// selected map exit & pathway when start and end both selected
				  name;			// Exit names
	private float density;		// screen density

	public TollDataView(){
		syncObject = new Object();
		moveSync = new Object();
		tollwayMap = Bitmap.createBitmap(1024, 768, Bitmap.Config.ARGB_8888);
		mapCanvas = new Canvas(tollwayMap);
		
		map = new Paint();
		name = new Paint();
		mapSelected = new Paint();
	}
	
	public TollDataView(OzTollData data){
		this();
		tollData=data;
		cityName = "";		
		
		dataSync = tollData.getDataSync();
	}
	
	public TollDataView(OzTollData data, int height, int width, Context context){
		this(data);
		appContext=context;
	}
	
	public void resizeView(float density){
		Log.w("tollDataView", "resizeView - density:"+density);
		// Setting the font
		name.setColor(Color.BLACK);
		float textSize = name.getTextSize();
		name.setTextSize(100);
		name.setTextScaleX(1.0f);
		Rect bounds = new Rect();
		name.getTextBounds("Tullamarine Fwy", 0, 15, bounds);
		int textHeight = bounds.bottom-bounds.top;
		name.setTextSize((float)textSize/(float)textHeight*100f);

		// Map stuff
		map.setColor(Color.BLACK);
		mapSelected.setColor(Color.GREEN);
		map.setStrokeWidth(6/density);
		mapSelected.setStrokeWidth(3/density);
		
		try {
			syncObject.notify();
		} catch (IllegalMonitorStateException e){
			// just ignore the error
		}
	}

	public Object getMoveSync(){
		return moveSync;
	}
	
	public Object getSync(){
		return syncObject;
	}
	
	public void setDataSync(Object syncMe){
		dataSync = syncMe;
	}
	
	public Object getDataSync(){
		return dataSync;
	}
	
	
	@Override
	public void run() {
		synchronized (dataSync){
			try {
				// Put in this condition so that if tollData has finished reading the file, the app wont be put to sleep
				// waiting for the data to be read
				if (!tollData.isFinished())
					dataSync.wait();
			} catch (InterruptedException e) {
				// just wait for it
			}
		}
		
		stillRunning=true;
		while (stillRunning){
			if (!cityName.equalsIgnoreCase(tollData.getCityName()))
				cityName=tollData.getCityName();

			mapCanvas.drawColor(Color.WHITE);
			synchronized(syncObject){
				// Draw the map here
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int tsc=0; tsc<tollData.getStreetCount(twc); tsc++){
						Street currentStreet = tollData.getStreet(twc, tsc);
						if (currentStreet.isValid())
							mapCanvas.drawCircle(drawX(currentStreet.getX()), drawY(currentStreet.getY()), 10, map);
						if ((currentStreet==tollData.getStart())||
							(currentStreet==tollData.getFinish()))
							mapCanvas.drawCircle(drawX(currentStreet.getX()), drawY(currentStreet.getY()), 6, mapSelected);
						
						String streetName = currentStreet.getName();
						float txtWidth = name.measureText(streetName);
						if (((tollData.getStart()==null)&&(!tollData.isFinished()))||
							(currentStreet.isValid())||
							(currentStreet==tollData.getStart())||
							(currentStreet==tollData.getFinish())){
							switch (currentStreet.getLocation()){
							case 1:
								// Draws to the right of the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())+20, drawY(currentStreet.getY())+5, name);
								break;
							case 2:
								// Draws the text vertically below the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())-(txtWidth/2), drawY(currentStreet.getY())+25, name);								
								break;
							case 3:
								// Draws the text vertically above the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())-(txtWidth/2), drawY(currentStreet.getY())-20, name);
								break;
							case 0:
							default:
								// Draws the text to the left of the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())-(txtWidth+20), drawY(currentStreet.getY())+5, name);
								break;								
							}
						}

					}

					for (int tpc=0; tpc<tollData.getPathwayCount(twc); tpc++)
						drawPathway(tollData.getPathway(twc, tpc));
				}
				for (int cc=0; cc<tollData.getConnectionCount(); cc++)
					drawPathway(tollData.getConnection(cc));
				
				try {
					syncObject.wait();
				} catch (InterruptedException e) {
					// just wait for screen to be moved
				}
			}
		}
	}
	
	/**
	 * 
	 * @param currentPathway
	 */
	public void drawPathway(Pathway currentPathway){
		if (currentPathway.getStart().getX()==currentPathway.getEnd().getX()){
			/* If the current spot is an invalid exit for the selected starting point, and is not the start
			 * or finish, this if statement will replace the bottom half of a circle with a line.
			 */
			if ((!currentPathway.getStart().isValid())&&
				(currentPathway.getStart()!=tollData.getStart())&&
				(currentPathway.getStart()!=tollData.getFinish()))
				mapCanvas.drawLine(drawX(currentPathway.getStart().getX()),
								   drawY(currentPathway.getStart().getY())-10,
								   drawX(currentPathway.getEnd().getX()),
								   drawY(currentPathway.getEnd().getY())+20, map);
			/* If the current spot is an invalid exit for the selected starting point, and is not the start
			 * or finish, this if statement will replace the top half of a circle with a line. 
			 */
			if ((!currentPathway.getEnd().isValid())&&
				(currentPathway.getEnd()!=tollData.getStart())&&
				(currentPathway.getEnd()!=tollData.getFinish()))
					mapCanvas.drawLine(drawX(currentPathway.getEnd().getX()),
									   drawY(currentPathway.getEnd().getY())-20,
									   drawX(currentPathway.getEnd().getX()),
									   drawY(currentPathway.getEnd().getY())+10,
									   map);

		} else if (currentPathway.getStart().getY()==currentPathway.getEnd().getY()){
			// Horizontal Roads, marking the map when a street is gone
			/* If the current spot is an invalid exit for the selected starting point, and is not the start
			 * or finish, this if statement will replace the right half of a circle with a line.
			 */
			if ((!currentPathway.getStart().isValid())&&
				(currentPathway.getStart()!=tollData.getStart())&&
				(currentPathway.getStart()!=tollData.getFinish()))
					mapCanvas.drawLine(drawX(currentPathway.getStart().getX())-10,
							 drawY(currentPathway.getStart().getY()),
							 drawX(currentPathway.getStart().getX())+20,
							 drawY(currentPathway.getStart().getY()),
							 map);
			/* If the current spot is an invalid exit for the selected starting point, and is not the start
			 * or finish, this if statement will replace the left half of a circle with a line.
			 */
			if ((!currentPathway.getEnd().isValid())&&
				(currentPathway.getEnd()!=tollData.getStart())&&
				(currentPathway.getEnd()!=tollData.getFinish()))
					mapCanvas.drawLine(drawX(currentPathway.getEnd().getX())-20,
									   drawY(currentPathway.getEnd().getY()),
									   drawX(currentPathway.getEnd().getX())+10,
									   drawY(currentPathway.getEnd().getY()),
									   map);
		}
		mapCanvas.drawLine(drawX(currentPathway.getStart().getX()),
				 drawY(currentPathway.getStart().getY()),
				 drawX(currentPathway.getEnd().getX()),
				 drawY(currentPathway.getEnd().getY()),
				 map);
		if (currentPathway.isRoute())
			mapCanvas.drawLine(drawX(currentPathway.getStart().getX()),
					 		   drawY(currentPathway.getStart().getY()),
					 		   drawX(currentPathway.getEnd().getX()),
					 		   drawY(currentPathway.getEnd().getY()),
					 		   mapSelected);
	}
	
	/** This function is used to get the map for OzTollView
	 * 
	 * @return
	 */
	public Bitmap getTollwayMap(){
		return tollwayMap;
	}
	
	/**
	 * This is used to mark the path on the map between the selected points.
	 */
	public void processPath(Street start, Street end){
		if ((start!=null)&&(end!=null)){
			ArrayList<Pathway> paths=tollData.getPathway(start, end);
			
			for (int pc=0; pc < paths.size(); pc++){
				paths.get(pc).setRoute(true);
			}
		}
	}

	/**This goes through the arrays and marks the roads.
	 * This Function may have been replaced by processPath().
	 * 
	 * @param tollway
	 * @param start
	 * @param end
	 */
	public void markPaths(int tollway, Street start, Street end){
		boolean markStart=false, markEnd=false;

		for (int pwc=0; pwc < tollData.getPathwayCount(tollway); pwc++){
			if ((tollData.getPathway(tollway, pwc).getStart()==start)||
				(tollData.getPathway(tollway, pwc).getStart()==end)){
				if ((!markStart)&&(!markEnd))
					markStart=true;
			}
			if ((markStart)&&(!markEnd)){
				// Need to figure out how to make sure only the roads between start and end
				// are marked.
				tollData.getPathway(tollway,pwc).setRoute(true);
				if ((tollData.getPathway(tollway, pwc).getEnd()==start)||
					(tollData.getPathway(tollway, pwc).getEnd()==end)){
					markEnd=true;
				}
			}
		}
	}
	
	
	/** 
	 * This function will go through the entire map and mark the valid exits, once a
	 * starting point has been selected.
	 * @param validStreet
	 */
	public void markRoads(Street validStreet){
		if (validStreet!=null){
			ArrayList<Street> exitList= tollData.getTollPointExits(validStreet);
			ArrayList<Street> tollwayConnections= new ArrayList<Street>();
			if (exitList.size()>0)
				for (int elc=0; elc < exitList.size(); elc++){
					exitList.get(elc).setValid(true);
					for (int cc=0; cc < tollData.getConnectionCount(); cc++){
						if (exitList.get(elc)==tollData.getConnection(cc).getStart())
							tollwayConnections.add(tollData.getConnection(cc).getEnd());
						if (exitList.get(elc)==tollData.getConnection(cc).getEnd())
							tollwayConnections.add(tollData.getConnection(cc).getStart());
					}
				}
			if (tollwayConnections.size()>0)
				for (int tc=0; tc < tollwayConnections.size(); tc++){
					markRoads(tollwayConnections.get(tc));
				}
		}
	}
	
	public float drawX(float mapPointX){
		return (mapPointX*70)+110;
	}
	
	public float drawY(float mapPointY){
		return (mapPointY*50)+15;
	}

	public void findStreet(Coordinates touchStart) {
		if (tollData.isFinished()){
			for (int twc=0; twc < tollData.getTollwayCount(); twc++)
				for (int sc=0; sc < tollData.getStreetCount(twc); sc++){
					Street currentStreet = tollData.getStreet(twc, sc);
					Coordinates streetCoords = new Coordinates(
							drawX(currentStreet.getX()),
							drawY(currentStreet.getY()));
					
					Rect bounds = new Rect();
					name.getTextBounds(currentStreet.getName(), 0, currentStreet.getName().length(), bounds);
					float minX=0, maxX=0, minY=0, maxY=0;
					/* The following switch statement will set the min/max x/y values needed for the current street
					 * to check if the street is selected by the user.
					 */
					switch (currentStreet.getLocation()){
						case 1:
							minX = streetCoords.getX()-20;
							// maxX adds the length of the text away
							maxX = streetCoords.getX()+(20)+(bounds.right-bounds.left);
							minY = streetCoords.getY()-(20);
							maxY = streetCoords.getY()+(20);
							break;
						case 2:
							// minX and maxX are around the text width which is printed in the middle of the street point
							minX = streetCoords.getX()-((bounds.right-bounds.left)/2);
							maxX = streetCoords.getX()+((bounds.right-bounds.left)/2);
							minY = streetCoords.getY()-(20);
							// maxY is the height of the text above the street point
							maxY = streetCoords.getY()+((bounds.bottom-bounds.top)+(25));
							break;
						case 3:
							minX = streetCoords.getX()-((bounds.right-bounds.left)/2);
							maxX = streetCoords.getX()+((bounds.right-bounds.left)/2);
							minY = streetCoords.getY()-(20+(bounds.bottom-bounds.top));
							maxY = streetCoords.getY()+20;
							break;
						case 0:
						default:
							minX = streetCoords.getX()-((bounds.right-bounds.left)+20);
							maxX = streetCoords.getX()+20;
							minY = streetCoords.getY()-20;
							maxY = streetCoords.getY()+20;
							break;
					}

					// The if statement to check if the street is selected
					if ((touchStart.getX()>minX)&&
						(touchStart.getX()<maxX)&&
						(touchStart.getY()>minY)&&
						(touchStart.getY()<maxY)){
						if ((getStart()==null)&&(currentStreet.isValid())){
							// If no selection has been made yet
							setStart(currentStreet);
							tollData.setStreetsToInvalid();
							markRoads(tollData.getStart());
							for (int cc=0; cc < tollData.getConnectionCount(); cc++){
								if (tollData.getConnection(cc).getStart()==tollData.getStart())
									markRoads(tollData.getConnection(cc).getEnd());
								else if (tollData.getConnection(cc).getEnd()==tollData.getStart())
									markRoads(tollData.getConnection(cc).getStart());
							}
							try {
								syncObject.notify();
							} catch (IllegalMonitorStateException e){
								// just so it wont crash
							}
						} else if (currentStreet==getStart()){
							if (getEnd()==null){
								// If the user deselects the start road
								setStart(null);
								// calling resetPaths resets the paths and the valid streets
								resetPaths();
								// Send handler to reset shownStart to false
								Message msg = mainHandler.obtainMessage();
								msg.what=7;
								mainHandler.sendMessage(msg);
								try {
									syncObject.notify();
								} catch (IllegalMonitorStateException e){
									// just so it wont crash
								}
							}
						} else if ((currentStreet.isValid())&&(getEnd()==null)){
							// If the user selects the end road
							setEnd(currentStreet);
							processPath(tollData.getStart(),tollData.getFinish());
							rateLayout = tollData.processToll(tollData.getStart(), tollData.getFinish(), appContext);
							rateCalculated=true;
							try {
								syncObject.notify();
							} catch (IllegalMonitorStateException e){
								// just so it wont crash
							}
						} else if (currentStreet==getEnd()){
							// If the user de-selects the end road
							setEnd(null);
							resetPaths();
							tollData.setStreetsToInvalid();
							markRoads(tollData.getStart());
							for (int cc=0; cc < tollData.getConnectionCount(); cc++){
								if (tollData.getConnection(cc).getStart()==tollData.getStart()){
									markRoads(tollData.getConnection(cc).getEnd());
								} else if (tollData.getConnection(cc).getEnd()==tollData.getStart()){
									markRoads(tollData.getConnection(cc).getStart());
								}
							}
							setRateCalculated(false);
							Message msg = mainHandler.obtainMessage();
							msg.what=8;
							mainHandler.sendMessage(msg);
							try {
								syncObject.notify();
							} catch (IllegalMonitorStateException e){
								// just so it wont crash
							}
						}
					}
				}
		}
	}

	public Street getEnd() {
		return tollData.getFinish();
	}
	
	public void setEnd(Street end) {
		tollData.setFinish(end);
	}

	/**
	 * @return the start
	 */
	public Street getStart() {
		return tollData.getStart();
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(Street start) {
		tollData.setStart(start);
	}

	/**
	 * @return the rateDialogText
	 */
	public LinearLayout getRateDialog() {
		return rateLayout;
	}

	/**
	 * @param rateDialogText the rateDialogText to set
	 */
	public void setRateDialog(LinearLayout rateDialog) {
		rateLayout = rateDialog;
	}

	/**
	 * @return the rateCalculated
	 */
	public boolean isRateCalculated() {
		return rateCalculated;
	}

	/**
	 * @param rateCalculated the rateCalculated to set
	 */
	public void setRateCalculated(boolean rateCalculated) {
		this.rateCalculated = rateCalculated;
	}

	public void resetPaths() {
		for (int twc=0; twc < tollData.getTollwayCount(); twc++){
			for (int pwc=0; pwc < tollData.getPathwayCount(twc); pwc++){
				tollData.getPathway(twc, pwc).setRoute(false);
			}
			for (int tec=0; tec < tollData.getStreetCount(twc); tec++){
				tollData.getStreet(twc, tec).setValid(false);
			}
		}
		for (int cc=0; cc < tollData.getConnectionCount(); cc++)
			tollData.getConnection(cc).setRoute(false);

		tollData.setValidStarts();
	}

	public Handler getMainHandler() {
		return mainHandler;
	}

	public void setMainHandler(Handler mainHandler) {
		this.mainHandler = mainHandler;
	}
}
