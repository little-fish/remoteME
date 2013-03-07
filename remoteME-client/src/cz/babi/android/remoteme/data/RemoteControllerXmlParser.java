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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.RemoteController;
import cz.babi.android.remoteme.entity.RemoteController.Row;
import cz.babi.android.remoteme.entity.RemoteController.Row.Button;

/**
 * XML parser.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class RemoteControllerXmlParser {
	
	private static final String TAG_CLASS_NAME =
			RemoteControllerXmlParser.class.getSimpleName();
	
	/* Name space. Do not need it. */
	private static final String NS = null;
	
	/**
	 * This is the only one method need to be called from outside.
	 * @param reader Reader.
	 * @return Instance of RemoteController.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public RemoteController parse(Reader reader) throws XmlPullParserException, IOException {
		/* Init xml parser. */
		XmlPullParser parser = Xml.newPullParser();
		
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(reader);
			parser.nextTag();
		} finally {
			
		}
		
		return readRemoteController(parser);
	}
	
	/**
	 * Read whole remote controller.
	 * @param parser Parser.
	 * @return Remote Controller.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private RemoteController readRemoteController(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRemoteController]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.REMOTE_CONTROLLER);
		
		RemoteController remoteController = new RemoteController();
		
		String id = "";
		String title = "";
		String author = "";
		String icon = "";
		String backgroundColor = "";
		ArrayList<Row> rows = new ArrayList<Row>();
		
		while(parser.next()!=XmlPullParser.END_TAG) {
			if(parser.getEventType()!=XmlPullParser.START_TAG) {
				continue;
			}
			
			String tagName = parser.getName();
			
			if(tagName.equals(RemoteController.RC_ID)) {
				id = readRCId(parser);
			} else if(tagName.equals(RemoteController.RC_TITLE)) {
				title = readRCTitle(parser);
			} else if(tagName.equals(RemoteController.RC_AUTHOR)) {
				author = readRCAuthor(parser);
			} else if(tagName.equals(RemoteController.RC_ICON)) {
				icon = readRCIcon(parser);
			} else if(tagName.equals(RemoteController.RC_BACKGROUND_COLOR)) {
				backgroundColor = readRCBackgroundColor(parser);
			} else if(tagName.equals(RemoteController.ROW)) {
				rows.add(readRow(parser, remoteController));
			} else {
				skip(parser);
			}
		}
		
		remoteController.setId(id);
		remoteController.setTitle(title);
		remoteController.setAuthor(author);
		remoteController.setIcon(icon);
		remoteController.setBackgroundColor(backgroundColor);
		remoteController.setRows(rows);
		
		return remoteController;
	}
	
	/**
	 * Read one row.
	 * @param parser Parser.
	 * @param remoteController Instance of RemoteController for acces to inner class.
	 * @return One row with buttons.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private Row readRow(XmlPullParser parser, RemoteController remoteController)
			throws XmlPullParserException, IOException {
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRow]");
		
		Row row = remoteController.new Row();
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.ROW);
		
		ArrayList<Button> buttons = new ArrayList<RemoteController.Row.Button>();
		
		while(parser.next()!=XmlPullParser.END_TAG) {
			if(parser.getEventType()!=XmlPullParser.START_TAG) {
				continue;
			}
			
			String tagName = parser.getName();
			
			if(tagName.equals(RemoteController.BUTTON)) {
				buttons.add(readButton(parser, row));
			} else {
				skip(parser);
			}
		}
		
		row.setButtons(buttons);
		
		return row;
	}
	
	/**
	 * Read one Button.
	 * @param parser Parser.
	 * @param row Instance of Row for acces to inner class.
	 * @return One Button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private Button readButton(XmlPullParser parser, Row row)
			throws XmlPullParserException, IOException {
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readButton]");
		
		Button button = row.new Button();
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.BUTTON);
		
		String backgroundColor = "";
		String icon = "";
		String iconColor = "";
		String textFirst = "";
		String textFirstColor = "";
		int textFirstSize = -1;
		String textSecond = "";
		String textSecondColor = "";
		int textSecondSize = -1;
		String action = "";
		
		while(parser.next()!=XmlPullParser.END_TAG) {
			if(parser.getEventType()!=XmlPullParser.START_TAG) {
				continue;
			}
			
			String tagName = parser.getName();
			
			if(tagName.equals(RemoteController.B_BACKGROUND_COLOR)) {
				backgroundColor = readBBackgroundColor(parser);
			} else if(tagName.equals(RemoteController.B_ICON)) {
				icon = readBIcon(parser);
			} else if(tagName.equals(RemoteController.B_ICON_COLOR)) {
				iconColor = readBIconColor(parser);
			} else if(tagName.equals(RemoteController.B_TEXT_FIRST)) {
				textFirst = readBFirstText(parser);
			} else if(tagName.equals(RemoteController.B_TEXT_FIRST_COLOR)) {
				textFirstColor = readBFirstTextColor(parser);
			} else if(tagName.equals(RemoteController.B_TEXT_FIRST_SIZE)) {
				textFirstSize = readBFirstTextSize(parser);
			} else if(tagName.equals(RemoteController.B_TEXT_SECOND)) {
				textSecond = readBSecondText(parser);
			} else if(tagName.equals(RemoteController.B_TEXT_SECOND_COLOR)) {
				textSecondColor = readBSecondTextColor(parser);
			} else if(tagName.equals(RemoteController.B_TEXT_SECOND_SIZE)) {
				textSecondSize = readBSecondTextSize(parser);
			} else if(tagName.equals(RemoteController.B_ACTION)) {
				action = readBAction(parser);
			} else {
				skip(parser);
			}
		}
		
		button.setBackgroundColor(backgroundColor);
		button.setIcon(icon);
		button.setIconColor(iconColor);
		button.setTextFirst(textFirst);
		button.setTextFirstColor(textFirstColor);
		button.setTextFirstSize(textFirstSize);
		button.setTextSecond(textSecond);
		button.setTextSecondColor(textSecondColor);
		button.setTextSecondSize(textSecondSize);
		button.setAction(action);
		
		return button;
	}
	
	/**
	 * Read Id of remote controller.
	 * @param parser Parser.
	 * @return Id of remote controller.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readRCId(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRCId]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.RC_ID);
		
		String id = readText(parser);
		
		return id;
	}
	
	/**
	 * Read title of remote controller.
	 * @param parser Parser.
	 * @return Title of remote controller.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readRCTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRCTitle]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.RC_TITLE);
		
		String title = readText(parser);
		
		return title;
	}
	
	/**
	 * Read author of remote controller.
	 * @param parser Parser.
	 * @return Author of remote controller.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readRCAuthor(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRCAuthor]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.RC_AUTHOR);
		
		String author = readText(parser);
		
		return author;
	}
	
	/**
	 * Read icon of remote controller.
	 * @param parser Parser.
	 * @return Icon of remote controller.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readRCIcon(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRCIcon]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.RC_ICON);
		
		String icon = readText(parser);
		
		return icon;
	}
	
	/**
	 * Read background color of whole remote controller.
	 * @param parser Parser.
	 * @return Background color of whole remote controller.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readRCBackgroundColor(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readRCBackgroundColor]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.RC_BACKGROUND_COLOR);
		
		String backgroundColor = readText(parser);
		
		return backgroundColor;
	}
	
	/**
	 * Read background color of single button.
	 * @param parser Parser.
	 * @return Background color of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBBackgroundColor(XmlPullParser parser)
			throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBBackgroundColor]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_BACKGROUND_COLOR);
		
		String backgroundColor = readText(parser);
		
		return backgroundColor;
	}
	
	/**
	 * Read icon of single button.
	 * @param parser Parser.
	 * @return Icon of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBIcon(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBIcon]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_ICON);
		
		String icon = readText(parser);
		
		return icon;
	}
	
	/**
	 * Read color of icon of single button.
	 * @param parser Parser.
	 * @return Color of icon of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBIconColor(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBIconColor]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_ICON_COLOR);
		
		String iconColor = readText(parser);
		
		return iconColor;
	}
	
	/**
	 * Read first text of single button.
	 * @param parser Parser.
	 * @return First text of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBFirstText(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBFirstText]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_TEXT_FIRST);
		
		String text = readText(parser);
		
		return text;
	}
	
	/**
	 * Read color of first text of single button.
	 * @param parser Parser.
	 * @return Color of first text of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBFirstTextColor(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBFirstTextColor]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_TEXT_FIRST_COLOR);
		
		String textColor = readText(parser);
		
		return textColor;
	}
	
	/**
	 * Read first text size.
	 * @param parser Parser.
	 * @return First text size.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private int readBFirstTextSize(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBFirstTextSize]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_TEXT_FIRST_SIZE);
		
		String textSizeString = readText(parser);
		
		try {
			return Integer.parseInt(textSizeString);
		} catch(NumberFormatException nfe) {
			return 0;
		}
	}
	
	/**
	 * Read second text of single button.
	 * @param parser Parser.
	 * @return Second text of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBSecondText(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBSecondText]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_TEXT_SECOND);
		
		String text = readText(parser);
		
		return text;
	}
	
	/**
	 * Read color of second text of single button.
	 * @param parser Parser.
	 * @return Color of second text of single button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBSecondTextColor(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBSecondTextColor]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_TEXT_SECOND_COLOR);
		
		String textColor = readText(parser);
		
		return textColor;
	}
	
	/**
	 * Read second text size.
	 * @param parser Parser.
	 * @return Second text size.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private int readBSecondTextSize(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBSecondTextSize]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_TEXT_SECOND_SIZE);
		
		String textSizeString = readText(parser);
		
		try {
			return Integer.parseInt(textSizeString);
		} catch(NumberFormatException nfe) {
			return 0;
		}
	}
	
	/**
	 * Read action of button.
	 * @param parser Parser.
	 * @return Action of button.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readBAction(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readBAction]");
		
		parser.require(XmlPullParser.START_TAG, NS, RemoteController.B_ACTION);
		
		String action = readText(parser);
		
		return action;
	}
	
	/**
	 * Getting text from an element.
	 * @param parser Parser.
	 * @return Text from an element. May return empty String.
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readText]");
		
		String result = "";
		
		if(parser.next()==XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[readText][Return: '" + result + "']");
		return result;
	}
	
	/**
	 * Skip tag we do not want to parse.
	 * @param parser Parser.
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[skip]");
		
		if(parser.getEventType()!=XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		
		int depth = 1;
		
		while(depth!=0) {
			switch(parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}
}
