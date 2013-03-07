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

package cz.babi.android.remoteme.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.crypto.AES128;
import cz.babi.android.remoteme.entity.Message;
import cz.babi.android.remoteme.entity.Message.SimpleMessage;
import cz.babi.android.remoteme.entity.Server;
import cz.babi.android.remoteme.ui.ActivityDialogServerSearch.SearchingFragment;

/**
 * This task is used for Scan Mode.
 * We are using UDP protocol here for sending broadcast request datagrams and
 * receive responses.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class UDPScanModeTask extends AsyncTask<Void, Void, ArrayList<Server>> {
	
	private SearchingFragment parentFragment = null;
	
	private MulticastSocket multicastSocket = null;
	
	private boolean stoppedByUser = false;
	
	private final SharedPreferences preferences;
	
	private static final String TAG_CLASS_NAME = UDPScanModeTask.class.getSimpleName();
	
	private static final AES128 AES128_DEFAULT = Common.AES128_DEFAULT;
	
	/** variable define port for our request and responses */
	private int port;
	/** variable define multicast address which datagrams will be sent to */
	private static final String MULTICAST_ADDR = "230.0.0.1";
	
	/**
	 * Constructor.
	 * @param parentFragmentActivity Parent activity. Needs for access to its methods.
	 */
	public UDPScanModeTask(SearchingFragment parentFragmentActivity) {
		this.parentFragment = parentFragmentActivity;
		
		preferences = PreferenceManager.getDefaultSharedPreferences(
				parentFragment.getActivity());
		
		port = Integer.valueOf(preferences.getString(
				this.parentFragment.getResources().getString(R.string.pref_name_udp_scan_mode_port),
				String.valueOf(4449)));
	}
	
	@Override
	protected ArrayList<Server> doInBackground(Void... params) {
		return findServers();
	}
	
	@Override
	protected void onPostExecute(ArrayList<Server> result) {
		if(parentFragment!=null) {
			parentFragment.setTaskStatus(SearchingFragment.TASK_ENDS);
			parentFragment.setFoundedServers(result);
			if(!stoppedByUser) parentFragment.updateUI();
		}
	}
	
	@Override
	protected void onPreExecute() {
		if(parentFragment!=null) {
			parentFragment.setTaskStatus(SearchingFragment.TASK_RUNNING);
			parentFragment.updateUI();
		}
	}
	
	/**
	 * Start Scan Mode.
	 * @return Founded servers.
	 */
	private ArrayList<Server> findServers() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Start scaning...]");
		
		try {
			multicastSocket = new MulticastSocket(port);
		} catch(IOException ioe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Can not create a MulticastSocket on port " + port + ".]", ioe);
		}
		
		InetAddress multicastAddress = null;
		
		try {
			multicastAddress = InetAddress.getByName(MULTICAST_ADDR);
		} catch (UnknownHostException uhe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Can not get InetAddress from multicast address "
					+ MULTICAST_ADDR + ".]", uhe);
		}
		
		/* Need to encrypt our request */
		String requestMessage = AES128_DEFAULT.encryptText(Message.SCAN_MODE_REQUEST.toString());
		
		byte[] request = null;
		try {
			request = requestMessage.getBytes(Common.CHARSET_UTF8);
		} catch(UnsupportedEncodingException uee) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][An error occurred while trying to encode text.]", uee);
		}
		
		DatagramPacket requestDatagramPacket = new DatagramPacket(
				request, request.length, multicastAddress, port);
		
		/* Here we send broadcast datagram */
		try {
			multicastSocket.send(requestDatagramPacket);
		} catch (IOException ioe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Can not send a request datagram to " +
					requestDatagramPacket.getAddress().getHostAddress() + ":" +
					requestDatagramPacket.getPort() + "]", ioe);
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Request sent!]");
		
		/* Need to prepare for responses */
		if(multicastAddress.isMulticastAddress()) {
			try {
				multicastSocket.joinGroup(multicastAddress);
			} catch (IOException ioe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Socket can not join into multicast group.]", ioe);
			}
		} else {
			try {
				multicastSocket.setBroadcast(true);
			} catch(SocketException se) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Can not turned broadcast on.[findServers]", se);
			}
		}
		
		ArrayList<Server> availableServers = new ArrayList<Server>();
		
		/* We do not want to receive our sent datagrams. */
		try {
			multicastSocket.setLoopbackMode(true);
		} catch (SocketException se) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Can not set LoopBack mode to socket.]", se);
		}
		
		/* We need to set up timeout for scanning. */
		try {
			multicastSocket.setSoTimeout(
					preferences.getInt(
							this.parentFragment.getResources().getString(
									R.string.pref_name_socket_timeout),
									Common.DEFAULT_SOCKET_TIMEOUT));
		} catch (SocketException se) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Can not set timeout to MulticastSocket.]", se);
		}
		
		/* There need to be a loop for waiting for an incoming responses. */
		while(true) {
			/* We do not know what size will be a encrypted response */
			byte[] response = new byte[128];
			DatagramPacket responseDatagramPacket = new DatagramPacket(
					response, response.length);
			
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Waiting for incoming response...]");
			
			/* Here we are waiting for incoming response */
			try {
				multicastSocket.receive(responseDatagramPacket);
			} catch (IOException ioe) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][An error occurred during receiving an udp datagram. " +
						"Probably timeout was reached or user canceled the task.]");
				break;
			}
			
			if(responseDatagramPacket.getAddress()!=null) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Received response from:" +
						responseDatagramPacket.getAddress().getHostAddress() + ":" +
						responseDatagramPacket.getPort() + "]");
				String responseMessage = new String(responseDatagramPacket.getData());
				
				/* Need to trim response message */
				responseMessage = new String(responseMessage.trim());
				
				/* Here we need to decrypt incoming response */
				String decryptedMessage = AES128_DEFAULT.decryptText(responseMessage);
				
				SimpleMessage simpleMessage = parseIncommingMessage(decryptedMessage);
				
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Response from server: " +
						simpleMessage.toString() + "]");
				
				if(simpleMessage.getId()==Message.SCAN_MODE_RESPONSE.getId()) {
					
					String hostName = simpleMessage.getAddInfo().split(SimpleMessage.SEPARATOR)[1];
					String hostOsName = simpleMessage.getAddInfo().split(SimpleMessage.SEPARATOR)[2];
					String macAddress = simpleMessage.getAddInfo().split(SimpleMessage.SEPARATOR)[3];
					Log.d(TAG_CLASS_NAME, "full: " + simpleMessage.getAddInfo());
					Log.d(TAG_CLASS_NAME, "host name: " + hostName);
					Log.d(TAG_CLASS_NAME, "host os name: " + hostOsName);
					Log.d(TAG_CLASS_NAME, "mac addr: " + macAddress);
					
					String hostAddress = responseDatagramPacket.getAddress().getHostAddress();
					long hostPort = responseDatagramPacket.getPort();
					
					availableServers.add(
							new Server(hostAddress, macAddress, hostPort, hostName, hostOsName));
					
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Hoooaaaa, right response catched!!!]");
				} else if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Wrong response :(]");
			} else if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Response datagram do not have address.]");
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][Founded servers:]");
		for(Server instance : availableServers) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][# " + instance.getServerName() + " > running on "
					+ instance.getOsName() + " > " + instance.getIpAddress() + ":"
					+ instance.getPort() + " > with mac: " + instance.getMacAddress() + "]");
		}
		
		try {
			if(multicastSocket.isConnected()) multicastSocket.leaveGroup(multicastAddress);
		} catch (IOException ioe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[findServers][Socket can not leave multicast group.]", ioe);
		}
		
		if(multicastSocket.isConnected()) multicastSocket.close();
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[findServers][End of task. Bye bey.]");
		
		return availableServers;
	}
	
	/**
	 * Parsing incomming message from raw String to SimpleMessage..
	 * @param incomingMessage Raw String.
	 * @return Parsed SimpleMessage.
	 */
	private SimpleMessage parseIncommingMessage(String incomingMessage) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[parseIncommingMessage]");
		
		int messageId = Integer.valueOf(
				incomingMessage.substring(0, incomingMessage.indexOf(
						SimpleMessage.SEPARATOR)));
		
		String addInfo = incomingMessage.substring(
				incomingMessage.indexOf(SimpleMessage.SEPARATOR),
				incomingMessage.length());
		
		return new SimpleMessage(messageId, addInfo);
	}
	
	/**
	 * If user cancel task we need to close socket.
	 */
	public void stopTask() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[stopTask]");
		multicastSocket.close();
		stoppedByUser = true;
	}
}
