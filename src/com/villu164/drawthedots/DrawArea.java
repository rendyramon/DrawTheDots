package com.villu164.drawthedots;

import android.content.Context;
import android.graphics.*;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrawArea extends View {

    private List<Stroke> _allStrokes; //all strokes that need to be drawn
    private SparseArray<Stroke> _activeStrokes; //use to retrieve the currently drawn strokes
    private Random _rdmColor = new Random();

    public DrawArea(Context context) {
        super(context);
        _allStrokes = new ArrayList<Stroke>();
        _activeStrokes = new SparseArray<Stroke>();
        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);
    }

    public void onDraw(Canvas canvas) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final int pointerCount = event.getPointerCount();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                pointDown((int)event.getX(), (int)event.getY(), event.getPointerId(0));
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int pc = 0; pc < pointerCount; pc++) {
                    pointMove((int) event.getX(pc), (int) event.getY(pc), event.getPointerId(pc));
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                for (int pc = 0; pc < pointerCount; pc++) {
                    pointDown((int)event.getX(pc), (int)event.getY(pc), event.getPointerId(pc));
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                break;
            }
        }
        invalidate();
        return true;
    }

    private void pointDown(int x, int y, int id) {
        //create a paint with random color
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setColor(_rdmColor.nextInt());

        //create the Stroke
        Point pt = new Point(x, y);
        Stroke stroke = new Stroke(paint, id);
        stroke.addPoint(pt);
        _activeStrokes.put(id, stroke);
        _allStrokes.add(stroke);
    }

    private void pointMove(int x, int y, int id) {
        //retrieve the stroke and add new point to its path
        Stroke stroke = _activeStrokes.get(id);
        if (stroke != null) {
            Point pt = new Point(x, y);
            stroke.addPoint(pt);
        }
    }
}