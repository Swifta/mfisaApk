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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.swifta.mats.LoginActivity;
import com.swifta.mats.R;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.Dealers;

import org.json.JSONException;
import org.json.JSONObject;

public class CashInActivity extends AppCompatActivity {

    private CashInActivity self = this;
    private String myName = "";
    private String myPassword = "";
    private boolean btn_clicked = true;

    private String operator;

    private EditText cashInAmount;
    private EditText cashInNumber;
    private EditText cashInDescription;
    private Button cashInButton;

    private ProgressDialog progressDialog;

    private boolean busy = false;

    private int amount = 0;
    private String mmo;
    private String paymentReference;
    private String description;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            progressDialog.hide();

            Bundle bundle = intent.getExtras();

            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.CASH_IN_COMPLETED) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.TRANSACTION_WAS_SUCCESSFUL)) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                            dialog.setMessage(Constants.TRANSACTION_WAS_SUCCESSFUL);
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            dialog.show();
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
        setContentView(R.layout.activity_cash_in);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getIntent().getExtras();
        operator = bundle.getString("dealer");
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
        cashInButton = (Button) findViewById(R.id.cash_in_pay);
        progressDialog = new ProgressDialog(self);

        cashInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                processMmo();
                if (processEditText()) {
                    if (com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)) {
                        progressDialog.setMessage(getResources().getString(R.string.wait));
                        progressDialog.show();

                        JSONObject data = new JSONObject();
                        try {
                            data.put("orginatingresourceid", myName.toLowerCase());
                            data.put("destinationresourceid", mmo);
                            data.put("amount", amount);
                            data.put("frommessage", description);
                            data.put("transactionid", Constants.TRANSACTION_ID);
                            data.put("mmo", mmo);
                            data.put("paymentreference", paymentReference);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(self, BackgroundServices.class);
                        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.CASH_IN_REQUEST);
                        intent.putExtra(Constants.JOB_DATA, data.toString());
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

    private void processMmo() {
        if (operator.equals(Dealers.READY_CASH.name())) {
            mmo = "readycash";
        }

        if (operator.equals(Dealers.FETS.name())) {
            mmo = "fets";
        }

        if (operator.equals(Dealers.TEASY_MOBILE.name())) {
            mmo = "teasymobile";
        }

        if (operator.equals(Dealers.PAGA.name())) {
            mmo = "pagatech";
        }

        if (operator.equals(Dealers.FORTIS.name())) {
            mmo = "fortis";
        }
    }

    private boolean processEditText() {

        cashInAmount = (EditText) findViewById(R.id.cash_in_amount);
        cashInNumber = (EditText) findViewById(R.id.subscriber_number);
        cashInDescription = (EditText) findViewById(R.id.cash_in_description);

        String amountValue = cashInAmount.getText().toString();
        paymentReference = cashInNumber.getText().toString();
        description = cashInDescription.getText().toString();

        if (amountValue.matches("") || paymentReference.matches("") || description.matches("")) {
            Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            amount = Integer.parseInt(amountValue);
            return true;
        }
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
