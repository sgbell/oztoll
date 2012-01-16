/**
 * 
 */
package com.bg.oztoll;

import java.util.Vector;

/**
 * @author bugman
 *
 */
public class TollPointExit {
	private Vector<Street> street;
	private Vector<TollRate> rates;
	
	public TollPointExit(){
		street = new Vector<Street>();
		rates = new Vector<TollRate>();
	}
	
	public Vector<TollRate> getRates(){
		return rates;
	}
	
	public void addRate(TollRate newRate){
		rates.add(newRate);
	}
	
	public void addExit(Street newStreet){
		street.add(newStreet);
	}
	
	public boolean isExit(String exit){
		for (int svc=0; svc < street.size(); svc++)
			if (street.get(svc).getName().equalsIgnoreCase(exit))
				return true;
		return false;
	}
}
