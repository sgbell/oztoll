/** TollPoint exit contains an array of the exits that belong to the same group.
 * It also has an array of the tollrates for this point. 
 */
package com.bg.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class TollPointExit {
	private ArrayList<Street> street;
	private ArrayList<TollRate> rates;
	
	public TollPointExit(){
		street = new ArrayList<Street>();
		rates = new ArrayList<TollRate>();
	}
	
	public ArrayList<TollRate> getRates(){
		return rates;
	}
	
	public void addRate(TollRate newRate){
		rates.add(newRate);
	}
	
	public void addExit(Street newStreet){
		street.add(newStreet);
	}
	
	/** isExit searchs this toll point to see if it contains the exit
	 * @param exit - Street we are looking for
	 * @return - found
	 */
	public boolean isExit(String exit){
		for (int svc=0; svc < street.size(); svc++)
			if (street.get(svc).getName().equalsIgnoreCase(exit))
				return true;
		return false;
	}
	
	public ArrayList<Street> getExits(){
		return street;
	}
}
