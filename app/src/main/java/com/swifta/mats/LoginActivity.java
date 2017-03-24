package com.swifta.mats;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.swifta.mats.service.BackgroundServices;
import com.swifta.mats.util.ApiJobs;
import com.swifta.mats.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private LoginActivity self = this;
    private Button signIn;
    private EditText usernameText;
    private EditText passwordText;
    private CheckBox showPassword;
    private ProgressDialog progressDialog;

    private boolean busy = false;
    private boolean status = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            try {
                Bundle bundle = intent.getExtras();

                JSONObject responseJson = new JSONObject(bundle.getString(Constants.JOB_RESPONSE, "{}"));

                JSONObject responseJson2 = responseJson.getJSONObject("TransactionResponses");
                JSONObject finalJson = responseJson2.getJSONObject("TransactionResponse");
                status = finalJson.getBoolean("responsemessage");

                if (status) {
                    SharedPreferences sharedPref = self.getSharedPreferences(Constants.STORE_USERNAME_KEY,
                            Context.MODE_PRIVATE);
                    Editor edit = sharedPref.edit();
                    edit.putString("username", usernameText.getText().toString());
                    edit.putString("password", passwordText.getText().toString());
                    edit.apply();

                    Toast.makeText(self, String.valueOf("Logged in as " + usernameText.getText().toString()),
                            Toast.LENGTH_LONG).show();

                    // Changes activity after saving the username and password
                    Intent actIntent = new Intent(self, MainActivity.class);
                    self.startActivity(actIntent);
                    self.finish();
                } else {
                    Toast.makeText(self, "Login unsuccessful. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(self, getResources().getString(R.string.retry_uncompleted_request), Toast.LENGTH_LONG).show();
            } finally {
                if (!status) {
                    progressDialog.hide();
                    busy = false;
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setting this makes sure we draw fullscreen
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        getSupportActionBar().hide();

        showPassword = (CheckBox) findViewById(R.id.show_pwd);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);

        progressDialog = new ProgressDialog(self);

        signIn = (Button) findViewById(R.id.sign_in);
        signIn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (usernameText.getText().toString().isEmpty() ||
                        passwordText.getText().toString().isEmpty()) {
                    Toast.makeText(self, getResources().getString(R.string.empty_credentials), Toast.LENGTH_LONG).show();
                } else {
                    if (com.swifta.mats.util.InternetCheck.isNetworkAvailable(self)) {
                        progressDialog.setMessage(getResources().getString(R.string.wait));
                        progressDialog.show();

                        JSONObject data = new JSONObject();
                        try {
                            data.put("username", usernameText.getText().toString());
                            data.put("password", passwordText.getText().toString());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(self, BackgroundServices.class);
                        intent.putExtra(Constants.JOB_IDENTITY, ApiJobs.LOGIN);
                        intent.putExtra(Constants.JOB_DATA, data.toString());
                        startService(intent);
                        busy = true;
                    } else {
                        Toast.makeText(self, getResources().getString(R.string.internet_connection_error), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        showPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (!isChecked) {
                    passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    passwordText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
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

    public void onBackPressed() {
        if (busy) {
            Toast.makeText(self, getResources().getString(R.string.processing_request), Toast.LENGTH_LONG).show();
        } else {
            finish();
        }
    }
}
