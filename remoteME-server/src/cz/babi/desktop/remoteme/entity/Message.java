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

import java.util.HashMap;

/**
 * Message holds Simple message objects.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Message {
	
	private static HashMap<Integer, SimpleMessage> cache = new HashMap<>();
	
	/** Scan_mode request. */
	public static SimpleMessage SCAN_MODE_REQUEST = getSimpleMessage(1);
	/** Scan_mode response. */
	public static SimpleMessage SCAN_MODE_RESPONSE = getSimpleMessage(2);
	/** Do we need encrypted communication. */
	public static SimpleMessage NEED_ENCRYPTED_COMMUNICATION = getSimpleMessage(3);
	/** Do we need password authentication. */
	public static SimpleMessage DO_I_NEED_PASSWORD = getSimpleMessage(4);
	/** Simple yes. */
	public static SimpleMessage YES = getSimpleMessage(5);
	/** Simple no. */
	public static SimpleMessage NO = getSimpleMessage(6);
	/** Client sends password to check. */
	public static SimpleMessage CHECK_PASSWORD = getSimpleMessage(7);
	/** Left mouse click. */
	public static SimpleMessage MOUSE_LEFT_CLICK = getSimpleMessage(8);
	/** Right mouse click. */
	public static SimpleMessage MOUSE_RIGHT_CLICK = getSimpleMessage(9);
	/** Mouse wheel. */
	public static SimpleMessage MOUSE_WHEEL = getSimpleMessage(10);
	/** Mouse moove. */
	public static SimpleMessage MOUSE_MOVE = getSimpleMessage(11);
	/** Simulate key stroke. */
	public static SimpleMessage KEY_STROKE = getSimpleMessage(12);
	/** Simulate clipboard */
	public static SimpleMessage KEY_CLIPBOARD = getSimpleMessage(13);
	/** User send this message to server if he wants to disconnect. */
	public static SimpleMessage BYE_BYE = getSimpleMessage(14);
	/** Used for special commands, like shutdown, restart od logoff. */
	public static SimpleMessage SPECIAL_COMMAND = getSimpleMessage(15);
	
	/**
	 * Get Simple message from cache.
	 * @param id Message ID.
	 * @return Simple message.
	 */
	private static SimpleMessage getSimpleMessage(int id) {
		SimpleMessage message = cache.get(id);
		
		if(message==null) {
			message = new SimpleMessage(id);
			cache.put(id, message);
		}
		
		return message;
	}
	
	/**
	 * Simple message. This message will be used for comunication between client and server.
	 * 
	 * @author Martin Misiarz
	 * @author dev.misiarz@gmail.com
	 * @created 8.2.2013, 18:26:42
	 */
	public static class SimpleMessage {
		
		public static final String SEPARATOR = ";";
		
		private final int id;
		private String addInfo;
		
		/**
		 * Costructor.
		 * @param id Message Identificator.
		 */
		public SimpleMessage(int id) {
			this.id = id;
		}
		
		/**
		 * Costructor.
		 * @param id Message identificator.
		 * @param addInfo Additional information.
		 */
		public SimpleMessage(int id, String addInfo) {
			this.id = id;
			this.addInfo = addInfo;
		}
		
		/**
		 * @return the addInfo
		 */
		public String getAddInfo() {
			return addInfo;
		}
		
		/**
		 * @param addInfo the addInfo to set
		 */
		public void setAddInfo(String addInfo) {
			this.addInfo = addInfo;
		}
		
		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		
		@Override
		public String toString() {
			return String.valueOf(this.id) + SEPARATOR + this.addInfo;
		}
	}
}
