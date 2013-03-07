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
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.ActionItem;

/**
 * QuickAction dialog, shows action list as icon and text. Currently supports vertical
 * and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 */
public class QuickAction extends CustomPopupWindow implements OnDismissListener {
	private View rootView;
	private View currentView;
	private ImageView imgArrowUp;
	private ImageView imgArrowDown;
	private final LayoutInflater layoutInflater;
	private ViewGroup viewGroup;
	private ScrollView scrollView;
	private OnActionItemClickListener onActionItemClickListener;
	private OnDismissListener onDismissListener;
	
	private final List<ActionItem> actionItems = new ArrayList<ActionItem>();
	
	private boolean didAction;
	
	private int childPosition;
	private int insertPosition;
	private int animationStyle;
	private final int orientation;
	private int rootWidth=0;
	
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	public static final int ANIM_GROW_FROM_CENTER = 1;
	public static final int NO_ANIM = 2;
	
	/**
	 * Constructor for default vertical layout.
	 * 
	 * @param context Context.
	 */
	public QuickAction(Context context) {
		this(context, VERTICAL);
	}
	
	/**
	 * Constructor allowing orientation override.
	 * 
	 * @param context Context.
	 * @param orientation Layout orientation, can be vartical or horizontal.
	 */
	public QuickAction(Context context, int orientation) {
		super(context);
		
		this.orientation = orientation;
		
		layoutInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		
		if(orientation==HORIZONTAL) {
			setRootViewId(R.layout.quickaction_popup_horizontal);
		} if(orientation==VERTICAL) {
			setRootViewId(R.layout.quickaction_popup_vertical);
		} else {
			setRootViewId(R.layout.quickaction_popup_warning);
		}
		
		animationStyle = NO_ANIM;
		childPosition = 0;
	}
	
	/**
	 * Get ActionItem at an index.
	 * 
	 * @param index Index of ActionItem (position from callback)
	 * 
	 * @return ActionItem at the position.
	 */
	public ActionItem getActionItem(int index) {
		return actionItems.get(index);
	}
	
	/**
	 * Get all ActionItems.
	 * 
	 * @return All ActionItems.
	 */
	public List<ActionItem> getAllActionItems() {
		return actionItems;
	}
	
	/**
	 * Here we need to prepare fake "radio buttons." In fact, ActionItem is not an
	 * original radio button so we need to change its icon manually.
	 * @param selected ActionItem's title which will be selected.
	 * @param needResetAll If we need to reset icon to "off" for all ActionItems.
	 * @author Martin Misiarz
	 * @author dev.misiarz@gmail.com
	 */
	public void changeActionItemImage(String selected, boolean needResetAll) {
		ArrayList<View> actionItemsViews = viewGroup.getTouchables();
		
		if(needResetAll) {
			for(View view : actionItemsViews) {
				ImageView imageView = (ImageView)view.findViewById(R.id.actionItem_icon);
				imageView.setImageResource(R.drawable.btn_radio_off);
			}
		}
		
		for(View view : actionItemsViews) {
			TextView textView = (TextView)view.findViewById(R.id.actionItem_title);
			if(textView.getText().toString().lastIndexOf(selected)!=-1) {
				ImageView imageView = (ImageView)view.findViewById(R.id.actionItem_icon);
				imageView.setImageResource(R.drawable.btn_radio_on);
				break;
			}
		}
	}
	
	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id.
	 */
	public void setRootViewId(int id) {
		rootView = layoutInflater.inflate(id, null);
		viewGroup = (ViewGroup)rootView.findViewById(R.id.actionItems);
		
		imgArrowDown = (ImageView)rootView.findViewById(R.id.popup_arrow_down);
		imgArrowUp = (ImageView)rootView.findViewById(R.id.popup_arrow_up);
		
		scrollView= (ScrollView)rootView.findViewById(R.id.scroller);
		
		// This was previously defined on show() method, moved here to prevent
		// force close that occured when tapping fastly on a view to show
		// quickaction dialog.
		// Thanx to zammbi (github.com/zammbi)
		rootView.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		setContentView(rootView);
	}
	
	/**
	 * Set animation style.
	 * 
	 * @param animationStyle Animation style, default is set to ANIM_AUTO.
	 */
	public void setAnimationStyle(int animationStyle) {
		this.animationStyle = animationStyle;
	}
	
