package com.villu164.drawthedots;
//got this from http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
// adb pull /data/data/com.villu164.drawthedots/databases
// add export PATH=${PATH}:~/android/adt/sdk/platform-tools to ~/.bashrc
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
 
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2; //if you change the structure, change this number +1
 
    // Database Name
    private static final String DATABASE_NAME = "PathsManager";
 
    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts"; //remove later
    private static final String TABLE_PATHS = "paths";
    private static final String TABLE_PATH_COLLECTIONS = "path_collections";
    
    private static final boolean DEBUG = false;
 
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NO = "phone_number";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_PATH_ID = "path_id";
    private static final String KEY_GROUP_ID = "group_id";
    private static final String KEY_PATH_COLLECTION_ID = "path_collection_id";
    private static final String KEY_SELECTED = "selected";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_STYLE = "style";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_COLOR = "color"; //String.valueOf(color.getRGB())
    
    void debug(String text){
    	if (!DEBUG) return;
    	System.out.println(text);
    }
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        //String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_PH_NO + " TEXT" + ")";
        
        String CREATE_TABLE_PATHS = "CREATE TABLE " + TABLE_PATHS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
        		+ KEY_SELECTED + " INTEGER,"
        		+ KEY_GROUP_ID + " INTEGER,"
                + KEY_X + " REAL,"
        		+ KEY_Y + " REAL"
                + ")"; //dont forget to remove comma
        
        String CREATE_TABLE_PATH_COLLECTIONS = "CREATE TABLE " + TABLE_PATH_COLLECTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
        		+ KEY_PATH_ID + " INTEGER,"
        		+ KEY_GROUP_ID + " INTEGER,"
                + KEY_STYLE + " INTEGER,"
                + KEY_WIDTH + " REAL,"
                + KEY_COLOR + " TEXT"
                + ")"; //dont forget to remove comma
        
        
        //db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL(CREATE_TABLE_PATHS);
        db.execSQL(CREATE_TABLE_PATH_COLLECTIONS);
    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATHS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATH_COLLECTIONS);
        // Create tables again
        onCreate(db);
    }
 
    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new path
    public void addPath(List<FloatPoint> path_points, int group_id) {
    	
        SQLiteDatabase db = this.getWritableDatabase();
		
		if (path_points != null) {
            for (FloatPoint fp: path_points) {
                if (fp != null) {
                	// Inserting Row
                	ContentValues values = new ContentValues();
                    values.put(KEY_SELECTED, fp.selected); // is path selected
                    values.put(KEY_GROUP_ID, group_id); // is path selected
                    values.put(KEY_X, fp.x); // is path selected
                    values.put(KEY_Y, fp.y); // is path selected
                    db.insert(TABLE_PATHS, null, values);
                }
            }
        }
        
        db.close(); // Closing database connection
    }
    
    // Deleting existing path
    public void deletePath(int group_id) {
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PATHS, KEY_GROUP_ID + " = ?", new String[] { String.valueOf(group_id) });
        db.delete(TABLE_PATH_COLLECTIONS, KEY_ID + " = ?", new String[] { String.valueOf(group_id) });
        db.close();
    }
    
    public void deleteAllPaths(){
		  for (int gpid: getGroupIds()) {
			  deletePath(gpid);
		  }
    }
    
    // Updating existing path
    // change if performance starts to be an issue
    public void updatePath(List<FloatPoint> path_points, int group_id) {
    	deletePath(group_id);
    	addPath(path_points, group_id);
    }
    
    // Get existing path
    public List<FloatPoint> getPath(int group_id){
    	List<FloatPoint> floatPointList = new ArrayList<FloatPoint>();
        SQLiteDatabase db = this.getReadableDatabase();
        //KEY_SELECTED, KEY_X, KEY_Y }, KEY_GROUP_ID + "=? or true",
        Cursor cursor = db.query(TABLE_PATHS, new String[] { KEY_ID,
        		KEY_SELECTED, KEY_X, KEY_Y, KEY_GROUP_ID }, KEY_GROUP_ID + "=?",
                new String[] { String.valueOf(group_id) }, null, null, null, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	int id = cursor.getInt(0);
            	float selected = cursor.getFloat(1);
            	float xx = cursor.getFloat(2);
            	float yy = cursor.getFloat(3);
            	int groupy_id = cursor.getInt(4);
            	debug(groupy_id + "/" + id + ":" + xx + ";" + yy + "  " + selected);
            	FloatPoint fp = new FloatPoint(xx + 10,yy + 10);
        		fp.selected = cursor.getInt(3);
                
                floatPointList.add(fp);
        	} while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return path
        return floatPointList;
    }
    
 // Get existing path
    public List<Integer> getGroupIds(){
    	List<Integer> gpids = new ArrayList<Integer>();
        SQLiteDatabase db = this.getReadableDatabase();
        //KEY_SELECTED, KEY_X, KEY_Y }, KEY_GROUP_ID + "=? or true",
        //Cursor cursor = db.query(TABLE_PATHS, new String[] { KEY_GROUP_ID }, KEY_GROUP_ID + "=?",
        //        new String[] { String.valueOf(group_id) }, null, null, null, null);
        String groupIdsQuery = "SELECT distinct " + KEY_GROUP_ID + " as group_id FROM " + TABLE_PATHS;
        Cursor cursor = db.rawQuery(groupIdsQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	int groupy_id = cursor.getInt(0);
            	gpids.add(groupy_id);
            	debug(groupy_id + " as groupid");
            	
        	} while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return path
        //return floatPointList;
        return gpids;
    }
    
    // Getting paths Count
    public int getPathsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PATH_COLLECTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        //.println(db.isOpen());
        Cursor cursor = db.rawQuery(countQuery, null);
        int pathsCount = cursor.getCount(); 
        cursor.close();
        db.close();
        // return count
        return pathsCount; 
    }
    
    public int getPathColor(int group_id) {
        String colorQuery = "SELECT " + KEY_COLOR + " FROM " + TABLE_PATH_COLLECTIONS + " WHERE " + KEY_ID + " = " + group_id;
        debug("getPathColor(" + group_id + ") " + colorQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        //.println(db.isOpen());
        Cursor cursor = db.rawQuery(colorQuery, null);
        int pathColor = Color.BLACK;
        if (cursor != null){
        	cursor.moveToFirst();
        	pathColor = cursor.getInt(0);
            try {
            	pathColor = cursor.getInt(0);
            } catch(Exception e) {
            	debug(e.toString());
            }

        }
        cursor.close();
        db.close();
        // return color
        return pathColor;
    }
    
    public int getNextGroupId(Paint paint) {
        String maxIdQuery = "SELECT coalesce(max(" + KEY_ID + "),0) as max_group_id FROM " + TABLE_PATH_COLLECTIONS;
        //System.out.println(maxIdQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues group_values = new ContentValues();
		group_values.put(KEY_COLOR, paint.getColor()); // is path selected
		group_values.put(KEY_WIDTH, paint.getStrokeWidth()); // is path selected
		int group_id = (int)db.insert(TABLE_PATH_COLLECTIONS, null, group_values);
		debug("saved color " + paint.getColor() + " to gpid " + group_id);
		return group_id;
    }
    
    
}