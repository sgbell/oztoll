package com.bg.oztoll;

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
		
	}
}
