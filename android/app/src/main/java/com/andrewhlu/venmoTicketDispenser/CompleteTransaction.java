package com.andrewhlu.venmoTicketDispenser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CompleteTransaction extends AsyncTask<String, String, String> {
    private static final String TAG = "VenmoTicketDispenser";
    private Context mContext;
    private Activity mActivity;
    private SharedPreferences mSharedPreferences;
    private String mAccessToken;
    private String mTransactionCode;
    private URL urlObject;

    CompleteTransaction(Context context, Activity activity, SharedPreferences sharedPreferences) {
        this.mContext = context;
        this.mActivity = activity;
        this.mSharedPreferences = sharedPreferences;
        this.mAccessToken = mSharedPreferences.getString("accessToken", "");
        this.mTransactionCode = mSharedPreferences.getString("transactionCode", "");
    }

    @Override
    protected String doInBackground(String... uri) {
        Log.d(TAG, "CompleteTransaction Running");
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
            // Construct the JSON object to send to backend
            JSONObject bodyObject = new JSONObject();
            bodyObject.put("completed", true);
            Log.v(TAG, bodyObject.toString());

            // Establish HTTP connection
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Place JSON in body
            byte[] outputInBytes = bodyObject.toString().getBytes("UTF-8");
            OutputStream os = connection.getOutputStream();
            os.write(outputInBytes);
            os.close();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // If this log is printed, then something went wrong with your call
                Log.d(TAG, "CompleteTransaction FAILED");
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

                // Dispense tickets
                Intent dispenseIntent = new Intent(mContext, BluetoothService.class);
                dispenseIntent.putExtra("numTickets", transactionObj.getInt("tickets"));
                mActivity.startService(dispenseIntent);

                // Clear transaction code
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("transactionCode", "");
                editor.apply();

                // Return to main activity
                Intent newIntent = new Intent(mContext, MainActivity.class);
                mActivity.startActivity(newIntent);
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
