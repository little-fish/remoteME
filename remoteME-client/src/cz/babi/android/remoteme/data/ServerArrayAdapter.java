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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.Server;

/**
 * This adapter is used in scan mode.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ServerArrayAdapter extends ArrayAdapter<Server> {
	
	private final Context context;
	private final int rowLayout;
	private final ArrayList<Server> foundedServers;
	private final ArrayList<Server> serversAlreadyInDb;
	
	public ServerArrayAdapter(Context context, int rowLayout,
			ArrayList<Server> foundedServers, ArrayList<Server> serversAlreadyInDb) {
		super(context, rowLayout, foundedServers);
		
		this.context = context;
		this.rowLayout = rowLayout;
		this.foundedServers = foundedServers;
		this.serversAlreadyInDb = serversAlreadyInDb;
	}
	
	@Override
	public View getView(int position, View row, ViewGroup parent) {
		
		if(row==null) {
			LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			row = layoutInflater.inflate(rowLayout, parent, false);
		}
		
		Server server = foundedServers.get(position);
		
		if(server!=null) {
			String osName = server.getOsName();
			String serverName = server.getServerName();
			String ipAddress = server.getIpAddress();
			String port = String.valueOf(server.getPort());
			
			ImageView osTypeImageView = (ImageView)row.findViewById(R.id.scan_serverrow_icon_os_type);
			if(osName.toLowerCase().indexOf("mac")!=-1)
				osTypeImageView.setImageResource(R.drawable.os_mac);
			else if(osName.toLowerCase().indexOf("win")!=-1)
				osTypeImageView.setImageResource(R.drawable.os_win);
			else osTypeImageView.setImageResource(R.drawable.os_tux);
			
			TextView serverNameTextView = (TextView)row.findViewById(
					R.id.scan_serverrow_text_server_name);
			if(serverName.compareTo("")==0) serverNameTextView.setText(
					R.string.server_details_empty_server_name_text);
			else serverNameTextView.setText(serverName);
			
			TextView serverAddressTextView = (TextView)row.findViewById(
					R.id.scan_serverrow_text_server_address);
			serverAddressTextView.setText(ipAddress + ":" + port);
			
			boolean alreadyExist = false;
			ImageView alreadyInDbImageView = (ImageView)row.findViewById(
					R.id.scan_serverrow_icon_exists);
			for(Server s : serversAlreadyInDb) {
				if(Common.getProperIpAddress(s.getIpAddress()).equals(ipAddress)) {
					alreadyExist = true;
					break;
				}
			}
			
			if(!alreadyExist) alreadyInDbImageView.setVisibility(View.GONE);
		}
		
		return row;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	
	@Override
	public boolean isEnabled(int position) {
		Server server = getItem(position);
		
		for(Server s : serversAlreadyInDb) {
			if(Common.getProperIpAddress(s.getIpAddress()).equals(server.getIpAddress())) {
				return false;
			}
		}
		
		return true;
	}
}
