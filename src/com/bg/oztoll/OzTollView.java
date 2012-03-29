/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

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
	private Paint map, name, mapDeactivated, mapSelected;
	private Object syncObject, dataSync;
	
	private Dialog rateDialog;
	private TextView rateDialogText;
	private boolean rateShown=false;
	
	public void setDataFile(OzTollData tollData){
		this.tollData = tollData;

		dataSync = tollData.getDataSync();
		new Thread(tollData).start();
		tollDataView = new TollDataView(this.tollData, getHeight(), getWidth());
		syncObject = tollDataView.getSync();
		tollDataViewBuilder = new Thread(tollDataView);
		tollDataViewBuilder.start();
	}
	
	public OzTollView(Context context) {
		super(context);
		getHolder().addCallback(this);
		
		thread = new DrawingThread(getHolder(), this);
		
		setFocusable(true);
		
		touchStart = new Coordinates(0,0);
		
		map = new Paint();
		name = new Paint();
		mapDeactivated = new Paint();
		mapSelected = new Paint();
		map.setColor(Color.WHITE);
		map.setStrokeWidth(5 / getResources().getDisplayMetrics().density);
		name.setColor(Color.BLUE);
		mapDeactivated.setColor(Color.GRAY);
		mapSelected.setColor(Color.BLACK);
		mapSelected.setStrokeWidth((float)2.0 / getResources().getDisplayMetrics().density);
		
		// Creating the rateDialog here which will be accessed from one of the other
		rateDialog = new Dialog(context);
		rateDialog.setContentView(R.layout.ratedialog);
		setRateDialogText((TextView) rateDialog.findViewById(R.id.mainText));
		rateDialog.setTitle("Trip Toll Result");
		Button closeButton = (Button) rateDialog.findViewById(R.id.close);
		closeButton.setText("Close");
		closeButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				rateDialog.dismiss();
				((ScrollView)rateDialog.findViewById(R.id.scrollView)).fullScroll(FOCUS_UP);
			}
		});
	}

	public void OnDraw(Canvas canvas){
		canvas.drawColor(Color.BLACK);
		
		if (tollDataView!= null){
			tollDataView.setWidth(getWidth());
			tollDataView.setHeight(getHeight());
			/* This is used to get the font size on the screen, which is used for displaying the
			 * road names vertically on the map
			 */
			float fontSize = name.getFontMetrics().bottom - name.getFontMetrics().top -2;
			
			synchronized (syncObject){
				ArrayList<Street> streets = tollDataView.getStreets();
				
				// currently streets is empty. :(
				if (streets!=null){
					for (int sc=0; sc < streets.size(); sc++){
						Coordinates currentStreet = new Coordinates(
								tollDataView.drawX(streets.get(sc).getX()),
								tollDataView.drawY(streets.get(sc).getY()));
					
						if ((streets.get(sc)==tollDataView.getStart())||
							(streets.get(sc)==tollDataView.getEnd())){
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 10, map);
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 6, mapSelected);
						} else if ((streets.get(sc).isValid())||(tollDataView.getStart()==null))
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 10, map);
						else
							canvas.drawCircle(currentStreet.getX(), currentStreet.getY(), 10, mapDeactivated);
						
						String streetName = streets.get(sc).getName();
						float txtWidth = name.measureText(streetName);
						switch (streets.get(sc).getLocation()){
							case 1:
								// Draws to the right of the street
								canvas.drawText(streetName, currentStreet.getX()+20 , currentStreet.getY()+5, name);
								break;
							case 2:
								// Draws the text vertically below the street
								for (int lc=0; lc < streetName.length(); lc++){
									canvas.drawText(""+streetName.charAt(lc), currentStreet.getX()-5, currentStreet.getY()+(fontSize*lc)+25, name);
								}
								break;
							case 3:
								// Draws the text vertically above the street
								for (int lc=0; lc < streetName.length(); lc++){
									canvas.drawText(""+streetName.charAt(lc), currentStreet.getX()-3, currentStreet.getY()-((streetName.length()-lc)*fontSize), name);
								}
								break;
							case 0:
							default:
								// Draws the text to the left of the street
								canvas.drawText(streetName, currentStreet.getX()-(txtWidth+20) , currentStreet.getY()+5, name);
								break;								
						}
					}
				}
				ArrayList<Pathway> paths = tollDataView.getPaths();
				if (paths!=null){
					for (int pc=0; pc< paths.size(); pc++){
						Pathway currentPathway = paths.get(pc);
						drawLine(tollDataView.drawX(currentPathway.getStart().getX()),
								 tollDataView.drawY(currentPathway.getStart().getY()),
								 tollDataView.drawX(currentPathway.getEnd().getX()),
								 tollDataView.drawY(currentPathway.getEnd().getY()),
								 map,
								 canvas,false);
						if (currentPathway.isRoute())
							drawLine(tollDataView.drawX(currentPathway.getStart().getX()),
									 tollDataView.drawY(currentPathway.getStart().getY()),
									 tollDataView.drawX(currentPathway.getEnd().getX()),
									 tollDataView.drawY(currentPathway.getEnd().getY()),
									 mapSelected,
									 canvas,true);
					}
				}
			}
			if (tollDataView.isRateCalculated()){
				this.getHandler().post( new Runnable() {

					@Override
					public void run() {
						if (!rateShown){
							rateDialogText.setText(Html.fromHtml(tollDataView.getRateDialogText()));
							rateDialog.show();
							rateShown=true;
							tollDataView.setRateCalculated(false);
						}
					}
				});
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
					startY+=10;
					endY-=10;
				}
				if (startY==endY){
					startX+=10;
					endX-=10;
				}
			}
			canvas.drawLine(startX,	startY,	endX, endY,	paint);
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
				tollDataView.getMove().setX(event.getX()-touchStart.getX());
				tollDataView.getMove().setY(event.getY()-touchStart.getY());
				tollDataView.checkMove();
			} else if (event.getAction() == MotionEvent.ACTION_UP){
				if ((tollDataView.getMove().getX()==0)&&
					(tollDataView.getMove().getY()==0)){
					tollDataView.findStreet(touchStart);
				}
				tollDataView.resetMove();
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

	/**
	 * @return the rateDialogText
	 */
	public TextView getRateDialogText() {
		return rateDialogText;
	}

	/**
	 * @param rateDialogText the rateDialogText to set
	 */
	public void setRateDialogText(TextView rateDialogText) {
		this.rateDialogText = rateDialogText;
	}

	public void reset() {
		tollDataView.setEnd(null);
		tollDataView.setStart(null);
		tollDataView.resetPaths();
		rateShown=false;
	}
}
