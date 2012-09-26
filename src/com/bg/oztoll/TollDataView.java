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
	private float density,		// screen density
				  xMultiplier=1,// xMultiplier for the screen size
				  yMultiplier=1;// yMultiplier for the screen size
	private Coordinates canvasMins;

	public TollDataView(){
		syncObject = new Object();
		moveSync = new Object();
		tollwayMap = Bitmap.createBitmap(1100, 768, Bitmap.Config.ARGB_8888);
		mapCanvas = new Canvas(tollwayMap);
		canvasMins= new Coordinates();
		
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
		// Setting the font
		name.setColor(Color.BLACK);
		float textSize = name.getTextSize();
		name.setTextSize(100);
		name.setTextScaleX(1.0f);
		Rect bounds = new Rect();
		name.getTextBounds("Tullamarine Fwy", 0, 15, bounds);
		int textHeight = bounds.bottom-bounds.top;
		name.setTextSize((float)(textSize*xMultiplier)/(float)textHeight*100f);

		// Map stuff
		map.setColor(Color.BLACK);
		mapSelected.setColor(Color.GREEN);
		map.setStrokeWidth((6*xMultiplier)/density);
		mapSelected.setStrokeWidth((3*xMultiplier)/density);
		
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
		stillRunning=true;
		while (stillRunning){
			synchronized (dataSync){
				try {
					// Put in this condition so that if tollData has finished reading the file, the app wont be put to sleep
					// waiting for the data to be read
					dataSync.wait();
				} catch (InterruptedException e) {
					// just wait for it
				}
			}
			if (!cityName.equalsIgnoreCase(tollData.getCityName()))
				cityName=tollData.getCityName();

			synchronized(syncObject){
				Coordinates[] mapSize = new Coordinates[2];
				mapSize[0] = new Coordinates();
				mapSize[1] = new Coordinates();
				canvasMins = new Coordinates();
				for (int twc=0; twc<tollData.getTollwayCount(); twc++)
					for (int tsc=0; tsc<tollData.getStreetCount(twc); tsc++){
						Street currentStreet = tollData.getStreet(twc, tsc);
						float nameLength = (name.measureText(currentStreet.getName()));
						switch (currentStreet.getLocation()){
							case 1:
								if ((drawX(currentStreet.getX())+nameLength+(20*xMultiplier))>mapSize[1].getX())
									mapSize[1].setX(drawX(currentStreet.getX())+(nameLength+(20*xMultiplier)));
								if ((drawX(currentStreet.getX())-(20*xMultiplier))<mapSize[0].getX())
									mapSize[0].setX(drawX(currentStreet.getX())-(20*xMultiplier));
								if (((drawY(currentStreet.getY())+(5*yMultiplier))>mapSize[1].getY()))
									mapSize[1].setY(drawY(currentStreet.getY())+(5*yMultiplier));
								if (((drawY(currentStreet.getY())-((5*yMultiplier)+name.getTextSize()))<mapSize[0].getY()))
									mapSize[0].setY(drawY(currentStreet.getY())-((5*yMultiplier)+name.getTextSize()));
								break;
							case 2:
								if ((drawX(currentStreet.getX())-(nameLength/2))<mapSize[0].getX())
									mapSize[0].setX((drawX(currentStreet.getX())-(nameLength/2)));
								if ((drawX(currentStreet.getX())+(nameLength/2))<mapSize[1].getX())
									mapSize[1].setX((drawX(currentStreet.getX())+(nameLength/2)));
								if ((drawY(currentStreet.getY())+(25*yMultiplier)+name.getTextSize())>mapSize[1].getY())
									mapSize[1].setY(drawY(currentStreet.getY())+(25*yMultiplier)+name.getTextSize());
								if ((drawY(currentStreet.getY())-(20*yMultiplier))<mapSize[0].getY())
									mapSize[0].setY(drawY(currentStreet.getY())-(20*yMultiplier));
								break;
							case 3:
								if ((drawX(currentStreet.getX())-(nameLength/2))<mapSize[0].getX())
									mapSize[0].setX((drawX(currentStreet.getX())-(nameLength/2)));
								if ((drawX(currentStreet.getX())+(nameLength/2))<mapSize[1].getX())
									mapSize[1].setX((drawX(currentStreet.getX())+(nameLength/2)));
								if ((drawY(currentStreet.getY())-((20*yMultiplier)+name.getTextSize())<mapSize[0].getY()))
									mapSize[0].setY(drawY(currentStreet.getY())-((20*yMultiplier)+name.getTextSize()));
								if ((drawY(currentStreet.getY())+(10*yMultiplier))>mapSize[1].getY())
									mapSize[1].setY(drawY(currentStreet.getY())+(10*yMultiplier));
								break;
							case 0:
							default:
								if ((drawX(currentStreet.getX())-(nameLength+(20*xMultiplier)))<mapSize[0].getX())
									mapSize[0].setX(drawX(currentStreet.getX())-(nameLength+(20*xMultiplier)));
								if ((drawX(currentStreet.getX())+(20*xMultiplier))>mapSize[1].getY())
									mapSize[1].setX(drawX(currentStreet.getX())+(20*xMultiplier));
								if (((drawY(currentStreet.getY())+(5*yMultiplier))>mapSize[1].getY()))
									mapSize[1].setY(drawY(currentStreet.getY())+(5*yMultiplier));
								if (((drawY(currentStreet.getY())-((5*yMultiplier)+name.getTextSize()))<mapSize[0].getY()))
									mapSize[0].setY(drawY(currentStreet.getY())-((5*yMultiplier)+name.getTextSize()));
								break;
						}
					}
				canvasMins.setX(0-mapSize[0].getX());
				canvasMins.setY(0-mapSize[0].getY());
				tollwayMap = Bitmap.createBitmap((((Float)(mapSize[1].getX()-mapSize[0].getX())).intValue()+10), (((Float)(mapSize[1].getY()-mapSize[0].getY())).intValue()+10), Bitmap.Config.ARGB_8888);
				mapCanvas = new Canvas(tollwayMap);

				mapCanvas.drawColor(Color.WHITE);

				// Draw the map here
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int tsc=0; tsc<tollData.getStreetCount(twc); tsc++){
						Street currentStreet = tollData.getStreet(twc, tsc);
						if ((currentStreet.isValid())||(!tollData.isFinished())||
							(currentStreet==tollData.getStart())||
							(currentStreet==tollData.getFinish()))
							mapCanvas.drawCircle(drawX(currentStreet.getX()), drawY(currentStreet.getY()), 10*xMultiplier, map);
						
						String streetName = currentStreet.getName();
						float txtWidth = name.measureText(streetName);
						if (((tollData.getStart()==null)&&(!tollData.isFinished()))||
							(currentStreet.isValid())||
							(currentStreet==tollData.getStart())||
							(currentStreet==tollData.getFinish())){
							switch (currentStreet.getLocation()){
							case 1:
								// Draws to the right of the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())+(20*xMultiplier), drawY(currentStreet.getY())+(5*yMultiplier), name);
								break;
							case 2:
								// Draws the text vertically below the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())-(txtWidth/2), drawY(currentStreet.getY())+(25*yMultiplier), name);								
								break;
							case 3:
								// Draws the text vertically above the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())-(txtWidth/2), drawY(currentStreet.getY())-(20*yMultiplier), name);
								break;
							case 0:
							default:
								// Draws the text to the left of the street
								mapCanvas.drawText(streetName, drawX(currentStreet.getX())-(txtWidth+(20*xMultiplier)), drawY(currentStreet.getY())+(5*yMultiplier), name);
								break;								
							}
						}

					}

					for (int tpc=0; tpc<tollData.getPathwayCount(twc); tpc++)
						mapCanvas.drawLine(drawX(tollData.getPathway(twc,tpc).getStart().getX()),
										   drawY(tollData.getPathway(twc,tpc).getStart().getY()),
										   drawX(tollData.getPathway(twc,tpc).getEnd().getX()),
										   drawY(tollData.getPathway(twc,tpc).getEnd().getY()),map);
				}

				for (int cc=0; cc<tollData.getConnectionCount(); cc++)
					mapCanvas.drawLine(drawX(tollData.getConnection(cc).getStart().getX()),
							   drawY(tollData.getConnection(cc).getStart().getY()),
							   drawX(tollData.getConnection(cc).getEnd().getX()),
							   drawY(tollData.getConnection(cc).getEnd().getY()),map);
				
				// Marking the map, (pathway and start and end)
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int tsc=0; tsc<tollData.getStreetCount(twc); tsc++){
						Street currentStreet = tollData.getStreet(twc, tsc);
						// if street is start or end. mark it with a selected circle
						if ((currentStreet==tollData.getStart())||
							(currentStreet==tollData.getFinish()))
							mapCanvas.drawCircle(drawX(currentStreet.getX()), drawY(currentStreet.getY()), 6*xMultiplier, mapSelected);
					}
					for (int tpc=0; tpc<tollData.getPathwayCount(twc); tpc++)
						if (tollData.getPathway(twc,tpc).isRoute())
							mapCanvas.drawLine(drawX(tollData.getPathway(twc,tpc).getStart().getX()),
									   drawY(tollData.getPathway(twc,tpc).getStart().getY()),
									   drawX(tollData.getPathway(twc,tpc).getEnd().getX()),
									   drawY(tollData.getPathway(twc,tpc).getEnd().getY()),mapSelected);
				}
				for (int cc=0; cc<tollData.getConnectionCount(); cc++)
					if (tollData.getConnection(cc).isRoute())
						mapCanvas.drawLine(drawX(tollData.getConnection(cc).getStart().getX()),
								   drawY(tollData.getConnection(cc).getStart().getY()),
								   drawX(tollData.getConnection(cc).getEnd().getX()),
								   drawY(tollData.getConnection(cc).getEnd().getY()),mapSelected);
				
				
			}
		}
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
		return (mapPointX*70*xMultiplier)+canvasMins.getX();
	}
	
	public float drawY(float mapPointY){
		return (mapPointY*50*yMultiplier)+canvasMins.getY();
	}

	public void findStreet(Coordinates touchStart, Coordinates origin) {
		touchStart.updateX(0-origin.getX());
		touchStart.updateY(0-origin.getY());
		Log.w ("ozToll","findStreet().touchStart(): x="+touchStart.getX()+", y="+touchStart.getY());
		if (tollData.isFinished()){
			boolean streetFound=false;
			int twc=0,
				sc=0;
			while ((!streetFound)&&(twc<tollData.getTollwayCount())){
				Street currentStreet = tollData.getStreet(twc, sc);
				
				if (currentStreet!=null){
					Log.w("ozToll","street:"+currentStreet.getName());
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
							minX = streetCoords.getX()-(20*xMultiplier);
							// maxX adds the length of the text away
							maxX = streetCoords.getX()+((20+bounds.right-bounds.left)*xMultiplier);
							minY = streetCoords.getY()-(20*xMultiplier);
							maxY = streetCoords.getY()+(20*xMultiplier);
							break;
						case 2:
							// minX and maxX are around the text width which is printed in the middle of the street point
							minX = streetCoords.getX()-((bounds.right-bounds.left)/2);
							maxX = streetCoords.getX()+((bounds.right-bounds.left)/2);
							minY = streetCoords.getY()-(20*xMultiplier);
							// maxY is the height of the text above the street point
							maxY = streetCoords.getY()+((bounds.bottom-bounds.top)+(25));
							break;
						case 3:
							minX = streetCoords.getX()-((bounds.right-bounds.left)/2);
							maxX = streetCoords.getX()+((bounds.right-bounds.left)/2);
							minY = streetCoords.getY()-((20+bounds.bottom-bounds.top)*xMultiplier);
							maxY = streetCoords.getY()+(20*xMultiplier);
							break;
						case 0:
						default:
							minX = streetCoords.getX()-((bounds.right-bounds.left+20)*xMultiplier);
							maxX = streetCoords.getX()+(20*xMultiplier);
							minY = streetCoords.getY()-(20*xMultiplier);
							maxY = streetCoords.getY()+(20*xMultiplier);
							break;
					}
					/*
					Log.w ("ozToll","findStreet(): minX="+minX+", maxX="+maxX+", minY="+minY+", maxY="+maxY);
					Log.w ("ozToll","findStreet() Street:"+currentStreet.getName()+
									": {"+currentStreet.getX()+","+currentStreet.getY()+"}{"+
									streetCoords.getX()+","+streetCoords.getY()+"}" +
									" Location:"+currentStreet.getLocation());
					*/
					
					// The if statement to check if the street is selected
					if ((touchStart.getX()>minX)&&
						(touchStart.getX()<maxX)&&
						(touchStart.getY()>minY)&&
						(touchStart.getY()<maxY)){
						if ((getStart()==null)&&(currentStreet.isValid())){
							// If no selection has been made yet
							setStart(currentStreet);
							//Log.w ("ozToll","findStreet() :"+currentStreet.getName());
							tollData.setStreetsToInvalid();
							markRoads(tollData.getStart());
							for (int cc=0; cc < tollData.getConnectionCount(); cc++){
								if (tollData.getConnection(cc).getStart()==tollData.getStart())
									markRoads(tollData.getConnection(cc).getEnd());
								else if (tollData.getConnection(cc).getEnd()==tollData.getStart())
									markRoads(tollData.getConnection(cc).getStart());
							}
							streetFound=true;
							//Log.w("ozToll","findStreet().notify()");
							synchronized(dataSync){
								try {
									dataSync.notify();
								} catch (IllegalMonitorStateException e){
									// just so it wont crash
								}
							}
						} else if (currentStreet==getStart()){
							if (getEnd()==null){
								// If the user deselects the start road
								setStart(null);
								// calling resetPaths resets the paths and the valid streets
								resetPaths();
								streetFound=true;
								synchronized(dataSync){
									try {
										dataSync.notify();
									} catch (IllegalMonitorStateException e){
										// just so it wont crash
									}
								}
								// Send handler to reset shownStart to false
								Message msg = mainHandler.obtainMessage();
								msg.what=7;
								mainHandler.sendMessage(msg);
							}
						} else if ((currentStreet.isValid())&&(getEnd()==null)){
							// If the user selects the end road
							setEnd(currentStreet);
							processPath(tollData.getStart(),tollData.getFinish());
							rateLayout = tollData.processToll(tollData.getStart(), tollData.getFinish(), appContext);
							rateCalculated=true;
							streetFound=true;
							synchronized(dataSync){
								try {
									dataSync.notify();
								} catch (IllegalMonitorStateException e){
									// just so it wont crash
								}
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
							streetFound=true;
							synchronized(dataSync){
								try {
									dataSync.notify();
								} catch (IllegalMonitorStateException e){
									// just so it wont crash
								}
							}
							setRateCalculated(false);
							Message msg = mainHandler.obtainMessage();
							msg.what=8;
							mainHandler.sendMessage(msg);
						}
					}
				}
				sc++;
				if (sc>tollData.getStreetCount(twc)){
					twc++;
					sc=0;
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

	public void setXMultiplier(float f) {
		xMultiplier = f;
	}

	public void setYMultiplier(float f) {
		yMultiplier = f;
	}
}
