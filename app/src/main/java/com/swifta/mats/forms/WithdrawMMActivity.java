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

import com.swifta.mats.MainActivity;
import com.swifta.mats.R;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Contants;
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
    private LinearLayout confirm_btns;
    private LinearLayout loading;
    private LinearLayout done_btns;

    //all editText below
    private EditText subscriber_number_rc;
    private EditText amount_rc;
    private EditText token_rc;
    private EditText description_rc;
    private EditText subscriber_number_fets;
    private EditText amount_fets;
    private EditText pin_fets;
    private EditText description_fets;
    private EditText subscriber_number_tm;
    private EditText amount_tm;
    private EditText pin_tm;
    private EditText description_tm;
    private EditText subscriber_number_paga;
    private EditText amount_paga;
    private EditText code_paga;
    private EditText description_paga;
    //end of all editText

    private boolean busy = false;

    private String refCode = "";
    private int amount = 0;
    private String mmo = "";
    private String reference = "";
    private int teasyPin = 0;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                Bundle bundle = intent.getExtras();

                JSONObject responseJson = new JSONObject(bundle.getString(Contants.JOB_RESPONSE, "{}"));
                System.out.println(responseJson.toString());
                if (responseJson.getInt("request") == Contants.CASH_OUT_COMPLETED) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Contants.TRANSACTION_WAS_SUCCESSFUL)) {
                            //uiDisplaySummary();
                        } else {
                            Toast.makeText(self, "PSA Rejected Request : " + psaTranResponse.getString("responsemessage"), Toast.LENGTH_LONG).show();
                            showForm();
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        showForm();
                        Toast.makeText(self, "Request Failed : " + showReport, Toast.LENGTH_LONG).show();
                    }
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                showForm();
                e.printStackTrace();
                Toast.makeText(self, "Request cannot be completed, Try again", Toast.LENGTH_LONG).show();
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
        //Toast.makeText(this, "I see :"+operator, Toast.LENGTH_LONG).show();

        SharedPreferences sharedPref = self.getSharedPreferences(Contants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", "UNKNOWN").toUpperCase();
        myPassword = sharedPref.getString("password", "UNKNOWN");
        getSupportActionBar();
        setTitle("Welcome " + myName);
        initEvents();
        btn_clicked = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(Contants.SERVICE_NOTIFICATION));
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
                System.out.println("I got clicked");
                try {
                    processEditText();
                    hideForm();
                    loading.setVisibility(View.VISIBLE);
                    JSONObject data = new JSONObject();
                    try {
                        data.put("receiver", refCode);
                        data.put("amount", amount);
                        data.put("mmo", mmo);
                        data.put("reference", reference);
                        data.put("teasypin", teasyPin);
                        data.put("agentId", myName);
                        data.put("agentPin", myPassword);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(self, BackgroundServices.class);
                    intent.putExtra(Contants.JOB_IDENTITY, ApiJobs.WITHDRAWAL_DEALER_ACCOUNT);
                    intent.putExtra(Contants.JOB_DATA, data.toString());
                    startService(intent);
                    busy = true;
                } catch (Exception eX) {
                    eX.printStackTrace();
                    Toast.makeText(self, "Cannot process this kind of request", Toast.LENGTH_LONG).show();
                }
            }

        });

        cancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                self.onBackPressed();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

/*	@Override
	public void onDestroy(){
		super.onDestroy();
		if(!btn_clicked){
			logout();
		}
	}*/

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //System.out.println("Back was pressed with ID:"+item.getTitle());
        if (id == R.id.logout) {
            //logout clicked
            onLogoutPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onLogoutPressed() {
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

    @SuppressWarnings("unused")
    private void processEditText() throws Exception {
        subscriber_number_rc = (EditText) findViewById(R.id.subscriber_number_rc);
        amount_rc = (EditText) findViewById(R.id.amount_rc);
        token_rc = (EditText) findViewById(R.id.token_rc);
        description_rc = (EditText) findViewById(R.id.description_rc);
        subscriber_number_fets = (EditText) findViewById(R.id.subscriber_number_fets);
        amount_fets = (EditText) findViewById(R.id.amount_fets);
        pin_fets = (EditText) findViewById(R.id.pin_fets);
        description_fets = (EditText) findViewById(R.id.description_fets);
        subscriber_number_tm = (EditText) findViewById(R.id.subscriber_number_tm);
        amount_tm = (EditText) findViewById(R.id.amount_tm);
        pin_tm = (EditText) findViewById(R.id.pin_tm);
        description_tm = (EditText) findViewById(R.id.description_tm);
        subscriber_number_paga = (EditText) findViewById(R.id.subscriber_number_paga);
        amount_paga = (EditText) findViewById(R.id.amount_paga);
        code_paga = (EditText) findViewById(R.id.code_paga);
        description_paga = (EditText) findViewById(R.id.description_paga);

        if (operator.equals(Dealers.READY_CASH.name())) {
            refCode = subscriber_number_rc.getText().toString();
            amount = Integer.parseInt(amount_rc.getText().toString());
            mmo = "readycash";
            reference = description_rc.getText().toString();
            teasyPin = Integer.parseInt(token_rc.getText().toString());
        }
        if (operator.equals(Dealers.FETS.name())) {
            refCode = subscriber_number_fets.getText().toString();
            amount = Integer.parseInt(amount_fets.getText().toString());
            mmo = "fets";
            reference = description_fets.getText().toString();
            teasyPin = Integer.parseInt(pin_fets.getText().toString());
        }
        if (operator.equals(Dealers.TEASY_MOBILE.name())) {
            refCode = subscriber_number_tm.getText().toString();
            amount = Integer.parseInt(amount_tm.getText().toString());
            mmo = "teasymobile";
            reference = description_tm.getText().toString();
            teasyPin = Integer.parseInt(pin_tm.getText().toString());
        }
        if (operator.equals(Dealers.PAGA.name())) {
            refCode = subscriber_number_paga.getText().toString();
            amount = Integer.parseInt(amount_paga.getText().toString());
            mmo = "pagatech";
            reference = description_paga.getText().toString();
            teasyPin = Integer.parseInt(code_paga.getText().toString());
            ;
        }
        System.out.println("Ref:" + refCode + " and amount:" + amount + " and mmo:" + mmo + " and reference:" +
                reference + " and PIN:" + teasyPin);
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
        confirm_btns.setVisibility(View.GONE);
    }
}
