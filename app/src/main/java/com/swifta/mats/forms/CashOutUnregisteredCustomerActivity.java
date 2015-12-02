package com.swifta.mats.forms;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.swifta.mats.util.InternetCheck;

import org.json.JSONException;
import org.json.JSONObject;

public class CashOutUnregisteredCustomerActivity extends AppCompatActivity {
    private CashOutUnregisteredCustomerActivity self = this;
    private String operator;
    private String myName;
    private String myPassword;
    private String mmo;
    private boolean busy = false;

    private String dealerIdValue;
    private String amountValue;
    private String redeemCodeValue;
    private String paymentReferenceValue;
    private String referenceNumberValue;

    private EditText dealerId;
    private EditText amount;
    private EditText redeemCode;
    private EditText paymentReference;
    private EditText referenceNumber;
    private Button cashOutButton;
    private ProgressDialog progressDialog;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.hide();
            Bundle bundle = intent.getExtras();

            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.UNREGISTERED_CASH_OUT) {
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
        setContentView(R.layout.activity_cash_out_unregistered_customer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getIntent().getExtras();
        operator = bundle.getString("dealer");
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPreferences.getString("username", Constants.UNKNOWN).toUpperCase();
        myPassword = sharedPreferences.getString("password", Constants.UNKNOWN);
        setTitle(myName);
        initEvents();
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
        cashOutButton = (Button) findViewById(R.id.cash_out_unregistered);
        progressDialog = new ProgressDialog(self);

        cashOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processMmo();
                if (processEditText()) {
                    if (InternetCheck.isNetworkAvailable(self)) {
                        progressDialog.setMessage(getResources().getString(R.string.wait));
                        progressDialog.show();

                        dealerIdValue = dealerId.getText().toString();
                        amountValue = amount.getText().toString();
                        redeemCodeValue = redeemCode.getText().toString();
                        paymentReferenceValue = paymentReference.getText().toString();
                        referenceNumberValue = referenceNumber.getText().toString();

                        JSONObject data = new JSONObject();
                        try {
                            data.put("orginatingresourceid", mmo);
                            data.put("destinationresourceid", dealerIdValue);
                            data.put("amount", amountValue);
                            data.put("redeemcode", redeemCodeValue);
                            data.put("mmo", mmo);
                            data.put("paymentreference", paymentReferenceValue);
                            data.put("referencenumber", referenceNumberValue);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(self, BackgroundServices.class);
                        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.CASH_OUT_UNREGISTERED_CUSTOMER);
                        intent.putExtra(Constants.JOB_DATA, data.toString());
                        startService(intent);
                        busy = true;
                    } else {
                        Toast.makeText(self, getResources().getString(R.string.internet_connection_error), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
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
        dealerId = (EditText) findViewById(R.id.cash_out_dealer);
        amount = (EditText) findViewById(R.id.cash_out_amount);
        redeemCode = (EditText) findViewById(R.id.redeem_code);
        paymentReference = (EditText) findViewById(R.id.payment_reference);
        referenceNumber = (EditText) findViewById(R.id.reference_number);

        dealerIdValue = dealerId.getText().toString();
        amountValue = amount.getText().toString();
        redeemCodeValue = redeemCode.getText().toString();
        paymentReferenceValue = paymentReference.getText().toString();
        referenceNumberValue = referenceNumber.getText().toString();

        if (dealerIdValue.matches("") || amountValue.matches("") || redeemCodeValue.matches("")
                || paymentReferenceValue.matches("") || referenceNumberValue.matches("")) {
            Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
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
