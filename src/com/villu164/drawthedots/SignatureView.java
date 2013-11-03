package com.villu164.drawthedots;
//originally taken from http://corner.squareup.com/2010/07/smooth-signatures.html
//
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;

public class SignatureView extends View {
	private static final float STROKE_WIDTH = 5f;
	private static final boolean DEBUG = true;
	  /** Need to track this so the dirty region can accommodate the stroke. **/
	  private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

	  private Paint paint = new Paint();
	  private Path path = new Path();
	  //private View contentView;
	  
	  private List<Stroke> _allStrokes; //all strokes that need to be drawn
	  private SparseArray<Stroke> _activeStrokes; //use to retrieve the currently drawn strokes
	  private Random _rdmColor = new Random();

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
	    //System.out.println(path.);
		  
	  }

	  
	  @Override
	  protected void onDraw(Canvas canvas) {
		  boolean choose = true;
		  if (choose) {
			  if (_allStrokes != null) {
	            for (Stroke stroke: _allStrokes) {
	                if (stroke != null) {
	                    Path path = stroke.getPath();
	                    Paint painter = stroke.getPaint();
	                    if ((path != null) && (painter != null)) {
	                        canvas.drawPath(path, painter);
	                    }
	                }
	            }
	        }
		  }
		  else {
			  canvas.drawPath(path, paint);
		  }
	  }

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    float eventX = event.getX();
	    float eventY = event.getY();
	    //int x = (int)eventX;
	    //int y = (int)eventY;
	    int id = event.getPointerId(0);
	    //if (DEBUG) System.out.println("Registered: " + event.toString());
	    switch (event.getAction()) {
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
	}