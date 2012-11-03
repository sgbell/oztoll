/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author bugman
 *
 */
public class MapOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayStreet> mOverlays = new ArrayList<OverlayStreet>();
	private Context mContext;
	private Drawable selectedRoad;
	private Handler mHandler;

	public MapOverlay(Drawable arg0) {
		super(boundCenterBottom(arg0));
	}
	
	public MapOverlay(Drawable defaultMarker, Drawable selectedMarker, Context context, Handler handler){
		super (boundCenterBottom(defaultMarker));
		mContext = context;
		selectedRoad = selectedMarker;
		mHandler = handler;
	}
	
	public void addOverlay(OverlayStreet overlay){
		mOverlays.add(overlay);
		populate();
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */
	@Override
	protected OverlayStreet createItem(int arg0) {
		// TODO Auto-generated method stub
		return mOverlays.get(arg0);
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#size()
	 */
	@Override
	public int size() {
		return mOverlays.size();
	}

	public void clearOverlay(){
		mOverlays.clear();
	}
	
	/** onTap is used to identify the street that is selected so we can then work with it
	 * 
	 */
	protected boolean onTap(int index){
		OverlayStreet item = mOverlays.get(index);
		Message newMessage = mHandler.obtainMessage();
		newMessage.what=9;
		newMessage.obj=item.getStreet();
		mHandler.dispatchMessage(newMessage);
		return true;
	}
	
	/**Overwrote this draw method so we can remove the shadow from the map.
	 */
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when){
		super.draw(canvas, mapView, false);
		return true;
	}
}
