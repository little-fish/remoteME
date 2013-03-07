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

package cz.babi.desktop.remoteme.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.Settings;
import cz.babi.desktop.remoteme.crypto.AES128;

/**
 * Class for common use.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Common {
	
	private static final Logger LOGGER = Logger.getLogger(Common.class
			.getSimpleName());
	
	public static final String CHARSET = "UTF-8";
	public static final String ALGORITHM_AES = "AES";
	public static final String ALGORITHM_SHA1 = "SHA-1";
	
	/**
	 * Variable define secretID for encrypting and decrypting messages. <b>MAKE
	 * SHURE THAT THIS SECRET ID IS THE SAME LIKE IN CLIENT APPLICATION</b>.
	 */
	public static final String SECRET_ID = "remoteME is really cool app";
	
	/**
	 * Variable define default password for encrypting and decrypting messages
	 * in scan mode. <b>MAKE SHURE THAT THIS SECRET ID IS THE SAME LIKE IN
	 * CLIENT APPLICATION</b>.
	 */
	public static final String DEFAULT_PASSWORD = "default-password";
	
	/** Will be used for UDP scan mode and for first TCP comunication. */
	public static final AES128 AES128_DEFAULT = new AES128(Common.SECRET_ID,
			Common.DEFAULT_PASSWORD);
	
	/** Special commands. */
	public static final String COMMAND_SHUTDOWN = "shutdown";
	public static final String COMMAND_RESTART = "restart";
	public static final String COMMAND_LOGOFF = "logoff";
	
	/** For programatically set logging on/off */
	public static int LOGLEVEL = Settings.getInstance().getLogLevel();
	public static boolean ERROR = LOGLEVEL > 2;
	public static boolean WARN = LOGLEVEL > 1;
	public static boolean DEBUG = LOGLEVEL > 0;
	
	public static ResourceBundle L10N = ResourceBundle
			.getBundle("cz.babi.desktop.remoteme.res.l10n");
	
	/** Default port. Can not be changed. */
	public static final int DEFAULT_PORT = 4449;
	
	/**
	 * Obtain proper IP address.
	 * 
	 * @return IP address. May return null.
	 */
	public static String getLocalIpAddress() {
		if(Common.ERROR)
			LOGGER.error("[getLocalIpAddress]");
		
		/* Obtain list of interfaces. */
		Enumeration<NetworkInterface> networkInterfaces = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch(SocketException e) {
			if(Common.ERROR)
				LOGGER.error("[getLocalIpAddress][Can not obtain "
						+ "network interfaces.]");
			
			return null;
		}
		
		while(networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			
			Enumeration<InetAddress> inetAddresses = networkInterface
					.getInetAddresses();
			
			while(inetAddresses.hasMoreElements()) {
				InetAddress inetAddress = inetAddresses.nextElement();
				
				/* This is our IP address. */
				if(inetAddress.isSiteLocalAddress()) {
					return inetAddress.getHostAddress();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Get Mac address.
	 * 
	 * @return Mac address. May return null.
	 */
	public static String getMapAddress() {
		if(Common.ERROR)
			LOGGER.error("[getMapAddress]");
		
		/* Obtain list of interfaces. */
		Enumeration<NetworkInterface> networkInterfaces = null;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch(SocketException e) {
			if(Common.ERROR)
				LOGGER.error("[getLocalIpAddress][Can not obtain "
						+ "network interfaces.]");
			
			return null;
		}
		
		while(networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			
			Enumeration<InetAddress> inetAddresses = networkInterface
					.getInetAddresses();
			
			while(inetAddresses.hasMoreElements()) {
				InetAddress inetAddress = inetAddresses.nextElement();
				
				/* This is our IP address. */
				if(inetAddress.isSiteLocalAddress()) {
					byte[] mac = null;
					try {
						mac = networkInterface.getHardwareAddress();
					} catch(SocketException e) {
						if(Common.ERROR)
							LOGGER.error("[getLocalIpAddress][Can not obtain "
									+ "hardware address.]");
						
						return null;
					}
					
					if(mac!=null) {
						StringBuilder sb = new StringBuilder();
						for(int i = 0; i < mac.length; i++) {
							sb.append(String.format("%02X%s", mac[i],
									(i < mac.length - 1) ? "-" : ""));
						}
						
						return sb.toString();
					} else
						return null;
				}
			}
		}
		
		return null;
	}
}
