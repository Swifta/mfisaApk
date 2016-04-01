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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.BillPaymentFragment;
import com.swifta.mats.LoginActivity;
import com.swifta.mats.R;
import com.swifta.mats.adapters.PreviewListAdapter;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.InternetCheck;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessServiceProviderActivity extends AppCompatActivity {
    private ProcessServiceProviderActivity self = this;
    private boolean busy = false;
    private String myName;

    private TextView title;
    private ScrollView container;
    private EditText serviceAmount;
    private EditText servicePhoneNumber;
    private EditText serviceAccNumber;
    private EditText serviceFirstname;
    private EditText serviceLastname;
    private EditText serviceDescription;
    private Button serviceSubmit;
    private ProgressDialog progressDialog;

    private ListView preview_list;
    private PreviewListAdapter adapter;

    private LinearLayout confirm_list;
    private LinearLayout confirmButtons;
    private Button confirmCancelButton;
    private Button confirm;

    String serviceAmountValue;
    String servicePhoneNumberValue;
    String serviceAccNumberValue;
    String serviceFirstnameValue;
    String serviceLastnameValue;
    String serviceDescriptionValue;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            progressDialog.hide();
            Bundle bundle = intent.getExtras();

            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.PAY_BILL_REQUEST) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("TransactionResponses")
                                .getJSONObject("TransactionResponse");
                        if (psaTranResponse.getString("responsemessage").equals(Constants.PAYBILL_REQUEST_WAS_SUCCESSFUL)) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                            dialog.setMessage(Constants.PAYBILL_REQUEST_WAS_SUCCESSFUL);
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
                                }
                            });
                            dialog.show();
                        } else {
                            String errorMessage = psaTranResponse.getString("responsemessage");

                            AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                            dialog.setMessage(getResources().getString(R.string.request_rejection_reason)
                                    + errorMessage.replace("_", " ")
                                    .toLowerCase());
                            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
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
        setContentView(R.layout.activity_process_service_provider);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        setTitle(myName);
        initEvents();
    }

    private void initEvents() {
        final String type = getIntent().getStringExtra("type");
        final String vendorId = getIntent().getStringExtra("vendorid");
        final String servicename = getIntent().getStringExtra("servicename");
        final String serviceamount = getIntent().getStringExtra("serviceamount");

        title = (TextView) findViewById(R.id.title);
        container = (ScrollView) findViewById(R.id.container);
        serviceAmount = (EditText) findViewById(R.id.service_amount);
        servicePhoneNumber = (EditText) findViewById(R.id.service_phone_number);
        serviceAccNumber = (EditText) findViewById(R.id.service_acc_number);
        serviceFirstname = (EditText) findViewById(R.id.service_firstname);
        serviceLastname = (EditText) findViewById(R.id.service_lastname);
        serviceDescription = (EditText) findViewById(R.id.service_description);

        preview_list = (ListView) findViewById(R.id.preview_list);

        confirm_list = (LinearLayout) findViewById(R.id.confirm_list);
        confirmButtons = (LinearLayout) findViewById(R.id.confirm_btns);
        confirmCancelButton = (Button) findViewById(R.id.confirm_cancel);
        confirm = (Button) findViewById(R.id.confirm);

        if (type.equals("telco")) {
            serviceFirstname.setVisibility(View.GONE);
            serviceLastname.setVisibility(View.GONE);
            serviceAmount.setVisibility(View.VISIBLE);
            serviceAccNumber.setVisibility(View.GONE);
        } else if (type.equals("cable")) {
            serviceAmount.setVisibility(View.GONE);
            serviceFirstname.setVisibility(View.VISIBLE);
            serviceLastname.setVisibility(View.VISIBLE);
            serviceAccNumber.setVisibility(View.VISIBLE);
        }
        serviceSubmit = (Button) findViewById(R.id.service_submit);
        progressDialog = new ProgressDialog(self);

        serviceSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (processEditText(type)) {

                    String[] left;
                    String[] right;
                    switch (type) {
                        case "telco":
                            // TODO Auto-generated method stub
                            left = new String[]{"Amount", "Phone Number", "Description"};
                            right = new String[]{servicePhoneNumberValue, servicePhoneNumberValue,
                                    serviceDescriptionValue};
                            adapter = new PreviewListAdapter(self, left, right);
                            preview_list.setAdapter(adapter);

                            container.setVisibility(View.GONE);
                            confirm_list.setVisibility(View.VISIBLE);
                            confirmButtons.setVisibility(View.VISIBLE);
                            title.setText(getResources().getString(R.string.confirm_bill_payment));
                            break;
                        case "cable":
                            // TODO Auto-generated method stub
                            left = new String[]{"Phone number", "Account Number", "First name", "Last name", "Description"};
                            right = new String[]{servicePhoneNumberValue, serviceAccNumberValue,
                                    serviceFirstnameValue, serviceLastnameValue, serviceDescriptionValue};
                            adapter = new PreviewListAdapter(self, left, right);
                            preview_list.setAdapter(adapter);

                            container.setVisibility(View.GONE);
                            confirm_list.setVisibility(View.VISIBLE);
                            confirmButtons.setVisibility(View.VISIBLE);
                            title.setText(getResources().getString(R.string.confirm_bill_payment));
                            break;
                    }
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (InternetCheck.isNetworkAvailable(self)) {
                    confirm_list.setVisibility(View.GONE);
                    confirmButtons.setVisibility(View.GONE);
                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        data.put("orginatingresourceid", myName.toLowerCase());
                        data.put("frommessage", serviceDescriptionValue.replace(" ", "%20"));
                        data.put("vendorid", vendorId);

                        if (type.equals("telco")) {
                            data.put("vendorservicename", "airtime");
                            data.put("amount", serviceAmountValue);
                            data.put("vendorparam1", "airtime");
                            data.put("vendorparam2", "payment");
                            data.put("vendoraccount", "12345678%7C" + servicePhoneNumberValue);
                        } else if (type.equals("cable")) {
                            data.put("vendorservicename", servicename.replace(" ", "%20"));
                            data.put("amount", serviceamount);
                            data.put("vendorparam1", serviceFirstnameValue);
                            data.put("vendorparam2", serviceLastnameValue);
                            data.put("vendoraccount", serviceAccNumberValue + "%7C" + servicePhoneNumberValue);
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(self, BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.PAYBILL_REQUEST);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(self, getResources().getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                }
            }
        });

        confirmCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm_list.setVisibility(View.GONE);
                confirmButtons.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean processEditText(String type) {
        serviceAmountValue = serviceAmount.getText().toString();
        servicePhoneNumberValue = servicePhoneNumber.getText().toString();
        serviceAccNumberValue = serviceAccNumber.getText().toString();
        serviceFirstnameValue = serviceFirstname.getText().toString();
        serviceLastnameValue = serviceLastname.getText().toString();
        serviceDescriptionValue = serviceDescription.getText().toString();

        /**  Telco ServiceProviders are pre-filled with vendorparam1 and vendorparam2 but need an amount
         *   Vendorparam1 will be pre-filled with "airtime"
         *   Vewndorparam2 will be pre-filled with "payment"
         *   CableTV ServiceProviders are pre-filled with amount but need the first-name and last-name
         **/

        if (serviceDescriptionValue.isEmpty() || servicePhoneNumberValue.isEmpty()) {

            if (type.equals("telco") && serviceAmountValue.isEmpty()) {
                Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_SHORT).show();
                return false;
            } else if (type.equals("cable") && (serviceLastnameValue.isEmpty() || serviceAccNumberValue.isEmpty() ||
                    serviceFirstnameValue.isEmpty())) {
                Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_SHORT).show();
                return false;
            }

            Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
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
