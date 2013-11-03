package com.villu164.drawthedots;
//got this from http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
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
import android.graphics.Paint;
import android.graphics.Path;
 
public class DatabaseHandler extends SQLiteOpenHelper {
 
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1; //if you change the structure, change this number +1
 
    // Database Name
    private static final String DATABASE_NAME = "PathsManager";
 
    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts"; //remove later
    private static final String TABLE_PATHS = "paths";
    private static final String TABLE_PATH_COLLECTIONS = "path_collections";
 
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
                + KEY_WIDTH + " INTEGER,"
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
        db.close();
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
        
        Cursor cursor = db.query(TABLE_PATHS, new String[] { KEY_ID,
        		KEY_SELECTED, KEY_X, KEY_Y }, KEY_GROUP_ID + "=?",
                new String[] { String.valueOf(group_id) }, null, null, null, null);
 
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
            	FloatPoint fp = new FloatPoint(cursor.getFloat(1) + 1,cursor.getFloat(2));
        		fp.selected = cursor.getInt(3);
                // Adding contact to list
                floatPointList.add(fp);
        	} while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return path
        return floatPointList;
    }
    // Getting paths Count
    public int getPathsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_PATHS;
        SQLiteDatabase db = this.getWritableDatabase();
        //.println(db.isOpen());
        Cursor cursor = db.rawQuery(countQuery, null);
        int pathsCount = cursor.getCount(); 
        cursor.close();
        db.close();
        // return count
        return pathsCount; 
    }
    public int getNextGroupId() {
        String maxIdQuery = "SELECT coalesce(max(" + KEY_GROUP_ID + "),0) as max_group_id FROM " + TABLE_PATHS;
        //System.out.println(maxIdQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        //System.out.println(db.isOpen());
        Cursor cursor = db.rawQuery(maxIdQuery, null);
        int maxGroupId = 0;
        if (cursor.moveToFirst())
        {
            do
            {           
            	maxGroupId = cursor.getInt(0);                  
            } while(cursor.moveToNext());           
        }
        cursor.close();
        db.close();
        // return count
        return maxGroupId + 1; 
    }
}