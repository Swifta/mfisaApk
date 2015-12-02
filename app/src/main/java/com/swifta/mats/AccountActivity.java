package com.swifta.mats;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {

    private AccountActivity self = this;
    private String username = "";
    private String newpassword = "";
    private String oldpassword = "";

    private TextView accountName;
    private EditText passwordText;
    private TextInputLayout passwordError;
    private Button changePassword;
    private Button submitPassword;
    private ProgressDialog progressDialog;
    private LinearLayout frame;

    private String status;
    private boolean busy = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            progressDialog.hide();

            try {
                Bundle bundle = intent.getExtras();

                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));
                JSONObject responseJson2 = responseJson.getJSONObject("TransactionResponses");
                JSONObject finalJson = responseJson2.getJSONObject("TransactionResponse");
                status = finalJson.getString("responsemessage");

                if (status.equals(Constants.PASSWORD_RESET_WAS_SUCCESSFUL)) {
                    SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putString("password", newpassword);
                    edit.apply();

                    // Clears the fields and changes display after saving the username and password successfully
                    passwordText.setText("");
                    frame.setVisibility(View.GONE);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                    dialog.setMessage(getResources().getString(R.string.successful_password_change) + newpassword);
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();

                    changePassword.setVisibility(View.VISIBLE);
                    busy = false;
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(self);
                    dialog.setMessage(getResources().getString(R.string.unsuccessful_password_change));
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
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
        setContentView(R.layout.activity_account);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initEvents();
    }

    private void initEvents() {
        accountName = (TextView) findViewById(R.id.account_name);
        changePassword = (Button) findViewById(R.id.change_password);
        passwordText = (EditText) findViewById(R.id.password_text);
        passwordError = (TextInputLayout) findViewById(R.id.password_error);
        submitPassword = (Button) findViewById(R.id.submit_password);
        frame = (LinearLayout) findViewById(R.id.form_frame);

        SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                Context.MODE_PRIVATE);
        username = sharedPref.getString("username", Constants.UNKNOWN);
        oldpassword = sharedPref.getString("password", Constants.UNKNOWN);

        accountName.setText(username);

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                changePassword.setVisibility(View.GONE);
                frame.setVisibility(View.VISIBLE);
                passwordError.setError("");
            }
        });

        submitPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processEditText()) {
                    if (com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)) {
                        progressDialog = new ProgressDialog(self);
                        progressDialog.setMessage("Changing password...");
                        progressDialog.show();

                        JSONObject data = new JSONObject();
                        try {
                            data.put("username", username);
                            data.put("newpassword", newpassword);
                            data.put("oldpassword", oldpassword);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(self, BackgroundServices.class);
                        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.CHANGE_PASSWORD);
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
        Intent actIntent = new Intent(self, LoginActivity.class);
        actIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(actIntent);
        finish();
    }

    /**
     * Gets the text from the password field
     */
    private boolean processEditText() {
        newpassword = passwordText.getText().toString();

        if (newpassword.matches("")) {
            passwordError.setError(getResources().getString(R.string.empty_credentials));
            return false;
        } else if (newpassword.length() < 5) {
            passwordError.setError(getResources().getString(R.string.short_password));
            return false;
        } else if (newpassword.length() > 30) {
            passwordError.setError(getResources().getString(R.string.long_password));
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
    public void onBackPressed() {
        if (busy) {
            Toast.makeText(self, getResources().getString(R.string.processing_request), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }
}
