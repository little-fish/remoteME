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

package cz.babi.android.remoteme.entity;

import java.util.Comparator;

import android.content.Context;
import android.util.Log;
import cz.babi.android.remoteme.common.Common;

/**
 * Remote controller comparator.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class RemoteControllerComparator implements Comparator<RemoteController> {
	
	private static final String TAG_CLASS_NAME =
			RemoteControllerComparator.class.getSimpleName();
	
	private Context context;
	
	/**
	 * Constructor.
	 * @param context Context.
	 */
	public RemoteControllerComparator(Context context) {
		this.context = context;
	}
	
	@Override
	public int compare(RemoteController first, RemoteController second) {
		
		/* Obtain first title. */
		String firstTitle = first.getTitle();
		if(first.getTitle().startsWith("string_")) {
			firstTitle = getStringResourceByName(first.getTitle().substring(
					first.getTitle().indexOf("_")+1));
			if(firstTitle.equals("")) firstTitle = first.getTitle();
		}
		
		/* Obtain second title. */
		String secondTitle = second.getTitle();
		if(second.getTitle().startsWith("string_")) {
			secondTitle = getStringResourceByName(second.getTitle().substring(
					second.getTitle().indexOf("_")+1));
			if(secondTitle.equals("")) secondTitle = second.getTitle();
		}
		
		return firstTitle.toLowerCase().compareTo(secondTitle.toLowerCase());
	}
	
	/**
	 * Get String resource by its name.
	 * @param resName Resource name.
	 * @return Resource.
	 */
	private String getStringResourceByName(String resName) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getStringResourceByName][" + resName + "]");
		
		String packageName = context.getPackageName();
		
		int resId = context.getResources().getIdentifier(resName, "string", packageName);
		
		if(resId==0) return "";
		else return context.getString(resId);
	}
}
