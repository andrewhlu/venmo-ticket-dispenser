package com.andrewhlu.venmoTicketDispenser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class LookupTransaction extends AsyncTask<String, String, String> {
    private static final String TAG = "VenmoTicketDispenser";
    private Context mContext;
    private Activity mActivity;
    private ArrayList<String> mTransactionArray;
    private ArrayAdapter mAdapter;
    private SharedPreferences mSharedPreferences;
    private String mAccessToken;
    private String mTransactionCode;
    private URL urlObject;



    LookupTransaction(Context context, Activity activity, ArrayList<String> transactionArray,
                      ArrayAdapter adapter, SharedPreferences sharedPreferences) {
        this.mContext = context;
        this.mActivity = activity;
        this.mTransactionArray = transactionArray;
        this.mAdapter = adapter;
        this.mSharedPreferences = sharedPreferences;
        this.mAccessToken = mSharedPreferences.getString("accessToken", "");
        this.mTransactionCode = mSharedPreferences.getString("transactionCode", "");
    }

    @Override
    protected String doInBackground(String... uri) {
        Log.d(TAG, "LookupTransaction Running");
        String responseString = null;
        try {
            String requestURL = "https://alu-moe.now.sh/api/venmo/transaction?code=" + mAccessToken + "&transaction=" + mTransactionCode;
            urlObject = new URL(requestURL);
        } catch(Exception e) {
            e.printStackTrace();
            responseString = "FAILED";
        }

        HttpURLConnection connection = null;
        try {
            // Establish HTTP connection
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // If this log is printed, then something went wrong with your call
                Log.d(TAG, "LookupTransaction FAILED");
            }
            return readFullyAsString(connection.getInputStream());
        } catch(Exception e) {
            e.printStackTrace();
            try {
                return readFullyAsString(connection.getErrorStream());
            } catch(Exception e2) {
                e2.printStackTrace();
                responseString = "FAILED";
            }
        } finally {
            connection.disconnect();
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.v(TAG, "Result: " + result);

        try {
            // Turn result into JSON object
            JSONObject resultObj = new JSONObject(result);

            // Check if successful
            if(resultObj.getBoolean("success")) {
                // Request was successful, get transaction object
                JSONObject transactionObj = resultObj.getJSONObject("transaction");

                // Display the ticket amount
                TextView ticketCountText = mActivity.findViewById(R.id.ticketCountText);
                ticketCountText.setText(transactionObj.getInt("tickets") + "");

                // Clear transaction array
                mTransactionArray.clear();

                // Get list of emails
                JSONObject emailObj = transactionObj.getJSONObject("emails");
                for(Iterator<String> i = emailObj.keys(); i.hasNext();) {
                    String key = i.next();

                    // Add this email to the transaction array
                    JSONObject email = emailObj.getJSONObject(key);
                    mTransactionArray.add(email.getDouble("amount") + " - " + email.getString("name"));
                }

                // Re-render the list view
                mAdapter.notifyDataSetChanged();
            }
            else {
                // An error occurred, display the error
                String error = resultObj.getString("error");
                Toast.makeText(mContext, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String readFullyAsString(InputStream inputStream) throws IOException {
        return readFully(inputStream).toString("UTF-8");
    }

    private ByteArrayOutputStream readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while((length = inputStream.read(buffer)) != -1) {
            stream.write(buffer, 0, length);
        }

        return stream;
    }
}
