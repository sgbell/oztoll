/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class OzTollView extends SurfaceView implements SurfaceHolder.Callback {
	private OzTollData tollData = null;
	private DrawingThread thread;
	private Thread tollDataViewBuilder;
	private Coordinates touchStart, origin, imageLocation;
	private TollDataView tollDataView;
	private Object syncObject, dataSync;
	private Handler mainHandler;
	float   screenXMultiplier=0,
			screenYMultiplier=0,
			originalXMultiplier=0,
			originalYMultiplier=0,
			oldDist = 1f,
			totalScale = 1.0f,
			lastScale;
	private ArrayList<Coordinates> eventCoords;
	
	private boolean fullMapLoaded=false; 
	
	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	
	private Dialog rateDialog;
	private boolean rateShown=false,
					textSizeAdjusted=false,
					scaleChanged=false;
	
	public void setDataFile(OzTollData tollData){
		this.tollData = tollData;

		dataSync = tollData.getDataSync();
		// following line is causing the crash
		tollDataView = new TollDataView(tollData, getHeight(), getWidth(), getContext());
		//tollDataView.resizeView(getResources().getDisplayMetrics().density);
		syncObject = tollDataView.getSync();
		tollDataView.setMainHandler(mainHandler);
		tollDataViewBuilder = new Thread(tollDataView);
		tollDataViewBuilder.setName("TollDataView");
		tollDataViewBuilder.start();
	}
	
	public OzTollView(Context context){
		super(context);
		getHolder().addCallback(this);
		
		thread = new DrawingThread(getHolder(), this);
		thread.setName("Drawing Thread");
		
		setFocusable(true);
		
		touchStart = new Coordinates(0,0);
		imageLocation = new Coordinates(0,0);
		origin = new Coordinates(0,0);
		eventCoords = new ArrayList<Coordinates>();
	}
	
	public OzTollView(Context context, OzTollData tollData, Handler handler) {
		this(context);
		mainHandler = handler;

		// This is causing it to crash
		setDataFile(tollData);
	}
	
	public void setHandler(Handler handler){
		mainHandler = handler;
	}
	
	/**
	 * This method Overrides the onSizeChanged, so we can make calculations for canvas size,
	 * and text size.
	 */
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		/*
		if (getWidth()>getHeight()){
			tollDataView.setXMultiplier((float)getWidth()/381);
			tollDataView.setYMultiplier((float)getHeight()/240);
		} else {
			tollDataView.setXMultiplier((float)getWidth()/240);
			tollDataView.setYMultiplier((float)getHeight()/381);
		}
		tollDataView.resizeView(getResources().getDisplayMetrics().density);
		*/
	}
	
	public void OnDraw(Canvas canvas){
		if (canvas!=null){
			synchronized(syncObject){
				canvas.save();
				
				canvas.scale(totalScale, totalScale);
				canvas.drawColor(Color.WHITE);
			
				
				//canvas.drawBitmap(tollDataView.getTollwayMap(), ((imageLocation.getX()+origin.getX())*totalScale), ((imageLocation.getY()+origin.getY())*totalScale), null);
				
				if (!tollData.isFinished()){
					Message newMessage = mainHandler.obtainMessage();
					newMessage.what=5;
					mainHandler.sendMessage(newMessage);
					try {
						dataSync.notify();
					} catch (IllegalMonitorStateException e){
						// just so it wont crash
					}
				} else {
					Message newMessage = mainHandler.obtainMessage();
					newMessage.what=6;
					mainHandler.sendMessage(newMessage);
				}
				synchronized (tollDataView){
					/*
					if ((tollDataView.isRateCalculated())&&(!rateShown)){
						Message msg = mainHandler.obtainMessage();
						msg.what=3;
						LinearLayout rateLayout = tollDataView.getRateDialog();
						msg.obj=rateLayout;	
						
						mainHandler.sendMessage(msg);
						rateShown=true;
					}
					// Added the following if statement, so that if the user deselects the end street, and the dialog has been shown
					// it will reset the value that tells the system the rate has been shown.
					
					if ((tollDataView.getEnd()==null)&&(rateShown)){
						rateShown=false;
					}*/
				}

				canvas.restore();
			}
		}
	}
	
	public boolean onTouchEvent(MotionEvent event){
		synchronized (thread.getSurfaceHolder()){
			switch (event.getAction() & MotionEvent.ACTION_MASK){
				case MotionEvent.ACTION_DOWN:
					mode=DRAG;
					// touchStart records when the use pressing the screen.
					touchStart.setX(event.getX());
					touchStart.setY(event.getY());
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode==DRAG){
						Coordinates newCoords = new Coordinates();
						newCoords.setX(event.getX()-touchStart.getX());
						newCoords.setY(event.getY()-touchStart.getY());
						if (eventCoords.size()>1){
							eventCoords.remove(0);
						}
						// event Coords will be used to calculate drift later
						eventCoords.add(newCoords);
						// This should move the image around
						imageLocation.setX(newCoords.getX());
						imageLocation.setY(newCoords.getY());
					} else if (mode==ZOOM){
						float newDist = spacing(event);
						if (newDist > 10f){
							float scale = newDist / oldDist;
							lastScale = (1f - scale)/10;
							// totalScale is the scale overall, before the user adjusts the scale with the pinch zoom.
							totalScale = totalScale-lastScale;
							if (totalScale < 0.5f){
								totalScale = 0.5f;
								oldDist=newDist;
							} else if (totalScale > 3f){
								totalScale = 3f;
								oldDist=newDist;
							}
						}
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					// Need to figure out if where the user pressed is a street
					// If map is only dragged like 3 pixels the app will treat it as 
					// a map selection.
					if ((imageLocation.getX()>-3)&&(imageLocation.getX()<3)&&
						(imageLocation.getY()>-3)&&(imageLocation.getY()<3)&&
						(mode==DRAG))
						//tollDataView.findStreet(touchStart,origin);
					
					origin.updateX(imageLocation.getX());
					origin.updateY(imageLocation.getY());
					imageLocation.reset();
					eventCoords= new ArrayList<Coordinates>();
					mode = NONE;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					lastScale=1f;
					if (oldDist > 10f){
						mode=ZOOM;
					}
					break;
			}

			return true;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Not using this
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		try {
			thread.start();
		} catch (IllegalThreadStateException e){
			thread = new DrawingThread(getHolder(), this);
			thread.setRunning(true);
			thread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

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
		//tollDataView.getTollwayMap().recycle();
	}

	public Dialog getRateDialog(){
		return rateDialog;
	}
	
	public void setRateDialog(Dialog newDialog){
		rateDialog = newDialog;
	}
	/*
	public void reset() {
		synchronized (tollDataView){
			tollDataView.setEnd(null);
			tollDataView.setStart(null);
			tollDataView.resetPaths();
			tollDataView.setRateCalculated(false);
			rateShown=false;
		}
	}*/
	
	private float spacing(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x*x+y*y);
	}
}