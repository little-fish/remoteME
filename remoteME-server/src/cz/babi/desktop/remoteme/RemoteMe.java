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

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.data.FileOperations;
import cz.babi.desktop.remoteme.entity.MyThreadGroups;
import cz.babi.desktop.remoteme.gui.TrayMenu;

/**
 * Main class of remoteME server application.
 * 
 * @author babi
 * @author dev.misiarz@gmail.com
 */
public class RemoteMe {
	
	private static final Logger LOGGER = Logger.getLogger(RemoteMe.class.getSimpleName());
	private static FileOperations fileOperations = FileOperations.getInstance();
	private static Settings settings = Settings.getInstance();
	private static MyThreadGroups myThreadGroups = MyThreadGroups.getInstance();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* User may add commands when starting the application. */
		if(args.length>0) {
			String command = args[0];
			/* If there is a debug argument, we need to set logging on. */
			if(command.equals(Common.DEBUG_ARGUMENT))
				settings.setLogLevel(Common.LOG_ERROR);
		}
		
		if(Common.DEBUG) LOGGER.debug("[main][Application started.]");
		
		RemoteMe remoteMe = new RemoteMe();
		
		/* Create/load settings. */
		remoteMe.settingsStuff();
		
		/* Create Tray menu. */
		new TrayMenu();
		
		/* Create TCP server. */
		if(settings.isVisibleServer()) remoteMe.createTCPServer();
		
		/* Create UDP server. */
		if(settings.isDiscoverableServer()) remoteMe.createUDPServer();
		
		if(Common.DEBUG) LOGGER.debug("[main][Active threads in group '" +
				myThreadGroups.getServersThreadGroup().getName() + "': " +
				myThreadGroups.getServersThreadGroup().activeCount()+ "]");
	}
	
	/**
	 * Load settings or create settings file and save settings to it.
	 */
	private void settingsStuff() {
		/* If settings file exist we can simple read settings from it. */
		if(fileOperations.checkFilePath(FileOperations.USER_SETTINGS_FILE)) {
			fileOperations.readSettingsFromXml(FileOperations.USER_SETTINGS_FILE);
		} else {
			/* If user data folder does not exists we need to create it. */
			if(!fileOperations.checkFilePath(FileOperations.USER_DATA_FOLDER)) {
				/* If user data folder was created successfully we cen try
				 * to create settings file. */
				if(fileOperations.createDirectory(FileOperations.USER_DATA_FOLDER))
					/* If settings file was created we can save settings to it. */
					if(fileOperations.createFile(FileOperations.USER_SETTINGS_FILE)) {
						fileOperations.saveSettingsToXml(FileOperations.USER_SETTINGS_FILE);
						fileOperations.setWritable(FileOperations.USER_SETTINGS_FILE, false);
					}
			}
			/* User data folder exists. */
			else
				/* If settings file was created we can save settings to it. */
				if(fileOperations.createFile(FileOperations.USER_SETTINGS_FILE)) {
					fileOperations.saveSettingsToXml(FileOperations.USER_SETTINGS_FILE);
					fileOperations.setWritable(FileOperations.USER_SETTINGS_FILE, false);
				}
		}
	}
	
	/**
	 * Create TCP server.
	 */
	private void createTCPServer() {
		int port;
		
		if(settings.isCustomConnectionPort()) port = settings.getConnectionPort();
		else port = Common.DEFAULT_PORT;
		
		myThreadGroups.createTCPServer(port);
	}
	
	/**
	 * Create UDP server.
	 */
	private void createUDPServer() {
		int port;
		
		if(settings.isCustomScanPort()) port = settings.getScanPort();
		else port = Common.DEFAULT_PORT;
		
		myThreadGroups.createUDPServer(port);
	}
}