/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * @author bugman
 *
 */
public class OzTollView extends SurfaceView implements SurfaceHolder.Callback {
	private OzTollData tollData = null;
	private DrawingThread thread;
	private Thread tollDataViewBuilder;
	private Coordinates touchStart;
	private TollDataView tollDataView;
	private Paint map, name;
	private Object syncObject;
	
	public void setDataFile(OzTollData tollData){
		this.tollData = tollData;

		tollDataView = new TollDataView(this.tollData, getHeight(), getWidth());
		syncObject = tollDataView.getSync();
		tollDataViewBuilder = new Thread(tollDataView);
		tollDataViewBuilder.start();
	}
	
	public OzTollView(Context context) {
		super(context);
		getHolder().addCallback(this);
		
		thread = new DrawingThread(getHolder(), this);
		
		setFocusable(true);
		
		touchStart = new Coordinates(0,0);
		
		map = new Paint();
		name = new Paint();
		map.setColor(Color.WHITE);
		map.setStrokeWidth(5 / getResources().getDisplayMetrics().density);
		name.setColor(Color.BLUE);
	}

	public void OnDraw(Canvas canvas){
		canvas.drawColor(Color.BLACK);
		
		if (tollDataView!= null){
			tollDataView.setWidth(getWidth());
			tollDataView.setHeight(getHeight());
			/* This is used to get the font size on the screen, which is used for displaying the
			 * road names vertically on the map
			 */
			float fontSize = name.getFontMetrics().bottom - name.getFontMetrics().top -2;
			
			synchronized (syncObject){
				ArrayList<Street> streets = tollDataView.getStreets();
				
				canvas.drawText("screenOrigin: "+tollDataView.getScreenOrigin().getY(), 0, 170, name);
				canvas.drawText("move: "+tollDataView.getMove().getY(), 0, 190, name);
				// currently streets is empty. :(
				if (streets!=null){
					canvas.drawText("Streets: "+streets.size(), 0, 150, name);
					for (int sc=0; sc < streets.size(); sc++){
						Coordinates currentStreet = new Coordinates(
								tollDataView.drawX(streets.get(sc).getX()),
								tollDataView.drawY(streets.get(sc).getY()));
					
						canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 10, map);
						
						String streetName = streets.get(sc).getName();
						float txtWidth = name.measureText(streetName);
						switch (streets.get(sc).getLocation()){
							case 1:
								// Draws to the right of the street
								canvas.drawText(streetName, currentStreet.getX()+20 , currentStreet.getY()+5, name);
								break;
							case 2:
								// Draws the text vertically below the street
								for (int lc=0; lc < streetName.length(); lc++){
									canvas.drawText(""+streetName.charAt(lc), currentStreet.getX()-5, currentStreet.getY()+(fontSize*lc)+25, name);
								}
								break;
							case 3:
								// Draws the text vertically above the street
								for (int lc=0; lc < streetName.length(); lc++){
									canvas.drawText(""+streetName.charAt(lc), currentStreet.getX()-3, currentStreet.getY()-((streetName.length()-lc)*fontSize), name);
								}
								break;
							case 0:
							default:
								// Draws the text to the left of the street
								canvas.drawText(streetName, currentStreet.getX()-(txtWidth+20) , currentStreet.getY()+5, name);
								break;								
						}
					}
				}
				ArrayList<Pathway> paths = tollDataView.getPaths();
				if (paths!=null){
					for (int pc=0; pc< paths.size(); pc++){
						Pathway currentPathway = paths.get(pc);
						drawLine(tollDataView.drawX(currentPathway.getStart().getX()),
								 tollDataView.drawY(currentPathway.getStart().getY()),
								 tollDataView.drawX(currentPathway.getEnd().getX()),
								 tollDataView.drawY(currentPathway.getEnd().getY()),
								 map,
								 canvas);
					}
				}
			}
		}
	}
	
	/** Old OnDraw(Canvas canvas)
	 * 
	public void OnDraw(Canvas canvas){
		ArrayList<Street> exitStreets;
		
		Paint map = new Paint();
		map.setColor(Color.WHITE);
		map.setStrokeWidth(5 / getResources().getDisplayMetrics().density);
		Paint deadMap = new Paint();
		deadMap.setColor(Color.GRAY);
		Paint name = new Paint();
		name.setColor(Color.BLUE);
		Paint canvasColor = new Paint();
		canvasColor.setColor(Color.BLACK);
		
		canvas.drawColor(Color.BLACK);

		if (tollData!=null){
			if (tollData.getTollwayCount()>0){
				/* This is used to get the font size on the screen, which is used for displaying the
				 * road names vertically on the map
				 *
				float fontSize = name.getFontMetrics().bottom - name.getFontMetrics().top -2;
				
				if (startStreet!=null)
					for (int twc=0; twc < tollData.getTollwayCount(); twc++)
						exitStreets = tollData.getTollPointExits(tollData.getTollwayName(twc), startStreet);
				
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int twi=0; twi<tollData.getStreetCount(twc); twi++){
						// The following draws the circle for the exit on the screen
						Coordinates street = new Coordinates (
								drawX(tollData.getStreetX(twc, twi)),
								drawY(tollData.getStreetY(twc, twi)));
						
						// The following tests to see if the street coordinates are on the screen, if it is, it will draw it.
						if ((street.getX()>-10)&&(street.getX()<getWidth()+10)&&(street.getY()>-10)&&(street.getY()<getHeight()+10)){
							/* This will be where we check if the point pressed is on the map, and if it is on the map
							 * it will mark the starting circle, otherwise startToll will be reset to null.
							 *
							if (startStreet!=null){
								
								if (startStreet==tollData.getStreet(twc, twi))
									canvas.drawCircle(street.getX(), street.getY(), 6, canvasColor);
								if (endStreet!=null){
									if (endStreet==tollData.getStreet(twc, twi))
										canvas.drawCircle(street.getX(), street.getY(), 6, canvasColor);
								}
							} else {
								canvas.drawCircle(street.getX(), street.getY(), 10, map);
							}
						
							/* The following gets the street name, and depending on the location stored in the
							 * xml file, it then draws the text on the screen.
							 *
							String streetName = tollData.getStreetName(twc, twi);
							float txtWidth = name.measureText(streetName);
							switch (tollData.getLocation(twc, twi)){
								case 1:
									// Draws to the right of the street
									canvas.drawText(streetName, street.getX()+20 , street.getY()+5, name);
									break;
								case 2:
									// Draws the text vertically below the street
									for (int lc=0; lc < streetName.length(); lc++){
										canvas.drawText(""+streetName.charAt(lc), street.getX()-5, street.getY()+(fontSize*lc)+25, name);
									}
									break;
								case 3:
									// Draws the text vertically above the street
									for (int lc=0; lc < streetName.length(); lc++){
										canvas.drawText(""+streetName.charAt(lc), street.getX()-3, street.getY()-((streetName.length()-lc)*fontSize), name);
									}
									break;
								case 0:
								default:
									// Draws the text to the left of the street
									canvas.drawText(streetName, street.getX()-(txtWidth+20) , street.getY()+5, name);
									break;								
							}
						}
						// Need to check the pathways to see if they are partly on the screen, if so draw them
						// Draw the road
						for (int pwc=0; pwc<tollData.getPathwayCount(twc); pwc++){
							Pathway currentPathway = tollData.getPathway(twc, pwc);
							drawLine(drawX(currentPathway.getStart().getX()),
									 drawY(currentPathway.getStart().getY()),
									 drawX(currentPathway.getEnd().getX()),
									 drawY(currentPathway.getEnd().getY()),
									 map,
									 canvas);
						}

					}
				}
				
				// Need to check it the connecting road is on the screen, if so draw it too
				// Draw the connecting road between tollways
				for (int cpc=0; cpc<tollData.getConnectionCount(); cpc++){
					Connection currentConnection = tollData.getConnection(cpc);
					drawLine(drawX(currentConnection.getStart().getX()),
							 drawY(currentConnection.getStart().getY()),
							 drawX(currentConnection.getEnd().getX()),
							 drawY(currentConnection.getEnd().getY()),
									map,
									canvas);						
				}
			}
		}
	}
	*/
	
	/** Draw line is a wrapper for Canvas.drawLine so it will only draw the line if it's
	 * within the visible space. 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param paint
	 * @param canvas
	 */
	public void drawLine(float startX, float startY, float endX, float endY, Paint paint, Canvas canvas){
		if ((((startX>0)&&(startX<getWidth()))||
			 ((endX>0)&&(endX<getWidth())))&&
			 (((startY>0)&&(startY<getHeight()))||
			 ((endY>0)&&(endY<getHeight())))){
			if (startX==endX){
				startY+=10;
				endY-=10;
			}
			if (startY==endY){
				startX+=10;
				endX-=10;
			}
			canvas.drawLine(startX,	startY,	endX, endY,	paint);
		}
	}
	
	/*
	public Street findStreet(float x, float y) {
		for (int twc=0; twc < tollData.getTollwayCount(); twc++)
			for (int sc=0; sc < tollData.getStreetCount(twc); sc++){
				Street currentStreet=tollData.getStreet(twc, sc);
				float streetX = drawX(currentStreet.getX()),
					  streetY = drawY(currentStreet.getY());
				
				if ((x>streetX-10)&&(x<streetX+10))
					if ((y>streetY-10)&&(y<streetY+10))
						return currentStreet;
			}
		return null;
	}*/
	
	public boolean onTouchEvent(MotionEvent event){
		synchronized (thread.getSurfaceHolder()){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				// touchStart records when the use pressing the screen.
				touchStart.setX(event.getX());
				touchStart.setY(event.getY());
			} else if (event.getAction() == MotionEvent.ACTION_MOVE){
				// move is a class storing where the screen is moving when a user drags the screen
				tollDataView.getMove().setX(event.getX()-touchStart.getX());
				tollDataView.getMove().setY(event.getY()-touchStart.getY());
				tollDataView.checkMove();
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				tollDataView.resetMove();
			}
			return true;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		boolean retry = true;
		thread.setRunning(false);
		while (retry){
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e){
				// keep trying until it's finished
			}
		}
	}

}
