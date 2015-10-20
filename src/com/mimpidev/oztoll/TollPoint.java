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
/** TollPoint lists the starting streets, and then the tolls from that point
 * It has one array of the starting streets and one array for the exits, 
 */
package com.mimpidev.oztoll;

import java.util.ArrayList;

/**
 * @author bugman
 *
 */
public class TollPoint {
	private ArrayList<Street> start;
	private ArrayList<TollPointExit> exit;
	
	public TollPoint(){
		start = new ArrayList<Street>();
		exit = new ArrayList<TollPointExit>();
	}
	
	public void addStart(Street newStart){
		start.add(newStart);
	}
	
	public boolean isStart(Street street){
		for (int svc=0; svc < start.size(); svc++)
			if (start.get(svc).compareLatLng(street.getLatLng()))
				return true;
		return false;
	}
	
	public void setStartValid(){
		for (int svc=0; svc < start.size(); svc++)
			start.get(svc).setValid(true);
	}
	
	public ArrayList<TollPointExit> getExit(){
		return exit;
	}
	
	public void addExit(TollPointExit tollExit){
		exit.add(tollExit);
	}
}
