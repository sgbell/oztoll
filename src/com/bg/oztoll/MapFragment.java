/**
 * 
 */
package com.bg.oztoll;

import java.util.List;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * @author bugman
 *
 */
public class MapFragment extends SherlockFragment {

	public static final String TAG = "ozTollMapFragment";
	private MapView mapView;
	private MapOverlay itemizedOverlay;

	private OzTollApplication global;
	private Handler handler;

	public MapFragment(){
		
	}
	
	public MapFragment(MapView mapView, Handler mainHandler){
		this.mapView=mapView;
		handler = mainHandler;
	}
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		
		setRetainInstance(true);

		// Set zoom on the map
		mapView.setBuiltInZoomControls(true);

	}
	
	public void onResume(){
		super.onResume();
		
		global = (OzTollApplication)this.getActivity().getApplication();
		
		/* Before we go and create a thread to handle adding the streets to the overlay,
		 and doing any modifications to them, try doing it here
		 */ 
		synchronized(global.getTollData().getDataSync()){
			while (!global.getTollData().isFinished()){
				Message newMessage = handler.obtainMessage();
				newMessage.what = 5;
				handler.dispatchMessage(newMessage);
				try {
					global.getTollData().getDataSync().wait();
				} catch (InterruptedException e) {
					
				}
			}
			// populate overlay array
			List<Overlay> mapOverlays = mapView.getOverlays();
			// The following two lines are the images used to mark the exits on the map
			Drawable defaultActiveRoad = getResources().getDrawable(R.drawable.activeroad);
			Drawable selectedRoad = getResources().getDrawable(R.drawable.selectedroad);
			// Creation of the overlay for the map
			itemizedOverlay = new MapOverlay(defaultActiveRoad, getSherlockActivity(), handler, global.getTollData());

			Point screenSize= new Point();
			screenSize.x=getSherlockActivity().getWindowManager().getDefaultDisplay().getWidth();
			screenSize.y=getSherlockActivity().getWindowManager().getDefaultDisplay().getHeight();
			itemizedOverlay.setMarkerTextSize(screenSize.y, screenSize.x);
			
			Message newMessage = handler.obtainMessage();
			newMessage.what = 10;
			handler.dispatchMessage(newMessage);
			
			for (int twc=0; twc < global.getTollData().getTollwayCount(); twc++)
				for (int tsc=0; tsc < global.getTollData().getStreetCount(twc); tsc++){
					OverlayStreet item = new OverlayStreet(global.getTollData().getStreet(twc, tsc));
					itemizedOverlay.addOverlay(item);
				}
			itemizedOverlay.doPopulate();
			
			// Add streets to overlay
			mapOverlays.add(itemizedOverlay);
			
			newMessage = handler.obtainMessage();
			newMessage.what = 6;
			handler.dispatchMessage(newMessage);
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle savedInstanceBundle){
		return inflater.inflate(R.layout.oztoll_map, null);
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		
		setMapView((MapView)view);
	}
	
	public void setMapView (MapView mapView){
		this.mapView = mapView;
	}
	
	public MapView getMapView(){
		return mapView;
	}
	
	public MapOverlay getOverlay(){
		return itemizedOverlay;
	}
}