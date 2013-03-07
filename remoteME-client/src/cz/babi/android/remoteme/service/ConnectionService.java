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

package cz.babi.android.remoteme.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.crypto.AES128;
import cz.babi.android.remoteme.entity.Message;
import cz.babi.android.remoteme.entity.Message.SimpleMessage;
import cz.babi.android.remoteme.entity.Server;
import cz.babi.android.remoteme.ui.ActivityDialogListOfRemoteControllers;

/**
 * This service is responsible for communication with server.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ConnectionService extends Service {
	
	private static final String TAG_CLASS_NAME = ConnectionService.class.getSimpleName();
	
	private static final int NOTIFICATION_ID = 0;
	
	public static Server server;
	
	public static Socket clientSocket;
	public static BufferedReader in;
	public static PrintWriter out;
	
	public static boolean needEncryptedCommunication;
	
	private static final AES128 AES128_DEFAULT = Common.AES128_DEFAULT;
	
	private final IBinder binder = new ConnectionBinder();
	
	private NotificationManager notificationManager;
	
	private SharedPreferences preferences;
	
	private WifiLock wifiLock;
	
	private boolean disconnectWithError = false;
	private boolean isNotificationVisible = false;
	
	/**
	 * Class for clients to access.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with
	 * IPC.
	 */
	public class ConnectionBinder extends Binder {
		public ConnectionService getService() {
			return ConnectionService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onBind]");
		return binder;
	}
	
	@Override
	public void onCreate() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		/* Lock Wi-Fi, if need so. */
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WiFiLockTag");
		if(preferences.getBoolean(getString(R.string.pref_name_keep_wifi_alive), true))
			lockWiFi();
		
		/* Display a notification about us starting. */
		if(preferences.getBoolean(getString(R.string.pref_name_show_notification), true)) {
			showNotification();
		}
	}
	
	@Override
	public void onDestroy() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onDestroy]");
		
		/* Cancel the persistent notification. */
		if(isNotificationVisible) notificationManager.cancel(NOTIFICATION_ID);
		
		/* If Wi-Fi is locked we need to unlock it. */
		if(wifiLock.isHeld()) unlockWifi();
		
		/* If there was no error we send 'bye bye' message to server and show toast to user. */
		if(!disconnectWithError) {
			/* Just tell to server that we are disconnecting. */
			SimpleMessage byeBye = Message.BYE_BYE;
			
			String message = byeBye.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(
					byeBye.toString());
			
			out.println(message);
			out.flush();
			
			/* Tell the user we stopped. */
			LayoutInflater mInflater = (LayoutInflater)this.getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			View toastLayout = mInflater.inflate(R.layout.toast_normal, null);
			
			TextView text = (TextView)toastLayout.findViewById(R.id.normal_text);
			text.setText(R.string.connection_service_disconnected_text);
			
			Toast toast = new Toast(this);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(toastLayout);
			toast.show();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onStartCommand]Received start id " + startId + ": " + intent);
		
		/* We want this service to continue running until it is explicitly
		 stopped, so return sticky. */
		return START_STICKY;
	}
	
	/**
	 * @return the disconnectWithError
	 */
	public boolean isDisconnectWithError() {
		return disconnectWithError;
	}
	
	/**
	 * Lock Wi-Fi
	 */
	private void lockWiFi() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[lockWiFi]");
		
		wifiLock.acquire();
	}
	
	/**
	 * Unlock Wi-Fi
	 */
	private void unlockWifi() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[unlockWifi]");
		
		wifiLock.release();
	}
	
	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[showNotification]");
		
		/* Set notification ifon. */
		int iconResource = R.drawable.os_tux;
		if(server.getOsName().toLowerCase().indexOf("mac")!=-1)
			iconResource = R.drawable.os_mac;
		else if(server.getOsName().toLowerCase().indexOf("win")!=-1)
			iconResource = R.drawable.os_win;
		
		Bitmap icon = BitmapFactory.decodeResource(getResources(), iconResource);
		
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this).
				setSmallIcon(R.drawable.app_icon_status_bar).
				setLargeIcon(icon).
				setContentTitle(getText(R.string.text_connected)).
				setContentText(Common.getProperIpAddress(server.getIpAddress()) + ":" +
						server.getPort()).
						setTicker(getText(R.string.text_connected) + " " + getText(R.string.text_to) +
								" " + Common.getProperIpAddress(server.getIpAddress()) + ":" +
								server.getPort());
		
		Intent resultIntent = new Intent(this, ActivityDialogListOfRemoteControllers.class);
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 1,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		notificationBuilder.setContentIntent(resultPendingIntent);
		
		/* Prevent before closing by user. */
		notificationBuilder.setOngoing(true);
		
		notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
		
		isNotificationVisible = true;
	}
	
	/**
	 * If server is unreachable we need to stop service and notice that to user.
	 */
	private void closeConnection() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[stopService]");
		
		if(clientSocket!=null)
			try {
				clientSocket.close();
			} catch(IOException e) {
				if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[stopService][Can not close client " +
						"socket.]");
			}
		
		ConnectionService.server = null;
		ConnectionService.clientSocket = null;
		ConnectionService.in = null;
		ConnectionService.out = null;
		ConnectionService.needEncryptedCommunication = false;
		
		LayoutInflater mInflater = (LayoutInflater)this.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View toastLayout = mInflater.inflate(R.layout.toast_warning, null);
		
		TextView text = (TextView)toastLayout.findViewById(R.id.warning_text);
		text.setText(R.string.connection_service_connection_lost_text);
		
		Toast toast = new Toast(this);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(toastLayout);
		toast.show();
		
		disconnectWithError = true;
	}
	
	/**
	 * Check connection error.
	 * @return Is connection error?
	 */
	private boolean checkConnectionError() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[checkConnectionError]");
		
		if(out==null) return true;
		else return out.checkError();
	}
	
	/**
	 * Move mouse.
	 * @param offsetX Amount X.
	 * @param offsetY Amount Y.
	 * @return If command is done.
	 */
	public boolean moveMouse(final float offsetX, final float offsetY) {
		if(!checkConnectionError()) {
			SimpleMessage mouseMove = Message.MOUSE_MOVE;
			mouseMove.setAddInfo(String.valueOf(offsetX) + SimpleMessage.SEPARATOR +
					String.valueOf(offsetY));
			
			String message = mouseMove.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(
					mouseMove.toString());
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[moveMouse][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
	
	/**
	 * Mouse wheel.
	 * @param wheelAmount Wheel amount.
	 * @return If command is done.
	 */
	public boolean mouseWheel(float wheelAmount) {
		if(!checkConnectionError()) {
			SimpleMessage mouseWheel = Message.MOUSE_WHEEL;
			mouseWheel.setAddInfo(String.valueOf(wheelAmount));
			
			String message = mouseWheel.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(
					mouseWheel.toString());
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[mouseWheel][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
	
	/**
	 * Left mouse click.
	 * @param button Clicked button.
	 * @return If command is done.
	 */
	public boolean mouseLeftClick() {
		if(!checkConnectionError()) {
			SimpleMessage mouseLeftClick = Message.MOUSE_LEFT_CLICK;
			
			String message = mouseLeftClick.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(
					mouseLeftClick.toString());
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[mouseLeftClick][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
	
	/**
	 * Right mouse click.
	 * @param button Clicked button.
	 * @return If command is done.
	 */
	public boolean mouseRightClick() {
		if(!out.checkError()) {
			SimpleMessage mouseRightClick = Message.MOUSE_RIGHT_CLICK;
			
			String message = mouseRightClick.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(
					mouseRightClick.toString());
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[mouseRightClick][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
	
	/**
	 * Simulate key stroke.
	 * @param character Character.
	 * @return If command is done.
	 */
	public boolean keyStroke(String character) {
		if(!out.checkError()) {
			SimpleMessage keyStroke = Message.KEY_STROKE;
			keyStroke.setAddInfo(character);
			
			String message = keyStroke.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(message);
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[keyStroke][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
	
	/**
	 * Simulate clipboard.
	 * @param character Character.
	 * @return If command is done.
	 */
	public boolean keyClipboard(String character) {
		if(!out.checkError()) {
			SimpleMessage keyClipboard = Message.KEY_CLIPBOARD;
			keyClipboard.setAddInfo(character);
			
			String message = keyClipboard.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(message);
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[keyClipboard][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
	
	/**
	 * Do special command. Like shutdown, restart or logoff.
	 * @param action Special command.
	 * @return If command is done.
	 */
	public boolean doSpecial(String action) {
		if(!out.checkError()) {
			SimpleMessage doSpecial = Message.SPECIAL_COMMAND;
			doSpecial.setAddInfo(action);
			
			String message = doSpecial.toString();
			if(needEncryptedCommunication) message = AES128_DEFAULT.encryptText(message);
			
			out.println(message);
			out.flush();
			
			return true;
		} else {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doSpecial][Server is disconnected.]");
			closeConnection();
			return false;
		}
	}
}
