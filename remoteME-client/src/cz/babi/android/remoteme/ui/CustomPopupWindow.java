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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Custom popup window.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 */
public class CustomPopupWindow {
	protected Context context;
	protected PopupWindow popupWindow;
	protected View rootView;
	protected Drawable background = null;
	protected WindowManager windowManager;
	
	/**
	 * Constructor.
	 * 
	 * @param context Context
	 */
	public CustomPopupWindow(Context context) {
		this.context = context;
		popupWindow = new PopupWindow(context);
		
		popupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_OUTSIDE) {
					popupWindow.dismiss();
					
					return true;
				}
				
				return false;
			}
		});
		
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
	}
	
	/**
	 * On dismiss
	 */
	protected void onDismiss() {}
	
	/**
	 * On show
	 */
	protected void onShow() {}
	
	/**
	 * On pre show
	 */
	protected void preShow() {
		if(rootView==null)
			throw new IllegalStateException("setContentView was not called with a view to display.");
		
		onShow();
		
		if(background==null)
			popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources()));
		else
			popupWindow.setBackgroundDrawable(background);
		
		popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
		popupWindow.setTouchable(true);
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		
		popupWindow.setContentView(rootView);
	}
	
	/**
	 * Set background drawable.
	 * 
	 * @param background Background drawable.
	 */
	public void setBackgroundDrawable(Drawable background) {
		this.background = background;
	}
	
	/**
	 * Set content view.
	 * 
	 * @param root Root view.
	 */
	public void setContentView(View root) {
		rootView = root;
		
		popupWindow.setContentView(root);
	}
	
	/**
	 * Set content view.
	 * 
	 * @param layoutResID Resource id.
	 */
	public void setContentView(int layoutResID) {
		LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		setContentView(inflator.inflate(layoutResID, null));
	}
	
	/**
	 * Set listener on window dismissed.
	 * 
	 * @param listener
	 */
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		popupWindow.setOnDismissListener(listener);
	}
	
	/**
	 * Dismiss the popup window.
	 */
	public void dismiss() {
		popupWindow.dismiss();
	}
}