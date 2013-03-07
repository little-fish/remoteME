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

package cz.babi.android.remoteme.data;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.RemoteController;

/**
 * Adapter for remote controllers.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public final class RemoteControllerArrayAdapter extends ArrayAdapter<RemoteController> {
	
	private static final String TAG_CLASS_NAME =
			RemoteControllerArrayAdapter.class.getSimpleName();
	
	private static RemoteControllerArrayAdapter instance = null;
	
	private final Context context;
	private final int rowLayout;
	private ArrayList<RemoteController> remoteControllers;
	
	/* If we need to obtain icon from file located in sd card, we need
	 * scale this icon to proper size. */
	private final int properIconWidth;
	private final int properIconHeight;
	
	/**
	 * Return singleton instance.
	 * @param context Context.
	 * @param rowLayout Layout.
	 * @return Instance of RemoteControllerArrayAdapter.
	 */
	public static RemoteControllerArrayAdapter getInstance(Context context, int rowLayout) {
		if(instance==null) {
			instance = new RemoteControllerArrayAdapter(context, rowLayout,
					new ArrayList<RemoteController>());
		}
		
		return instance;
	}
	
	/**
	 * @return Return true if instance of adapter is null.
	 */
	public static boolean isInstanceNull() {
		return instance==null;
	}
	
	/**
	 * Set instance null.
	 */
	public static void setInstanceNull() {
		instance = null;
	}
	
	/**
	 * Private constructor.
	 * @param context Context.
	 * @param rowLayout Row layout.
	 * @param remoteControllers Remote controllers.
	 */
	private RemoteControllerArrayAdapter(Context context, int rowLayout,
			ArrayList<RemoteController> remoteControllers) {
		super(context, rowLayout, remoteControllers);
		
		this.context = context;
		this.rowLayout = rowLayout;
		this.remoteControllers = remoteControllers;
		
		Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.app_default);
		properIconWidth = defaultBitmap.getWidth();
		properIconHeight = defaultBitmap.getHeight();
	}
	
	/**
	 * Add Controller to adapter.
	 * @param remoteController
	 */
	public void addController(RemoteController remoteController) {
		remoteControllers.add(remoteController);
	}
	
	/**
	 * Return remote Controller on specific index.
	 * @param index Index.
	 * @return Remote controller.
	 */
	public RemoteController getController(int index) {
		return remoteControllers.get(index);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View row, ViewGroup parent) {
		
		if(row==null) {
			LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			row = layoutInflater.inflate(rowLayout, parent, false);
		}
		
		RemoteController rc = remoteControllers.get(position);
		
		if(rc!=null) {
			String rcTitle = rc.getTitle();
			String rcAuthor = rc.getAuthor();
			String rcIcon = rc.getIcon();
			
			TextView rcTitleTextView = (TextView)row.findViewById(R.id.remote_controller_title);
			if(rcTitle.length()!=0) {
				if(rcTitle.startsWith("string_")) {
					rcTitle = getStringResourceByName(
							rcTitle.substring(rcTitle.indexOf("_")+1));
				}
			}
			
			rcTitleTextView.setText(rcTitle);
			
			TextView rcAuthorTextView = (TextView)row.findViewById(R.id.remote_controller_author);
			if(rcAuthor.length()==0) rcAuthorTextView.setVisibility(View.GONE);
			else {
				rcAuthorTextView.setVisibility(View.VISIBLE);
				rcAuthorTextView.setText(context.getText(R.string.remote_controller_author_text) +
						" " + rcAuthor);
			}
			
			ImageView rcIconImageView = (ImageView)row.findViewById(R.id.remote_controller_icon);
			
			/*If there is no text in icon variable we can set default icon. */
			if(rcIcon.length()!=0) {
				String rcIconUri = "drawable/" + rcIcon;
				int rcIconId = context.getResources().getIdentifier(rcIconUri, null,
						context.getPackageName());
				
				/* If icon exists in drawable folder. */
				if(rcIconId!=0) {
					rcIconImageView.setBackgroundResource(rcIconId);
				} else {
					/* Need to check, if Icon exists in SD card. */
					File sdCard = Environment.getExternalStorageDirectory();
					
					/* Also need to add extension to filename. */
					String[] extensions = new String[]{ ".jpg", ".jpeg", ".png", ".bmp" };
					
					File iconFile = null;
					for(String extension : extensions) {
						iconFile = new File(sdCard.getAbsolutePath() + "/" +
								Common.APP_FOLDER + "/" + Common.ICON_FOLDER + "/" + rcIcon + extension);
						if(iconFile.exists()) break;
						else iconFile = null;
					}
					
					/* If icon is located on SD card. */
					if(iconFile!=null) {
						Bitmap iconBitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
						
						/* if Bitmap was created succesfully we need to scale it and create drawable from it. */
						if(iconBitmap!=null) {
							Bitmap scaledBitmap = Bitmap.createScaledBitmap(iconBitmap, properIconWidth, properIconHeight, false);
							BitmapDrawable drawable = new BitmapDrawable(context.getResources(), scaledBitmap);
							
							if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
								rcIconImageView.setBackground(drawable);
							} else rcIconImageView.setBackgroundDrawable(drawable);
						}
					} else
						/* If there is no icon, just set default icon. */
						rcIconImageView.setBackgroundResource(R.drawable.app_default);
				}
			} else
				/* If icon text, just set default icon. */
				rcIconImageView.setBackgroundResource(R.drawable.app_default);
		}
		
		return row;
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
