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

package cz.babi.android.remoteme.data;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.Server;

/**
 * This class will provide necessary operations with database.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
	
	private static final String TAG_CLASS_NAME = MySQLiteOpenHelper.class.getName();
	
	/** Instance of DatabaseHelper class. */
	private static MySQLiteOpenHelper instance;
	/** Database name. */
	private static final String DATABASE_NAME = "remoteme-db.sqlite";
	/** Database version */
	private static final int DATABASE_VERSION = 1;
	
	private static final String TABLE_SERVER = "server";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_IP_ADDRESS = "ip_address";
	public static final String COLUMN_MAC_ADDRESS = "mac_address";
	public static final String COLUMN_PORT = "port";
	public static final String COLUMN_SERVER_NAME = "server_name";
	public static final String COLUMN_PASSWORD = "password";
	public static final String COLUMN_OS_NAME = "os_name";
	public static final String COLUMN_ADDED = "added";
	public static final String COLUMN_LAST_CONNECTED = "last_connectd";
	
	private static final String DATABASE_CREATE = "create table " +
			TABLE_SERVER + "(" +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_SERVER_NAME + " text, " +
			COLUMN_IP_ADDRESS + " text not null, " +
			COLUMN_MAC_ADDRESS + " text, " +
			COLUMN_PORT + " integer not null, " +
			COLUMN_PASSWORD + " text, " +
			COLUMN_ADDED + " text not null, " +
			COLUMN_LAST_CONNECTED + " text, " +
			COLUMN_OS_NAME + " text" +
			");";
	
	/**
	 * Private constructor.
	 * @param context Application context
	 */
	private MySQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[MySQLiteOpenHelper][Private constructor.]");
	}
	
	/**
	 * Method to get instance of DatabaseHelper class.
	 * There is used a singleton.
	 * @param context Application context.
	 * @return Instance of DatabaseHelper class.
	 */
	public static synchronized MySQLiteOpenHelper getInstance(Context context) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getInstance]");
		if(instance==null) instance = new MySQLiteOpenHelper(context);
		return instance;
	}
	
	/**
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(
	 * android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate][Database was created.]");
		database.execSQL(DATABASE_CREATE);
	}
	
	/**
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(
	 * android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onUpgrade][Upgrading database from version '"
				+ oldVersion + "' to '"	+ newVersion + "', which will destroy all old data.]");
		database.execSQL("drop table if exists " + TABLE_SERVER);
		onCreate(database);
	}
	
	/**
	 * Method for get Server instance from database cursor.
	 * @param cursor Database cursor pointing to specific raw.
	 * @return Server instance.
	 */
	private Server getServerFromCursor(Cursor cursor) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getServerFromCursor]");
		long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
		String ipAddress = cursor.getString(cursor.getColumnIndex(COLUMN_IP_ADDRESS));
		String macAddress = cursor.getString(cursor.getColumnIndex(COLUMN_MAC_ADDRESS));
		long port = cursor.getLong(cursor.getColumnIndex(COLUMN_PORT));
		String password = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
		String serverName = cursor.getString(cursor.getColumnIndex(COLUMN_SERVER_NAME));
		String osName = cursor.getString(cursor.getColumnIndex(COLUMN_OS_NAME));
		Date added = Common.convertStringToDate(cursor.getString(
				cursor.getColumnIndex(COLUMN_ADDED)),
				Common.DATEFORMAT_DATABASE);
		Date lastConnected = Common.convertStringToDate(
				cursor.getString(cursor.getColumnIndex(COLUMN_LAST_CONNECTED)),
				Common.DATEFORMAT_DATABASE);
		
		return new Server(id, ipAddress, macAddress, port, password, serverName,
				osName, added, lastConnected);
	}
	
	/**
	 * Method for get new ContentValues from server.
	 * @param server Server.
	 * @return ContentValues filled from input server.
	 */
	private ContentValues getContentValueFromServer(Server server) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getContentValueFromServer]");
		ContentValues ret = new ContentValues();
		ret.put(COLUMN_IP_ADDRESS, server.getIpAddress());
		ret.put(COLUMN_MAC_ADDRESS, server.getMacAddress());
		ret.put(COLUMN_PORT, server.getPort());
		ret.put(COLUMN_SERVER_NAME, server.getServerName());
		ret.put(COLUMN_PASSWORD, server.getPassword());
		ret.put(COLUMN_OS_NAME, server.getOsName());
		ret.put(COLUMN_ADDED, Common.convertDateToString(
				Common.DATEFORMAT_DATABASE, server.getAdded()));
		ret.put(COLUMN_LAST_CONNECTED, Common.convertDateToString(
				Common.DATEFORMAT_DATABASE, server.getLastConnected()));
		
		return ret;
	}
	
	/**
	 * Method add new Server into database.
	 * @param server New server.
	 * @return ID of inserted record.
	 */
	public long addServer(Server server) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[addServer][Init.]");
		/* Need to get writable database. */
		SQLiteDatabase database = this.getWritableDatabase();
		long idOfNewRecord = -1;
		
		/* Need to create content values from server. */
		ContentValues contentValues = getContentValueFromServer(server);
		
		/* Insert new server into the database. */
		idOfNewRecord = database.insert(TABLE_SERVER, null, contentValues);
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[addServer][New server was added: '"
				+ server.getIpAddress() + ":" +	server.getPort() + "'.]");
		
		/* Also we need to close connection to the database. */
		database.close();
		
		return idOfNewRecord;
	}
	
	/**
	 * Method for get server from database by its id.
	 * @param serverId Server id.
	 * @return Server from database.
	 */
	public Server getServer(int serverId) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getServer]");
		Server ret = null;
		/* Here we need only readable database. */
		SQLiteDatabase database = this.getReadableDatabase();
		
		/* Try to point cursor to specific raw in table. */
		Cursor cursor = database.query(TABLE_SERVER, null, COLUMN_ID + "=?",
				new String[] { String.valueOf(serverId) }, null, null, null);
		
		/* If cursor is not null we are able to get Server instance from it. */
		if(cursor!=null) {
			cursor.moveToFirst();
			
			ret = getServerFromCursor(cursor);
			
			cursor.close();
		}
		
		database.close();
		
		return ret;
	}
	
	/**
	 * Method for get all servers from database.
	 * @return All servers from database.
	 */
	public ArrayList<Server> getAllServers() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getAllServers]");
		ArrayList<Server> ret = new ArrayList<Server>();
		/* Here we need only readable database. */
		SQLiteDatabase database = this.getReadableDatabase();
		
		/* Here we try to load all raws from table. */
		Cursor cursor = database.query(TABLE_SERVER, null, null, null, null, null, null);
		
		/* If there are some raws we need to add them to list. */
		if(cursor!=null) {
			cursor.moveToFirst();
			
			while(!cursor.isAfterLast()) {
				ret.add(getServerFromCursor(cursor));
				cursor.moveToNext();
			}
			
			cursor.close();
		}
		
		database.close();
		
		return ret;
	}
	
	/**
	 * Method for get Cursor for all servers in database.
	 * @param orderBy Order by.
	 * @return Cursor with all server.
	 */
	public Cursor getCursorForAllServers(String orderBy) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getCursorForAllServers]");
		/* Here we need only readable database. */
		SQLiteDatabase database = this.getReadableDatabase();
		
		/* Here we try to load all raws from table. */
		return database.query(TABLE_SERVER, null, null, null, null, null, orderBy);
	}
	
	
	/**
	 * Method for delete server from database.
	 * @param serverIdDelete Server's ID to delete.
	 */
	public void deleteServer(int serverIdDelete) {
		/* Need to get writable database. */
		SQLiteDatabase database = this.getWritableDatabase();
		
		database.delete(TABLE_SERVER, COLUMN_ID + "=?",
				new String[] { String.valueOf(serverIdDelete) });
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[deleteServer][Server with ID='" + serverIdDelete
				+ "'was deleted.]");
		
		database.close();
	}
	
	/**
	 * Method for update server in database.
	 * @param serverToUpdate Server to update with new values.
	 */
	public void updateServer(Server serverToUpdate) {
		/* Need to get writable database. */
		SQLiteDatabase database = this.getWritableDatabase();
		
		/* Need to create content values from server. */
		ContentValues contentValues = getContentValueFromServer(serverToUpdate);
		
		database.update(TABLE_SERVER, contentValues, COLUMN_ID + "=?",
				new String[] {String.valueOf(serverToUpdate.getId())});
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[updateServer][Server was updated: '" +
				serverToUpdate.getIpAddress() + ":" + serverToUpdate.getPort() + "'.]");
		
		database.close();
	}
}
