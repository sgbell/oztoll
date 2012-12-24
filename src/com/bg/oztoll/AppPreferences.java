/**
 * 
 */
package com.bg.oztoll;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import android.os.Bundle;

/**
 * @author bugman
 *
 */
public class AppPreferences extends SherlockPreferenceActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
	}
	
}
