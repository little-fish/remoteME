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

import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Class holds map of keys.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class MappedKeys {
	
	public static final String DIVIDER = ";-;";
	
	/** Used for key stroke. */
	public static HashMap<String, Integer[]> keyMap = getKeyMap();
	
	/**
	 * Get map of keys contains string character as key and array of integers as their codes.
	 * @return Map of keys.
	 */
	private static HashMap<String, Integer[]> getKeyMap() {
		if(keyMap==null) {
			keyMap = new HashMap<>();
			
			keyMap.put("a", new Integer[] { KeyEvent.VK_A });
			keyMap.put("b", new Integer[] { KeyEvent.VK_B });
			keyMap.put("c", new Integer[] { KeyEvent.VK_C });
			keyMap.put("d", new Integer[] { KeyEvent.VK_D });
			keyMap.put("e", new Integer[] { KeyEvent.VK_E });
			keyMap.put("f", new Integer[] { KeyEvent.VK_F });
			keyMap.put("g", new Integer[] { KeyEvent.VK_G });
			keyMap.put("h", new Integer[] { KeyEvent.VK_H });
			keyMap.put("i", new Integer[] { KeyEvent.VK_I });
			keyMap.put("j", new Integer[] { KeyEvent.VK_J });
			keyMap.put("k", new Integer[] { KeyEvent.VK_K });
			keyMap.put("l", new Integer[] { KeyEvent.VK_L });
			keyMap.put("m", new Integer[] { KeyEvent.VK_M });
			keyMap.put("n", new Integer[] { KeyEvent.VK_N });
			keyMap.put("o", new Integer[] { KeyEvent.VK_O });
			keyMap.put("p", new Integer[] { KeyEvent.VK_P });
			keyMap.put("q", new Integer[] { KeyEvent.VK_Q });
			keyMap.put("r", new Integer[] { KeyEvent.VK_R });
			keyMap.put("s", new Integer[] { KeyEvent.VK_S });
			keyMap.put("t", new Integer[] { KeyEvent.VK_T });
			keyMap.put("u", new Integer[] { KeyEvent.VK_U });
			keyMap.put("v", new Integer[] { KeyEvent.VK_V });
			keyMap.put("w", new Integer[] { KeyEvent.VK_W });
			keyMap.put("x", new Integer[] { KeyEvent.VK_X });
			keyMap.put("y", new Integer[] { KeyEvent.VK_Y });
			keyMap.put("z", new Integer[] { KeyEvent.VK_Z });
			
			keyMap.put("A", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_A });
			keyMap.put("B", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_B });
			keyMap.put("C", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_C });
			keyMap.put("D", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_D });
			keyMap.put("E", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_E });
			keyMap.put("F", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_F });
			keyMap.put("G", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_G });
			keyMap.put("H", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_H });
			keyMap.put("I", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_I });
			keyMap.put("J", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_J });
			keyMap.put("K", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_K });
			keyMap.put("L", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_L });
			keyMap.put("M", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_M });
			keyMap.put("N", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_N });
			keyMap.put("O", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_O });
			keyMap.put("P", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_P });
			keyMap.put("Q", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Q });
			keyMap.put("R", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_R });
			keyMap.put("S", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_S });
			keyMap.put("T", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_T });
			keyMap.put("U", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_U });
			keyMap.put("V", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_V });
			keyMap.put("W", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_W });
			keyMap.put("X", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_X });
			keyMap.put("Y", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Y });
			keyMap.put("Z", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_Z });
			
			keyMap.put("1", new Integer[] { KeyEvent.VK_1 });
			keyMap.put("2", new Integer[] { KeyEvent.VK_2 });
			keyMap.put("3", new Integer[] { KeyEvent.VK_3 });
			keyMap.put("4", new Integer[] { KeyEvent.VK_4 });
			keyMap.put("5", new Integer[] { KeyEvent.VK_5 });
			keyMap.put("6", new Integer[] { KeyEvent.VK_6 });
			keyMap.put("7", new Integer[] { KeyEvent.VK_7 });
			keyMap.put("8", new Integer[] { KeyEvent.VK_8 });
			keyMap.put("9", new Integer[] { KeyEvent.VK_9 });
			keyMap.put("0", new Integer[] { KeyEvent.VK_0 });
			
			keyMap.put("f1", new Integer[] { KeyEvent.VK_F1 });
			keyMap.put("f2", new Integer[] { KeyEvent.VK_F2 });
			keyMap.put("f3", new Integer[] { KeyEvent.VK_F3 });
			keyMap.put("f4", new Integer[] { KeyEvent.VK_F4 });
			keyMap.put("f5", new Integer[] { KeyEvent.VK_F5 });
			keyMap.put("f6", new Integer[] { KeyEvent.VK_F6 });
			keyMap.put("f7", new Integer[] { KeyEvent.VK_F7 });
			keyMap.put("f8", new Integer[] { KeyEvent.VK_F8 });
			keyMap.put("f9", new Integer[] { KeyEvent.VK_F9 });
			keyMap.put("f10", new Integer[] { KeyEvent.VK_F10 });
			keyMap.put("f11", new Integer[] { KeyEvent.VK_F11 });
			keyMap.put("f12", new Integer[] { KeyEvent.VK_F12 });
			keyMap.put("f13", new Integer[] { KeyEvent.VK_F13 });
			keyMap.put("f14", new Integer[] { KeyEvent.VK_F14 });
			keyMap.put("f15", new Integer[] { KeyEvent.VK_F15 });
			keyMap.put("f16", new Integer[] { KeyEvent.VK_F16 });
			keyMap.put("f17", new Integer[] { KeyEvent.VK_F17 });
			keyMap.put("f18", new Integer[] { KeyEvent.VK_F18 });
			keyMap.put("f19", new Integer[] { KeyEvent.VK_F19 });
			keyMap.put("f20", new Integer[] { KeyEvent.VK_F20 });
			keyMap.put("f21", new Integer[] { KeyEvent.VK_F21 });
			
			keyMap.put("enter", new Integer[] { KeyEvent.VK_ENTER });
			keyMap.put("esc", new Integer[] { KeyEvent.VK_ESCAPE });
			keyMap.put("shift", new Integer[] { KeyEvent.VK_SHIFT });
			keyMap.put("alt", new Integer[] { KeyEvent.VK_ALT });
			keyMap.put("ctrl", new Integer[] { KeyEvent.VK_CONTROL });
			keyMap.put("up", new Integer[] { KeyEvent.VK_UP });
			keyMap.put("down", new Integer[] { KeyEvent.VK_DOWN });
			keyMap.put("left", new Integer[] { KeyEvent.VK_LEFT });
			keyMap.put("right", new Integer[] { KeyEvent.VK_RIGHT });
			keyMap.put("space", new Integer[] { KeyEvent.VK_SPACE });
			keyMap.put("backspace", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SPACE });
			keyMap.put("windows", new Integer[] { KeyEvent.VK_WINDOWS });
			keyMap.put("tab", new Integer[] { KeyEvent.VK_TAB });
			keyMap.put("pageup", new Integer[] { KeyEvent.VK_PAGE_UP });
			keyMap.put("end", new Integer[] { KeyEvent.VK_END });
			keyMap.put("insert", new Integer[] { KeyEvent.VK_INSERT });
			keyMap.put("home", new Integer[] { KeyEvent.VK_HOME });
			keyMap.put("pagedown", new Integer[] { KeyEvent.VK_PAGE_DOWN });
			keyMap.put("less", new Integer[] { KeyEvent.VK_LESS });
			keyMap.put("greather", new Integer[] { KeyEvent.VK_GREATER });
			
			keyMap.put("`", new Integer[] { KeyEvent.VK_BACK_QUOTE });
			keyMap.put("-", new Integer[] { KeyEvent.VK_MINUS });
			keyMap.put("=", new Integer[] { KeyEvent.VK_EQUALS });
			keyMap.put("~", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE});
			keyMap.put("!", new Integer[] { KeyEvent.VK_EXCLAMATION_MARK });
			keyMap.put("@", new Integer[] { KeyEvent.VK_AT });
			keyMap.put("#", new Integer[] { KeyEvent.VK_NUMBER_SIGN });
			keyMap.put("$", new Integer[] { KeyEvent.VK_DOLLAR });
			keyMap.put("%", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_5 });
			keyMap.put("^", new Integer[] { KeyEvent.VK_CIRCUMFLEX });
			keyMap.put("&", new Integer[] { KeyEvent.VK_AMPERSAND });
			keyMap.put("*", new Integer[] { KeyEvent.VK_ASTERISK });
			keyMap.put("(", new Integer[] { KeyEvent.VK_LEFT_PARENTHESIS });
			keyMap.put(")", new Integer[] { KeyEvent.VK_RIGHT_PARENTHESIS });
			keyMap.put("_", new Integer[] { KeyEvent.VK_UNDERSCORE });
			keyMap.put("[", new Integer[] { KeyEvent.VK_OPEN_BRACKET });
			keyMap.put("]", new Integer[] { KeyEvent.VK_CLOSE_BRACKET });
			keyMap.put("\\", new Integer[] { KeyEvent.VK_BACK_SLASH });
			keyMap.put("{", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET });
			keyMap.put("}", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET });
			keyMap.put("|", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH });
			keyMap.put(";", new Integer[] { KeyEvent.VK_SEMICOLON });
			keyMap.put(":", new Integer[] { KeyEvent.VK_COLON });
			keyMap.put("\\", new Integer[] { KeyEvent.VK_QUOTE });
			keyMap.put("\"", new Integer[] { KeyEvent.VK_QUOTEDBL });
			keyMap.put(",", new Integer[] { KeyEvent.VK_COMMA });
			keyMap.put("<", new Integer[] { KeyEvent.VK_LESS });
			keyMap.put(".", new Integer[] { KeyEvent.VK_PERIOD });
			keyMap.put(">", new Integer[] { KeyEvent.VK_GREATER });
			keyMap.put("/", new Integer[] { KeyEvent.VK_SLASH });
			keyMap.put("?", new Integer[] { KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH });
			keyMap.put(" ", new Integer[] { KeyEvent.VK_SPACE });
			
			/*
			 * There is some bug with '+'
			 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6942481
			 */
			keyMap.put("+", new Integer[] { KeyEvent.VK_ADD });
		}
		
		return keyMap;
	}
}
