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

		/*
		canvas.drawText("Moving : "+move.getX()+","+move.getY(), 0, 150, point);
		canvas.drawText("Start : "+touchStart.getX()+","+touchStart.getY(), 0, 190, point);
		canvas.drawText("Map Pos : "+screenOrigin.getX()+","+screenOrigin.getY(), 0, 170, point);
			*/	
		if (tollData!=null){
			if (tollData.getTollwayCount()>0){
				//boolean left = false;
				for (int twc=0; twc<tollData.getTollwayCount(); twc++){
					for (int twi=0; twi<tollData.getStreetCount(twc); twi++){
						Coordinates street = new Coordinates (
								((tollData.getStreetX(twc, twi)*70)+(getWidth()/2)-10)+move.getX()+screenOrigin.getX(),
								(tollData.getStreetY(twc, twi)*50)+15+move.getY()+screenOrigin.getY());
						canvas.drawCircle(street.getX(), street.getY(), 10, map);
						
						float txtWidth = name.measureText(tollData.getStreetName(twc, twi));
						switch (tollData.getLocation(twc, twi)){
							case 1:
								canvas.drawText(tollData.getStreetName(twc, twi), street.getX()+10 , street.getY()-20, name);
								break;
							case 2:
								canvas.drawText(tollData.getStreetName(twc, twi), street.getX()-(txtWidth+10) , street.getY()+25, name);
								break;
							case 3:
								canvas.drawText(tollData.getStreetName(twc, twi), street.getX()+10 , street.getY()+25, name);
								break;
							case 0:
							default:
								canvas.drawText(tollData.getStreetName(twc, twi), street.getX()-(txtWidth+10) , street.getY()-20, name);
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
					//canvas.drawText("Connections Name: "+tollData.connectionsTest, 0, 150, point);
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
