/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
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
	private Coordinates touchStart;
	private TollDataView tollDataView;
	private Paint map, name, mapSelected;
	private Object syncObject, dataSync;
	private Context appContext;
	private Handler mainHandler;
	float   screenXMultiplier=0,
			screenYMultiplier=0,
			originalXMultiplier=0,
			originalYMultiplier=0,
			oldDist = 1f;
	private ArrayList<Coordinates> eventCoords;
	
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
		tollDataView = new TollDataView(tollData, getHeight(), getWidth(), getContext());
		syncObject = tollDataView.getSync();
		tollDataViewBuilder = new Thread(tollDataView);
		tollDataViewBuilder.setName("TollDataView");
		tollDataViewBuilder.start();
	}
	
	public OzTollView(Context context, OzTollData tollData, Handler handler) {
		super(context);
		getHolder().addCallback(this);
		mainHandler = handler;
		
		thread = new DrawingThread(getHolder(), this);
		thread.setName("Drawing Thread");
		
		setFocusable(true);
		
		touchStart = new Coordinates(0,0);
		eventCoords = new ArrayList<Coordinates>();
		
		map = new Paint();
		name = new Paint();
		mapSelected = new Paint();
		map.setColor(Color.BLACK);
		name.setColor(Color.BLACK);
		mapSelected.setColor(Color.GREEN);

		setDataFile(tollData);
	}

	/**
	 * This method Overrides the onSizeChanged, so we can make calculations for canvas size,
	 * and text size.
	 */
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		super.onSizeChanged(w, h, oldw, oldh);
		
		tollDataView.setWidth(getWidth());
		tollDataView.setHeight(getHeight());
		if (getWidth()>getHeight()){
			originalXMultiplier=screenXMultiplier=(float)getWidth()/381;
			originalYMultiplier=screenYMultiplier=(float)getHeight()/240;
		} else {
			originalXMultiplier=screenXMultiplier=(float)getWidth()/240;
			originalYMultiplier=screenYMultiplier=(float)getHeight()/381;
		}
		
		tollDataView.setXMultiplier(screenXMultiplier);
		tollDataView.setYMultiplier(screenYMultiplier);
		map.setStrokeWidth((6*screenXMultiplier) / getResources().getDisplayMetrics().density);
		mapSelected.setStrokeWidth((float)(3*screenXMultiplier) / getResources().getDisplayMetrics().density);

		synchronized (syncObject){
			tollDataView.resetScreenOrigin();
			syncObject.notify();
		}
		
		textSizeAdjusted=false;
	}
	
	public void adjustTextSize (){
		name = new Paint();
		float textSize = name.getTextSize();
		// setting correct text size for screen
		//Log.w ("ozToll","tollData.getStreetName(0,0) :"+tollData.getStreetName(0, 0));
		name.setTextSize(100);
		name.setTextScaleX(1.0f);
		Rect bounds = new Rect();
		name.getTextBounds(tollData.getStreetName(0, 0), 0, tollData.getStreetName(0, 0).length(), bounds);
		int textHeight = bounds.bottom-bounds.top;
		//Log.w("ozToll", "adjustTextSize :"+textHeight);
				
		// Setting Text Height
		Log.w("ozToll", "tollData.adjustTextSize().setTextSize :"+(textSize*screenXMultiplier/(float)textHeight)*100f);
		name.setTextSize(((float)(textSize*screenXMultiplier)/(float)textHeight)*100f);

		textSizeAdjusted=true;
	}
	
	public void OnDraw(Canvas canvas){
		canvas.drawColor(Color.WHITE);
		
		if (tollDataView!= null){
			if ((tollData!=null)&&(tollData.getTollwayCount()>0)&&(tollData.getStreetCount(0)>0)&&(!textSizeAdjusted)){
				//Log.w ("ozToll","adjusting Text Size");
				adjustTextSize();
			}
			
			/* This is used to get the font size on the screen, which is used for displaying the
			 * road names vertically on the map
			 * 
			 * origin = screenSize(240)/2-10
			 */
			
			synchronized (syncObject){
				if (scaleChanged){
					tollDataView.setXMultiplier(screenXMultiplier);
					tollDataView.setYMultiplier(screenYMultiplier);
					map.setStrokeWidth((6*screenXMultiplier) / getResources().getDisplayMetrics().density);
					mapSelected.setStrokeWidth((float)(3*screenXMultiplier) / getResources().getDisplayMetrics().density);
					adjustTextSize();
					scaleChanged=false;
				}

				// streets is an array generated by the tollDataView thread.
				ArrayList<Street> streets = tollDataView.getStreets();
				
				// currently streets is empty. :(
				if (streets!=null){
					/*
					 * Original Layout:
					 * Screen Size: 240,381
					 * 
					 * Layout on X10:
					 * Screen Size: 480,816 
					 * 
					 */
					for (int sc=0; sc < streets.size(); sc++){
						// currentStreet contains the coordinates of the street, converted to it's onscreen location
						Coordinates currentStreet = new Coordinates(
								tollDataView.drawX(streets.get(sc).getX()),
								tollDataView.drawY(streets.get(sc).getY()));
						
						if ((streets.get(sc)==tollDataView.getStart())||
							(streets.get(sc)==tollDataView.getEnd())){
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 10*screenXMultiplier, map);
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 6*screenXMultiplier, mapSelected);
						} else if ((streets.get(sc).isValid())||(tollDataView.getStart()==null))
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 10*screenXMultiplier, map);
						
						String streetName = streets.get(sc).getName();
						float txtWidth = name.measureText(streetName);
						if ((tollDataView.getStart()==null)||
							(streets.get(sc).isValid())||
							(streets.get(sc)==tollDataView.getStart())||
							(streets.get(sc)==tollDataView.getEnd())){
							switch (streets.get(sc).getLocation()){
							case 1:
								// Draws to the right of the street
								canvas.drawText(streetName, currentStreet.getX()+(20*screenXMultiplier) , currentStreet.getY()+(5*screenYMultiplier), name);
								break;
							case 2:
								// Draws the text vertically below the street
								canvas.drawText(streetName, currentStreet.getX()-(txtWidth/2), currentStreet.getY()+(25*screenYMultiplier), name);								
								break;
							case 3:
								// Draws the text vertically above the street
								canvas.drawText(streetName, currentStreet.getX()-(txtWidth/2), currentStreet.getY()-(20*screenYMultiplier), name);
								break;
							case 0:
							default:
								// Draws the text to the left of the street
								canvas.drawText(streetName, currentStreet.getX()-(txtWidth+(20*screenXMultiplier)), currentStreet.getY()+(5*screenYMultiplier), name);
								break;								
							}
						}
					}
				}
				ArrayList<Pathway> paths = tollDataView.getPaths();
				if (paths!=null){
					for (int pc=0; pc< paths.size(); pc++){
						Pathway currentPathway = paths.get(pc);
						if (tollDataView.getStart()!=null){
							// The current if statement covers the vertical lines
							if (currentPathway.getStart().getX()==currentPathway.getEnd().getX()){
								/* If the current spot is an invalid exit for the selected starting point, and is not the start
								 * or finish, this if statement will replace the bottom half of a circle with a line.
								 */
								if ((!currentPathway.getStart().isValid())&&
									(currentPathway.getStart()!=tollDataView.getStart())&&
									(currentPathway.getStart()!=tollDataView.getEnd()))
										drawLine(tollDataView.drawX(currentPathway.getStart().getX()),
												(tollDataView.drawY(currentPathway.getStart().getY())-(10*screenYMultiplier)),
												tollDataView.drawX(currentPathway.getStart().getX()),
												(tollDataView.drawY(currentPathway.getStart().getY())+(20*screenYMultiplier)),
												map,
												canvas,false);
								/* If the current spot is an invalid exit for the selected starting point, and is not the start
								 * or finish, this if statement will replace the top half of a circle with a line. 
								 */
								if ((!currentPathway.getEnd().isValid())&&
									(currentPathway.getEnd()!=tollDataView.getStart())&&
									(currentPathway.getEnd()!=tollDataView.getEnd()))
										drawLine(tollDataView.drawX(currentPathway.getEnd().getX()),
												(tollDataView.drawY(currentPathway.getEnd().getY())-(20*screenYMultiplier)),
												tollDataView.drawX(currentPathway.getEnd().getX()),
												(tollDataView.drawY(currentPathway.getEnd().getY())+(10*screenYMultiplier)),
												map,
												canvas,false);
							} else if (currentPathway.getStart().getY()==currentPathway.getEnd().getY()){
								// Horizontal Roads, marking the map when a street is gone
								/* If the current spot is an invalid exit for the selected starting point, and is not the start
								 * or finish, this if statement will replace the right half of a circle with a line.
								 */
								if ((!currentPathway.getStart().isValid())&&
									(currentPathway.getStart()!=tollDataView.getStart())&&
									(currentPathway.getStart()!=tollDataView.getEnd()))
										drawLine((tollDataView.drawX(currentPathway.getStart().getX())-(10*screenXMultiplier)),
												tollDataView.drawY(currentPathway.getStart().getY()),
												(tollDataView.drawX(currentPathway.getStart().getX())+(20*screenXMultiplier)),
												tollDataView.drawY(currentPathway.getStart().getY()),
												map,
												canvas,false);
								/* If the current spot is an invalid exit for the selected starting point, and is not the start
								 * or finish, this if statement will replace the left half of a circle with a line.
								 */
								if ((!currentPathway.getEnd().isValid())&&
									(currentPathway.getEnd()!=tollDataView.getStart())&&
									(currentPathway.getEnd()!=tollDataView.getEnd()))
										drawLine(((tollDataView.drawX(currentPathway.getEnd().getX()))-(20*screenXMultiplier)),
												tollDataView.drawY(currentPathway.getEnd().getY()),
												(tollDataView.drawX(currentPathway.getEnd().getX())+(10*screenXMultiplier)),
												tollDataView.drawY(currentPathway.getEnd().getY()),
												map,
												canvas,false);
							} 
						}
						drawLine((tollDataView.drawX(currentPathway.getStart().getX())),
								 (tollDataView.drawY(currentPathway.getStart().getY())),
								 (tollDataView.drawX(currentPathway.getEnd().getX())),
								 (tollDataView.drawY(currentPathway.getEnd().getY())),
								 map,
								 canvas,false);
						if (currentPathway.isRoute())
							drawLine((tollDataView.drawX(currentPathway.getStart().getX())),
									 (tollDataView.drawY(currentPathway.getStart().getY())),
									 (tollDataView.drawX(currentPathway.getEnd().getX())),
									 (tollDataView.drawY(currentPathway.getEnd().getY())),
									 mapSelected,
									 canvas,true);
					}
				}
			}
			synchronized (tollDataView){
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
				}
			}
			synchronized (tollDataView.getMoveSync()){
				tollDataView.getMoveSync().notify();
			}
		}
	}
	
	/** Draw line is a wrapper for Canvas.drawLine so it will only draw the line if it's
	 * within the visible space. 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param paint
	 * @param canvas
	 * @param markedRoad 
	 */
	public void drawLine(float startX, float startY, float endX, float endY, Paint paint, Canvas canvas, boolean markedRoad){
		if ((((startX>0)&&(startX<getWidth()))||
			 ((endX>0)&&(endX<getWidth())))&&
			 (((startY>0)&&(startY<getHeight()))||
			 ((endY>0)&&(endY<getHeight())))){
			if (!markedRoad){
				if (startX==endX){
					startY+=((10*screenYMultiplier)-(1*screenYMultiplier));
					endY-=((10*screenYMultiplier)-(1*screenYMultiplier));
				}
				if (startY==endY){
					startX+=((10*screenXMultiplier)-(1*screenXMultiplier));
					endX-=((10*screenXMultiplier)-(1*screenXMultiplier));
				}
			}
			canvas.drawLine(startX,	startY,	endX, endY,	paint);
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
						eventCoords.add(newCoords);
						// move is a class storing where the screen is moving when a user drags the screen
						tollDataView.getMove().setX(event.getX()-touchStart.getX());
						tollDataView.getMove().setY(event.getY()-touchStart.getY());
						tollDataView.checkMove();
					} else if (mode==ZOOM){
						float newDist = spacing(event);
						if (newDist > 10f){
							float scale = newDist / oldDist;
							if (scale < 0.5f){
								scale = 0.5f;
							} else if (scale > 3f){
								scale = 3f;
							}
							screenXMultiplier=originalXMultiplier*scale;
							screenYMultiplier=originalYMultiplier*scale;
							Log.d("Zoom", "Scale: "+scale+"X");
							scaleChanged=true;
						}
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					if ((tollDataView.getMove().getX()>-3)&&(tollDataView.getMove().getX()<3)&&
							(tollDataView.getMove().getY()>-3)&&(tollDataView.getMove().getY()<3)&&
							(mode==DRAG)){
							tollDataView.findStreet(touchStart,name);
					}
					tollDataView.resetMove(eventCoords);
					eventCoords= new ArrayList<Coordinates>();
					mode = NONE;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
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
		// TODO Auto-generated method stub
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
	}

	public Dialog getRateDialog(){
		return rateDialog;
	}
	
	public void setRateDialog(Dialog newDialog){
		rateDialog = newDialog;
	}
	
	public void reset() {
		synchronized (tollDataView){
			tollDataView.setEnd(null);
			tollDataView.setStart(null);
			tollDataView.resetPaths();
			tollDataView.setRateCalculated(false);
			rateShown=false;
		}
	}
	
	private float spacing(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x*x+y*y);
	}
}
