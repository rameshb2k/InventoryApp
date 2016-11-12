package com.example.android.inventoryapp.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.inventoryapp.R;


/**
 * Created by Ramesh on 11/9/2016.
 */

public class InventoryContract {
    private InventoryContract() {}

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_NAME = "name";
        public static final String COLUMN_INVENTORY_PRICE = "price";
        public static final String COLUMN_INVENTORY_IMAGE = "image";
        public static final String COLUMN_INVENTORY_WEIGHT = "weight";
        public static final String COLUMN_INVENTORY_SUPPLIER= "supplier";
        public static final String COLUMN_INVENTORY_EMAIL="email";

        public static final int IMAGE_DEFAULT = 0;
        public static final int IMAGE_APPLE = 1;
        public static final int IMAGE_AVOCADO = 2;
        public static final int IMAGE_BANNANA = 3;
        public static final int IMAGE_BROCCOLI = 4;
        public static final int IMAGE_EGG = 5;
        public static final int IMAGE_GRAPES = 6;
        public static final int IMAGE_PEPPERS = 7;
        public static final int IMAGE_STRAWBERRIES = 8;
        public static final int IMAGE_TOMATO = 9;


        public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER NOT NULL, " +
                InventoryEntry.COLUMN_INVENTORY_IMAGE + " INTEGER, " +
                InventoryEntry.COLUMN_INVENTORY_WEIGHT + " INTEGER NOT NULL DEFAULT 0, " +
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER + " TEXT NOT NULL, " +
                InventoryEntry.COLUMN_INVENTORY_EMAIL + " TEXT NOT NULL" + ");";

        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME + " );";
    }

}
