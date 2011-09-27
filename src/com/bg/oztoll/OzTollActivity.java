package com.bg.oztoll;

import java.util.ArrayList;

import com.bg.oztoll.OzTollActivity.GraphicObject.Coordinates;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

public class OzTollActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new Panel(this));
    }

    class Panel extends SurfaceView implements SurfaceHolder.Callback {
    	private TutorialThread _thread;
    	private ArrayList<GraphicObject> _graphics = new ArrayList<GraphicObject>();

		public Panel(Context context) {
			super(context);
			getHolder().addCallback(this);
			_thread = new TutorialThread(getHolder(),this);
			setFocusable(true);
		}
    	
		public void onDraw(Canvas canvas){
			canvas.drawColor(Color.BLACK);
			Bitmap bitmap;
			Coordinates coords;
			
			for (GraphicObject graphic: _graphics){
				bitmap = graphic.getGraphic();
				coords = graphic.getCoordinates();
				canvas.drawBitmap(bitmap, coords.getX(), coords.getY(), null);
			}
		}
		
		public void updatePhysics(){
			GraphicObject.Coordinates coord;
			GraphicObject.Speed speed;
			
			for (GraphicObject graphic : _graphics) {
				coord = graphic.getCoordinates();
				speed = graphic.getSpeed();
				
				// Direction
				if (speed.getXDirection() == GraphicObject.Speed.X_DIRECTION_RIGHT) {
					coord.setX(coord.getX()+speed.getX());
				} else {
					coord.setX(coord.getX()-speed.getX());
				}
				if (speed.getYDirection() == GraphicObject.Speed.Y_DIRECTION_DOWN) {
					coord.setY(coord.getY() + speed.getY());
				} else {
					coord.setY(coord.getY() - speed.getY());
				}
				
				// borders for x..
				if (coord.getX() < 0){
					speed.toggleXDirection();
					coord.setX(-coord.getX());
				} else if (coord.getX() + graphic.getGraphic().getWidth() > getWidth()) {
					speed.toggleXDirection();
					coord.setX(coord.getX() + getWidth() - (coord.getX() + graphic.getGraphic().getWidth()));
				}
				
				// borders for y..
				if (coord.getY() < 0){
					speed.toggleYDirection();
					coord.setY(-coord.getY());
				} else if (coord.getY() + graphic.getGraphic().getHeight() > getHeight()) {
					speed.toggleYDirection();
					coord.setY(coord.getY() + getHeight() - (coord.getY() + graphic.getGraphic().getHeight()));
				}
			}
		}
		
		public boolean onTouchEvent(MotionEvent event){
			synchronized (_thread.getSurfaceHolder()){
				if (event.getAction() == MotionEvent.ACTION_DOWN){
					GraphicObject graphic = new GraphicObject(BitmapFactory.decodeResource(getResources(),R.drawable.icon));
					graphic.getCoordinates().setX((int) event.getX() - graphic.getGraphic().getWidth()/2);
					graphic.getCoordinates().setY((int) event.getY() - graphic.getGraphic().getHeight()/2);
					_graphics.add(graphic);					
				}
				return true;				
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			_thread.setRunning(true);
			_thread.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			boolean retry = true;
			_thread.setRunning(false);
			while (retry){
				try{
					_thread.join();
					retry=false;
				} catch(InterruptedException e){
					// Just gonna keep trying.
				}
			}
		}
    }
    
    class TutorialThread extends Thread {
    	private SurfaceHolder _surfaceHolder;
    	private Panel _panel;
    	private boolean _run=false;
    	
    	public TutorialThread(SurfaceHolder surfaceHolder, Panel panel){
    		_surfaceHolder = surfaceHolder;
    		_panel = panel;
    	}
    	
    	public SurfaceHolder getSurfaceHolder(){
    		return _surfaceHolder;
    	}
    	
    	public void setRunning(boolean run){
    		_run = run;
    	}
    	
    	public void run(){
    		Canvas c;
    		
    		while (_run){
    			c = null;
    			try {
    				c = _surfaceHolder.lockCanvas(null);
    				synchronized(_surfaceHolder) {
    					_panel.updatePhysics();
    					_panel.onDraw(c);
    				}
    			} finally {
    				if (c != null){
    					_surfaceHolder.unlockCanvasAndPost(c);
    				}
    			}
    		}
    	}
    }
    
    class GraphicObject{
    	private Bitmap _bitmap;
    	private Coordinates _coordinates;
    	private Speed _speed;
    	
    	public GraphicObject(Bitmap bitmap){
    		_bitmap = bitmap;
    		_coordinates = new Coordinates();
    		_speed = new Speed();
    	}
    	
    	public Bitmap getGraphic(){
    		return _bitmap;
    	}
    	
    	public Coordinates getCoordinates(){
    		return _coordinates;
    	}
    	
    	public Speed getSpeed(){
    		return _speed;
    	}
    	
    	public class Coordinates{
    		private int _x = 100,
    					_y = 0;
    		
    		public int getX(){
    			return _x+_bitmap.getWidth()/2;
    		}
    		
    		public int getY(){
    			return _y+_bitmap.getHeight()/2;
    		}
    		
    		public void setY(int value){
    			_y = value - _bitmap.getHeight()/2;
    		}
    		
    		public void setX(int value){
    			_x = value - _bitmap.getWidth()/2;
    		}
    		
    		public String toString(){
    			return "Coordinates: (" + _x + "/" + _y + ")"; 
    		}
    	}
    	
    	public class Speed {
    		public static final int X_DIRECTION_RIGHT = 1;
    		public static final int X_DIRECTION_LEFT = -1;
    		public static final int Y_DIRECTION_DOWN = 1;
    		public static final int Y_DIRECTION_UP = -1;
    		
    		private int _x = 1;
    		private int _y = 1;
    		
    		private int _xDirection = X_DIRECTION_RIGHT;
    		private int _yDirection = Y_DIRECTION_DOWN;
    		
    		public int getXDirection(){
    			return _xDirection;
    		}
    		
    		public void toggleXDirection(){
    			if (_xDirection == X_DIRECTION_RIGHT){
    				_xDirection = X_DIRECTION_LEFT;
    			} else {
    				_xDirection = X_DIRECTION_RIGHT;
    			}
    		}
    		
    		public int getYDirection(){
    			return _yDirection;
    		}
    		
    		public void setYDirection(int direction){
    			_yDirection = direction;
    		}
    		
    		public void toggleYDirection(){
    			if (_yDirection == Y_DIRECTION_DOWN){
    				_yDirection = Y_DIRECTION_UP;
    			} else {
    				_yDirection = Y_DIRECTION_DOWN;
    			}
    		}
    		
    		public int getX(){
    			return _x;
    		}
    		
    		public void setX(int speed){
    			_x = speed;
    		}
    		
    		public int getY(){
    			return _y;
    		}
    		
    		public void setY(int speed){
    			_y = speed;
    		}
    		
    		public String toString(){
    			String xDirection;
    			if (_xDirection == X_DIRECTION_RIGHT){
    				xDirection = "right";
    			} else {
    				xDirection = "left";
    			}
    			return "Speed: x: " + _x + " | y: " + _y + " | xDirection: " + xDirection;
    		}
    	}
    	
    }
}