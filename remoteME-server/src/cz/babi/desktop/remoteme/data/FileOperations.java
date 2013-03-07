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

package cz.babi.desktop.remoteme.data;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import cz.babi.desktop.remoteme.Settings;
import cz.babi.desktop.remoteme.common.Common;

/**
 * Some file operations.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public final class FileOperations {
	
	private static final org.apache.log4j.Logger LOGGER =
			org.apache.log4j.Logger.getLogger(FileOperations.class);
	
	private static Settings settings = Settings.getInstance();
	
	private static FileOperations instance = new FileOperations();
	
	private static final String USER_DIR = "user-data";
	private static final String SETTINGS_FILE = "settings.xml";
	private static final String CURRENT_DIR = getPathToRealCurrentDirectory();
	
	public static final File USER_DATA_FOLDER = new File(CURRENT_DIR + File.separator +
			USER_DIR);
	public static final File USER_SETTINGS_FILE = new File(CURRENT_DIR + File.separator +
			USER_DIR + File.separator + SETTINGS_FILE);
	
	/**
	 * Private constructor.
	 */
	private FileOperations() {}
	
	/**
	 * Get singleton instance.
	 * @return Instance Instance of settings.
	 */
	public static FileOperations getInstance() {
		return instance;
	}
	
	/**
	 * Check if input folder/file exists.
	 * @param path Folder or patch to check.
	 * @return Existence of input folder/file.
	 */
	public boolean checkFilePath(File path) {
		if(path==null) return false;
		else return path.exists();
	}
	
	/**
	 * Create input file.
	 * @param file file to create.
	 * @return Is created?
	 */
	public boolean createFile(File file) {
		boolean isCreated = false;
		
		try {
			isCreated = file.createNewFile();
		} catch (IOException ex) {
			if(Common.ERROR) LOGGER.error("[createFile][Can not create file '"
					+ file.getPath() + "'.]", ex);
		}
		
		return isCreated;
	}
	
	/**
	 * Create input folder(s).
	 * @param directory Folder(s) to create.
	 * @return Is created?
	 */
	public boolean createDirectory(File directory) {
		return directory.mkdirs();
	}
	
	/**
	 * Set writable flag to input file.
	 * @param file File to set.
	 * @param canWriteToFile Is writable or not.
	 */
	public void setWritable(File file, boolean canWriteToFile) {
		file.setWritable(canWriteToFile);
	}
	
	/**
	 * Save settings to input file.
	 * @param file Input file.
	 * @return Settings saved?
	 */
	public boolean saveSettingsToXml(File file) {
		JAXBContext context = null;
		Marshaller marshaller = null;
		
		try {
			context = JAXBContext.newInstance(Settings.class);
		} catch(JAXBException jex) {
			if(Common.ERROR) LOGGER.error("[saveSettingsToXml][Can not obtain context.]", jex);
			return false;
		}
		
		try {
			marshaller = context.createMarshaller();
		} catch(JAXBException jex) {
			if(Common.ERROR) LOGGER.error("[saveSettingsToXml][Can not create marshaller.]", jex);
			return false;
		}
		
		try {
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		} catch(PropertyException pex) {
			if(Common.ERROR) LOGGER.error("[saveSettingsToXml][Can not set property to marshaller.]", pex);
			return false;
		}
		
		try {
			marshaller.marshal(settings, file);
		} catch(JAXBException jex) {
			if(Common.ERROR) LOGGER.error("[saveSettingsToXml][Can not save settings.]", jex);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Load settings from input file.
	 * @param file File to load.
	 * @return Is loading done?
	 */
	public boolean readSettingsFromXml(File file) {
		Settings newSettings;
		JAXBContext context = null;
		Unmarshaller unmarshaller = null;
		
		try {
			context = JAXBContext.newInstance(Settings.class);
		} catch (JAXBException jex) {
			if(Common.ERROR) LOGGER.error("[readSettingsFromXml][Can not obtain context.]", jex);
			return false;
		}
		
		try {
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException jex) {
			if(Common.ERROR) LOGGER.error("[readSettingsFromXml][Can not create unmarshaller.]", jex);
			return false;
		}
		
		try {
			newSettings = (Settings)unmarshaller.unmarshal(file);
		} catch (JAXBException jex) {
			if(Common.ERROR) LOGGER.error("[readSettingsFromXml][Can not load settings.]", jex);
			return false;
		}
		
		/* If loaded settings is not null. */
		if(newSettings!=null) {
			settings.setConnectionPort(newSettings.getConnectionPort());
			settings.setCustomConnectionPort(newSettings.isCustomConnectionPort());
			settings.setCustomScanPort(newSettings.isCustomScanPort());
			settings.setDiscoverableServer(newSettings.isDiscoverableServer());
			settings.setEncryptedCommunication(newSettings.isEncryptedCommunication());
			settings.setLogLevel(newSettings.getLogLevel());
			settings.setProtectWithPassword(newSettings.isProtectWithPassword());
			settings.setScanPort(newSettings.getScanPort());
			settings.setUserPassword(newSettings.getUserPassword());
			settings.setVisibleServer(newSettings.isVisibleServer());
			return true;
		} else return false;
	}
	
	/**
	 * Obtain directory when jar file is located.
	 * @return Current directory.
	 */
	private static String getPathToRealCurrentDirectory() {
		String ret = "";
		
		try {
			String pathToJar = Common.class.getProtectionDomain().getCodeSource().
					getLocation().getPath();
			File fileToJar = new File(pathToJar);
			
			ret =  URLDecoder.decode(fileToJar.getAbsolutePath().substring(0,
					fileToJar.getAbsolutePath().lastIndexOf(File.separator)), "UTF-8");
		} catch(UnsupportedEncodingException ex) {
			if(Common.ERROR) LOGGER.error("[getPathToRealCurrentDirectory][Can not obtain " +
					"current directory.]" + ex);
		}
		
		return ret;
	}
	
	/**
	 * Cal after user change settings.
	 */
	public void needSaveSettings() {
		/* If settings file exist we can save settings to it. */
		if(checkFilePath(USER_SETTINGS_FILE)) {
			setWritable(USER_SETTINGS_FILE, true);
			saveSettingsToXml(USER_SETTINGS_FILE);
			setWritable(USER_SETTINGS_FILE, false);
		} else {
			/* If user data folder does not exists we need to create it. */
			if(!checkFilePath(USER_DATA_FOLDER)) {
				/* If user data folder was created successfully we cen try
				 * to create settings file. */
				if(createDirectory(USER_DATA_FOLDER))
					/* If settings file was created we can save settings to it. */
					if(createFile(USER_SETTINGS_FILE)) {
						saveSettingsToXml(USER_SETTINGS_FILE);
						setWritable(USER_SETTINGS_FILE, false);
					}
			}
			/* User data folder exists. */
			else
				/* If settings file was created we can save settings to it. */
				if(createFile(USER_SETTINGS_FILE)) {
					saveSettingsToXml(USER_SETTINGS_FILE);
					setWritable(USER_SETTINGS_FILE, false);
				}
		}
	}
}
