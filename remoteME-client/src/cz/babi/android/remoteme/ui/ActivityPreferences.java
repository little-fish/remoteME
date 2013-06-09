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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.data.XMLParsingTask;

/**
 * Preferene Activity
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private EditTextPreference udpScanModePort;
	private ListPreference keyboardSimulation;
	private ListPreference orientationLock;
	private SeekBarPreference seekBarPreference;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.activity_preferences);

		udpScanModePort = (EditTextPreference)getPreferenceScreen().
				findPreference(getString(R.string.pref_name_udp_scan_mode_port));
		/* Need to set summary contains current port. */
		setUdpScanModePortSummary(PreferenceManager.getDefaultSharedPreferences(this));
		/* Need to set listener for validation. */
		udpScanModePort.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int newPort = Integer.valueOf(String.valueOf(newValue));

				/* Simply check if user entered right port number. If not, new value
				 * will not be saved. */
				if(newPort>47808 || newPort<1) return false;
				else return true;
			}
		});

		keyboardSimulation = (ListPreference)getPreferenceScreen().
				findPreference(getString(R.string.pref_name_keyboard_simulation));
		/* Set proper summary. */
		setKeyboardSimulationSummary(PreferenceManager.getDefaultSharedPreferences(this));
		/* Set proper summary to orientation lock. */
		orientationLock = (ListPreference)getPreferenceManager().
				findPreference(getString(R.string.pref_name_orientation_lock));
		setOrientationLockSummary(PreferenceManager.getDefaultSharedPreferences(this));

		/* This is our custom seekbar preference. */
		seekBarPreference = (SeekBarPreference)getPreferenceScreen().
				findPreference(getString(R.string.pref_name_mouse_wheel_smooth));
		/* Set proper summary to mouse wheel smooth */
		setMouseWheelSmoothSummary(PreferenceManager.getDefaultSharedPreferences(this));

		setOrientation();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		/* Update UDP port summary. */
		if(key.compareTo(getString(R.string.pref_name_udp_scan_mode_port))==0) {
			setUdpScanModePortSummary(sharedPreferences);
		} else
			/* Update Keyboard simulation summary. */
			if(key.compareTo(getString(R.string.pref_name_keyboard_simulation))==0) {
				setKeyboardSimulationSummary(sharedPreferences);
			} else
				/* Update mouse wheel scroll speed summary. */
				if(key.compareTo(getString(R.string.pref_name_mouse_wheel_smooth))==0) {
					setMouseWheelSmoothSummary(sharedPreferences);
				} else
					/* Update orientation lock summary. */
					if(key.compareTo(getString(R.string.pref_name_orientation_lock))==0) {
						setOrientationLockSummary(sharedPreferences);
					} else
						/* Set debug mode. */
						if(key.compareTo(getString(R.string.pref_name_debug_mode))==0) {
							setDebugMode(sharedPreferences);
						} else
							/* If user change visible remtotes we need to parsing them again and fill
							 * adapter with new data. */
							if(key.compareTo(getString(R.string.pref_name_visible_remotes))==0) {
								XMLParsingTask xmlParsingTask = new XMLParsingTask(this);
								xmlParsingTask.execute();
							}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();

		/* Unregister listener. */
		getPreferenceScreen().getSharedPreferences().
		unregisterOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		/* Register listener. */
		getPreferenceScreen().getSharedPreferences().
		registerOnSharedPreferenceChangeListener(this);
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
	 * Set UDP scan mode port summary.
	 * @param sharedPreferences Shared preferences.
	 */
	private void setUdpScanModePortSummary(SharedPreferences sharedPreferences) {
		String currentPort = sharedPreferences.getString(
				getString(R.string.pref_name_udp_scan_mode_port), String.valueOf(4449));
		udpScanModePort.setSummary("(" + currentPort + ") " +
				getString(R.string.preferences_item_udp_port_summary));
	}

	/**
	 * Set Keyboard simulation summary.
	 * @param sharedPreferences Shared preferences.
	 */
	private void setKeyboardSimulationSummary(SharedPreferences sharedPreferences) {
		String currentKeyboadSimulation = sharedPreferences.
				getString(getString(R.string.pref_name_keyboard_simulation),
						getString(R.string.pref_value_clipboard));
		if(currentKeyboadSimulation.compareTo(getString(R.string.pref_value_clipboard))==0) {
			keyboardSimulation.setSummary("(" +
					getString(R.string.preferences_value_clipboard_simulation_text) + ") " +
					getString(R.string.preferences_value_clipboard_simulation_summary));
		} else {
			keyboardSimulation.setSummary("(" +
					getString(R.string.preferences_value_key_stroke_simulation_text) + ") " +
					getString(R.string.preferences_value_key_stroke_simulation_summary));
		}
	}

	/**
	 * Set Mouse wheel smooth summary.
	 * @param sharedPreferences Shared preferences.
	 */
	private void setMouseWheelSmoothSummary(SharedPreferences sharedPreferences) {
		seekBarPreference.setSummary("(" + sharedPreferences.getInt(
				getString(R.string.pref_name_mouse_wheel_smooth), 40) + ") " +
				getString(R.string.preferences_item_mouse_wheel_summary));
	}

	/**
	 * Set Orientation lock summary.
	 * @param sharedPreferences Shared preferences.
	 */
	private void setOrientationLockSummary(SharedPreferences sharedPreferences) {
		String currentOrientationLock = sharedPreferences.
				getString(getString(R.string.pref_name_orientation_lock),
						getString(R.string.pref_value_default));
		if(currentOrientationLock.equals(getString(R.string.pref_value_default))) {
			orientationLock.setSummary("(" +
					getString(R.string.preferences_value_orientation_default_text) + ") " +
					getString(R.string.preferences_item_orientation_lock_summary));
		} else if(currentOrientationLock.equals(getString(R.string.pref_value_portait))) {
			orientationLock.setSummary("(" +
					getString(R.string.preferences_value_orientation_portrait_text) + ") " +
					getString(R.string.preferences_item_orientation_lock_summary));
		} else {
			orientationLock.setSummary("(" +
					getString(R.string.preferences_value_orientation_landscape_text) + ") " +
					getString(R.string.preferences_item_orientation_lock_summary));
		}
	}

	/**
	 * Set debug mode.
	 * @param sharedPreferences Shared preferences.
	 */
	private void setDebugMode(SharedPreferences sharedPreferences) {
		boolean debugMode = sharedPreferences.
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
}
