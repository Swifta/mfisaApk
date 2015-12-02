package com.swifta.mats.forms;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.LoginActivity;
import com.swifta.mats.R;
import com.swifta.mats.WithdrawalActivity;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class CompleteCashOutActivity extends AppCompatActivity {

    private CompleteCashOutActivity self = this;
    private String myName = "";
    private String myPassword = "";
    private boolean btn_clicked = true;

    private TextView dealer;
    private TextView subscriber;
    private TextView amount;
    private TextView mmOperator;
    private EditText otpValue;
    private Button submit;
    private ProgressDialog progressDialog;

    private boolean busy = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            progressDialog.hide();
            Bundle bundle = intent.getExtras();

            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.COMPLETE_CASH_OUT_COMPLETED) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.TRANSACTION_WAS_SUCCESSFUL)) {

                            SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_CASHOUT_DATA,
                                    Context.MODE_PRIVATE);

                            // Clears the cache after successful cashout transaction
                            sharedPref.edit().clear().apply();
                            AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                            dialog.setMessage(Constants.TRANSACTION_WAS_SUCCESSFUL);
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            dialog.show();

                            Intent i = new Intent(self, WithdrawalActivity.class);
                            startActivity(i);
                        } else {
                            String errorMessage = psaTranResponse.getString("responsemessage");
                            AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                            dialog.setMessage(getResources().getString(R.string.request_rejection_reason) + errorMessage.replace("_", " ")
                                    .toLowerCase());
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            dialog.show();
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                        dialog.setMessage("Request failed : " + showReport);
                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        dialog.show();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(self, getResources().getString(R.string.retry_uncompleted_request), Toast.LENGTH_LONG).show();
            } finally {
                busy = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_cash_out);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();
        myPassword = sharedPref.getString("password", Constants.UNKNOWN);
        setTitle(myName);
        initEvents();
        btn_clicked = false;
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

    private void initEvents() {
        dealer = (TextView) findViewById(R.id.dealer_cashout);
        subscriber = (TextView) findViewById(R.id.subscriber_number_cashout);
        amount = (TextView) findViewById(R.id.amount_cashout);
        mmOperator = (TextView) findViewById(R.id.mm_operator);
        otpValue = (EditText) findViewById(R.id.cashout_otp);
        submit = (Button) findViewById(R.id.submit);
        progressDialog = new ProgressDialog(self);

        final SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_CASHOUT_DATA,
                Context.MODE_PRIVATE);
        dealer.setText(sharedPref.getString("destinationresourceid", "unknown"));
        amount.setText(String.valueOf(sharedPref.getInt("amount", 0)));
        mmOperator.setText(sharedPref.getString("mmo", "unknown"));
        subscriber.setText(sharedPref.getString("paymentreference", "unknown"));

        submit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otpValue.getText().toString().matches("")) {
                    Toast.makeText(self, getResources().getString(R.string.empty_reference_number), Toast.LENGTH_SHORT).show();
                } else {
                    if (com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)) {
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();
                        JSONObject cashoutData = new JSONObject();

                        try {
                            cashoutData.put("orginatingresourceid", sharedPref.getString("orginatingresourceid", "unknown"));
                            cashoutData.put("destinationresourceid", sharedPref.getString("destinationresourceid", "unknown"));
                            cashoutData.put("amount", sharedPref.getInt("amount", 0));
                            cashoutData.put("agentpassword", sharedPref.getString("agentPin", "unknown"));
                            cashoutData.put("mmo", sharedPref.getString("mmo", "unknown"));
                            cashoutData.put("paymentreference", sharedPref.getString("reference", "unknown"));
                            cashoutData.put("referencenumber", Integer.parseInt(otpValue.getText().toString()));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }

                        Intent intent = new Intent(self, BackgroundServices.class);
                        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.COMPLETE_CASHOUT_REQUEST);
                        intent.putExtra(Constants.JOB_DATA, cashoutData.toString());
                        startService(intent);
                        busy = true;
                    } else {
                        Toast.makeText(self, getResources().getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);
        return true;
    }

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
    public void onBackPressed() {
        if (busy) {
            Toast.makeText(self, getResources().getString(R.string.processing_request), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }
}
