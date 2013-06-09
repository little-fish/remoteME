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

package cz.babi.android.remoteme.ui;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.entity.RemoteController;
import cz.babi.android.remoteme.entity.RemoteController.Row;
import cz.babi.android.remoteme.entity.RemoteController.Row.Button;
import cz.babi.android.remoteme.service.ConnectionService;

/**
 * This activity represents a basic remote controller.
 * Whole layout will be filled with data obtained from intent.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityRemoteController extends Activity {

	private static final String TAG_CLASS_NAME =
			ActivityRemoteController.class.getSimpleName();

	/* Default colors. They will be user did not define them. */
	private static final int DEFAULT_REMOTE_BACKGROUND = Color.WHITE;
	private static final int DEFAULT_BUTTON_BACKGROUND = Color.GRAY;
	private static final int DEFAULT_BUTTON_ICON_COLOR = Color.WHITE;
	private static final int DEFAULT_BUTTON_TEXT_COLOR = Color.WHITE;

	private RemoteController remoteController = null;

	private ConnectionService connectionService;
	private ServiceConnection serviceConnection;

	private boolean isServiceBound = false;

	/* If we need to obtain icon from file located in sd card, we need
	 * scale this icon to proper size. */
	private int properIconWidth;
	private int properIconHeight;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");

		super.onCreate(savedInstanceState);

		/* Create new service connection. */
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onServiceConnected]");
				connectionService = ((ConnectionService.ConnectionBinder)service).getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onServiceDisconnected]");
				connectionService = null;
			}
		};

		bindService();

		Intent intent = getIntent();
		remoteController = (RemoteController)intent.getSerializableExtra("remoteController");

		LinearLayout.LayoutParams layoutParamsButtonToRow = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, 1.0f);
		layoutParamsButtonToRow.setMargins(4, 0, 4, 0);

		LinearLayout.LayoutParams layoutParamsButton = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams layoutParamsRowToMain = null;

		/* Obtain proper width and height of icon.
		 * Icon loaded from sd card will be scalled to those proportions. */
		Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.control_stop);
		properIconWidth = defaultBitmap.getWidth();
		properIconHeight = defaultBitmap.getHeight();

		/* Layout represents whole remote controller. */
		LinearLayout mainLayout = new LinearLayout(this);
		mainLayout.setOrientation(LinearLayout.VERTICAL);

		/* Need to parse remote controller background color. */
		if(remoteController.getBackgroundColor().length()!=0) {
			int rcBackgroundColor = getColorIntFromString("#" + remoteController.getBackgroundColor());
			if(rcBackgroundColor!=Integer.MAX_VALUE) mainLayout.setBackgroundColor(rcBackgroundColor);
			else mainLayout.setBackgroundColor(DEFAULT_REMOTE_BACKGROUND);
		} else mainLayout.setBackgroundColor(DEFAULT_REMOTE_BACKGROUND);

		/* Iterate every row. */
		for(int r=0; r<remoteController.getRows().size(); r++) {
			/* Create new Linear layout represents single row. */
			LinearLayout rowLayout = new LinearLayout(this);
			rowLayout.setOrientation(LinearLayout.HORIZONTAL);

			/* Obtain row from remote controller. */
			Row row = remoteController.getRows().get(r);

			/* Iterate every button in row. */
			for(int b=0; b<row.getButtons().size(); b++) {
				/* One button in row. */
				final Button button = row.getButtons().get(b);

				/* Create new Linear layout represents single button. */
				LinearLayout buttonLayout = new LinearLayout(this);
				buttonLayout.setOrientation(LinearLayout.VERTICAL);
				buttonLayout.setClickable(true);
				buttonLayout.setFocusable(true);
				buttonLayout.setGravity(Gravity.CENTER);
				buttonLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						doSomething(button.getAction());
					}
				});

				/* Need to set button background. */
				int cBackgroundColor = DEFAULT_BUTTON_BACKGROUND;
				if(button.getBackgroundColor().length()!=0) {
					cBackgroundColor = getColorIntFromString("#" + button.getBackgroundColor());
					if(cBackgroundColor==Integer.MAX_VALUE) cBackgroundColor = DEFAULT_BUTTON_BACKGROUND;
				}
				buttonLayout.setBackgroundColor(cBackgroundColor);

				/* Need to set clicked button's background. */
				int lighterColor = getLighterColor(cBackgroundColor, .3f);

				Drawable pressedDrawable = new ColorDrawable(lighterColor);

				StateListDrawable stateListDrawable = new StateListDrawable();
				stateListDrawable.addState(new int[] { android.R.attr.state_pressed },
						pressedDrawable);
				stateListDrawable.addState(new int[] {}, buttonLayout.getBackground());

				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
					buttonLayout.setBackground(stateListDrawable);
				} else buttonLayout.setBackgroundDrawable(stateListDrawable);

				/* Some fun with icon.. */
				ImageView iconView = null;

				int bIconColor = DEFAULT_BUTTON_ICON_COLOR;
				if(button.getIconColor().length()!=0) {
					bIconColor = getColorIntFromString("#" + button.getIconColor());
					if(bIconColor==Integer.MAX_VALUE) bIconColor = DEFAULT_BUTTON_ICON_COLOR;
				}

				/*If there is some text in icon variable. */
				if(button.getIcon().length()!=0) {
					String bIconUri = "drawable/" + button.getIcon();
					int bIconId = getResources().getIdentifier(bIconUri, null,
							getPackageName());

					/* If icon exists in drawable folder. */
					if(bIconId!=0) {
						Drawable drawableIcon = getResources().getDrawable(bIconId);
						drawableIcon.setColorFilter(bIconColor, Mode.MULTIPLY);

						/* Set icon to view. */
						iconView = new ImageView(this);
						iconView.setImageDrawable(drawableIcon);
						iconView.setPadding(12, 2, 12, 0);
					} else {
						/* Need co check if icon is located on sc card. */
						File sdCard = Environment.getExternalStorageDirectory();

						/* Also need to add extension to filename. */
						String[] extensions = new String[]{ ".jpg", ".jpeg", ".png", ".bmp" };

						File iconFile = null;
						for(String extension : extensions) {
							iconFile = new File(sdCard.getAbsolutePath() + "/" +
									Common.APP_FOLDER + "/" + Common.ICON_FOLDER + "/" +
									button.getIcon() + extension);
							if(iconFile.exists()) break;
							else iconFile = null;
						}

						/* If icon is located on SD card. */
						if(iconFile!=null) {
							Bitmap iconBitmap = BitmapFactory.decodeFile(iconFile.getAbsolutePath());

							/* if Bitmap was created succesfully we need to scale it and create
							 * drawable from it. */
							if(iconBitmap!=null) {
								Bitmap scaledBitmap = Bitmap.createScaledBitmap(
										iconBitmap, properIconWidth, properIconHeight, false);

								BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), scaledBitmap);

								bitmapDrawable.setColorFilter(bIconColor, Mode.MULTIPLY);

								iconView = new ImageView(this);
								if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
									iconView.setBackground(bitmapDrawable);
								} else iconView.setBackgroundDrawable(bitmapDrawable);
							}
						} else {
							/* Icon does not exist! */
						}
					}
				} else {
					/* Icon does not exist! */
				}

				/* Some fun with FIRST text. */
				TextView firstNoteView = null;
				/* If there is some FIRST text to display. */
				if(button.getTextFirst().length()!=0) {
					firstNoteView = new TextView(this);

					/* Set FIRST text color. */
					int bTextFirstColor = DEFAULT_BUTTON_TEXT_COLOR;
					if(button.getTextFirstColor().length()!=0) {
						bTextFirstColor = getColorIntFromString("#" + button.getTextFirstColor());
						if(bTextFirstColor==Integer.MAX_VALUE) bTextFirstColor = DEFAULT_BUTTON_TEXT_COLOR;
					}
					firstNoteView.setTextColor(bTextFirstColor);

					/* Set FIRST text size. */
					if(button.getTextFirstSize()>0) firstNoteView.setTextSize(
							button.getTextFirstSize());

					/* Obtain proper FIRST text. */
					String buttonTextFirst = "";
					if(button.getTextFirst().startsWith("string_")) {
						buttonTextFirst = getStringResourceByName(
								button.getTextFirst().substring(
										button.getTextFirst().indexOf("_")+1));
					} else buttonTextFirst = button.getTextFirst();

					/* Set FIRST text, gravity and padding. */
					firstNoteView.setText(buttonTextFirst);
					firstNoteView.setGravity(Gravity.CENTER);
					firstNoteView.setPadding(0, 2, 0, 0);
				} else {
					/* FIRST text does not exists! */
				}

				/* Some fun with SECOND text. */
				TextView secondNoteView = null;
				/* If there is some SECOND text to display. */
				if(button.getTextSecond().length()!=0) {
					secondNoteView = new TextView(this);

					/* Set SECOND text color. */
					int bTextSecondColor = DEFAULT_BUTTON_TEXT_COLOR;
					if(button.getTextSecondColor().length()!=0) {
						bTextSecondColor = getColorIntFromString("#" + button.getTextSecondColor());
						if(bTextSecondColor==Integer.MAX_VALUE) bTextSecondColor = DEFAULT_BUTTON_TEXT_COLOR;
					}
					secondNoteView.setTextColor(bTextSecondColor);

					/* Set SECOND text size. */
					if(button.getTextSecondSize()>0) secondNoteView.setTextSize(
							button.getTextSecondSize());

					/* Obtain proper SECOND text. */
					String buttonTextSecond = "";
					if(button.getTextSecond().startsWith("string_")) {
						buttonTextSecond = getStringResourceByName(
								button.getTextSecond().substring(
										button.getTextSecond().indexOf("_")+1));
					} else buttonTextSecond = button.getTextSecond();

					/* Set SECOND text, gravity and padding. */
					secondNoteView.setText(buttonTextSecond);
					secondNoteView.setGravity(Gravity.CENTER);
					secondNoteView.setPadding(0, 2, 0, 0);
				} else {
					/* SECOND text does not exists! */
				}

				/* Add icon to buttons's layout.*/
				if(iconView!=null) buttonLayout.addView(iconView, layoutParamsButton);

				/* Add FIRST text to button's layout. */
				if(firstNoteView!=null) buttonLayout.addView(firstNoteView, layoutParamsButton);

				/* Add SECOND text to button's layout. */
				if(secondNoteView!=null) buttonLayout.addView(secondNoteView, layoutParamsButton);

				/* Add button to row. */
				rowLayout.addView(buttonLayout, layoutParamsButtonToRow);
			}

			/* Set proper margins. */
			if(r==0) {
				layoutParamsRowToMain = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT, 1.0f);
				layoutParamsRowToMain.setMargins(4, 8, 4, 4);
			} else if(r==remoteController.getRows().size()-1) {
				layoutParamsRowToMain = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT, 1.0f);
				layoutParamsRowToMain.setMargins(4, 4, 4, 8);
			} else {
				layoutParamsRowToMain = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT, 1.0f);
				layoutParamsRowToMain.setMargins(4, 4, 4, 4);
			}

			/* Add row to main layout. */
			mainLayout.addView(rowLayout, layoutParamsRowToMain);
		}

		/* Set whole view. */
		setContentView(mainLayout);

		setOrientation();

		setWakeLock();
	}

	/**
	 * Bind service.
	 */
	private void bindService() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[bindService]");

		/* Establish a connection with the service.  We use an explicit
		 * class name because we want a specific service implementation that
		 * we know will be running in our own process (and thus won't be
		 * supporting component replacement by other applications). */
		bindService(new Intent(ActivityRemoteController.this,
				ConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		isServiceBound = true;
	}

	@Override
	protected void onDestroy() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onDestroy]");

		unbindService();

		super.onDestroy();
	}

	/**
	 * Unbind service.
	 */
	private void unbindService() {
		if (isServiceBound) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[unbindService]");

			/* Detach our existing connection. */
			unbindService(serviceConnection);
			isServiceBound = false;
		}
	}

	/**
	 * Set orientation.
	 */
	private void setOrientation() {
		String currentOrientationLock = PreferenceManager.getDefaultSharedPreferences(this).
				getString(getString(R.string.pref_name_orientation_lock),
						getString(R.string.pref_value_default));

		if(currentOrientationLock.equals(getString(R.string.pref_value_portait))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if(currentOrientationLock.equals(getString(R.string.pref_value_landscape))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	/**
	 * Set wake lock.
	 */
	private void setWakeLock() {
		boolean wakeLock = PreferenceManager.getDefaultSharedPreferences(this).
				getBoolean(getString(R.string.pref_name_keep_device_awake), false);

		if(wakeLock) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Parse String to int define specific color.
	 * @param colorString Color in string.
	 * @return Int define color. May return -1 if IllegalArgumentException
	 * is thrown.
	 */
	private int getColorIntFromString(String colorString) {
		int ret = Integer.MAX_VALUE;

		try {
			ret = Color.parseColor(colorString.toUpperCase());
		} catch(IllegalArgumentException iae) {
			if(Common.ERROR) Log.e(TAG_CLASS_NAME, "[getColorIntFromString]" +
					"[Can not parse input String to Color int.][Input string" +
					" is '" + colorString + "']");
		}

		return ret;
	}

	/**
	 * Get lighter color from source.
	 * @param sourceColor Source color.
	 * @param factor Saturation.
	 * @return Lighter color.
	 */
	private int getLighterColor(int sourceColor, float factor) {
		int red = Color.red(sourceColor);
		int green = Color.green(sourceColor);
		int blue = Color.blue(sourceColor);

		float lighterRed = (1-factor)*red + factor*255;
		float lighterGreen = (1-factor)*green + factor*255;
		float lighterBlue = (1-factor)*blue + factor*255;

		return Color.rgb((int)lighterRed, (int)lighterGreen, (int)lighterBlue);
	}

	/**
	 * Method will call service to do something.
	 * @param action Action.
	 */
	private void doSomething(String action) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doSomething][" + action + "]");

		/* Check for some special commands. */
		if(action.equals(Common.COMMAND_SHUTDOWN) ||
				action.equals(Common.COMMAND_RESTART) ||
				action.equals(Common.COMMAND_LOGOFF)) {
			if(!connectionService.doSpecial(action)) finish();
		} else {
			if(!connectionService.keyStroke(action)) finish();
		}
	}

	/**
	 * Get String resource by its name.
	 * @param resName Resource name.
	 * @return Resource.
	 */
	private String getStringResourceByName(String resName) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[getStringResourceByName][" + resName + "]");

		String packageName = getPackageName();

		int resId = getResources().getIdentifier(resName, "string", packageName);

		if(resId==0) return "";
		else return getString(resId);
	}
}
