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

package cz.babi.desktop.remoteme;

import javax.xml.bind.annotation.XmlRootElement;

import cz.babi.desktop.remoteme.common.Common;

/**
 * Settings.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
@XmlRootElement(name="settings")
public class Settings {
	
	private static Settings instance;
	
	/**
	 * Constructor.
	 */
	private Settings() {}
	
	/**
	 * Return singleton instance;
	 * @return Setting instance.
	 */
	public static Settings getInstance() {
		if(instance==null) instance = new Settings();
		return instance;
	}
	
	public static void setInstance(Settings settings) {
		Settings.instance = settings;
	}
	
	/** Is server visible - will server response to client's request? */
	private boolean visibleServer = true;
	
	/** Is server discoverable? (Used in Scan mode) */
	private boolean discoverableServer = true;
	
	/** Connection port. */
	private int connectionPort = Common.DEFAULT_PORT;
	
	/** Scan mode port. */
	private int scanPort = Common.DEFAULT_PORT;
	
	/** Custom connection port. */
	private boolean customConnectionPort = false;
	
	/** Custom scan mode port. */
	private boolean customScanPort = false;
	
	/** For programatically set logging on/off.
	 * For DEBUG only set 1, for DEBUG and WARN set 2, for DEBUG, WARN, and ERROR set 3.
	 * For disable logging set 0. */
	private int logLevel = 0;
	
	/** For encrypted communication. */
	private boolean encryptedCommunication = false;
	
	/** If users needs password for autentication. */
	private boolean protectWithPassword = false;
	
	/** Server user password. */
	private String userPassword = "";
	
	/**
	 * @return the visibleServer
	 */
	public boolean isVisibleServer() {
		return visibleServer;
	}
	
	/**
	 * @param visibleServer the visibleServer to set
	 */
	public void setVisibleServer(boolean visibleServer) {
		this.visibleServer = visibleServer;
	}
	
	/**
	 * @return the discoverableServer
	 */
	public boolean isDiscoverableServer() {
		return discoverableServer;
	}
	
	/**
	 * @param discoverableServer the discoverableServer to set
	 */
	public void setDiscoverableServer(boolean discoverableServer) {
		this.discoverableServer = discoverableServer;
	}
	
	/**
	 * @return the connectionPort
	 */
	public int getConnectionPort() {
		return connectionPort;
	}
	
	/**
	 * @param connectionPort the connectionPort to set
	 */
	public void setConnectionPort(int connectionPort) {
		this.connectionPort = connectionPort;
	}
	
	/**
	 * @return the scanPort
	 */
	public int getScanPort() {
		return scanPort;
	}
	
	/**
	 * @param scanPort the scanPort to set
	 */
	public void setScanPort(int scanPort) {
		this.scanPort = scanPort;
	}
	
	/**
	 * @return the customConnectionPort
	 */
	public boolean isCustomConnectionPort() {
		return customConnectionPort;
	}
	
	/**
	 * @param customConnectionPort the customConnectionPort to set
	 */
	public void setCustomConnectionPort(boolean customConnectionPort) {
		this.customConnectionPort = customConnectionPort;
	}
	
	/**
	 * @return the customScanPort
	 */
	public boolean isCustomScanPort() {
		return customScanPort;
	}
	
	/**
	 * @param customScanPort the customScanPort to set
	 */
	public void setCustomScanPort(boolean customScanPort) {
		this.customScanPort = customScanPort;
	}
	
	/**
	 * @return the logLevel
	 */
	public int getLogLevel() {
		return logLevel;
	}
	
	/**
	 * @param logLevel the logLevel to set
	 */
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}
	
	/**
	 * @return the encryptedCommunication
	 */
	public boolean isEncryptedCommunication() {
		return encryptedCommunication;
	}
	
	/**
	 * @param encryptedCommunication the encryptedCommunication to set
	 */
	public void setEncryptedCommunication(boolean encryptedCommunication) {
		this.encryptedCommunication = encryptedCommunication;
	}
	
	/**
	 * @return the protectWithPassword
	 */
	public boolean isProtectWithPassword() {
		return protectWithPassword;
	}
	
	/**
	 * @param protectWithPassword the protectWithPassword to set
	 */
	public void setProtectWithPassword(boolean protectWithPassword) {
		this.protectWithPassword = protectWithPassword;
	}
	
	/**
	 * @return the userPassword
	 */
	public String getUserPassword() {
		return userPassword;
	}
	
	/**
	 * @param userPassword the userPassword to set
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
}
