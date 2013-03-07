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

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import cz.babi.android.remoteme.R;
import cz.babi.android.remoteme.common.Common;
import cz.babi.android.remoteme.service.ConnectionService;

/**
 * Activity represent Mouse remote.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class ActivityRemoteMouseAndKeyboard extends Activity {
	
	private static final String TAG_CLASS_NAME =
			ActivityRemoteMouseAndKeyboard.class.getSimpleName();
	
	private DrawView drawView;
	private EditText editText;
	private LinearLayout simpleLeayout;
	
	private ConnectionService connectionService;
	private ServiceConnection serviceConnection;
	
	private SharedPreferences preferences;
	
	private boolean isClipboarSelected;
	
	private boolean isServiceBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onCreate]");
		
		super.onCreate(savedInstanceState);
		
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
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		/* Here we obtain which keyboard simulation is selected and set class variable. */
		String currentKeyboardSimulation = preferences.getString(
				getString(R.string.pref_name_keyboard_simulation),
				getString(R.string.pref_value_clipboard));
		if(currentKeyboardSimulation.compareTo(getString(R.string.pref_value_clipboard))==0)
			isClipboarSelected = true;
		else isClipboarSelected = false;
		
		bindService();
		
		drawView = new DrawView(this);
		
		editText = new EditText(this);
		editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		/* Because we are using 'invisible' edit text for handle key stroke we need to add some
		 * text to this edit text. Because if user stroke 'back space' before other keys and
		 * edit text is empty, its text change listener will not fire onTextChanged method. */
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<50; i++) sb.append(" ");
		editText.setText(sb.toString());
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String oneCharacter = s.subSequence(start, start+count).toString();
				
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onTextChanged][" + oneCharacter + "]");
				
				/* If length of character is 0, it means that backspace was stroke. */
				if(oneCharacter.length()==0) {
					if(!connectionService.keyStroke(Common.BACK_SPACE_KEY_STROKE));
				} else {
					/* Which method is selected. */
					if(isClipboarSelected) {
						if(!connectionService.keyClipboard(oneCharacter)) doFinish();
					} else {
						if(!connectionService.keyStroke(oneCharacter)) doFinish();
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[beforeTextChanged]");
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[afterTextChanged]");
			}
		});
		
		simpleLeayout = new LinearLayout(this);
		simpleLeayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		simpleLeayout.addView(drawView);
		simpleLeayout.addView(editText);
		
		setContentView(simpleLeayout);
		
		drawView.requestFocus();
		
		setOrientation(preferences);
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
		bindService(new Intent(ActivityRemoteMouseAndKeyboard.this,
				ConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
		isServiceBound = true;
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
	
	@Override
	protected void onDestroy() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onDestroy]");
		
		unbindService();
		
		super.onDestroy();
	}
	
	/**
	 * Close activity
	 */
	private void doFinish() {
		if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[doFinish]");
		
		this.finish();
	}
	
	/**
	 * Set orientation.
	 */
	private void setOrientation(SharedPreferences preferences) {
		String currentOrientationLock = preferences.
				getString(getString(R.string.pref_name_orientation_lock),
						getString(R.string.pref_value_default));
		
		if(currentOrientationLock.equals(getString(R.string.pref_value_portait))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if(currentOrientationLock.equals(getString(R.string.pref_value_landscape))) {
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}
	
	/**
	 * With this class we are able to draw a line in touch moving.
	 *
	 * @author Martin Misiarz
	 * @author dev.misiarz@gmail.com
	 * @author johncarl
	 */
	private class DrawView extends View implements OnGestureListener {
		
		private final String TAG_CLASS_NAME = DrawView.class.getSimpleName();
		
		private List<PointF> linePoints;
		
		private Paint paintText;
		private Paint paintLine;
		
		private Path linePath;
		
		private GestureDetector gestureDetector;
		
		private boolean showAndHideKeyboard;
		
		float mouseWheelSmooth;
		
		private final int backgroundColor = Color.WHITE;
		private final int paintColor = Color.rgb(255, 165, 0);
		private final int fontColor = Color.GRAY;
		
		private final int textSpaceLine;
		private final int textYPosition;
		
		/**
		 * Constructor.
		 * @param context Context.
		 */
		public DrawView(Context context) {
			super(context);
			
			linePoints = new ArrayList<PointF>();
			
			paintText = new Paint();
			paintLine = new Paint();
			
			linePath = new Path();
			
			gestureDetector = new GestureDetector(getContext(), this);
			
			setFocusable(true);
			setFocusableInTouchMode(true);
			setBackgroundColor(backgroundColor);
			
			paintLine.setColor(paintColor);
			paintLine.setAntiAlias(true);
			paintLine.setStrokeWidth(Common.getDisplayWidth(getContext())/70);
			paintLine.setStyle(Style.STROKE);
			
			paintText.setColor(fontColor);
			paintText.setAntiAlias(true);
			paintText.setTextSize(Common.getDisplayWidth(getContext())/23);
			paintText.setTextAlign(Align.CENTER);
			
			showAndHideKeyboard = false;
			
			mouseWheelSmooth = (float)preferences.getInt(
					getString(R.string.pref_name_mouse_wheel_smooth), 40) / 65;
			
			if(Common.getDisplayHeight(context)>800) {
				textSpaceLine = Common.getDisplayHeight(context)/280;
				textYPosition = (int)(Common.getDisplayHeight(context)/6.5);
			} else {
				textSpaceLine = Common.getDisplayHeight(context)/240;
				textYPosition = (int)(Common.getDisplayHeight(context)/4.5);
			}
			
			/* Used for double touch. */
			/* lastTimeTouch = -1; */
		}
		
		@SuppressLint("DrawAllocation")
		@Override
		public void onDraw(Canvas canvas) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onDraw]");
			linePath = new Path();
			
			/* If there is only one point (single touch) we can draw a circle. */
			if(linePoints.size()==1) {
				canvas.drawCircle(linePoints.get(0).x, linePoints.get(0).y, 10, paintLine);
			} else {
				if(linePoints.size()>1) {
					for(int i=linePoints.size()-2; i<linePoints.size(); i++) {
						if(i>=0){
							PointF point = linePoints.get(i);
							if(i==0) {
								PointF next = linePoints.get(i+1);
								point.dx = ((next.x-point.x)/3);
								point.dy = ((next.y-point.y)/3);
							} else if(i==linePoints.size()-1) {
								PointF prev = linePoints.get(i-1);
								point.dx = ((point.x-prev.x)/3);
								point.dy = ((point.y-prev.y)/3);
							} else {
								PointF next = linePoints.get(i+1);
								PointF prev = linePoints.get(i-1);
								point.dx = ((next.x-prev.x)/3);
								point.dy = ((next.y-prev.y)/3);
							}
						}
					}
				}
				
				boolean first = true;
				for(int i=0; i<linePoints.size(); i++) {
					PointF point = linePoints.get(i);
					if(first) {
						first = false;
						linePath.moveTo(point.x, point.y);
					} else {
						PointF prev = linePoints.get(i-1);
						linePath.cubicTo(prev.x + prev.dx, prev.y + prev.dy,
								point.x - point.dx, point.y - point.dy,
								point.x, point.y);
					}
				}
				
				canvas.drawPath(linePath, paintLine);
			}
			
			String textToDraw = getResources().getString(R.string.remote_mouse_description_text);
			int x = canvas.getWidth()/2;
			int y = canvas.getHeight()/2-textYPosition;
			for(String line: textToDraw.split("\n")) {
				canvas.drawText(line, x, y, paintText);
				y += -paintLine.ascent()*textSpaceLine + paintLine.descent();
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			
			/* Here we need to catch touch with three fingers. */
			switch(event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_DOWN:
					if(event.getPointerCount()>=3) {
						if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onTouchEvent]" +
								"[Show/Hide keyboard.]");
						
						InputMethodManager inputMethodManager = (InputMethodManager)
								getSystemService(Context.INPUT_METHOD_SERVICE);
						
						if(showAndHideKeyboard) {
							inputMethodManager.toggleSoftInput(
									InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
							
							showAndHideKeyboard = false;
						} else {
							inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
									InputMethodManager.HIDE_IMPLICIT_ONLY);
							
							showAndHideKeyboard = true;
						}
						
						linePoints.clear();
						
						invalidate();
						
						editText.requestFocus();
						editText.setSelection(editText.getText().length());
						
						return true;
					}
			}
			
			return gestureDetector.onTouchEvent(event);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onDown]");
			
			linePoints.clear();
			
			invalidate();
			
			PointF point = new PointF();
			point.x = e.getX();
			point.y = e.getY();
			
			linePoints.add(point);
			
			return true;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onFling]");
			
			return true;
		}
		
		@Override
		public void onLongPress(MotionEvent e) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onLongPress]");
			
			if(!connectionService.mouseRightClick()) doFinish();
			
			invalidate();
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			
			/* For scrolling efect we need to catch up two or more finger movement. */
			if(e2.getPointerCount()==1) {
				if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onScroll][Mooving.][distance X: " + distanceX + "][distance Y: " + distanceY + "]");
				if(!connectionService.moveMouse(distanceX, distanceY)) doFinish();
				
				if(linePoints.size()>50) linePoints.remove(0);
				
				PointF point = new PointF();
				
				point.x = e2.getX();
				point.y = e2.getY();
				
				linePoints.add(point);
				
				invalidate();
			} else if(e2.getPointerCount()==2) {
				float finalDistanceY = distanceY*mouseWheelSmooth;
				
				if(distanceY>0) {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onScroll][Scrolling up.]");
					if(!connectionService.mouseWheel(finalDistanceY)) doFinish();
				} else {
					if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onScroll][Scrolling down.]");
					if(!connectionService.mouseWheel(finalDistanceY)) doFinish();
				}
				
				linePoints.clear();
				invalidate();
			}
			
			return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onShowPress]");
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if(Common.DEBUG) Log.d(TAG_CLASS_NAME, "[onSingleTapUp]");
			
			if(!connectionService.mouseLeftClick()) doFinish();
			
			return true;
		}
		
		/**
		 * Class presents point object with X and Y coordinates.
		 * 
		 * @author Martin Misiarz
		 * @author dev.misiarz@gmail.com
		 */
		private class PointF {
			
			public float x, y, dx, dy;
			
			/**
			 * Constructor.
			 */
			public PointF() {}
		}
	}
}
