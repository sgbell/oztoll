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

/**OzStorage is used to open a file from external storage. It checks if the external storage can
 * be accessed and if it can, opens the file using OzTollData. 
 */
package com.mimpidev.oztoll;

import java.io.File;

import android.content.SharedPreferences;
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
			if (externalStatus()==EXTERNAL_READ_WRITE){
				File directory = new File(Environment.getExternalStorageDirectory()+"/oztoll");
				if (!directory.exists()){
					directory.mkdirs();
				}
			}
			String dataFileName = Environment.getExternalStorageDirectory()+
					"/oztoll/"+filename;
			File testfile = new File(dataFileName);
			if (testfile.exists())
				datafile = new OzTollData(dataFileName);
			else
				datafile = null;
		}
	}

	public void keepFile(File saveFile) {
		if (externalStatus()==EXTERNAL_READ_WRITE){
			File oldFile = new File(Environment.getExternalStorageDirectory()+"/oztoll/oztoll.xml");
			oldFile.delete();
			saveFile.renameTo(new File(Environment.getExternalStorageDirectory()+"/oztoll/oztoll.xml"));
		}
	}

	public boolean removeFile(File fileToRemove) {
		if (externalStatus()==EXTERNAL_READ_WRITE){
			return fileToRemove.delete();
		}
		
		return false;
	}
	
	public boolean removeFile(String filename){
		if (externalStatus()==EXTERNAL_READ_WRITE){
			File openFile= new File(Environment.getExternalStorageDirectory()+"/oztoll/"+filename);
			if (openFile!=null)
				return openFile.delete();
		}
		
		return false;
	}
	
	public OzTollData openExternalFile(String filename, SharedPreferences preferences){
		if ((externalStatus()==EXTERNAL_READ_ONLY)||(externalStatus()==EXTERNAL_READ_WRITE)){
			File openFile= new File(Environment.getExternalStorageDirectory()+"/oztoll/"+filename);
			
			if (openFile.exists()){
				OzTollData newDataFile = new OzTollData(openFile,preferences);
				newDataFile.readFile();
				
				return newDataFile;
			}
		}
		
		return null;
	}
	
	public File saveFiletoExternal(String filename){
		if ((externalStatus()<EXTERNAL_ERROR)&&
			(externalStatus()==OzStorage.EXTERNAL_READ_WRITE)){
			// Create the directory
			File directory = new File(Environment.getExternalStorageDirectory()+"/oztoll");
			if (!directory.exists()){
				directory.mkdirs();
			}
			File saveFile = new File(Environment.getExternalStorageDirectory()+"/oztoll/"+filename);
			return saveFile;
		}

		return null;
	}
	
	public int externalStatus(){
		String state = Environment.getExternalStorageState();
		
		// Following If Statements check sdcard availability & writability
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
