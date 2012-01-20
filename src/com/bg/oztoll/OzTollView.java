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
	private float originX, originY,
				  viewx, viewy,
				  touchStartX, touchStartY;
	
	public void setDataFile(OzTollData tollData){
		this.tollData = tollData;
		originX = tollData.getOriginX();
		originY = tollData.getOriginY();
	}
	
	public OzTollView(Context context) {
		super(context);
		getHolder().addCallback(this);
		
		thread = new DrawingThread(getHolder(), this);
		
		setFocusable(true);
		
		viewx=viewy=0;
		touchStartX=touchStartY=0;
		
	}
	
	public void OnDraw(Canvas canvas){
		Paint point = new Paint();
		point.setColor(Color.BLUE);
		Paint canvasColor = new Paint();
		canvasColor.setColor(Color.BLACK);
		
		canvas.drawColor(Color.BLACK);

		canvas.drawText(viewx+","+viewy, 0, 150, point);
		canvas.drawText(touchStartX+","+touchStartY, 0, 300, point);
		canvas.drawText("Num of Tollways: "+tollData.getTollwayCount(), 0, 20, point);
		
		if ((tollData!=null)&&(tollData.getTollwayCount()>0)){
			boolean left=false;
			for (int twc=0; twc<tollData.getTollwayCount(); twc++)
				for (int twi=0; twi<tollData.getTollCount(twc); twi++){
					float streetx = ((tollData.getStreetX(twc, twi)-originX)/500)+((getWidth()/2)-10);
					float streety = ((originY-tollData.getStreetY(twc, twi))/200)+30;
						
					canvas.drawCircle(streetx, streety, 4, point);
					if (left){
						point.setTextAlign(Paint.Align.LEFT);
						canvas.drawText(tollData.getStreetName(twc, twi), streetx+15 , streety, point);
						left=false;
					} else {
						point.setTextAlign(Paint.Align.RIGHT);
						canvas.drawText(tollData.getStreetName(twc, twi), streetx-15 , streety, point);
						left=true;
					}
				}
		}
	}
	
	
	public boolean onTouchEvent(MotionEvent event){
		synchronized (thread.getSurfaceHolder()){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				touchStartX = event.getX();
				touchStartY = event.getY();
			} else if (event.getAction() == MotionEvent.ACTION_MOVE){
				viewx=event.getX()-touchStartX;
				viewy=event.getY()-touchStartY;				
			} //else if (event.getAction() == MotionEvent.ACTION_UP){
			//}
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
