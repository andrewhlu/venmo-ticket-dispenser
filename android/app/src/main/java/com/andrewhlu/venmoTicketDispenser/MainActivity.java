package com.andrewhlu.venmoTicketDispenser;

import android.app.ActionBar;
import android.app.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private static final String TAG = "VenmoTicketDispenser";
    private SharedPreferences mSharedPreferences;

    private View appView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appView = getWindow().getDecorView();
        appView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
            if((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                Log.v(TAG, "System UI visible");

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideActionBar();
                    }
                }, 5000);
            }
            else {
                Log.v(TAG, "System UI hidden");
            }
            }
        });

        // Get shared preferences
        mSharedPreferences = getSharedPreferences("VenmoTicketDispenserPreferences", MODE_PRIVATE);

        // Check if access token exists. If not, open login activity
        if(mSharedPreferences.getString("accessToken", "").isEmpty()) {
            // String is empty, navigate to login activity
            Intent newIntent = new Intent(this, LoginActivity.class);
            startActivity(newIntent);
        }

        ImageView settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener((view) -> {
            // Navigate to login activity
            Intent newIntent = new Intent(this, LoginActivity.class);
            startActivity(newIntent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        hideActionBar();
    }

    protected void hideActionBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        appView.setSystemUiVisibility(uiOptions);

        ActionBar actionBar = getActionBar();

        if(actionBar != null) {
            actionBar.hide();
        }
    }
}
