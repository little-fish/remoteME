/**
 * Copyright 2013 Martin Misiarz (dev.misiarz@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.babi.android.remoteme.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.RemoteController;
import cz.babi.android.remoteme.ui.MultiSelectListPreference;

/**
 * XML parsing task.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class XMLParsingTask extends AsyncTask<Void, RemoteController, Void> {
	
	private static final String TAG_CLASS_NAME = XMLParsingTask.class.getSimpleName();
	
	private RemoteControllerXmlParser xmlParser = null;
	
	private Context context = null;
	
	public XMLParsingTask(Context context) {
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground]");
		
		/* First of all we need to obtain xml files from assets folder. */
		AssetManager assetManager = context.getAssets();
		
		String[] filesInAsset = null;
		ArrayList<Reader> readers = new ArrayList<Reader>();
		
		try {
			/* Only check for files in 'controllers' folder. */
			filesInAsset = assetManager.list("controllers");
		} catch(IOException e) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not obtain list " +
					"of files from assets folder.]");
		}
		
		/* If there are some files. */
		if(filesInAsset!=null) {
			/* Iterate every file and check if it is an xml. */
			for(String file : filesInAsset) {
				if(file.endsWith(".xml")) {
					/* Here we need to get Reader from that xml file. */
					InputStream is = null;
					Reader reader = null;
					
					try {
						is = assetManager.open("controllers/" + file);
						reader = new InputStreamReader(is, Common.CHARSET_UTF8);
					} catch(IOException e) {
						if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not " +
								"open input stream or reader from xml file.]");
					}
					
					/* Add Reader to list. */
					if(is!=null && reader!=null) readers.add(reader);
				}
			}
		}
		
		/* Now, lets check for files in sd card. */
		File sdCard = Environment.getExternalStorageDirectory();
		File appFolder = new File(sdCard, Common.APP_FOLDER);
		
		/* If app folder exists on sd card. */
		if(appFolder.exists()) {
			File[] filesInSD = null;
			
			filesInSD = appFolder.listFiles();
			
			/* If there are some files. */
			if(filesInSD!=null) {
				/* Iterate every file and check if it is an xml. */
				for(File file : filesInSD) {
					if(file.isFile() && file.getName().endsWith(".xml")) {
						/* Here we need to get Reader from that xml file. */
						InputStream is = null;
						Reader reader = null;
						
						try {
							is = new FileInputStream(file);
							reader = new InputStreamReader(is, Common.CHARSET_UTF8);
						} catch(IOException e) {
							if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not " +
									"open input stream from file.]");
						}
						
						if(is!=null && reader!=null) readers.add(reader);
					}
				}
			}
		}
		
		/* Here we need to add controller for mouse and keyboard which is not define via xml. */
		RemoteController mouseAndKeyboard = new RemoteController(
				"id_mouse_and_keyboard",
				context.getResources().getString(R.string.preferences_category_mouse_and_keyboard_text),
				context.getResources().getString(R.string.text_app_author),
				"app_mouse_and_keyboard");
		publishProgress(mouseAndKeyboard);
		
		/* Now we have all available readers and can start parsing. */
		for(int i=0; i<readers.size(); i++) {
			RemoteController remoteController = null;
			
			try {
				remoteController = xmlParser.parse(readers.get(i));
			} catch(XmlPullParserException e) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not " +
						"parse xml file.]");
			} catch(IOException e) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not " +
						"parse xml file.]");
			}
			
			/* If parser did not return null we can add parsed controller to adapter. */
			if(remoteController!=null)	publishProgress(remoteController);
		}
		
		return null;
	}
	
	@Override
	protected void onPreExecute() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onPreExecute]");
		
		xmlParser = new RemoteControllerXmlParser();
		
		/* Because we want to create new adaper contains new data. */
		RemoteControllerArrayAdapter.setInstanceNull();
		
		/* Clear lists of entries. */
		MultiSelectListPreference.listEntries.clear();
		MultiSelectListPreference.listEntryValues.clear();
		
		super.onPreExecute();
	}
	
	@Override
	protected void onProgressUpdate(RemoteController... values) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onProgressUpdate]");
		
		RemoteController remoteController = values[0];
		
		RemoteControllerArrayAdapter adapter = RemoteControllerArrayAdapter.getInstance(context,
				R.layout.row_remote_controller);
		
		/* obtain proper title. */
		String title = "";
		if(remoteController.getTitle().startsWith("string_")) {
			title = getStringResourceByName(remoteController.getTitle().
					substring(remoteController.getTitle().indexOf("_")+1));
		}
		
		if(title.length()==0) title = remoteController.getTitle();
		
		/* Add controller title and id to lists. */
		MultiSelectListPreference.listEntries.add(title);
		MultiSelectListPreference.listEntryValues.add(remoteController.getId());
		
		/* Add controller to array if need to. */
		String visibleRemotesString = (PreferenceManager.getDefaultSharedPreferences(context)).
				getString(context.getString(R.string.pref_name_visible_remotes), "");
		/* If there are some visible remotes. */
		if(!visibleRemotesString.equals("")) {
			String[] visibleRemotes = visibleRemotesString.split(MultiSelectListPreference.DEFAULT_SEPARATOR);
			/* Need to check if parsed remote controller is visible or not. */
			for(String remoteID : visibleRemotes) {
				if(remoteController.getId().equals(remoteID)) {
					adapter.add(remoteController);
					
					break;
				}
			}
		}
		
		super.onProgressUpdate(values);
	}
	
	/**
	 * Get String resource by its name.
	 * @param resName Resource name.
	 * @return Resource.
	 */
	private String getStringResourceByName(String resName) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getStringResourceByName][" + resName + "]");
		
		String packageName = context.getPackageName();
		
		int resId = context.getResources().getIdentifier(resName, "string", packageName);
		
		if(resId==0) return "";
		else return context.getString(resId);
	}
}
