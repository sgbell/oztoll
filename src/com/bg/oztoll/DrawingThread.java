package com.bg.oztoll;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class DrawingThread extends Thread {
	private OzTollView mainPanel;
	private SurfaceHolder surfaceHolder;
	private boolean run;
	
	public DrawingThread(SurfaceHolder surface, OzTollView panel){
		surfaceHolder = surface;
		mainPanel = panel;
	}
	
	public SurfaceHolder getSurfaceHolder(){
		return surfaceHolder;
	}
	
	public void setRunning (boolean run){
		this.run = run;
	}
	
	public void run(){
		Canvas c;
		while (run){
			c = null;
			try {
				c = surfaceHolder.lockCanvas(null);
				synchronized (surfaceHolder){
					mainPanel.OnDraw(c);
				}
			} finally {
				if (c != null){
					surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}
