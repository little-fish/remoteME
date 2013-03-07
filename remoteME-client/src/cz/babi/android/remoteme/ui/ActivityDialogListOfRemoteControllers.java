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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.data.RemoteControllerArrayAdapter;
import cz.babi.android.remoteme.entity.RemoteController;
import cz.babi.android.remoteme.entity.RemoteControllerComparator;
import cz.babi.android.remoteme.service.ConnectionService;

/**
 * Activity represents list of remote controllers.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityDialogListOfRemoteControllers extends FragmentActivity {
	
	private static final String TAG_CLASS_NAME =
			ActivityDialogListOfRemoteControllers.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_list_of_remote_controllers);
		
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
	 * This fragment holds list of remote controllers.
	 *
	 * @author Martin Misiarz
	 * @author dev.misiarz@gmail.com
	 */
	public static class RemoteControllersFragment extends ListFragment implements OnItemClickListener {
		
		private static final String TAG_CLASS_NAME = RemoteControllersFragment.class.getSimpleName();
		
		private ConnectionService connectionService;
		private ServiceConnection serviceConnection;
		
		private boolean isServiceBound = false;
		
		private Button btnCancel = null;
		
		private View fragmentView = null;
		
		private RemoteControllerArrayAdapter rcArrayAdapter = null;
		
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
			
			this.fragmentView = inflater.inflate(R.layout.fragment_remote_controllers, container, false);
			
			btnCancel = (Button)this.fragmentView.findViewById(
					R.id.remote_controllers_btn_cancel);
			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});
			
			setRetainInstance(true);
			
			/* Create new service connection. */
			serviceConnection = new ServiceConnection() {
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onServiceConnected]");
					connectionService = ((ConnectionService.ConnectionBinder)service).getService();
				}
				
				@Override
				public void onServiceDisconnected(ComponentName name) {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onServiceDisconnected]");
					connectionService = null;
				}
			};
			
			return this.fragmentView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onViewCreated]");
			
			/* Bind service. */
			bindService();
			
			/* Obtain an adapter. */
			rcArrayAdapter = RemoteControllerArrayAdapter.getInstance(getActivity(),
					R.layout.row_remote_controller);
			
			/* Sort adapter. */
			rcArrayAdapter.sort(new RemoteControllerComparator(getActivity()));
			
			/* Set adapter. */
			setListAdapter(rcArrayAdapter);
			
			/* Set listener to list view. */
			getListView().setOnItemClickListener(this);
			
			super.onActivityCreated(savedInstanceState);
		}
		
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			/* Clicked remote controller. */
			RemoteController remoteController = rcArrayAdapter.getController(position);
			
			/* Because controller for mouse and keyboard is not define in xml we need to start its
			 * activity in different way. */
			if(remoteController.getTitle().equals(
					getActivity().getResources().getString(
							R.string.preferences_category_mouse_and_keyboard_text)) &&
							remoteController.getAuthor().equals(
									getActivity().getResources().getString(R.string.text_app_author)) &&
									remoteController.getRows()==null) {
				Intent mouseAndKeyboard = new Intent(getActivity(),
						ActivityRemoteMouseAndKeyboard.class);
				
				getActivity().startActivity(mouseAndKeyboard);
			} else {
				/* Need to put remote controller object to intent. */
				Intent remoteIntent = new Intent(getActivity(), ActivityRemoteController.class);
				remoteIntent.putExtra("remoteController", remoteController);
				getActivity().startActivity(remoteIntent);
			}
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onSaveInstanceState]");
			
			super.onSaveInstanceState(outState);
			
			this.fragmentView = this.getView();
			
			/* Need to unbind service. */
			unbindService();
		}
		
		@Override
		public void onResume() {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onResume]");
			
			/* Here we need to check, if service is connected to server.
			 * If not - finish activity. */
			if(connectionService!=null)
				if(connectionService.isDisconnectWithError()) getActivity().finish();
			
			super.onResume();
		}
		
		@Override
		public void onDestroy() {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onDestroy]");
			
			super.onDestroy();
			
			/* Need to unbind service. */
			unbindService();
			
			/* Service is no needed anymore. */
			connectionService.stopSelf();
		}
		
		/**
		 * Bind service.
		 */
		private void bindService() {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[bindService]");
			
			/* Establish a connection with the service.  We use an explicit
			 * class name because we want a specific service implementation that
			 * we know will be running in our own process (and thus won't be
			 * supporting component replacement by other applications). */
			getActivity().bindService(new Intent(getActivity(),
					ConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
			isServiceBound = true;
		}
		
		/**
		 * Unbind service.
		 */
		void unbindService() {
			if(isServiceBound) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[unbindService]");
				
				/* Detach our existing connection. */
				getActivity().unbindService(serviceConnection);
				isServiceBound = false;
			}
		}
	}
}
