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

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;

/**
 * Simple seekbar preference screen.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class SeekBarPreference extends DialogPreference implements OnSeekBarChangeListener {
	
	private static final String TAG_CLASS_NAME = SeekBarPreference.class.getSimpleName();
	
	/* Real defaults */
	private final int defaultValue = 40;
	private final int maxValue = 100;
	private final int minValue = 0;
	
	/* Current value */
	private int currentValue;
	
	private SeekBar seekBar;
	private TextView currentValueText;
	
	/**
	 * Constructor.
	 * @param context Context.
	 * @param attrs AtributSet.
	 */
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected View onCreateDialogView() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreateDialogView]");
		
		currentValue = getPersistedInt(defaultValue);
		
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.seekbar, null);
		
		seekBar = (SeekBar)view.findViewById(R.id.seekbar);
		seekBar.setMax(maxValue - minValue);
		seekBar.setProgress(currentValue - minValue);
		seekBar.setOnSeekBarChangeListener(this);
		
		currentValueText = (TextView)view.findViewById(R.id.seekbar_current_value);
		currentValueText.setText(Integer.toString(currentValue));
		
		return view;
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int newValue = progress + minValue;
		
		if(newValue>maxValue) newValue = maxValue;
		else if(newValue<minValue) newValue = minValue;
		
		/* If new value is rejected, we need to revert to the previous value. */
		if(!callChangeListener(newValue)) {
			seekBar.setProgress(currentValue - minValue);
			return;
		}
		
		/* Store new value. */
		currentValue = newValue;
		persistInt(newValue);
		
		/* Show new value to user. */
		currentValueText.setText(String.valueOf(currentValue));
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onProgressChanged][" + currentValue + "]");
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onStartTrackingTouch]");
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onStopTrackingTouch]");
		
		notifyChanged();
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		int defValue = a.getInt(index, defaultValue);
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onGetDefaultValue][" + defValue + "]");
		
		return defValue;
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,	Object defaultValue) {
		if(restorePersistedValue) currentValue = getPersistedInt(currentValue);
		else {
			int defValue = this.defaultValue;
			try {
				defValue = (Integer)defaultValue;
			} catch(Exception ex) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[onSetInitialValue][Invalid default value][" +
						defaultValue.toString() + "]");
			}
			
			persistInt(defValue);
			currentValue = defValue;
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onSetInitialValue][" + currentValue + "]");
	}
}
