package com.andrewhlu.venmoTicketDispenser;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class SecuritySettingsActivity extends AppCompatActivity {
    private static final String TAG = "VenmoTicketDispenser";
    private View appView;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_settings);

        // Get shared preferences
        mSharedPreferences = getSharedPreferences("VenmoTicketDispenserPreferences", MODE_PRIVATE);

        appView = getWindow().getDecorView();

        Button nfcAddButton = findViewById(R.id.nfcAddButton);
        Button nfcRemoveButton = findViewById(R.id.nfcRemoveButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button applicationSettingsButton = findViewById(R.id.applicationSettingsButton);
        Button bluetoothSettingsButton = findViewById(R.id.bluetoothSettingsButton);

        EditText passcodeText = findViewById(R.id.passcodeText);

        checkValidCard(getIntent());

        // Button listeners
        nfcAddButton.setOnClickListener((view) -> {
            // Enter Add NFC tag mode
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("addingNfcTag", true);
            editor.apply();

            // Display toast to instruct user to scan card
            Toast.makeText(this, "Scan a new NFC tag to add to the whitelist!", Toast.LENGTH_SHORT).show();
        });

        nfcRemoveButton.setOnClickListener((view) -> {
            // Clear all NFC tags
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putStringSet("nfcTags", new HashSet<>());
            editor.apply();

            Toast.makeText(this, "NFC tags list cleared!", Toast.LENGTH_SHORT).show();
        });

        saveButton.setOnClickListener((view) -> {
            // Save the passcode
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("passcode", passcodeText.getText().toString());
            editor.apply();

            if(passcodeText.getText().toString().isEmpty()) {
                // Passcode field was empty, show removed message
                Toast.makeText(this, "Passcode removed!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Passcode saved!", Toast.LENGTH_SHORT).show();
            }
        });

        applicationSettingsButton.setOnClickListener((view) -> {
            // Exit Add NFC tag mode
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("addingNfcTag", false);
            editor.apply();

            // Go to app settings activity
            Intent newIntent = new Intent(this, SettingsActivity.class);
            startActivity(newIntent);
        });

        bluetoothSettingsButton.setOnClickListener((view) -> {
            // Exit Add NFC tag mode
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean("addingNfcTag", false);
            editor.apply();

            // Go to Bluetooth settings activity
            Intent newIntent = new Intent(this, BluetoothSettingsActivity.class);
            startActivity(newIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showActionBar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "Starting onNewIntent");

        checkValidCard(intent);
    }

    private void checkValidCard(Intent intent) {
        String action = intent.getAction();
        if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // This intent was triggered by an NFC tag
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // First, check if the app is initialized
            if(mSharedPreferences.getString("accessToken", "").isEmpty()) {
                // The app is not initialized, return to login activity
                Intent newIntent = new Intent(this, LoginActivity.class);
                startActivity(newIntent);
            }
            // Next, check if we are in add mode
            else if(mSharedPreferences.getBoolean("addingNfcTag", false)) {
                // We are in add mode, add the tag's serial number to the NFC tags list
                Set<String> nfcTags = new HashSet<>(mSharedPreferences.getStringSet("nfcTags", new HashSet<>()));
                nfcTags.add(bytesToHex(tag.getId()));

                // Save new NFC tags list
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putStringSet("nfcTags", nfcTags);
                editor.putBoolean("addingNfcTag", false);
                editor.apply();

                // Show confirmation toast
                Toast.makeText(this, "NFC tag added!", Toast.LENGTH_SHORT).show();
            }
            // Lastly, if passcode exists, check whether the tag is in the set
            else if(!mSharedPreferences.getString("passcode", "").isEmpty()) {
                // Get NFC tag serial number
                String serial = bytesToHex(tag.getId());

                // Get the list of NFC tags
                Set<String> nfcTags = new HashSet<>(mSharedPreferences.getStringSet("nfcTags", new HashSet<>()));

                // Check if the set contains the tag
                if(nfcTags.contains(serial)) {
                    // NFC tag is valid, display confirmation toast
                    Toast.makeText(this, "Valid NFC tag", Toast.LENGTH_SHORT).show();
                }
                else {
                    // NFC tag is not valid, display message
                    Toast.makeText(this, "Inalid NFC tag", Toast.LENGTH_SHORT).show();

                    // Return to main activity
                    Intent newIntent = new Intent(this, MainActivity.class);
                    startActivity(newIntent);
                }
            }
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
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
