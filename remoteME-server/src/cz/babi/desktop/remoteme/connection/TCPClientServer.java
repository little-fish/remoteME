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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.entity.MyThreadGroups;
import cz.babi.desktop.remoteme.interfaces.DefaultServer;

/**
 * This class represent TCP server,
 * which will be used for init communication with remote clients.
 * The server is waiting for incoming requests (clients) and after it
 * receive right request, it will create a new thread represents one client.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class TCPClientServer extends Observable implements Runnable, DefaultServer {
	
	private static final Logger LOGGER = Logger.getLogger(TCPClientServer.class.getSimpleName());
	
	/** Variable define user port. */
	private final int PORT;
	/** Variable define if server can run. */
	private boolean keepRunning = false;
	
	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;
	
	/** List holds all clients. */
	private ArrayList<TCPClientHandler> clients;
	
	/**
	 * Constructor of TCP server.
	 * @param port Port to set.
	 */
	public TCPClientServer(int port) {
		if(Common.DEBUG) LOGGER.debug("[TCPClientServer][Public constructor.]");
		
		this.PORT = port;
		clients = new ArrayList<>();
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
		if(Common.DEBUG) LOGGER.debug("[startServer][TCP Server is starting on port: '" + PORT + "'.]");
		
		try {
			serverSocket = new ServerSocket(PORT);
		} catch(IOException ioe) {
			LOGGER.error("[startServer][Can not create a ServerSocket on port " +
					PORT + ".]", ioe);
			stopServer();
			return;
		}
		
		keepRunning = true;
		
		if(Common.DEBUG) LOGGER.debug("[startServer][TCP Server is ready for incoming requests.]");
	}
	
	@Override
	public void waitForConnection() {
		/* There need to be a loop for waiting for an incoming clients. */
		while(true) {
			if(Common.DEBUG) LOGGER.debug("[waitForConnection][Waiting for incoming request...]");
			
			try {
				clientSocket = serverSocket.accept();
			} catch(IOException ioe) {
				if(Common.ERROR) LOGGER.error("[waitForConnection][An error occurred during " +
						"receiving an tcp packet.]");
				return;
			}
			
			Thread tcpClientHandler = new Thread(MyThreadGroups.getInstance().getTcpThreadGroup(),
					new TCPClientHandler(clientSocket, this));
			
			tcpClientHandler.setDaemon(false);
			tcpClientHandler.start();
			
			if(Common.DEBUG) LOGGER.debug("[waitForConnection][[Active threads in group '" +
					MyThreadGroups.getInstance().getServersThreadGroup().getName() + "': " +
					MyThreadGroups.getInstance().getServersThreadGroup().activeCount()+ "]");
		}
	}
	
	@Override
	public void stopServer() {
		if(Common.DEBUG) LOGGER.debug("[stopServer]");
		
		if(keepRunning) keepRunning = false;
		
		if(serverSocket!=null && !serverSocket.isClosed()) {
			try {
				serverSocket.close();
			} catch(IOException ioe) {
				if(Common.ERROR) LOGGER.error("[stopServer][Can not close ServerSocket.]", ioe);
			}
			
			if(Common.DEBUG) LOGGER.debug("[stopServer][Server is off.]");
		}
	}
	
	/**
	 * Remove all clients.
	 */
	public void clearClients() {
		clients.clear();
		
		setChanged();
		notifyObservers(clients.size());
	}
	
	/**
	 * @return the clients
	 */
	public ArrayList<TCPClientHandler> getClients() {
		return clients;
	}
	
	/**
	 * Add client to list.
	 * @param client New Client.
	 */
	public void removeClient(TCPClientHandler client) {
		clients.remove(client);
		
		setChanged();
		notifyObservers(clients.size());
	}
	
	/**
	 * Remove client from list.
	 * @param client Client to remove.
	 */
	public void addClient(TCPClientHandler client) {
		clients.add(client);
		
		setChanged();
		notifyObservers(clients.size());
	}
}
