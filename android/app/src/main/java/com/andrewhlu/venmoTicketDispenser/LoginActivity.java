package com.andrewhlu.venmoTicketDispenser;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private View appView;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get shared preferences
        mSharedPreferences = getSharedPreferences("VenmoTicketDispenserPreferences", MODE_PRIVATE);

        appView = getWindow().getDecorView();

        Button gmailSignInButton = findViewById(R.id.signInButton);
        gmailSignInButton.setOnClickListener((view) -> {
            // Open browser to start OAuth flow
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://alu-moe.now.sh/api/auth/venmo"));
            startActivity(browserIntent);
        });

        Button continueButton = findViewById(R.id.continueButton);
        EditText accessTokenInput = findViewById(R.id.accessTokenInput);
        continueButton.setOnClickListener((view) -> {
            if(accessTokenInput.getText().toString().isEmpty()) {
                // Access token input is empty, display message
                Toast.makeText(this, "Please enter an access token!", Toast.LENGTH_SHORT).show();
            }
            else {
                // Check if access token is valid
                new LoginWithAccessToken(getApplicationContext(), this, accessTokenInput.getText().toString(), mSharedPreferences).execute("");
            }
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
