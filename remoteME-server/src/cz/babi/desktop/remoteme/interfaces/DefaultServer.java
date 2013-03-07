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

package cz.babi.desktop.remoteme.interfaces;

/**
 * Interface that define default methods for working with servers.
 * 
 * @author babi
 * @author dev.misiarz@gmail.cm
 */
public interface DefaultServer {
	
	/**
	 * Method for start server.
	 */
	void startServer();
	
	/**
	 * Here we are waiting for connection.
	 */
	void waitForConnection();
	
	/**
	 * Method for stop server.
	 */
	void stopServer();
}
