/** Extended OverlayItem so that I could link the street to it.
 * 
 */
package com.mimpidev.oztoll;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * @author bugman
 *
 */
public class OverlayStreet extends OverlayItem {
	private Street street;

	public OverlayStreet(GeoPoint point, String title, String message) {
		super(point, title, message);
	}

	public OverlayStreet(Street street) {
		super(new GeoPoint((int)street.getY(),(int)street.getX()),street.getName(),"");
		this.street=street;
	}
	
	public Street getStreet(){
		return street;
	}
}
