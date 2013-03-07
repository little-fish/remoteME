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

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import cz.babi.android.remoteme.ActivityMain;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.ActionItem;

/**
 * Custom actionbar.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Actionbar extends RelativeLayout {
	
	private static final String TAG_CLASS_NAME = Actionbar.class.getSimpleName();
	
	public static final String ORDER_BY = "order-by";
	
	/** Shared preferences for this application. */
	private final SharedPreferences preferences;
	private final SharedPreferences.Editor editor;
	
	private static final int ID_ADD_NEW_SERVER_MANUAL = 0;
	private static final int ID_ADD_NEW_SERVER_SCAN = 1;
	
	public Actionbar(Context context) {
		super(context);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
	}
	
	public Actionbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
	}
	
	public Actionbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		editor = preferences.edit();
	}
	
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		
		setupButtons();
	}
	
	private void setupButtons(){
		ImageButton addServerButton = (ImageButton)findViewById(
				R.id.actionbar_add_new_server);
		ImageButton orderServersButton = (ImageButton)findViewById(
				R.id.actionbar_order_servers);
		
		ActionItem addNewServerManual = new ActionItem(ID_ADD_NEW_SERVER_MANUAL,
				getResources().getString(R.string.quickaction_view_addNewServerManual_text));
		ActionItem addNewServerScan = new ActionItem(ID_ADD_NEW_SERVER_SCAN,
				getResources().getString(R.string.quickaction_view_addNewServerScan_text));
		
		ActionItem orderByName = new ActionItem(
				getResources().getInteger(R.integer.pref_value_order_by_name),
				getResources().getString(R.string.quickaction_view_orderServersName_text),
				getResources().getDrawable(R.drawable.btn_radio_off));
		ActionItem orderByLastConnected = new ActionItem(
				getResources().getInteger(R.integer.pref_value_order_by_last_connected),
				getResources().getString(R.string.quickaction_view_orderServersLastConnected_text),
				getResources().getDrawable(R.drawable.btn_radio_off));
		ActionItem orderByIPAddress = new ActionItem(
				getResources().getInteger(R.integer.pref_value_order_by_ip_address),
				getResources().getString(R.string.quickaction_view_orderServersIPAddress_text),
				getResources().getDrawable(R.drawable.btn_radio_off));
		ActionItem orderByOSType = new ActionItem(
				getResources().getInteger(R.integer.pref_value_order_by_os_type),
				getResources().getString(R.string.quickaction_view_orderServersOSType_text),
				getResources().getDrawable(R.drawable.btn_radio_off));
		
		final QuickAction quickActionAddNewServer = new QuickAction(getContext(),
				QuickAction.VERTICAL);
		quickActionAddNewServer.setAnimationStyle(QuickAction.ANIM_GROW_FROM_CENTER);
		quickActionAddNewServer.addActionItem(addNewServerManual);
		quickActionAddNewServer.addActionItem(addNewServerScan);
		
		quickActionAddNewServer.setOnActionItemClickListener(
				new QuickAction.OnActionItemClickListener() {
					@Override
					public void onItemClick(QuickAction source, int pos, int actionId) {
						switch (actionId) {
							case ID_ADD_NEW_SERVER_MANUAL:
								if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onClick][Add new server " +
										"manually.]");
								
								Intent mIntentManual = new Intent(getContext(),
										ActivityDialogServerDetails.class);
								mIntentManual.putExtra("IS_NEW", true);
								getContext().startActivity(mIntentManual);
								
								break;
							case ID_ADD_NEW_SERVER_SCAN:
								if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onClick][Scan mode.]");
								
								/* Need to check if we are connected to some network. */
								if(Common.checkConnection(getContext())) {
									Intent mIntentScan = new Intent(getContext(),
											ActivityDialogServerSearch.class);
									getContext().startActivity(mIntentScan);
								} else {
									Intent mIntentNoConnection = new Intent(getContext(),
											ActivityDialogNoConnection.class);
									getContext().startActivity(mIntentNoConnection);
								}
								
								break;
							default:
								break;
						}
					}
				});
		
		quickActionAddNewServer.setOnDismissListener(new QuickAction.OnDismissListener() {
			@Override
			public void onDismiss() {
				ImageButton addServerButton = (ImageButton)findViewById(
						R.id.actionbar_add_new_server);
				addServerButton.setImageResource(R.drawable.icon_add_server_normal);
			}
		});
		
		final QuickAction quickActionOrderServers = new QuickAction(getContext(),
				QuickAction.VERTICAL);
		quickActionOrderServers.setAnimationStyle(QuickAction.ANIM_GROW_FROM_CENTER);
		quickActionOrderServers.addActionItem(orderByName);
		quickActionOrderServers.addActionItem(orderByLastConnected);
		quickActionOrderServers.addActionItem(orderByIPAddress);
		quickActionOrderServers.addActionItem(orderByOSType);
		
		int orderBy = preferences.getInt(getResources().getString(R.string.pref_name_server_order),
				getResources().getInteger(R.integer.pref_value_order_by_name));
		prepareRadioButtons(quickActionOrderServers, orderBy, false);
		
		quickActionOrderServers.setOnActionItemClickListener(
				new QuickAction.OnActionItemClickListener() {
					@Override
					public void onItemClick(QuickAction source, int pos, int actionId) {
						ActionItem actionItem = source.getActionItem(pos);
						
						if(actionId==getResources().getInteger(R.integer.pref_value_order_by_name)) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onClick][Order by name.]");
							ActivityMain.BROADCAST_INTENT_REFRESH_SERVERS.putExtra(ORDER_BY,
									getResources().getInteger(R.integer.pref_value_order_by_name));
						} else if(actionId==getResources().getInteger(R.integer.pref_value_order_by_last_connected)) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onClick][Order by last " +
									"connect.]");
							ActivityMain.BROADCAST_INTENT_REFRESH_SERVERS.putExtra(ORDER_BY,
									getResources().getInteger(R.integer.pref_value_order_by_last_connected));
						} else if(actionId==getResources().getInteger(R.integer.pref_value_order_by_ip_address)) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onClick][Order by IP " +
									"address.]");
							ActivityMain.BROADCAST_INTENT_REFRESH_SERVERS.putExtra(ORDER_BY,
									getResources().getInteger(R.integer.pref_value_order_by_ip_address));
						} else if(actionId==getResources().getInteger(R.integer.pref_value_order_by_os_type)) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onClick][Order OS type.]");
							ActivityMain.BROADCAST_INTENT_REFRESH_SERVERS.putExtra(ORDER_BY,
									getResources().getInteger(R.integer.pref_value_order_by_os_type));
						} else {
							ActivityMain.BROADCAST_INTENT_REFRESH_SERVERS.putExtra(ORDER_BY,
									getResources().getInteger(R.integer.pref_value_order_by_name));
						}
						
						source.changeActionItemImage(actionItem.getTitle(), true);
						
						editor.putInt(getResources().getString(R.string.pref_name_server_order),
								actionId);
						editor.commit();
						
						getContext().sendBroadcast(ActivityMain.BROADCAST_INTENT_REFRESH_SERVERS);
					}
				});
		
		quickActionOrderServers.setOnDismissListener(new QuickAction.OnDismissListener() {
			@Override
			public void onDismiss() {
				ImageButton orderServersButton = (ImageButton)findViewById(
						R.id.actionbar_order_servers);
				orderServersButton.setImageResource(R.drawable.icon_order_servers_normal);
			}
		});
		
		addServerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quickActionAddNewServer.show(v, false);
				
				ImageButton addServerButton = (ImageButton)findViewById(
						R.id.actionbar_add_new_server);
				addServerButton.setImageResource(R.drawable.icon_add_server_pressed);
			}
		});
		
		orderServersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quickActionOrderServers.show(v, false);
				
				ImageButton orderServersButton = (ImageButton)findViewById(
						R.id.actionbar_order_servers);
				orderServersButton.setImageResource(R.drawable.icon_order_servers_pressed);
			}
		});
	}
	
	/**
	 * Here we need to prepare "radio buttons." In fact, ActionItem is not radio button
	 * so we need to change its icon manually.
	 * @param quickAction QuickAction which containt ActionItems.
	 * @param selected ActionItem which will be selected.
	 * @param needResetAll If we need to change icon to "off" for all ActionItems.
	 */
	private void prepareRadioButtons(QuickAction quickAction, int selected,
			boolean needResetAll) {
		
		List<ActionItem> actionItems = quickAction.getAllActionItems();
		
		for(ActionItem actionItem : actionItems) {
			if(actionItem.getActionId()==selected) {
				quickAction.changeActionItemImage(actionItem.getTitle(), needResetAll);
				break;
			}
		}
	}
}
