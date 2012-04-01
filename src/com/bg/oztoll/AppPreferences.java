/**
 * 
 */
package com.bg.oztoll;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author bugman
 *
 */
public class AppPreferences extends PreferenceActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
	}
}
