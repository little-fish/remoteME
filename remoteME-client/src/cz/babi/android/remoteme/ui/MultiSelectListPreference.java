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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;

/**
 * MultiSelectListPreference for devices running Android in the API earlier than level 11.
 * 
 * @author Krzysztof Suszyński
 * @author https://gist.github.com/cardil/4754571
 */
public class MultiSelectListPreference extends ListPreference {
	
	public static final String DEFAULT_SEPARATOR = "--";
	
	/* We will use this lists to filling from xml files. */
	public static ArrayList<CharSequence> listEntries = new ArrayList<CharSequence>();
	public static ArrayList<CharSequence> listEntryValues = new ArrayList<CharSequence>();
	
	private String separator;
	private boolean[] entryChecked;
	
	public MultiSelectListPreference(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		
		/* Here we need to set our list. */
		setEntries(listEntries.toArray(new CharSequence[listEntries.size()]));
		setEntryValues(listEntryValues.toArray(new CharSequence[listEntryValues.size()]));
		
		entryChecked = new boolean[getEntries().length];
		separator = DEFAULT_SEPARATOR;
	}
	
	public MultiSelectListPreference(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();
		if (entries == null || entryValues == null
				|| entries.length != entryValues.length) {
			throw new IllegalStateException(
					"MultiSelectListPreference requires an entries array and an entryValues "
							+ "array which are both the same length");
		}
		
		restoreCheckedEntries();
		OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean val) {
				entryChecked[which] = val;
			}
		};
		builder.setMultiChoiceItems(entries, entryChecked, listener);
	}
	
	private CharSequence[] unpack(CharSequence val) {
		if (val == null || "".equals(val)) {
			return new CharSequence[0];
		} else {
			return ((String) val).split(separator);
		}
	}
	
	/**
	 * Gets the entries values that are selected
	 * 
	 * @return the selected entries values
	 */
	public CharSequence[] getCheckedValues() {
		return unpack(getValue());
	}
	
	private void restoreCheckedEntries() {
		CharSequence[] entryValues = getEntryValues();
		
		// Explode the string read in sharedpreferences
		CharSequence[] vals = unpack(getValue());
		
		if (vals != null) {
			List<CharSequence> valuesList = Arrays.asList(vals);
			for (int i = 0; i < entryValues.length; i++) {
				CharSequence entry = entryValues[i];
				entryChecked[i] = valuesList.contains(entry);
			}
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		List<CharSequence> values = new ArrayList<CharSequence>();
		
		CharSequence[] entryValues = getEntryValues();
		if (positiveResult && entryValues != null) {
			for (int i = 0; i < entryValues.length; i++) {
				if (entryChecked[i] == true) {
					String val = (String) entryValues[i];
					values.add(val);
				}
			}
			
			String value = join(values, separator);
			/* Disabled auto summary. */
			//			setSummary(prepareSummary(values));
			setValueAndEvent(value);
		}
	}
	
	private void setValueAndEvent(String value) {
		if (callChangeListener(unpack(value))) {
			setValue(value);
		}
	}
	
	@SuppressWarnings("unused")
	private CharSequence prepareSummary(List<CharSequence> joined) {
		List<String> titles = new ArrayList<String>();
		CharSequence[] entryTitle = getEntries();
		CharSequence[] entryValues = getEntryValues();
		int ix = 0;
		for (CharSequence value : entryValues) {
			if (joined.contains(value)) {
				titles.add((String) entryTitle[ix]);
			}
			ix += 1;
		}
		return join(titles, ", ");
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray typedArray, int index) {
		return typedArray.getTextArray(index);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue,
			Object rawDefaultValue) {
		String value = null;
		CharSequence[] defaultValue;
		if (rawDefaultValue == null) {
			defaultValue = new CharSequence[0];
		} else {
			defaultValue = (CharSequence[]) rawDefaultValue;
		}
		List<CharSequence> joined = Arrays.asList(defaultValue);
		String joinedDefaultValue = join(joined, separator);
		if (restoreValue) {
			value = getPersistedString(joinedDefaultValue);
		} else {
			value = joinedDefaultValue;
		}
		
		/* Disabled auto summary. */
		//		setSummary(prepareSummary(Arrays.asList(unpack(value))));
		setValueAndEvent(value);
	}
	
	/**
	 * Joins array of object to single string by separator
	 * 
	 * Credits to kurellajunior on this post
	 * http://snippets.dzone.com/posts/show/91
	 * 
	 * @param iterable
	 *            any kind of iterable ex.: <code>["a", "b", "c"]</code>
	 * @param separator
	 *            separetes entries ex.: <code>","</code>
	 * @return joined string ex.: <code>"a,b,c"</code>
	 */
	protected static String join(Iterable<?> iterable, String separator) {
		Iterator<?> oIter;
		if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
			return "";
		StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
		while (oIter.hasNext())
			oBuilder.append(separator).append(oIter.next());
		return oBuilder.toString();
	}
	
}