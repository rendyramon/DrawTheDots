package com.villu164.drawthedots;


import com.villu164.drawthedots.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);

		//Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.felt2);
		//Canvas canvas = new Canvas(bitmap);
		sig_view = (SignatureView) findViewById(R.id.signatureView1);

		//final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		db = new DatabaseHandler(this);
		sig_view.init_db(db);
		sig_view.init_parent(this);
		//db.onUpgrade(db.getWritableDatabase(), 0, 1);
		System.out.println(db.getPathsCount());
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//message(keyCode + "");
		//t=48 d=32 space=62 backspace=67 
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			/* Sample for handling the Menu button globally */
			message("No menu :(");
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
		}
		
		return false;
	} 

	public void message(String message){
		message(message,false);
	}

	public void message(String message, boolean long_message){
		int message_length = Toast.LENGTH_SHORT;
		if (long_message) message_length = Toast.LENGTH_LONG;
		Toast.makeText(getApplicationContext(), message,message_length).show();
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
