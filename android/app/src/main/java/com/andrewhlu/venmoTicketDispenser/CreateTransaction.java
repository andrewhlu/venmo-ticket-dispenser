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
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateTransaction extends AsyncTask<String, String, String> {
    private static final String TAG = "VenmoTicketDispenser";
    private Context mContext;
    private Activity mActivity;
    private String mAccessToken;
    private SharedPreferences mSharedPreferences;
    private URL urlObject;

    CreateTransaction(Context context, Activity activity, SharedPreferences sharedPreferences) {
        this.mContext = context;
        this.mActivity = activity;
        this.mSharedPreferences = sharedPreferences;
        this.mAccessToken = mSharedPreferences.getString("accessToken", "");
    }

    @Override
    protected String doInBackground(String... uri) {
        Log.d(TAG, "CreateTransaction Running");
        String responseString = null;
        try {
            String requestURL = "https://alu-moe.now.sh/api/venmo/transaction/create?code=" + mAccessToken;
            urlObject = new URL(requestURL);
        } catch(Exception e) {
            e.printStackTrace();
            responseString = "FAILED";
        }

        HttpURLConnection connection = null;
        try {
            // Establish HTTP connection
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // If this log is printed, then something went wrong with your call
                Log.d(TAG, "CreateTransaction FAILED");
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
                // Update was successful, write new settings
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("transactionCode", resultObj.getString("code"));
                editor.apply();

                // Start transaction activity
                Intent newIntent = new Intent(mContext, TransactionActivity.class);
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
