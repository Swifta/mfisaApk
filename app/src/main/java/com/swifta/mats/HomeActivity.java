package com.swifta.mats;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.InternetCheck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private HomeActivity self = this;
    private String myName = "";
    private TextView withdrawalButton;
    private TextView floatTransferButton;
    private TextView readMiniStatementButton;
    private TextView cashInButton;
    private TextView billPaymentButton;
    private TextView transactionTitle;
    private TextView transactionType;
    private TextView transactionDate;
    private TextView transactionTime;
    private TextView transactionStatus;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                Bundle bundle = intent.getExtras();
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                JSONObject responseJson2 = responseJson.getJSONObject("TransactionResponses");

                JSONArray finalJson = responseJson2.getJSONArray("TransactionResponse");

                if (finalJson.length() != 0) {
                    // Automatically populates the views with the first data from the JSON response
                    JSONObject arrayValue = finalJson.getJSONObject(0);

                    // Sets up the layout to display the data from the JSON response

                    String transactionTypeValue = arrayValue.getString("transactiontype").toLowerCase();
                    String date = arrayValue.getString("date");
                    String status = arrayValue.getString("status");

                    transactionTitle.setVisibility(View.VISIBLE);
                    transactionType.setVisibility(View.VISIBLE);
                    transactionType.setText(transactionTypeValue);
                    transactionDate.setVisibility(View.VISIBLE);
                    transactionDate.setText(getDayFromDate(date));
                    transactionTime.setVisibility(View.VISIBLE);
                    transactionTime.setText(getTimeFromDate(date));
                    transactionStatus.setVisibility(View.VISIBLE);


                    // If the status failed, creates a visual cue by setting the text color to red,
                    // sets the text color to green if successful and to yellow if pending
                    switch (status) {
                        case "SUCCESSFUL":
                            transactionStatus.setTextColor(ContextCompat.getColor(self, android.R.color.holo_green_dark));
                            break;
                        case "PENDING":
                            transactionStatus.setTextColor(ContextCompat.getColor(self, R.color.yellow));
                            break;
                        case "FAILED":
                            transactionStatus.setTextColor(ContextCompat.getColor(self, android.R.color.holo_red_dark));
                            break;
                        case "NOT_ENOUGH_FUNDS":
                            transactionStatus.setTextColor(ContextCompat.getColor(self, android.R.color.holo_red_dark));
                            break;
                        default:
                            transactionStatus.setTextColor(ContextCompat.getColor(self, android.R.color.black));
                    }

                    transactionStatus.setText(status);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // Sets up the layout to handle null response
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        getSupportActionBar();
        setTitle(myName);
        initEvents();
    }

    private void initEvents() {
        withdrawalButton = (TextView) findViewById(R.id.withdrawal);
        floatTransferButton = (TextView) findViewById(R.id.float_transfer);
        readMiniStatementButton = (TextView) findViewById(R.id.mini_statement);
        cashInButton = (TextView) findViewById(R.id.cash_in);
        billPaymentButton = (TextView) findViewById(R.id.bill_payment);
        transactionTitle = (TextView) findViewById(R.id.transaction_title);
        transactionType = (TextView) findViewById(R.id.transaction_type);
        transactionDate = (TextView) findViewById(R.id.transaction_date);
        transactionTime = (TextView) findViewById(R.id.transaction_time);
        transactionStatus = (TextView) findViewById(R.id.transaction_status);

        withdrawalButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent actIntent = new Intent(self, WithdrawalActivity.class);
                startActivity(actIntent);
            }
        });

        floatTransferButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent actIntent = new Intent(self, FloatTransferActivity.class);
                startActivity(actIntent);
            }

        });

        readMiniStatementButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InternetCheck.isNetworkAvailable(self)) {
                    Intent intent = new Intent(self, MiniStatementActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(self, getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        cashInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MMOperatorsActivity.class);
                intent.putExtra(Constants.PREVIOUS_ACTIVITY, Constants.CASH_IN);
                startActivity(intent);
            }
        });

        billPaymentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, BillPaymentActivity.class);
                startActivity(intent);
            }
        });

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", Constants.UNKNOWN);

        JSONObject data = new JSONObject();
        try {
            data.put("username", username);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Intent intent = new Intent(self, BackgroundServices.class);
        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_MINI_STATEMENT);
        intent.putExtra(Constants.JOB_DATA, data.toString());
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //System.out.println("Back was pressed with ID:"+item.getTitle());
        if (id == R.id.logout) {
            //logout clicked
            confirmLogout();
            return true;
        } else if (id == R.id.my_account) {
            openAccountActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Confirms that the user really wants to logout
     */

    public void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.logout_confirmation))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        logout();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //builder
                    }

                }).show();
    }

    private void logout() {
        Intent actIntent = new Intent(self, LoginActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        finish();
    }

    private void openAccountActivity() {
        Intent actIntent = new Intent(self, AccountActivity.class);
        startActivity(actIntent);
    }

    /**
     * Retrieves the transaction day from the date
     */
    private String getDayFromDate(String date) {
        return date.substring(0, 10);
    }

    /**
     * Retrieves the transaction time from the date
     */
    private String getTimeFromDate(String date) {
        String dateValue = date.substring(11, 16);

        // Determines whether to append a "PM" prefix or "AM"
        if (Integer.parseInt(dateValue.substring(0, 2)) >= 12) {
            return dateValue += " PM";
        } else {
            return dateValue += " AM";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(Constants.SERVICE_NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
