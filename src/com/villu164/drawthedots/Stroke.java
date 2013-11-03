package com.villu164.drawthedots;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class Stroke {
    private Path _path;
    private Paint _paint;
    private int _id = -1;

    public Stroke (Paint paint, int id) {
        _paint = paint;
        _id = id;
    }

    public Path getPath() {
        return _path;
    }

    public Paint getPaint() {
        return _paint;
    }
    
    public int getId(){
    	return _id;
    }

    public void addPoint(Point pt) {
        if (_path == null) {
            _path = new Path();
            _path.moveTo(pt.x, pt.y);
        } else {
            _path.lineTo(pt.x, pt.y);
        }
    }
    
    public void addPoint(float x, float y) {
        if (_path == null) {
            _path = new Path();
            _path.moveTo(x, y);
        } else {
            _path.lineTo(x, y);
        }
        
    }
}