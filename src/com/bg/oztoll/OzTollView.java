/**
 * 
 */
package com.bg.oztoll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
	private Coordinates origin;
	private Coordinates move, touchStart, screenOrigin;
	
	public void setDataFile(OzTollData tollData){
		this.tollData = tollData;
		
		origin=new Coordinates(tollData.getOriginX(),tollData.getOriginY());
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
				float fontSize = name.getFontMetrics().bottom - name.getFontMetrics().top -2;
				
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int twi=0; twi<tollData.getStreetCount(twc); twi++){

						if ((((origin.getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX())>getWidth())
							move.setX(getWidth()-((tollData.getStreetX(twc, twi)*70)+((getWidth()/2)-10)+screenOrigin.getX()));
						Coordinates street = new Coordinates (
								((tollData.getStreetX(twc, twi)*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
								(tollData.getStreetY(twc, twi)*50)+15+move.getY()+screenOrigin.getY());
						canvas.drawCircle(street.getX(), street.getY(), 10, map);
						
						String streetName = tollData.getStreetName(twc, twi);
						float txtWidth = name.measureText(streetName);
						switch (tollData.getLocation(twc, twi)){
							case 1:
								canvas.drawText(streetName, street.getX()+20 , street.getY()+5, name);
								break;
							case 2:
								for (int lc=0; lc < streetName.length(); lc++){
									canvas.drawText(""+streetName.charAt(lc), street.getX()-5, street.getY()+(fontSize*lc)+25, name);
								}
								break;
							case 3:
								for (int lc=0; lc < streetName.length(); lc++){
									canvas.drawText(""+streetName.charAt(lc), street.getX()-3, street.getY()-((streetName.length()-lc)*fontSize), name);
								}
								break;
							case 0:
							default:
								canvas.drawText(streetName, street.getX()-(txtWidth+20) , street.getY()+5, name);
								break;								
						}
					}
					for (int pwc=0; pwc<tollData.getPathwayCount(twc); pwc++){
						Pathway currentPathway = tollData.getPathway(twc, pwc);
						canvas.drawLine(((currentPathway.getStart().getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
										(currentPathway.getStart().getY()*50)+15+move.getY()+screenOrigin.getY(),
										((currentPathway.getEnd().getX()*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
										(currentPathway.getEnd().getY()*50)+15+move.getY()+screenOrigin.getY(),
										map);
					}
					
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
				touchStart.setX(event.getX());
				touchStart.setY(event.getY());
			} else if (event.getAction() == MotionEvent.ACTION_MOVE){
				move.setX(event.getX()-touchStart.getX());
				move.setY(event.getY()-touchStart.getY());
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				screenOrigin.updateX(move.getX());
				screenOrigin.updateY(move.getY());
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
