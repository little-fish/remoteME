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

import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * Class with static references to frequently used icons.
 *
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class Icons {
	
	private static final String RES = "/cz/babi/desktop/remoteme/res/";
	private static final HashMap<String, ImageIcon> cache = new HashMap<>();
	
	public static final ImageIcon APP_ICON = get("remoteme-64.png");
	public static final ImageIcon PAYPAL_DONATE = get("paypal-donate-146.png");
	
	/**
	 * Return an ImageIcon of a requested name loaded from default resource directory.
	 * Icons are cached for faster access.
	 * 
	 * @return Icon
	 */
	public static ImageIcon get(String name) {
		ImageIcon icon = cache.get(name);
		if(icon==null) {
			icon = new ImageIcon(Icons.class.getResource(RES + name));
			cache.put(name, icon);
		}
		
		return icon;
	}
}
