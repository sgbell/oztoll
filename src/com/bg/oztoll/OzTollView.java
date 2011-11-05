/**
 * 
 */
package com.bg.oztoll;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;

/**
 * @author bugman
 *
 */
public class OzTollView extends SurfaceView implements SurfaceHolder.Callback {

	public OzTollView(Context context) {
		super(context);
		getHolder().addCallback(this);
		setFocusable(true);
	}
	
	public void onDraw(Canvas canvas){
		canvas.drawColor(Color.BLACK);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}
