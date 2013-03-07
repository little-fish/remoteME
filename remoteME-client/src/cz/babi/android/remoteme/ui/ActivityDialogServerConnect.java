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

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.connection.InitConnectionTask;

/**
 * Activity represents connection dialog.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityDialogServerConnect extends FragmentActivity {
	
	private static final String TAG_CLASS_NAME =
			ActivityDialogServerConnect.class.getSimpleName();
	
	ConnectionFragment fragment = null;
	
	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_server_connect);
		
		/* Prevent Activity dialog from closing on outside touch. */
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			setFinishOnTouchOutside(false);
		}
		
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
	
	/**
	 * This fragment represent whole view of parent activity.
	 *
	 * @author Martin Misiarz
	 * @author dev.misiarz@gmail.com
	 */
	public static class ConnectionFragment extends Fragment {
		
		private static final String TAG_CLASS_NAME =
				ConnectionFragment.class.getSimpleName();
		
		private Button btnConnect = null;
		private Button btnCancel = null;
		private ProgressBar mProgressBar = null;
		private TextView mTitle = null;
		private TextView connectionStatusText = null;
		private ImageView initConnectionStatusIcon = null;
		private ImageView authConnectionStatusIcon = null;
		private RelativeLayout initConnectionLayout = null;
		private RelativeLayout authConnectionLayout = null;
		private TextView connectionStatusDivider = null;
		
		private View fragmentView = null;
		
		/* We will use this constants for progress update. */
		public static final int PREPARE_INIT_UI = 1;
		public static final int UPDATE_INIT_UI_FALSE = 2;
		public static final int UPDATE_INIT_UI_TRUE = 3;
		public static final int PREPARE_AUTH_UI = 4;
		public static final int UPDATE_AUTH_UI_FALSE = 5;
		public static final int UPDATE_AUTH_UI_TRUE = 6;
		public static final int ERROR_INIT = 7;
		public static final int ERROR_AUTH = 8;
		public static final int TASK_NOT_RUNNING = 9;
		public static final int TASK_ENDS = 10;
		
		private int taskStatus = TASK_NOT_RUNNING;
		
		private InitConnectionTask initConnectionTask = null;
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreateView]");
			
			/* After device rotation we need to check if fragment's view is null or not.
			 * If not, we can restore whole view like it was before rotation. */
			if(this.fragmentView!=null) {
				((ViewGroup)this.fragmentView.getParent()).removeView(this.fragmentView);
				return this.fragmentView;
			}
			
			this.fragmentView = inflater.inflate(R.layout.fragment_server_connect, container, false);
			
			btnConnect = (Button)this.fragmentView.findViewById(
					R.id.server_connect_btn_repeat);
			btnConnect.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskStatus = TASK_NOT_RUNNING;
					startConnectTask();
				}
			});
			
			btnCancel = (Button)this.fragmentView.findViewById(
					R.id.server_connect_btn_cancel);
			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(initConnectionTask!=null) initConnectionTask.stopTask();
					getActivity().finish();
				}
			});
			
			mProgressBar = (ProgressBar)this.fragmentView.findViewById(
					R.id.server_connect_progress_bar);
			
			mTitle = (TextView)this.fragmentView.findViewById(
					R.id.server_connect_title);
			
			connectionStatusText = (TextView)this.fragmentView.findViewById(
					R.id.server_connection_status);
			
			initConnectionStatusIcon = (ImageView)this.fragmentView.findViewById(
					R.id.init_connection_status);
			authConnectionStatusIcon = (ImageView)this.fragmentView.findViewById(
					R.id.auth_connection_status);
			
			initConnectionLayout = (RelativeLayout)this.fragmentView.findViewById(
					R.id.init_connection_layout);
			authConnectionLayout = (RelativeLayout)this.fragmentView.findViewById(
					R.id.auth_connection_layout);
			
			connectionStatusDivider = (TextView)this.fragmentView.findViewById(
					R.id.connection_status_divider);
			
			setRetainInstance(true);
			
			return this.fragmentView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onActivityCreated]");
			
			super.onActivityCreated(savedInstanceState);
			
			startConnectTask();
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onSaveInstanceState]");
			
			super.onSaveInstanceState(outState);
			
			this.fragmentView = this.getView();
		}
		
		/**
		 * Simply start init connection (if need it).
		 */
		public void startConnectTask() {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[startTask]");
			
			if(taskStatus==TASK_NOT_RUNNING) {
				initConnectionTask = new InitConnectionTask(this);
				initConnectionTask.execute();
			}
		}
		
		/**
		 * This method is call to prepare widgets.
		 */
		public void updateUI() {
			switch(taskStatus) {
				case PREPARE_INIT_UI:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][PREPARE_INIT_UI]");
					initConnectionLayout.setVisibility(View.VISIBLE);
					break;
				case UPDATE_INIT_UI_FALSE:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][UPDATE_INIT_UI_FALSE]");
					initConnectionStatusIcon.setVisibility(View.VISIBLE);
					initConnectionStatusIcon.setImageResource(R.drawable.ic_cab_cancel_holo_dark);
					break;
				case UPDATE_INIT_UI_TRUE:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][UPDATE_INIT_UI_TRUE]");
					initConnectionStatusIcon.setVisibility(View.VISIBLE);
					initConnectionStatusIcon.setImageResource(R.drawable.ic_cab_done_holo_dark);
					break;
				case PREPARE_AUTH_UI:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][PREPARE_AUTH_UI]");
					authConnectionLayout.setVisibility(View.VISIBLE);
					break;
				case UPDATE_AUTH_UI_FALSE:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][UPDATE_AUTH_UI_FALSE]");
					authConnectionStatusIcon.setVisibility(View.VISIBLE);
					authConnectionStatusIcon.setImageResource(R.drawable.ic_cab_cancel_holo_dark);
					break;
				case UPDATE_AUTH_UI_TRUE:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][UPDATE_AUTH_UI_TRUE]");
					authConnectionStatusIcon.setVisibility(View.VISIBLE);
					authConnectionStatusIcon.setImageResource(R.drawable.ic_cab_done_holo_dark);
					break;
				case ERROR_INIT:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][ERROR_INIT]");
					mTitle.setText(R.string.server_connect_title_error_text);
					connectionStatusDivider.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);
					btnConnect.setEnabled(true);
					connectionStatusText.setVisibility(View.VISIBLE);
					connectionStatusText.setText(R.string.server_connect_error_init_text);
					initConnectionTask = null;
					break;
				case ERROR_AUTH:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][ERROR_AUTH]");
					mTitle.setText(R.string.server_connect_title_error_text);
					connectionStatusDivider.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);
					btnConnect.setEnabled(true);
					connectionStatusText.setVisibility(View.VISIBLE);
					connectionStatusText.setText(R.string.server_connect_error_auth_text);
					initConnectionTask = null;
					break;
				case TASK_NOT_RUNNING:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][TASK_NOT_RUNNING]");
					mTitle.setText(R.string.server_connect_title_connecting_text);
					connectionStatusDivider.setVisibility(View.GONE);
					mProgressBar.setVisibility(View.VISIBLE);
					
					connectionStatusText.setVisibility(View.GONE);
					initConnectionLayout.setVisibility(View.GONE);
					initConnectionStatusIcon.setVisibility(View.GONE);
					authConnectionLayout.setVisibility(View.GONE);
					authConnectionStatusIcon.setVisibility(View.GONE);
					btnConnect.setEnabled(false);
					break;
				case TASK_ENDS:
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][TASK_ENDS]");
					mTitle.setText(R.string.text_connected);
					connectionStatusDivider.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);
					connectionStatusText.setVisibility(View.VISIBLE);
					connectionStatusText.setText(R.string.server_connect_connected_text);
					btnCancel.setEnabled(false);
					break;
			}
		}
		
		/**
		 * @param taskStatus the taskStatus to set
		 */
		public void setTaskStatus(int taskStatus) {
			this.taskStatus = taskStatus;
		}
		
		/**
		 * @return the taskStatus
		 */
		public int getTaskStatus() {
			return taskStatus;
		}
	}
}
