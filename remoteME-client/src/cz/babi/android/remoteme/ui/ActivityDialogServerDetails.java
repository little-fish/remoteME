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

import java.util.Date;
import java.util.regex.Matcher;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import cz.babi.android.remoteme.ActivityMain;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.data.MySQLiteOpenHelper;
import cz.babi.android.remoteme.entity.Server;

/**
 * Activity showing server details.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityDialogServerDetails extends Activity {
	
	private static final String TAG_CLASS_NAME = ActivityDialogServerDetails.class.getName();
	
	/* Server to edit. */
	private Server server;
	/* If we trying to add new or edit server. */
	private boolean isNew;
	/* If we trying to add new server from Scan Mode. */
	private boolean isNewFromScan;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		super.onCreate(savedInstanceState);
		
		isNew = getIntent().getBooleanExtra("IS_NEW", false);
		isNewFromScan = getIntent().getBooleanExtra("IS_NEW_FROM_SCAN", false);
		
		setContentView(R.layout.activity_server_details);
		
		/* Prevent Activity dialog from closing on outside touch. */
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			setFinishOnTouchOutside(false);
		}
		
		TextView title = (TextView)findViewById(R.id.server_details_title);
		EditText serverNameEditText = (EditText)findViewById(R.id.server_details_server_name);
		EditText ipAddressEditText = (EditText)findViewById(R.id.server_details_ipAddress);
		EditText portEditText = (EditText)findViewById(R.id.server_details_port);
		EditText passwordEditText = (EditText)findViewById(R.id.server_details_password);
		RadioGroup rbtngOsType = (RadioGroup)findViewById(R.id.server_details_rbtng_os_type);
		
		/* if we want to edit server we need to prepare some fields */
		if(!isNew) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][Server will be edited or created new from " +
					"Scan mode > preparing UI widgets...]");
			Bundle extras = getIntent().getExtras();
			server = (Server)extras.get("SERVER");
			
			/* IP address is stored in format: xxx.xxx.xxx.xxx for correct sorting. */
			String ipAddress = Common.getProperIpAddress(server.getIpAddress());
			
			serverNameEditText.setText(server.getServerName());
			ipAddressEditText.setText(ipAddress);
			portEditText.setText(String.valueOf(server.getPort()));
			passwordEditText.setText(server.getPassword());
			if(server.getOsName().toLowerCase().contains("mac")) {
				rbtngOsType.check(R.id.server_details_os_mac);
			} else if(server.getOsName().toLowerCase().contains("win")) {
				rbtngOsType.check(R.id.server_details_os_windows);
			} else {
				rbtngOsType.check(R.id.server_details_os_unix);
			}
			
			if(!isNewFromScan) title.setText(R.string.server_details_edit_title_text);
			else title.setText(R.string.server_details_new_title_text);
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][Creating new server.]");
			title.setText(R.string.server_details_new_title_text);
		}
		
		//TODO: Not implemented yet.
		final String macAddress = (server!=null) ? server.getMacAddress() : "";
		
		Button btnOkEdit = (Button)findViewById(
				R.id.server_details_btn_ok);
		btnOkEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnOk.onClicklistener]" +
						"[OK was clicked > Validation in progress...]");
				EditText ipAddressEditText = (EditText)findViewById(
						R.id.server_details_ipAddress);
				String ipAddress = String.valueOf(ipAddressEditText.getText());
				Matcher ipAddressMatcher = Patterns.IP_ADDRESS.matcher(ipAddress);
				
				EditText portEditText = (EditText)findViewById(
						R.id.server_details_port);
				
				boolean needShowWarningMessage = false;
				StringBuilder warningMessage = new StringBuilder();
				
				long port;
				if(String.valueOf(portEditText.getText()).length()>0)
					port = Long.valueOf(String.valueOf(portEditText.getText()));
				else port = Common.DEFAULT_CONNECTION_PORT;
				
				/* Check if port is correct <1024,65535> */
				if(port<1024 || port>65535) {
					if(warningMessage.length()==0)
						warningMessage.append(getResources().getText(R.string.server_details_warning_message_title));
					
					needShowWarningMessage = true;
					warningMessage.append(getResources().getText(R.string.server_details_warning_port));
				}
				
				/* Check if ip address is correct */
				if(!ipAddressMatcher.matches()) {
					warningMessage.append(getResources().getText(R.string.server_details_warning_message_title));
					needShowWarningMessage = true;
					warningMessage.append(getResources().getText(R.string.server_details_warning_ip_address));
				}
				
				/* Show warning toast if there are some incorrect data */
				if(needShowWarningMessage) {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnOk.onClicklistener]" +
							"[Ups. Validation error.]");
					warningMessage.append(getResources().getText(R.string.server_details_warning_message_footer));
					LayoutInflater mInflater = getLayoutInflater();
					View toastLayout = mInflater.inflate(R.layout.toast_warning,
							(ViewGroup)findViewById(R.id.toast_layout_root));
					
					TextView text = (TextView)toastLayout.findViewById(R.id.warning_text);
					text.setText(warningMessage.toString());
					
					Toast toast = new Toast(ActivityDialogServerDetails.this);
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setView(toastLayout);
					toast.show();
				} else {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnOk.onClicklistener]" +
							"[Validation passed.]");
					/* We need to store IP address in format: xxx.xxx.xxx.xxx
					 * for correct sorting. */
					ipAddress = Common.getDbIpAddress(ipAddress);
					
					EditText serverNameEditText = (EditText)findViewById(
							R.id.server_details_server_name);
					EditText passwordEditText = (EditText)findViewById(
							R.id.server_details_password);
					RadioGroup rbtngOsType = (RadioGroup)findViewById(
							R.id.server_details_rbtng_os_type);
					int selectedOsType = rbtngOsType.getCheckedRadioButtonId();
					RadioButton rbtnSelected = (RadioButton)findViewById(selectedOsType);
					
					String serverName = String.valueOf(serverNameEditText.getText());
					String password = String.valueOf(passwordEditText.getText());
					String osType = String.valueOf(rbtnSelected.getText());
					
					if(isNew) {
						if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnOk.onClicklistener]" +
								"[Trying to add new server to DB > Manually created.]");
						Server newServer = new Server(ipAddress, macAddress, port, password,
								serverName, osType, new Date(),	null);
						
						MySQLiteOpenHelper.getInstance(ActivityDialogServerDetails.this).
						addServer(newServer);
					} else {
						server.setIpAddress(ipAddress);
						server.setMacAddress(macAddress);
						server.setPort(port);
						server.setPassword(password);
						server.setServerName(serverName);
						server.setOsName(osType);
						
						if(isNewFromScan) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnOk.onClicklistener]" +
									"[Trying to add new server to DB > From Scan mode.]");
							MySQLiteOpenHelper.getInstance(
									ActivityDialogServerDetails.this).addServer(server);
						} else {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnOk.onClicklistener]" +
									"[Trying to update server.]");
							MySQLiteOpenHelper.getInstance(
									ActivityDialogServerDetails.this).updateServer(server);
						}
					}
					
					ActivityMain.needRefreshServers = true;
					finish();
				}
			}
		});
		
		Button btnCancelEdit = (Button)findViewById(
				R.id.server_details_btn_cancel);
		btnCancelEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][btnCancel.onClicklistener]");
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
