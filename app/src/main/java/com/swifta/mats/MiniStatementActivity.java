package com.swifta.mats;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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

import java.util.ArrayList;

public class MiniStatementActivity extends AppCompatActivity {

    private MiniStatementActivity self = this;
    private boolean busy = false;

    private TextView noStatement;
    private LinearLayout generalLayout;
    private LinearLayout container;

    private CardView countCardView;
    private TextView floatTextView;
    private TextView cashInTextView;
    private TextView cashOutTextView;
    private TextView paymentTextView;

    private LinearLayout floatLayout;
    private LinearLayout cashInLayout;
    private LinearLayout cashOutLayout;
    private LinearLayout paymentLayout;

    ArrayList<Statement> floatList = new ArrayList<Statement>();
    ArrayList<Statement> cashInList = new ArrayList<Statement>();
    ArrayList<Statement> cashOutList = new ArrayList<Statement>();
    ArrayList<Statement> paymentList = new ArrayList<Statement>();

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
                    // Automatically populates with data from the JSON response
                    for (int i = 0; i < finalJson.length(); i++) {
                        JSONObject arrayValue = finalJson.getJSONObject(i);

                        String transactionTypeValue = arrayValue.getString("transactiontype");
                        String date = arrayValue.getString("date");
                        int amount = arrayValue.getInt("amount");
                        String receiver = arrayValue.getString("receiver");
                        String status = arrayValue.getString("status");

                        // Adds to the respective arraylist based on the type of transaction being performed
                        switch (transactionTypeValue) {
                            case "FLOAT_TRANSFER":
                                floatList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                            case "CASH_IN":
                                cashInList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                            case "CASH_OUT":
                                cashOutList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                            case "PAYMENT":
                                paymentList.add(new Statement(transactionTypeValue, date, amount, receiver, status));
                                break;
                        }
                    }

                    busy = false;
                    generalLayout.setVisibility(View.GONE);
                    noStatement.setVisibility(View.GONE);
                    countCardView.setVisibility(View.VISIBLE);

                    // Sets the numeric values beside each transaction type
                    floatTextView.setText(" (" + String.valueOf(floatList.size()) + ")");
                    cashInTextView.setText(" (" + String.valueOf(cashInList.size()) + ")");
                    cashOutTextView.setText(" (" + String.valueOf(cashOutList.size()) + ")");
                    paymentTextView.setText(" (" + String.valueOf(paymentList.size()) + ")");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // Sets up the layout to handle null response
                generalLayout.setVisibility(View.GONE);
                countCardView.setVisibility(View.GONE);
                noStatement.setVisibility(View.VISIBLE);
                busy = false;
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

        countCardView = (CardView) findViewById(R.id.count_cardview);
        floatTextView = (TextView) findViewById(R.id.float_transfer_count);
        cashInTextView = (TextView) findViewById(R.id.cash_in_count);
        cashOutTextView = (TextView) findViewById(R.id.withdrawal_count);
        paymentTextView = (TextView) findViewById(R.id.payment_count);

        floatLayout = (LinearLayout) findViewById(R.id.float_layout);
        cashInLayout = (LinearLayout) findViewById(R.id.cash_in_layout);
        cashOutLayout = (LinearLayout) findViewById(R.id.withdrawal_layout);
        paymentLayout = (LinearLayout) findViewById(R.id.payment_layout);

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

        floatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(floatList, "Float Transfer");
            }
        });

        cashInLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(cashInList, "Cash In");
            }
        });

        cashOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(cashOutList, "Cash Out");
            }
        });

        paymentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionsClickAction(paymentList, "Bill Payment");
            }
        });

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

    private void transactionsClickAction(ArrayList<Statement> arrayList, String string) {
        if (arrayList.size() > 0) {
            Intent i = new Intent(this, TransactionActivity.class);
            i.putExtra("name", string);
            i.putExtra("amount", arrayList.size());
            i.putParcelableArrayListExtra("arraylist", arrayList);
            startActivity(i);
        } else {
            Toast.makeText(MiniStatementActivity.this, "No " + string + " transaction available", Toast.LENGTH_SHORT).show();
        }
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
            Toast.makeText(self, getResources().getString(R.string.processing_request), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }
}
