package com.swifta.mats.forms;

import android.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.LoginActivity;
import com.swifta.mats.R;
import com.swifta.mats.WithdrawalActivity;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.Dealers;

import org.json.JSONException;
import org.json.JSONObject;

public class WithdrawMMActivity extends AppCompatActivity {

    private WithdrawMMActivity self = this;
    private String myName = "";
    private String myPassword = "";
    private boolean btn_clicked = true;

    private Button okBtn;
    private Button cancelBtn;

    private TextView title;
    private String operator;

    private LinearLayout ready_cash;
    private LinearLayout fets;
    private LinearLayout teasy_mobile;
    private LinearLayout paga;
    private LinearLayout fortis;
    private LinearLayout confirm_btns;
    private LinearLayout loading;
    private LinearLayout done_btns;

    //all editText below
    private EditText dealerRc;
    private EditText subscriber_number_rc;
    private EditText amount_rc;
    private EditText token_rc;
    private EditText description_rc;
    private EditText dealerFets;
    private EditText subscriber_number_fets;
    private EditText amount_fets;
    private EditText pin_fets;
    private EditText description_fets;
    private EditText dealerTm;
    private EditText subscriber_number_tm;
    private EditText amount_tm;
    private EditText pin_tm;
    private EditText description_tm;
    private EditText dealerPaga;
    private EditText subscriber_number_paga;
    private EditText amount_paga;
    private EditText code_paga;
    private EditText description_paga;
    private EditText dealerFortis;
    private EditText subscriber_number_fortis;
    private EditText amount_fortis;
    private EditText code_fortis;
    private EditText description_fortis;
    //end of all editText

    private boolean busy = false;
    private boolean isFilled = true;

    private String subscriberNumber = "";
    private int amount = 0;
    private String mmo = "";
    private String reference = "";
    private int teasyPin = 0;
    private String dealerId;
    JSONObject data;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            loading.setVisibility(View.GONE);
            showForm();

