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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * ActionItem, displayed as menu with icon and text.
 * 
 * @author Lorensius. W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 */
public class ActionItem {
	private Drawable icon;
	private Bitmap thumb;
	private String title;
	private int actionId = -1;
	private boolean selected;
	private boolean sticky;
	
	/**
	 * Constructor.
	 * 
	 * @param actionId ActionItem id for case statements.
	 * @param title Title.
	 * @param icon Icon to use.
	 */
	public ActionItem(int actionId, String title, Drawable icon) {
		this.title = title;
		this.icon = icon;
		this.actionId = actionId;
	}
	
	/**
	 * Constructor.
	 */
	public ActionItem() {
		this(-1, null, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param actionId ActionItem id of the item.
	 * @param title Text to show for the item.
	 */
	public ActionItem(int actionId, String title) {
		this(actionId, title, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param icon {@link Drawable} ActionItem icon.
	 */
	public ActionItem(Drawable icon) {
		this(-1, null, icon);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param actionId ActionItem ID of item.
	 * @param icon {@link Drawable} ActionItem icon.
	 */
	public ActionItem(int actionId, Drawable icon) {
		this(actionId, null, icon);
	}
	
	/**
	 * Set ActionItem title.
	 * 
	 * @param title ActionItem title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Get action title.
	 * 
	 * @return ActionItem title.
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Set ActionItem icon.
	 * 
	 * @param icon {@link Drawable} ActionItem icon.
	 */
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
	/**
	 * Get ActionItem icon.
	 * @return {@link Drawable} ActionItem icon.
	 */
	public Drawable getIcon() {
		return this.icon;
	}
	
	/**
	 * Set ActionItem id.
	 * 
	 * @param actionId ActionItem id for this ActionItem.
	 */
	public void setActionId(int actionId) {
		this.actionId = actionId;
	}
	
	/**
	 * @return Our ActionItem id.
	 */
	public int getActionId() {
		return actionId;
	}
	
	/**
	 * Set sticky status of button.
	 * 
	 * @param sticky True for sticky, pop up sends event but does not disappear.
	 */
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
	
	/**
	 * @return True if button is sticky, menu stays visible after press.
	 */
	public boolean isSticky() {
		return sticky;
	}
	
	/**
	 * Set selected flag.
	 * 
	 * @param selected Flag to indicate the item is selected.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Check if ActionItem is selected.
	 * 
	 * @return true or false.
	 */
	public boolean isSelected() {
		return this.selected;
	}
	
	/**
	 * Set thumb.
	 * 
	 * @param thumb Thumb image.
	 */
	public void setThumb(Bitmap thumb) {
		this.thumb = thumb;
	}
	
	/**
	 * Get thumb image.
	 * 
	 * @return Thumb image.
	 */
	public Bitmap getThumb() {
		return this.thumb;
	}
}