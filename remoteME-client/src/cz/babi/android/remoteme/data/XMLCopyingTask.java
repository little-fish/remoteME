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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import cz.babi.android.remoteme.common.Common;

/**
 * Task for copy files from asset folder to sd card.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class XMLCopyingTask extends AsyncTask<Void, Void, Void> {
	
	private static final String TAG_CLASS_NAME = XMLCopyingTask.class.getSimpleName();
	
	private Context context = null;
	
	public XMLCopyingTask(Context context) {
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground]");
		
		AssetManager assetManager = context.getAssets();
		
		String[] filesToCopy = null;
		
		try {
			/* Only check for files in 'controllers_to_sd' folder. */
			filesToCopy = assetManager.list("controllers_to_sd");
		} catch (IOException e) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not obtain list " +
					"of files from assets folder.]");
		}
		
		/* If there are some files. */
		if(filesToCopy!=null) {
			/* Iterate every file and do copy. */
			for(String fileName : filesToCopy) {
				InputStream inputStream = null;
				OutputStream outputStream = null;
				
				try {
					/* Target path. */
					String targetPath = Environment.getExternalStorageDirectory() +
							"/" + Common.APP_FOLDER + "/";
					/* Obtain input and output stream. */
					inputStream = assetManager.open("controllers_to_sd/" + fileName);
					outputStream = new FileOutputStream(targetPath + fileName);
					/* Do copy. */
					copyFile(inputStream, outputStream);
					
					inputStream.close();
					
					outputStream.flush();
					outputStream.close();
					
					inputStream = null;
					outputStream = null;
				} catch(IOException e) {
					if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not " +
							"copy file '" + fileName + "'.]");
				}
			}
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onPostExecute]");
		
		/* Set instance to null. */
		RemoteControllerArrayAdapter.setInstanceNull();
		
		/* Start new xml parsing. */
		XMLParsingTask xmlParsingTask = new XMLParsingTask(context);
		xmlParsingTask.execute();
		
		super.onPostExecute(result);
	}
	
	/**
	 * Do copy file.
	 * @param inputStream File input stream.
	 * @param outputStream File output stream.
	 * @throws IOException
	 */
	private void copyFile(InputStream inputStream, OutputStream outputStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		
		while((read=inputStream.read(buffer))!=-1){
			outputStream.write(buffer, 0, read);
		}
	}
}
