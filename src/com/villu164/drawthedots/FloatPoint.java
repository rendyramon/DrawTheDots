package com.villu164.drawthedots;

public class FloatPoint {
	public float x;
	public float y;
	public boolean selected = false;
	public FloatPoint(float x, float y){
		this.x = x;
		this.y = y;
	}
	public FloatPoint(float x, float y, boolean selected){
		this.x = x;
		this.y = y;
		this.selected = selected;
	}
	
	public void select(){
		selected = true;
	}
	
	public void deselect(){
		selected = false;
	}

	public void set_selected(int i){
		if (i == 0) deselect();
		else select();
	}
	
	public void set_selected(boolean b){
		selected = b;
	}
	
	public int selected_int(){
		if (selected) return 1;
		return 0;
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
