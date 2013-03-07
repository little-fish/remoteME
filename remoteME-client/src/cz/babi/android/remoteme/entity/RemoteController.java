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

package cz.babi.android.remoteme.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class represents remote controller.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class RemoteController implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String REMOTE_CONTROLLER = "remoteController";
	
	public static final String RC_ID = "rcId";
	public static final String RC_TITLE = "rcTitle";
	public static final String RC_AUTHOR = "rcAuthor";
	public static final String RC_ICON = "rcIcon";
	public static final String RC_BACKGROUND_COLOR = "rcBackgroundColor";
	
	public static final String ROW = "row";
	public static final String BUTTON = "button";
	
	public static final String B_BACKGROUND_COLOR = "bBackgroundColor";
	public static final String B_ICON = "bIcon";
	public static final String B_ICON_COLOR = "bIconColor";
	public static final String B_TEXT_FIRST = "bTextFirst";
	public static final String B_TEXT_FIRST_COLOR = "bTextFirstColor";
	public static final String B_TEXT_FIRST_SIZE = "bTextFirstSize";
	public static final String B_TEXT_SECOND = "bTextSecond";
	public static final String B_TEXT_SECOND_COLOR = "bTextSecondColor";
	public static final String B_TEXT_SECOND_SIZE = "bTextSecondSize";
	public static final String B_ACTION = "bAction";
	
	private String id;
	private String title;
	private String author;
	private String icon;
	private String backgroundColor;
	private ArrayList<Row> rows;
	
	/**
	 * Constructor.
	 * @param title Title to set.
	 * @param author Author to set.
	 * @param icon Icon string to set.
	 */
	public RemoteController(String id, String title, String author, String icon) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.icon = icon;
	}
	
	/**
	 * Constructor.
	 */
	public RemoteController() {}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}
	
	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	/**
	 * @return the backgroundColor
	 */
	public String getBackgroundColor() {
		return backgroundColor;
	}
	
	/**
	 * @param backgroundColor the backgroundColor to set
	 */
	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	/**
	 * @return the rows
	 */
	public ArrayList<Row> getRows() {
		return rows;
	}
	
	/**
	 * @param rows the rows to set
	 */
	public void setRows(ArrayList<Row> rows) {
		this.rows = rows;
	}
	
	/**
	 * Class holds row in remote controller.
	 * 
	 * @author Martin Misiarz
	 * @author dev.misiarz@gmail.com
	 */
	public class Row implements Serializable {
		
		private static final long serialVersionUID = 2L;
		
		private ArrayList<Button> buttons;
		
		/**
		 * @return the buttons
		 */
		public ArrayList<Button> getButtons() {
			return buttons;
		}
		
		/**
		 * @param buttons the buttons to set
		 */
		public void setButtons(ArrayList<Button> buttons) {
			this.buttons = buttons;
		}
		
		/**
		 * Class represents single button.
		 * 
		 * @author Martin Misiarz
		 * @author dev.misiarz@gmail.com
		 */
		public class Button implements Serializable {
			
			private static final long serialVersionUID = 3L;
			
			private String backgroundColor;
			private String icon;
			private String iconColor;
			private String textFirst;
			private String textFirstColor;
			private int textFirstSize;
			private String textSecond;
			private String textSecondColor;
			private int textSecondSize;
			private String action;
			/**
			 * @return the backgroundColor
			 */
			public String getBackgroundColor() {
				return backgroundColor;
			}
			/**
			 * @param backgroundColor the backgroundColor to set
			 */
			public void setBackgroundColor(String backgroundColor) {
				this.backgroundColor = backgroundColor;
			}
			/**
			 * @return the icon
			 */
			public String getIcon() {
				return icon;
			}
			/**
			 * @param icon the icon to set
			 */
			public void setIcon(String icon) {
				this.icon = icon;
			}
			/**
			 * @return the iconColor
			 */
			public String getIconColor() {
				return iconColor;
			}
			/**
			 * @param iconColor the iconColor to set
			 */
			public void setIconColor(String iconColor) {
				this.iconColor = iconColor;
			}
			/**
			 * @return the textFirst
			 */
			public String getTextFirst() {
				return textFirst;
			}
			/**
			 * @param textFirst the textFirst to set
			 */
			public void setTextFirst(String textFirst) {
				this.textFirst = textFirst;
			}
			/**
			 * @return the textFirstColor
			 */
			public String getTextFirstColor() {
				return textFirstColor;
			}
			/**
			 * @param textFirstColor the textFirstColor to set
			 */
			public void setTextFirstColor(String textFirstColor) {
				this.textFirstColor = textFirstColor;
			}
			/**
			 * @return the textFirstSize
			 */
			public int getTextFirstSize() {
				return textFirstSize;
			}
			/**
			 * @param textFirstSize the textFirstSize to set
			 */
			public void setTextFirstSize(int textFirstSize) {
				this.textFirstSize = textFirstSize;
			}
			/**
			 * @return the textSecond
			 */
			public String getTextSecond() {
				return textSecond;
			}
			/**
			 * @param textSecond the textSecond to set
			 */
			public void setTextSecond(String textSecond) {
				this.textSecond = textSecond;
			}
			/**
			 * @return the textSecondColor
			 */
			public String getTextSecondColor() {
				return textSecondColor;
			}
			/**
			 * @param textSecondColor the textSecondColor to set
			 */
			public void setTextSecondColor(String textSecondColor) {
				this.textSecondColor = textSecondColor;
			}
			/**
			 * @return the textSecondSize
			 */
			public int getTextSecondSize() {
				return textSecondSize;
			}
			/**
			 * @param textSecondSize the textSecondSize to set
			 */
			public void setTextSecondSize(int textSecondSize) {
				this.textSecondSize = textSecondSize;
			}
			/**
			 * @return the action
			 */
			public String getAction() {
				return action;
			}
			/**
			 * @param action the action to set
			 */
			public void setAction(String action) {
				this.action = action;
			}
			
		}
	}
}
