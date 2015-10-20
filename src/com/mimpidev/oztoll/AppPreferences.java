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
package com.mimpidev.oztoll;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import android.os.Bundle;
import android.preference.ListPreference;

/**
 * @author bugman
 *
 */
public class AppPreferences extends SherlockPreferenceActivity {
	private OzTollApplication global;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		
    	// This is used to access the object that is going to be used to share the data across both activities
    	global = (OzTollApplication)getApplication();
    	
    	ListPreference selectedCityPreference = (ListPreference) findPreference("selectedCity");
    	if (selectedCityPreference!=null){
    		CharSequence entries[] = new String[global.getTollData().getCities().size()];
    		
    		for (int cityCount=0; cityCount<global.getTollData().getCities().size(); cityCount++){
    			entries[cityCount] = global.getTollData().getCityById(cityCount).getCityName();
    		}
    		selectedCityPreference.setEntries(entries);
    		selectedCityPreference.setEntryValues(entries);
    	}
	}
	
}
