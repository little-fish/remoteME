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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.ActionItem;
import cz.babi.android.remoteme.entity.Server;
import cz.babi.android.remoteme.service.ConnectionService;
import cz.babi.android.remoteme.ui.ActivityDialogNoConnection;
import cz.babi.android.remoteme.ui.ActivityDialogServerConnect;
import cz.babi.android.remoteme.ui.ActivityDialogServerDelete;
import cz.babi.android.remoteme.ui.ActivityDialogServerDetails;
import cz.babi.android.remoteme.ui.QuickAction;

/**
 * Class that extends SimpleCursrAdapter for our own definition of view.
 * 
 * @see android.widget.SimpleCursorAdapter
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 * @created 15.8.2012, 0:13:01
 */
public class DatabaseCursorAdapter extends SimpleCursorAdapter {
	
	private final Context context;
	private Cursor cursor;
	private final int layout;
	
	private final int idIndex;
	private final int osNameIndex;
	private final int passwordIndex;
	private final int serverNameIndex;
	private final int ipAddressIndex;
	private final int portIndex;
	
	private final QuickAction quickActionServer;
	public static final int ID_SERVER_CONNECT = 1;
	public static final int ID_SERVER_EDIT = 2;
	public static final int ID_SERVER_DELETE = 3;
	
	/* After long click on server raw (it's View) we need to keep this raw highlihted. */
	private View pressedView;
	
