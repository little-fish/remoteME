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

import java.util.ArrayList;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.connection.UDPScanModeTask;
import cz.babi.android.remoteme.data.MySQLiteOpenHelper;
import cz.babi.android.remoteme.data.ServerArrayAdapter;
import cz.babi.android.remoteme.entity.Server;

/**
 * Activity represents searching dialog.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityDialogServerSearch extends FragmentActivity {
	
	private static final String TAG_CLASS_NAME =
			ActivityDialogServerSearch.class.getSimpleName();
	
	SearchingFragment fragment = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_server_search);
		
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
	public static class SearchingFragment extends ListFragment {
		
		private static final String TAG_CLASS_NAME = SearchingFragment.class.getSimpleName();
		
		private View fragmentView = null;
		
		private Button btnSearch = null;
		private Button btnCancel = null;
		private ProgressBar mProgressBar = null;
		private TextView mTextViewTitle = null;
		private LinearLayout searchLayout = null;
		
		/* We will use this constants for progress update. */
		public static final int TASK_NOT_RUNNING = 0;
		public static final int TASK_RUNNING = 1;
		public static final int TASK_ENDS = 2;
		
		private int taskStatus = TASK_NOT_RUNNING;
		
		private UDPScanModeTask scanModeTask = null;
		private ArrayList<Server> foundedServers = new ArrayList<Server>();
		private ServerArrayAdapter serverArrayAdapter = null;
		
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
			
			this.fragmentView = inflater.inflate(R.layout.fragment_server_search, container);
			
			btnSearch = (Button)this.fragmentView.findViewById(
					R.id.server_search_btn_repeat);
			btnSearch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					taskStatus = TASK_NOT_RUNNING;
					startScanTask();
				}
			});
			
			btnCancel = (Button)this.fragmentView.findViewById(
					R.id.server_search_btn_cancel);
			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(scanModeTask!=null) scanModeTask.stopTask();
					getActivity().finish();
				}
			});
			
			mProgressBar = (ProgressBar)this.fragmentView.findViewById(
					R.id.server_search_progress_bar);
			
			mTextViewTitle = (TextView)this.fragmentView.findViewById(
					R.id.server_search_title);
			
			searchLayout = (LinearLayout)this.fragmentView.findViewById(R.id.search_layout);
			
			setRetainInstance(true);
			
			return this.fragmentView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onActivityCreated]");
			
			super.onActivityCreated(savedInstanceState);
			
			if(serverArrayAdapter==null) {
				serverArrayAdapter = new ServerArrayAdapter(
						getActivity(), R.layout.row_server_scan, foundedServers,
						MySQLiteOpenHelper.getInstance(getActivity()).getAllServers());
				setListAdapter(serverArrayAdapter);
			} else if(taskStatus==TASK_RUNNING)
				getListView().getEmptyView().setVisibility(View.GONE);
			
			startScanTask();
		}
		
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onListItemClick][Clicked on position '" + position + "' > '"
					+ ((Server)getListAdapter().getItem(position)).getIpAddress() + ":"
					+ ((Server)getListAdapter().getItem(position)).getPort() + "']");
			
			super.onListItemClick(l, v, position, id);
			
			Server clickedServer = (Server)getListAdapter().getItem(position);
			
			Intent mIntentCreateNewServer = new Intent(getActivity(),
					ActivityDialogServerDetails.class);
			mIntentCreateNewServer.putExtra("IS_NEW", false);
			mIntentCreateNewServer.putExtra("IS_NEW_FROM_SCAN", true);
			mIntentCreateNewServer.putExtra("SERVER", clickedServer);
			getActivity().startActivity(mIntentCreateNewServer);
			getActivity().finish();
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onSaveInstanceState]");
			
			super.onSaveInstanceState(outState);
			
			this.fragmentView = this.getView();
		}
		
		/**
		 * Simply start new scan task (if need it).
		 */
		public void startScanTask() {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[startTask]");
			
			if(taskStatus==TASK_NOT_RUNNING) {
				scanModeTask = new UDPScanModeTask(this);
				scanModeTask.execute();
			}
		}
		
		/**
		 * This method is call to prepare widgets.
		 */
		public void updateUI() {
			/* If task is running we need to show progress dialog end update some widgets. */
			if(taskStatus==TASK_RUNNING) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][TASK_RUNNING]");
				mTextViewTitle.setText(R.string.server_search_title_searching_text);
				
				mProgressBar.setVisibility(View.VISIBLE);
				
				btnSearch.setEnabled(false);
				
				searchLayout.setVisibility(View.VISIBLE);
				
				serverArrayAdapter.clear();
				serverArrayAdapter.notifyDataSetChanged();
				
				getListView().getEmptyView().setVisibility(View.GONE);
			} else {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateUI][TASK_NOT_RUNNING]");
				mTextViewTitle.setText(R.string.server_search_title_founded_servers_text);
				
				mProgressBar.setVisibility(View.GONE);
				
				btnSearch.setEnabled(true);
				
				scanModeTask = null;
				
				/* If user canceled searching task. */
				if(foundedServers!=null) {
					if(!foundedServers.isEmpty())
						searchLayout.setVisibility(View.GONE);
					
					serverArrayAdapter.clear();
					for(Server s : foundedServers)
						serverArrayAdapter.insert(s, serverArrayAdapter.getCount());
					serverArrayAdapter.notifyDataSetChanged();
				}
			}
		}
		
		/**
		 * @param taskStatus the taskStatus to set
		 */
		public void setTaskStatus(int taskStatus) {
			this.taskStatus = taskStatus;
		}
		
		/**
		 * @param foundedServers List of founded servers.
		 */
		public void setFoundedServers(ArrayList<Server> foundedServers) {
			this.foundedServers = foundedServers;
		}
	}
}
