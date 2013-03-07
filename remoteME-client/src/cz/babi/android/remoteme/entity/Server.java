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

import java.io.Serializable;
import java.util.Date;

/**
 * Server.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Server implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String ipAddress;
	private String macAddress;
	private long port;
	private String password;
	private String serverName;
	private String osName;
	private Date added;
	private Date lastConnected;
	
	/**
	 * Constructor.
	 * @param id Server ID.
	 * @param ipAddress Server IP address.
	 * @param macAddress Mac address.
	 * @param port Server port.
	 * @param password Server password.
	 * @param serverName Server name.
	 * @param osName Operation System name.
	 * @param added When server was added to database.
	 * @param lastConnected When server was used for last.
	 */
	public Server(long id, String ipAddress, String macAddress, long port, String password,
			String serverName, String osName, Date added, Date lastConnected) {
		this.id = id;
		this.ipAddress = ipAddress;
		this.macAddress = (macAddress!=null) ? macAddress : "";
		this.port = port;
		this.password = (password!=null) ? password : "";
		this.serverName = (serverName!=null) ? serverName : "";
		this.osName = (osName!=null) ? osName : "";
		this.added = added;
		this.lastConnected = lastConnected;
	}
	
	/**
	 * Constructor.
	 * @param ipAddress Server IP address.
	 * @param macAddress Mac address.
	 * @param port Server port.
	 * @param password Server password.
	 * @param serverName Server name.
	 * @param osName Operation System name.
	 * @param added When server was added to database.
	 * @param lastConnected When server was used for last.
	 */
	public Server(String ipAddress, String macAddress, long port, String password,
			String serverName, String osName, Date added, Date lastConnected) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.password = (password!=null) ? password : "";
		this.serverName = (serverName!=null) ? serverName : "";
		this.osName = (osName!=null) ? osName : "";
		this.added = added;
		this.lastConnected = lastConnected;
	}
	
	/**
	 * Constructor.
	 * @param ipAddress Server IP address.
	 * @param port Server port.
	 * @param serverName Server name.
	 * @param osName Operation System name.
	 */
	public Server(String ipAddress, String macAddress, long port, String serverName, String osName) {
		this.ipAddress = ipAddress;
		this.macAddress = macAddress;
		this.port = port;
		this.serverName = (serverName!=null) ? serverName : "";
		this.osName = (osName!=null) ? osName : "";
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	
	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}
	
	/**
	 * @return the port
	 */
	public long getPort() {
		return port;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}
	
	/**
	 * @return the osName
	 */
	public String getOsName() {
		return osName;
	}
	
	/**
	 * @return the added
	 */
	public Date getAdded() {
		return added;
	}
	
	/**
	 * @return the lastConnected
	 */
	public Date getLastConnected() {
		return lastConnected;
	}
	
	/**
	 * @param ipAddress the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	/**
	 * @param macAddress the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	/**
	 * @param port the port to set
	 */
	public void setPort(long port) {
		this.port = port;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	/**
	 * @param osName the osName to set
	 */
	public void setOsName(String osName) {
		this.osName = osName;
	}
	
	/**
	 * @param lastConnected the lastConnected to set
	 */
	public void setLastConnected(Date lastConnected) {
		this.lastConnected = lastConnected;
	}
}
