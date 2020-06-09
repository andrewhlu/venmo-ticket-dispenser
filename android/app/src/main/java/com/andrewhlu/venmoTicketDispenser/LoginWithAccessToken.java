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

public class LoginWithAccessToken extends AsyncTask<String, String, String>  {
    private static final String TAG = "VenmoTicketDispenser";
    private Context mContext;
    private Activity mActivity;
    private URL urlObject;
    private String mAccessToken;
    private SharedPreferences mSharedPreferences;

    LoginWithAccessToken(Context context, Activity activity, String accessToken, SharedPreferences sharedPreferences) {
        this.mContext = context;
        this.mActivity = activity;
        this.mAccessToken = accessToken;
        this.mSharedPreferences = sharedPreferences;
    }

    @Override
    protected String doInBackground(String... uri) {
        Log.d(TAG, "LoginWithAccessToken Running");
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
            connection = (HttpURLConnection) urlObject.openConnection();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // If this log is printed, then something went wrong with your call
                Log.d(TAG, "LoginWithAccessToken FAILED");
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
                // App is already initialized, store code and return to main activity
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("accessToken", mAccessToken);
                editor.apply();

                Toast.makeText(mContext, "Valid access code", Toast.LENGTH_SHORT).show();

                // Return to main activity
                Intent newIntent = new Intent(mContext, MainActivity.class);
                mActivity.startActivity(newIntent);
            }
            else if(resultObj.getString("error").equals("Invalid access code")) {
                // This access code is invalid, display a toast
                Toast.makeText(mContext, "Invalid access code", Toast.LENGTH_SHORT).show();
            }
            else {
                // This access code is valid, but the app is not initialized
                Toast.makeText(mContext, resultObj.getString("error"), Toast.LENGTH_SHORT).show();
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
