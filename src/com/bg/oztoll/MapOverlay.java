/**
 * 
 */
package com.bg.oztoll;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

/**
 * @author bugman
 *
 */
public class MapOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayStreet> mOverlays = new ArrayList<OverlayStreet>();
	private Context mContext;
	private Drawable selectedRoad,
					 invisibleMarker;
	private Handler mHandler;
	private OzTollData tollData;

	public MapOverlay(Drawable arg0) {
		super(boundCenterBottom(arg0));
	}
	
	public MapOverlay(Drawable defaultMarker, Context context, Handler handler, OzTollData tollData){
		super (boundCenterBottom(defaultMarker));
		mContext = context;
		mHandler = handler;
		
		Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		invisibleMarker = new BitmapDrawable(mContext.getResources(),image);
		this.tollData=tollData;
	}
	
    /**
     * createMarker allows us to create a numbered marker which will be put on the screen.
     * @param positionNumber
     * @return
     */
    public Drawable createMarker(int positionNumber, boolean selected){
    	Bitmap image;
    	
    	if (selected){
    		image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.selectedroad);
    	} else {
        	image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.activeroad);
    	}
    	image = image.copy(Bitmap.Config.ARGB_8888, true);
    	Canvas canvas = new Canvas(image);
    	Paint paint = new Paint();
    	paint.setColor(Color.WHITE);
    	paint.setTextAlign(Align.CENTER);
    	canvas.drawText(Integer.toString(positionNumber), 10, 15, paint);
    	Drawable d = new BitmapDrawable(mContext.getResources(),image);
    	d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    	
        return d;
    }
	
    public void doPopulate(){
    	for (int oc=0; oc < mOverlays.size(); oc++){
    		OverlayStreet overlay = mOverlays.get(oc);
    		if ((overlay.getStreet().isValid())&&
    			(overlay.getStreet()!=tollData.getFinish())){
    			overlay.setMarker(createMarker(overlay.getStreet().getLocation(),false));
    		} else {
    			if ((tollData.getStart()==overlay.getStreet())||
    				(tollData.getFinish()==overlay.getStreet()))
    				overlay.setMarker(createMarker(overlay.getStreet().getLocation(),true));
    			else
    				overlay.setMarker(invisibleMarker);
    		}
    		
    	}
    	setLastFocusedIndex(-1);
    	populate();
    }
    
    public void addOverlay(OverlayStreet overlay){
		mOverlays.add(overlay);
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
		setLastFocusedIndex(-1);
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
