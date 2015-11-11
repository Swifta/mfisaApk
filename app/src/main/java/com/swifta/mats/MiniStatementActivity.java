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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MiniStatementActivity extends AppCompatActivity {

    private MiniStatementActivity self = this;
    private boolean busy = false;

    private TextView noStatement;
    private LinearLayout generalLayout;
    private LinearLayout container;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            try {
                Bundle bundle = intent.getExtras();

                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                JSONObject responseJson2 = responseJson.getJSONObject("TransactionResponses");

                if (!responseJson2.toString().equals("null")) {
                    JSONArray finalJson = responseJson2.getJSONArray("TransactionResponse");

                    if (finalJson.length() != 0) {
                        // Automatically populates the trailers with data from the JSON response
                        for (int i = 0; i < finalJson.length(); i++) {
                            JSONObject arrayValue = finalJson.getJSONObject(i);

                            // Sets up the layout to display the data from the JSON response
                            generalLayout.setVisibility(View.GONE);
                            View v = getLayoutInflater().inflate(R.layout.mini_statement_item, null);
                            container.addView(v);

                            String transactionTypeValue = arrayValue.getString("transactiontype");
                            String date = arrayValue.getString("date");

                            TextView transactionType = (TextView) v.findViewById(R.id.transactiontype);
                            transactionType.setText(transactionTypeValue.replace("_", " ") + " on "
                                    + getDayFromDate(date) + " at " + getTimeFromDate(date));

                            TextView amount = (TextView) v.findViewById(R.id.amount);
                            amount.setText("Amount: " + arrayValue.getInt("amount"));

                            TextView receiver = (TextView) v.findViewById(R.id.receiver);
                            receiver.setText("Receiver: " + arrayValue.getString("receiver"));

                            // If the status failed, creates a visual cue by setting the text color to red,
                            // and sets the text color to green otherwise
                            TextView status = (TextView) v.findViewById(R.id.status);
                            if (arrayValue.getString("status").equals("FAILED")) {
                                status.setTextColor(ContextCompat.getColor(self, android.R.color.holo_red_dark));
                            } else {
                                status.setTextColor(ContextCompat.getColor(self, android.R.color.holo_green_dark));
                            }
                            status.setText(arrayValue.getString("status"));

                            busy = false;
                        }
                    } else {
                        noStatement.setVisibility(View.VISIBLE);
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(self, "Your request cannot be completed now, please try again.", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini_statement);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initEvent();
    }

    private void initEvent() {
        noStatement = (TextView) findViewById(R.id.no_statement);
        generalLayout = (LinearLayout) findViewById(R.id.general_layout);
        container = (LinearLayout) findViewById(R.id.container);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "UNKNOWN");

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
        busy = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mini_statement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Confirms that the user really wants to logout
     */
    public void confirmLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to logout?")
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
        Intent actIntent = new Intent(self, MainActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        finish();
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
        String dateValue = date.substring(11, 19);

        // Determines whether to append a "PM" prefix or "AM"
        if (Integer.parseInt(dateValue.substring(0, 2)) <= 12) {
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

    @Override
    public void onBackPressed() {
        if (busy) {
            Toast.makeText(self, "Currently processing a request. Please wait...", Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }
}
