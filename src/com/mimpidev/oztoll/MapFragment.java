/**
 * 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
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
	private SharedPreferences preferences;
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
		preferences = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity().getBaseContext());
		
		handler = global.getMainActivityHandler();

		paint = new Paint();
    	paint.setColor(Color.WHITE);
    	paint.setTextAlign(Align.CENTER);

		
		mapView.setOnMarkerClickListener(new OnMarkerClickListener(){

			@Override
			public boolean onMarkerClick(Marker selected) {
				LatLng latLngChanged = new LatLng(((double)Math.round(selected.getPosition().latitude*1000000)/1000000),
						                          ((double)Math.round(selected.getPosition().longitude*1000000)/1000000));
				
				// Working here. need to add code to findStreetByLatLng
				Street currentStreet=global.getTollData().findStreetByLatLng(latLngChanged);
				// Just incase something changed in the data file and the street doesn't exist in the tolldata
				// check the returned value, make sure it's not null
				if (currentStreet!=null){
					Message newMessage = handler.obtainMessage();
					newMessage.what=9;
					newMessage.obj=currentStreet;
					handler.dispatchMessage(newMessage);
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
			// change this depending on if city is selected in preferences
			String selectedCity = preferences.getString("selectedCity", "");
			int cityCount=0;
			if (!selectedCity.equalsIgnoreCase(""))
				cityCount=global.getTollData().getCityId(selectedCity);
			mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(global.getTollData().getCityById(cityCount).getOrigin().getLatLng(),12));

			Point screenSize= new Point();
			int currentApi = android.os.Build.VERSION.SDK_INT;
			if (currentApi < 13){
				screenSize.x=getSherlockActivity().getWindowManager().getDefaultDisplay().getWidth();
				screenSize.y=getSherlockActivity().getWindowManager().getDefaultDisplay().getHeight();
			} else 
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
		
		ArrayList<Street> validStreets = global.getTollData().getValidStreetsArray("");
		for (int streetCount=0; streetCount<validStreets.size(); streetCount++){
			Street currentStreet = validStreets.get(streetCount);
			MarkerOptions newMarker = new MarkerOptions();
			newMarker.position(currentStreet.getLatLng());
			if ((global.getTollData().getFinish()==currentStreet)||
				(global.getTollData().getStart()==currentStreet))
				newMarker.icon(BitmapDescriptorFactory.fromBitmap(createMarker(currentStreet.getLocation(),true)));
			else
				newMarker.icon(BitmapDescriptorFactory.fromBitmap(createMarker(currentStreet.getLocation(),false)));
			
			mapView.addMarker(newMarker);
		}
	}
	
    /**
     * createMarker allows us to create a numbered marker which will be put on the screen.
     * @param positionNumber
     * @return
     */
    public Bitmap createMarker(char positionAlpha, boolean selected){
    	Bitmap image;
    	
    	if (selected){
    		image = BitmapFactory.decodeResource(getResources(), R.drawable.selectedroad);
    	} else {
        	image = BitmapFactory.decodeResource(getResources(), R.drawable.activeroad);
    	}
    	image = image.copy(Bitmap.Config.ARGB_8888, true);
    	Canvas canvas = new Canvas(image);
    	
    	canvas.drawText(String.valueOf(positionAlpha), (canvas.getWidth()/2), ((paint.getTextSize()/2)+((canvas.getHeight()-2)/2))-2, paint);
    	
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
			paint.setTextSize(((float)(textSize*(xMultiplier/2))/(float)textHeight*100f));
		} else {
			paint.setTextSize(((float)(textSize*xMultiplier)/(float)textHeight*100f));
		}
	}
}
