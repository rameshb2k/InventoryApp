package com.example.android.inventoryapp.data;

/**
 * Created by Ramesh on 11/9/2016.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.SQL_CREATE_ENTRIES;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.SQL_DELETE_ENTRIES;



public class InventoryDbHelper extends SQLiteOpenHelper {

    //database name and version
    public static final int INV_DATABASE_VERSION = 1;
    public static final String INV_DATABASE_NAME = "test.db";

    //Constructor
    public InventoryDbHelper(Context invContext) {
        super(invContext,INV_DATABASE_NAME, null, INV_DATABASE_VERSION);
    }
    //When the database if first created
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    //upgrade database schema: delete table entries and recreate the database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}
