/**
 * 
 */
package com.bg.oztoll;

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
	private Coordinates origin[], move, touchStart, screenOrigin;
	
	public void setDataFile(OzTollData tollData){
		this.tollData = tollData;
		
		origin=tollData.getMapLimits();
	}
	
	public OzTollView(Context context) {
		super(context);
		getHolder().addCallback(this);
		
		thread = new DrawingThread(getHolder(), this);
		
		setFocusable(true);
		
		move = new Coordinates(0,0);
		touchStart = new Coordinates(0,0);
		screenOrigin = new Coordinates(0,0);
	}
	
	public float drawX(float mapPointX){
		return ((mapPointX*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX();
	}
	
	public float drawY(float mapPointY){
		return (mapPointY*50)+15+move.getY()+screenOrigin.getY();
	}
	
	public void OnDraw(Canvas canvas){
		Paint map = new Paint();
		map.setColor(Color.WHITE);
		map.setStrokeWidth(5 / getResources().getDisplayMetrics().density);
		Paint name = new Paint();
		name.setColor(Color.BLUE);
		Paint canvasColor = new Paint();
		canvasColor.setColor(Color.BLACK);
		
		canvas.drawColor(Color.BLACK);

		if (tollData!=null){
			if (tollData.getTollwayCount()>0){
				/* This is used to get the font size on the screen, which is used for displaying the
				 * road names vertically on the map
				 */
				float fontSize = name.getFontMetrics().bottom - name.getFontMetrics().top -2;
				
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int twi=0; twi<tollData.getStreetCount(twc); twi++){

						// This makes sure the user does not move the screen too far to the west of the map, loosing the screen
						// Need to make similar ones for north south and east
						canvas.drawText("Tulla.X :"+drawX(tollData.getStreetX(0, 0)),0,130, name);
						canvas.drawText("Origin[0].X :"+drawX(origin[0].getX()),0,150, name);
						canvas.drawText("Origin[1].X :"+drawX(origin[1].getX()),0,170, name);
						canvas.drawText("Width :"+getWidth(),0,190, name);
						
						
						//if ((((origin[0].getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX())>getWidth())
						//	move.setX(getWidth()-((origin[0].getX()*70)+((getWidth()/2)-10)+screenOrigin.getX()));
						//If the user moves the map to the east 
						//else if ((((origin[1].getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX())<0)
						//	move.setX(0-((origin[1].getX()*70)+(getWidth()/2)-10)+screenOrigin.getX());
						//canvas.drawText("West X="+(((origin[1].getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX()), 0, 300, name);
						//canvas.drawText("Move X="+(origin[1].getX()*70), 0, 320, name);
						
						// The following draws the circle for the exit on the screen
						Coordinates street = new Coordinates (
								drawX(tollData.getStreetX(twc, twi)),
								drawY(tollData.getStreetY(twc, twi)));
						// The following tests to see if the street coordinates are on the screen, if it is, it will draw it.
						if ((street.getX()>-10)&&(street.getX()<getWidth()+10)&&(street.getY()>-10)&&(street.getY()<getHeight()+10)){
							canvas.drawCircle(street.getX(), street.getY(), 10, map);
						
							/* The following gets the street name, and depending on the location stored in the
							 * xml file, it then draws the text on the screen.
							 */
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
					}
					// Draw the road
					for (int pwc=0; pwc<tollData.getPathwayCount(twc); pwc++){
						Pathway currentPathway = tollData.getPathway(twc, pwc);
						canvas.drawLine(((currentPathway.getStart().getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
										(currentPathway.getStart().getY()*50)+15+move.getY()+screenOrigin.getY(),
										((currentPathway.getEnd().getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
										(currentPathway.getEnd().getY()*50)+15+move.getY()+screenOrigin.getY(),
										map);
					}
					
					// Draw the connecting road between tollways
					for (int cpc=0; cpc<tollData.getConnectionCount(); cpc++){
						Connection currentConnection = tollData.getConnection(cpc);
						canvas.drawLine(((currentConnection.getStart().getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
								(currentConnection.getStart().getY()*50)+15+move.getY()+screenOrigin.getY(),
								((currentConnection.getEnd().getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
								(currentConnection.getEnd().getY()*50)+15+move.getY()+screenOrigin.getY(),
								map);						
					}
				}
			}
		}
	}
	
	
	public boolean onTouchEvent(MotionEvent event){
		synchronized (thread.getSurfaceHolder()){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				// touchStart records when the use pressing the screen.
				touchStart.setX(event.getX());
				touchStart.setY(event.getY());
			} else if (event.getAction() == MotionEvent.ACTION_MOVE){
				// move is a class storing where the screen is moving when a user drags the screen
				move.setX(event.getX()-touchStart.getX());
				move.setY(event.getY()-touchStart.getY());
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				/* When the user takes their finger off the screen, screenOrigin is used to keep a record
				 * of where the screen is moved to, so the user can view the whole map
				 */
				screenOrigin.updateX(move.getX());
				screenOrigin.updateY(move.getY());
				// reset is called so the screen does not magically move after letting go, because if we dont reset it,
				// an extra move will be added to the screen
				move.reset();
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
