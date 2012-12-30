/**
 * 
 */
package com.mimpidev.oztoll;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

/**
 * @author bugman
 *
 */
public class MapFragment extends SherlockMapFragment {

	public static final String TAG = "ozTollMapFragment";
	private GoogleMap mapView;
	private OzTollApplication global;
	//private SharedPreferences preferences;
	
	private Handler handler;

	public MapFragment(){
		
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
}
