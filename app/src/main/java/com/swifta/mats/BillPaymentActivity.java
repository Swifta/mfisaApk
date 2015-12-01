package com.swifta.mats;

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
import android.widget.Toast;

import com.swifta.mats.forms.ProcessServiceProviderActivity;
import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;
import com.swifta.mats.util.InternetCheck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BillPaymentActivity extends AppCompatActivity {

    private BillPaymentActivity self = this;
    private boolean busy = false;
    private String myName;
    private Button dstvButton;
    private Button gotvButton;
    private Button mtn;

    private ProgressDialog progressDialog;
    public static String vendorId;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.hide();
            Bundle bundle = intent.getExtras();
            try {
                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                if (responseJson.getInt("request") == Constants.GET_SERVICE_PROVIDER_DETAILS) {
                    if (responseJson.getBoolean("success")) {
                        JSONObject psaResponse = responseJson.getJSONObject("psa");
                        JSONObject psaTranResponse = psaResponse.getJSONObject("getserviceproviderdetails");
                        Object json = psaTranResponse.get("getserviceproviderdetail");

                        // If the service provider is a telco, go straight to the form
                        if (json instanceof JSONObject) {
                            Intent objectIntent = new Intent(self, ProcessServiceProviderActivity.class);
                            objectIntent.putExtra("type", "telco");
                            objectIntent.putExtra("vendorid", vendorId);
                            objectIntent.putExtra("servicename", psaTranResponse.getJSONObject("getserviceproviderdetail").getString("servicename"));
                            startActivity(objectIntent);
                        } else if (json instanceof JSONArray) {
                            // If not, open up a list of searchable service names with prefixed prices
                            Intent newIntent = new Intent(self, ServiceProviderDetailsActivity.class);
                            newIntent.putExtra("data", json.toString());
                            newIntent.putExtra("vendorid", vendorId);
                            startActivity(newIntent);
                        }
                    } else {
                        String showReport = responseJson.getString("message");
                        Toast.makeText(self, "Request failed : " + showReport, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                Toast.makeText(self, getResources().getString(R.string.retry_uncompleted_request), Toast.LENGTH_LONG).show();
            } finally {
                busy = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        myName = sharedPref.getString("username", Constants.UNKNOWN).toUpperCase();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(myName);
        initEvents();
    }

    private void initEvents() {
        dstvButton = (Button) findViewById(R.id.dstv);
        gotvButton = (Button) findViewById(R.id.gotv);
        mtn = (Button) findViewById(R.id.mtn);
        progressDialog = new ProgressDialog(self);

        dstvButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(self)) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.DSTV_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(self, BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(self, getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        gotvButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(self)) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.GOTV_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(self, BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(self, getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (InternetCheck.isNetworkAvailable(self)) {

                    progressDialog.setMessage(getResources().getString(R.string.wait));
                    progressDialog.show();

                    JSONObject data = new JSONObject();
                    try {
                        vendorId = Constants.MTN_VENDOR_ID;
                        data.put(Constants.VENDOR_ID, vendorId);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(self, BackgroundServices.class);
                    intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.GET_SERVICE_PROVIDER_DETAILS);
                    intent.putExtra(Constants.JOB_DATA, data.toString());
                    startService(intent);
                    busy = true;
                } else {
                    Toast.makeText(self, getResources().getString(R.string.internet_connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
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
