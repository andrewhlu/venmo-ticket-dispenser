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
import java.net.URLEncoder;

public class UpdateSettings extends AsyncTask<String, String, String> {
    private static final String TAG = "VenmoTicketDispenser";
    private Context mContext;
    private Activity mActivity;
    private String mAccessToken;
    private String mIdentifier;
    private float mCostPerTicket;
    private String mVenmoHandle;
    private SharedPreferences mSharedPreferences;
    private URL urlObject;

    UpdateSettings(Context context, Activity activity, String accessToken, String identifier,
                   float costPerTicket, String venmoHandle, SharedPreferences sharedPreferences) {
        this.mContext = context;
        this.mActivity = activity;
        this.mAccessToken = accessToken;
        this.mIdentifier = identifier;
        this.mCostPerTicket = costPerTicket;
        this.mVenmoHandle = venmoHandle;
        this.mSharedPreferences = sharedPreferences;
    }

    @Override
    protected String doInBackground(String... uri) {
        Log.d(TAG, "UpdateSettings Running");
        String responseString = null;
        try {
            String requestURL = "https://alu-moe.now.sh/api/venmo?code=" + mAccessToken;
            urlObject = new URL(requestURL);
        } catch(Exception e) {
            e.printStackTrace();
            responseString = "FAILED";
        }

        HttpURLConnection connection = null;
        try {
            // Construct the JSON object to send to backend
            JSONObject bodyObject = new JSONObject();

            // Put identifier in object if not already defined
            String identifier = mSharedPreferences.getString("identifier", "");
            if(identifier.isEmpty()) {
                bodyObject.put("identifier", mIdentifier);
            }

            // Put cost per ticket and venmo handle
            bodyObject.put("costPerTicket", mCostPerTicket);
            bodyObject.put("venmoHandle", mVenmoHandle);

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
                Log.d(TAG, "UpdateSettings FAILED");
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

            // First, check if code is initialized
            if(resultObj.getBoolean("initialized")) {
                // Update was successful, write new settings
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("identifier", resultObj.getString("identifier"));
                editor.putFloat("costPerTicket", (float) resultObj.getDouble("costPerTicket"));
                editor.putString("venmoHandle", resultObj.getString("venmoHandle"));
                editor.apply();

                Toast.makeText(mContext, "Settings saved!", Toast.LENGTH_SHORT).show();

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
