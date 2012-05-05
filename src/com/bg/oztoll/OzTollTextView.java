/**
 * 
 */
package com.bg.oztoll;

import android.widget.LinearLayout;

/**
 * @author bugman
 *
 */
public class OzTollTextView{
	private OzTollData tollData;
	
	public OzTollTextView(){
		
	}

	public OzTollTextView(OzTollData data) {
		tollData = data;
	}

	public void addStreets(LinearLayout layout) {
		for (int twc=0; twc<tollData.getTollwayCount(); twc++){
			for (int sc=0; sc<tollData.getStreetCount(twc); sc++){
				
			}
		}
	}

}
