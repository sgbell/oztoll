/**
 * 
 */
package com.mimpidev.oztoll;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * @author bugman
 *
 */
public class MapFragment extends SherlockMapFragment {

	public static final String TAG = "ozTollMapFragment";
	private GoogleMap mapView;
	private OzTollApplication global;
	//private SharedPreferences preferences;
	private Paint paint;
	
	private Handler handler;

	public MapFragment(){
	}
	
	public MapFragment(Handler mainHandler){
		handler = mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		
		setRetainInstance(true);

	}
	
	public void onResume(){
		super.onResume();

		global = (OzTollApplication)getSherlockActivity().getApplication();
		//preferences = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity().getBaseContext());
		
		handler = global.getMainActivityHandler();

		paint = new Paint();
    	paint.setColor(Color.WHITE);
    	paint.setTextAlign(Align.CENTER);

		
		mapView.setOnMarkerClickListener(new OnMarkerClickListener(){

			@Override
			public boolean onMarkerClick(Marker selected) {
				LatLng latLngChanged = new LatLng(((double)Math.round(selected.getPosition().latitude*1000000)/1000000),
						                          ((double)Math.round(selected.getPosition().longitude*1000000)/1000000));
				for (int twc=0; twc < global.getTollData().getTollwayCount(); twc++)
					for (int tsc=0; tsc < global.getTollData().getStreetCount(twc); tsc++){
						Street currentStreet = global.getTollData().getStreet(twc, tsc);
						LatLng currentLatLng = new GeoPoint((int)currentStreet.getY(),(int)currentStreet.getX()).getLatLng();
						if ((currentLatLng.latitude==latLngChanged.latitude)&&
							(currentLatLng.longitude==latLngChanged.longitude)){
							Message newMessage = handler.obtainMessage();
							newMessage.what=9;
							newMessage.obj=currentStreet;
							handler.dispatchMessage(newMessage);
							return true;
						}
					}
				return true;
			}
		});
		
		/* Before we go and create a thread to handle adding the streets to the overlay,
		 and doing any modifications to them, try doing it here
		 */ 
		Message newMessage = handler.obtainMessage();
		newMessage.what = 5;
		handler.dispatchMessage(newMessage);
		synchronized(global.getDatasync()){
			while (!global.getTollData().isFinished()){
				newMessage = handler.obtainMessage();
				newMessage.what = 5;
				handler.dispatchMessage(newMessage);
				try {
					global.getDatasync().wait();
				} catch (InterruptedException e) {
					
				}
			}
			mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(global.getTollData().getOrigin().getLatLng(),12));
			

			Point screenSize= new Point();
			getSherlockActivity().getWindowManager().getDefaultDisplay().getSize(screenSize);
			setMarkerTextSize(screenSize.y, screenSize.x,getResources().getBoolean(R.bool.isTablet));
			
			populateMarkers();
			
			newMessage = handler.obtainMessage();

			//newMessage = handler.obtainMessage();
			newMessage.what = 6;
			handler.dispatchMessage(newMessage);
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		View view=super.onCreateView(inflater, vg, savedInstanceBundle);
		mapView = getMap();
		return view;
	}
	
	public void populateMarkers(){
		mapView.clear();
		for (int twc=0; twc < global.getTollData().getTollwayCount(); twc++)
			for (int tsc=0; tsc < global.getTollData().getStreetCount(twc); tsc++){
				Street newStreet = global.getTollData().getStreet(twc, tsc);
				if ((newStreet.isValid())||
					(newStreet.equals(global.getTollData().getStart()))||
					(newStreet.equals(global.getTollData().getFinish()))){
					MarkerOptions newMarker = new MarkerOptions();
					newMarker.position(new GeoPoint((int)newStreet.getY(),(int)newStreet.getX()).getLatLng());
		    		if ((newStreet.isValid())&&
	        			(newStreet!=global.getTollData().getFinish())){
						newMarker.icon(BitmapDescriptorFactory.fromBitmap(createMarker(newStreet.getLocation(),false)));
		        	} else {
		        		if ((global.getTollData().getStart()==newStreet)||
		        			(global.getTollData().getFinish()==newStreet))
							newMarker.icon(BitmapDescriptorFactory.fromBitmap(createMarker(newStreet.getLocation(),true)));
	        		}
		    		mapView.addMarker(newMarker);
				}
			}
	}
	
    /**
     * createMarker allows us to create a numbered marker which will be put on the screen.
     * @param positionNumber
     * @return
     */
    public Bitmap createMarker(int positionNumber, boolean selected){
    	Bitmap image;
    	
    	if (selected){
    		image = BitmapFactory.decodeResource(getResources(), R.drawable.selectedroad);
    	} else {
        	image = BitmapFactory.decodeResource(getResources(), R.drawable.activeroad);
    	}
    	image = image.copy(Bitmap.Config.ARGB_8888, true);
    	Canvas canvas = new Canvas(image);
    	
    	canvas.drawText(Integer.toString(positionNumber), (canvas.getWidth()/2), ((paint.getTextSize()/2)+((canvas.getHeight()-2)/2))-2, paint);
    	
        return image;
    }

    public void setMarkerTextSize(int height, int width, boolean isTablet) {
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
