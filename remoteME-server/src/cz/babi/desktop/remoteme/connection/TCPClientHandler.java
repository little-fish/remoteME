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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.Settings;
import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.common.Controller;
import cz.babi.desktop.remoteme.crypto.AES128;
import cz.babi.desktop.remoteme.entity.Message;
import cz.babi.desktop.remoteme.entity.Message.SimpleMessage;

/**
 * Client handler.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class TCPClientHandler implements Runnable {
	
	private static final Logger LOGGER = Logger.getLogger(TCPClientHandler.class.getSimpleName());
	
	private static Settings settings = Settings.getInstance();
	
	private static final AES128 AES128_DEFAULT = Common.AES128_DEFAULT;
	
	private final Socket clientSocket;
	private TCPClientServer server;
	
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	/* This controller will do all job. */
	private Controller controller;
	
	/**
	 * Constructor.
	 * @param clientSocket Client socket.
	 */
	public TCPClientHandler(Socket clientSocket, TCPClientServer server) {
		if(Common.DEBUG) LOGGER.debug("[TCPClientHandler][Public constructor.]");
		
		this.clientSocket = clientSocket;
		this.server = server;
		
		try {
			in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		} catch(IOException ioe) {
			if(Common.ERROR) LOGGER.error("[TCPClientHandler][Can not create input reader " +
					"from client socket.]",	ioe);
			closeConnection();
			return;
		}
		
		try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch(IOException ioe) {
			if(Common.ERROR) LOGGER.error("[TCPClientHandler][Can not create output writer " +
					"from client socket.]",	ioe);
			closeConnection();
			return;
		}
		
		controller = new Controller();
		
		if(!controller.initRobot()) {
			closeConnection();
			return;
		}
		
		controller.initClipboard();
	}
	
	@Override
	public void run() {
		/* First of all, there will be a password check request. */
		String incomingMessage = null;
		
		try {
			incomingMessage = in.readLine();
		} catch(IOException ioe) {
			if(Common.ERROR) LOGGER.error("[run][Can not obtain an incoming message.]", ioe);
		}
		
		if(incomingMessage!=null) {
			String decryptedRequest = AES128_DEFAULT.decryptText(incomingMessage);
			SimpleMessage simpleMessage = parseIncommingMessage(decryptedRequest);
			
			String responseMessage;
			
			if(simpleMessage.getId()==Message.DO_I_NEED_PASSWORD.getId()) {
				if(settings.isProtectWithPassword()) {
					if(Common.DEBUG) LOGGER.debug("[run][User needs password comunicate with " +
							"server.]");
					responseMessage = AES128_DEFAULT.encryptText(Message.YES.toString());
					
					/* Send message that user needs password. */
					out.println(responseMessage);
					
					if(Common.DEBUG) LOGGER.debug("[run][Waiting for 'check password' message...]");
					
					try {
						incomingMessage = in.readLine();
					} catch(IOException ioe) {
						if(Common.ERROR) LOGGER.error("[run][Can not obtain an incoming message.]", ioe);
					}
					
					decryptedRequest = AES128_DEFAULT.decryptText(incomingMessage);
					simpleMessage = parseIncommingMessage(decryptedRequest);
					
					if(simpleMessage.getId()==Message.CHECK_PASSWORD.getId()) {
						/* If both passwords are empty. */
						if(settings.getUserPassword().isEmpty() && simpleMessage.getAddInfo().isEmpty()) {
							if(Common.DEBUG) LOGGER.debug("[run][Both passwords are empty.]");
							responseMessage = AES128_DEFAULT.encryptText(Message.YES.toString());
						} else
							/* If both password are same. */
							if(settings.getUserPassword().compareTo(simpleMessage.getAddInfo())==0) {
								if(Common.DEBUG) LOGGER.debug("[run][Both passwords are same.]");
								responseMessage = AES128_DEFAULT.encryptText(Message.YES.toString());
							} else {
								/* Passwords are not same. So there is no need to wait for another requests. */
								if(Common.DEBUG) LOGGER.debug("[run][Passwords are not same.]");
								responseMessage = AES128_DEFAULT.encryptText(Message.NO.toString());
								out.println(responseMessage);
								return;
							}
						
						/* Send message that user needs password. */
						out.println(responseMessage);
					} else if(Common.DEBUG) LOGGER.debug("[run][The incoming 'pasword check' message " +
							"has a wrong ID.]");
					
				} else {
					if(Common.DEBUG) LOGGER.debug("[run][User do not needs password communicate " +
							"with server.]");
					responseMessage = AES128_DEFAULT.encryptText(Message.NO.toString());
					
					/* Send message that user do not needs password. */
					out.println(responseMessage);
				}
			} else {
				if(Common.DEBUG) LOGGER.debug("[run][The incoming 'do i need password' message " +
						"has a wrong ID.]");
				return;
			}
			
			if(Common.DEBUG) LOGGER.debug("[run][Waiting for 'need encrypted communication' " +
					"request.]");
			
			incomingMessage = null;
			try {
				incomingMessage = in.readLine();
			} catch(IOException ioe) {
				if(Common.ERROR) LOGGER.error("[run][Can not obtain an incoming message.]", ioe);
			}
			
			if(incomingMessage!=null) {
				decryptedRequest = AES128_DEFAULT.decryptText(incomingMessage);
				simpleMessage = parseIncommingMessage(decryptedRequest);
				
				if(simpleMessage.getId()==Message.NEED_ENCRYPTED_COMMUNICATION.getId()) {
					if(settings.isEncryptedCommunication()) {
						if(Common.DEBUG) LOGGER.debug("[run][Need encrypted communication.]");
						responseMessage = AES128_DEFAULT.encryptText(Message.YES.toString());
						/* Send message that we need encrypted communication. */
						out.println(responseMessage);
					} else {
						if(Common.DEBUG) LOGGER.debug("[run][Do not need encrypted communication.]");
						responseMessage = AES128_DEFAULT.encryptText(Message.NO.toString());
						/* Send message that we do not need encrypted communication. */
						out.println(responseMessage);
					}
				} else if(Common.DEBUG) LOGGER.debug("[run][The incoming 'need encrypted " +
						"communication' message has a wrong ID.]");
			} else if(Common.DEBUG) LOGGER.debug("[run][Ups, there is no incoming message.]");
		} else if(Common.DEBUG) LOGGER.debug("[run][Ups, there is no incoming message.]");
		
		if(Common.DEBUG) LOGGER.debug("[run][Now we can wait for another requests.]");
		
		server.addClient(this);
		
		/* And now we are able to obtain incoming requests. */
		try {
			while((incomingMessage=in.readLine())!=null) {
				if(!incomingMessage.isEmpty()) {
					/* Let's decrypt incoming message. If need so. */
					if(settings.isEncryptedCommunication())
						incomingMessage = AES128_DEFAULT.decryptText(incomingMessage);
					
					if(incomingMessage!=null) {
						/* Let's parse incoming message. */
						SimpleMessage parsedMessage = parseIncommingMessage(incomingMessage);
						
						/* If user wants to disconnect. */
						if(parsedMessage.getId()==Message.BYE_BYE.getId()) {
							if(Common.DEBUG) LOGGER.debug("[run][User wants to disconnect. Bye bye.]");
							server.removeClient(this);
							break;
						}
						
						/* Here we need to process incoming parsed message and obtain response. */
						Object response = processIncomingMessage(parsedMessage);
						
						/* If response is not null, we encrypt it (if need so) and send it. */
						if(response!=null) {
							if(response instanceof String) {
								if(settings.isEncryptedCommunication())
									response = AES128_DEFAULT.encryptText(String.valueOf(response));
								out.println(response);
							}
						}
					}
				} else if(Common.DEBUG) LOGGER.debug("[run][Incoming message is empty. " +
						"Waiting for another...]");
			}
		} catch(IOException ioe) {
			if(Common.ERROR) LOGGER.error("[run][Can not obtain an incoming message.]", ioe);
		}
		
		if(Common.DEBUG) LOGGER.debug("[run][Finish.]");
	}
	
	/**
	 * Parsing incomming message from raw String to SimpleMessage..
	 * @param incomingMessage Raw String.
	 * @return Parsed SimpleMessage.
	 */
	private SimpleMessage parseIncommingMessage(String incomingMessage) {
		if(Common.DEBUG) LOGGER.debug("[parseIncommingMessage][" + incomingMessage + "]");
		
		int messageId = Integer.valueOf(
				incomingMessage.substring(0, incomingMessage.indexOf(
						SimpleMessage.SEPARATOR)));
		
		String addInfo = incomingMessage.substring(
				incomingMessage.indexOf(SimpleMessage.SEPARATOR)+1,
				incomingMessage.length());
		
		return new SimpleMessage(messageId, addInfo);
	}
	
	/**
	 * Process incoming message.
	 * @param message Parsed incoming message.
	 * @return Response to incoming message. Can be null if there is not answer.
	 */
	private Object processIncomingMessage(SimpleMessage message) {
		if(Common.DEBUG) LOGGER.debug("[processIncomingMessage]");
		
		if(message.getId()==Message.MOUSE_MOVE.getId()) {
			controller.mouseMove(message.getAddInfo());
			return null;
		} else if(message.getId()==Message.MOUSE_LEFT_CLICK.getId()) {
			controller.mouseLeftClick();
			return null;
		} else if(message.getId()==Message.MOUSE_RIGHT_CLICK.getId()) {
			controller.mouseRightClick();
			return null;
		} else if(message.getId()==Message.MOUSE_WHEEL.getId()) {
			controller.mouseWheel(message.getAddInfo());
			return null;
		} else if(message.getId()==Message.KEY_CLIPBOARD.getId()) {
			controller.keyClipboard(message.getAddInfo());
			return null;
		} else if(message.getId()==Message.KEY_STROKE.getId()) {
			controller.keyStroke(message.getAddInfo());
			return null;
		} else if(message.getId()==Message.SPECIAL_COMMAND.getId()) {
			controller.doSpecial(message.getAddInfo());
			return null;
		} else {
			if(Common.DEBUG) LOGGER.debug("[processIncomingMessage][Nothing to do - wrong " +
					"message ID.]");
			return null;
		}
	}
	
	/**
	 * This will close all necessary objects.
	 */
	private void closeConnection() {
		if(Common.DEBUG) LOGGER.debug("[closeConnection]");
		
		try {
			if(in!=null) in.close();
		} catch (IOException ioe) {
			if(Common.ERROR) LOGGER.error("[closeConnection][Can not close input reader.]", ioe);
		}
		
		if(out!=null) out.close();
		
		try {
			if(clientSocket!=null)
				if(clientSocket.isConnected())
					clientSocket.close();
		} catch (IOException ioe) {
			if(Common.ERROR) LOGGER.error("[closeConnection][Can not close client socket.]", ioe);
		}
	}
	
	/**
	 * This method is call when we need to close socket - stop
	 * waiting for incomming messages.
	 */
	public void forceDisconnect() {
		if(Common.DEBUG) LOGGER.debug("[forceDisconnect]");
		
		try {
			if(clientSocket!=null)
				if(clientSocket.isConnected())
					clientSocket.close();
		} catch (IOException ioe) {
			if(Common.ERROR) LOGGER.error("[closeConnection][Can not close client socket.]", ioe);
		}
	}
}
