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

package cz.babi.android.remoteme.ui;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import cz.babi.android.remoteme.ActivityMain;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.data.MySQLiteOpenHelper;

/**
 * Activity for delete server.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityDialogServerDelete extends Activity {
	
	private static final String TAG_CLASS_NAME = ActivityDialogServerDelete.class.getName();
	
	private int idToDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();
		idToDelete = Integer.valueOf(String.valueOf(extras.get("ID_TO_DELETE")));
		
		setContentView(R.layout.activity_server_delete);
		
		/* Prevent Activity dialog from closing on outside touch. */
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			setFinishOnTouchOutside(false);
		}
		
		Button btnOk = (Button)findViewById(
				R.id.server_delete_btn_ok);
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MySQLiteOpenHelper.getInstance(ActivityDialogServerDelete.this).
				deleteServer(idToDelete);
				
				ActivityMain.needRefreshServers = true;
				finish();
			}
		});
		
		Button btnCancel = (Button)findViewById(
				R.id.server_delete_btn_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		setOrientation();
	}
	
	/**
	 * Set orientation.
	 */
	private void setOrientation() {
		String currentOrientationLock = PreferenceManager.getDefaultSharedPreferences(this).
				getString(getString(R.string.pref_name_orientation_lock),
						getString(R.string.pref_value_default));
		
		if(currentOrientationLock.equals(getString(R.string.pref_value_portait))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if(currentOrientationLock.equals(getString(R.string.pref_value_landscape))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
}
