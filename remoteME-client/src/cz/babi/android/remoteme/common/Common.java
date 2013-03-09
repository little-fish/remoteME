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

package cz.babi.android.remoteme.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import cz.babi.android.remoteme.crypto.AES128;

/**
 * Class for common use.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Common {
	
	private static final String TAG = Common.class.getName();
	
	/** Support link. */
	public static final String SUPPORT_LINK =
			"https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=" +
					"dev%2emisiarz%40gmail%2ecom&lc=CZ&item_name=Beer%20for%20a%20remoteME&" +
					"currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";
	
	public static final String CHARSET_UTF8 = "UTF-8";
	public static final String ALGORITHM_AES = "AES";
	public static final String ALGORITHM_SHA1 = "SHA-1";
	
	/** Default connection TCP port. */
	public static final long DEFAULT_CONNECTION_PORT = 4449;
	
	/** Default socket timeout. */
	public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
	
	/** Default mouse wheel smooth. */
	public static final int DEFAULT_MOUSE_WHEEL_SMOOTH = 40;
	
	/** Variable define secretID for encrypting and decrypting messages.
	 * <b>MAKE SHURE THAT THIS SECRET ID IS THE SAME LIKE IN SERVER APPLICATION</b> */
	public static final String SECRET_ID = "remoteME is really cool app";
	
	/** Variable define default password for encrypting and decrypting messages in scan mode.
	 * <b>MAKE SHURE THAT THIS SECRET ID IS THE SAME LIKE IN SERVER APPLICATION</b> */
	public static final String DEFAULT_PASSWORD = "default-password";
	
	/** Will be used for UDP scan mode and for first TCP comunication. */
	public static final AES128 AES128_DEFAULT = new AES128(Common.SECRET_ID,
			Common.DEFAULT_PASSWORD);
	
	/** Name of app folder in sd card. */
	public static final String APP_FOLDER = "remoteME";
	
	/** Name of icon folder inside app folder. */
	public static final String ICON_FOLDER = "ico";
	
	/** Special commands. */
	public static final String COMMAND_SHUTDOWN = "shutdown";
	public static final String COMMAND_RESTART = "restart";
	public static final String COMMAND_LOGOFF = "logoff";
	
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat DATEFORMAT_TIME =
	new SimpleDateFormat("HH:mm:ss");
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat DATEFORMAT_SERVER =
	new SimpleDateFormat("dd.MMMM yyyy, HH:mm");
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat DATEFORMAT_DATABASE =
	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static int displayWidth = -1;
	public static int displayHeight = -1;
	
	/* For programatically set logging on/off */
	public static int LOGLEVEL = 3;
	public static boolean ERROR = LOGLEVEL > 2;
	public static boolean WARN = LOGLEVEL > 1;
	public static boolean DEBUG = LOGLEVEL > 0;
	
	/** This variable is used for back space. */
	public static final String BACK_SPACE_KEY_STROKE = "backspace";
	
	/**
	 * Method which convert <b>Date</b> to <b>String</b>.
	 * @param dateFormat SimpleDateFormat which we will convert to.
	 * @param inputDate Input date in Date type.
	 * @return Formatted input date in String type. May return empty String.
	 */
	public static String convertDateToString(SimpleDateFormat dateFormat, Date inputDate) {
		if(inputDate==null) return "";
		else return new StringBuilder(dateFormat.format(inputDate)).toString();
	}
	
	/**
	 * Method which convert <b>String</b> to <b>Date</b>.
	 * @param inputDate Input date in String type.
	 * @param dateFormat SimpleDateFormat which we will convert from.
	 * @return Date. May return null.
	 */
	public static Date convertStringToDate(String inputDate, SimpleDateFormat dateFormat) {
		Date date = null;
		
		if(inputDate!=null && inputDate.compareTo("")!=0) {
			try {
				date = dateFormat.parse(inputDate);
			} catch (ParseException pe) {
				Log.e(TAG, "An error occurred while trying convert String to Date.", pe);
			}
		}
		
		return date;
	}
	
	/**
	 * Method for parsing raw IP Address string.
	 * @param fullIpAddress Raw IP Address.
	 * @return Correct IP Address.
	 */
	public static String getProperIpAddress(String fullIpAddress) {
		/* IP address is stored in format: xxx.xxx.xxx.xxx for correct sorting. */
		String[] ipAddressArray = fullIpAddress.split("\\.");
		String part1 = String.valueOf(Integer.valueOf(ipAddressArray[0]));
		String part2 = String.valueOf(Integer.valueOf(ipAddressArray[1]));
		String part3 = String.valueOf(Integer.valueOf(ipAddressArray[2]));
		String part4 = String.valueOf(Integer.valueOf(ipAddressArray[3]));
		return part1 + "." + part2 + "." + part3 + "." + part4;
	}
	
	/**
	 * Method for parsing IP Address string.
	 * @param properIpAddress Raw IP Address.
	 * @return DB IP Address.
	 */
	public static String getDbIpAddress(String properIpAddress) {
		
		String[] ipAddressArray = properIpAddress.split("\\.");
		for(int i=0; i<ipAddressArray.length; i++) {
			if(ipAddressArray[i].length()!=3) {
				if(ipAddressArray[i].length()==2) {
					ipAddressArray[i] = "0" + ipAddressArray[i];
				} else {
					ipAddressArray[i] = "00" + ipAddressArray[i];
				}
			}
		}
		
		return ipAddressArray[0] + "." + ipAddressArray[1] + "." +
		ipAddressArray[2] + "." + ipAddressArray[3];
	}
	
	/**
	 * Check if device is connected to some network.
	 * @param context Context.
	 * @return True if device is connected, False if not.
	 */
	public static boolean checkConnection(Context context) {
		ConnectivityManager connManager = (ConnectivityManager)context.
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if(mWifi.isConnected()) return true;
		else return false;
	}
	
	/**
	 * Return display width in pixels.
	 * @param context Context.
	 * @return Display width in pixels.
	 */
	public static int getDisplayWidth(Context context) {
		if(displayWidth==-1) calculateDisplaySize(context);
		
		return displayWidth;
	}
	
	/**
	 * Return display height in pixels.
	 * @param context Context.
	 * @return Display height in pixels.
	 */
	public static int getDisplayHeight(Context context) {
		if(displayHeight==-1) calculateDisplaySize(context);
		
		return displayHeight;
	}
	
	/**
	 * Calculate displey size.
	 * @param context Context.
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private static void calculateDisplaySize(Context context) {
		WindowManager windowManager = (WindowManager)context.getSystemService(
				Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point screenSize = new Point();
		
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(screenSize);
			
			displayWidth = screenSize.x;
			displayHeight = screenSize.y;
		} else {
			displayWidth = display.getWidth();
			displayHeight = display.getHeight();
		}
	}
}
