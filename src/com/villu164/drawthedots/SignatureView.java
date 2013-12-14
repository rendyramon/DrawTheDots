package com.villu164.drawthedots;
//originally taken from http://corner.squareup.com/2010/07/smooth-signatures.html
//multi-path is from http://stackoverflow.com/questions/18316382/change-path-color-without-changing-previous-paths @leadrien
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.Toast;

public class SignatureView extends View {
	private static final float STROKE_WIDTH = 5f;
	private static final boolean DEBUG = true;
	private DatabaseHandler db;
	private FullscreenActivity fsa;
	/** Need to track this so the dirty region can accommodate the stroke. **/
	private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
	private static final float FINGER_WIDTH = (float)20;
	private int _selection_index = 0;
	private Paint paint = new Paint();
	private Path path = new Path();
	//private View contentView;

	private List<Stroke> _allStrokes; //all strokes that need to be drawn
	private List<FloatPoint> _selectedFPs; //all strokes that need to be drawn
	private boolean _selectedFPs_valid = false;
	private SparseArray<Stroke> _activeStrokes; //use to retrieve the currently drawn strokes
	private Stroke _selectedStroke = null;
	private boolean _has_selection = false;
	private Random _rdmColor = new Random();
	private Path play_path;

	private Paint selected_paint = new Paint();
	private Paint selected_point_paint = new Paint();
	private Paint deselected_point_paint = new Paint();
	private Paint white_fill = new Paint();
	private Paint red_fill = new Paint();
	private Paint black_fill = new Paint();
	private Paint black_text = new Paint();
	private Paint debug_text = new Paint();
	private boolean play_game = false;
	private boolean debug = false;
	private boolean has_completed = false;
	private FloatPoint last_point = null;
	private FloatPoint moving_point = null;
	private FloatPoint last_point_of_line = null;
	private int progress_index = 1;
	private int invalidates_called = 0;

	/**
	 * Optimizes painting by invalidating the smallest possible area.
	 */
	private float lastTouchX;
	private float lastTouchY;
	private final RectF dirtyRect = new RectF();

