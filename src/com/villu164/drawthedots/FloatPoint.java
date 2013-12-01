package com.villu164.drawthedots;

public class FloatPoint {
	public float x;
	public float y;
	private boolean selected = false;
	public FloatPoint(float x, float y, boolean selected){
		this.x = x;
		this.y = y;
		if (selected) select();
	}
	
	public FloatPoint(float x, float y, float selected){
		this.x = x;
		this.y = y;
		if (selected > 0) {
			select();
			System.out.println("Setting: " + this.toString() + " to true");
		}
	}
	
	
	public void select(){
		selected = true;
	}
	

	public void toggle_select(){
		selected = !selected;
	}

	public boolean get_selected(){
		if (selected) {
			System.out.println("TRUETRUETRUE: " + this.toString());
			return true;
		}
		return false;
	}
	public float get_selected_int(){
		if (selected) {
			System.out.println("TRUETRUETRUE: " + this.toString());
			return (float)1.0;
		}
		return (float)0;
	}
	
	public float distance(FloatPoint fp) {
		float dx = (float)(fp.x - x);
		float dy = (float)(fp.y - y);
		float dist = (float)Math.sqrt(dx*dx + dy*dy);
		return dist;
	}
	
	//allowing biggest delta difference between points to make the algo faster
	//returning -1 if out of range
	public float distance(FloatPoint fp, float delta) {
		float dx = (float)(fp.x - x);
		if (Math.abs(dx) > delta) return -1;
		float dy = (float)(fp.y - y);
		if (Math.abs(dy) > delta) return -1;
		float dist = (float)Math.sqrt(dx*dx + dy*dy);
		return dist;
	}
	
	public String toString(){
		return selected + "["+x + "," + y + "]";
	}
	
}
