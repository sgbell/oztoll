/**
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
