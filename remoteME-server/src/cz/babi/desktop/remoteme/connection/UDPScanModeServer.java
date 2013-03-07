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

package cz.babi.desktop.remoteme.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.crypto.AES128;
import cz.babi.desktop.remoteme.entity.Message;
import cz.babi.desktop.remoteme.entity.Message.SimpleMessage;
import cz.babi.desktop.remoteme.interfaces.DefaultServer;

/**
 * This class represent an UDP server,
 * which will be used for communication with devices in SCAN MODE.
 * Simple, the server is waiting for incoming requests and after it
 * receive right request, it will send appropriate response.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class UDPScanModeServer implements Runnable, DefaultServer {
	
	private static final Logger LOGGER = Logger.getLogger(UDPScanModeServer.class.getSimpleName());
	
	private static final AES128 AES128_DEFAULT = Common.AES128_DEFAULT;
	
	/** Variable define multicast address which datagrams will be sent to. */
	private static final String MULTICAST_ADDR = "230.0.0.1";
	/** Variable define if server can  run. */
	private boolean keepRunning = false;
	/** Variable define user port. */
	private final int PORT;
	
	private String hostName = "";
	private String hostOsName = "";
	private String macAddress = "";
	
	private MulticastSocket multicastSocket = null;
	private InetAddress multicastAddress = null;
	
	/**
	 * Constructor of UDP server.
	 * @param port Port to set.
	 */
	public UDPScanModeServer(int port) {
		if(Common.DEBUG) LOGGER.debug("[UDPScanModeServer][Public constructor.]");
		
		this.PORT = port;
	}
	
	@Override
	public void run() {
		if(Common.DEBUG) LOGGER.debug("[run][Start task.]");
		
		startServer();
		if(keepRunning) waitForConnection();
	}
	
	/**
	 * @see cz.babi.desktop.remoteme.interfaces.DefaultServer#startServer()
	 */
	@Override
	public void startServer() {
		if(Common.DEBUG) LOGGER.debug("[startServer][UDP Server is starting on port: '" + PORT + "'.]");
		
		/* Inicialization of multicast socket to user port. */
		try {
			multicastSocket = new MulticastSocket(PORT);
		} catch(IOException ioe) {
			if(Common.ERROR) LOGGER.error("[startServer][Can not create a MulticastSocket " +
					"on port " + PORT + ".]", ioe);
			stopServer();
			return;
		}
		
		/* Need to get instance of InetAddress from multicast address. */
		try {
			multicastAddress = InetAddress.getByName(MULTICAST_ADDR);
			hostOsName = System.getProperty("os.name");
			if(hostOsName.toLowerCase().contains("win"))
				hostName = InetAddress.getLocalHost().getHostName();
		} catch(UnknownHostException uhe) {
			if(Common.ERROR) LOGGER.error("[startServer][Can not get InetAddress from " +
					"multicast address " + MULTICAST_ADDR + ".]", uhe);
			stopServer();
			return;
		}
		
		
		/* If instance of InetAddress is an multicast address then
		 * we need to join multicast socket to this address. */
		if(multicastAddress.isMulticastAddress()) {
			try {
				multicastSocket.joinGroup(multicastAddress);
			} catch (IOException ioe) {
				if(Common.ERROR) LOGGER.error("[startServer][Socket can not join into " +
						"multicast group.]", ioe);
				stopServer();
				return;
			}
			/* If not, then we need to setBroadcast to true. */
		} else {
			try {
				multicastSocket.setBroadcast(true);
			} catch(SocketException se) {
				if(Common.ERROR) LOGGER.error("[startServer][Can not turned broadcast on.]", se);
				stopServer();
				keepRunning = false;
				return;
			}
		}
		
		/* We do now want to receive our sent datagrams. */
		try {
			multicastSocket.setLoopbackMode(true);
		} catch (SocketException se) {
			if(Common.ERROR) LOGGER.error("[startServer][Can not set LoopBack mode to " +
					"socket.]", se);
			stopServer();
			keepRunning = false;
			return;
		}
		
		keepRunning = true;
		
		if(Common.DEBUG) LOGGER.debug("[startServer][UDP Server is ready for incoming " +
				"requests.]");
	}
	
	/**
	 * 
	 */
	@Override
	public void waitForConnection() {
		/* There need to be a loop for waiting for an incoming requests. */
		while(true) {
			if(Common.DEBUG) LOGGER.debug("[waitForConnection][Waiting for incoming request...]");
			
			/* We do not know what size will be a encrypted request */
			byte[] request = new byte[128];
			DatagramPacket requestDatagramPacket = new DatagramPacket(
					request, request.length);
			
			/* Here we are waiting for incoming requests */
			try {
				multicastSocket.receive(requestDatagramPacket);
			} catch (IOException ioe) {
				if(Common.ERROR) LOGGER.error("[waitForConnection][An error occurred during " +
						"receiving an udp datagram.]", ioe);
				stopServer();
				return;
			}
			
			if(Common.DEBUG) LOGGER.debug("[waitForConnection][Received request from: " +
					requestDatagramPacket.getAddress().getHostAddress() + ":" +
					requestDatagramPacket.getPort() + ".]");
			
			String requestMessage = new String(requestDatagramPacket.getData());
			
			/* Need to trim request message */
			requestMessage = new String(requestMessage.trim());
			
			/* Here we need to decrypt incoming message */
			String decrypteddMessage = AES128_DEFAULT.decryptText(requestMessage);
			
			if(Common.DEBUG) LOGGER.debug("[waitForConnection][Raw request message:][" +
					requestMessage + "]");
			if(Common.DEBUG) LOGGER.debug("[waitForConnection][Decrypted request message:][" +
					decrypteddMessage + "]");
			
			/* Let's parse decrypted message. */
			SimpleMessage message = parseIncommingMessage(decrypteddMessage);
			
			/* If received datagram contains specific request,
			 * thereafter we need to send our response. */
			if(message.getId()==Message.SCAN_MODE_REQUEST.getId()) {
				if(Common.DEBUG) LOGGER.debug("[waitForConnection][Yes, catched right request :)]");
				
				macAddress = Common.getMapAddress();
				
				byte[] response = null;
				try {
					/* Need to add host name to our response. */
					SimpleMessage simpleResponse = Message.SCAN_MODE_RESPONSE;
					simpleResponse.setAddInfo(hostName + SimpleMessage.SEPARATOR + hostOsName +
							SimpleMessage.SEPARATOR + ((macAddress!=null) ? macAddress : ""));
					
					response = AES128_DEFAULT.encryptText(simpleResponse.toString()).
							getBytes(Common.CHARSET);
				} catch(UnsupportedEncodingException uee) {
					if(Common.ERROR) LOGGER.error("[waitForConnection][An error occurred " +
							"while trying to encode text.]", uee);
				}
				
				if(response!=null) {
					DatagramPacket responseDatagramPacket = new DatagramPacket(
							response, response.length, multicastAddress, PORT);
					
					try {
						multicastSocket.send(responseDatagramPacket);
					} catch (IOException ioe) {
						if(Common.ERROR) LOGGER.error("[waitForConnection][Can not send a " +
								"response datagram to " +
								requestDatagramPacket.getAddress().getHostAddress() + ":" +
								requestDatagramPacket.getPort() + ".]", ioe);
						stopServer();
						return;
					}
					
					if(Common.DEBUG) LOGGER.debug("[waitForConnection][Response sent!]");
				} else if(Common.ERROR) LOGGER.error("[waitForConnection][Response is 'null', " +
						"can not sent it.]");
				/* We can also receive an response send by another servers,
				 * so in that case we can discard received datagram
				 * and start waiting for another. */
			} else if(Common.DEBUG) LOGGER.debug("[waitForConnection][Oouu, wrong request " +
					"catched.]");
		}
	}
	
	/**
	 * Here we can set up a class variable for stop receiving incoming requests
	 * and also leave group and close socket (if so).
	 * @see cz.babi.desktop.remoteme.interfaces.DefaultServer#stopServer()
	 */
	@Override
	public void stopServer() {
		if(Common.DEBUG) LOGGER.debug("[stopServer]");
		
		if(keepRunning) keepRunning = false;
		
		if(multicastSocket!=null) {
			if(!multicastSocket.isClosed()) {
				if(multicastAddress!=null) {
					/* In the end we need to leave multicast group and close the socket. */
					try {
						multicastSocket.leaveGroup(multicastAddress);
					} catch (IOException ioe) {
						if(Common.ERROR) LOGGER.error("[stopServer][Socket can not leave " +
								"multicast group.]", ioe);
					}
				}
				
				multicastSocket.close();
			}
		}
		
		if(Common.DEBUG) LOGGER.debug("[stopServer][Server is off.]");
	}
	
	/**
	 * Parsing incomming message from raw String to SimpleMessage..
	 * @param incomingMessage Raw String.
	 * @return Parsed SimpleMessage.
	 */
	private SimpleMessage parseIncommingMessage(String incomingMessage) {
		if(Common.DEBUG) LOGGER.debug("[parseIncommingMessage][" + incomingMessage + "]");
		
		int messageId = Integer.valueOf(
				incomingMessage.substring(0, incomingMessage.indexOf(
						SimpleMessage.SEPARATOR)));
		
		String addInfo = incomingMessage.substring(
				incomingMessage.indexOf(SimpleMessage.SEPARATOR),
				incomingMessage.length());
		
		return new SimpleMessage(messageId, addInfo);
	}
}
