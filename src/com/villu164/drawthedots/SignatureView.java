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

	private Paint paint = new Paint();
	private Path path = new Path();
	//private View contentView;

	private List<Stroke> _allStrokes; //all strokes that need to be drawn
	private SparseArray<Stroke> _activeStrokes; //use to retrieve the currently drawn strokes
	private Stroke _selectedStroke = null;
	private boolean _has_selection = false;
	private Random _rdmColor = new Random();

	private Paint selected_paint = new Paint();

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

	private void init_strokes(){
		_allStrokes = new ArrayList<Stroke>();
		_activeStrokes = new SparseArray<Stroke>();
	}

	public void init_db(DatabaseHandler db){
		this.db = db;
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
		invalidate();
	}


	/**
	 * Creates the dots on the last stroke
	 */
	public void make_dots() {
		db.getGroupIds();
		for (int gpid: db.getGroupIds()) {
			List<FloatPoint> nfp = db.getPath(gpid);
			Stroke new_stroke = new Stroke(db,gpid); 
			_allStrokes.add(new_stroke);
			invalidate();
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		boolean choose = true;
		if (choose) {
			if (_allStrokes != null) {
				for (Stroke stroke: _allStrokes) {
					if (stroke != null) {
						Path path = stroke.getPath();
						int id = stroke.getId();
						Paint painter = stroke.getPaint();
						if ((path != null) && (painter != null)) {
							canvas.drawPath(path, painter);
							selected_paint.setStyle(Paint.Style.FILL);
							selected_paint.setColor(Color.BLACK);
							selected_paint.setStrokeWidth(stroke.getPaint().getStrokeWidth());
							List<FloatPoint> points = stroke.getFloatPoints();
							for (FloatPoint fp: points) {
								canvas.drawCircle(fp.x, fp.y, 3, selected_paint);
							}
						}
					}
				}
			}
		}
		else {
			canvas.drawPath(path, paint);
			//canvas.draw
		}
		if (_has_selection) {
			float cx = (float)200.4;
			float cy = (float)200.3;
			Paint new_paint = _selectedStroke.getPaint();
			//selected_paint.setColor(new_paint.getColor());
			selected_paint.setColor(Color.RED);
			selected_paint.setStrokeWidth(new_paint.getStrokeWidth());
			selected_paint.setStyle(Paint.Style.FILL);
			List<FloatPoint> points = _selectedStroke.getSelectedPoints();
			for (FloatPoint fp: points) {
				canvas.drawCircle(fp.x, fp.y, 3, selected_paint);
			}
			if (points.size() == 0) message("No points yet");
			//canvas.drawCircle(cx, cy, 30, new_paint);
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//in the future, consider making the touch to a circle or something OR double-click draw would be a circle
		//take the dirtyRect implementation from the previous version and use it to smooth the lines -- Later
		float eventX = event.getX(0);
		float eventY = event.getY(0);
		int action = event.getAction();
		//int x = (int)eventX;
		//int y = (int)eventY;
		int id = event.getPointerId(0);
		if (!_has_selection){

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
				stroke.addPoint(eventX, eventY);
				_activeStrokes.put(id, stroke);
				_allStrokes.add(stroke);
				// There is no end point yet, so don't waste cycles invalidating.
				return true;

			case MotionEvent.ACTION_MOVE:
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
						stroke_move.addPoint(historicalX, historicalY);
					}
				}

				// After replaying history, connect the line to the touch point.
				path.lineTo(eventX, eventY);
				if (stroke_move != null) {
					//Point pt_move = new Point(x, y);
					stroke_move.addPoint(eventX, eventY);
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
				System.out.println("id is " + id);
				stroke_up.save(db);
			}
		}
		else{
			switch(action){
			case MotionEvent.ACTION_DOWN:
				//message("Cannot draw, selection is active!");
				//_selectedStroke.getSelectedPoints().add(new FloatPoint(eventX,eventY));
				_selectedStroke.addToPath(new FloatPoint(eventX,eventY));
				invalidate();
				break;
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

	public void message(String message){
		message(message, false);
	}
	public void message(String message, boolean length){
		fsa.message(message, length);
	}

	public void select() {
		// TODO Auto-generated method stub
		if (_has_selection) {
			_has_selection = false;
			_selectedStroke = null;
			message("DeSelected!");
		}
		else {
			Stroke stroke = new Stroke(paint,0);
			if (_allStrokes.size() > 0) {
				stroke = _allStrokes.get(_allStrokes.size() - 1);
				int id = stroke.getId();
				_selectedStroke = stroke;
				_has_selection = true;
				message("Selected! id=" + id,true);
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