package com.example.android.inventoryapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import static android.content.ContentUris.withAppendedId;
import static com.example.android.inventoryapp.data.InventoryContract.BASE_CONTENT_URI;
import static com.example.android.inventoryapp.data.InventoryContract.PATH_INVENTORY;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.CONTENT_URI;


/**
 * Created by Ramesh on 11/9/2016.
 */

public class InventoryActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    InventoryCursorAdapter inventoryAdapter;
    String[] projection = {InventoryEntry._ID,
            InventoryEntry.COLUMN_INVENTORY_NAME,
            InventoryEntry.COLUMN_INVENTORY_PRICE,
            InventoryEntry.COLUMN_INVENTORY_IMAGE,
            InventoryEntry.COLUMN_INVENTORY_WEIGHT,
            InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
            InventoryEntry.COLUMN_INVENTORY_EMAIL};
    int numRowsDeleted;
    private InventoryDbHelper mDbHelper;
    //String selection = InventoryEntry.COLUMN_INVENTORY_IMAGE + "=?";
    //String [] selectionArgs = new String[] { String.valueOf(InventoryEntry.IMAGE_BANNANA) };
    private Cursor cursor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        final Intent editorIntent = new Intent(InventoryActivity.this, InventoryEditorActivity.class);

        // Setup FAB to open InventoryEditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("InventoryActivity", "fab floating action button");
                editorIntent.setData(null);
                startActivity(editorIntent);
            }
        });

        mDbHelper = new InventoryDbHelper(this);

        inventoryAdapter = new InventoryCursorAdapter(this, cursor);

        ListView inventoryListView = (ListView) findViewById(R.id.List);
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        inventoryListView.setAdapter(inventoryAdapter);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                final Uri contentUri = ContentUris.withAppendedId(CONTENT_URI, id);
                editorIntent.setData(contentUri);
                Log.v("InventoryListView Click", contentUri.toString());
                startActivity(editorIntent);
            }
        });


        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getSupportLoaderManager().restartLoader(0, null, this);
    }


    //Insert Dummy Inventory
    private void insertInventory() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //create content values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, "Dummy Item");
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, 3);
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE, 1);
        values.put(InventoryEntry.COLUMN_INVENTORY_WEIGHT, 150);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, "ABC PRODUCE COMPANY");
        values.put(InventoryEntry.COLUMN_INVENTORY_EMAIL, "abc_produce@gmail.com");

        Log.v("InventoryActivity", "Image :  " + String.valueOf(R.drawable.ic_banana));

        Uri newRowUri = getContentResolver().insert(CONTENT_URI, values);

        Long newRowId = ContentUris.parseId(newRowUri);
        Log.v("InventoryActivity", "New row ID " + newRowUri.toString());
        Log.v("InventoryActivity", "New row ID " + newRowId);
        if (newRowId != -1) {
            Toast.makeText(this, "Inventory saved with id: " + newRowId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error with saving Inventory", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertInventory();
                //displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
               // numRowsDeleted = getContentResolver().delete(CONTENT_URI, null, null);
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory item.
                Log.v("showDeleteConfirmation", "DELETE");

                numRowsDeleted = getContentResolver().delete(CONTENT_URI, null, null);

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory item.
                if (dialog != null) {
                    Log.v("showDeleteConfirmation", "CANCEL");
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
     /*
     * Takes action based on the ID of the Loader that's being created
     */

        switch (id) {
            case 0:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        CONTENT_URI,        // Table to query
                        projection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments9
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        inventoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        inventoryAdapter.swapCursor(null);
    }

}
