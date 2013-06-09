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

package cz.babi.android.remoteme;

import java.io.File;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.data.DatabaseCursorAdapter;
import cz.babi.android.remoteme.data.MySQLiteOpenHelper;
import cz.babi.android.remoteme.data.RemoteControllerArrayAdapter;
import cz.babi.android.remoteme.data.XMLCopyingTask;
import cz.babi.android.remoteme.data.XMLParsingTask;
import cz.babi.android.remoteme.ui.Actionbar;
import cz.babi.android.remoteme.ui.ActivityAbout;
import cz.babi.android.remoteme.ui.ActivityPreferences;
import cz.babi.android.remoteme.ui.MultiSelectListPreference;

/**
 * Class represents main activity.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityMain extends ListActivity {

	private static final String TAG_CLASS_NAME = ActivityMain.class.getSimpleName();

	/** This variables are using for checking if application needs to create
	 * some preferences. */
	//TODO: Do not forget to increase preferences version!!!
	private static final int SHARED_PREFERENCES_VERSION = 1;

	/** Shared preferences for this application. */
	private SharedPreferences preferences;

	public static final String BROADCAST_ACTION_REFRESH_SERVERS =
			"cz.babi.android.remoteme.action.refreshservers";

	public static final Intent BROADCAST_INTENT_REFRESH_SERVERS =
			new Intent(BROADCAST_ACTION_REFRESH_SERVERS);

	/** When onResume() is called we need to check if we need to refresh list with servers. */
	public static boolean needRefreshServers = false;

	private MySQLiteOpenHelper databaseHelper;
	private Cursor serverCursor;
	private DatabaseCursorAdapter databaseCursorAdapter;

	/* If there is 0 then we need to copy some files to sd card.*/
	private int appStartPrefVersion = 0;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		/* Set debug mode. */
		setDebugMode();

		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");

		/* Set orientation. */
		setOrientation();

		appStartPrefVersion = getSharedPrefVersion();

		/* We need to check if app is start for the first time. If so, we need to
		 * prepare some preferences and folders. */
		if(savedInstanceState==null) {
			/* Check preferences. */
			if(checkFirstStart(appStartPrefVersion)) preparePreferences();
			/* Check folders. */
			if(!checkFoldersOnSDCard()) createFoldersOnSDCard();
		}

		setContentView(R.layout.activity_main);

		this.databaseHelper = MySQLiteOpenHelper.getInstance(this.getApplicationContext());

		int orderBy = preferences.getInt(getString(R.string.pref_name_server_order),
				getResources().getInteger(R.integer.pref_value_order_by_name));
		reOrderCursor(orderBy);
		startManagingCursor(this.serverCursor);

		String[] from = new String[] { MySQLiteOpenHelper.COLUMN_IP_ADDRESS,
				MySQLiteOpenHelper.COLUMN_PORT, MySQLiteOpenHelper.COLUMN_SERVER_NAME,
				MySQLiteOpenHelper.COLUMN_PASSWORD, MySQLiteOpenHelper.COLUMN_OS_NAME};

		int[] to = new int[] {R.id.serverrow_text_server_address,
				R.id.serverrow_text_server_address, R.id.serverrow_text_server_name,
				R.id.serverrow_icon_locked, R.id.serverrow_icon_os_type};

		this.databaseCursorAdapter = new DatabaseCursorAdapter(
				this, R.layout.row_server_main, this.serverCursor, from, to);

		setListAdapter(this.databaseCursorAdapter);

		/* Set empty view to list view. */
		TextView emptyView = (TextView)findViewById(R.id.main_empty);
		getListView().setEmptyView(emptyView);

		/* We need to set proper background. We will check aspect ration of screen resolution. */
		LinearLayout activityLayout = (LinearLayout)findViewById(R.id.activity_main_layout);
		Drawable back;

		int width = Common.getDisplayWidth(this);
		int height = Common.getDisplayHeight(this);

		/*
		 * Known ratios:
		 * 320x480 = 1.5
		 * 480x800 = 1.6666
		 * 1280x720 = 1.6652
		 */
		double ratio = (double)height/width;
		if(ratio>1.6) {
			/* For 1280x720, and so on.. */
			back = getResources().getDrawable(R.drawable.background);
		} else {
			/* For 1280x800, and so on.. */
			back = getResources().getDrawable(R.drawable.background_w);
		}

		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
			activityLayout.setBackground(back);
		} else activityLayout.setBackgroundDrawable(back);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreateOptionsMenu]");

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_activity_main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_activity_main_preferences:
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onOptionsItemSelected][Preferences.]");
			startActivityForResult(new Intent(this, ActivityPreferences.class), 0);
			return true;
		case R.id.menu_activity_main_about:
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onOptionsItemSelected][About.]");
			startActivity(new Intent(this, ActivityAbout.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onActivityResult]");

		/* Set orientation after preferences activity finish. */
		setOrientation();

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Broadcast receiver which will receive intent to reorder servers.
	 */
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onReceive]");

			int orderBy;
			Bundle orderByBundle = intent.getExtras();

			if(orderByBundle==null) orderBy = preferences.getInt(
					getString(R.string.pref_name_server_order),
					getResources().getInteger(R.integer.pref_value_order_by_name));
			else orderBy = orderByBundle.getInt(Actionbar.ORDER_BY,
					getResources().getInteger(R.integer.pref_value_order_by_name));

			reOrderCursor(orderBy);

			startManagingCursor(serverCursor);
			databaseCursorAdapter.changeCursor(serverCursor);
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onResume]");

		super.onResume();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BROADCAST_ACTION_REFRESH_SERVERS);
		this.registerReceiver(this.broadcastReceiver, intentFilter);

		if(needRefreshServers) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onResume][Refreshing cursor...]");
			reOrderCursor(preferences.getInt(
					getString(R.string.pref_name_server_order),
					getResources().getInteger(R.integer.pref_value_order_by_name)));

			startManagingCursor(serverCursor);
			databaseCursorAdapter.changeCursor(serverCursor);

			needRefreshServers = false;
		}

		/* If there is no adapter created we need to start parsing task.
		 * Need to parsing data when app is starting. With that there is no
		 * delay when showing dialog with controllers. */
		if(RemoteControllerArrayAdapter.isInstanceNull()) {
			/* Files are copied to sd card ONLY at FIRST start of app ever.
			 * After that copy it starts xml parsing.
			 * If app was already started before we can start parsing xml. */
			if(needCopyControllersToSD(appStartPrefVersion)) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onResume][Need copy xml " +
						"files to sd card.]");

				XMLCopyingTask xmlCopyingTask = new XMLCopyingTask(this);
				xmlCopyingTask.execute();

				appStartPrefVersion = getSharedPrefVersion();
			} else {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onResume][Do not need copy xml " +
						"files to sd card. Just start parsing.]");
				XMLParsingTask xmlParsingTask = new XMLParsingTask(this);
				xmlParsingTask.execute();
			}
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPause() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onPause]");

		super.onPause();

		stopManagingCursor(serverCursor);

		this.unregisterReceiver(this.broadcastReceiver);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void finish() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[finish]");

		/* Clear adapter. */
		RemoteControllerArrayAdapter.setInstanceNull();

		/* When finishing activity the empty view always appear.
		 * Its due to removing cursor from lis view. So simple set
		 * empty text to this view. */
		TextView emptyView = (TextView)findViewById(R.id.main_empty);
		emptyView.setText("");

		this.databaseHelper.close();
		stopManagingCursor(serverCursor);
		this.serverCursor.close();

		super.finish();
	}

	/**
	 * Get version of shared preferences.
	 * @return Version of shared preferences.
	 */
	private int getSharedPrefVersion() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[checkFirstStart]");

		return preferences.getInt(
				getString(R.string.pref_name_shared_pref_version), 0);
	}

	/**
	 * Check if we need to prepare some preferences.
	 * @return Need to create some preferences?
	 */
	private boolean checkFirstStart(int preferencesVersion) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[checkFirstStart]");

		if(preferencesVersion<SHARED_PREFERENCES_VERSION) return true;
		else return false;
	}

	/**
	 * Do we need copy remote controllers to sd card.
	 * @param preferencesVersion Version of shared preferences.
	 * @return Need copy files. Return true if there is no shared preferences.
	 */
	private boolean needCopyControllersToSD(int preferencesVersion) {
		//		return preferencesVersion==0;
		/* In this version we do not need copy controllers to sd card.
		 * It will be enabled in feature. */
		return false;
	}

	/**
	 * Method prepare some preferences.
	 */
	private void preparePreferences() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[preparePreferences]");

		SharedPreferences.Editor editor = preferences.edit();

		editor.putInt(getString(R.string.pref_name_shared_pref_version),
				SHARED_PREFERENCES_VERSION);
		editor.putInt(getString(R.string.pref_name_server_order),
				getResources().getInteger(R.integer.pref_value_order_by_name));
		editor.putInt(getString(R.string.pref_name_socket_timeout),
				Common.DEFAULT_SOCKET_TIMEOUT);
		editor.putInt(getString(R.string.pref_name_mouse_wheel_smooth),
				Common.DEFAULT_MOUSE_WHEEL_SMOOTH);
		editor.putString(getString(R.string.pref_name_udp_scan_mode_port),
				String.valueOf(Common.DEFAULT_CONNECTION_PORT));
		editor.putString(getString(R.string.pref_name_keyboard_simulation),
				getString(R.string.pref_value_clipboard));
		editor.putBoolean(getString(R.string.pref_name_keep_wifi_alive), true);
		editor.putBoolean(getString(R.string.pref_name_keep_device_awake), false);
		editor.putString(getString(R.string.pref_name_orientation_lock),
				getString(R.string.pref_value_default));
		editor.putBoolean(getString(R.string.pref_name_show_notification), true);
		editor.putBoolean(getString(R.string.pref_name_debug_mode), false);

		String visibleRemotes[] = getResources().getStringArray(
				R.array.default_visible_remotes_values);
		StringBuilder sbVisibleRemote = new StringBuilder();
		/* Iterate every visible remotes and append string. */
		for(int i=0; i<visibleRemotes.length; i++) {
			sbVisibleRemote.append(visibleRemotes[i]);
			if(i<visibleRemotes.length-1) sbVisibleRemote.append(
					MultiSelectListPreference.DEFAULT_SEPARATOR);
		}
		editor.putString(getString(R.string.pref_name_visible_remotes), sbVisibleRemote.toString());

		editor.commit();

		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[preparePreferences][All necessary " +
				"preferences was created.]");
	}

	/**
	 * Check if needed folders exist on sd card.
	 * @return True if needed folders exist.
	 */
	private boolean checkFoldersOnSDCard() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[checkFoldersOnSDCard]");

		File sdCard = Environment.getExternalStorageDirectory();

		File appFolder = new File(sdCard.getAbsolutePath(), Common.APP_FOLDER);

		if(appFolder.exists()) {
			File icoFolder = new File(appFolder.getAbsolutePath(), Common.ICON_FOLDER);

			if(icoFolder.exists()) return true;
			else return false;
		} else return false;
	}

	/**
	 * Create folders on sd card.
	 */
	private void createFoldersOnSDCard() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[createFoldersOnSDCard]");

		File sdCard = Environment.getExternalStorageDirectory();

		File icoFolder = new File(sdCard.getAbsolutePath() + "/" +
				Common.APP_FOLDER + "/" + Common.ICON_FOLDER);

		if(!icoFolder.exists()) icoFolder.mkdirs();
	}

	/**
	 * Set orientation.
	 */
	private void setOrientation() {
		String currentOrientationLock = preferences.
				getString(getString(R.string.pref_name_orientation_lock),
						getString(R.string.pref_value_default));

		if(currentOrientationLock.equals(getString(R.string.pref_value_portait))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if(currentOrientationLock.equals(getString(R.string.pref_value_landscape))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}

	/**
	 * Set debug mode.
	 */
	private void setDebugMode() {
		boolean debugMode = preferences.
				getBoolean(getString(R.string.pref_name_debug_mode), false);

		if(debugMode) {
			Common.DEBUG = true;
			Common.WARN = true;
			Common.ERROR = true;
		} else {
			Common.DEBUG = false;
			Common.WARN = false;
			Common.ERROR = false;
		}
	}

	/**
	 * Method will reorder Cursor.
	 * @param orderBy Order by.
	 */
	private void reOrderCursor(int orderBy) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[reOrderCursor]");

		if(orderBy==getResources().getInteger(R.integer.pref_value_order_by_name)) {
			serverCursor = databaseHelper.getCursorForAllServers(
					MySQLiteOpenHelper.COLUMN_SERVER_NAME);
		} else if(orderBy==getResources().getInteger(R.integer.pref_value_order_by_last_connected)) {
			serverCursor = databaseHelper.getCursorForAllServers(
					MySQLiteOpenHelper.COLUMN_LAST_CONNECTED + " DESC");
		} else if(orderBy==getResources().getInteger(R.integer.pref_value_order_by_ip_address)) {
			serverCursor = databaseHelper.getCursorForAllServers(
					MySQLiteOpenHelper.COLUMN_IP_ADDRESS);
		} else if(orderBy==getResources().getInteger(R.integer.pref_value_order_by_os_type)) {
			serverCursor = databaseHelper.getCursorForAllServers(
					MySQLiteOpenHelper.COLUMN_OS_NAME);
		} else {
			serverCursor = databaseHelper.getCursorForAllServers(
					MySQLiteOpenHelper.COLUMN_SERVER_NAME);
		}
	}
}