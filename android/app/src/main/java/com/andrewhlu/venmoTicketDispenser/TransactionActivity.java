package com.andrewhlu.venmoTicketDispenser;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TransactionActivity extends AppCompatActivity {
    private static final String TAG = "VenmoTicketDispenser";
    private SharedPreferences mSharedPreferences;

    private View appView;
    private Activity mActivity = this;

    private ArrayList<String> mTransactionArray;
    private ArrayAdapter mAdapter;

    private Handler handler = new Handler();
    private Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // Launch the asynchronous process to grab the web API
                    new LookupTransaction(getApplicationContext(), mActivity, mTransactionArray,
                            mAdapter, mSharedPreferences).execute("");
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

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

        // Set up transaction array and adapter
        mTransactionArray = new ArrayList<>();
        ListView transactionList = mActivity.findViewById(R.id.transactionList);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mTransactionArray);
        transactionList.setAdapter(mAdapter);

        TextView ticketPriceText = findViewById(R.id.ticketPriceText);
        String ticketPrice = String.format(Locale.US, "$%.2f = 1 Ticket", mSharedPreferences.getFloat("costPerTicket", 0.0f));
        ticketPriceText.setText(ticketPrice);

        TextView venmoHandleText = findViewById(R.id.venmoHandleText);
        String venmoHandle = "@" + mSharedPreferences.getString("venmoHandle", "");
        venmoHandleText.setText(venmoHandle);

        TextView transactionCodeText = findViewById(R.id.transactionCodeText);
        String transactionCode = mSharedPreferences.getString("identifier", "") + " " + mSharedPreferences.getString("transactionCode", "");
        transactionCodeText.setText(transactionCode);

        TextView ticketCountText = findViewById(R.id.ticketCountText);
        ticketCountText.setText("0");

        Button printTicketsButton = findViewById(R.id.printTicketsButton);
        printTicketsButton.setOnClickListener((view -> {
            // TODO: call bluetooth
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideActionBar();

        // Start the timer to poll for transaction changes every 3000 ms
        timer.schedule(task, 0, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cancel the timer to poll for transaction changes
        timer.cancel();
    }

//    @Override
//    public void onBackPressed() {
//        // Do nothing on back pressed
//    }

    protected void hideActionBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        appView.setSystemUiVisibility(uiOptions);

        ActionBar actionBar = getActionBar();

        if(actionBar != null) {
            actionBar.hide();
        }
    }
}
