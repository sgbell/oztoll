/**
 * 
 */
package com.mimpidev.oztoll;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author bugman
 *
 */
public class TutorialFragment extends SherlockFragment {
	private OzTollApplication global;
	private Handler handler;
	private SharedPreferences preferences;
	
	private Button close, previous, next;
	private ImageView imageWindow;
	private Bitmap image;
	private int screenCount = 0;

	public static final String TAG = "TutorialFragment";
	
	public TutorialFragment(){
	}
	
	public TutorialFragment(Handler mainHandler){
		handler=mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);

		global = (OzTollApplication)getSherlockActivity().getApplication();
		preferences = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity().getBaseContext());
	}
	
	public void onResume(){
		super.onResume();

		global = (OzTollApplication)getSherlockActivity().getApplication();
		handler = global.getMainActivityHandler();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		View view = inflater.inflate(R.layout.tutorial_view, null, false);
		/* Build new interface here inserting r.layout.splash into r.id.tutorial
		*  On user touch, it will then remove the r.layout.splash & begin a tutorial
		*  identifying the important parts of the program including "Clear", "Settings" buttons,
		*  which streets are selected, and which are not. How to clear the screen.
		*/
		
		return view;
	}
	
	public void processView(){
		float widthScale, heightScale;
		Canvas canvas;
		Paint paint = new Paint();
    	paint.setColor(Color.RED);
    	paint.setTextAlign(Align.CENTER);

    	Point screenSize= new Point();
		getSherlockActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);

		switch (screenCount){
			case 1:
				//imageWindow.setImageResource(R.drawable.tutorial_1);
				image = BitmapFactory.decodeResource(getResources(), R.drawable.tutorial_1);
				previous.setVisibility(Button.INVISIBLE);
				break;
			case 2:
				image = BitmapFactory.decodeResource(getResources(), R.drawable.tutorial_1).copy(Bitmap.Config.ARGB_8888, true);
				previous.setVisibility(Button.VISIBLE);
				widthScale=(float)image.getWidth()/360;
				heightScale=(float)image.getHeight()/610;
		    	image = image.copy(Bitmap.Config.ARGB_8888, true);
		    	canvas = new Canvas(image);
		    	paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(paint.getStrokeWidth()+5);
		    	canvas.drawCircle((276*widthScale), (19*heightScale), (15*widthScale), paint);
		    	canvas.drawCircle((333*widthScale), (19*heightScale), (15*widthScale), paint);

		    	canvas.drawLine((276*widthScale), (19*heightScale), image.getWidth()/4, image.getHeight()/4, paint);
		    	canvas.drawLine((333*widthScale), (19*heightScale), image.getWidth()/2, image.getHeight()/4, paint);
				paint.setStrokeWidth(paint.getStrokeWidth()-5);
				
				setMarkerTextSize(screenSize.y,screenSize.x,getResources().getBoolean(R.bool.isTablet),paint);
		    	canvas.drawText("Clear", (image.getWidth()/4)-10, (image.getHeight()/4)+paint.getTextSize(), paint);
		    	canvas.drawText("Settings", (image.getWidth()/2)+10, (image.getHeight()/4)+paint.getTextSize(), paint);
				break;
			case 3:
				image = BitmapFactory.decodeResource(getResources(), R.drawable.tutorial_2).copy(Bitmap.Config.ARGB_8888, true);
				canvas = new Canvas(image);
				widthScale=(float)image.getWidth()/360;
				heightScale=(float)image.getHeight()/610;
		    	paint.setStyle(Paint.Style.STROKE);
				
				paint.setStrokeWidth(paint.getStrokeWidth()+5);
				canvas.drawCircle((128*widthScale), (427*heightScale), (15*widthScale), paint);
				canvas.drawCircle((153*widthScale), (281*heightScale), (15*widthScale), paint);
				setMarkerTextSize(screenSize.y,screenSize.x,getResources().getBoolean(R.bool.isTablet),paint);
				canvas.drawLine((128*widthScale), (427*heightScale), (image.getWidth()/2), (image.getHeight()/2)+(paint.getTextSize()*2)+3, paint);
				canvas.drawLine((153*widthScale), (281*heightScale), (image.getWidth()/2), (image.getHeight()/2)+paint.getTextSize(), paint);
		    	
		    	paint.setStrokeWidth(paint.getStrokeWidth()-5);
		    	canvas.drawText("Available Street", (image.getWidth()/2), (image.getHeight()/2)+(paint.getTextSize()*2), paint);
				break;
			case 4:
				canvas = new Canvas(image);
				widthScale=(float)image.getWidth()/360;
				heightScale=(float)image.getHeight()/610;
		    	paint.setStyle(Paint.Style.STROKE);
				
				paint.setStrokeWidth(paint.getStrokeWidth()+5);
				canvas.drawCircle((51*widthScale), (93*heightScale), (15*widthScale), paint);
		    	canvas.drawLine((51*widthScale), (93*heightScale), image.getWidth()/4, image.getHeight()/4, paint);

		    	paint.setStrokeWidth(paint.getStrokeWidth()-5);
				setMarkerTextSize(screenSize.y,screenSize.x,getResources().getBoolean(R.bool.isTablet),paint);
		    	canvas.drawText("Selected Street", (image.getWidth()/4)+10, (image.getHeight()/4)+paint.getTextSize(), paint);
				break;
		}
		imageWindow.setImageBitmap(image);
		imageWindow.setScaleType(ImageView.ScaleType.FIT_CENTER);
	}
	
    public void setMarkerTextSize(int height, int width, boolean isTablet, Paint paint) {
		float xMultiplier;
		
		
		if (width>height){
			xMultiplier = width/381;
		} else {
			xMultiplier = width/240;
		}
		
		float textSize = paint.getTextSize();
		paint.setTextSize(100);
		paint.setTextScaleX(1.0f);
		Rect bounds = new Rect();
		paint.getTextBounds("Ty", 0, 2, bounds);
		int textHeight = bounds.bottom-bounds.top;
		if (isTablet){
			paint.setTextSize(((float)(textSize*(xMultiplier/2))/(float)textHeight*100f)-1);
		} else {
			paint.setTextSize(((float)(textSize*xMultiplier)/(float)textHeight*100f)-1);
		}
	}
}
