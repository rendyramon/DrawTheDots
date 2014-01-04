package com.villu164.drawthedots;


import java.util.Calendar;

import com.villu164.drawthedots.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas; 
import android.graphics.Color;
import android.graphics.Paint;
import com.villu164.drawthedots.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private SignatureView sig_view;
	private DatabaseHandler db;
	long starting_time = System.currentTimeMillis();
	int last_key = 0;
	float ALLOWED_DOUBLE_PRESS = (float)500.0;
	boolean toggle_keyboard = false;
	private View contentView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		//Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.felt2);
		//Canvas canvas = new Canvas(bitmap);
		sig_view = (SignatureView) findViewById(R.id.signatureView1);

		//final View controlsView = findViewById(R.id.fullscreen_content_controls);
		contentView = findViewById(R.id.fullscreen_content);
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		db = new DatabaseHandler(this);
		sig_view.init_db(db);
		sig_view.init_parent(this);
		//db.onUpgrade(db.getWritableDatabase(), 0, 1);
		//System.out.println(db.getPathsCount());
		
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});
		mSystemUiHider.hide();
		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});
	}

	private long get_time(){
		return System.currentTimeMillis();
	}

	private boolean was_double(int key_code){
		long now = get_time();
		boolean match = (last_key == key_code && (now - starting_time) < ALLOWED_DOUBLE_PRESS);
		last_key = key_code;
		starting_time = now;
		if (match) last_key = 0;
		return match;
	}


	@Override
	public boolean onKeyUp( int keyCode, KeyEvent event ) {
		boolean double_press = was_double(keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (double_press) exit();
			else message("DoubleTap to exit :D");
			return true;
		case KeyEvent.KEYCODE_MENU:
			if (double_press) {
				toggleKeyboard();
			}
			else {
				if (toggle_keyboard){
					toggle_keyboard = !toggle_keyboard;
					toggleKeyboard(false);
				}
				else {
					if (TOGGLE_ON_CLICK) {
						mSystemUiHider.toggle();
					} else {
						mSystemUiHider.show();
					}
				}
			}
			/* Sample for handling the Menu button globally */
			//message("No menu :(");
			return true;
		case KeyEvent.KEYCODE_C:
			sig_view.clear(true);
			return true;
		case KeyEvent.KEYCODE_D:
			sig_view.toggle_debug();
			return true;
		case KeyEvent.KEYCODE_R:
			sig_view.make_dots();
			return true;
		case KeyEvent.KEYCODE_T:
			sig_view.toggle_play();
			return true;
		case KeyEvent.KEYCODE_SPACE:
			sig_view.reset();
			sig_view.select(0);
			return true;
		case KeyEvent.KEYCODE_DEL:
			sig_view.clear();
			return true;
		case KeyEvent.KEYCODE_F:
			Intent intent = new Intent(this, EditDrawingActivity.class);
		    //EditText editText = (EditText) findViewById(R.id.edit_message);
		    //String message = editText.getText().toString();
		    //intent.putExtra(EXTRA_MESSAGE, message);
		    startActivity(intent);
		    return true;
		}

		return super.onKeyUp( keyCode, event );
	}

	public void toggleKeyboard(){
		toggleKeyboard(toggle_keyboard);
		toggle_keyboard = !toggle_keyboard;
	}
	
	public void toggleKeyboard(boolean show){
		if (show){
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(contentView.getWindowToken(),0); 
		}
		else {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
		}
	}
	
	
	public void exit(){
		finish();
		//
		//Anyway if your app consist of only 1 activity you should call finish();
		//int pid = android.os.Process.myPid();
		//android.os.Process.killProcess(pid);
		//
	}

	public void message(String message){
		message(message,false);
		int i = 2;
	}

	public void message(String message, boolean long_message){
		int message_length = Toast.LENGTH_SHORT;
		if (long_message) message_length = Toast.LENGTH_LONG;
		Toast.makeText(getApplicationContext(), message,message_length).show();
	}

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	public void clearAll(View view){
		//R.id.signatureView1

		sig_view.clear(true);
	}	

	public void clearLast(View view){
		//R.id.signatureView1

		sig_view.clear();
	}

	public void selectLast(View view){
		sig_view.reset();
		sig_view.select(0);
	}

	public void selectNext(View view){
		sig_view.select(1);
	}

	public void selectPrevious(View view){
		sig_view.select(-1);
	}

	public void nextStep(View view){
		sig_view.make_dots(); //cannot use at the moment, because
	}

	public void playGame(View view){
		sig_view.toggle_play();
	}

}
