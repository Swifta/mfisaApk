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
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.MainActivity;
import com.swifta.mats.R;
import com.swifta.mats.adapters.PreviewListAdapter;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class DepositFloatActivity extends AppCompatActivity {

    private DepositFloatActivity self = this;
    private String myName = "";
    private String myPassword = "";
    private boolean btn_clicked = true;

    private ListView preview_list;
    private PreviewListAdapter adapter;

    private ListView success_list;
    private PreviewListAdapter successAdapter;

    private Button okBtn;
    private Button cancelBtn;
    private Button confirmBtn;
    private Button confirm_cancelBtn;
    private Button okay_doneBtn;
    private Button back_done;

    private EditText amount;
    private EditText dealer_id;
    private EditText description;

    private EditText otpTxt;

    private LinearLayout form;
    private LinearLayout btns;
    private LinearLayout confirm_list;
    private LinearLayout confirm_btns;
    private LinearLayout done_btns;
    private LinearLayout loading;
    private LinearLayout otp_layout;
    private LinearLayout back_btns;
    private LinearLayout tranx_success_list;

    private TextView title;
    private boolean busy = false;

    private SharedPreferences sharedPref;
    private boolean canClear = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                Bundle bundle = intent.getExtras();
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));

                if (responseJson.getInt("request") == Constants.OTP_REQUEST) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.FLOAT_TRANSFER_REQUEST_TOKEN_SUCCESS)) {
                            // means PSA confirmed that the process was successful
                            // then write the transaction ID in memory.
                            int transaction_id = psaTranResponse.getInt("TransactionId");
                            uiHandleSuccess(transaction_id);
                        } else {
                            String errorMessage = psaTranResponse.getString("responsemessage");
                            Toast.makeText(self, "Your request was rejected because " + errorMessage.replace("_", " ")
                                    .toLowerCase(), Toast.LENGTH_LONG).show();
                            uiHandleFailed();
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        uiHandleFailed();
                        Toast.makeText(self, "Your request failed: " + showReport, Toast.LENGTH_LONG).show();
                    }
                } else if (responseJson.getInt("request") == Constants.OTP_COMPLETE_REQUEST) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.TRANSACTION_WAS_SUCCESSFUL)) {
                            uiDisplaySummary();
                        } else {
                            String errorMessage = psaTranResponse.getString("responsemessage");
                            Toast.makeText(self, "Your request was rejected because " + errorMessage.replace("_", " ")
                                    .toLowerCase(), Toast.LENGTH_LONG).show();
                            reEnterOTP();
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        reEnterOTP();
                        Toast.makeText(self, "Your request failed: " + showReport, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                uiHandleFailed();
                e.printStackTrace();
                Toast.makeText(self, "Your request cannot be completed, please try again.", Toast.LENGTH_LONG).show();
            } finally {
                busy = false;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_float);
        sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", "UNKNOWN").toUpperCase();
        myPassword = sharedPref.getString("password", "UNKNOWN");
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
            Toast.makeText(self, "Currently processing a request. Please wait..", Toast.LENGTH_LONG).show();
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
        form = (LinearLayout) findViewById(R.id.form);
        btns = (LinearLayout) findViewById(R.id.btns);
        confirm_list = (LinearLayout) findViewById(R.id.confirm_list);
        confirm_btns = (LinearLayout) findViewById(R.id.confirm_btns);
        done_btns = (LinearLayout) findViewById(R.id.done_btns);
        loading = (LinearLayout) findViewById(R.id.loading);
        otp_layout = (LinearLayout) findViewById(R.id.otp_layout);
        back_btns = (LinearLayout) findViewById(R.id.back_btns);
        tranx_success_list = (LinearLayout) findViewById(R.id.tranx_success_list);

        okBtn = (Button) findViewById(R.id.ok);
        cancelBtn = (Button) findViewById(R.id.cancel);
        confirmBtn = (Button) findViewById(R.id.confirm);
        confirm_cancelBtn = (Button) findViewById(R.id.confirm_cancel);
        okay_doneBtn = (Button) findViewById(R.id.okay_done);
        back_done = (Button) findViewById(R.id.back_done);

        preview_list = (ListView) findViewById(R.id.preview_list);
        success_list = (ListView) findViewById(R.id.success_list);

        amount = (EditText) findViewById(R.id.amount);
        dealer_id = (EditText) findViewById(R.id.dealer);
        otpTxt = (EditText) findViewById(R.id.otp);
        description = (EditText) findViewById(R.id.description);

        title = (TextView) findViewById(R.id.title);

        okBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (amount.getText().toString().isEmpty() || dealer_id.getText().toString().isEmpty()
                        || amount.getText().toString().isEmpty()) {
                    Toast.makeText(self, "Please fill in the correct details.", Toast.LENGTH_LONG).show();
                } else {
                    // TODO Auto-generated method stub
                    String left[] = {"Amount", "Dealer ID", "Description"};
                    String right[] = {amount.getText().toString(), dealer_id.getText().toString(),
                            description.getText().toString()};
                    //adapter.setList(left, right);
                    //adapter.notifyDataSetChanged();
                    adapter = new PreviewListAdapter(self, left, right);
                    preview_list.setAdapter(adapter);

                    form.setVisibility(View.GONE);
                    btns.setVisibility(View.GONE);
                    done_btns.setVisibility(View.GONE);
                    loading.setVisibility(View.GONE);
                    confirm_list.setVisibility(View.VISIBLE);
                    confirm_btns.setVisibility(View.VISIBLE);
                    title.setText("Confirm Float Transfer Transaction");
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

        confirmBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                confirm_list.setVisibility(View.GONE);
                confirm_btns.setVisibility(View.GONE);
                loading.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                JSONObject data = new JSONObject();
                try {
                    data.put("username", myName);
                    data.put("password", myPassword);
                    data.put("amount", amount.getText().toString());
                    data.put("dealer", dealer_id.getText().toString());
                    data.put("description", description.getText().toString());
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Intent intent = new Intent(self, BackgroundServices.class);
                intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.DEPOSIT_FLOAT);
                intent.putExtra(Constants.JOB_DATA, data.toString());
                startService(intent);
                busy = true;
            }

        });

        confirm_cancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                form.setVisibility(View.VISIBLE);
                btns.setVisibility(View.VISIBLE);
                done_btns.setVisibility(View.GONE);
                confirm_list.setVisibility(View.GONE);
                confirm_btns.setVisibility(View.GONE);
                title.setText("Deposit Float");
            }

        });

        okay_doneBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //communicate with the background service
                otp_layout.setVisibility(View.GONE);
                done_btns.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                JSONObject data = new JSONObject();
                try {
                    JSONObject dt = new JSONObject(sharedPref.getString(Constants.TMP_DEPOSIT_FLOAT_DATA,
                            "{}"));
                    data.put("username", myName);
                    data.put("password", myPassword);
                    data.put("dealer", dt.getString("dealer"));
                    data.put("amount", dt.getInt("amount"));
                    data.put("transaction_id", dt.getInt("transaction_id"));
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
            }

        });

        back_done.setOnClickListener(new OnClickListener() {

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
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

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
        if (busy) {
            Toast.makeText(self, "Currently processing a request. Please wait...", Toast.LENGTH_LONG).show();
        } else {
            Intent actIntent = new Intent(self, MainActivity.class);
            actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(actIntent);
            finish();
        }
    }

    private void uiHandleFailed() {
        confirm_list.setVisibility(View.VISIBLE);
        confirm_btns.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void reEnterOTP() {
        otp_layout.setVisibility(View.VISIBLE);
        done_btns.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void uiHandleSuccess(int transaction_id) {
        //store or replace value of dealer, amount and transacion ID to be pushed when
        //okay is pressed and user want to complete previous request
        try {
            Editor edit = sharedPref.edit();
            JSONObject data = new JSONObject();
            data.put("dealer", dealer_id.getText().toString());
            data.put("amount", amount.getText().toString());
            data.put("transaction_id", transaction_id);
            edit.putString(Constants.TMP_DEPOSIT_FLOAT_DATA, data.toString());
            edit.apply();
            TextView transaction_idTxt = (TextView) findViewById(R.id.transaction_id);
            title.setText("Dealer OTP Confirmation");
            transaction_idTxt.setText("Transaction ID: " + transaction_id);
            otp_layout.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
            done_btns.setVisibility(View.VISIBLE);
        } catch (JSONException jE) {
            jE.printStackTrace();
            Toast.makeText(self, "Data issue : " + jE.getMessage(), Toast.LENGTH_LONG).show();
            uiHandleFailed();
        }
    }

    private void uiDisplaySummary() {
        canClear = true;
        //the display of successful status.
        String left[] = {"Status : "};
        String right[] = {"TRANSACTION SUCCESSFUL"};
        successAdapter = new PreviewListAdapter(self, left, right);
        success_list.setAdapter(successAdapter);

        otp_layout.setVisibility(View.GONE);
        done_btns.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        tranx_success_list.setVisibility(View.VISIBLE);
        back_btns.setVisibility(View.VISIBLE);

    }
}
