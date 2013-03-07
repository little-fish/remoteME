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

package cz.babi.desktop.remoteme.entity;

import cz.babi.desktop.remoteme.connection.TCPClientServer;
import cz.babi.desktop.remoteme.connection.UDPScanModeServer;

/**
 * Here we are holding all threads.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public final class MyThreadGroups {
	
	private static MyThreadGroups instance = new MyThreadGroups();
	
	private final ThreadGroup serversThreadGroup;
	private final ThreadGroup tcpThreadGroup;
	
	private TCPClientServer tcpServer;
	private UDPScanModeServer udpScanModeServer;
	
	private MyThreadGroups() {
		serversThreadGroup = new ThreadGroup("remoteMe-serversGroup");
		tcpThreadGroup = new ThreadGroup(serversThreadGroup, "tcpGroup");
	}
	
	/**
	 * Get singleton instance.
	 * @return Instance.
	 */
	public static MyThreadGroups getInstance() {
		return instance;
	}
	
	/**
	 * @return the threadGroup
	 */
	public ThreadGroup getServersThreadGroup() {
		return serversThreadGroup;
	}
	
	/**
	 * @return the tcpClientstThreadGroup
	 */
	public ThreadGroup getTcpThreadGroup() {
		return tcpThreadGroup;
	}
	
	/**
	 * @return the tcpServer
	 */
	public TCPClientServer getTcpServer() {
		return tcpServer;
	}
	
	/**
	 * @return the udpScanModeServer
	 */
	public UDPScanModeServer getUdpScanModeServer() {
		return udpScanModeServer;
	}
	
	/**
	 * Create UDP server.
	 * @param port Port to bind.
	 */
	public void createUDPServer(int port) {
		udpScanModeServer = new UDPScanModeServer(port);
		
		Thread udpServerThread = new Thread(serversThreadGroup,
				udpScanModeServer, "UDP Server");
		udpServerThread.setDaemon(false);
		udpServerThread.start();
	}
	
	/**
	 * Create TCP server.
	 * @param port Port to bind.
	 */
	public void createTCPServer(int port) {
		tcpServer = new TCPClientServer(port);
		
		Thread tcpServerThread = new Thread(serversThreadGroup,
				tcpServer, "TCP Server");
		tcpServerThread.setDaemon(false);
		tcpServerThread.start();
	}
}
