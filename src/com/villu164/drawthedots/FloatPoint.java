package com.villu164.drawthedots;

public class FloatPoint {
	public float x;
	public float y;
	public int selected = 0;
	public FloatPoint(float x, float y){
		this.x = x;
		this.y = y;
	}
	public FloatPoint(float x, float y, int selected){
		this.x = x;
		this.y = y;
		this.selected = selected;
	}
	
	public float distance(FloatPoint fp) {
		float dx = (float)(fp.x - x);
		float dy = (float)(fp.y - y);
		float dist = (float)Math.sqrt(dx*dx + dy*dy);
		return dist;
	}
	
	public String toString(){
		return selected + "["+x + "," + y + "]";
	}
	
}