	/**
	 * Contstructor.
	 * @see android.widget.SimpleCursorAdapter#SimpleCursorAdapter(Context, int, Cursor, String[], int[])
	 */
	@SuppressWarnings("deprecation")
	public DatabaseCursorAdapter(final Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		
		/* There is nothing we can do with that. For now. */
		super(context, layout, cursor, from, to);
		
		this.context = context;
		this.cursor = cursor;
		this.layout = layout;
		
		this.idIndex = this.cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_ID);
		this.osNameIndex = this.cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_OS_NAME);
		this.passwordIndex = this.cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_PASSWORD);
		this.serverNameIndex = this.cursor.getColumnIndex(
				MySQLiteOpenHelper.COLUMN_SERVER_NAME);
		this.ipAddressIndex = this.cursor.getColumnIndex(
				MySQLiteOpenHelper.COLUMN_IP_ADDRESS);
		this.portIndex = this.cursor.getColumnIndex(MySQLiteOpenHelper.COLUMN_PORT);
		
		/* Prepare QuickAction for long click on server. */
		ActionItem serverConnect = new ActionItem(ID_SERVER_CONNECT, context.getResources().
				getString(R.string.quickaction_adapter_serverConnect_text),
				context.getResources().getDrawable(R.drawable.server_connect));
		ActionItem serverEdit = new ActionItem(ID_SERVER_EDIT, context.getResources().
				getString(R.string.quickaction_adapter_serverEdit_text),
				context.getResources().getDrawable(R.drawable.server_edit));
		ActionItem serverDelete = new ActionItem(ID_SERVER_DELETE, context.getResources().
				getString(R.string.quickaction_adapter_serverDelete_text),
				context.getResources().getDrawable(R.drawable.server_delete));
		
		quickActionServer = new QuickAction(context, QuickAction.VERTICAL);
		quickActionServer.setAnimationStyle(QuickAction.ANIM_GROW_FROM_CENTER);
		quickActionServer.addActionItem(serverConnect);
		quickActionServer.addActionItem(serverEdit);
		quickActionServer.addActionItem(serverDelete);
		
		quickActionServer.setOnActionItemClickListener(
				new QuickAction.OnActionItemClickListener() {
					@Override
					public void onItemClick(QuickAction source, int pos, int actionId) {
						View selectedView = quickActionServer.getCurrentView();
						int selectedServerId = (Integer)selectedView.getTag(R.ids.server_id);
						
						switch (actionId) {
							case ID_SERVER_CONNECT:
								Server serverToConnect = MySQLiteOpenHelper.
								getInstance(DatabaseCursorAdapter.this.context).
								getServer(selectedServerId);
								
								/* Need to check if we are connected to some network. */
								if(Common.checkConnection(DatabaseCursorAdapter.this.context)) {
									Intent mIntentConnection = new Intent(
											DatabaseCursorAdapter.this.context,
											ActivityDialogServerConnect.class);
									
									ConnectionService.server = serverToConnect;
									
									DatabaseCursorAdapter.this.context.startActivity(mIntentConnection);
								} else {
									Intent mIntentNoConnection = new Intent(
											DatabaseCursorAdapter.this.context,
											ActivityDialogNoConnection.class);
									DatabaseCursorAdapter.this.context.
									startActivity(mIntentNoConnection);
								}
								
								break;
							case ID_SERVER_EDIT:
								Server editedServer = MySQLiteOpenHelper.
								getInstance(DatabaseCursorAdapter.this.context).
								getServer(selectedServerId);
								
								Intent mIntentServerDetails = new Intent(DatabaseCursorAdapter.this.context,
										ActivityDialogServerDetails.class);
								
								mIntentServerDetails.putExtra("IS_NEW", false);
								mIntentServerDetails.putExtra("IS_NEW_FROM_SCAN", false);
								mIntentServerDetails.putExtra("SERVER", editedServer);
								
								DatabaseCursorAdapter.this.context.startActivity(mIntentServerDetails);
								
								break;
							case ID_SERVER_DELETE:
								Intent mIntentServerDelete = new Intent(DatabaseCursorAdapter.this.context,
										ActivityDialogServerDelete.class);
								mIntentServerDelete.putExtra("ID_TO_DELETE", selectedServerId);
								DatabaseCursorAdapter.this.context.startActivity(mIntentServerDelete);
								
								break;
						}
					}
				});
		
		quickActionServer.setOnDismissListener(new QuickAction.OnDismissListener() {
			@Override
			public void onDismiss() {
				pressedView.setSelected(false);
			}
		});
		
		
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		View v = view;
		
		if(v==null) {
			LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = layoutInflater.inflate(layout, parent, false);
		}
		
		cursor.moveToPosition(position);
		
		ImageView osTypeImageView = (ImageView)v.findViewById(R.id.serverrow_icon_os_type);
		String osName = this.cursor.getString(osNameIndex);
		if(osName.toLowerCase().indexOf("mac")!=-1)
			osTypeImageView.setImageResource(R.drawable.os_mac);
		else if(osName.toLowerCase().indexOf("win")!=-1)
			osTypeImageView.setImageResource(R.drawable.os_win);
		else osTypeImageView.setImageResource(R.drawable.os_tux);
		
		ImageView passwordImageView = (ImageView)v.findViewById(R.id.serverrow_icon_locked);
		String password = this.cursor.getString(passwordIndex);
		if(password.trim().length()<1) passwordImageView.setVisibility(View.INVISIBLE);
		else passwordImageView.setVisibility(View.VISIBLE);
		
		TextView serverNameTextView = (TextView)v.findViewById(
				R.id.serverrow_text_server_name);
		String serverName = this.cursor.getString(serverNameIndex);
		if(serverName.compareTo("")==0) serverNameTextView.setText(
				R.string.server_details_empty_server_name_text);
		else serverNameTextView.setText(serverName);
		
		TextView serverAddressTextView = (TextView)v.findViewById(
				R.id.serverrow_text_server_address);
		
		/* IP address is stored in format: xxx.xxx.xxx.xxx for correct sorting. */
		String ipAddress = Common.getProperIpAddress(this.cursor.getString(ipAddressIndex));
		
		String port = this.cursor.getString(portIndex);
		serverAddressTextView.setText(ipAddress + ":" + port);
		
		/* We need to set server's id to all singles views.
		 * If we will want to delete, edit or connect to server,
		 * we will know it's id. */
		int id = this.cursor.getInt(idIndex);
		v.setTag(R.ids.server_id, id);
		
		v.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				quickActionServer.show(v, false);
				
				/* This is because we want to keep selected server row. */
				pressedView = v;
				pressedView.setSelected(true);
				
				return true;
			}
		});
		
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int selectedServerId = (Integer)v.getTag(R.ids.server_id);
				
				Server serverToConnect = MySQLiteOpenHelper.
						getInstance(DatabaseCursorAdapter.this.context).
						getServer(selectedServerId);
				
				/* Need to check if we are connected to some network. */
				if(Common.checkConnection(DatabaseCursorAdapter.this.context)) {
					Intent mIntentConnection = new Intent(
							DatabaseCursorAdapter.this.context,
							ActivityDialogServerConnect.class);
					
					ConnectionService.server = serverToConnect;
					
					DatabaseCursorAdapter.this.context.startActivity(mIntentConnection);
				} else {
					Intent mIntentNoConnection = new Intent(
							DatabaseCursorAdapter.this.context,
							ActivityDialogNoConnection.class);
					DatabaseCursorAdapter.this.context.
					startActivity(mIntentNoConnection);
				}
			}
		});
		
		return v;
	}
	
	/**
	 * @see android.widget.SimpleCursorAdapter#changeCursor(Cursor)
	 */
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		this.cursor = cursor;
	}
}
