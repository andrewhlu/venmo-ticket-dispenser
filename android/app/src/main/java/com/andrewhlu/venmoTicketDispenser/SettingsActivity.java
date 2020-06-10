package com.andrewhlu.venmoTicketDispenser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private View appView;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Get shared preferences
        mSharedPreferences = getSharedPreferences("VenmoTicketDispenserPreferences", MODE_PRIVATE);

        appView = getWindow().getDecorView();

        Button logoutButton = findViewById(R.id.logoutButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        Button saveButton = findViewById(R.id.saveButton);

        EditText identifierText = findViewById(R.id.identifierText);
        String identifier = mSharedPreferences.getString("identifier", "");

        if(identifier.isEmpty()) {
            // Disable cancel button
            cancelButton.setEnabled(false);
        }
        else {
            // Prevent identifier from being edited
            identifierText.setText(identifier);
            identifierText.setEnabled(false);
        }

        EditText costPerTicketText = findViewById(R.id.costPerTicketText);
        float costPerTicket = mSharedPreferences.getFloat("costPerTicket", 0.0f);
        costPerTicketText.setText(Float.toString(costPerTicket));

        EditText venmoHandleText = findViewById(R.id.venmoHandleText);
        String venmoHandle = mSharedPreferences.getString("venmoHandle", "");
        venmoHandleText.setText(venmoHandle);

        // Button listeners
        logoutButton.setOnClickListener((view) -> {
            // Prompt user to confirm logout
            String[] options = {"Yes", "No"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure you want to logout?");
            builder.setItems(options, (dialogInterface, i) -> {
                if(options[i].equals("Yes")) {
                    // Clear all shared preferences
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("accessToken", "");
                    editor.putString("identifier", "");
                    editor.putFloat("costPerTicket", 0.0f);
                    editor.putString("venmoHandle", "");
                    editor.putString("transactionCode", "");
                    editor.apply();

                    // Display logged out toast
                    Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show();

                    // Navigate to login activity
                    Intent newIntent = new Intent(this, LoginActivity.class);
                    startActivity(newIntent);
                }
            });
            builder.show();
        });

        cancelButton.setOnClickListener((view) -> {
            // Return to main activity
            Intent newIntent = new Intent(this, MainActivity.class);
            startActivity(newIntent);
        });

        saveButton.setOnClickListener((view) -> {
            // Send data to API
            new UpdateSettings(getApplicationContext(), this, identifierText.getText().toString(),
                    Float.parseFloat(costPerTicketText.getText().toString()),
                    venmoHandleText.getText().toString(), mSharedPreferences).execute("");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showActionBar();
    }

    @Override
    public void onBackPressed() {
        // Check if the identifier has been set. If so, return to main activity
        String identifier = mSharedPreferences.getString("identifier", "");

        if(!identifier.isEmpty()) {
            // Return to main activity
            Intent newIntent = new Intent(this, MainActivity.class);
            startActivity(newIntent);
        }
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
