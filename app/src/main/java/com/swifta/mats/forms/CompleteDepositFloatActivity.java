package com.swifta.mats.forms;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.swifta.mats.LoginActivity;
import com.swifta.mats.R;
import com.swifta.mats.adapters.PreviewListAdapter;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class CompleteDepositFloatActivity extends AppCompatActivity {

    private CompleteDepositFloatActivity self = this;
    private String myName = "";
    private String myPassword = "";
    private boolean btn_clicked = true;

    private boolean busy = false;

    private SharedPreferences sharedPref;
    private boolean canClear = false;

    private LinearLayout otp_layout;
    private LinearLayout loading;
    private LinearLayout tranx_success_list;
    private LinearLayout confirm_btns;
    private LinearLayout back_btns;

    private Button confirmBtn;
    private Button backBtn;
    private Button back_doneBtn;

    private ListView preview_list;
    private PreviewListAdapter adapter;

    private EditText otpTxt;

    private ListView success_list;
    private PreviewListAdapter successAdapter;

    private JSONObject resumedData;

    private int transaction_id = 0;
    private String dealer = "";
    private int amount = 0;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                Bundle bundle = intent.getExtras();

                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                System.out.println(responseJson.toString());
                if (responseJson.getInt("request") == Constants.OTP_COMPLETE_REQUEST) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.TRANSACTION_WAS_SUCCESSFUL)) {
                            uiDisplaySummary();
                        } else {
                            String errorMessage = psaTranResponse.getString("responsemessage");
                            Toast.makeText(self, getResources().getString(R.string.request_rejection_reason) + errorMessage.replace("_", " ")
                                    .toLowerCase(), Toast.LENGTH_LONG).show();
                            reEnterOTP();
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        reEnterOTP();
                        Toast.makeText(self, "Your request failed : " + showReport, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                reEnterOTP();
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
        setContentView(R.layout.activity_complete_deposit_float);
        sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();
        myPassword = sharedPref.getString("password", Constants.UNKNOWN);
        try {
            resumedData = new JSONObject(sharedPref.getString(Constants.TMP_DEPOSIT_FLOAT_DATA, "{}"));
            if (resumedData.has("transaction_id")) {
                transaction_id = resumedData.getInt("transaction_id");
            }
            if (resumedData.has("dealer")) {
                dealer = resumedData.getString("dealer");
            }
            if (resumedData.has("amount")) {
                amount = resumedData.getInt("amount");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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

    public void onBackPressed() {
        // do something here and don't write super.onBackPressed()
        if (busy) {
            Toast.makeText(self, getResources().getString(R.string.processing_request), Toast.LENGTH_LONG).show();
        } else {
            //super.onBackPressed();
            //clear all the stored cache of uncompleted float transfer.
            if (canClear) {
                Editor edit = sharedPref.edit();
                edit.remove(Constants.TMP_DEPOSIT_FLOAT_DATA);
                edit.apply();
            }
            finish();
        }
    }

    private void initEvents() {
        otp_layout = (LinearLayout) findViewById(R.id.otp_layout);
        loading = (LinearLayout) findViewById(R.id.loading);
        tranx_success_list = (LinearLayout) findViewById(R.id.tranx_success_list);
        confirm_btns = (LinearLayout) findViewById(R.id.confirm_btns);
        back_btns = (LinearLayout) findViewById(R.id.back_btns);

        confirmBtn = (Button) findViewById(R.id.confirm);
        back_doneBtn = (Button) findViewById(R.id.back_done);
        backBtn = (Button) findViewById(R.id.back);

        otpTxt = (EditText) findViewById(R.id.otp);

        preview_list = (ListView) findViewById(R.id.preview_list);
        success_list = (ListView) findViewById(R.id.success_list);
        String left[] = {"Transaction ID", "Dealer", "Amount"};
        String right[] = {String.valueOf(transaction_id), dealer, String.valueOf(amount)};
        adapter = new PreviewListAdapter(self, left, right);
        preview_list.setAdapter(adapter);

        confirmBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //attempt to use the data to communicate with psa
                if (com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)) {
                    otp_layout.setVisibility(View.GONE);
                    confirm_btns.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", myName);
                        data.put("password", myPassword);
                        data.put("dealer", dealer);
                        data.put("amount", amount);
                        data.put("transaction_id", transaction_id);
                        data.put("otp", otpTxt.getText().toString());
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(self, BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.COMPLETE_DEPOSIT_FLOAT);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(self, getResources().getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                }
            }
        });

        backBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onBackPressed();
            }

        });

        back_doneBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onBackPressed();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
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
        if (busy) {
            Toast.makeText(self, getResources().getString(R.string.processing_request), Toast.LENGTH_LONG).show();
        } else {
            Intent actIntent = new Intent(self, LoginActivity.class);
            actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(actIntent);
            finish();
        }
    }

    private void reEnterOTP() {
        otp_layout.setVisibility(View.VISIBLE);
        confirm_btns.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void uiDisplaySummary() {
        canClear = true;
        // Displays successful status.
        String left[] = {""};
        String right[] = {"TRANSACTION SUCCESSFUL"};
        successAdapter = new PreviewListAdapter(self, left, right);
        success_list.setAdapter(successAdapter);

        otp_layout.setVisibility(View.GONE);
        confirm_btns.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        tranx_success_list.setVisibility(View.VISIBLE);
        back_btns.setVisibility(View.VISIBLE);
    }
}
