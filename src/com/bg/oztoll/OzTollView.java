/**
 * 
 */
package com.bg.oztoll;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.widget.Toast;

/**
 * @author bugman
 *
 */
public class OzTollView extends SurfaceView implements SurfaceHolder.Callback {
	private OzTollData tollData = null;
	private DrawingThread thread;
	private int originX, originY;
	private boolean updateScreen = true;
	
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

		/*
		Toast.makeText(this.getContext(),
        		"Width: "+this.getWidth(),
        		Toast.LENGTH_LONG).show();
		*/
	}
	
	public void OnDraw(Canvas canvas){
		Paint point = new Paint();
		point.setColor(Color.BLUE);
		
		//if (updateScreen){
			canvas.drawColor(Color.BLACK);
			
			if (tollData!=null){
				boolean left=false;
				int lastx=0, lasty=0;
				for (int twc=0; twc<tollData.getTollwayCount(); twc++)
					for (int twi=0; twi<tollData.getTollCount(twc); twi++){
						int streetx = ((tollData.getStreetX(twc, twi)-originX)/500)+((getWidth()/2)-10);
						int streety = ((originY-tollData.getStreetY(twc, twi))/500)+30;
						if ((lastx!=0)&&(lasty!=0)){
							if ((streetx>lastx-10)&&(streetx<lastx+10)){
								streety+=30;
							}
						}
						canvas.drawCircle(streetx, streety , 4, point);
						if (left){
							point.setTextAlign(Paint.Align.LEFT);
							canvas.drawText(tollData.getStreetName(twc, twi), streetx+32 , streety, point);
							left=false;
						} else {
							point.setTextAlign(Paint.Align.RIGHT);
							canvas.drawText(tollData.getStreetName(twc, twi), streetx-32 , streety, point);
							left=true;
						}
						lastx=streetx;
						lasty=streety;
					}
				updateScreen=false;
			}
		//}
	}
	
	
	public boolean onTouchEvent(MotionEvent event){
		
		return true;
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
