package com.costumerental.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.costumerental.app.models.Costume;

import java.util.ArrayList;
import java.util.List;

// SQLite database helper for offline storage
public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "costume_rental.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table name
    private static final String TABLE_COSTUMES = "costumes";
    
    // Column names
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_SIZE = "size";
    private static final String COL_PRICE = "price";
    private static final String COL_IMAGE = "image";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create costumes table
        String createTable = "CREATE TABLE " + TABLE_COSTUMES + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_SIZE + " TEXT, " +
                COL_PRICE + " REAL, " +
                COL_IMAGE + " TEXT)";
        db.execSQL(createTable);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COSTUMES);
        onCreate(db);
    }
    
    // Save costume to local database
    public void saveCostume(Costume costume) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, costume.getId());
        values.put(COL_NAME, costume.getName());
        values.put(COL_SIZE, costume.getSize());
        values.put(COL_PRICE, costume.getPrice());
        values.put(COL_IMAGE, costume.getImage());
        
        // Insert or replace
        db.insertWithOnConflict(TABLE_COSTUMES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }
    
    // Save all costumes
    public void saveCostumes(List<Costume> costumes) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COSTUMES, null, null); // Clear existing data
        
        for (Costume costume : costumes) {
            ContentValues values = new ContentValues();
            values.put(COL_ID, costume.getId());
            values.put(COL_NAME, costume.getName());
            values.put(COL_SIZE, costume.getSize());
            values.put(COL_PRICE, costume.getPrice());
            values.put(COL_IMAGE, costume.getImage());
            db.insert(TABLE_COSTUMES, null, values);
        }
        db.close();
    }
    
    // Get all costumes from local database
    public List<Costume> getAllCostumes() {
        List<Costume> costumes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COSTUMES, null, null, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                Costume costume = new Costume();
                costume.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                costume.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
                costume.setSize(cursor.getString(cursor.getColumnIndexOrThrow(COL_SIZE)));
                costume.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE)));
                costume.setImage(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)));
                costumes.add(costume);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return costumes;
    }
}

