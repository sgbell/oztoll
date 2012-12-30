/**OzStorage is used to open a file from external storage. It checks if the external storage can
 * be accessed and if it can, opens the file using OzTollData. 
 */
package com.mimpidev.oztoll;

import android.os.Environment;

/**
 * @author bugman
 *
 */
public class OzStorage {
	public static final int EXTERNAL_READ_WRITE=0,
							EXTERNAL_READ_ONLY=1,
							EXTERNAL_ERROR=2;
	private OzTollData datafile;
	
	public OzStorage(){
		setTollData("melbourne.xml");
	}
	
	public OzTollData getTollData(){
		return datafile;
	}
	
	public String getTollDataStatus(){
		if (datafile==null)
			return "Can not open file";
		else
			return "File Opened";
	}
	
	public void setTollData(String filename){
		// As we don't need to write to the file, we make sure we can read from the external storage.
		if (externalStatus()<EXTERNAL_ERROR){
			String dataFileName = Environment.getExternalStorageDirectory()+
					"/oztoll/"+filename;
			datafile = new OzTollData(dataFileName);
		}
	}
	
	public int externalStatus(){
		String state = Environment.getExternalStorageState();
		
		// Following If Statements check sdcard availability & wriability
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write to the media
			//available = writable = true;
			return EXTERNAL_READ_WRITE;
		}  else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			return EXTERNAL_READ_ONLY;
		} else {
			// The storage isn't available
			return EXTERNAL_ERROR;
		}		
	}
}