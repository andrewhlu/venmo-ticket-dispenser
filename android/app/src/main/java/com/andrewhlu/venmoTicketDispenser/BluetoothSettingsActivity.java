package com.andrewhlu.venmoTicketDispenser;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class BluetoothSettingsActivity extends AppCompatActivity {
    private View appView;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_settings);

        // Get shared preferences
        mSharedPreferences = getSharedPreferences("VenmoTicketDispenserPreferences", MODE_PRIVATE);

        appView = getWindow().getDecorView();

        Button startServiceButton = findViewById(R.id.startServiceButton);
        Button stopServiceButton = findViewById(R.id.stopServiceButton);
        Button manualDispenseButton = findViewById(R.id.manualDispenseButton);
        Button applicationSettingsButton = findViewById(R.id.applicationSettingsButton);

        EditText manualDispenseText = findViewById(R.id.manualDispenseText);
        manualDispenseText.setText("0");

        // Button listeners
        startServiceButton.setOnClickListener((view) -> {
            // Start Bluetooth Service
            Intent dispenseIntent = new Intent(this, BluetoothService.class);
            startService(dispenseIntent);
        });

        stopServiceButton.setOnClickListener((view) -> {
            // Stop Bluetooth Service
            Intent dispenseIntent = new Intent(this, BluetoothService.class);
            stopService(dispenseIntent);
        });

        manualDispenseButton.setOnClickListener((view) -> {
            // Dispense tickets
            Intent dispenseIntent = new Intent(this, BluetoothService.class);
            dispenseIntent.putExtra("numTickets", Integer.parseInt(manualDispenseText.getText().toString()));
            startService(dispenseIntent);
        });

        applicationSettingsButton.setOnClickListener((view) -> {
            // Go to app settings activity
            Intent newIntent = new Intent(this, SettingsActivity.class);
            startActivity(newIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showActionBar();
    }

    protected void showActionBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        appView.setSystemUiVisibility(uiOptions);

        ActionBar actionBar = getActionBar();

        if(actionBar != null) {
            actionBar.show();
        }
    }
}
