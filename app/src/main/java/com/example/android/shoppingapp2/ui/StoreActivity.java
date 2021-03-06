package com.example.android.shoppingapp2.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.shoppingapp2.R;
import com.example.android.shoppingapp2.model.Keeper;
import com.example.android.shoppingapp2.model.ShoppingDbHelper;
import com.example.android.shoppingapp2.model.ShoppingItem;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class StoreActivity extends Activity  {

    private Button scanBtn,storeBtn,displayBtn;
    private TextView formatTxt, contentTxt;
    private EditText productNameEditText, priceEditText;
    public static final String TAG = StoreActivity.class.getSimpleName();

    private ShoppingItem mShoppingItem;
    private int mRowNumber;
    private Keeper mShoppingList = new Keeper();

    private String mName, mItemPrice, mUPC, mCategory;
    private int mQuantity;
    private double mSubtotal;

    Context context;
    ShoppingDbHelper shoppingDbHelper;
    SQLiteDatabase sqLiteDatabase;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        scanBtn = (Button) findViewById(R.id.scan_button);
        storeBtn = (Button) findViewById(R.id.store_button);
        displayBtn = (Button) findViewById(R.id.display_button);
        formatTxt = (TextView) findViewById(R.id.scan_format);
        contentTxt = (TextView) findViewById(R.id.scan_content);
        productNameEditText = (EditText) findViewById(R.id.productNameEditText);
        priceEditText = (EditText) findViewById(R.id.priceEditText);

        // Make the Store Button invisible. Only make it visible after a scanning result
        // is successful so that the user can store the item if he or she so desires.
        storeBtn.setVisibility(View.INVISIBLE);

        // Variables for the HTTP Request

        Log.d(TAG, "Main UI code is running!");


        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(v.getId()==R.id.scan_button){
                    //scan

                    IntentIntegrator scanIntegrator = new IntentIntegrator(StoreActivity.this);
                    scanIntegrator.initiateScan();

                }

            }
        });

        storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addItem();
            }
        });

        // Initialize shopping item

        mShoppingItem = new ShoppingItem();

        //Initialize UserDBHelper and SQLiteDB

        shoppingDbHelper = new ShoppingDbHelper(getApplicationContext());
        sqLiteDatabase = shoppingDbHelper.getReadableDatabase();

        cursor = shoppingDbHelper.getShoppingItem(sqLiteDatabase);


        // Initialize the Row Number

        mRowNumber = 0;

        if(cursor.moveToFirst()){

            do{

                String productName, category;
                int quantity;
                double unitPrice, subtotal;

                quantity = cursor.getInt(0);
                productName = cursor.getString(1);
                unitPrice = cursor.getDouble(2);
                category = cursor.getString(3);
                subtotal = cursor.getDouble(4);

                mShoppingItem = new ShoppingItem(quantity, productName, unitPrice, category, subtotal);

                subtotal = quantity * unitPrice;

                mShoppingItem.setSubtotal(subtotal);

                mShoppingList.addShoppingItem(mShoppingItem, mRowNumber);

                mRowNumber++;

            }

            while(cursor.moveToNext());

        }

        mQuantity = 1;

        displayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StoreActivity.this, DisplayShoppingList.class);

                // Next, I will pass in the array of shopping items, mShoppingList, a "Keeper" object
                // to DisplayShoppingList.java


                intent.putExtra(getString(R.string.ROW_NUMBER), mRowNumber);

                intent.putExtra(getString(R.string.SHOPPING_LIST), mShoppingList.mShoppingItems);

                startActivity(intent);
            }
        });

    }

    private void getScannedItemInfo(String upc) {

        // API Key for Walmart

        String apiKey = "";

        String itemLookUpUrl = "http://api.walmartlabs.com/v1/items?apiKey=" +
                apiKey + "&upc=" + upc;


        if(isNetworkAvailable()) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(itemLookUpUrl)
                    .build();


            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(StoreActivity.this, "Error", Toast.LENGTH_LONG).show();

                        }
                    });
                    alertUserAboutError();

                }

                @Override
                public void onResponse(final Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    try {
                        final String jsonData = response.body().string();


                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        JSONObject mainObject = new JSONObject(jsonData);
                                        JSONArray items = mainObject.getJSONArray("items");

                                        for(int i=0; i<items.length(); i++){
                                            JSONObject item = items.getJSONObject(i);
                                            mName = item.getString("name");
                                            mItemPrice = item.getString("salePrice");
                                            mCategory = item.getString("categoryPath");

                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();

                                    }

                                        mShoppingItem.setProductName(mName);
                                        productNameEditText.setText(mName);

                                    if(mItemPrice==null){
                                        Toast.makeText(StoreActivity.this, "Price unavailable",Toast.LENGTH_LONG).show();;
                                    }

                                    else {

                                        mShoppingItem.setItemPrice(Double.parseDouble(mItemPrice));
                                        priceEditText.setText("$" + mItemPrice);
                                        mSubtotal = Double.parseDouble(mItemPrice) * mQuantity;
                                    }


                                    }




                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }

                }
            });

        }

        else{

            Toast.makeText(this, getString(R.string.network_unavailable_message),
                    Toast.LENGTH_LONG).show();
        }
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }

        return isAvailable;

    }

    private void alertUserAboutError() {

        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve scan result

        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanningResult != null) {
            //we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            formatTxt.setText("FORMAT: " + scanFormat);

            contentTxt.setText("CONTENT: " + scanContent);
            mUPC = scanContent;

            mShoppingItem.setUPC(mUPC);

            getScannedItemInfo(mUPC);

            // Make STORE button visible so that user can store the scanned item if he or she
            // wishes.

            storeBtn.setVisibility(View.VISIBLE);

        }

        else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void addItem() {

        context = this;

        // Perform DB insertion...

        // Initialize shoppingDBhelper object and SQLiteDatabase object.

            shoppingDbHelper = new ShoppingDbHelper(context);
            sqLiteDatabase = shoppingDbHelper.getWritableDatabase();

            // Insert the item details in the database
            shoppingDbHelper.addItem(mUPC, mQuantity, mName, mItemPrice, mCategory,
                    mSubtotal+"", sqLiteDatabase);

            Toast.makeText(StoreActivity.this, "Shopping Item Saved", Toast.LENGTH_LONG).show();

            shoppingDbHelper.close();

            finish(); // Go back to Main Activity

    }

}
