package com.andrewhlu.venmoTicketDispenser;

import android.app.ActionBar;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "VenmoTicketDispenser";
    private SharedPreferences mSharedPreferences;

    private View appView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to ticket dispenser via Bluetooth
        Intent dispenseIntent = new Intent(this, BluetoothService.class);
        startService(dispenseIntent);

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
        // Check if transaction code exists. If so, resume transaction
        else if(!mSharedPreferences.getString("transactionCode", "").isEmpty()) {
            // Transaction code exists, navigate to transaction activity
            Intent newIntent = new Intent(this, TransactionActivity.class);
            startActivity(newIntent);
        }

        Button startTransactionButton = findViewById(R.id.startTransactionButton);
        startTransactionButton.setOnClickListener((view) -> {
            // Create a new transaction
            new CreateTransaction(getApplicationContext(), this, mSharedPreferences).execute("");
        });

        ImageView settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener((view) -> {
            // Check if a passcode exists
            if(mSharedPreferences.getString("passcode", "").isEmpty()) {
                // No passcode is set, navigate to settings activity
                Intent newIntent = new Intent(this, SettingsActivity.class);
                startActivity(newIntent);
            }
            else {
                // Prompt user to enter passcode
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Enter Passcode");

                // Set up the input
                final EditText input = new EditText(this);

                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText().toString().equals(mSharedPreferences.getString("passcode", ""))) {
                            // Passcode is correct, navigate to settings activity
                            Intent newIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(newIntent);
                        }
                        else {
                            // Passcode is incorrect, display toast
                            Toast.makeText(getApplicationContext(), "Incorrect passcode", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideActionBar();
    }

    @Override
    public void onBackPressed() {
        // Do nothing on back pressed
    }

    protected void hideActionBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        appView.setSystemUiVisibility(uiOptions);

        ActionBar actionBar = getActionBar();

        if(actionBar != null) {
            actionBar.hide();
        }
    }
}
