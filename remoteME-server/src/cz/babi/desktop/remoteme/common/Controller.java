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

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.entity.Message.SimpleMessage;

/**
 * This class represents controller for Robot class.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Controller {
	
	private static final Logger LOGGER = Logger.getLogger(Controller.class.getSimpleName());
	
	private Robot robot;
	private Clipboard clipboard;
	private ClipboardOwner clipboardOwner;
	
	/**
	 * Init Robot.
	 * @return If robot is inited.
	 */
	public boolean initRobot() {
		try {
			robot = new Robot();
		} catch(AWTException awte) {
			if(Common.ERROR) LOGGER.error("[TCPClientHandler][Can not create Robot.]", awte);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Init clipboard.
	 */
	public void initClipboard() {
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		clipboardOwner = new ClipboardOwner() {
			@Override
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
				if(Common.DEBUG) LOGGER.debug("[lostOwnership]");
			}
		};
	}
	
	/**
	 * Move mouse.
	 * @param addInfo String include offsetX and offsetY.
	 */
	public void mouseMove(String addInfo) {
		String[] offsets = addInfo.split(SimpleMessage.SEPARATOR);
		float offsetX = Float.valueOf(offsets[0]);
		float offsetY = Float.valueOf(offsets[1]);
		
		if(Common.DEBUG) LOGGER.debug("[mouseMove][" + offsetX*-1 + ";" + offsetY*-1 + "]");
		
		Point currentLocation = MouseInfo.getPointerInfo().getLocation();
		
		robot.mouseMove(currentLocation.x + (int)offsetX*-1,
				currentLocation.y + (int)offsetY*-1);
	}
	
	/**
	 * Left mouse click.
	 */
	public void mouseLeftClick() {
		if(Common.DEBUG) LOGGER.debug("[mouseLeftClick]");
		
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	/**
	 * Right mouse click.
	 */
	public void mouseRightClick() {
		if(Common.DEBUG) LOGGER.debug("[mouseRightClick]");
		
		robot.mousePress(InputEvent.BUTTON3_MASK);
		robot.mouseRelease(InputEvent.BUTTON3_MASK);
	}
	
	/**
	 * Mouse wheel.
	 * @param addInfo Wheel amout.
	 */
	public void mouseWheel(String addInfo) {
		if(Common.DEBUG) LOGGER.debug("[mouseWheel]");
		
		float wheelAmount = Float.valueOf(addInfo);
		if(Common.DEBUG) LOGGER.debug("[mouseWheel][" + wheelAmount + "]");
		robot.mouseWheel(((int)wheelAmount*-1));
	}
	
	/**
	 * Simulate clipboard.
	 * @param character Character to paste from clipboard.
	 */
	public void keyClipboard(String addInfo) {
		if(Common.DEBUG) LOGGER.debug("[keyClipboard][" + addInfo + "]");
		
		StringSelection stringSelection = new StringSelection(addInfo);
		clipboard.setContents(stringSelection, clipboardOwner);
		
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
	}
	
	/**
	 * Simulate key stroke.
	 * @param character Key(s) to stroke. May contains more commands.
	 */
	public void keyStroke(String addInfo) {
		Integer[] keyCodes = null;
		ArrayList<Integer[]> keyCodesList = null;
		
		/* Need to check if addInfo contains more than one command. */
		String[] commands = addInfo.split(MappedKeys.DIVIDER);
		
		/* There is only one command. */
		if(commands.length==1) {
			keyCodes = MappedKeys.keyMap.get(addInfo);
			
			if(keyCodes!=null) {
				makeStroke(keyCodes);
			} else if(Common.DEBUG) LOGGER.debug("[keyStroke][No mapped key for '" +
					addInfo + "'.]");
		} else {
			keyCodesList = new ArrayList<Integer[]>();
			/* For every command we need to obtain its key code(s) */
			for(int i=0; i<commands.length; i++) {
				Integer[] command = MappedKeys.keyMap.get(commands[i]);
				if(command!=null) keyCodesList.add(command);
			}
			
			if(!keyCodesList.isEmpty()) {
				/* Iterate arrays in list. */
				ArrayList<Integer> list = new ArrayList<Integer>();
				for(Integer[] codes : keyCodesList) {
					for(Integer code : codes) list.add(code);
				}
				
				keyCodes = new Integer[list.size()];
				for(int i=0; i<keyCodes.length; i++) keyCodes[i] = list.get(i);
				
				makeStroke(keyCodes);
			} else if(Common.DEBUG) LOGGER.debug("[keyStroke][No mapped key for '" +
					addInfo + "'.]");
		}
	}
	
	/**
	 * Make key stroke.
	 * @param character Key(s) to stroke.
	 */
	private void makeStroke(Integer[] keyCodes) {
		/*
		 * There is a bug with shift and arrows.
		 * Workaround is turn num lock off.
		 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4908075
		 */
		boolean needSetNumlockOn = false;
		/* If numlock is on. */
		if(Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK)) {
			boolean isShift = false;
			boolean isArrow = false;
			
			/* Check if there is a shift or an arrow. */
			for(Integer integer : keyCodes) {
				if(integer==KeyEvent.VK_SHIFT) isShift = true;
				if(integer==KeyEvent.VK_LEFT) isArrow = true;
				if(integer==KeyEvent.VK_RIGHT) isArrow = true;
				if(integer==KeyEvent.VK_UP) isArrow = true;
				if(integer==KeyEvent.VK_DOWN) isArrow = true;
			}
			
			/* Disable numlock. */
			if(isShift && isArrow) {
				robot.keyPress(KeyEvent.VK_NUM_LOCK);
				robot.keyRelease(KeyEvent.VK_NUM_LOCK);
				needSetNumlockOn = true;
			}
		}
		
		/* Do press. */
		for(int i=0; i<keyCodes.length; i++) {
			if(Common.DEBUG) LOGGER.debug("[keyPress][" + keyCodes[i] + "]");
			
			robot.keyPress(keyCodes[i]);
		}
		
		/* Do release. */
		for(int i=keyCodes.length-1; i>=0; i--){
			if(Common.DEBUG) LOGGER.debug("[keyRelease][" + keyCodes[i] + "]");
			
			robot.keyRelease(keyCodes[i]);
		}
		
		/* Set numlock back. */
		if(needSetNumlockOn) {
			robot.keyPress(KeyEvent.VK_NUM_LOCK);
			robot.keyRelease(KeyEvent.VK_NUM_LOCK);
		}
	}
	
	/**
	 * Do special command.
	 * @param specialCommand Special command.
	 */
	public void doSpecial(String specialCommand) {
		if(Common.DEBUG) LOGGER.debug("[doSpecial]");
		switch(specialCommand) {
			case Common.COMMAND_SHUTDOWN:
				doShutdown();
				break;
			case Common.COMMAND_RESTART:
				doRestart();
				break;
			case Common.COMMAND_LOGOFF:
				doLogoff();
				break;
			default:
				if(Common.DEBUG) LOGGER.debug("[doSpecial][Wrong command.]");
				break;
		}
	}
	
	/**
	 * Shut down computer
	 */
	public void doShutdown() {
		if(Common.DEBUG) LOGGER.debug("[doShutdown]");
		
		String shutdownCommand = null;
		
		if(SystemUtils.IS_OS_AIX)
			shutdownCommand = "shutdown -Fh now";
		else if(SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC||
				SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD ||
				SystemUtils.IS_OS_UNIX)
			shutdownCommand = "shutdown -h now";
		else if(SystemUtils.IS_OS_HP_UX)
			shutdownCommand = "shutdown -hy 1";
		else if(SystemUtils.IS_OS_IRIX)
			shutdownCommand = "shutdown -y -g 1";
		else if(SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_SUN_OS)
			shutdownCommand = "shutdown -y -i5 -g0";
		else if(SystemUtils.IS_OS_WINDOWS_XP || SystemUtils.IS_OS_WINDOWS_VISTA ||
				SystemUtils.IS_OS_WINDOWS_7 || System.getProperty("os.name").startsWith("win"))
			shutdownCommand = "shutdown.exe -s -t 0";
		else {
			if(Common.DEBUG) LOGGER.debug("[doShutdown][Unknown OS.]");
			return;
		}
		
		try {
			Runtime.getRuntime().exec(shutdownCommand);
		} catch(IOException e) {
			if(Common.ERROR) LOGGER.error("[doShutdown][Ups. Can not shut down pc.]");
		}
	}
	
	/**
	 * Restart computer.
	 */
	public void doRestart() {
		if(Common.DEBUG) LOGGER.debug("[doRestart]");
		
		String shutdownCommand = null;
		
		if(SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC||
				SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_NET_BSD || SystemUtils.IS_OS_OPEN_BSD ||
				SystemUtils.IS_OS_UNIX)
			shutdownCommand = "shutdown -r now";
		else if(SystemUtils.IS_OS_WINDOWS_XP || SystemUtils.IS_OS_WINDOWS_VISTA ||
				SystemUtils.IS_OS_WINDOWS_7 || System.getProperty("os.name").startsWith("win"))
			shutdownCommand = "shutdown.exe -r -t 0";
		else {
			if(Common.DEBUG) LOGGER.debug("[doRestart][Unknown OS.]");
			return;
		}
		
		try {
			Runtime.getRuntime().exec(shutdownCommand);
		} catch(IOException e) {
			if(Common.ERROR) LOGGER.error("[doRestart][Ups. Can not restart pc.]");
		}
	}
	
	/**
	 * Log off current user.
	 */
	public void doLogoff() {
		if(Common.DEBUG) LOGGER.debug("[doLogoff]");
		
		String shutdownCommand = null;
		
		if(SystemUtils.IS_OS_WINDOWS_XP || SystemUtils.IS_OS_WINDOWS_VISTA ||
				SystemUtils.IS_OS_WINDOWS_7 || System.getProperty("os.name").startsWith("win"))
			shutdownCommand = "shutdown.exe -l";
		else {
			if(Common.DEBUG) LOGGER.debug("[doLogoff][Unknown OS.]");
			return;
		}
		
		try {
			Runtime.getRuntime().exec(shutdownCommand);
		} catch(IOException e) {
			if(Common.ERROR) LOGGER.error("[doLogoff][Ups. Can not log off user.]");
		}
	}
}
