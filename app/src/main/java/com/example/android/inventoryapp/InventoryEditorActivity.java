package com.example.android.inventoryapp;

/**
 * Created by Ramesh on 11/9/2016.
 */
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;


import org.w3c.dom.Text;

import static android.R.attr.duration;
import static android.R.id.message;
import static com.example.android.inventoryapp.R.id.increase_stock;
import static com.example.android.inventoryapp.R.id.price;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.IMAGE_DEFAULT;


public class InventoryEditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    /**
     * EditText field to enter the Inventory Product name
     */
    private EditText mNameEditText;


    /**
     * EditText field to enter the Product Price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the Inventory weight
     */
    private EditText mWeightEditText;
    private TextView mWeightText;

    /**
     * EditText field to enter the Product Image
     */
    private Spinner mImageSpinner;

    //Supplier name & email
    private EditText mSupplierEditText;
    private EditText mEmailEditText;

    private ImageView mProductImage;


    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mImageId = 0;
    private int mImageResourceId = 0;//No Image


    Uri editorUri;

    String[] projection = { InventoryContract.InventoryEntry._ID,
            COLUMN_INVENTORY_NAME,
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE,
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE,
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_WEIGHT,
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER,
            InventoryContract.InventoryEntry.COLUMN_INVENTORY_EMAIL};
    String selection;
    String[] selectionArgs;
    int rowsDeleted = 0;

    //check if changes were made by adding a OnTouchListener
    private boolean mInventoryHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    //
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventoy item.
                Log.v("showDeleteConfirmation", "DELETE");
                deleteInventoryItem();
                finish();
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

    /**
     * Perform the deletion of the Inventory Item in the database.
     */
    private void deleteInventoryItem() {
        if (editorUri != null) {
            Log.v("deleteInventoryItem", editorUri.toString());
            rowsDeleted = 0;
            selection = InventoryEntry._ID;//+ "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(editorUri))};
            rowsDeleted = getContentResolver().delete(editorUri, selection, selectionArgs);
            // Show a toast message depending on whether or not the deletion was successful
            if (rowsDeleted == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Delete Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Inventory Item Deleted",
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }


    //method for creating a “Discard changes” dialog
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //Hook up the back button
    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    //onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_editor);

        //getIntent  //getData()
        editorUri = getIntent().getData();
        if (editorUri == null) {
            Log.v("InventoryEditorActivity", "editorUri is null");
            setTitle("Add Inventory Item");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Inventory Item");
            Log.v("InventoryEditorActivity", editorUri.toString());
            getSupportLoaderManager().restartLoader(1, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_inventory_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_inventory_price);
        mWeightEditText = (EditText) findViewById(R.id.modify_stock_by);
        mWeightText = (TextView) findViewById(R.id.edit_stock);
        mImageSpinner = (Spinner) findViewById(R.id.spinner_image);
        mSupplierEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mEmailEditText = (EditText) findViewById(R.id.edit_supplier_email);
        mProductImage = (ImageView) findViewById(R.id.product_image);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mImageSpinner.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
        //Order Button
        Button orderButton = (Button) findViewById(R.id.order_button);
        final String nameString = mNameEditText.getText().toString().trim();
        final String emailString = mEmailEditText.getText().toString().trim();
        final String supplierString = mSupplierEditText.getText().toString().trim();
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        supplierString,emailString, null));
                intent.putExtra(Intent.EXTRA_SUBJECT, nameString);
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(intent, "Choose an Email client :"));

            }
        });
        //Increase Stock
        Button increaseStockButton = (Button) findViewById(R.id.increase_stock);
        increaseStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightString = mWeightText.getText().toString().trim();
                String weightEditString = mWeightEditText.getText().toString().trim();
                int weightEditInt = 0;
                if (TextUtils.isEmpty(weightEditString)) {
                    weightEditInt = 0;
                } else {
                    weightEditInt = Integer.parseInt(weightEditString);
                }
                int weightInt = 0;
                if (TextUtils.isEmpty(weightString)) {
                    weightInt = 0;
                } else {
                    weightInt = Integer.parseInt(weightString);
                }
                weightInt = weightInt + weightEditInt;

                mWeightText.setText(String.valueOf(weightInt));//Set Stock to updated value

            }
        });

        //Decrease Stock
        Button decreaseStockButton = (Button) findViewById(R.id.decrease_stock);
        decreaseStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightString = mWeightText.getText().toString().trim();
                String weightEditString = mWeightEditText.getText().toString().trim();
                int weightEditInt = 0;
                if (TextUtils.isEmpty(weightEditString)) {
                    weightEditInt = 0;
                } else {
                    weightEditInt = Integer.parseInt(weightEditString);
                }
                int weightInt = 0;
                if (TextUtils.isEmpty(weightString)) {
                    weightInt = 0;
                } else {
                    weightInt = Integer.parseInt(weightString);
                }
                weightInt = weightInt - weightEditInt;
                if (weightInt < 0) {weightInt = 0;}

                mWeightText.setText(String.valueOf(weightInt));//Set Stock to updated value
            }
        });

    }//oncreate

    /**
     * Setup the dropdown spinner that allows the user to select the image of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter imageSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_image_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        imageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mImageSpinner.setAdapter(imageSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mImageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               String selection = (String) parent.getItemAtPosition(position);

                mImageResourceId = position;
                int imageResourceId = 0;
                switch (mImageResourceId) {
                    case 0: break;
                    case 1: imageResourceId = R.drawable.ic_apple; break;
                    case 2: imageResourceId = R.drawable.ic_avocado; break;
                    case 3: imageResourceId = R.drawable.ic_banana; break;
                    case 4: imageResourceId = R.drawable.ic_broccoli; break;
                    case 5: imageResourceId = R.drawable.ic_egg; break;
                    case 6: imageResourceId = R.drawable.ic_grapes; break;
                    case 7: imageResourceId = R.drawable.ic_peppers; break;
                    case 8: imageResourceId = R.drawable.ic_strawberries; break;
                    case 9: imageResourceId = R.drawable.ic_tomato; break;
                    default: break;
                }
                mProductImage.setImageResource(imageResourceId);

            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mImageResourceId = 0; // Unknown
            }
        });
    }


//Get user input from the editor and save new inventory item into database

    private void saveInventory() {


        Log.v("SaveInventory", "editorUri not checked yet");
        InventoryDbHelper mDbHelper = new InventoryDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String weightString = mWeightText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        int weightInt = 0;

        if ((TextUtils.isEmpty(nameString)) &&
                (TextUtils.isEmpty(priceString)) &&
                (TextUtils.isEmpty(weightString)) &&
                (TextUtils.isEmpty(supplierString)) &&
                (TextUtils.isEmpty(emailString)) &&
                ( mImageResourceId == 0)) { return; }

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "Item should have name",Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Item should have price",Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(weightString)) {
            weightInt = 0;
        } else {
            weightInt = Integer.parseInt(weightString);
        }

        //create content values
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, nameString);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_INVENTORY_WEIGHT, weightInt);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER, supplierString);
        values.put(InventoryEntry.COLUMN_INVENTORY_EMAIL, emailString);
        values.put(InventoryEntry.COLUMN_INVENTORY_IMAGE,mImageResourceId );;

        Log.v("SaveInventory", "editorUri not checked yet");


        if (editorUri == null) {
            Log.v("SaveInventory", "editorUri is null");
            Uri newRowUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            Log.v("SaveInventory", "newRowUri");
            // Show a toast message depending on whether or not the insertion was successful
            if (newRowUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_inventory_successful),
                        Toast.LENGTH_SHORT).show();
                editorUri = newRowUri;
                getSupportLoaderManager().restartLoader(1, null, this);
            }

            long newRowId = ContentUris.parseId(newRowUri);
            Log.v("EditorActivity", "New row ID " + newRowUri.toString());
            Log.v("EditorActivity", "New row ID " + newRowId);
            if (newRowId != -1) {
                Toast.makeText(this, "Inventory Item saved with id: " + newRowId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error with saving Inventory Item", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = 0;
            selection = InventoryEntry._ID;//+ "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(editorUri))};
            rowsUpdated = getContentResolver().update(editorUri, values, selection, selectionArgs);


            // Show a toast message depending on whether or not the insertion was successful
            if (rowsUpdated == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Updated Inventory Item Failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Inventory Item Updated",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new iem, hide the "Delete" menu item.
        if (editorUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Do nothing for now
                saveInventory();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now

                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        /* Takes action based on the ID of the Loader that's being created
                */

        switch (id) {
            case 1:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,   // Parent activity context
                        editorUri,        // Table to query
                        projection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (rowsDeleted == 0) {
            data.moveToNext();
            String name = data.getString(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
            int price = data.getInt(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));
            int imageId = data.getInt(data.getColumnIndexOrThrow(InventoryEntry.COLUMN_INVENTORY_IMAGE));
            int weight = data.getInt(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_WEIGHT));
            String supplier = data.getString(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_SUPPLIER));
            String email = data.getString(data.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_EMAIL));

            mNameEditText.setText(name);
            mPriceEditText.setText(String.valueOf(price));
            Log.v("onLoadFinished", String.valueOf(weight));
            mWeightText.setText(String.valueOf(weight));
            mImageSpinner.setSelection(imageId);
            mSupplierEditText.setText(supplier);
            mEmailEditText.setText(email);
            int imageResourceId = 0;
            switch (imageId) {
                case 0: break;
                case 1: imageResourceId = R.drawable.ic_apple; break;
                case 2: imageResourceId = R.drawable.ic_avocado; break;
                case 3: imageResourceId = R.drawable.ic_banana; break;
                case 4: imageResourceId = R.drawable.ic_broccoli; break;
                case 5: imageResourceId = R.drawable.ic_egg; break;
                case 6: imageResourceId = R.drawable.ic_grapes; break;
                case 7: imageResourceId = R.drawable.ic_peppers; break;
                case 8: imageResourceId = R.drawable.ic_strawberries; break;
                case 9: imageResourceId = R.drawable.ic_tomato; break;
                default: break;
            }
            mProductImage.setImageResource(imageResourceId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText(null);
        mPriceEditText.setText(String.valueOf(0));
        mWeightText.setText(String.valueOf(0));
        mSupplierEditText.setText(null);
        mEmailEditText.setText(null);

    }

}
