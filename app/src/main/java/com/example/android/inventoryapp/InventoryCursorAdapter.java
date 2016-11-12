package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

import static android.R.attr.id;
import static com.example.android.inventoryapp.R.id.sale;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry.CONTENT_URI;
import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry._ID;

/**
 * Created by Ramesh on 11/9/2016.
 */

public class InventoryCursorAdapter extends CursorAdapter {


    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //  Fill out this method and return the list item view (instead of null)
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //  Fill out this method
        // Find fields to populate in inflated template
        TextView inventoryName = (TextView) view.findViewById(R.id.name);
        TextView inventoryWeight = (TextView) view.findViewById(R.id.summary);
        TextView inventoryPrice = (TextView) view.findViewById(R.id.price);
        ImageView inventoryImage = (ImageView) view.findViewById(R.id.product_image);

        final int currentPosition = cursor.getPosition();
        final int mCount = cursor.getCount();

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_NAME));
        int weight = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_WEIGHT));
        int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_IMAGE));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_INVENTORY_PRICE));

        //SALE BUTTON
        final String salename = name;
        final int saleWeight = weight;

        final int cursorId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID));
        Button saleButton = (Button) view.findViewById(sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                   Log.v("CURRENT POSITION", String.valueOf(currentPosition));
                    Log.v("CURRENT COUNT", String.valueOf(mCount));
                    Log.v("CURRENT_NAME", salename);
                    Log.v("CURSOR_ID", String.valueOf(cursorId));
                    Log.v("saleWeight", String.valueOf(salename));
                    int newWeight = 0;
                    if (saleWeight > 0) {newWeight = saleWeight -1;} else {newWeight = saleWeight;}
                    Log.v("newWeight", String.valueOf(newWeight));

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_INVENTORY_WEIGHT, newWeight);
                    final Uri contentUri = ContentUris.withAppendedId(CONTENT_URI, cursorId);
                    int rowsUpdated = 0;
                    String selection = InventoryContract.InventoryEntry._ID;//+ "=?";
                    String [] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(contentUri))};
                    rowsUpdated = context.getContentResolver().update(contentUri, values, selection, selectionArgs);

                }
            }

        });
        //


        Log.v("CursorAdapter ImageId", String.valueOf(imageId));
        Log.v("InventoryCursorAdapter", String.valueOf(R.drawable.ic_banana));

        if (String.valueOf(weight).isEmpty()) {
            weight = 0;
        }
        if (TextUtils.isEmpty(String.valueOf(weight))) {
            weight = 0;
        }
        // Populate fields with extracted properties
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
        Log.v("Cursor ResourceId", String.valueOf(imageResourceId));
        inventoryName.setText(name);
        inventoryWeight.setText("Stock: "+String.valueOf(weight)+" lbs");
        inventoryPrice.setText("Price: $"+String.valueOf(price)+"/lb");
        inventoryImage.setImageResource(imageResourceId);
    }

}