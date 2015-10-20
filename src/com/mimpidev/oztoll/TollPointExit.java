/**
 * 
 * Copyright (C) 2015  Sam Bell
 * @email - sam@mimpidev.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or  any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sam Bell - initial API and implementation
 * 
 */
/** TollPoint exit contains an array of the exits that belong to the same group.
 * It also has an array of the tollrates for this point. 
 */
package com.mimpidev.oztoll;

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
