package com.villu164.drawthedots;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class Stroke {
    private Path _path;
    private Paint _paint;
    private int _id = -1;
    private boolean saved = false;
    private String save_name = null;
    private List<FloatPoint> _raw_path; //all elements that You need
    private List<FloatPoint> _selection_points = new ArrayList<FloatPoint>();; //all elements that You need

    public Stroke(List<FloatPoint> path_points, int group_id){
    	_paint = new Paint();
    	_paint.setColor(Color.BLACK);
    	_paint.setStyle(Paint.Style.STROKE);
     	_paint.setStrokeWidth(5f);
     	
    	Stroke _temp_stroke = new Stroke(_paint, group_id);
    	if (path_points != null) {
            for (FloatPoint fp: path_points) {
                if (fp != null) {
                	_temp_stroke.addPoint(fp);
                }
            }
        }
    	_path = _temp_stroke.getPath();
    	_temp_stroke = null;
    	_id = group_id;
    }
    
    public Stroke(DatabaseHandler db, int group_id){
    	List<FloatPoint> nfp = db.getPath(group_id);
    	Stroke _temp_stroke = new Stroke(_paint, group_id);
    	if (nfp != null) {
            for (FloatPoint fp: nfp) {
                if (fp != null) {
                	_temp_stroke.addPoint(fp);
                }
            }
        }
    	_paint = new Paint();
    	_paint.setColor(db.getPathColor(group_id));
    	_paint.setStyle(Paint.Style.STROKE);
     	_paint.setStrokeWidth(5f);
    	
    	_path = _temp_stroke.getPath();
    	_temp_stroke = null;
    	_id = group_id;
    }
    
    public Stroke(String save_id){
    	loadFrom(save_id); //allows the stroke to be created from the saved location
    }
    
    public Stroke(Paint paint, int id) {
        _paint = paint;
        _id = id;
        _raw_path = new ArrayList<FloatPoint>();
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
        _raw_path.add(new FloatPoint(x,y));
    }
    
    public void addPoint(FloatPoint fp) {
        addPoint(fp.x,fp.y);
    }
    
    public void saveAs(String save_id){
    	//this should save the stroke data
    	//_raw_path and _id to sql
    }    
    
    public void loadFrom(String save_id){
    	//this should load the necessary fields from the id
    }
    public void save(DatabaseHandler db){
    	//sql update
    	if (!saved) {
    		_id = db.getNextGroupId(_paint);
    		db.addPath(_raw_path, _id);
    		saved = true;
    	}
    }
    public void load(){
    	//sql reload
    }
    public List<FloatPoint> getFloatPoints(){
    	return _raw_path;
    }
    
    public void addToPath(FloatPoint fp){
    	FloatPoint min_fp = nearestPoint(fp,20);
    	if (min_fp != null) min_fp.toggle_select();
    }
    
    public FloatPoint nearestPoint(FloatPoint fp){
    	float min = 0;
    	FloatPoint min_fp = null;
    	for (FloatPoint line_fp: _raw_path) {
    		float compare = fp.distance(line_fp);
            if (min_fp == null){
            	min = compare;
            	min_fp = line_fp;
            }
            else {
            	if (compare < min) {
            		min = compare;
            		min_fp = line_fp;
            	}
            }
        }
    	return min_fp;
    }
    
    public FloatPoint nearestPoint(FloatPoint fp, float delta){
    	float min = 0;
    	float min_selected = 0;
    	FloatPoint min_fp = null;
    	FloatPoint min_selected_fp = null;
    	for (FloatPoint line_fp: _raw_path) {
    		float compare = fp.distance(line_fp, delta);
    		if (compare < 0) continue;
            if (min_fp == null){
            	min = compare;
            	min_fp = line_fp;
            }
            if (line_fp.selected && min_selected_fp == null){
            	min_selected = compare;
            	min_selected_fp = line_fp;
            }
            if (line_fp.selected){
                if (compare < min_selected) {
                	min_selected = compare;
                	min_selected_fp = line_fp;
            	}
            }
            else{
                if (compare < min) {
            		min = compare;
            		min_fp = line_fp;
            	}
            }
        }
    	if (min_selected_fp != null) return min_selected_fp;
    	return min_fp;
    }
    
    public String toString(){
    	String out = "";
    	if (_raw_path != null) {
            for (FloatPoint fp: _raw_path) {
                if (fp != null) {
                	out += fp.toString();
                }
            }
        }
    	return out;
    }
    
}