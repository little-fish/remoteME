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

package cz.babi.desktop.remoteme.gui;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.connection.TCPClientHandler;
import cz.babi.desktop.remoteme.data.Icons;
import cz.babi.desktop.remoteme.entity.MyThreadGroups;

/**
 * Tray menu.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class TrayMenu {
	
	private static final Logger LOGGER = Logger.getLogger(TrayMenu.class.getSimpleName());
	private final ResourceBundle l10n = Common.L10N;
	
	private PopupMenu popupMenu;
	private TrayIcon trayIcon;
	private SystemTray systemTray;
	
	private MenuItem miExit;
	private MenuItem miServerInformation;
	
	private MainFrame mainFrame;
	
	/**
	 * Constructor.
	 */
	public TrayMenu() {
		initTrayMenu();
	}
	
	/**
	 * Init tray menu.
	 */
	private void initTrayMenu() {
		if(Common.DEBUG) LOGGER.debug("[initTrayMenu]");
		
		/* Check if the SystemTray is supported. */
		if(!SystemTray.isSupported()) {
			if(Common.ERROR) LOGGER.error("[initTrayMenu][System Tray is not supported.]");
			return;
		}
		
		String appName = l10n.getString("Application.name");
		String appVersion = l10n.getString("Application.version");
		
		systemTray = SystemTray.getSystemTray();
		popupMenu = new PopupMenu();
		trayIcon = new TrayIcon(Icons.APP_ICON.getImage(),
				appName + " v" + appVersion);
		
		/* Init server information menu item. */
		String sServerInformation = l10n.getString("TrayMenu.menuitem.serverinformation.text");
		miServerInformation = new MenuItem(sServerInformation);
		miServerInformation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mainFrame==null) mainFrame = new MainFrame();
				mainFrame.showFrame();
			}
		});
		
		/* Init exit menu item. */
		String sExit = l10n.getString("TrayMenu.menuitem.exit.text");
		miExit = new MenuItem(sExit);
		miExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeAllConnectionsAndServers();
				System.exit(0);
			}
		});
		
		/* Add menu items to popup menu. */
		popupMenu.add(miServerInformation);
		popupMenu.addSeparator();
		popupMenu.add(miExit);
		
		/* Add popup menu to tray icon. */
		trayIcon.setPopupMenu(popupMenu);
		trayIcon.setImageAutoSize(true);
		
		try {
			/* Add tray icon to system tray. */
			systemTray.add(trayIcon);
		} catch(AWTException e) {
			if(Common.ERROR) LOGGER.error("[initTrayMenu][TrayIcon could not be added to " +
					"System Tray.]");
		}
	}
	
	/**
	 * Close all connections and stop servers.
	 */
	private void closeAllConnectionsAndServers() {
		/* Stop UDP server. */
		MyThreadGroups.getInstance().getUdpScanModeServer().stopServer();
		if(MyThreadGroups.getInstance().getTcpServer()!=null) {
			/* Close connection for all clients. */
			for(TCPClientHandler client : MyThreadGroups.getInstance().getTcpServer().getClients())
				client.forceDisconnect();
			/* Stop TCP server. */
			MyThreadGroups.getInstance().getTcpServer().stopServer();
		}
	}
}