            Bundle bundle = intent.getExtras();
            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.CASH_OUT_COMPLETED) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.CASHOUT_TRANSACTION_WAS_SUCCESSFUL)) {
                            Toast.makeText(self, "Transaction was successful. Please wait for your OTP to complete this process.", Toast.LENGTH_LONG).show();
                            showForm();

                            SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_CASHOUT_DATA,
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = sharedPref.edit();
                            edit.putString("orginatingresourceid", data.getString("agentId"));
                            edit.putString("destinationresourceid", data.getString("dealerId"));
                            edit.putInt("amount", data.getInt("amount"));
                            edit.putString("agentpassword", data.getString("agentPin"));
                            edit.putString("mmo", data.getString("mmo"));
                            edit.putString("paymentreference", data.getString("reference"));
                            edit.apply();

                            Intent i = new Intent(self, WithdrawalActivity.class);
                            startActivity(i);
                        } else {
                            String errorMessage = psaTranResponse.getString("responsemessage");
                            Toast.makeText(self, getResources().getString(R.string.request_rejection_reason)
                                    + errorMessage.replace("_", " ")
                                    .toLowerCase(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        Toast.makeText(self, "Request Failed : " + showReport, Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_withdraw_mm);
        Bundle bundle = this.getIntent().getExtras();
        operator = bundle.getString("dealer");
        title = (TextView) findViewById(R.id.title);
        title.setText(operator.replace("_", " ") + " [WITHDRAWAL]");

        processFormView();

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();
        myPassword = sharedPref.getString("password", Constants.UNKNOWN);
        getSupportActionBar();
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

    private void processFormView() {
        ready_cash = (LinearLayout) findViewById(R.id.ready_cash);
        fets = (LinearLayout) findViewById(R.id.fets);
        teasy_mobile = (LinearLayout) findViewById(R.id.teasy_mobile);
        paga = (LinearLayout) findViewById(R.id.paga);
        fortis = (LinearLayout) findViewById(R.id.fortis);
        confirm_btns = (LinearLayout) findViewById(R.id.confirm_btns);
        loading = (LinearLayout) findViewById(R.id.loading);
        done_btns = (LinearLayout) findViewById(R.id.done_btns);

        showForm();
    }

    private void initEvents() {
        okBtn = (Button) findViewById(R.id.ok_btn);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);

        okBtn.setOnClickListener(new OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         // TODO Auto-generated method stub
                                         processEditText();
                                         if (isFilled) {
                                             if (com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)) {
                                                 hideForm();
                                                 loading.setVisibility(View.VISIBLE);
                                                 data = new JSONObject();
                                                 try {
                                                     data.put("receiver", subscriberNumber);
                                                     data.put("amount", amount);
                                                     data.put("mmo", mmo);
                                                     data.put("reference", reference);
                                                     data.put("teasypin", teasyPin);
                                                     data.put("agentId", myName);
                                                     data.put("agentPin", myPassword);
                                                     data.put("dealerId", dealerId);
                                                 } catch (JSONException e) {
                                                     // TODO Auto-generated catch block
                                                     e.printStackTrace();
                                                 }
                                                 Intent intent = new Intent(self, BackgroundServices.class);
                                                 intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.WITHDRAWAL_DEALER_ACCOUNT);
                                                 intent.putExtra(Constants.JOB_DATA, data.toString());
                                                 startService(intent);
                                                 busy = true;
                                             } else {
                                                 Toast.makeText(self, getResources().getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                                             }
                                         } else {
                                             Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_LONG).show();
                                         }
                                     }
                                 }
        );

        cancelBtn.setOnClickListener(new OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             // TODO Auto-generated method stub
                                             self.onBackPressed();
                                         }
                                     }

        );
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
        if (id == R.id.logout) {
            //logout clicked
            onLogoutPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLogoutPressed() {
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

    @SuppressWarnings("unused")
    private void processEditText() {
        dealerRc = (EditText) findViewById(R.id.dealer_rc);
        subscriber_number_rc = (EditText) findViewById(R.id.subscriber_number_rc);
        amount_rc = (EditText) findViewById(R.id.amount_rc);
        token_rc = (EditText) findViewById(R.id.token_rc);
        description_rc = (EditText) findViewById(R.id.description_rc);
        dealerFets = (EditText) findViewById(R.id.dealer_fets);
        subscriber_number_fets = (EditText) findViewById(R.id.subscriber_number_fets);
        amount_fets = (EditText) findViewById(R.id.amount_fets);
        pin_fets = (EditText) findViewById(R.id.pin_fets);
        description_fets = (EditText) findViewById(R.id.description_fets);
        dealerTm = (EditText) findViewById(R.id.dealer_teasy);
        subscriber_number_tm = (EditText) findViewById(R.id.subscriber_number_tm);
        amount_tm = (EditText) findViewById(R.id.amount_tm);
        pin_tm = (EditText) findViewById(R.id.pin_tm);
        description_tm = (EditText) findViewById(R.id.description_tm);
        dealerPaga = (EditText) findViewById(R.id.dealer_paga);
        subscriber_number_paga = (EditText) findViewById(R.id.subscriber_number_paga);
        amount_paga = (EditText) findViewById(R.id.amount_paga);
        code_paga = (EditText) findViewById(R.id.code_paga);
        description_paga = (EditText) findViewById(R.id.description_paga);
        dealerFortis = (EditText) findViewById(R.id.dealer_fortis);
        subscriber_number_fortis = (EditText) findViewById(R.id.subscriber_number_fortis);
        amount_fortis = (EditText) findViewById(R.id.amount_fortis);
        code_fortis = (EditText) findViewById(R.id.code_fortis);
        description_fortis = (EditText) findViewById(R.id.description_fortis);

        if (operator.equals(Dealers.READY_CASH.name())) {
            if (dealerRc.getText().toString().isEmpty() || subscriber_number_rc.getText().toString().isEmpty() ||
                    description_rc.getText().toString().isEmpty() || token_rc.getText().toString().isEmpty()
                    || amount_rc.getText().toString().isEmpty()) {
                isFilled = false;
            } else {
                dealerId = dealerRc.getText().toString();
                subscriberNumber = subscriber_number_rc.getText().toString();
                amount = Integer.parseInt(amount_rc.getText().toString());
                mmo = "readycash";
                reference = description_rc.getText().toString();
                teasyPin = Integer.parseInt(token_rc.getText().toString());
                isFilled = true;
            }
        }
        if (operator.equals(Dealers.FETS.name())) {
            if (dealerFets.getText().toString().isEmpty() || subscriber_number_fets.getText().toString().isEmpty() ||
                    amount_fets.getText().toString().isEmpty() || description_fets.getText().toString().isEmpty()
                    || pin_fets.getText().toString().isEmpty()) {
                isFilled = false;
            } else {
                dealerId = dealerFets.getText().toString();
                subscriberNumber = subscriber_number_fets.getText().toString();
                amount = Integer.parseInt(amount_fets.getText().toString());
                mmo = "fets";
                reference = description_fets.getText().toString();
                teasyPin = Integer.parseInt(pin_fets.getText().toString());
                isFilled = true;
            }
        }
        if (operator.equals(Dealers.TEASY_MOBILE.name())) {
            if (dealerTm.getText().toString().isEmpty() || subscriber_number_tm.getText().toString().isEmpty() ||
                    amount_tm.getText().toString().isEmpty() || description_tm.getText().toString().isEmpty()
                    || pin_tm.getText().toString().isEmpty()) {
                isFilled = false;
            } else {
                dealerId = dealerTm.getText().toString();
                subscriberNumber = subscriber_number_tm.getText().toString();
                amount = Integer.parseInt(amount_tm.getText().toString());
                mmo = "teasymobile";
                reference = description_tm.getText().toString();
                teasyPin = Integer.parseInt(pin_tm.getText().toString());
                isFilled = true;
            }
        }
        if (operator.equals(Dealers.PAGA.name())) {
            if (dealerPaga.getText().toString().isEmpty() || subscriber_number_paga.getText().toString().isEmpty() ||
                    amount_paga.getText().toString().isEmpty() || description_paga.getText().toString().isEmpty()
                    || code_paga.getText().toString().isEmpty()) {
                isFilled = false;
            } else {
                dealerId = dealerPaga.getText().toString();
                subscriberNumber = subscriber_number_paga.getText().toString();
                amount = Integer.parseInt(amount_paga.getText().toString());
                mmo = "pagatech";
                reference = description_paga.getText().toString();
                teasyPin = Integer.parseInt(code_paga.getText().toString());
                isFilled = true;
            }
        }
        if (operator.equals(Dealers.FORTIS.name())) {

            if (dealerFortis.getText().toString().isEmpty() || subscriber_number_fortis.getText().toString().isEmpty() ||
                    amount_fortis.getText().toString().isEmpty() || description_fortis.getText().toString().isEmpty()
                    || code_fortis.getText().toString().isEmpty()) {
                isFilled = false;
            } else {
                dealerId = dealerFortis.getText().toString();
                subscriberNumber = subscriber_number_fortis.getText().toString();
                amount = Integer.parseInt(amount_fortis.getText().toString());
                mmo = "fortis";
                reference = description_fortis.getText().toString();
                teasyPin = Integer.parseInt(code_fortis.getText().toString());
                isFilled = true;
            }
        }
    }

    private void showForm() {
        if (operator.equals(Dealers.READY_CASH.name())) {
            ready_cash.setVisibility(View.VISIBLE);
        }
        if (operator.equals(Dealers.FETS.name())) {
            fets.setVisibility(View.VISIBLE);
        }
        if (operator.equals(Dealers.TEASY_MOBILE.name())) {
            teasy_mobile.setVisibility(View.VISIBLE);
        }
        if (operator.equals(Dealers.PAGA.name())) {
            paga.setVisibility(View.VISIBLE);
        }
        if (operator.equals(Dealers.FORTIS.name())) {
            fortis.setVisibility(View.VISIBLE);
        }
        confirm_btns.setVisibility(View.VISIBLE);
    }

    private void hideForm() {
        if (operator.equals(Dealers.READY_CASH.name())) {
            ready_cash.setVisibility(View.GONE);
        }
        if (operator.equals(Dealers.FETS.name())) {
            fets.setVisibility(View.GONE);
        }
        if (operator.equals(Dealers.TEASY_MOBILE.name())) {
            teasy_mobile.setVisibility(View.GONE);
        }
        if (operator.equals(Dealers.PAGA.name())) {
            paga.setVisibility(View.GONE);
        }
        if (operator.equals(Dealers.FORTIS.name())) {
            fortis.setVisibility(View.GONE);
        }
        confirm_btns.setVisibility(View.GONE);
    }
}