	public SignatureView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init_strokes();


		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(STROKE_WIDTH);
	}

	public void toggle_play(){
		play_game = !play_game;
		progress_index = 0;
		moving_point = null;
		last_point = null;
		has_completed = false;
		invalidate();
	}

	public void toggle_debug(){
		debug = !debug;
		invalidate();
	}

	private void init_strokes(){
		_allStrokes = new ArrayList<Stroke>();
		_selectedFPs = new ArrayList<FloatPoint>();
		_activeStrokes = new SparseArray<Stroke>();
		selected_paint.setStyle(Paint.Style.STROKE);
		selected_paint.setColor(Color.BLUE);
		selected_paint.setStrokeWidth(STROKE_WIDTH);
		selected_point_paint.setStyle(Paint.Style.FILL);
		selected_point_paint.setColor(Color.RED);
		selected_point_paint.setStrokeWidth(STROKE_WIDTH);
		deselected_point_paint.setStyle(Paint.Style.FILL);
		deselected_point_paint.setColor(Color.BLACK);
		deselected_point_paint.setStrokeWidth(STROKE_WIDTH);
		black_fill.setStyle(Paint.Style.FILL);
		black_fill.setColor(Color.BLACK);
		black_fill.setStrokeWidth(STROKE_WIDTH);
		white_fill.setStyle(Paint.Style.FILL);
		white_fill.setColor(Color.WHITE);
		white_fill.setStrokeWidth(STROKE_WIDTH);
		red_fill.setStyle(Paint.Style.FILL);
		red_fill.setColor(Color.RED);
		red_fill.setStrokeWidth(STROKE_WIDTH);
		black_text.setStyle(Paint.Style.FILL);
		black_text.setColor(Color.BLACK);
		black_text.setStrokeWidth(STROKE_WIDTH);
		black_text.setTextAlign(Paint.Align.CENTER);
		black_text.setTextSize(14);
		debug_text.setStyle(Paint.Style.FILL);
		debug_text.setColor(Color.BLACK);
		debug_text.setStrokeWidth(STROKE_WIDTH);
		debug_text.setTextAlign(Paint.Align.LEFT);
		debug_text.setTextSize(10);
	}

	public void init_db(DatabaseHandler db){
		this.db = db;
	}

	public void test1(Canvas canvas, Paint paint){
		Path path = new Path();
		path.moveTo(10, 10);
		path.lineTo(20, 20);
		path.lineTo(30, 30);
		path.moveTo(40, 40);
		path.lineTo(50, 50);
		path.lineTo(40,60);
		path.close();
		canvas.drawPath(path, paint);
	}

	/**
	 * Erases the signature.
	 */
	public void clear() {
		//path.reset();

		Stroke stroke = new Stroke(paint,0);
		if (_allStrokes.size() > 0) 
			stroke = _allStrokes.remove(_allStrokes.size() - 1);
		int id = stroke.getId();
		if (id > -1) _activeStrokes.delete(id);
		// Repaints the entire view.
		_has_selection = false;
		play_game = false;
		invalidate();
	}

	public void clear(boolean clear_all) {
		path.reset();
		if (clear_all) {
			db.deleteAllPaths();
			init_strokes();
		}
		else {

			if (_allStrokes.size() > 0) {
				_allStrokes.remove(_allStrokes.size() - 1);
			}
			//if (_activeStrokes.size() > 0) _activeStrokes.remove(_activeStrokes.size() - 1);
		}

		// Repaints the entire view.
		_has_selection = false;
		play_game = false;
		invalidate();
	}


	/**
	 * Creates the dots on the last stroke
	 */
	public void make_dots() {
		for(Stroke stroke: _allStrokes){
			stroke.save(db);
		}
		init_strokes();
		for (int gpid: db.getGroupIds()) {
			List<FloatPoint> nfp = db.getPath(gpid);
			Stroke new_stroke = new Stroke(db,gpid); 
			_allStrokes.add(new_stroke);
			_selectedFPs_valid = false;
			invalidate();
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		int count_index = 1;
		int line_count_index = 1;
		int total_selected_count = 0;

		boolean choose = true;
		boolean draw_enabled = true;
		if (choose) {
			if (_allStrokes != null) {
				for (Stroke stroke: _allStrokes) {
					if (stroke != null) {
						Path path = stroke.getPath();
						int id = stroke.getId();
						Paint painter = stroke.getPaint();
						if ((path != null) && (painter != null)) {
							if (!play_game) {
								if(_has_selection && _selectedStroke == stroke) canvas.drawPath(_selectedStroke.getPath(), selected_paint);
								else {
									canvas.drawPath(path, painter);
								}
							}

							List<FloatPoint> points = stroke.getFloatPoints();
							if (points == null) continue;
							boolean play_path_has_fps = false;
							play_path = new Path();
							for (FloatPoint fp: points) {
								//Here begins the first loop for the points -> lets try to do everything here
								if (draw_enabled) {
									play_path_has_fps = true;
									if (fp == points.get(0)) {
										play_path.moveTo(fp.x, fp.y);
									}
									else {
										play_path.lineTo(fp.x, fp.y);
									}
								}
								if (fp.get_selected()) {
									if (!_selectedFPs_valid) _selectedFPs.add(fp);

									line_count_index++;
									if (fp == last_point) draw_enabled = false;
								}
								//Play region out



								//
								if (!fp.get_selected()) {
									//if (!play_game) canvas.drawCircle(fp.x, fp.y, 3, deselected_point_paint);
								}
								else{

								}
							}


							//Play path draw
							if (play_game && play_path_has_fps) canvas.drawPath(play_path, selected_paint);
							//Play path draw


							if (count_index == 1 && play_game){ //this is the draggable line visible when connecting the dots
								if (!has_completed && last_point != null && moving_point != null) canvas.drawLine(last_point.x, last_point.y, moving_point.x, moving_point.y, deselected_point_paint);
							}
							if (!play_game) {
								for (FloatPoint fp: points) {

									if (fp.get_selected()) {
										canvas.drawCircle(fp.x, fp.y, 3, selected_point_paint);
									}
									else {
										canvas.drawCircle(fp.x, fp.y, 3, deselected_point_paint);
									}
								}
							}
						}
					}
				}
			}
			_selectedFPs_valid = true;
			//loop through the selected dots
			int selected_points_counter = 0;
			FloatPoint expected_moving_point = null;
			int simple_counter = 0;
			last_point_of_line = null;
			if (_has_selection) {
				for (FloatPoint fp: _selectedFPs) {
					if (simple_counter == 0 && last_point == null) last_point = fp;
					if (simple_counter == progress_index) expected_moving_point = fp;
					last_point_of_line = fp;
					simple_counter++;
				}
				has_completed = (last_point_of_line != null && last_point_of_line == last_point);
				if (moving_point != null && expected_moving_point != null) {
					float dist = expected_moving_point.distance(moving_point, FINGER_WIDTH);
					if (!has_completed && expected_moving_point.distance(moving_point, FINGER_WIDTH) > 0) {
						last_point = expected_moving_point; //moving point is here to help draw the live-line
						moving_point = null;
						progress_index++;
					}
				}
				for (FloatPoint fp: _selectedFPs) {
					canvas.drawCircle(fp.x, fp.y, black_text.getTextSize(), black_fill);
					if (fp == last_point) {
						canvas.drawCircle(fp.x, fp.y, black_text.getTextSize() - 2, red_fill);
					}
					else {
						canvas.drawCircle(fp.x, fp.y, black_text.getTextSize() - 2, white_fill);	
					}
					canvas.drawText((selected_points_counter + 1) + "", fp.x, fp.y + black_text.getTextSize()/2, black_text);
					//count_index++;

					selected_points_counter++;
				}
			}
		}
		//else {canvas.drawPath(path, paint);}
		//canvas.drawText(progress_index + ";" + line_count_index + ";" + count_index, 50,50, debug_text);
		if (debug) canvas.drawText("play:" + play_game + ";select:" + _has_selection + " " + progress_index + ";" + "has_completed:"+ has_completed + ";" + line_count_index + ";" + count_index + "; invalidates_called=" + invalidates_called, 0,debug_text.getTextSize(), debug_text);
		//if (debug) test1(canvas,selected_paint);
		
	}


	private boolean select_action_switch(MotionEvent event){
		//in the future, consider making the touch to a circle or something OR double-click draw would be a circle
		//take the dirtyRect implementation from the previous version and use it to smooth the lines -- Later
		float eventX = event.getX(0);
		float eventY = event.getY(0);
		int action = event.getAction();
		//int x = (int)eventX;
		//int y = (int)eventY;
		int id = event.getPointerId(0);


		switch(action){
		case MotionEvent.ACTION_UP:
			//message("Cannot draw, selection is active!");
			//_selectedStroke.getSelectedPoints().add(new FloatPoint(eventX,eventY));
			_selectedFPs_valid = false;
			_selectedFPs = new ArrayList<FloatPoint>();
			FloatPoint touch_down_fp = new FloatPoint(eventX,eventY,false);
			FloatPoint nearest = _selectedStroke.nearestPoint(touch_down_fp,FINGER_WIDTH);
			if (nearest != null) nearest.toggle_select();
			else {

				//					List<FloatPoint> _selection_points = new ArrayList<FloatPoint>();; //all elements that You need
				if (_allStrokes != null) {
					FloatPoint min_selected_fp = null;
					Stroke closest_stroke = null;
					float min_selected = 0;
					for (Stroke stroke: _allStrokes) {
						if (stroke != null) {
							nearest = stroke.nearestPoint(touch_down_fp, FINGER_WIDTH);
							if (nearest != null){
								if (min_selected_fp == null){
									min_selected_fp = nearest;
									closest_stroke = stroke;
								}
								float compare = min_selected_fp.distance(nearest, FINGER_WIDTH);
								if (compare > 0 && compare < min_selected){
									min_selected_fp = nearest;
									closest_stroke = stroke;
									min_selected = compare;
								}
							}
						}
					}
					if (min_selected_fp != null){
						min_selected_fp.toggle_select();
						if (min_selected_fp.get_selected()) last_point = null;
						
						//last_point = min_selected_fp;
						_selectedStroke = closest_stroke;
						invalidate();
						return true;
						//invalidate();
					}
				}
				//if (nearest == null) _has_selection = false;




			}
			invalidate();
			break;
		}
		invalidate();
		return true;
	}

	private boolean play_action_switch(MotionEvent event){
		//in the future, consider making the touch to a circle or something OR double-click draw would be a circle
		//take the dirtyRect implementation from the previous version and use it to smooth the lines -- Later
		float eventX = event.getX(0);
		float eventY = event.getY(0);
		int action = event.getAction();
		//int x = (int)eventX;
		//int y = (int)eventY;
		int id = event.getPointerId(0);


		//int id = _activeStrokes.size() + 1;
		//if (DEBUG) System.out.println("Registered: " + event.toString());
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			path.moveTo(eventX, eventY);
			lastTouchX = eventX;
			lastTouchY = eventY;

			if (play_game) {
				//last_point = new FloatPoint(eventX,eventY,false);
				return true;
			}
		case MotionEvent.ACTION_MOVE:
			if (play_game) {
				moving_point = new FloatPoint(eventX,eventY,false);
				invalidate();
				return true;
			}
		case MotionEvent.ACTION_UP:
			if (play_game) {

				return true;
			}
		}

		return true;
	}

	private boolean draw_action_switch(MotionEvent event){
		//in the future, consider making the touch to a circle or something OR double-click draw would be a circle
		//take the dirtyRect implementation from the previous version and use it to smooth the lines -- Later
		float eventX = event.getX(0);
		float eventY = event.getY(0);
		int action = event.getAction();
		//int x = (int)eventX;
		//int y = (int)eventY;
		int id = event.getPointerId(0);


		//int id = _activeStrokes.size() + 1;
		//if (DEBUG) System.out.println("Registered: " + event.toString());
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			path.moveTo(eventX, eventY);
			lastTouchX = eventX;
			lastTouchY = eventY;

			//create a paint with random color
			Paint paint_rnd = new Paint();
			paint_rnd.setStyle(Paint.Style.STROKE);
			paint_rnd.setStrokeWidth(STROKE_WIDTH);
			paint_rnd.setColor(_rdmColor.nextInt());

			//create the Stroke
			//Point pt = new Point(x,y);
			Stroke stroke = new Stroke(paint_rnd, id);
			stroke.addPoint(eventX, eventY, false);
			_activeStrokes.put(id, stroke);
			_allStrokes.add(stroke);
			reset();
			// There is no end point yet, so don't waste cycles invalidating.
			return true;

		case MotionEvent.ACTION_MOVE:
			if (play_game) {
				moving_point = new FloatPoint(eventX,eventY,false);
				invalidate();
				return true;
			}
		case MotionEvent.ACTION_UP:

			// Start tracking the dirty region.
			resetDirtyRect(eventX, eventY);
			Stroke stroke_move = _activeStrokes.get(id);

			// When the hardware tracks events faster than they are delivered, the
			// event will contain a history of those skipped points.
			int historySize = event.getHistorySize();
			for (int i = 0; i < historySize; i++) {
				float historicalX = event.getHistoricalX(i);
				float historicalY = event.getHistoricalY(i);
				//int historical_x = (int)historicalX;
				//int historical_y = (int)historicalY;
				expandDirtyRect(historicalX, historicalY);
				path.lineTo(historicalX, historicalY);
				if (stroke_move != null) {
					//Point pt_move = new Point(historical_x, historical_y);
					stroke_move.addPoint(historicalX, historicalY, false);
				}
			}

			// After replaying history, connect the line to the touch point.
			path.lineTo(eventX, eventY);
			if (stroke_move != null) {
				//Point pt_move = new Point(x, y);
				stroke_move.addPoint(eventX, eventY, false);
			}
			break;

		default:
			System.out.println("Ignored touch event: " + event.toString());
			return false;
		}

		// Include half the stroke width to avoid clipping.
		invalidate(
				(int) (dirtyRect.left - HALF_STROKE_WIDTH),
				(int) (dirtyRect.top - HALF_STROKE_WIDTH),
				(int) (dirtyRect.right + HALF_STROKE_WIDTH),
				(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

		lastTouchX = eventX;
		lastTouchY = eventY;
		if (id == 0 && MotionEvent.ACTION_UP == event.getAction()){
			Stroke stroke_up = _activeStrokes.get(id);
			//System.out.println("id is " + id);
			//stroke_up.save(db); //not saving to database, to make it faster
		}

		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (play_game){
			if (_has_selection){
				play_action_switch(event);
			}
			else {
				message("not allowed");
			}
		}
		else {
			if (!_has_selection){
				draw_action_switch(event);
			}
			else{
				select_action_switch(event);
			}
		}
		return true;
	}

	/**
	 * Called when replaying history to ensure the dirty region includes all
	 * points.
	 */
	private void expandDirtyRect(float historicalX, float historicalY) {
		if (historicalX < dirtyRect.left) {
			dirtyRect.left = historicalX;
		} else if (historicalX > dirtyRect.right) {
			dirtyRect.right = historicalX;
		}
		if (historicalY < dirtyRect.top) {
			dirtyRect.top = historicalY;
		} else if (historicalY > dirtyRect.bottom) {
			dirtyRect.bottom = historicalY;
		}
	}

	/**
	 * Resets the dirty region when the motion event occurs.
	 */
	private void resetDirtyRect(float eventX, float eventY) {

		// The lastTouchX and lastTouchY were set when the ACTION_DOWN
		// motion event occurred.
		dirtyRect.left = Math.min(lastTouchX, eventX);
		dirtyRect.right = Math.max(lastTouchX, eventX);
		dirtyRect.top = Math.min(lastTouchY, eventY);
		dirtyRect.bottom = Math.max(lastTouchY, eventY);
	}

	public void debug(String message){
		System.out.println(message);
	}

	public void message(String message){
		message(message, false);
	}
	public void message(String message, boolean length){
		fsa.message(message, length);
	}

	public void select(){
		reset();
		select(0);
	}

	public void move(int move_by){
		if (_allStrokes.size() == 0) {
			_selection_index = 0;
			return;
		}
		_selection_index = (_selection_index + _allStrokes.size() + move_by) % _allStrokes.size();	

	}
	public void reset(){
		_selection_index = _allStrokes.size() - 1;
	}

	public void select(int move_by) {
		// TODO Auto-generated method stub
		if (!_has_selection && move_by != 0) reset();

		if (_has_selection && move_by == 0) {
			_has_selection = false;
			_selectedStroke = null;
			//message("DeSelected!");
		}
		else {
			Stroke stroke = new Stroke(paint,0);
			if (_allStrokes.size() > 0) {
				move(move_by);
				stroke = _allStrokes.get(_selection_index);
				int id = stroke.getId();
				_selectedStroke = stroke;
				_has_selection = true;
				//message("Selected! id=" + _selection_index + "/" + _allStrokes.size(),true);
			}
			else {
				message("Draw something first!");
			}
		}
		invalidate();
	}

	public void init_parent(FullscreenActivity fullscreenActivity) {
		// TODO Auto-generated method stub
		fsa = fullscreenActivity; 
	}
}