	/**
	 * Set listener for ActionItem clicked.
	 * 
	 * @param listener Listener.
	 */
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		onActionItemClickListener = listener;
	}
	
	/**
	 * Add ActionItem.
	 * 
	 * @param action {@link ActionItem}
	 */
	public void addActionItem(ActionItem action) {
		actionItems.add(action);
		
		String title = action.getTitle();
		Drawable icon = action.getIcon();
		
		View container;
		
		if(orientation==HORIZONTAL) {
			container = layoutInflater.inflate(R.layout.actionitem_horizontal, null);
		} else if(orientation==VERTICAL) {
			container = layoutInflater.inflate(R.layout.actionitem_vertical, null);
		} else {
			container = layoutInflater.inflate(R.layout.actionitem_warning, null);
		}
		
		ImageView img = (ImageView)container.findViewById(R.id.actionItem_icon);
		TextView text = (TextView)container.findViewById(R.id.actionItem_title);
		
		if(icon!=null) {
			img.setImageDrawable(icon);
		} else {
			img.setVisibility(View.GONE);
		}
		
		if(title!=null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}
		
		final int pos = childPosition;
		final int actionId = action.getActionId();
		
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onActionItemClickListener!=null) {
					onActionItemClickListener.onItemClick(
							QuickAction.this, pos, actionId);
					dismiss();
				}
				
				if(!getActionItem(pos).isSticky()) {
					didAction = true;
					dismiss();
				}
			}
		});
		
		container.setFocusable(true);
		container.setClickable(true);
		
		if(orientation==HORIZONTAL && childPosition!=0) {
			View separator = layoutInflater.inflate(R.layout.horizontal_separator, null);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			
			separator.setLayoutParams(params);
			separator.setPadding(5, 0, 5, 0);
			
			viewGroup.addView(separator, insertPosition);
			
			insertPosition++;
		}
		
		viewGroup.addView(container, insertPosition);
		
		childPosition++;
		insertPosition++;
	}
	
	/**
	 * Show quickaction popup. Popup is automatically positioned, on top
	 * or bottom of anchor view.
	 * @param anchor Anchor view.
	 * @param needCorrectPosition There is a small bug, when trying to show popup under
	 * the dialog - it always shows at wrong location, so we need to workaround for it.
	 */
	public void show(View anchor, boolean needCorrectPosition) {
		currentView = anchor;
		preShow();
		
		int xPos, yPos, arrowPos;
		
		didAction = false;
		
		int[] location = new int[2];
		
		anchor.getLocationOnScreen(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());
		
		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		//LayoutParams.WRAP_CONTENT));
		
		rootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		int rootHeight = rootView.getMeasuredHeight();
		
		if(rootWidth==0) {
			rootWidth = rootView.getMeasuredWidth();
		}
		
		int screenWidth = Common.getDisplayWidth(context);
		int screenHeight = Common.getDisplayHeight(context);
		
		//automatically get X coord of popup (top left)
		if((anchorRect.left + rootWidth)>screenWidth) {
			xPos = anchorRect.left - (rootWidth-anchor.getWidth());
			xPos = (xPos<0) ? 0 : xPos;
			
			arrowPos = anchorRect.centerX() - xPos;
		} else {
			if(anchor.getWidth()>rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}
			
			arrowPos = anchorRect.centerX() - xPos;
		}
		
		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;
		
		boolean onTop = (dyTop>dyBottom) ? true : false;
		
		if(onTop) {
			if(rootHeight>dyTop) {
				yPos = 15;
				LayoutParams l = scrollView.getLayoutParams();
				l.height = dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;
			
			if(rootHeight > dyBottom) {
				LayoutParams l = scrollView.getLayoutParams();
				l.height = dyBottom;
			}
		}
		
		showArrow(((onTop) ? R.id.popup_arrow_down : R.id.popup_arrow_up), arrowPos);
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		
		
		if(needCorrectPosition) {
			int freeSpace = (screenWidth - anchor.getWidth())/2;
			
			yPos = yPos - (int)(freeSpace*1.47);
			
			popupWindow.showAtLocation(anchor, Gravity.TOP, 0, yPos);
		} else popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		
	}
	
	/**
	 * Set animation style.
	 * 
	 * @param screenWidth screen width.
	 * @param requestedX distance from left edge.
	 * @param onTop flag to indicate where the popup should be displayed. Set
	 * 			TRUE if displayed on top of anchor view and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		//int arrowPos = requestedX - imgArrowUp.getMeasuredWidth()/2;
		
		switch(animationStyle) {
			case ANIM_GROW_FROM_CENTER:
				popupWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center :
					R.style.Animations_PopDownMenu_Center);
				break;
			default :
				break;
		}
	}
	
	/**
	 * Show arrow.
	 * 
	 * @param whichArrow arrow type resource id.
	 * @param requestedX distance from left screen.
	 */
	private void showArrow(int whichArrow, int requestedX) {
		final View showArrow = (whichArrow==R.id.popup_arrow_up) ?
				imgArrowUp : imgArrowDown;
		final View hideArrow = (whichArrow==R.id.popup_arrow_up) ?
				imgArrowDown : imgArrowUp;
		
		final int arrowWidth = imgArrowUp.getMeasuredWidth();
		
		ViewGroup.MarginLayoutParams param =
				(ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
		
		param.leftMargin = requestedX - arrowWidth / 2;
		
		showArrow.setVisibility(View.VISIBLE);
		hideArrow.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Set listener for window dismissed. This listener will only be fired
	 * if the quicakction dialog is dismissed by clicking outside the dialog
	 * or clicking on sticky item.
	 */
	public void setOnDismissListener(QuickAction.OnDismissListener listener) {
		setOnDismissListener(this);
		
		onDismissListener = listener;
	}
	
	@Override
	public void onDismiss() {
		if(!didAction && onDismissListener!=null) {
			onDismissListener.onDismiss();
		}
	}
	
	/**
	 * Listener for item click.
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(QuickAction source, int pos, int actionId);
	}
	
	/**
	 * Listener for window dismiss.
	 */
	public interface OnDismissListener {
		public abstract void onDismiss();
	}
	
	/**
	 * Return current View.
	 * @return Current View.
	 */
	public View getCurrentView() {
		return currentView;
	}
}