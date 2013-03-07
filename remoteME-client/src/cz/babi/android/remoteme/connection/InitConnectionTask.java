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

package cz.babi.android.remoteme.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import cz.babi.android.remoteme.ActivityMain;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.crypto.AES128;
import cz.babi.android.remoteme.data.MySQLiteOpenHelper;
import cz.babi.android.remoteme.entity.Message;
import cz.babi.android.remoteme.entity.Message.SimpleMessage;
import cz.babi.android.remoteme.service.ConnectionService;
import cz.babi.android.remoteme.ui.ActivityDialogListOfRemoteControllers;
import cz.babi.android.remoteme.ui.ActivityDialogServerConnect.ConnectionFragment;

/**
 * This task is used for init connection with server.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class InitConnectionTask extends AsyncTask<Void, Integer, Void> {
	
	private static final String TAG_CLASS_NAME = InitConnectionTask.class.getSimpleName();
	
	private final SharedPreferences preferences;
	
	private int socketTimeout;
	
	private static final AES128 AES128_DEFAULT = Common.AES128_DEFAULT;
	
	private ConnectionFragment parentFragment = null;
	
	private String remoteHost;
	private String remotePassword;
	private int remotePort;
	
	private Socket clientSocket;
	private BufferedReader in;
	private PrintWriter out;
	
	private boolean needEncryptedCommunication;
	
	private boolean canceledByUser = false;
	
	/**
	 * Constructor.
	 * @param parentFragmentActivity Parent activity. Needs for access to its methods.
	 */
	public InitConnectionTask(ConnectionFragment parentFragmentActivity) {
		
		remoteHost = Common.getProperIpAddress(ConnectionService.server.getIpAddress());
		remotePassword = ConnectionService.server.getPassword();
		remotePort = (int)ConnectionService.server.getPort();
		
		this.parentFragment = parentFragmentActivity;
		this.clientSocket = new Socket();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(
				parentFragment.getActivity());
		
		socketTimeout = preferences.getInt(
				this.parentFragment.getResources().getString(R.string.pref_name_socket_timeout),
				5000);
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground]");
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][remoteHost][" + remoteHost + "]");
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][remotePassword][" + remotePassword + "]");
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][remotePort][" + remotePort + "]");
		
		publishProgress(ConnectionFragment.PREPARE_INIT_UI);
		
		/* There are two attempts to connect to the server. */
		int attempt = 1;
		while(attempt<3) {
			try {
				clientSocket.connect(new InetSocketAddress(remoteHost, remotePort), socketTimeout/2);
				break;
			} catch(IOException ioe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not connect to host.]" +
						"[Attempt: " + attempt + ".]");
				attempt++;
				if(attempt==3) clientSocket = null;
			}
		}
		
		/* If user canceled connecting task */
		if(canceledByUser) return null;
		
		if(clientSocket!=null) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Connected to server.]");
			
			try {
				clientSocket.setTcpNoDelay(true);
			} catch(SocketException e) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not set TCP no " +
						"delay to socket.]");
			}
			
			try {
				in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			} catch(IOException ioe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not create input " +
						"reader from client socket.]");
				stopTask();
				return null;
			}
			
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch(IOException ioe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not create output " +
						"writer from client socket.]");
				stopTask();
				return null;
			}
			
			publishProgress(ConnectionFragment.UPDATE_INIT_UI_TRUE);
			publishProgress(ConnectionFragment.PREPARE_AUTH_UI);
			
			/* Prepare and send simple 'do i need password' message to the server. */
			SimpleMessage needPasswordMessage = Message.DO_I_NEED_PASSWORD;
			String encryptedPasswordMessage = AES128_DEFAULT.encryptText(
					needPasswordMessage.toString());
			
			out.print(encryptedPasswordMessage);
			out.flush();
			
			String rawAnswer = null;
			try {
				rawAnswer = in.readLine();
			} catch(IOException e) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not read " +
						"from input stream.]");
			}
			
			if(rawAnswer!=null) {
				String decryptedAnswer = AES128_DEFAULT.decryptText(rawAnswer);
				SimpleMessage simpleAnswer = parseIncommingMessage(decryptedAnswer);
				
				if(simpleAnswer.getId()==Message.YES.getId()) {
					/* Prepare and send simple "password check" message to the server. */
					SimpleMessage passwordMessage = Message.CHECK_PASSWORD;
					passwordMessage.setAddInfo(remotePassword);
					
					encryptedPasswordMessage = AES128_DEFAULT.encryptText(
							passwordMessage.toString());
					
					out.print(encryptedPasswordMessage);
					out.flush();
					
					rawAnswer = null;
					try {
						rawAnswer = in.readLine();
					} catch(IOException e) {
						if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not read " +
								"from input stream.]");
					}
					
					if(rawAnswer!=null) {
						decryptedAnswer = AES128_DEFAULT.decryptText(rawAnswer);
						simpleAnswer = parseIncommingMessage(decryptedAnswer);
						
						if(simpleAnswer.getId()==Message.YES.getId()) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Password " +
									"match. Let's roll.]");
							
							publishProgress(ConnectionFragment.UPDATE_AUTH_UI_TRUE);
							publishProgress(ConnectionFragment.TASK_ENDS);
						} else if(simpleAnswer.getId()==Message.NO.getId()) {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Password " +
									"does not match ,(]");
							
							publishProgress(ConnectionFragment.UPDATE_AUTH_UI_FALSE);
							publishProgress(ConnectionFragment.ERROR_AUTH);
							return null;
						} else {
							if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Looks " +
									"like we catched a wrong response.]");
						}
					} else {
						if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][There is no " +
								"response.]");
					}
				} else if(simpleAnswer.getId()==Message.NO.getId()) {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][We do not need " +
							"password for comunicate with server.]");
					
					publishProgress(ConnectionFragment.UPDATE_AUTH_UI_TRUE);
					publishProgress(ConnectionFragment.TASK_ENDS);
				} else {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Response has wrong" +
							" ID.]");
					publishProgress(ConnectionFragment.UPDATE_INIT_UI_FALSE);
					publishProgress(ConnectionFragment.ERROR_INIT);
					return null;
				}
				
				/* Prepare and send simple 'need encryption communication' message to the server. */
				SimpleMessage needEncryptedComMessage = Message.NEED_ENCRYPTED_COMMUNICATION;
				String encryptedNeedEncryptedCom = AES128_DEFAULT.encryptText(
						needEncryptedComMessage.toString());
				
				out.print(encryptedNeedEncryptedCom);
				out.flush();
				
				rawAnswer = null;
				try {
					rawAnswer = in.readLine();
				} catch(IOException e) {
					if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[doInBackground][Can not read " +
							"from input stream.]");
				}
				
				/* Now we need to check response to our request about encrypted communication. */
				if(rawAnswer!=null) {
					decryptedAnswer = AES128_DEFAULT.decryptText(rawAnswer);
					simpleAnswer = parseIncommingMessage(decryptedAnswer);
					
					/* If we need an encrypted communication we need to check if our password match. */
					if(simpleAnswer.getId()==Message.YES.getId()) {
						needEncryptedCommunication = true;
						if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][We need " +
								"encrypted communication.]");
					} else if(simpleAnswer.getId()==Message.NO.getId()) {
						needEncryptedCommunication = false;
						if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][We do not need " +
								"encrypted communication.]");
					}
				}
			} else {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Can not connect to server.]");
				publishProgress(ConnectionFragment.UPDATE_INIT_UI_FALSE);
				publishProgress(ConnectionFragment.ERROR_INIT);
			}
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doInBackground][Can not connect to server.]");
			publishProgress(ConnectionFragment.UPDATE_INIT_UI_FALSE);
			publishProgress(ConnectionFragment.ERROR_INIT);
		}
		
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onProgressUpdate]");
		super.onProgressUpdate(values);
		
		int status =  values[0];
		
		parentFragment.setTaskStatus(status);
		
		parentFragment.updateUI();
	}
	
	@Override
	protected void onPostExecute(Void result) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onPostExecute]");
		
		/* If we are connected to server. */
		if(parentFragment.getTaskStatus()==ConnectionFragment.TASK_ENDS) {
			
			/* Update server in database. */
			ConnectionService.server.setLastConnected(new Date());
			MySQLiteOpenHelper.getInstance(parentFragment.getActivity()).
			updateServer(ConnectionService.server);
			ActivityMain.needRefreshServers = true;
			
			/* Here we need to set client and remote objet for comunication from service. */
			ConnectionService.clientSocket = clientSocket;
			ConnectionService.in = in;
			ConnectionService.out = out;
			ConnectionService.needEncryptedCommunication = needEncryptedCommunication;
			
			/* Start service. */
			Intent connectionServiceIntent = new Intent(parentFragment.getActivity(),
					ConnectionService.class);
			parentFragment.getActivity().startService(connectionServiceIntent);
			
			/* Show dialog when user can choose remote controller. */
			Intent chooseRemoteController = new Intent(parentFragment.getActivity(),
					ActivityDialogListOfRemoteControllers.class);
			
			parentFragment.getActivity().startActivity(chooseRemoteController);
			
			parentFragment.getActivity().finish();
		}
	}
	
	@Override
	protected void onPreExecute() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onPreExecute]");
		if(parentFragment!=null) {
			parentFragment.setTaskStatus(ConnectionFragment.TASK_NOT_RUNNING);
			parentFragment.updateUI();
		}
	}
	
	/**
	 * Parsing incomming message from raw String to SimpleMessage..
	 * @param incomingMessage Raw String.
	 * @return Parsed SimpleMessage.
	 */
	private SimpleMessage parseIncommingMessage(String incomingMessage) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[parseIncommingMessage]");
		
		int messageId = Integer.valueOf(
				incomingMessage.substring(0, incomingMessage.indexOf(
						SimpleMessage.SEPARATOR)));
		
		String addInfo = incomingMessage.substring(
				incomingMessage.indexOf(SimpleMessage.SEPARATOR),
				incomingMessage.length());
		
		return new SimpleMessage(messageId, addInfo);
	}
	
	/**
	 * Close client socket.
	 */
	public void stopTask() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[stopTask]");
		
		try {
			if(in!=null) in.close();
		} catch (IOException ioe) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[stopTask][Can not close input reader.]");
		}
		
		if(out!=null) out.close();
		
		if(clientSocket!=null) {
			try {
				clientSocket.close();
			} catch(IOException ioe) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[stopTask][Can not close socket.]");
			}
		}
	}
}
